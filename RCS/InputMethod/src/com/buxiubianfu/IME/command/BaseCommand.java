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

public class BaseCommand implements ICommand {

	
	
	
	private String LogTag="BaseCommand";
	private Context _Context;
	
	public BaseCommand()
	{}
	
    public BaseCommand(Context context)
    {
    	_Context=context;
    }
	
    
	
	public Context get_Context() {
		return _Context;
	}



	public void set_Context(Context _Context) {
		this._Context = _Context;
	}



	@Override
	public String Do(CommandData commandData) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * ������Ϣ
	 * 
	 * @param msg
	 * @throws UnsupportedEncodingException
	 * @throws IOException
	 */
	public void send(String msg,OutputStream oStream) throws UnsupportedEncodingException,
			IOException {
		try {
			Log.d(LogTag, msg);
			String strSend = CommandConst.DATA_BEGIN + msg + CommandConst.DATA_END;
			oStream.write(strSend.getBytes("GBK"));
			oStream.flush();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	

	
	
	
}
