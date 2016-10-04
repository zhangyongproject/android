package com.cyjh.svc.proto;

import java.nio.ByteBuffer;

public abstract class ProtoBufDataWraper
{
	public abstract byte[] getProBufData();

	public ByteBuffer toByteBuffer()
	{
		int len = getProBufData().length;
		byte[] array = new byte[len+4];
		ByteBuffer buffer = ByteBuffer.wrap(array);
		buffer.putInt(len);
		buffer.put(getProBufData());
		buffer.flip();
		
		return buffer;
	}
}
