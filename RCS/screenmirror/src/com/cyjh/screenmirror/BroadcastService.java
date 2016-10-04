package com.cyjh.screenmirror;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Environment;
import android.os.IBinder;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import com.cyjh.screenmirror.MirrorServer.CONNECT_STATE;
import com.cyjh.screenmirror.utils.Utils;

public class BroadcastService extends Service {

    public static File isConnected = new File("/sdcard", ".cyjh.connected");

    @Override
    public IBinder onBind(Intent arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    private void requestScrenncapStop() {
        PrintStream ps = new PrintStream(captureProc.getOutputStream());
        ps.println("exit");
        captureProc.destroy();
    }

    public void sendScreencap(DatagramSocket sock, InetAddress addr, int port) {
        InputStream is = null;
        PrintStream ps = null;
        try {
            is = captureProc.getInputStream();
            ps = new PrintStream(captureProc.getOutputStream());
        } catch (Exception e) {
            Log.d("Mirror", "sendScreencap exception!!!");
        }

        ps.println("capture");
        byte len[] = new byte[4];
        try {
            is.read(len);
            byte data[] = new byte[Utils.bytes2Int(len, 0)];
            is.read(data);
            DatagramPacket packet = new DatagramPacket(data, data.length, addr, port);
            sock.send(packet);
        } catch (IOException e) {
            return;
        }
    }

    public byte[] getScreencap() {
        InputStream is = null;
        PrintStream ps = null;
        try {
            is = captureProc.getInputStream();
            ps = new PrintStream(captureProc.getOutputStream());
        } catch (Exception e) {
            Log.d("Mirror", "sendScreencap exception!!!");
        }

        ps.println("capture");
        byte len[] = new byte[4];
        try {
            is.read(len);
            byte data[] = new byte[Utils.bytes2Int(len, 0)];
            is.read(data);
            return data;
        } catch (IOException e) {
            return null;
        }
    }

    public Thread createListThread() {
        return new Thread("list") {

            public void run() {
                // while (!mQuitting) {
                // try {
                //
                // String msg = "uyouclient=online";
                // InetAddress wireBa = Utils.getWiredBroadcast();
                // InetAddress wlanBa = Utils.getWlanBroadcast();
                //
                // if (wireBa != null) {
                // DatagramPacket packet = new DatagramPacket(msg.getBytes(),
                // msg.length(), wireBa, 12306);
                // udp1.send(packet);
                // Log.i("MirrorDisplay", "send to wired " + wireBa);
                // }
                //
                // if (wlanBa != null) {
                // DatagramPacket packet = new DatagramPacket(msg.getBytes(),
                // msg.length(), wlanBa, 12306);
                // udp1.send(packet);
                // Log.i("MirrorDisplay", "send to wlan " + wlanBa);
                // }
                // Thread.sleep(10000);
                // } catch (InterruptedException e) {
                // } catch (IOException e) {
                // }
                // }
            }
        };
    }

    public static final int PackStructEnum_client_idle = 0;
    public static final int PackStructEnum_client_busy = 1;
    public static final int PackStructEnum_server_idle = 2;
    public static final int PackStructEnum_server_busy = 3;

    public static final int PackStructEnum_askconnect = 4;
    public static final int PackStructEnum_allowconnect = 5;
    public static final int PackStructEnum_refuseconnect = 6;

    public static final int PackStructEnum_reboot_task = 50;
    public static final int PackStructEnum_reboot_can = 51;
    public static final int PackStructEnum_reboot_ok = 52;
    public static final int PackStructEnum_reboot_force = 1000;
    public static final int PackStructEnum_ImgData = 100;

    private static class UDPPack {

        private int mType = -1;
        private byte[] mGuid = new byte[16];
        private int mLength;
        private InetAddress mRemoteAddress;
        private int mRemotePort;

        public UDPPack(int type) {
            mType = type;
        }

        public int getType() {
            return mType;
        }

        public String getClientGuidAsString() {
            return Base64.encodeToString(mGuid, Base64.DEFAULT).trim();
        }

        public static void reply(DatagramSocket socket, UDPPack source, int type, byte... data) throws IOException {
            if (data == null) {
                data = new byte[0];
            }
            byte[] bytes = new byte[4 + 4 + 16 + 4 + data.length];
            bytes[0] = (byte) 0xff;
            bytes[1] = (byte) 0xff;
            bytes[2] = (byte) 0xff;
            bytes[3] = (byte) 0xff;
            bytes[4] = (byte) ((type >> 0x00) & 0xFF);
            bytes[5] = (byte) ((type >> 0x08) & 0xFF);
            bytes[6] = (byte) ((type >> 0x10) & 0xFF);
            bytes[7] = (byte) ((type >> 0x18) & 0xFF);
            System.arraycopy(source.mGuid, 0, bytes, 8, 16);
            bytes[24] = (byte) ((data.length >> 0x00) & 0xFF);
            bytes[25] = (byte) ((data.length >> 0x08) & 0xFF);
            bytes[26] = (byte) ((data.length >> 0x10) & 0xFF);
            bytes[27] = (byte) ((data.length >> 0x18) & 0xFF);
            System.arraycopy(data, 0, bytes, 28, data.length);
            DatagramPacket packet = new DatagramPacket(bytes, bytes.length, source.mRemoteAddress, source.mRemotePort);
            socket.send(packet);
        }

        public static UDPPack read(DatagramSocket socket) throws IOException {
            byte[] bytes = new byte[4 + 4 + 16 + 4];
            DatagramPacket packet = new DatagramPacket(bytes, bytes.length);
            socket.receive(packet);
            System.out.println("receive packet: " + packet.getLength());
            if (packet.getLength() != bytes.length) {
                return null;
            }
            byte[] data = packet.getData();
            if ((data[0] & 0xff) != 0xff || (data[1] & 0xff) != 0xff || (data[2] & 0xff) != 0xff || (data[3] & 0xff) != 0xff) {
                return null;
            }
            int type = 0;
            int length = 0;
            type |= (data[4] & 0xff) << 0x00;
            type |= (data[5] & 0xff) << 0x08;
            type |= (data[6] & 0xff) << 0x10;
            type |= (data[7] & 0xff) << 0x18;
            UDPPack result = new UDPPack(type);
            length |= (data[24] & 0xff) << 0x00;
            length |= (data[25] & 0xff) << 0x08;
            length |= (data[26] & 0xff) << 0x10;
            length |= (data[27] & 0xff) << 0x18;
            result.mLength = length;
            System.arraycopy(data, 5, result.mGuid, 0, 16);
            result.mRemoteAddress = packet.getAddress();
            result.mRemotePort = packet.getPort();
            return result;
        }
    }

    private class UDPServerThread implements Runnable {
        private String mRequestRebootClient;
        private long mRequestRebootTime;
        private String mRequestConnectClient;
        private long mRequestConnectTime;

        @Override
        public void run() {
            SystemClock.sleep(5000);
            while (!mQuitting) {
                try {
                    UDPPack pack = UDPPack.read(udp2);
                    if (pack == null) {
                        continue;
                    }

                    switch (pack.getType()) {
                    case PackStructEnum_server_idle: {
                        if (!isConnected.exists()) {
                            UDPPack.reply(udp2, pack, PackStructEnum_client_idle);
                        } else {
                            UDPPack.reply(udp2, pack, PackStructEnum_client_busy);
                        }
                        UDPPack.reply(udp2, pack, PackStructEnum_ImgData, getScreencap());
                        break;
                    }
                    case PackStructEnum_askconnect: {
                        String client = pack.getClientGuidAsString();
                        long now = System.currentTimeMillis();
                        long diff = now - mRequestConnectTime;
                        if (isConnected.exists() && !TextUtils.equals(mRequestConnectClient, client)) {
                            UDPPack.reply(udp2, pack, PackStructEnum_refuseconnect);
                        } else {
                            if (mRequestConnectClient == null || mRequestConnectClient.equals(client) || diff > 5000) {
                                UDPPack.reply(udp2, pack, PackStructEnum_allowconnect);
                                mRequestConnectClient = client;
                                mRequestConnectTime = now;
                            } else {
                                UDPPack.reply(udp2, pack, PackStructEnum_refuseconnect);
                            }
                        }
                        break;
                    }
                    case PackStructEnum_reboot_task: {
                        String client = pack.getClientGuidAsString();
                        long now = System.currentTimeMillis();
                        long diff = now - mRequestRebootTime;
                        if (mRequestRebootClient == null || mRequestRebootClient.equals(client) || diff > 5000) {
                            mRequestRebootClient = client;
                            mRequestRebootTime = now;
                            UDPPack.reply(udp2, pack, PackStructEnum_reboot_can);
                        }
                        break;
                    }
                    case PackStructEnum_reboot_ok: {
                        long now = System.currentTimeMillis();
                        long diff = now - mRequestRebootTime;
                        String client = pack.getClientGuidAsString();
                        if (mRequestRebootClient != null && mRequestRebootClient.equals(client) && diff <= 5000) {
                            Runtime.getRuntime().exec("su -c 'reboot'");
                        }
                        break;
                    }
                    case PackStructEnum_reboot_force: {
                        Runtime.getRuntime().exec("su -c 'reboot'");
                        break;
                    }
                    }
                } catch (SocketTimeoutException e) {
                } catch (IOException e) {
                }
            }
        }
    }

    public Thread createUdpServerThread() {
        return new Thread(new UDPServerThread());
    }

    public Thread createUdpServerThread1() {
        return new Thread() {

            public void run() {

                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                byte buf[] = new byte[100];
                while (!mQuitting) {
                    try {
                        DatagramPacket packet = new DatagramPacket(buf, buf.length);
                        udp2.receive(packet);
                        InetAddress h = packet.getAddress();

                        String msg = new String(buf, 0, packet.getLength());

                        Log.i("MirrorDisplay", "Receive " + msg);

                        if (!msg.equals("uyouserver=online"))
                            continue;

                        synchronized (lock) {
                            if (!hosts.containsKey(h)) {
                                hosts.put(h, Long.valueOf(System.currentTimeMillis()));
                                msg = "uyouclient=online";

                                packet = new DatagramPacket(msg.getBytes(), msg.length(), h, 12306);
                                udp1.send(packet);

                                Log.i("MirrorDisplay", "send ack new " + packet.getAddress());
                                sendScreencap(udp1, packet.getAddress(), 12306);
                            } else {
                                Log.i("MirrorDisplay", "send ack " + packet.getAddress() + "port " + packet.getPort());
                                hosts.put(h, Long.valueOf(System.currentTimeMillis()));
                                sendScreencap(udp1, packet.getAddress(), 12306);
                            }
                        }
                    } catch (SocketTimeoutException e) {
                    } catch (IOException e) {
                    }
                }
            }
        };
    }

    public Thread createMonitorThread() {
        return new Thread() {
            public void run() {
                while (!mQuitting) {
                    long curTime = System.currentTimeMillis();
                    synchronized (lock) {
                        for (Iterator<Entry<InetAddress, Long>> it = hosts.entrySet().iterator(); it.hasNext();) {
                            Entry<InetAddress, Long> entry = it.next();
                            Long time = entry.getValue();
                            // InetAddress addr = entry.getKey();
                            if (curTime - time.longValue() > 30 * 1000) {
                                it.remove();
                            }
                        }
                    }
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                    }
                }
            }
        };
    }

