package com.cyjh.rcs.service;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.media.MediaCodec;
import android.media.MediaCodec.BufferInfo;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.text.TextUtils;
import android.view.InputEvent;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.Surface;

import com.cyjh.library.Command;
import com.cyjh.library.Keyboard;
import com.cyjh.library.Packet;
import com.cyjh.library.Touchpad;
import com.cyjh.library.Utilities;

@SuppressLint("Recycle")
public class ExecuteRemoteController implements Handler.Callback {

    private class SubThread extends Thread {

        public SubThread(String name) {
            super(name);
        }

        @Override
        public void run() {
            String name = getName();
            if (TextUtils.equals(name, THREAD_TCP_SERVICE)) {
                onTcpServiceRun();
            } else if (TextUtils.equals(name, THREAD_UDP_SERVICE)) {
                onUdpServiceRun();
            } else if (TextUtils.equals(name, THREAD_LIVE_CHECK)) {
                onLiveCheckRun();
            } else if (TextUtils.equals(name, THREAD_SCREEN_CAPTURE)) {
                onScreenCaptureRun();
            } else if (TextUtils.equals(name, THREAD_VIDEO_OUTPUT)) {
                onVideoOutputRun();
            } else if (TextUtils.equals(name, THREAD_INPUT_INJECT)) {
                onInputInjectRun();
            } else if (TextUtils.equals(name, THREAD_COMMAND_PROCESS)) {
                onCommandProcessRun();
            }
        }

    }

    private static final byte TYPE_VIDEO = (byte) 200;
    private static final byte TYPE_KEY = (byte) 202;
    private static final byte TYPE_TOUCH = (byte) 201;

    private static final int MESSAGE_KEY_INJECT = 1;
    private static final int MESSAGE_TOUCH_INJECT = 2;
    private static final int MESSAGE_SCREEN_CAPTURE = 0;

    private static final byte PACK_STRUCT_ENUM_CLIENT_IDLE = 0;
    private static final byte PACK_STRUCT_ENUM_CLIENT_BUSY = 1;
    private static final byte PACK_STRUCT_ENUM_IMAGE = 99;
    private static final byte PACK_STRUCT_ENUM_SERVER_IDLE = 2;
    private static final byte PACK_STRUCT_ENUM_SERVER_BUSY = 3;

    public static final String SYS_RCS_CODEC_QUALITY = "sys.rcs.codec.quality";
    public static final String SYS_RCS_LANDSCAPE_HEIGHT = "sys.rcs.landscape.height";
    public static final String SYS_RCS_LANDSCAPE_WIDTH = "sys.rcs.landscape.width";
    public static final String SYS_RCS_LOG_ENABLE = "sys.rcs.log.enable";
    public static final String SYS_RCS_PORTRAIT_HEIGHT = "sys.rcs.portrait.height";
    public static final String SYS_RCS_PORTRAIT_WIDTH = "sys.rcs.portrait.width";
    public static final String SYS_RCS_SCREEN_ORIENTATION = "sys.rcs.screen.orientation";
    public static final String SYS_RCS_VERSION = "sys.rcs.version";

    private static final String THREAD_COMMAND_PROCESS = "THREAD_COMMAND_PROCESS";
    private static final String THREAD_INPUT_INJECT = "THREAD_INPUT_INJECT";
    private static final String THREAD_LIVE_CHECK = "THREAD_LIVE_CHECK";
    private static final String THREAD_SCREEN_CAPTURE = "THREAD_SCREEN_CAPTURE";
    private static final String THREAD_TCP_SERVICE = "THREAD_TCP_SERVICE";
    private static final String THREAD_UDP_SERVICE = "THREAD_UDP_SERVICE";
    private static final String THREAD_VIDEO_OUTPUT = "THREAD_VIDEO_OUTPUT";

    private static final int TYPE_KEY_DOWN = 1;
    private static final int TYPE_KEY_TYPE = 3;
    private static final int TYPE_KEY_UP = 2;
    private static final int TYPE_TOUCH_DOWN = 1;
    private static final int TYPE_TOUCH_MOVE = 3;
    private static final int TYPE_TOUCH_UP = 2;

    private static final Keyboard mKeyboard = new Keyboard();
    private static final Touchpad mTouchpad = new Touchpad();

