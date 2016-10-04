package com.buxiubianfu.IME;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.app.ActivityManager;
import android.app.ActivityManager.MemoryInfo;
import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.os.StatFs;
import android.text.format.Formatter;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;

public class Utils {

	public final static String ACTION_NAME = "ACTION_NAME";

	public final static String ACTION_NAME_1 = "ACTION_NAME_1";

	public final static String INTENT_NAME = "INTENT_NAME";

	public final static String START_DOWNLOAD_DATA = "START_DOWNLOAD_DATA";

	public final static String FINISH_DOWNLOAD = "FINISH_DOWNLOAD_DATA";

	/**
	 * ��ǰ�ڴ�ʹ�õĴ�С
	 * 
	 * @param context
	 * @return
	 */
	public static String getAvailMemory(Context context) {
		// ��ȡandroid��ǰ�����ڴ��С
		ActivityManager am = (ActivityManager) context
				.getSystemService(Context.ACTIVITY_SERVICE);
		MemoryInfo mi = new MemoryInfo();
		am.getMemoryInfo(mi);
		// mi.avaiMem;��ǰϵͳ�����ڴ�
		return Formatter.formatFileSize(context, mi.availMem);
		// ����õ��ڴ��С���
	}

	/**
	 * �ڴ��ܿռ��С
	 * 
	 * @param context
	 * @return
	 */
	public static String getTotalMemory(Context context) {
		String str1 = "/proc/meminfo";// ϵͳ�ڴ���Ϣ�ļ�
		String str2;
		String[] arrayOfString;
		long initial_memory = 0;

		try {
			FileReader localFileReader = new FileReader(str1);
			BufferedReader localBufferedReader = new BufferedReader(
					localFileReader, 8192);
			str2 = localBufferedReader.readLine();// ��ȡmeminfo��һ�У�ϵͳ�ڴ��С
			arrayOfString = str2.split("\\s+");
			for (String num : arrayOfString) {
				Log.i(str2, num + "\t");
			}
			initial_memory = Integer.valueOf(arrayOfString[1]).intValue() * 1024;// ���ϵͳ���ڴ棬��λKB
			localBufferedReader.close();
		} catch (IOException e) {

		}
		return Formatter.formatFileSize(context, initial_memory);
		// ByteתλKB��MB
	}

	/**
	 * ����ʣ��ռ�
	 * 
	 * @param path
	 * @return
	 */
	public static long getAvailableSize(String path) {
		StatFs fileStats = new StatFs(path);
		fileStats.restat(path);
		return fileStats.getAvailableBlocksLong()
				* fileStats.getBlockSizeLong(); // ע����fileStats.getFreeBlocks()������
	}

	/**
	 * �����ܿռ�
	 * 
	 * @param path
	 * @return
	 */
	public static long getTotalSize(String path) {
		StatFs fileStats = new StatFs(path);
		fileStats.restat(path);
		return fileStats.getBlockCountLong() * fileStats.getBlockSizeLong();
	}

	/**
	 * ��ȡSD�����ܿռ�
	 * 
	 * @return
	 */
	public static String getSDTotalSize() {
		if (Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {
			return getTotalSize(Environment.getExternalStorageDirectory()
					.toString()) / (1024 * 1024) + "";
		}
		return "";
	}

	/**
	 * ����SD����ʣ��ռ�
	 * 
	 * @return ʣ��ռ�
	 */
	public static String getSDAvailableSize() {
		if (Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {
			return getAvailableSize(Environment.getExternalStorageDirectory()
					.toString()) / (1024 * 1024) + "";
		}
		return "";
	}

	/**
	 * ��ȡ�ֻ��ֱ���
	 * 
	 * @param context
	 * @return
	 */
	public static String getDisplay(Context context) {
		// ��ȡ��Ļ�ܶȣ�����1��
		DisplayMetrics dm = new DisplayMetrics();
		dm = context.getResources().getDisplayMetrics();
		int screenWidth = dm.widthPixels; // ��Ļ�����أ��磺480px��
		int screenHeight = dm.heightPixels; // ��Ļ�ߣ����أ��磺800px��

		String dinsityStr = screenWidth + "*" + screenHeight;
		return dinsityStr;
	}

	public static void KeyDownHome(Context context) {
		Intent intent = new Intent(Intent.ACTION_MAIN);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.addCategory(Intent.CATEGORY_HOME);
		context.startActivity(intent);
	}

	/*public static void KeyDownDpadLeft() {
		// Runtime runtime = Runtime.getRuntime();
		// try {
		// runtime.exec("input keyevent " + KeyEvent.KEYCODE_DPAD_LEFT);
		// } catch (IOException e) {
		// e.printStackTrace();
		// }
		//
		simulateKeystroke(KeyEvent.KEYCODE_DPAD_LEFT);
	}*/


	/**
	 * ����json
	 * 
	 * @param text
	 * @return
	 */
	public static List<String> parserJson(String text) {
		try {
			List<String> list = new ArrayList<String>();
			JSONTokener jsonParser = new JSONTokener(text);
			// �����ʱ�Ķ�ȡλ����"name" :
			// �ˣ���ônextValue����"yuanzhifei89"��String��
			JSONObject person = (JSONObject) jsonParser.nextValue();
			// �������ľ���JSON����Ĳ�����
			String cmdName = person.getString("cmd");
			String cmdData = person.getString("data");
			list.add(cmdName);
			list.add(cmdData);
			return list;
		} catch (JSONException ex) {
			// �쳣�������
			return null;
		}

	}

	public static void simulateKeystroke(final int KeyCode) {
		new Thread(new Runnable() {
			public void run() {
				try {
					Instrumentation inst = new Instrumentation();
					inst.sendKeyDownUpSync(KeyCode);
				} catch (Exception e) {
				}
			}
		}).start();
	}

}
