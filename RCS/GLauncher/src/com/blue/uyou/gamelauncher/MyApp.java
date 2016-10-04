package com.blue.uyou.gamelauncher;

import android.app.Application;
import android.content.Intent;
import android.widget.Toast;

import com.blue.uyou.gamelauncher.app.IconCache;
import com.blue.uyou.gamelauncher.app.LauncherModel;
import com.blue.uyou.gamelauncher.app.LauncherModel.Callbacks;

public class MyApp extends Application {
	public LauncherModel mModel;
	public IconCache mIconCache;

	@Override
	public void onCreate() {
		super.onCreate();
		mIconCache = new IconCache(this);
		mModel = new LauncherModel(this, mIconCache);
		try {
			startService(new Intent("com.buxiubianfu.action.INPUT"));
		} catch (Exception e) {
			e.printStackTrace();
			Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
		}
		try {
			startService(new Intent("com.blue.uyou.inputservice.InputService"));
		} catch (Exception e) {
			e.printStackTrace();
			Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
		}
		// new Thread() {
		// @Override
		// public void run() {
		// execRootCmd("/system/bin/start_ajs &");
		//
		// }
		// }.start();
	}

	// private static String execRootCmd(String cmd) {
	// String result = "";
	// DataOutputStream dos = null;
	// DataInputStream dis = null;
	//
	// try {
	// Process p = Runtime.getRuntime().exec("su");
	// dos = new DataOutputStream(p.getOutputStream());
	// dis = new DataInputStream(p.getInputStream());
	// System.out.println(cmd);
	//
	// dos.writeBytes(cmd + "\n");
	// dos.flush();
	// dos.writeBytes("exit\n");
	// dos.flush();
	//
	// String line = null;
	// while ((line = dis.readLine()) != null) {
	// result += line;
	// }
	// p.waitFor();
	// } catch (Exception e) {
	// e.printStackTrace();
	// } finally {
	// if (dos != null) {
	// try {
	// dos.close();
	// } catch (IOException e) {
	// e.printStackTrace();
	// }
	// }
	// if (dis != null) {
	// try {
	// dis.close();
	// } catch (IOException e) {
	// e.printStackTrace();
	// }
	// }
	// }
	// return result;
	//
	// }
	// private class StreamGobbler extends Thread {
	//
	// InputStream is;
	// String type;
	//
	// public StreamGobbler(InputStream is, String type) {
	// this.is = is;
	// this.type = type;
	// }
	//
	// public void run() {
	// try {
	// InputStreamReader isr = new InputStreamReader(is);
	// BufferedReader br = new BufferedReader(isr);
	// String line = null;
	// while ((line = br.readLine()) != null) {
	// if (type.equals("Error")) {
	// System.out.println("Error	:" + line);
	// } else {
	// System.out.println("Debug:" + line);
	// }
	// }
	// } catch (IOException ioe) {
	// ioe.printStackTrace();
	// }
	// }
	// }
	public LauncherModel setLauncher(Callbacks callbacks) {
		mModel.initialize(callbacks);
		return mModel;
	}

	public LauncherModel getLauncherModel() {
		return mModel;
	}
}