    public static void main(String[] args) throws Exception {
        Looper.prepare();
        new ExecuteRemoteController();
        Looper.loop();
    }

    private final Class<?> mClassInputManager;
    private final Class<?> mClassSurfaceControl;
    private final Method mMethodInjectInputEvent;
    private final Method mMethodScreenshotToBitmap;
    private final Method mMethodScreenshotToSurface;
    private final Object mValueDefaultDisplay;
    private final Object mValueInjectInputEventMode;
    private final Object mValueInputManagerInstance;
    
    private int mCountOutputBytes;
    private int mCountScreenshot;
    private final Handler mHandler;
    private boolean mHasClient;
    private DataInputStream mStreamInput;
    private DataOutputStream mStreamOutput;
    private long mLastTimeHeartbeat;
    private MediaCodec mMediaCodec;
    private int mScreenLandscapeHeight;
    private int mScreenLandscapeWidth;
    private int mScreenOrientation;
    private int mScreenPortraitHeight;
    private int mScreenPortraitWidth;
    private byte[] mScreenshotImage = new byte[0];
    private Surface mSurface;

    private int mVideoQuality = 100;
    private String mVersion = "0.0.0.0";

    private ExecuteRemoteController() throws Exception {
        mHandler = new Handler(this);

        {
            mClassSurfaceControl = Class.forName("android.view.SurfaceControl");
            mMethodScreenshotToBitmap = mClassSurfaceControl.getMethod("screenshot", Integer.TYPE, Integer.TYPE);
            mMethodScreenshotToSurface = mClassSurfaceControl.getMethod("screenshot", IBinder.class, Surface.class, Integer.TYPE, Integer.TYPE);
            mValueDefaultDisplay = mClassSurfaceControl.getMethod("getBuiltInDisplay", Integer.TYPE).invoke(null, 0);
        }

        {
            mClassInputManager = Class.forName("android.hardware.input.InputManager");
            mMethodInjectInputEvent = mClassInputManager.getMethod("injectInputEvent", InputEvent.class, Integer.TYPE);
            mValueInjectInputEventMode = mClassInputManager.getField("INJECT_INPUT_EVENT_MODE_ASYNC").get(null);
            mValueInputManagerInstance = mClassInputManager.getMethod("getInstance").invoke(null);
        }

        new SubThread(THREAD_TCP_SERVICE).start();
        new SubThread(THREAD_UDP_SERVICE).start();
        new SubThread(THREAD_COMMAND_PROCESS).start();
    }

    @Override
    public boolean handleMessage(Message message) {
        switch (message.what) {
        case MESSAGE_SCREEN_CAPTURE: {
            try {
                int width = mScreenLandscapeWidth / 8;
                int height = mScreenLandscapeHeight / 8;
                Utilities.log(this, "create screenshot %dx%d ...", width, height);
                Bitmap bitmap = (Bitmap) mMethodScreenshotToBitmap.invoke(null, width, height);
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                bitmap.compress(CompressFormat.JPEG, 100, out);
                out.close();
                mScreenshotImage = out.toByteArray();
                Utilities.log(this, "screenshot size: %dx%d %d", bitmap.getWidth(), bitmap.getHeight(), mScreenshotImage.length);
            } catch (Exception e) {
                Utilities.log(this, e, "create screenshot failed");
            }
            mHandler.sendEmptyMessageDelayed(MESSAGE_SCREEN_CAPTURE, 60000);
            break;
        }
        case MESSAGE_KEY_INJECT: {
            try {
                KeyEvent event = (KeyEvent) message.obj;
                Object result = mMethodInjectInputEvent.invoke(mValueInputManagerInstance, event, mValueInjectInputEventMode);
                Utilities.log(this, "inject %s return %s", event, result);
            } catch (Exception e) {
                Utilities.log(this, e, "inject event failed");
            }
            break;
        }
        case MESSAGE_TOUCH_INJECT: {
            try {
                MotionEvent event = (MotionEvent) message.obj;
                Object result = mMethodInjectInputEvent.invoke(mValueInputManagerInstance, event, mValueInjectInputEventMode);
                event.recycle();
                Utilities.log(this, "inject %s return %s", event, result);
            } catch (Exception e) {
                Utilities.log(this, e, "inject event failed");
            }
            break;
        }
        default:
            break;
        }
        return false;
    }

