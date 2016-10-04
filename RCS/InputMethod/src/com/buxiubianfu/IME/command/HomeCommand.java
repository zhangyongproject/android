package com.buxiubianfu.IME.command;

import java.util.Map;

import android.content.Context;



import com.buxiubianfu.IME.Utils;
import com.buxiubianfu.IME.command.Data.CommandData;

public class HomeCommand extends BaseCommand {

	private Context _Context;
	public HomeCommand(Context context)
	{
		_Context=context;
	}
	@Override
	public String Do(CommandData commandData) {
		Utils.KeyDownHome(_Context);
		return null;
	}

}
