package com.blue.uyou.gamelauncher.utils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.util.Log;
import android.widget.Toast;

import com.blue.uyou.gamelauncher.R;
public class AppUtils {
	private static final String TAG = AppUtils.class.getSimpleName();

	public static boolean isInstall(Context c, String pkgName) {
		PackageInfo packInfo = null;
		try {
			packInfo = c.getPackageManager().getPackageInfo(pkgName,
					PackageManager.GET_ACTIVITIES);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		if (packInfo != null) {
			return true;
		} else {
			return false;
		}
	}

	public static String getVersion(Context context) {
		try {
			PackageManager manager = context.getPackageManager();
			PackageInfo info = manager.getPackageInfo(context.getPackageName(),
					0);
			return info.versionName;
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
	}

	public static void startActivity(Context c, Intent intent) {
		if (intent == null) {
			Toast.makeText(c, c.getString(R.string.not_found_app),
					Toast.LENGTH_SHORT).show();
			Log.e(TAG, "intent is null");
			return;
		}
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		try {
			c.startActivity(intent);
		} catch (Exception e) {
			Toast.makeText(c, c.getString(R.string.start_app_error),
					Toast.LENGTH_SHORT).show();
		}
	}

	public static void startActivity(Context c, Intent intent, String errMsg) {
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		try {
			c.startActivity(intent);
		} catch (Exception e) {
			Toast.makeText(c, errMsg, Toast.LENGTH_SHORT).show();
		}
	}
}
