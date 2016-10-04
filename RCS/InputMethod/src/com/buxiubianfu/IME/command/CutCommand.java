package com.buxiubianfu.IME.command;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.view.inputmethod.InputConnection;
import com.buxiubianfu.IME.command.Data.CommandData;

public class CutCommand extends BaseCommand {
	public InputConnection _InputConnection;
	public ClipboardManager _ClipboardManager;
	public OutputStream _oStream;
	public CutCommand(InputConnection inputConnection,
			ClipboardManager clipboardManager, OutputStream oStream)
	{
		_InputConnection=inputConnection;
		_ClipboardManager=clipboardManager;
		_oStream=oStream;
	}
	@SuppressLint("NewApi")
	@Override
	public String Do( CommandData commandData) {
		try {
			CharSequence clipText = _InputConnection.getSelectedText(0);
			String CopyText = clipText.toString();
			_InputConnection.commitText("", 1);
			String SendText = CopyText;
			_ClipboardManager.setPrimaryClip(ClipData.newPlainText(null,
					CopyText));
			super.send(CopyText,_oStream);
		} catch (UnsupportedEncodingException e) {

		} catch (IOException e) {

		}
		return "";
	}

}
