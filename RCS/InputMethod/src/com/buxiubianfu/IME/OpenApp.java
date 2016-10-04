package com.buxiubianfu.IME;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

public class OpenApp {

	private static String packName = "";

	public static String isTargetGame(Context context) {
		try {
			ActivityManager am = (ActivityManager) context
					.getSystemService(Context.ACTIVITY_SERVICE);
			ComponentName cn = am.getRunningTasks(1).get(0).topActivity;
			// Toast.makeText(context, cn.getPackageName(),
			// Toast.LENGTH_SHORT).show();
			PackageInfo info = PackageUtil.getPackageInfo(context,
					cn.getPackageName());
			PackageManager pm = context.getPackageManager();
			String name = info.applicationInfo.loadLabel(pm).toString();
			if (!packName.equals(info.packageName)) {
				packName = info.packageName;
				// Toast.makeText(context, packName+"="+name,
				// Toast.LENGTH_LONG).show();

				return packName + "=" + name;
			}
			//
			// List<RunningTaskInfo> tasksInfo =
			// activityManager.getRunningTasks(1);
			// List<String> packageNames = new ArrayList<String>();
			// ActivityManager mActivityManager = (ActivityManager)
			// context.getSystemService(Context.ACTIVITY_SERVICE);
			// List<RunningAppProcessInfo> appProcessInfos =
			// mActivityManager.getRunningAppProcesses();
			// for (RunningAppProcessInfo appProcess : appProcessInfos) {
			// if (appProcess.importance ==
			// RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
			// packageNames.add(appProcess.processName);
			// Toast.makeText(context, appProcess.processName,
			// Toast.LENGTH_SHORT).show();
			// }
			// }

			// String packageName = "";
			// SupportedGame game = Util.getRunGame(packageNames);
			//
			// if (game != null) {
			// packageName = game.getPackageName();
			// if (!currPackageName.equals(packageName)) {
			// FloatWindowManager.setCurrPackageName(packageName);
			// currPackageName = packageName;
			// FloatWindowManager.setStatus(game.getAppFloatStatus());
			// if (game.getAppFloatStatus() ==
			// FloatWindowManager.STATUS_SHOW_SHARE_IMAGE) {
			// // �ж���Ϸ��ͼ��Map�����Ƿ��б������Ϸ�Ľ�ͼbitmap
			// if ((FloatWindowScreenCapView.screencapMap.get(currPackageName))
			// != null) {
			// FloatWindowScreenCapView.isScreenCap = false;
			// } else {
			// FloatWindowScreenCapView.isScreenCap = true;
			// }
			// }
			// }
			// return true;
			// } else {
			// // �뿪��Ϸʱ
			// if (!currPackageName.equals(packageName)) {
			// FloatWindowManager.setCurrPackageName("");
			// SupportedGameDao.getInstance().updateFloatStatusByPackageName(currPackageName,
			// FloatWindowManager.getStatus());
			// FloatWindowManager.setStatus(FloatWindowManager.STATUS_HIDE);
			// currPackageName = packageName;
			// }
			// return false;
			//
			// }
			//
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}

}
