package com.buxiubianfu.IME.command.Data;

import java.util.HashMap;
import java.util.Map;

public class CommandData {

	private String CmdName;
	private String CmdData;
	private String text;
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	private Map<String,Object> Params=new HashMap<String,Object>();
	public Map<String, Object> getParams() {
		return Params;
	}
	public void setParams(Map<String, Object> params) {
		Params = params;
	}
	public String getCmdName() {
		return CmdName;
	}
	public void setCmdName(String cmdName) {
		CmdName = cmdName;
	}
	public String getCmdData() {
		return CmdData;
	}
	public void setCmdData(String cmdData) {
		CmdData = cmdData;
	}
	
	
}
