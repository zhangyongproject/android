package com.buxiubianfu.IME.command.Data;

import com.buxiubianfu.IME.Utils;

public class DownLoadCreator extends BaseCreator {


	public DownLoadCreator(String CmdName, String CmdData) {
		
		super.SetCommandData(CmdName, CmdData);

	}

	@Override
	public CommandData Create() {
		CommandData cd = super.getCommandData();
		if ("downloadapp".equals(cd.getCmdName())) {
			cd.getParams().put(Utils.INTENT_NAME, cd.getCmdName());
			cd.getParams().put(Utils.START_DOWNLOAD_DATA, cd.getCmdData());
		} else {
			cd.getParams().put(Utils.INTENT_NAME, cd.getCmdName());
		}
		return cd;
	}

}
