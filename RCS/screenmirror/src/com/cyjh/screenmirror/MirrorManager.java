package com.cyjh.screenmirror;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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

import android.util.Log;
import com.cyjh.screenmirror.utils.Utils;

public class MirrorManager {

    private Process captureProc;
    private Process mirrorProc;
    private Thread thread1, thread2, thread3;
    public boolean mQuitting = false;
    private DatagramSocket udp1;
    private DatagramSocket udp2;

    // lock to protect the host lists
    private Object lock = new Object();
    private Map<InetAddress, Long> hosts = null;
    private String apkPath;

    private static MirrorManager sManager = null;

    public static MirrorManager getInstance() {
        if (sManager == null) {
            sManager = new MirrorManager();
        }

        return sManager;
    }

    private MirrorManager() {
        apkPath = getApkPath();

        startService(Main.class.getName());
        startService(TouchEventServer.class.getName());
        Log.i("MirrorDisplay", "start mirror Video service " + mirrorProc);
    }

    public void create() {
        apkPath = getApkPath();
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

    public void destroy() {
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

    public void requestScrenncapStop() {
        PrintStream ps = new PrintStream(captureProc.getOutputStream());
        ps.println("exit");
        captureProc.destroy();
        captureProc = null;
    }

    public void sendScreencap(DatagramSocket sock, InetAddress addr, int port) {

        InputStream is = captureProc.getInputStream();

        PrintStream ps = new PrintStream(captureProc.getOutputStream());
        ps.println("capture");
        byte len[] = new byte[4];
        try {
            is.read(len);
            Log.i("MirrorDisplay", "captureLength " + Utils.bytes2Int(len, 0));
            byte data[] = new byte[Utils.bytes2Int(len, 0)];
            is.read(data);
            DatagramPacket packet = new DatagramPacket(data, data.length, addr, port);
            sock.send(packet);
        } catch (IOException e) {
            return;
        }
    }

    public Thread createListThread() {
        return new Thread("list") {

            public void run() {
                Log.d("Mirror", "createListThread()");
                while (!mQuitting) {
                    try {

                        String msg = "uyouclient=online";
                        InetAddress wireBa = Utils.getWiredBroadcast();
                        InetAddress wlanBa = Utils.getWlanBroadcast();

                        if (wireBa != null) {
                            DatagramPacket packet = new DatagramPacket(msg.getBytes(), msg.length(), wireBa, 12306);
                            udp1.send(packet);
                            Log.i("MirrorDisplay", "send to wired " + wireBa);
                        }

                        if (wlanBa != null) {
                            DatagramPacket packet = new DatagramPacket(msg.getBytes(), msg.length(), wlanBa, 12306);
                            udp1.send(packet);
                            Log.i("MirrorDisplay", "send to wlan " + wlanBa);
                        }
                        Thread.sleep(10000);
                    } catch (InterruptedException e) {
                    } catch (IOException e) {
                    }
                }
            }
        };
    }

    public Thread createUdpServerThread() {
        return new Thread() {

            public void run() {
                Log.d("Mirror", "createUdpServerThread()");
                byte buf[] = new byte[100];
                while (!mQuitting) {
                    try {
                        DatagramPacket packet = new DatagramPacket(buf, buf.length);
                        udp2.receive(packet);
                        InetAddress h = packet.getAddress();

                        String msg = new String(buf, 0, packet.getLength());

                        Log.i("MirrorDisplay2", "Receive " + msg);

                        if (!msg.equals("uyouserver=online"))
                            continue;

                        synchronized (lock) {
                            if (!hosts.containsKey(h)) {
                                hosts.put(h, Long.valueOf(System.currentTimeMillis()));
                                msg = "uyouclient=online";

                                packet = new DatagramPacket(msg.getBytes(), msg.length(), h, 12306);
                                udp1.send(packet);

                                Log.i("MirrorDisplay2", "send ack new " + packet.getAddress());
                                sendScreencap(udp1, packet.getAddress(), 12306);
                            } else {
                                Log.i("MirrorDisplay2", "send ack " + packet.getAddress() + "port " + packet.getPort());
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
                Log.d("Mirror", "createMonitorThread()");
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

    protected Process startCaptureProcess() {
        String apkPath = Utils.getApkPath();
        String cmdline[] = new String[] { "su", "root", "/system/bin/sh", "-c", "export CLASSPATH=" + apkPath + ";" + "/system/bin/app_process /system/bin com.cyjh.screenmirror.Thumnail" };
        try {
            return Runtime.getRuntime().exec(cmdline);
        } catch (IOException e) {
            return null;
        }
    }

    public String[] getEnvironment() {
        Map<String, String> map = System.getenv();
        String myEnv[] = new String[map.size()];
        int i = 0;
        for (Iterator<String> it = map.keySet().iterator(); it.hasNext(); i++) {
            String key = it.next();
            String value = map.get(key);
            myEnv[i] = key + "=" + value;

        }
        return myEnv;
    }

    public void startMirrorService1() {
        startService("com.cyjh.screenmirror.Main");
    }

    public void startService(String who) {
        try {
            String script = "export CLASSPATH=%s;while true;do /system/bin/app_process /system/bin %s;sleep 1;done";
            script = String.format(script, apkPath, who);
            String cmdline[] = new String[] { "su", "root", "/system/bin/sh", "-c", script };

            String oldEnv[] = getEnvironment();
            String env[] = new String[oldEnv.length + 1];
            System.arraycopy(oldEnv, 0, env, 0, oldEnv.length);
            env[env.length - 1] = "CLASSPATH=" + apkPath;

            mirrorProc = Runtime.getRuntime().exec(cmdline, env, new File("/system/bin"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean checkService() {
        if (MirrorServer.checkMirrorServer())
            return true;
        else
            return false;
    }

    public void checkAndStopService() {
        if (MirrorServer.checkMirrorServer()) {
            MirrorServer.requestStop();
        }
        if (mirrorProc != null) {
            mirrorProc.destroy();
            mirrorProc = null;
        }
    }

    private String getApkPath() {
        String cmdline = "pm path com.cyjh.screenmirror\n";
        try {
            Process proc = Runtime.getRuntime().exec(cmdline);
            BufferedReader br = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            String path = br.readLine();
            return path.substring("package:".length());

        } catch (IOException e) {
            return "";
        }
    }
}
