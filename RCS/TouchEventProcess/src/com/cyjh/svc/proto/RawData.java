package com.cyjh.svc.proto;

import com.cyjh.svc.proto.Protocol.Message;

public class RawData extends ProtoBufDataWraper
{
	private Message msg = null;

	public RawData(Message msg)
	{
		this.msg = msg;
	}

	@Override
	public byte[] getProBufData()
	{
		// TODO Auto-generated method stub
		if (msg == null)
			return null;
		return msg.toByteArray();
	}

}
