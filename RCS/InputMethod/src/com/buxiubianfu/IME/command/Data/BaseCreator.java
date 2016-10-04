package com.buxiubianfu.IME.command.Data;

public class BaseCreator implements IParamCreator {

	public BaseCreator() {
	}

	public BaseCreator(String CmdName, String CmdData) {
		SetCommandData(CmdName, CmdData);
	}

	public BaseCreator(String CmdName, String CmdData, String text) {
		SetCommandData(CmdName, CmdData);
		CommandData.setText(text);
	}

	public BaseCreator(String CmdName, String CmdData, String key, Object value) {
		SetCommandData(CmdName, CmdData);
		CommandData.getParams().put(key, value);
	}

	@Override
	public CommandData Create() {
		return CommandData;
	}

	private CommandData CommandData;

	public CommandData getCommandData() {
		return CommandData;
	}

	public void setCommandData(CommandData commandData) {
		CommandData = commandData;
	}

	public void SetCommandData(String CmdName, String CmdData) {

		CommandData.setCmdData(CmdData);
		CommandData.setCmdName(CmdName);

	}

}
