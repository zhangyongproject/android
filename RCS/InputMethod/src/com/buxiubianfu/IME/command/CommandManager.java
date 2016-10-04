package com.buxiubianfu.IME.command;

import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import com.buxiubianfu.IME.command.Data.CommandData;

import android.content.ClipboardManager;
import android.content.Context;
import android.view.inputmethod.InputConnection;

public class CommandManager {

	private Map<String,ICommand> Commands=new HashMap<String,ICommand>();
	
	public CommandManager(Context context,OutputStream oStream,InputConnection inputConnection,ClipboardManager clipboardManager)
	{
		Commands.put(CommandConst.DOWNLOAD_APP, new BroadcastCommand(context));
		Commands.put(CommandConst.DOWNLOAD_PAUSE, new BroadcastCommand(context));
		Commands.put(CommandConst.TOTAL_MEMORY, new SendJsonCommand(context, oStream));
		Commands.put(CommandConst.AVAIL_MEMORY, new SendJsonCommand(context, oStream));
		Commands.put(CommandConst.KEY_DOWN_CHAR, new SetInputCommand(inputConnection));
		Commands.put(CommandConst.MOBILE_DISPLAY, new SendJsonCommand(context,oStream));
		Commands.put(CommandConst.KEY_DOWN_HOME, new HomeCommand(context));
		Commands.put(CommandConst.OPEN_APPLIACTION, new OpenAppCommand(context));
		Commands.put(CommandConst.KEY_DOWN_BACK, new BackCommand());
		Commands.put(CommandConst.KEY_DOWN_SPECIAL, new SpecialCommand(context, inputConnection));
		
		Commands.put(CommandConst.CLIP_TEXT, new ClipCommand(clipboardManager));
		Commands.put(CommandConst.COPY_TEXT, new CopyCommand(inputConnection, clipboardManager, oStream));
		Commands.put(CommandConst.CUT_TEXT,new CutCommand(inputConnection, clipboardManager, oStream));
		Commands.put(CommandConst.PASTE_TEXT, new PasteCommand(clipboardManager));
	}
	
	public void Do(CommandData cmdData)
	{
		if(cmdData.getText().indexOf(CommandConst.CLIP_TEXT)>-1)
		{
			Commands.get(CommandConst.CLIP_TEXT).Do(cmdData);
		}
		if(cmdData.getText().indexOf(CommandConst.COPY_TEXT)>-1)
		{
			Commands.get(CommandConst.COPY_TEXT).Do(cmdData);
		}
		if(cmdData.getText().indexOf(CommandConst.CUT_TEXT)>-1)
		{
			Commands.get(CommandConst.CUT_TEXT).Do(cmdData);
		}
		
		Commands.get(cmdData.getCmdName()).Do(cmdData);
	}
}
