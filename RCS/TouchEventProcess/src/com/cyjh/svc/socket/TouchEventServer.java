/**
 * <p>Title: TouchEventServer.java</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2015</p>
 * <p>Company: CYJH</p>
 * @author CaiCQ
 * @date Mar 3, 2015
 * @version 1.0
 */
package com.cyjh.svc.socket;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import android.util.Log;

import com.cyjh.input.InputEventStub;
import com.cyjh.svc.proto.Protocol.Message;
import com.cyjh.svc.utils.CLog;
import com.cyjh.svc.utils.ConfigConstants;

/**
 * @author CaiCQ
 * 
 *         Mar 3, 2015
 */
public class TouchEventServer {
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
            server = new ServerSocket(ConfigConstants.SERVER_PORT);
        } catch (IOException e) {
            log("create server socket failed");
            return;
        }
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
                int length;
                try {
                    if (in == null) {
                        in = new DataInputStream(socket.getInputStream());
                    }
                } catch (IOException e) {
                    log("open input stream failed");
                    break;
                }
                long time = System.currentTimeMillis();
                log("event read...");
                try {
                    length = in.readInt();
                } catch (IOException e) {
                    log("read message size error");
                    break;
                }
                if (length < 0 || length > ConfigConstants.MAX_MSG_LENGTH) {
                    log("invalid message size: %s", length);
                    continue;
                } else if (length == 0) {
                    log("receive keep message");
                    continue;
                }
                byte[] array = new byte[length];
                Message message;
                try {
                    in.readFully(array);
                    message = Message.parseFrom(array);
                } catch (Exception e) {
                    log("read message failed");
                    break;
                }
                log("event:id=%s, action=%s, x=%s, y=%s", message.getId(), message.getActionType(), message.getX(), message.getY());

                switch (message.getActionType()) {
                case TOUCH_DOWN:
                    mStub.TouchDownEvent(message.getX(), message.getY(), message.getId());
                    break;
                case TOUCH_UP:
                    mStub.TouchUpEvent(message.getId());
                    break;
                case TOUCH_MOVE:
                    mStub.TouchMoveEvent(message.getX(), message.getY(), message.getId(), 0);
                    break;
                }
                log("event sent: %s", System.currentTimeMillis() - time);
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
}
