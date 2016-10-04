package com.buxiubianfu.IME.command;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.buxiubianfu.IME.command.Data.CommandData;
import com.buxiubianfu.IME.command.Data.OpenAppData;
import com.google.gson.Gson;
import com.kaopu.download.util.Log;

public class OpenAppCommand extends BaseCommand {
	private Context _Context;
	private Gson gson;

	public OpenAppCommand(Context context) {
		_Context = context;
		gson = new Gson();
	}

	@Override
	public String Do(CommandData commandData) {
		String cmdData = commandData.getCmdData();
		Log.d("ime", "cmdData:"+cmdData);
		if (TextUtils.isEmpty(cmdData)) {
			return null;
		}
		OpenAppData openAppData = gson.fromJson(cmdData, OpenAppData.class);
		
		if (openAppData == null) {
			return null;
		}
		try {
			Intent intent = new Intent();
			intent.setClassName(openAppData.getPackageName(),
					openAppData.getActivityName());
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			_Context.startActivity(intent);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}
}
