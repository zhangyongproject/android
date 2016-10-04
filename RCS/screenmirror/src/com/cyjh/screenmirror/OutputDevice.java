package com.cyjh.screenmirror;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import android.media.MediaCodec;
import android.util.Log;

public class OutputDevice extends EncodeDevice {

	private static final String LOGTAG = "MirrorDisplay";

	protected DataOutputStream os;

	public OutputDevice(OutputStream o, int width, int height) {
		this(o, width, height, 100);
	}

	/**
	 * 
	 * @param o
	 * @param width
	 * @param height
	 * @param quality 视频质量(10-100)
	 */
	public OutputDevice(OutputStream o, int width, int height, int quality) {
		super("Mirror", width, height, quality);
		os = new DataOutputStream(o);
	}

	@Override
	public EncoderRunnable onSurfaceCreated(MediaCodec codec) {
		return new EncoderRunnable(this);
	}

	@Override
	public void sendFrame(ByteBuffer data) throws IOException {
	    //Log.e(LOGTAG, "copy buffer begin");
		ByteBuffer buf = ByteBuffer.allocate(data.limit());
		buf.order(ByteOrder.nativeOrder());
//		buf.putInt(0);
//		buf.putInt(data.limit());
		buf.put(data);
		//Log.e(LOGTAG, "copy buffer end");
		//if (mLastPrintTime == 0) {
		//	mLastPrintTime = System.currentTimeMillis();
		//}
//		int total = data.limit() + 8;
		mSendTotalBytes += buf.limit();
		try {
		    os.write(0xff);
		    os.write(0xfe);
		    os.write(0x00);//packet type: video
		    os.writeInt(buf.limit());
		    //Log.e(LOGTAG, "send buffer begin");
			os.write(buf.array(), 0, buf.limit());
			os.flush();
			Log.e(LOGTAG, "mSendTotalBytes: " + mSendTotalBytes);
			//Log.e(LOGTAG, "send buffer end");
		} catch (IOException e) {
			//Log.d(LOGTAG, "sendFrame exception " + e);
			e.printStackTrace();
			throw e;
		}
		//long now = System.currentTimeMillis();
		//long time = now - mLastPrintTime;
		//if (time > 5000) {
		//	System.out.printf("Send Bytes: %.2f KB/s\n", mSendTotalBytes * 1000	/ time / 1024);
		//	mSendTotalBytes = 0;
		//	mLastPrintTime = now;
		//}
	}

	private long mSendTotalBytes;
	private long mLastPrintTime;
}
