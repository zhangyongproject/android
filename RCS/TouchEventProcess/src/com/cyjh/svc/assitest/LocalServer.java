/**
* <p>Title: LocalServer.java</p>
* <p>Description: </p>
* <p>Copyright: Copyright (c) 2015</p>
* <p>Company: CYJH</p>
* @author CaiCQ
* @date Mar 12, 2015
* @version 1.0
*/ 
package com.cyjh.svc.assitest;

import java.io.DataOutputStream;
import java.io.IOException;

import com.cyjh.svc.utils.CLog;
import com.cyjh.svc.utils.ConfigConstants;

import android.net.LocalServerSocket;
import android.net.LocalSocket;
import android.os.Process;

/**
 * @author CaiCQ
 *
 * Mar 12, 2015
 */
public class LocalServer
{
	private static final String TAG = ConfigConstants.COMMON_TAG;
	
	public void start()
	{
		new Thread()
		{
			public void run()
			{
				int thisPid = Process.myPid();
				CLog.i(TAG, "the pid of touch event server is " + thisPid);
				try
				{
					LocalServerSocket ssocket = new LocalServerSocket("svc_2015");
					while(true)
					{
						LocalSocket socket = ssocket.accept();
						sendBackPid(socket);
						socket.close();
					}
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
			}
		}.start();
	}
	
	/**
	 * 回送本进程的进程ID 
	 * 
	 * @author CaiCQ
	 * Mar 12, 2015
	 */
	public void sendBackPid(LocalSocket socket)
	{
		int thisPid = Process.myPid();
		CLog.i(TAG, "the pid of touch event server is " + thisPid);
		DataOutputStream dos;
		try
		{
			dos = new DataOutputStream(socket.getOutputStream());
			dos.writeInt(thisPid);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
}
