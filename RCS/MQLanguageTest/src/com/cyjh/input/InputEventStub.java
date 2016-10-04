package com.cyjh.input;



public class InputEventStub
{
		//设置设备的type类型，PROTO_TYPE_A为0，PROTO_TYPE_B为1
		public native void SetProtoType(int iProtoType);
		public native void WriteInitData(int iX, int iY);
		
    public native void TouchDownEvent(int iX, int iY, int iFingerId);
    public native void TouchMoveEvent(int iX, int iY, int iFingerId, int iTime);
    public native void TouchUpEvent(int iFingerId);
    

    static 
    {
    	//System.load(ClientService.libPath);
    }
}
