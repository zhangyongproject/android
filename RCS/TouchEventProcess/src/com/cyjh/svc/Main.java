/**
 * <p>Title: Main.java</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2015</p>
 * <p>Company: CYJH</p>
 * @author CaiCQ
 * @date Mar 3, 2015
 * @version 1.0
 */
package com.cyjh.svc;

import com.cyjh.svc.assitest.LocalServer;
import com.cyjh.svc.socket.TouchEventServer;
import com.cyjh.svc.utils.CLog;

/**
 * @author CaiCQ
 * 
 *         Mar 3, 2015
 */
public class Main
{
	private static final String TAG=Main.class.getSimpleName();
	
	/**
	 * 
	 * @author CaiCQ Mar 3, 2015
	 */
	public static void main(String[] args)
	{
		new Main().run(args);
	}

	private void run(String[] args)
	{
		new LocalServer().start();
		new TouchEventServer().start();
		CLog.i(TAG, "Main Thread ends.");
		System.exit(0);
	}
}
