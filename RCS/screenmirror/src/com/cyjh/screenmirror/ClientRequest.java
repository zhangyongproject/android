package com.cyjh.screenmirror;

import java.lang.reflect.Method;

import android.hardware.input.InputManager;
import android.util.Log;
import android.view.KeyCharacterMap;


public class ClientRequest {
	
	private final static String LOGTAG = "MirrorDisplay"; 
	final InputManager im;
	final Method injectInputEventMethod;
	final KeyCharacterMap kcm;
	
	public ClientRequest(InputManager im, Method inputMethod, KeyCharacterMap kcm) {
		this.im = im;
		this.injectInputEventMethod = inputMethod;
		this.kcm = kcm;
	}
	
	public void onConnected() {
		
	}
	
	
	
	
	/*
	 * type:<mousemove|mouseup|mousedown|rotate|scroll|home|backspace
	 *       |up|down|left|right|back|menu|wakeup|keychar>
	 * mousemove:
	 * */
	public void onCommandArrived(String cmd) 
	{
		int typeIdx = cmd.indexOf("type:");
		if(typeIdx == -1) {
			Log.e(LOGTAG, "Wrong formated command " + cmd);
		}
		
		
	}
	
	

}
