package com.cyjh.screenmirror;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import android.annotation.SuppressLint;
import android.graphics.Point;
import android.hardware.input.InputManager;
import android.os.Looper;
import android.os.RemoteException;
import android.os.SystemClock;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;

	@SuppressLint("NewApi") 
	public class Main {

	public static final String LOGTAG = "MirrorDisplay";
	
	public static void main(String[] args) {
	    BroadcastService.isConnected.delete();
		Thread.currentThread().setName("mirror_service");
		Looper.prepare();
				
//		VirtualDisplayFactory vdf = new SurfaceControlVirtualDisplayFactory();
//		
//		Point size = SurfaceControlVirtualDisplayFactory.getCurrentDisplaySize();
//		Log.d(LOGTAG, "displaySize " + size.x + "x" +size.y);
//		
//		OutputDevice mirror = new OutputDevice(size.x, size.y);
//		mirror.registerVirtualDisplay(null, vdf, 0);
		
        MirrorServer server = MirrorServer.getMirrorServer();
        server.start();
        
        Looper.loop();
	}
	
	
}
