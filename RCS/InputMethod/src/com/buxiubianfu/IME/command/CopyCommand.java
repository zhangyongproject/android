package com.buxiubianfu.IME.command;

import java.io.OutputStream;
import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.view.inputmethod.InputConnection;
import com.buxiubianfu.IME.command.Data.CommandData;

public class CopyCommand extends BaseCommand {

	public InputConnection _InputConnection;
	public ClipboardManager _ClipboardManager;
	public OutputStream _oStream;

	public CopyCommand(InputConnection inputConnection,
			ClipboardManager clipboardManager, OutputStream oStream) {
		_InputConnection = inputConnection;
		_ClipboardManager = clipboardManager;
		_oStream = oStream;
	}

	@SuppressLint("NewApi")
	@Override
	public String Do( CommandData commandData) {
		try {

			CharSequence clipText = _InputConnection.getSelectedText(0);
			String CopyText = clipText.toString();
			String SendText = CopyText;
			_ClipboardManager.setPrimaryClip(ClipData.newPlainText(null,
					CopyText));
			super.send(CopyText, _oStream);
		} catch (Exception e) {
		}
		return "";
	}

}
