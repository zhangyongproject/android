package com.cyjh.screenmirror;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

import com.cyjh.screenmirror.utils.Utils;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaFormat;
import android.media.MediaCodec.BufferInfo;
import android.media.MediaCodecInfo.CodecCapabilities;
import android.os.Build;
import android.os.SystemClock;
import android.util.Log;
import android.view.Surface;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
public abstract class EncodeDevice {
    protected static final String LOGTAG = "MirrorDisplay";
    private static final boolean DEBUG = false;
    private static final long TIMEOUT_USEC = 1000000;
    private final int mQuality;

    protected int colorFormat;
    protected Point encSize;
    protected int mHeight;
    protected int mWidth;
    protected String name;
    protected boolean useSurface;

    protected VirtualDisplayFactory vdf;
    protected MediaCodec mMediaCodec;
    protected VirtualDisplay virtualDisplay;
    protected Thread localEncoderThread;
    protected EncoderRunnable localEncoderRunnable;

    private boolean mQuitting;

    private SurfaceRefresh screenRefresh;
    private Thread screenshotThread;
    private ScreenshotRecord screenshot;
    private IsLiveCheck mIsLiveCheck;

    private class IsLiveCheck extends Thread {
        private long mTime;
        private boolean mExit;

        public void check() {
            mTime = SystemClock.uptimeMillis();
        }

        public void exit() {
            mExit = true;
        }

        @Override
        public void run() {
            mExit = false;
            mTime = SystemClock.uptimeMillis();
            long now;
            while (!mExit) {
                SystemClock.sleep(1000);
                now = SystemClock.uptimeMillis();
                if (now - mTime > 30000) {
                    Log.d(LOGTAG, "kill send frame thread");
                    System.exit(1);
                }else{
                    Log.d(LOGTAG, "send frame thread is live");
                }
            }
        }
    }

    public EncodeDevice(String name, int width, int height, int quality) {
        useSurface = true;
        if (quality <= 0 || quality > 100) {
            quality = 100;
        }
        /*
         * 根据视频质量计算视频尺寸
         */
        mQuality = quality;
        this.mWidth = (int) (width / 100f * mQuality);

        this.mHeight = (int) (height / 100f * mQuality);
        Log.d(LOGTAG, "Width=" + mWidth + ", Height=" + mHeight);
        this.name = name;
        screenRefresh = null;
    }

    public static int getSupportedDimension(int dim) {
        if (dim <= 144)
            return 144;
        if (dim <= 176)
            return 176;
        if (dim <= 240)
            return 240;
        if (dim <= 288)
            return 288;
        if (dim <= 320)
            return 320;
        if (dim <= 352)
            return 352;
        if (dim <= 480)
            return 480;
        if (dim <= 576)
            return 576;
        if (dim <= 720)
            return 720;
        if (dim <= 1024)
            return 1024;
        if (dim <= 1280)
            return 1280;

        return 1920;
    }

    public static boolean isRecognizedFormat(int colorFormat) {
        switch (colorFormat) {
        case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Planar:
        case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420PackedPlanar:
        case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar:
        case MediaCodecInfo.CodecCapabilities.COLOR_TI_FormatYUV420PackedSemiPlanar:
            return true;
        }
        return false;
    }

    @SuppressLint("NewApi")
    private int selectColorFormat(MediaCodecInfo codecInfo, String mimeType) throws Exception {

        CodecCapabilities capabilities = codecInfo.getCapabilitiesForType(mimeType);
        if (DEBUG)
            Log.i(LOGTAG, "Available color formats: " + capabilities.colorFormats.length);

        for (int i = 0; i < capabilities.colorFormats.length; i++) {
            int colorFormat = capabilities.colorFormats[i];
            if (isRecognizedFormat(colorFormat)) {
                if (DEBUG)
                    Log.i(LOGTAG, "Using: " + colorFormat);
                return colorFormat;
            }
        }

        throw new Exception("Unable to find suitable color format");

    }

