package com.buxiubianfu.IME.command;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import com.buxiubianfu.IME.command.Data.CommandData;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class SendJsonCommand extends BaseCommand {

	private String LogTag = "SendJsonCommand";
	private OutputStream _oStream;

	public SendJsonCommand(Context context, OutputStream oStream) {
		super(context);
		_oStream = oStream;
		// TODO Auto-generated constructor stub
	}

	@Override
	public String Do( CommandData commandData) {
		for (Map.Entry<String, Object> s : commandData.getParams().entrySet()) {
			try {
				sendJson(s.getKey(), s.getValue().toString());
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return "";
	}

	/**
	 * 发送json（协议）的消息
	 * 
	 * @param cmd
	 *            指令
	 * @param data
	 *            数据
	 * @throws UnsupportedEncodingException
	 * @throws IOException
	 */
	public void sendJson(String cmd, String data)
			throws UnsupportedEncodingException, IOException {

		try {

			Log.d(LogTag, cmd);
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("cmd", cmd);
			jsonObject.put("data", data);
			String strSend = CommandConst.DATA_BEGIN + jsonObject
					+ CommandConst.DATA_END;
			_oStream.write(strSend.getBytes("GBK"));
			_oStream.flush();
		} catch (JSONException e) {
			e.printStackTrace();
		}

	}
}
