package com.buxiubianfu.IME.command;



import android.view.KeyEvent;

import com.buxiubianfu.IME.Utils;
import com.buxiubianfu.IME.command.Data.CommandData;

public class BackCommand extends BaseCommand {

	@Override
	public String Do(CommandData commandData) {
		Utils.simulateKeystroke(KeyEvent.KEYCODE_BACK);
	return "";
	}

}