    private MediaCodecInfo findEncoder() {
        MediaCodecInfo codecInfo = null;
        try {
            int numCodecs = MediaCodecList.getCodecCount();
            for (int i = 0; i < numCodecs; i++) {
                MediaCodecInfo found = MediaCodecList.getCodecInfoAt(i);

                if (!found.isEncoder())
                    continue;

                String types[] = found.getSupportedTypes();
                for (int j = 0; j < types.length; j++) {
                    String type = types[j];
                    if (!type.equalsIgnoreCase("video/avc"))
                        continue;

                    if (codecInfo == null)
                        codecInfo = found;

                    Log.i(LOGTAG, "codec ---> " + codecInfo.getName());

                    CodecCapabilities caps = codecInfo.getCapabilitiesForType("video/avc");
                    int attr[] = caps.colorFormats;

                    if (DEBUG) {
                        for (int k = 0; k < attr.length; k++) {
                            Log.i(LOGTAG, "colorFormat: " + attr[k]);
                        }
                    }

                    MediaCodecInfo.CodecProfileLevel level[] = caps.profileLevels;

                    if (DEBUG) {
                        for (int k = 0; k < level.length; k++) {
                            Log.i(LOGTAG, "profile/level: " + level[k].profile + "/" + level[k].level);
                        }
                    }

                }
            }

        } catch (Exception e) {
            Log.w(LOGTAG, "Failed to create MeidaCodec " + e.getMessage());
            return null;
        }

        return codecInfo;
    }