    private Process startCaptureProcess() {
        String apkPath = Utils.getApkPath();
        String cmdline[] = new String[] { "su", "root", "/system/bin/sh", "-c", "export CLASSPATH=" + apkPath + ";" + "/system/bin/app_process /system/bin com.cyjh.screenmirror.Thumnail" };
        try {
            return Runtime.getRuntime().exec(cmdline);
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    public void onCreate() {
        Log.e("MirrorDisplay", "delete: " + isConnected + " " + isConnected.delete());
        MirrorManager.getInstance();
        Notification notification = new Notification(R.drawable.ic_launcher, "Notification", System.currentTimeMillis());
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
        notification.setLatestEventInfo(this, "Notification", "notification", pendingIntent);
        startForeground(1, notification);
        hosts = new HashMap<InetAddress, Long>();
        try {
            udp1 = new DatagramSocket();
            udp2 = new DatagramSocket(12306);

            udp2.setSoTimeout(1000); // Not block

            thread1 = createListThread();
            thread1.start();

            thread2 = createUdpServerThread();
            thread2.start();

            thread3 = createMonitorThread();
            thread3.start();

            captureProc = startCaptureProcess();

        } catch (SocketException e) {
            Log.e("MirrorDisplay", "exception " + e);
            e.printStackTrace();
        } catch (Exception e) {
            Log.e("MirrorDisplay", "exception " + e);
        }
    }

    public void log(String format, Object... objects) {
        if (format != null) {
            System.out.printf(format, objects);
            System.out.println();
        }
    }

    @Override
    public void onDestroy() {
        mQuitting = true;
        hosts.clear();
        hosts = null;
        try {
            thread1.interrupt();
            thread2.interrupt();
            thread3.interrupt();
            thread1.join();
            thread2.join();
            thread3.join();
            requestScrenncapStop();
        } catch (InterruptedException e) {
        }
        if (udp2 != null) {
            udp2.close();
            udp2 = null;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        // Log.i("MirrorDisplay", "start commmand");
        return super.onStartCommand(intent, flags, startId);
    }

    private DatagramSocket udp1, udp2;
    private Thread thread1, thread2, thread3;
    private Process captureProc;
    private boolean mQuitting = false;
    private Map<InetAddress, Long> hosts;
    private Object lock = new Object();

}
