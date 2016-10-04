package com.buxiubianfu.IME.command;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import com.buxiubianfu.IME.command.Data.CommandData;

public class ClipCommand extends BaseCommand {

	private ClipboardManager _ClipboardManager;
	public ClipCommand(ClipboardManager clipboardManager)
	{
		_ClipboardManager=clipboardManager;
		
	}
	
	
	
	@SuppressLint("NewApi")
	@Override
	public String Do( CommandData commandData) {
		String text=commandData.getText();
		if (text.length() >= CommandConst.CLIP_TEXT.length()) {
			text = text.substring(CommandConst.CLIP_TEXT.length());
			String SendText=text;
			
			_ClipboardManager.setPrimaryClip(ClipData.newPlainText(null,
					SendText));
		}
		return "";
	}

}
