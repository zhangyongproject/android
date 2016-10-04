package com.blue.uyou.gamelauncher.app;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedData {
	private static SharedData sharedData = new SharedData();
	private  static final String KEY_FILE = "mysharddata";
	private SharedPreferences mSharedPreferences = null;

	private SharedData() {
	}

	public static SharedData getInstance() {
		return sharedData;
	}

	public void init(Context context) {
		mSharedPreferences = context.getSharedPreferences(KEY_FILE,
				Context.MODE_PRIVATE);
	}

	public void removeData(String key) {
		mSharedPreferences.edit().remove(key).commit();
	}

	public void saveData(String key, int value) {
		mSharedPreferences.edit().putInt(key, value).commit();
	}

	public void saveData(String key, boolean value) {
		mSharedPreferences.edit().putBoolean(key, value).commit();
	}

	public void saveData(String key, float value) {
		mSharedPreferences.edit().putFloat(key, value).commit();
	}

	public void saveData(String key, long value) {
		mSharedPreferences.edit().putLong(key, value).commit();
	}

	public void saveData(String key, String value) {
		mSharedPreferences.edit().putString(key, value).commit();
	}

	public boolean readData(String key, boolean defValue) {
		return mSharedPreferences.getBoolean(key, defValue);
	}

	public float readData(String key, float defValue) {
		return mSharedPreferences.getFloat(key, defValue);
	}

	public int readData(String key, int defValue) {
		return mSharedPreferences.getInt(key, defValue);
	}

	public long readData(String key, long defValue) {
		return mSharedPreferences.getLong(key, defValue);
	}

	public String readData(String key, String defValue) {
		return mSharedPreferences.getString(key, defValue);
	}
}
