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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.concurrent.ArrayBlockingQueue;

import android.os.Looper;

import com.cyjh.input.InputEventStub;
import com.cyjh.svc.proto.RawData;
import com.cyjh.svc.proto.Protocol.Message;
import com.cyjh.svc.utils.CLog;
import com.cyjh.svc.utils.ConfigConstants;
import com.google.protobuf.InvalidProtocolBufferException;

/**
 * @author CaiCQ
 * 
 *         Mar 3, 2015
 */
public class TouchEventServer1
{
	private static final String TAG = TouchEventServer1.class.getSimpleName();

	private boolean mSvrSocketCreated = false;
	private ServerSocket mSvrSocket = null;
	private InputStream mInput = null;
	private OutputStream mOutput = null;
	private ArrayBlockingQueue<Message> mQueue = null;
	
	private InputEventStub mStub = null;
	
	public TouchEventServer1()
	{
		mStub = new InputEventStub();
		mStub.SetProtoType(0);
		mQueue = new ArrayBlockingQueue<Message>(256);
	}
	
	/**
	 * start the touch event server 
	 * waiting for connection then wait for
	 * touch event action
	 * 
	 * @author CaiCQ
	 * Mar 5, 2015
	 */
	public void start()
	{
		init();
		if (mSvrSocketCreated)
			new HandleMessageThread().start();
		
		while (true)
		{
			listen();
			readMessageLoop();
			CLog.i(TAG, "connection has closed!!!");
		}
	}

	public void stop()
	{

	}

	/**
	 * 建立服务端的socket
	 * 
	 * @author CaiCQ
	 * Mar 5, 2015
	 */
	private void init()
	{
		for (int i = 0; i < 30; i++)
		{
			try
			{
				CLog.i(TAG, "ready to create server socket.");
				mSvrSocket = new ServerSocket(ConfigConstants.SERVER_PORT);
				mSvrSocketCreated = true;
				CLog.i(TAG, "create server socket successfully.");
				break;
			}
			catch (IOException e)
			{
				e.printStackTrace();
				try
				{
					Thread.sleep(1000);
				}
				catch (InterruptedException e1)
				{
					e1.printStackTrace();
				}
			}
		}
	}

	/**
	 * 监听客户端连接
	 * 
	 * @author CaiCQ
	 * Mar 5, 2015
	 */
	private void listen()
	{
		if (!mSvrSocketCreated)
			return;
		try
		{
			CLog.i(TAG, "Server is running");
			CLog.i(TAG, "Server is listening...");
			Socket socket = mSvrSocket.accept();
			mInput = socket.getInputStream();
			mOutput = socket.getOutputStream();
			CLog.d(TAG, "a client connected.");
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

	}

	/**
	 * 接收到一個消息包放到队列中
	 * 
	 * @author CaiCQ
	 * Mar 9, 2015
	 */
	private int readMessageLoop()
	{
		int count = 0;
		int msgLength = 0;
		while (true)
		{
			msgLength = readLength();
			if (msgLength < 0)
				break;
			else if (msgLength > ConfigConstants.MAX_MSG_LENGTH)
				continue;

			try
			{
				byte[] buf = null;
				byte[] array = new byte[msgLength];
				ByteBuffer buffer = ByteBuffer.wrap(array);
				while (buffer.hasRemaining())
				{
					buf = new byte[buffer.remaining()];
					if ((count = mInput.read(buf)) < 0)
						return count;
					buffer.put(buf, buffer.position(), count);
				}
				Message msg = Message.parseFrom(array);
				mQueue.put(msg);
			}
			catch (InvalidProtocolBufferException e)
			{
				e.printStackTrace();
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
			catch (IOException e)
			{
				e.printStackTrace();
				break;
			}
		}
		
		try
		{
			mInput.close();
			mOutput.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

		return msgLength;
	}
	
	private int readLength()
	{
		int len = 0;
		try
		{
			byte[] buf = new byte[4];
			
			if ((len = mInput.read(buf)) < 0)
			{
				return len;
			}
			ByteBuffer buffer = ByteBuffer.wrap(buf);
			len = buffer.getInt();
		}
		catch (IOException e)
		{
			e.printStackTrace();
			len = -2;
		}
		return len;
	}

/**
 * 保留	
 * 
 * @author CaiCQ
 * Mar 24, 2015
 */
	private void sendMessage(Message msg)
	{
		RawData raw = new RawData(msg);
		try
		{
			mOutput.write(raw.toByteBuffer().array());
			mOutput.flush();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	private class HandleMessageThread extends Thread
	{
		@Override
		public void run()
		{
			Looper.prepare();
			while (true)
			{
				try
				{
					Message msg = mQueue.take();
					printMessage(msg);
					handleMessage(msg);
				}
				catch (InterruptedException e)
				{
					e.printStackTrace();
				}
			}
		}

		private void handleMessage(Message msg)
		{
			int id = msg.getId();
			int x = msg.getX();
			int y = msg.getY();

			switch (msg.getActionType())
			{
			case TOUCH_DOWN:
				mStub.TouchDownEvent(x, y, id);
				break;
			case TOUCH_UP:
				mStub.TouchUpEvent(id);
				break;
			case TOUCH_MOVE:
				mStub.TouchMoveEvent(x, y, id, 0);
				break;
			}
		}

	}
	
	private void printMessage(Message msg)
	{
		CLog.d(TAG, "[TYPE = " + msg.getActionType() + String.format("]   id:%d  ; point(%d, %d)", msg.getId(), msg.getX(), msg.getY()));
	}
}
