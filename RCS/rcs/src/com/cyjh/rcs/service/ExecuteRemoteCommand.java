package com.cyjh.rcs.service;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;

import android.content.ClipData;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;

import com.cyjh.library.Command;
import com.cyjh.library.Packet;
import com.cyjh.library.Script;
import com.cyjh.library.Utilities;

public class ExecuteRemoteCommand implements Runnable, Handler.Callback {

    private static final byte COMMAND_TYPE_REQUEST_INSTALL = 100;
    private static final byte COMMAND_TYPE_RESPONSE_INSTALL_SUCCESSFUL = 101;
    private static final byte COMMAND_TYPE_RESPONSE_INSTALL_FAILED = 102;

    private static final byte COMMAND_TYPE_REQUEST_REBOOT = 103;
    private static final byte COMMAND_TYPE_RESPONSE_REBOOT_SUCCESSFUL = 104;
    private static final byte COMMAND_TYPE_RESPONSE_REBOOT_FAILED = 105;

    private static final byte COMMAND_TYPE_RESPONSE_PASTE = (byte) 150;
    private static final byte COMMAND_TYPE_RESPONSE_ORIENTATION = (byte) 151;

    private class PasteCommandProcessor implements Runnable {

        private final String mText;

        public PasteCommandProcessor(String text) {
            mText = text;
        }

        @Override
        public void run() {
            try {
                Utilities.log(this, "paste :%s", mText);
                ClipData data = ClipData.newPlainText(null, mText);
                mMethodIClipboardSetPrimaryClip.invoke(mObjectClassIClipboardService, data, "android");
            } catch (Exception e) {
                Utilities.log(this, e);
            }
        }

    }

    private class RemoteCommandProcessor implements Runnable {
        private Socket mSocket;

        public RemoteCommandProcessor(Socket socket) {
            mSocket = socket;
            Utilities.log(this, "%s", socket.getRemoteSocketAddress());
        }

        @Override
        public void run() {
            process(mSocket);
        }

    }

    public static void main(String[] args) throws Exception {
        Looper.prepare();
        new Thread(new ExecuteRemoteCommand()).start();
        Looper.loop();
    }

    private final Class<?> mClassServiceManager;
    private final Class<?> mClassIClipboard;
    private final Class<?> mClassIClipboardStub;
    private final Method mMethodServiceManagerGetService;
    private final Method mMethodIClipboardStubAsInterface;
    private final Method mMethodIClipboardSetPrimaryClip;
    private final Object mObjectClassIClipboardService;

    private final Handler mHandler;

    public ExecuteRemoteCommand() throws Exception {
        mHandler = new Handler(this);
        {
            mClassServiceManager = Class.forName("android.os.ServiceManager");
            mMethodServiceManagerGetService = mClassServiceManager.getMethod("getService", String.class);
            mClassIClipboard = Class.forName("android.content.IClipboard");
            mClassIClipboardStub = Class.forName("android.content.IClipboard$Stub");
            mMethodIClipboardStubAsInterface = mClassIClipboardStub.getMethod("asInterface", IBinder.class);
            mObjectClassIClipboardService = mMethodIClipboardStubAsInterface.invoke(null, mMethodServiceManagerGetService.invoke(null, "clipboard"));
            mMethodIClipboardSetPrimaryClip = mClassIClipboard.getMethod("setPrimaryClip", ClipData.class, String.class);
        }
    }

    public void run() {
        try {
            ServerSocket server = new ServerSocket(12581);
            while (!server.isClosed()) {
                Socket socket = server.accept();
                new Thread(new RemoteCommandProcessor(socket)).start();
            }
            server.close();
        } catch (Exception e) {
            Utilities.log(this, e);
        }
    }