    private void onCommandProcessRun() {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        try {
            for (String line = in.readLine(); line != null; line = in.readLine()) {
                Utilities.log(this, line);
                Command command = Command.parse(line);
                if (TextUtils.equals(command.getName(), "set")) {
                    for (String key : command.keys()) {
                        String value = command.get(key);
                        Utilities.log(this, "set property: %s as %s", key, value);
                        if (TextUtils.equals(SYS_RCS_LANDSCAPE_WIDTH, key)) {
                            mScreenLandscapeWidth = Integer.parseInt(value);
                        } else if (TextUtils.equals(SYS_RCS_LANDSCAPE_HEIGHT, key)) {
                            mScreenLandscapeHeight = Integer.parseInt(value);
                        } else if (TextUtils.equals(SYS_RCS_PORTRAIT_WIDTH, key)) {
                            mScreenPortraitWidth = Integer.parseInt(value);
                        } else if (TextUtils.equals(SYS_RCS_PORTRAIT_HEIGHT, key)) {
                            mScreenPortraitHeight = Integer.parseInt(value);
                        } else if (TextUtils.equals(SYS_RCS_SCREEN_ORIENTATION, key)) {
                            mScreenOrientation = Integer.parseInt(value);
                        } else if (TextUtils.equals(SYS_RCS_CODEC_QUALITY, key)) {
                            mVideoQuality = Integer.parseInt(value);
                        } else if (TextUtils.equals(SYS_RCS_VERSION, key)) {
                            mVersion = value;
                        }
                    }
                }
            }
        } catch (Exception e) {
            Utilities.log(this, e);
            System.exit(0);
        }
    }

