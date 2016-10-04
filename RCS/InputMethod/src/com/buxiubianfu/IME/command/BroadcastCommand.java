package com.buxiubianfu.IME.command;

import java.util.Map;

import com.buxiubianfu.IME.Utils;
import com.buxiubianfu.IME.command.Data.CommandData;

import android.content.Context;
import android.content.Intent;

public class BroadcastCommand extends BaseCommand {

	public BroadcastCommand(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	@Override
	public String Do( CommandData commandData) {
		Intent mIntent = new Intent(Utils.ACTION_NAME);
		for (Map.Entry<String, Object> s : commandData.getParams().entrySet()) {
			mIntent.putExtra(s.getKey(), s.getValue().toString());
		}
		super.get_Context().sendBroadcast(mIntent);
		return null;
	}



}
