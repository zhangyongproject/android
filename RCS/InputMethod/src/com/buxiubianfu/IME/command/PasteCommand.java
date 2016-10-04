package com.buxiubianfu.IME.command;

import android.annotation.SuppressLint;
import android.content.ClipboardManager;
import android.view.inputmethod.InputConnection;
import com.buxiubianfu.IME.command.Data.CommandData;

public class PasteCommand extends BaseCommand {

	private ClipboardManager _ClipboardManager;
	private InputConnection _InputConnection;
	public PasteCommand(ClipboardManager clipboardManager)
	{
		_ClipboardManager=clipboardManager;
	}
	
	@SuppressLint("NewApi")
	@Override
	public String Do( CommandData commandData) {
		if (_ClipboardManager.hasPrimaryClip()) {
			String ClipText = _ClipboardManager.getPrimaryClip()
					.getItemAt(0).getText().toString();
			// 将文本中的回车键替换成\n
			ClipText = ClipText.replaceAll(CommandConst.CLIP_ENTER, "\n");
			_InputConnection.commitText(
					((CharSequence) ClipText), 1);
		}
		
		return "";
	}

}