    public void onInputInjectRun() {
        while (true) {
            try {
                ByteBuffer buffer = Packet.unpack(mStreamInput);
                if (buffer == null) {
                    Utilities.log(this, "read packet failed");
                    break;
                }
                buffer.get();
                buffer.get();
                byte type = buffer.get();
                int length = buffer.getInt();
                Utilities.log(this, "type=%s, length=%s", type, length);
                if (type == TYPE_TOUCH && length == 10) {

                    int x = buffer.getShort() & 0xffff;
                    int y = buffer.getShort() & 0xffff;
                    int action = buffer.get();
                    int time = buffer.getInt();
                    int finger = buffer.get();

                    int orientation = mScreenOrientation;
                    if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                        x -= (mScreenLandscapeWidth - mScreenPortraitWidth) / 2;
                        y -= (mScreenLandscapeHeight - mScreenPortraitHeight) / 2;
                    }
                    Utilities.log(this, "event:finger=%s, action=%s, time=%s, x=%s, y=%s", finger, action, time, x, y);
                    mTouchpad.getEvents().clear();
                    if (TYPE_TOUCH_DOWN == action) {
                        mTouchpad.onTouch(finger, MotionEvent.ACTION_DOWN, x, y);
                    } else if (TYPE_TOUCH_UP == action) {
                        mTouchpad.onTouch(finger, MotionEvent.ACTION_UP, x, y);
                    } else if (TYPE_TOUCH_MOVE == action) {
                        mTouchpad.onTouch(finger, MotionEvent.ACTION_MOVE, x, y);
                    }
                    for (MotionEvent event : mTouchpad.getEvents()) {
                        mHandler.obtainMessage(MESSAGE_TOUCH_INJECT, event).sendToTarget();
                    }
                } else if (type == TYPE_KEY && length == 5) {
                    int key = buffer.getInt();
                    int action = buffer.get();
                    key = Keyboard.getAndroidKeyCodeFromCSharp(key);
                    Utilities.log(this, "event:key=%s, action=%s", key, action);
                    if (TYPE_KEY_DOWN == action) {
                        for (KeyEvent event : mKeyboard.getEvents(KeyEvent.ACTION_DOWN, key)) {
                            mHandler.obtainMessage(MESSAGE_KEY_INJECT, event).sendToTarget();
                        }
                    } else if (TYPE_KEY_UP == action) {
                        for (KeyEvent event : mKeyboard.getEvents(KeyEvent.ACTION_UP, key)) {
                            mHandler.obtainMessage(MESSAGE_KEY_INJECT, event).sendToTarget();
                        }
                    } else if (TYPE_KEY_TYPE == action) {
                        if (key == 10001) {
                            Command command = new Command("start");
                            command.set("type", "activity");
                            command.set("action", "android.intent.action.MAIN");
                            command.set("category", "android.intent.category.HOME");
                            command.set("flags", Intent.FLAG_ACTIVITY_NEW_TASK);
                            System.out.println(command);
                        } else if (key == 10003) {
                            Command command = new Command("start");
                            command.set("type", "activity");
                            command.set("component", "com.android.systemui/.recent.RecentsActivity");
                            command.set("flags", Intent.FLAG_ACTIVITY_NEW_TASK);
                            System.out.println(command);
                        } else if (key == 10004) {
                            mKeyboard.setCapsLockEnabled(true);
                        } else if (key == 10005) {
                            mKeyboard.setCapsLockEnabled(false);
                        } else {
                            for (KeyEvent event : mKeyboard.getEvents(KeyEvent.ACTION_DOWN, key)) {
                                mHandler.obtainMessage(MESSAGE_KEY_INJECT, event).sendToTarget();
                            }
                            for (KeyEvent event : mKeyboard.getEvents(KeyEvent.ACTION_UP, key)) {
                                mHandler.obtainMessage(MESSAGE_KEY_INJECT, event).sendToTarget();
                            }
                        }
                    } else {
                        continue;
                    }
                }
            } catch (IOException e) {
                Utilities.log(this, e, "read packet failed");
                break;
            }
        }
        Utilities.log(this, "exit input inject thread");
        try {
            mStreamInput.close();
        } catch (IOException e) {
            Utilities.log(this, e);
        }
    }

    public void onLiveCheckRun() {
        while (true) {
            if (mLastTimeHeartbeat == 0) {
                mLastTimeHeartbeat = System.currentTimeMillis();
            }
            long now = System.currentTimeMillis();
            long diff = now - mLastTimeHeartbeat;
            if (diff > 10000) {
                Utilities.log(this, "thread is dead");
                System.exit(1);
            } else {
                SystemClock.sleep(10000);
                Utilities.log(this, "status: %.2f FPS; %.2f KBPS", mCountScreenshot / 10d, mCountOutputBytes / 10240d);
                mCountScreenshot = 0;
                mCountOutputBytes = 0;
            }
        }
    }

    public void onScreenCaptureRun() {
        try {
            long sleep = 40 + 100 - mVideoQuality;
            while (true) {
                mCountScreenshot++;
                int width = mScreenLandscapeWidth * mVideoQuality / 100;
                int height = mScreenLandscapeHeight * mVideoQuality / 100;
                mMethodScreenshotToSurface.invoke(null, mValueDefaultDisplay, mSurface, width, height);
                SystemClock.sleep(sleep);
            }
        } catch (Exception e) {
            Utilities.log(this, e);
        }
        Utilities.log(this, "exit screen capture thread");
        System.exit(0);
    }

    private void onTcpServiceRun() {
        try {
            ServerSocket server = new ServerSocket(12580);
            Socket socket = server.accept();
            server.close();

            Utilities.log(this, "create client: %s:%d", socket.getInetAddress(), socket.getPort());
            Utilities.log(this, "client start ...");

            mHasClient = true;

            socket.setTcpNoDelay(true);
            mStreamInput = new DataInputStream(socket.getInputStream());
            mStreamOutput = new DataOutputStream(socket.getOutputStream());

            Utilities.log(this, "create media codec: %sx%s:%s", mScreenLandscapeWidth, mScreenLandscapeHeight, mVideoQuality);
            int width = mScreenLandscapeWidth * mVideoQuality / 100;
            int height = mScreenLandscapeHeight * mVideoQuality / 100;
            Utilities.log(this, "create media codec: %sx%s", width, height);
            MediaFormat format = MediaFormat.createVideoFormat("video/avc", width, height);
            format.setInteger(MediaFormat.KEY_BIT_RATE, (int) (125000 / 100f * mVideoQuality));
            format.setInteger(MediaFormat.KEY_FRAME_RATE, (int) (1000f / (40 + 100 - mVideoQuality)));
            format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 5);
            format.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);

            mMediaCodec = MediaCodec.createEncoderByType("video/avc");
            mMediaCodec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
            mSurface = mMediaCodec.createInputSurface();
            mMediaCodec.start();

            new SubThread(THREAD_LIVE_CHECK).start();
            new SubThread(THREAD_VIDEO_OUTPUT).start();
            new SubThread(THREAD_INPUT_INJECT).start();
            new SubThread(THREAD_SCREEN_CAPTURE).start();

        } catch (IOException e) {
            Utilities.log(this, e, "tcp service exception");
        }
    }

    private void onUdpServiceRun() {
        mHandler.sendEmptyMessageDelayed(MESSAGE_SCREEN_CAPTURE, 5000);
        DatagramSocket socket;
        try {
            int port = 12306;
            Utilities.log(this, "start udp service at 0.0.0.0/%d", port);
            socket = new DatagramSocket(port);
        } catch (Exception e) {
            Utilities.log(this, e, "create udp socket failed");
            return;
        }
        byte[] bytes = new byte[32768];
        DatagramPacket packet = new DatagramPacket(bytes, bytes.length);
        while (!socket.isClosed()) {
            try {
                packet.setData(bytes, 0, bytes.length);
                socket.receive(packet);

                InetAddress address = packet.getAddress();
                int port = packet.getPort();

                Utilities.log(this, "request: client: %s:%d", address, port);
                ByteBuffer buffer = Packet.unpack(packet);
                if (buffer == null) {
                    Utilities.log(this, "request: read packet failed");
                    continue;
                }
                int type = buffer.get();
                switch (type) {
                case PACK_STRUCT_ENUM_SERVER_IDLE:
                case PACK_STRUCT_ENUM_SERVER_BUSY:
                    if (mHasClient) {
                        Utilities.log(this, "response: server busy");
                        buffer = Packet.pack(PACK_STRUCT_ENUM_CLIENT_BUSY, mVersion.getBytes());
                    } else {
                        Utilities.log(this, "response: server idle");
                        buffer = Packet.pack(PACK_STRUCT_ENUM_CLIENT_IDLE, mVersion.getBytes());
                    }
                    packet.setAddress(address);
                    packet.setPort(port);
                    packet.setData(buffer.array());
                    socket.send(packet);

                    Utilities.log(this, "response: server screenshot ...");
                    buffer = Packet.pack(PACK_STRUCT_ENUM_IMAGE, mScreenshotImage);
                    packet.setAddress(address);
                    packet.setPort(port);
                    packet.setData(buffer.array());
                    socket.send(packet);
                    break;
                default:
                    Utilities.log(this, "request: invalid packet type: %s", type);
                    break;
                }
            } catch (Exception e) {
                Utilities.log(this, e);
            }
        }
        socket.close();
    }

    public void onVideoOutputRun() {
        try {
            BufferInfo info = new BufferInfo();
            ByteBuffer[] buffers = null;
            Utilities.log(this, "start stream thread");
            while (true) {
                int index = mMediaCodec.dequeueOutputBuffer(info, 1000000);
                if (index >= 0) {
                    if (buffers == null) {
                        buffers = mMediaCodec.getOutputBuffers();
                    }
                    ByteBuffer buffer = buffers[index];
                    buffer.limit(info.offset + info.size);
                    buffer.position(info.offset);
                    try {
                        mStreamOutput.write(Packet.pack(TYPE_VIDEO, buffer).array());
                        mStreamOutput.flush();
                        mCountOutputBytes += buffer.limit();
                        mMediaCodec.releaseOutputBuffer(index, false);
                        mLastTimeHeartbeat = System.currentTimeMillis();
                    } catch (IOException e) {
                        Utilities.log(this, e, "stream thread error");
                        break;
                    }
                } else if (index == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                    Utilities.log(this, "on codec buffers changed");
                    buffers = null;
                } else if (index == MediaCodec.INFO_TRY_AGAIN_LATER) {
                    Utilities.log(this, "on codec dequeue buffer timeout");
                } else {
                    Utilities.log(this, "on codec error: %s", index);
                }
            }
        } catch (Exception e) {
            Utilities.log(this, e, "stream thread error");
        }
        Utilities.log(this, "exit video output thread");
        System.exit(0);
    }
}