    @SuppressLint("NewApi")
    public Surface createDisplaySurface() {
        if (android.os.Build.VERSION.SDK_INT < 18)
            return null;

        signalEnd();
        mMediaCodec = null;

        if (findEncoder() == null) {
            Log.e(LOGTAG, "Cannot find avc codec");
            return null;
        }
        // 根据视频质量计算相关参数
        MediaFormat mediaFormat = MediaFormat.createVideoFormat("video/avc", mWidth, mHeight);
        mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, (int) (125000 / 100f * mQuality));
        mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, (int) (1000f / (40 + 100 - mQuality)));
        mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 5);
        mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
        mMediaCodec = MediaCodec.createEncoderByType("video/avc");

        mMediaCodec.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);

        Surface localSurface = mMediaCodec.createInputSurface();

        localEncoderRunnable = onSurfaceCreated(mMediaCodec);
        localEncoderThread = new Thread(localEncoderRunnable, "Encoder");
        mMediaCodec.start();

        Log.e(LOGTAG, "create SurfaceRefresh ~~~");

        if (Utils.supportsVDF())
            screenRefresh = new SurfaceRefresh(null);

        return localSurface;
    }

    public Surface createInputSurface() {
        return mMediaCodec.createInputSurface();
    }

    void destroyDisplaySurface(MediaCodec venc) {
        if (venc == null)
            return;

        try {
            venc.stop();
            venc.release();

            if (virtualDisplay != null) {
                virtualDisplay.release();
                virtualDisplay = null;
                Log.e(LOGTAG, "Destroy display surface");
            }

            if (vdf != null) {
                vdf.release();
                vdf = null;
            }

            if (screenRefresh != null) {
                screenRefresh.quit();
                screenRefresh = null;
            }
        } catch (Exception e) {

        }
    }

    public int getBitrate(int maxBitrate) {
        return 2000000;
    }

    public int getColorFormat() {
        return colorFormat;
    }

    public Point getEncodingDimensions() {

        return encSize;
    }

    public MediaCodec getMediaCodec() {

        return mMediaCodec;
    }

    public boolean isConnected() {
        if (mMediaCodec == null)
            return false;
        else
            return true;
    }

    public abstract EncoderRunnable onSurfaceCreated(MediaCodec codec);

    public void registerVirtualDisplay(Context context, VirtualDisplayFactory vdf, int densityDpi) {

        Surface localSurface = createDisplaySurface();
        if (localSurface == null) {
            Log.e(LOGTAG, "Unable to create surface");
            return;
        }

        Log.i(LOGTAG, "Created surface");

        this.vdf = vdf;
        virtualDisplay = vdf.createVirtualDisplay(name, mWidth, mHeight, densityDpi, 3, localSurface, null);
        localEncoderThread.start();
    }

    public void registerScreenshot() {
        Surface localSurface = createDisplaySurface();
        if (localSurface == null) {
            Log.e(LOGTAG, "Unable to create surface");
            return;
        }

        Log.i(LOGTAG, "Created surface");

        screenshot = new ScreenshotRecord(localSurface, mWidth, mHeight);
        screenshotThread = new Thread() {
            public void run() {
                try {
                    // 根据视频质量参数计算桢间隔
                    long sleep = 40 + 100 - mQuality;
                    Log.e(LOGTAG, "sleep: " + sleep);
                    while (!mQuitting) {
                        screenshot.screenshot();
                        Thread.sleep(sleep);
                    }
                } catch (InterruptedException e) {

                }
            }
        };
        screenshotThread.start();
        localEncoderThread.start();
    }

    @SuppressLint("InlinedApi")
    public void setSurfaceFormat(MediaFormat video) {
        colorFormat = MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface;
    }

    @SuppressLint("NewApi")
    public void signalEnd() {

        if (mMediaCodec != null) {
            try {
                mMediaCodec.signalEndOfInputStream();

            } catch (Exception e) {
            }

        }
    }

    public void stop() {
        if (virtualDisplay != null) {
            virtualDisplay.release();
            virtualDisplay = null;
        }

        if (vdf != null) {
            vdf.release();
            vdf = null;
        }

    }

    public boolean supportsSurface() {

        if (android.os.Build.VERSION.SDK_INT < 19)
            return false;

        return useSurface;
    }

    public void useSurface(boolean useSurface) {
        this.useSurface = useSurface;
    }

    public void stream() {
        mIsLiveCheck = new IsLiveCheck();
        mIsLiveCheck.start();
        Log.d(LOGTAG, "create: " + BroadcastService.isConnected);
        try {
            BroadcastService.isConnected.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            BufferInfo info = new BufferInfo();
            ByteBuffer[] buffers = null;
            Log.d(LOGTAG, "stream~~~~");
            Log.e(LOGTAG, "start loop");
            while (!mQuitting) {
                //Log.e(LOGTAG, "dequeueOutputBuffer begin");
                int index = mMediaCodec.dequeueOutputBuffer(info, TIMEOUT_USEC);
                //Log.e(LOGTAG, "dequeueOutputBuffer end");
                if (index >= 0) {
                    if (buffers == null) {
                        buffers = mMediaCodec.getOutputBuffers();
                    }
                    //Log.e(LOGTAG, "get buffer begin");
                    ByteBuffer buffer = buffers[index];
                    buffer.limit(info.offset + info.size);
                    buffer.position(info.offset);
                    //Log.e(LOGTAG, "get buffer end");
                    // Log.d(LOGTAG, "dequeue a buffer offset "+info.offset
                    // +" size: "+info.size+" format "+mMediaCodec.getOutputFormat());
                    try {
                        //Log.e(LOGTAG, "send frame begin");
                        sendFrame(buffer);
                        mMediaCodec.releaseOutputBuffer(index, false);
                        //Log.e(LOGTAG, "send frame end");
                        mIsLiveCheck.check();
                    } catch (IOException e) {
                        localEncoderRunnable.setStop();
                        destroyDisplaySurface(mMediaCodec);
                        mQuitting = true;
                        //Log.e(LOGTAG, "exit loop", e);
                    }
                } else if (index == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                    Log.e(LOGTAG, "Codec buffers_changed");
                    buffers = null;
                } else if (index == MediaCodec.INFO_TRY_AGAIN_LATER) {
                    Log.e(LOGTAG, "Codec dequeue buffer timed out.");
                    if (screenRefresh != null)
                        screenRefresh.repaintScreen();
                } else {
                    Log.e(LOGTAG, "Code error " + index);
                }
            }
            Log.e(LOGTAG, "end loop");
        } catch (Exception e) {
            Log.e(LOGTAG, "loop error", e);
        }
        Log.d(LOGTAG, "delete: " + BroadcastService.isConnected);
        BroadcastService.isConnected.delete();
        mIsLiveCheck.exit();
    }

    public abstract void sendFrame(ByteBuffer buffer) throws IOException;

    public class EncoderRunnable implements Runnable {

        private EncodeDevice m_encDev;

        public EncoderRunnable(EncodeDevice encDev) {
            m_encDev = encDev;
        }

        public void run() {
            while (!mQuiting) {
                m_encDev.stream();
            }
            MirrorServer.getMirrorServer().setConnectState(MirrorServer.CONNECT_STATE.CLIENT_NOT_CONNECTED);
        }

        public void setStop() {
            mQuiting = true;
        }

        private boolean mQuiting = false;
    }
}
