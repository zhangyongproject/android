package com.cyjh.svc.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.util.Log;

public final class CLog
{
	private static final boolean mIsDebug = true;
	private static SimpleDateFormat mFormat = new SimpleDateFormat("HH:mm:ss");

	//	print log lick android.util.Log
	public static void v(String TAG, String msg)
	{
		if (mIsDebug)
		{
			Log.v(TAG, "  ["+ mFormat.format(new Date())+"]  " + msg);
			System.out.println("["+ mFormat.format(new Date())+"]  " + "[" + TAG + "]  " + msg);
		}
	}

	public static void e(String TAG, String msg)
	{
		if (mIsDebug)
		{
			Log.e(TAG, "  ["+ mFormat.format(new Date())+"]  " + msg);
			System.out.println("["+ mFormat.format(new Date())+"]  " + "[" + TAG + "]  " + msg);
		}
	}

	public static void w(String TAG, String msg)
	{
		if (mIsDebug)
		{
			Log.w(TAG, "  ["+ mFormat.format(new Date())+"]  " + msg);
			System.out.println("["+ mFormat.format(new Date())+"]  " + "[" + TAG + "]  " + msg);
		}
	}

	public static void d(String TAG, String msg)
	{
		if (mIsDebug)
		{
			Log.d(TAG, "  ["+ mFormat.format(new Date())+"]  " + msg);
			System.out.println("["+ mFormat.format(new Date())+"]  " + "[" + TAG + "]  " + msg);
		}
	}

	public static void i(String TAG, String msg)
	{
		if (mIsDebug)
		{
			Log.i(TAG, "  ["+ mFormat.format(new Date())+"]  " + msg);
			System.out.println("["+ mFormat.format(new Date())+"]  " + "[" + TAG + "]  " + msg);
		}
	}

}
