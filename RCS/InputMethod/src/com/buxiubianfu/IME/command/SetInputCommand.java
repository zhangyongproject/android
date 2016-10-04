package com.buxiubianfu.IME.command;

import java.util.Map;

import com.buxiubianfu.IME.command.Data.CommandData;

import android.view.inputmethod.InputConnection;
public class SetInputCommand extends BaseCommand {
	
	private InputConnection _InputConnection;
	public SetInputCommand(InputConnection inputConnection)
	{
		_InputConnection=inputConnection;
	}
	
	@Override
	public String Do(CommandData commandData) {
		// 将文本中的回车键替换成\n
		String inputContent=commandData.getParams().get(CommandConst.KEY_DOWN_CHAR).toString();
				inputContent = inputContent.replaceAll(CommandConst.CLIP_ENTER, "\n");
				_InputConnection.commitText(
						((CharSequence) inputContent), 1);
		return "";
	}

}
