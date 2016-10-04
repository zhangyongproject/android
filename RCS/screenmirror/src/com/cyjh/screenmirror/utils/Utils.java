package com.cyjh.screenmirror.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;

import android.graphics.Bitmap;
import android.util.Log;

public class Utils {
	public static final String LOGTAG = "MirrorDislay";
	public static int bytes2Int(byte[] src, int offset) {
		int value;
		
		value = (int) ((src[offset] &0xff)
				| ((src[offset+1]&0xff)<<8)
				| ((src[offset+2]&0xff)<<16) 
				| ((src[offset+3]&0xff)<<24));
		return value;
	}
	
	public static byte[] int2Bytes(int src) {
		byte value[] = new byte[4];
		
		value[0] = (byte)(src & 0xFF);
		value[1] = (byte)((src >> 8) & 0xFF);
		value[2] = (byte)((src >> 16) & 0xFF);
		value[3] = (byte)((src >> 24) & 0xFF);
		return value;
	}
	public static void int2Bytes(int src,byte value[],int offset) {
        
        value[offset++] = (byte)(src & 0xFF);
        value[offset++] = (byte)((src >> 8) & 0xFF);
        value[offset++] = (byte)((src >> 16) & 0xFF);
        value[offset++] = (byte)((src >> 24) & 0xFF);
    }
	public static String getProperty(String key) {
		try {
			Class<?> SystemProperties = Class.forName("android.os.SystemProperties");
			Method getMethod = SystemProperties.getDeclaredMethod("get", new Class<?>[]{String.class});
			String device = (String)getMethod.invoke(null, new Object[]{key});
			return device;
		}catch(ClassNotFoundException e) {	
		}catch(NoSuchMethodException e) {	
		}catch(InvocationTargetException e) {
		}catch(IllegalAccessException e) {
		}
		return null;
	}
	
	public static boolean supportsVDF() {
		String device = getProperty("ro.fota.device");
		if(device !=null && device.equals("uyou001")) {
			Log.w("MirrorDisplay", "uyou buggy");
			return false;
		}
		Log.i("MirrorDisplay", "supportsVDF");
		return true;
	}
	
	public static Method getScreenshotMethod() {
		try {
			Class<?> SurfaceControlClass = Class.forName("android.view.SurfaceControl");
			Method screenshot = SurfaceControlClass.getDeclaredMethod("screenshot", new Class<?>[]{int.class, int.class});
			return screenshot;
		}catch(ClassNotFoundException e) {	
			Log.i("MirrorDisplay", "SurfaceControl not found");
			return null;
		}catch(NoSuchMethodException e) {
			Log.i("MirrorDisplay", "screenshot method not found");
			return null;
		}
	}
	public static Bitmap screenshot(int w, int h) {
		if(screenshotMethod == null) {
			screenshotMethod = getScreenshotMethod();
		}
		if(screenshotMethod == null) {
			Log.w(LOGTAG, "Cannot find screenshot method");
			return null;
		}
		try {
			Bitmap bitmap = (Bitmap)screenshotMethod.invoke(null, new Object[]{w, h});
			return bitmap;
		}catch(IllegalAccessException e) {
			Log.i("MirrorDisplay", "screenshot illegal access");
			return null;
		}catch(InvocationTargetException e) {
			Log.i("MirrorDisplay", "screenshot target access");
			return null;
		}
	
	}
	
	public static InetAddress getWiredAddr() {
		try {
			NetworkInterface eth0 = NetworkInterface.getByName("eth0");
			int sz = eth0.getInterfaceAddresses().size();
			for(int i=0; i<sz; i++) {
				InetAddress addr = eth0.getInterfaceAddresses().get(i).getAddress();
				if(addr != null)
					return addr;
			}
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NullPointerException e) {
		} catch (Exception e) {			
		}
		return null;
	}
	
	public static  InetAddress getWiredBroadcast () {
		try {
			NetworkInterface eth0 = NetworkInterface.getByName("eth0");
			int sz = eth0.getInterfaceAddresses().size();
			for(int i=0; i<sz; i++) {
				InetAddress ba = eth0.getInterfaceAddresses().get(i).getBroadcast();
				if(ba !=null)
					return ba;
			}
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NullPointerException e) {
		} catch (Exception e) {
		}
		return null;
	}
	
	public static InetAddress getWlanAddr() {
		try {
			NetworkInterface wlan0 = NetworkInterface.getByName("wlan0");
			int sz = wlan0.getInterfaceAddresses().size();
			for(int i=0; i<sz; i++) {
				InetAddress addr = wlan0.getInterfaceAddresses().get(i).getAddress();
				if(addr!=null)
					return addr;
			}
		}catch (SocketException e) {
			e.printStackTrace();
		}catch(NullPointerException e) {
		}catch(Exception e) {
		}
		return null;
	}
	
	public static InetAddress getWlanBroadcast() {
		try {
			NetworkInterface wlan0 = NetworkInterface.getByName("wlan0");
			int sz = wlan0.getInterfaceAddresses().size();
			for (int i=0; i<sz; i++) {
				InetAddress ba = wlan0.getInterfaceAddresses().get(i).getBroadcast();
				if(ba !=null)
					return ba;
			}
		}catch(SocketException e) {
			e.printStackTrace();
		}catch(NullPointerException e) {
		}catch(Exception e) {
		}
		return null;
	}
	
	public static String getApkPath()
	{
		String cmdline = "pm path com.cyjh.screenmirror\n";
		try {
			Process proc = Runtime.getRuntime().exec(cmdline);
			BufferedReader br =new BufferedReader(new InputStreamReader(proc.getInputStream()));
			String apkPath = br.readLine();
			return apkPath.substring("package:".length());
			
		}catch(IOException e) {
			return "";
		}
	}
	private static Method screenshotMethod = null;
}
