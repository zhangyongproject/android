package com.buxiubianfu.IME;

import java.io.DataOutputStream;
import java.io.OutputStream;
import java.util.List;

import android.content.Context;
import android.provider.Settings;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;

/**
 * ݔ�뷨�ГQ����
 * 
 * @author linbinghuang
 * 
 */
public class InputChangeManager {

	private static InputChangeManager manager;

	private InputChangeManager() {
	};

	public static InputChangeManager getInstance() {
		if (manager == null) {
			manager = new InputChangeManager();
		}
		return manager;

	}

	/**
	 * ��ȡ���뷨�б�
	 * 
	 * @param context
	 * @return
	 */
	public static List<InputMethodInfo> getInputList(Context context) {
		InputMethodManager imm = (InputMethodManager) context
				.getSystemService(Context.INPUT_METHOD_SERVICE);
		return imm.getInputMethodList();
	}

	/**
	 * ��ѭ���л����뷨
	 * 
	 * @param context
	 */
	public void changeInput(Context context) {
		List<InputMethodInfo> infos = getInputList(context);
		if (infos == null || infos.size() <= 1) {
			return;
		}
		int index = matchInputId(context, infos);
		changeInput(infos.get(index).getId());

	}

	/**
	 * ƥ�����뷨id������һ�����뷨
	 * 
	 * @param context
	 * @param infos
	 * @return
	 */
	private int matchInputId(Context context, List<InputMethodInfo> infos) {
		int size = infos.size();
		String currInputId = Settings.Secure.getString(
				context.getContentResolver(),
				Settings.Secure.DEFAULT_INPUT_METHOD);
		for (int i = 0; i < size; i++) {
			if (currInputId == infos.get(i).getId()) {
				++i;
				if (i == size) {
					return 0;
				}
				return i;
			}
		}
		return 0;

	}

	/**
	 * ���뷨�л�
	 * 
	 * @param inputId
	 */
	public void changeInput(String inputId) {
		execShell("ime enable " + inputId);
		execShell("ime set " + inputId);
	}

	private void execShell(String cmd) {
		try {
			// Ȩ������
			Process p = Runtime.getRuntime().exec("su");
			// ��ȡ�����
			OutputStream outputStream = p.getOutputStream();
			DataOutputStream dataOutputStream = new DataOutputStream(
					outputStream);
			// ������д��
			dataOutputStream.writeBytes(cmd);
			// �ύ����
			dataOutputStream.flush();
			// �ر�������
			dataOutputStream.close();
			outputStream.close();
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

}
