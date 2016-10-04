package com.cyjh.screenmirror;

public class SimpleCmdParser {
	public final static int COMMAND_UNKNOWN    = 0x00000000;
	public final static int COMMAND_MOUSE_MOVE = 0x12340001;
	public final static int COMMAND_MOUSE_UP =   0x12340002;
	public final static int COMMAND_MOUSE_DOWN = 0x12340003;
	public final static int COMMAND_KEYCODE	 = 0x12340004;
	public final static int COMMAND_HOME		 = 0x12340005;
	public final static int COMMAND_BACK		 = 0x12340006;
	public final static int COMMAND_MENU		 = 0x12340007;
	public final static int COMMAND_LEFT		 = 0x12340008;
	public final static int COMMAND_RIGHT		 = 0x12340009;
	public final static int COMMAND_UP			 = 0x1234000a;
	public final static int COMMAND_DOWN		 = 0x1234000b;
	public final static int COMMAND_SCROLL	  	 = 0x1234000c;

	public SimpleCmdParser(byte[] s) {
		cmd = s.clone();
		pos = 0;
	}

	public boolean getInt(int ival[]) {
		if (pos + 3 > cmd.length)
			return false;

		ival[0] = cmd[0] | (cmd[1] << 8) | (cmd[2] << 16) | (cmd[3] << 24);
		pos +=3;
		return true;
	}
	
	public boolean getFloat(float fval[]) {
		if (pos + 3 >cmd.length) 
			return false;
		
		fval[0] = cmd[0]|(cmd[1] <<8) |(cmd[2]<<16) |(cmd[3] << 24);
		if(Float.isNaN(fval[0]))
			return false;
		pos += 3;
		return true;
	}
	
	public boolean getBytes(byte bytes[]) {
		if(pos +3 > cmd.length)
			return false;
		int strLen = cmd[0]|(cmd[1]<<8)|(cmd[2]<<16)|(cmd[3]<<24);
		
		for(int i=0; i<strLen; i++)
			bytes[i] = cmd[pos+3+i];
		pos = pos+3+strLen;
		
		return true;
	}

	public static class InputCommand {
		public InputCommand()
		{
			type = COMMAND_UNKNOWN;
			x = 0;
			y = 0;
			keycode = 0;
			shift = 0;
		}
		public int getType() { return type;}
		public float getX() { return x; }
		public float getY() { return y; }
		public int getKeycode() { return keycode; }
		public int getShift() {return shift; }
		
		public void setType (int type) {this.type = type; }
		public void setX(int x) {this.x = x; }
		public void setY(int y) {this.y = y; }
		public void setShift(int shift) {this.shift = shift; }
		public void setCommand(int type, float x, float y, int keyCode, int shift)
		{
			this.type  = type;
			this.x = x;
			this.y = y;
			this.keycode = keyCode;
			this.shift = shift;
		}
		
		private int type;
		private float x;
		private float y;
		private int keycode;
		private int shift;
	}
	
	public InputCommand parse()
	{
		int type[] = new int[1];
		float x[] = new float[1];
		float y[] = new float[1];
		int keyCode[] = new int[1];
		int shift[] = new int[1];
		
		InputCommand myCmd = new InputCommand();
		boolean ret = getInt(type);
		if(!ret)
			return myCmd;
		
		switch(type[0]) {
			case COMMAND_MOUSE_MOVE: 
			case COMMAND_MOUSE_DOWN:
			case COMMAND_MOUSE_UP:
			{
				ret = getFloat(x) && getFloat(y);
				if(ret) {
					myCmd.setCommand(type[0], x[0], y[0], 0, 0);
				}
				break;
			}
			case COMMAND_MENU: 
			case COMMAND_BACK:
			case COMMAND_HOME:
			case COMMAND_LEFT:
			case COMMAND_RIGHT:
			case COMMAND_UP:
			case COMMAND_DOWN:{
				myCmd.setCommand(type[0], 0, 0, 0, 0);
				break;
			}
			case COMMAND_KEYCODE: {
				ret = getInt(keyCode) && getInt(shift);
				if(ret) {
					myCmd.setCommand(type[0], 0, 0, keyCode[0], shift[0]);
				}
				break;
			}
				
			default:
		}
		return myCmd;
	}

	private byte[] cmd;
	private int pos;
}
