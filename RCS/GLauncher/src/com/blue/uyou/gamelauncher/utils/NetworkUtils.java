package com.blue.uyou.gamelauncher.utils;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

public class NetworkUtils {
	public static String TAG = "NetworkUtils";

	public static boolean isNetworkAvailable(Context ctx) {
		ConnectivityManager cm = (ConnectivityManager) ctx
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (cm == null) {
			return false;
		}
		NetworkInfo info = cm.getActiveNetworkInfo();
		if (info != null) {
			return info.isAvailable();
		}
		return false;
	}

	public static int getWifiSignalIntensity(Context context) {
		int num = 0;
		WifiManager mWifiManager = (WifiManager) context
				.getSystemService(Context.WIFI_SERVICE);
		WifiInfo mWifiInfo = mWifiManager.getConnectionInfo();
		int wifi = mWifiInfo.getRssi();// 获取wifi信号强度
		num = WifiManager.calculateSignalLevel(wifi, 5);
		return num;
	}

	public static NetworkInfo getActiveNetworInfo(Context ctx) {
		ConnectivityManager cm = (ConnectivityManager) ctx
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (cm != null) {
			return cm.getActiveNetworkInfo();
		}
		return null;
	}

	public static String getNetworkName(Context context) {
		String networkName = null;
		if (context != null) {
			int networkType = getNetworkType(context);
			if (ConnectivityManager.TYPE_WIFI == networkType) {
				networkName = getSSID(context);
			} else if (ConnectivityManager.TYPE_ETHERNET == networkType) {
				networkName = "eth";
			}
		}
		return networkName;
	}

	public static String getSSID(Context context) {
		String ssid = "";
		if (context != null) {
			int networkType = getNetworkType(context);
			if (ConnectivityManager.TYPE_WIFI == networkType) {
				WifiManager mWifiManager = (WifiManager) context
						.getSystemService(Context.WIFI_SERVICE);
				ssid = mWifiManager.getConnectionInfo().getSSID();
				Log.d(TAG, "ssid:" + ssid);
			}
		}
		if (StringUtils.isNotEmpty(ssid)) {
			return StringUtils.replace(ssid, "\"", StringUtils.EMPTY);
		}
		return ssid;
	}

	public static int getNetworkType(Context context) {
		int result = -1;
		if (context != null) {
			ConnectivityManager mConnectivityManager = (ConnectivityManager) context
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo mNetworkInfo = mConnectivityManager
					.getActiveNetworkInfo();
			Log.d(TAG, "mNetworkInfo:" + mNetworkInfo);
			if (mNetworkInfo != null) {
				boolean available = mNetworkInfo.isAvailable();
				Log.d(TAG, "available:" + available);
				if (available) {
					result = mNetworkInfo.getType();
				}
			}
		}
		return result;
	}

	public static boolean isNetworkConnected(Context context) {
		if (context != null) {
			ConnectivityManager mConnectivityManager = (ConnectivityManager) context
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo mNetworkInfo = mConnectivityManager
					.getActiveNetworkInfo();
			if (mNetworkInfo != null) {
				return mNetworkInfo.isAvailable();
			}
		}
		return false;
	}

	public static boolean isAppRunningTop(Context context) {
		Log.d(TAG, "getIMAppRunningTop");

		boolean isAppRunningTop = false;
		ActivityManager activityManager = (ActivityManager) context
				.getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningTaskInfo> tasksInfo = activityManager.getRunningTasks(1);
		if (tasksInfo.size() > 0) {
			if (tasksInfo.get(0).topActivity.getPackageName().equals(
					"com.tcl.boxui")) {
				isAppRunningTop = true;
			}
		}
		Log.d(TAG, "isAppRunningTop->" + isAppRunningTop);

		return isAppRunningTop;
	}

	protected static boolean isTopActivity(Context context) {
		String activityName = "com.tcl.boxui.network.NetworkSettingsActivity";
		ActivityManager activityManager = (ActivityManager) context
				.getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningTaskInfo> tasksInfo = activityManager.getRunningTasks(1);
		if (tasksInfo.size() > 0) {
			System.out.println("---------------包名-----------"
					+ tasksInfo.get(0).topActivity.getClassName());
			// 应用程序位于堆栈的顶层
			if (activityName
					.equals(tasksInfo.get(0).topActivity.getClassName())) {
				return true;
			}
		}
		return false;
	}
}
