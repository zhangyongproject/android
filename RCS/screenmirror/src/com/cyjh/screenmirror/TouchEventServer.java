/**
 * <p>Title: TouchEventServer.java</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2015</p>
 * <p>Company: CYJH</p>
 * @author CaiCQ
 * @date Mar 3, 2015
 * @version 1.0
 */
package com.cyjh.screenmirror;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import android.util.Log;

import com.cyjh.input.InputEventStub;

/**
 * @author CaiCQ
 * 
 *         Mar 3, 2015
 */
public class TouchEventServer {
    public static final int SERVER_PORT = 17890;
    private final InputEventStub mStub;

    public TouchEventServer() {
        mStub = new InputEventStub();
        mStub.SetProtoType(0);
    }

    private void log(String log, Object... objects) {
        if (log == null) {
            return;
        }
        if (objects != null && objects.length > 0) {
            log = String.format(log, objects);
        }
        Log.d(getClass().getName(), log);
    }

    private void loop() {
        ServerSocket server;
        log("start input stream ...");
        try {
            server = new ServerSocket(SERVER_PORT);
        } catch (IOException e) {
            log("create server socket failed");
            return;
        }
        Packet packet=new Packet();
        while (true) {
            Socket socket;
            try {
                socket = server.accept();
            } catch (IOException e) {
                log("accept client socket failed");
                break;
            }
            DataInputStream in = null;
            while (true) {
                try {
                    if (in == null) {
                        in = new DataInputStream(socket.getInputStream());
                    }
                } catch (IOException e) {
                    log("open input stream failed");
                    break;
                }
                try {
                    packet.read(in);
                } catch (IOException e) {
                    log("read packet failed");
                    break;
                }
                if(packet.getType()==1&&packet.getLength()==10){
                    int x=packet.getShort(0);
                    int y=packet.getShort(2);
                    byte action=packet.getByte(4);
                    int time=packet.getInteger(5);
                    byte id=packet.getByte(9);
                    log("event:id=%s, action=%s, x=%s, y=%s", id, action, x, y);

                    switch (action) {
                    case 1:
                        mStub.TouchDownEvent(x, y, id);
                        break;
                    case 2:
                        mStub.TouchUpEvent(id);
                        break;
                    case 3:
                        mStub.TouchMoveEvent(x, y, id, 0);
                        break;
                    }
                    log("event sent: %s", System.currentTimeMillis() - time);
                }
            }
            try {
                socket.close();
            } catch (Exception e) {
                log("close client socket failed");
            }
        }
        try {
            server.close();
        } catch (Exception e) {
            log("close server socket failed");
        }
    }

    public void start() {
        while (true) {
            loop();
        }
    }
    
    public static void main(String[] args) {
        new TouchEventServer().start();
    }
}