    private void process(Socket socket) {
        try {
            DataInputStream in = new DataInputStream(socket.getInputStream());
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            ByteBuffer buffer = Packet.unpack(in);
            if (buffer == null) {
                Utilities.log(this, "read packet failed");
                socket.close();
                return;
            }
            buffer.get();
            buffer.get();
            byte type = buffer.get();
            int length = buffer.getInt();
            Utilities.log(this, "packet type: %s", type);
            Utilities.log(this, "packet length: %s", length);
            switch (type) {
            case COMMAND_TYPE_RESPONSE_PASTE: {
                mHandler.post(new PasteCommandProcessor(Utilities.getString(buffer, length)));
                break;
            }
            case COMMAND_TYPE_REQUEST_REBOOT: {
                if (reboot()) {
                    out.write(Packet.pack(COMMAND_TYPE_RESPONSE_REBOOT_SUCCESSFUL).array());
                } else {
                    out.write(Packet.pack(COMMAND_TYPE_RESPONSE_REBOOT_FAILED).array());
                }
                out.flush();
                break;
            }
            case COMMAND_TYPE_RESPONSE_ORIENTATION: {
                int data = buffer.getInt();
                Utilities.log(this, "orientation data: %s", data);
                Command command = new Command("send");
                command.set("type", "broadcast");
                command.set("action", "pipo.android.roatescreen.action");
                command.set("s:Rotation_rj", String.valueOf(data));
                System.out.println(command);
                break;
            }
            case COMMAND_TYPE_REQUEST_INSTALL: {
                String name = Utilities.getString(buffer, 260).trim();
                byte[] checksum = Utilities.getBytes(buffer, 16);
                int count = buffer.getInt();
                Utilities.log(this, "file name: %s", name);
                Utilities.log(this, "file checksum: %s", Utilities.getString(checksum));
                Utilities.log(this, "file length: %s", length);
                boolean success = false;
                byte[] data = new byte[0];
                if (TextUtils.equals(name, "uyouupgrade.zip")) {
                    name = Utilities.format("receive/%016x.zip", System.currentTimeMillis());
                    File file = Utilities.getExternalStorageFile(name);
                    Utilities.log(this, "file path: %s", file.getPath());
                    if (Utilities.write(in, file, count)) {
                        String upgrade = Utilities.format("upgrade-%016x", System.currentTimeMillis());
                        Script script = new Script("/system/bin/sh");
                        script.put("mount -o remount /system");
                        script.put("cd %s", file.getParent());
                        script.put("busybox rm -rf %s", upgrade);
                        script.put("busybox mkdir %s", upgrade);
                        script.put("cd %s", upgrade);
                        script.put("pwd");
                        script.put("busybox unzip %s", file.getPath());
                        script.put("echo 'start uyouupgrade.sh'");
                        script.put("busybox ash uyouupgrade.sh $PWD 2>&1");
                        script.put("echo 'end uyouuw pgrade.sh'");
                        script.put("cd ..");
                        script.put("pwd");
                        script.put("busybox rm -rf %s 2>&1", upgrade);
                        script.put("busybox rm %s", file.getPath());
                        script.put("exit");
                        script.put("exit");
                        String result = script.execute();
                        if (result != null) {
                            success = true;
                            data = result.getBytes();
                            Utilities.log(this, result);
                        }
                    }
                } else {
                    name = Utilities.format("receive/%016x.apk", System.currentTimeMillis());
                    File file = Utilities.getExternalStorageFile(name);
                    Utilities.log(this, "file path: %s", file.getPath());
                    data = new byte[0];
                    if (Utilities.write(in, file, count)) {
                        String result = Utilities.shell("pm install -r %s", file.getPath());
                        Utilities.log(this, "execute result: %s", result);
                        if (result != null && result.trim().endsWith("Success")) {
                            success = true;
                            data = new byte[0];
                        }
                    }
                    file.delete();
                }
                if (success) {
                    out.write(Packet.pack(COMMAND_TYPE_RESPONSE_INSTALL_SUCCESSFUL, data).array());
                } else {
                    out.write(Packet.pack(COMMAND_TYPE_RESPONSE_INSTALL_FAILED, data).array());
                }
                out.flush();
            }
            }

        } catch (Exception e) {
            Utilities.log(this, e);
        }
        try {
            socket.close();
        } catch (IOException e) {
            Utilities.log(this, e);
        }
    }

    private boolean reboot() {
        try {
            Runtime.getRuntime().exec("reboot");
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    @Override
    public boolean handleMessage(Message arg0) {
        // TODO Auto-generated method stub
        return false;
    }
}
