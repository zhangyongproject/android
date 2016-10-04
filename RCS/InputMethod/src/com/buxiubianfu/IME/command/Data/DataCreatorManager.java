package com.buxiubianfu.IME.command.Data;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;

import com.buxiubianfu.IME.IME;
import com.buxiubianfu.IME.Utils;
import com.buxiubianfu.IME.command.CommandConst;

public class DataCreatorManager {

	
	
	private Map<String,IParamCreator>  ParamCreators=new HashMap<String,IParamCreator>();
	
	public DataCreatorManager(String CmdName,String CmdData,String text,Context context)
	{
		 ParamCreators.put(CommandConst.DOWNLOAD_APP, new DownLoadCreator(CmdName,CmdData));
		 ParamCreators.put(CommandConst.DOWNLOAD_PAUSE, new DownLoadCreator(CmdName,CmdData));
		 ParamCreators.put(CommandConst.TOTAL_MEMORY, new BaseCreator(CmdName,CmdData,CommandConst.TOTAL_MEMORY,Utils.getSDTotalSize()));
		 ParamCreators.put(CommandConst.AVAIL_MEMORY, new BaseCreator(CmdName,CmdData,CommandConst.AVAIL_MEMORY,Utils.getSDAvailableSize()));
		 ParamCreators.put(CommandConst.KEY_DOWN_CHAR, new BaseCreator(CmdName, CmdData, CommandConst.KEY_DOWN_CHAR, CmdData));
		 ParamCreators.put(CommandConst.MOBILE_DISPLAY, new BaseCreator(CmdName, CmdData, CommandConst.MOBILE_DISPLAY, Utils.getDisplay(context)));
		 ParamCreators.put(CommandConst.KEY_DOWN_HOME, new BaseCreator(CmdName,CmdData));
		 ParamCreators.put(CommandConst.KEY_DOWN_BACK,new BaseCreator(CmdName,CmdData));
		 ParamCreators.put(CommandConst.KEY_DOWN_SPECIAL, new BaseCreator(CmdName,CmdData));
		 
		 ParamCreators.put(CommandConst.CLIP_TEXT, new BaseCreator(CmdName, CmdData, text));
		 ParamCreators.put(CommandConst.COPY_TEXT, new BaseCreator(CmdName, CmdData, text));
		 ParamCreators.put(CommandConst.CUT_TEXT, new BaseCreator(CmdName, CmdData, text));
		 ParamCreators.put(CommandConst.PASTE_TEXT, new BaseCreator(CmdName, CmdData, text));
	}
	
	public CommandData Create(String CmdName,String text)
	{
		if(text.indexOf(CommandConst.CLIP_TEXT)>-1)
		{
			return ParamCreators.get(CommandConst.CLIP_TEXT).Create();
		}
		if(text.indexOf(CommandConst.COPY_TEXT)>-1)
		{
			return ParamCreators.get(CommandConst.COPY_TEXT).Create();
		}
		if(text.indexOf(CommandConst.CUT_TEXT)>-1)
		{
			return ParamCreators.get(CommandConst.CUT_TEXT).Create();
		}
		
		return ParamCreators.get(CmdName).Create();
		
	}
	
	
}
