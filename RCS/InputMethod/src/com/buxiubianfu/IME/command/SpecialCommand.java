package com.buxiubianfu.IME.command;



import android.content.Context;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;


import com.buxiubianfu.IME.Utils;
import com.buxiubianfu.IME.command.Data.CommandData;

public class SpecialCommand extends BaseCommand {

	private Context _Context;
	private InputConnection _InputConnection;
	public SpecialCommand(Context context,InputConnection inputConnection)
	{
		_Context=context;
		_InputConnection=inputConnection;
	}
	
	@Override
	public String Do( CommandData commandData) {
		
		if (commandData.getCmdData().equals("home")) {
			Utils.KeyDownHome(_Context);
		} else if (commandData.getCmdData().equals("end")) {
			// 获取当前输入发 并传入对应的keycode
			_InputConnection.sendKeyEvent(
					new KeyEvent(KeyEvent.ACTION_DOWN,
							KeyEvent.KEYCODE_ENTER));
		} else if (commandData.getCmdData().equals("delete")) {
			_InputConnection
					.sendKeyEvent(
							new KeyEvent(KeyEvent.ACTION_DOWN,
									KeyEvent.KEYCODE_DEL));
		} else if (commandData.getCmdData().equals("backspace")) {
			_InputConnection
					.sendKeyEvent(
							new KeyEvent(KeyEvent.ACTION_DOWN,
									KeyEvent.KEYCODE_DEL));
		} else if (commandData.getCmdData().equals("up")) {
			_InputConnection.sendKeyEvent(
					new KeyEvent(KeyEvent.ACTION_DOWN,
							KeyEvent.KEYCODE_DPAD_UP));
		} else if (commandData.getCmdData().equals("down")) {
			_InputConnection.sendKeyEvent(
					new KeyEvent(KeyEvent.ACTION_DOWN,
							KeyEvent.KEYCODE_DPAD_DOWN));
		} else if (commandData.getCmdData().equals("left")) {
			_InputConnection.sendKeyEvent(
					new KeyEvent(KeyEvent.ACTION_DOWN,
							KeyEvent.KEYCODE_DPAD_LEFT));
		} else if (commandData.getCmdData().equals("right")) {

			// switchInputMethod("com.baidu.input/.ImeService");
			_InputConnection.sendKeyEvent(
					new KeyEvent(KeyEvent.ACTION_DOWN,
							KeyEvent.KEYCODE_DPAD_RIGHT));
		} else if (commandData.getCmdData().equals("enter")) {
			_InputConnection.performEditorAction(
					EditorInfo.IME_ACTION_DONE);
	
			_InputConnection.performEditorAction(
					EditorInfo.IME_ACTION_SEND);
			_InputConnection.sendKeyEvent(
					new KeyEvent(KeyEvent.ACTION_DOWN,

					KeyEvent.KEYCODE_ENTER));
			_InputConnection
					.sendKeyEvent(
							new KeyEvent(KeyEvent.ACTION_UP,
									KeyEvent.KEYCODE_ENTER));
			// getCurrentInputConnection().performEditorAction(EditorInfo.IME_ACTION_GO);
			// sendDownUpKeyEvents(KeyEvent.KEYCODE_ENTER);

			// KeyEvent eDown = new KeyEvent(0, 0, KeyEvent.ACTION_DOWN,
			// KeyEvent.KEYCODE_ENTER, 0, 0, 0, 0,
			// KeyEvent.FLAG_SOFT_KEYBOARD);
			// KeyEvent eUp = new KeyEvent(0, 0, KeyEvent.ACTION_UP,
			// KeyEvent.KEYCODE_ENTER,
			// 0, 0, 0, 0, KeyEvent.FLAG_SOFT_KEYBOARD);
			//
			// onKeyDown(KeyEvent.KEYCODE_ENTER, eDown);
			// onKeyUp(KeyEvent.KEYCODE_ENTER, eUp);
		}
		
		
		return "";
	}

}
