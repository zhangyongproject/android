package com.buxiubianfu.IME;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ClipboardManager.OnPrimaryClipChangedListener;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.inputmethodservice.InputMethodService;
import android.media.AudioManager;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.RecoverySystem;
import android.util.Log;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;

import com.buxiubianfu.IME.command.GetUpdateInfoCommand;
import com.buxiubianfu.IME.command.OpenAppCommand;
import com.buxiubianfu.IME.command.Data.CommandData;

public class IME extends InputMethodService {
	public static final String ACTION = "com.buxiubianfu.action.INPUT";
	private static final String TAG = IME.class.getSimpleName();
	private static final String DATA_BEGIN = "_BXBF_DATA_BEGIN";
	private static final String DATA_END = "_BXBF_DATA_END";
	private static final String CLIP_TEXT = "_CLIP-BXBF-TEXT"; // 剪切
	private static final String COPY_TEXT = "_COPY-BXBF-TEXT"; // 复制
	private static final String PASTE_TEXT = "_PASTE-BXBF-TEXT"; // 粘贴
	private static final String CUT_TEXT = "_CUT-BXBF-TEXT"; // 分割文本
	private static final String KEEP_ALIVE = "_KEEP-BXBF-TEXT"; // 心跳包
	private static final String CLIP_ENTER = "_BXBF_DATA_ENTER";

	private static final String LogTag = "BXBF";

	private static final String SD_CARD_SPACE = "getSDCardSpace";

	private static final String MOBILE_DISPLAY = "getMobileDisplay";
	private static final String KEY_DOWN_HOME = "keyDownHome";
	private static final String KEY_DOWN_BACK = "keyDownBack";
	private static final String KEY_DOWN_MENU = "keyDownMenu";
	private static final String KEY_DOWN_CHAR = "keyDownChar";
	private static final String KEY_DOWN_SPECIAL = "keySpecial";
	private static final String DOWNLOAD_FINISH = "downloadFinish";
	private static final String OPEN_APP = "appRunning";
	private static final String IME_STATUS = "getImeStatus";
	private static final String OPEN_APPLIACTION = "openApp";
	private static final String GET_UPDATE_INFO = "getUpdateInfo";
	private static final String GET_VOLUME = "getVolume";
	private static final String SET_VOLUME = "setVolume";
	private static final String FACTORY_DATA_RESET = "factorydatareset";
	private static final String OPEN_RECENT_APPLICATIONS = "openRecentApplications";
	public static final String SERVICE_CMD = "service_cmd";
	public static final String SERVICE_SENDJSON_TO_PC_DATA = "sendjson_to_pc_data";
	public static final String ANDROID_TO_PC_UPGRADEFINISH = "android_to_pc_upgradefinish";
	public static final String ANDROID_TO_PC_UPGRADEINFO = "android_to_pc_upgradeinfo";
	public static final String PC_TO_ANDROID_PUSH_SUCCESS = "pc_to_android_push_success";

	// private BufferedInputStream mSocketIn;
	// private BufferedOutputStream out;
	private boolean delayFlag = false; // 用于onStartInput函数处理多次触发，做延迟操作
	private BroadcastReceiver m_BroadCast;
	private ClipboardManager clipboardManager;
	CharSequence charTextCharSequence;
	private String content;
	private String RecvText;
	private String SendText;
	private CharSequence clipText;
	// private static final String SERVER_IP = "127.0.0.1";
	private static final int SERVER_PORT = 25556; // 0.8 35786 0.9 35787
	private Socket socket;
	private BufferedReader buffer;
	private OutputStream oStream;
	private recvThread recv;
	private static Thread imeThread;
	private ServerSocket serverSocket;
	private String currentDisplay = "";
	private AudioManager mAudioManager;

	private List<String> list = new ArrayList<String>();
	private Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// if (socket == null) {
			// ToastUtil.showToast(getApplicationContext(), "socket 是空的");
			// } else if (!socket.isConnected()) {
			// ToastUtil.showToast(getApplicationContext(), "socket 没连接");
			// } else {
			// ToastUtil.showToast(getApplicationContext(), "我还没死");
			// }
			// handler.sendEmptyMessageDelayed(1, 10000);
			super.handleMessage(msg);
		}

	};
	private final static String LOG_PATH = Environment
			.getExternalStorageDirectory().getPath()
			+ File.separatorChar
			+ "imelog.txt";
	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			String sendStr = OpenApp.isTargetGame(IME.this);
			if (!sendStr.equals("")) {
				try {
					if (oStream != null) {
						sendJson(OPEN_APP, sendStr);
					}
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			mHandler.sendEmptyMessageDelayed(1, 500);
			super.handleMessage(msg);
		}

	};
	private static final boolean[] filterMap = new boolean[0xff];

	private static final int[] key = new int[] { 0x02, 0x03, 0x04, 0x05, 0x06,
			0x07, 0x08, 0x09, 0x0a, 0x0b, 0x0c, 0x0d, 0x0e, 0x0f, 0x10, 0x11,
			0x12, 0x17, 0x18, 0x19, 0x1a, 0x1b, 0x1c, 0x1d, 0x1e, 0x1f, 0x20,
			0x21, 0x22, 0x23, 0x24, 0x25, 0x26, 0x27, 0x28, 0x29, 0x2a, 0x2b,
			0x2c, 0x2d, 0x2e, 0x2f, 0x30, 0x31, 0x32, 0x33, 0x34, 0x35, 0x36,
			0x37, 0x38, 0x44, 0x45, 0x46, 0x47, 0x48, 0x49, 0x4a, 0x4b, 0x4c };

	static {
		int i;
		for (i = 0; i < 0xff; i++) {
			filterMap[i] = false;
		}
		for (i = 0; i < key.length; i++) {
			filterMap[key[i]] = true;
		}
	}

	public static boolean isFilterKeyCode(int code) {
		if (filterMap[code]) {
			return true;
		}

		return false;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d(TAG, "onStartCommand");
		if (intent != null) {
			String service_cmd = intent.getStringExtra(SERVICE_CMD);
			Log.d(TAG, "onStartCommand cmd :" + service_cmd);
			if (ANDROID_TO_PC_UPGRADEINFO.equals(service_cmd)) {
				String data = intent
						.getStringExtra(SERVICE_SENDJSON_TO_PC_DATA);
				Log.d(TAG, "onStartCommand data :" + data);
				try {
					sendJson(service_cmd, data);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			if (ANDROID_TO_PC_UPGRADEFINISH.equals(service_cmd)) {
				String data = intent
						.getStringExtra(SERVICE_SENDJSON_TO_PC_DATA);
				Log.d(TAG, "onStartCommand data :" + data);
				try {
					sendJson(service_cmd, data);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

		}
		return START_REDELIVER_INTENT;
	}

	/**
	 * 循环读取回复数据的线程
	 * 
	 * @author linxiaoqing
	 * 
	 */
	class recvThread implements Runnable {
		boolean bRun = true;

		recvThread() {
			super();
		}

		@Override
		public void run() {
			Looper.prepare();
			while (bRun) {
				// 读取输入的数据(阻塞读)

				if (!socket.isConnected()) {
					continue;
				}

				try {
					// sendJson(KEEP_ALIVE, ""); // 心跳包(判断连接是否已经断开)
					if (!currentDisplay.equals(Utils.getDisplay(IME.this))) {
						// 当屏幕横竖屏切换时通知PC端
						sendJson(MOBILE_DISPLAY, Utils.getDisplay(IME.this));
						currentDisplay = Utils.getDisplay(IME.this);
					}
				} catch (Exception e) {
					reStartSocket();
				}

				// len = buffer.read(buf);
				// content = new String(buf, 0, len);
				// try {
				// content = buffer.readLine();
				content = readCMDFromSocket(buffer);

				if (content == null || content.isEmpty() || content.equals("")) {
					continue;
				}
				if (content.contains(DATA_END)) {
					content.indexOf(DATA_BEGIN);
				}

				list.clear();

				// 把多个合并的包拆分开
				/*
				 * doSplitItem(content); Log.d(LogTag, "Split " + content +
				 * " to list size " + list.size()); for (int i = 0; i <
				 * list.size(); i++) { RecvData(DATA_BEGIN + list.get(i) +
				 * DATA_END); }
				 */

				// 拆分遇到了问题。不再拆分了
				RecvData(content);

				// } catch (IOException e) {
				// e.printStackTrace();
				// }
				//
			}
			Looper.loop();
		}
	}

	/**
	 * 重启Socket服务端
	 */
	public void reStartSocket() {
		try {
			if (oStream != null) {
				oStream.close();
			}
			if (buffer != null) {
				buffer.close();
			}
			if (socket != null) {
				socket.close();
			}
			if (serverSocket != null) {
				serverSocket.close();
			}
			serverSocket = new ServerSocket(SERVER_PORT);
			socket = serverSocket.accept();
			socket.setSoTimeout(3000);

			// ToastUtil.showToast(this, "socket连接连接");
			this.buffer = new BufferedReader(new InputStreamReader(
					socket.getInputStream(), "GBK"));
			// mSocketIn = new BufferedInputStream(socket.getInputStream());
			oStream = socket.getOutputStream();
			// FileUtils.writeFile(LOG_PATH,
			// "时间："+DateUtil.getTime()+"日志：socket_log"+"现在重新连接", true);

			new Thread(recv).start();
			Log.i("BXBF", "服务重启成功！");
		} catch (SocketException e) {
			// FileUtils.writeFile(LOG_PATH,
			// "时间："+DateUtil.getTime()+"日志："+e.getMessage(), true);
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 迭代截取数据
	 * 
	 * @param text
	 * @return
	 */
	public List<String> doSplitItem(String text) {
		if (text.contains(DATA_BEGIN)) {
			String text1 = text.split(DATA_END)[0];
			text1 = text1.split(DATA_BEGIN)[1];
			String text2 = "";
			try {
				text2 = text.split(DATA_END)[1];
			} catch (Exception e) {
				e.printStackTrace();
			}
			list.add(text1);
			if (text2.contains(DATA_BEGIN)) {
				doSplitItem(text2);
			} else {
				return list;
			}
		}
		return list;
	}

	/**
	 * @param in
	 * @return 解析流的信息
	 */
	public String readCMDFromSocket(BufferedReader in) {
		String numReadedBytes = null;
		try {
			numReadedBytes = in.readLine();
			if (numReadedBytes != null) {
				Log.i("BXBF", numReadedBytes);
			} else {
				return null;
			}
		} catch (Exception e) {

			e.printStackTrace();
		}

		return numReadedBytes;
	}

	// public String resolveCmdName(String cmd) {
	//
	// String cmdName = null;
	// int i = cmd.indexOf("data:");
	// cmdName = cmd.substring(5, i - 1);
	// return cmdName;
	// }
	//
	// public String resolveCmdData(String cmd) {
	//
	// String cmdData = null;
	// int i = cmd.indexOf("data:");
	// cmdData = cmd.substring(i + 5, cmd.length() - 1);
	// return cmdData;
	// }

	/**
	 * 文本超过1024 tcp 会自动分次接收，该函数用于文本拼接
	 * 
	 * @param text
	 */
	private void RecvData(String text) {
		Log.d("BXBF", "Recved Data : " + text);
		int iEnd = 0;
		if (text.indexOf(DATA_BEGIN) == 0) {
			RecvText = "";
			RecvText = text.substring(DATA_BEGIN.length());
			iEnd = RecvText.length() - DATA_END.length();
			if (RecvText.substring(iEnd).equals((DATA_END))) {
				RecvText = RecvText.substring(0, iEnd);
				DataManage(RecvText);
			}
			return;
		}
		iEnd = text.length() - DATA_END.length();
		if (iEnd >= 0 && text.substring(iEnd).equals(DATA_END)) {
			RecvText += text.substring(0, iEnd);
			DataManage(RecvText);
			return;
		}
		RecvText += text;
	}

	/**
	 * 广播注册
	 */
	public void registerBoradcastReceiver() {
		IntentFilter myIntentFilter = new IntentFilter();
		myIntentFilter.addAction(Utils.ACTION_NAME_1);
		myIntentFilter.addAction("ACTION_NAME_2");
		registerReceiver(mBroadcastReceiver, myIntentFilter);
	}

	/**
	 * 广播接收
	 */
	private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			// 下载完成广播
			if (action.equals(Utils.ACTION_NAME_1)) {
				try {
					sendJson(DOWNLOAD_FINISH, "");
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
				// 重启服务端广播
			} else if (action.equals("ACTION_NAME_2")) {
				reStartSocket();
			}
		}

	};

	/**
	 * 接收到的指令处理
	 * 
	 * @param text
	 */
	private void DataManage(String text) {

		List<String> list = Utils.parserJson(text);
		if (list == null || list.get(0) == null || list.get(1) == null) {
			Log.i("BXBF", "竟然空了");
			return;
		}
		String cmdName = list.get(0);
		String cmdData = list.get(1);
		Log.d(TAG, "cmdName:" + cmdName + ",cmdData:" + cmdData);
		// 重构后需要测试
		// DataCreatorManager dcm=new
		// DataCreatorManager(cmdName,cmdData,text,IME.this);
		// CommandManager cm=new
		// CommandManager(IME.this,oStream,this.getCurrentInputConnection(),clipboardManager);
		// cm.Do(dcm.Create(cmdName, text));

		// 开始下载指令
		if ("downloadapp".equals(cmdName)) {
			Intent mIntent = new Intent(Utils.ACTION_NAME);
			mIntent.putExtra(Utils.INTENT_NAME, cmdName);
			mIntent.putExtra(Utils.START_DOWNLOAD_DATA, cmdData);
			sendBroadcast(mIntent);
			return;
		}

		// 暂停下载指令
		if ("downloadPause".equals(cmdName)) {
			Intent mIntent = new Intent(Utils.ACTION_NAME);
			mIntent.putExtra(Utils.INTENT_NAME, cmdName);
			sendBroadcast(mIntent);
			return;
		}

		// if ("getdownStart".equals(cmdName)) {
		// // downloadView.start();
		// Intent mIntent = new Intent(Utils.ACTION_NAME);
		// mIntent.putExtra(Utils.INTENT_NAME, cmdName);
		// sendBroadcast(mIntent);
		// return;
		// }

		// 获取总内存大小
		if (cmdName.equals(SD_CARD_SPACE)) {
			try {
				sendJson(SD_CARD_SPACE, Utils.getSDAvailableSize() + "/"
						+ Utils.getSDTotalSize());
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return;
		}

		// 获取键盘输入的字符
		if (cmdName.equals(KEY_DOWN_CHAR)) {
			setInputText(cmdData);
			return;
		}

		// 屏幕分辨率
		if (cmdName.equals(MOBILE_DISPLAY)) {
			try {
				sendJson(MOBILE_DISPLAY, Utils.getDisplay(IME.this));
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			// Log.i("BXBF", "发送数据" + Utils.getDisplay(IME.this));
			return;
		}

		// 返回主页键处理
		if (cmdName.equals(KEY_DOWN_HOME)) {
			Utils.KeyDownHome(IME.this);
			return;
		}

		// 返回键的处理
		if (cmdName.equals(KEY_DOWN_BACK)) {
			Utils.simulateKeystroke(KeyEvent.KEYCODE_BACK);
			return;
		}

		// menu/taskmgr键的处理
		if (cmdName.equals(KEY_DOWN_MENU)) {
			Utils.simulateKeystroke(KeyEvent.KEYCODE_MENU);
			return;
		}
		if (cmdName.equals(OPEN_APPLIACTION)) {
			OpenAppCommand openAppCommand = new OpenAppCommand(this);
			CommandData commandData = new CommandData();
			commandData.setCmdName(cmdName);
			commandData.setCmdData(cmdData);
			commandData.setText(text);
			openAppCommand.Do(commandData);
			return;
		}
		if (cmdName.equals(GET_UPDATE_INFO)) {
			GetUpdateInfoCommand getUpdateInfoCommand = new GetUpdateInfoCommand(
					this);
			CommandData commandData = new CommandData();
			commandData.setCmdName(cmdName);
			commandData.setCmdData(cmdData);
			getUpdateInfoCommand.Do(commandData);
			return;
		}
		if (cmdName.equals(PC_TO_ANDROID_PUSH_SUCCESS)) {
			String filePath = "";
			try {
				JSONObject jsonObject = new JSONObject(cmdData);
				filePath = jsonObject.getString("filePath");
			} catch (JSONException e) {
				e.printStackTrace();
			}
			Log.d(TAG, "发送路径，触发升级,filepath is " + filePath);
			Intent intent = new Intent("com.ugam e.upgrade.broadcast.ACTION");
			intent.putExtra("complete", filePath);
			sendBroadcast(intent);
			return;
		}
		if (cmdName.equals(GET_VOLUME)) {
			try {
				sendJson(GET_VOLUME, String.valueOf(mAudioManager
						.getStreamVolume(AudioManager.STREAM_SYSTEM)));
			} catch (IOException e) {
				e.printStackTrace();
			}
			return;
		}
		if (cmdName.equals(SET_VOLUME)) {
			int valume = Integer.parseInt(cmdData);
			mAudioManager
					.setStreamVolume(AudioManager.STREAM_SYSTEM, valume, 0);
			return;
		}
		if (cmdName.equals(FACTORY_DATA_RESET)) {
			try {
				RecoverySystem.rebootWipeUserData(this);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if (cmdName.equals(OPEN_RECENT_APPLICATIONS)) {
			try {
				Intent intent = new Intent();
				intent.setClassName("com.android.systemui",
						"com.android.systemui.recent.RecentsActivity");
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(intent);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		// 特殊键的处理

		if (cmdName.equals(KEY_DOWN_SPECIAL)) {
			if (cmdData.equals("home")) {
				Utils.KeyDownHome(IME.this);
			} else if (cmdData.equals("end")) {
				// 获取当前输入发 并传入对应的keycode
				this.getCurrentInputConnection().sendKeyEvent(
						new KeyEvent(KeyEvent.ACTION_DOWN,
								KeyEvent.KEYCODE_ENTER));
			} else if (cmdData.equals("delete")) {
				Utils.simulateKeystroke(KeyEvent.KEYCODE_FORWARD_DEL);
			} else if (cmdData.equals("backspace")) {
				Utils.simulateKeystroke(KeyEvent.KEYCODE_DEL);
			} else if (cmdData.equals("up")) {
				this.getCurrentInputConnection().sendKeyEvent(
						new KeyEvent(KeyEvent.ACTION_DOWN,
								KeyEvent.KEYCODE_DPAD_UP));
			} else if (cmdData.equals("down")) {
				this.getCurrentInputConnection().sendKeyEvent(
						new KeyEvent(KeyEvent.ACTION_DOWN,
								KeyEvent.KEYCODE_DPAD_DOWN));
			} else if (cmdData.equals("left")) {
				this.getCurrentInputConnection().sendKeyEvent(
						new KeyEvent(KeyEvent.ACTION_DOWN,
								KeyEvent.KEYCODE_DPAD_LEFT));
			} else if (cmdData.equals("right")) {
				this.getCurrentInputConnection().sendKeyEvent(
						new KeyEvent(KeyEvent.ACTION_DOWN,
								KeyEvent.KEYCODE_DPAD_RIGHT));
			} else if (cmdData.equals("tab")) {
				Utils.simulateKeystroke(KeyEvent.KEYCODE_TAB);
			} else if (cmdData.equals("enter")) {
				/*getCurrentInputConnection().performEditorAction(
						EditorInfo.IME_ACTION_DONE);
				this.sendDownUpKeyEvents(KeyEvent.KEYCODE_ENTER);
				getCurrentInputConnection().performEditorAction(
						EditorInfo.IME_ACTION_SEND);
				this.getCurrentInputConnection().sendKeyEvent(
						new KeyEvent(KeyEvent.ACTION_DOWN,

						KeyEvent.KEYCODE_ENTER));
				this.getCurrentInputConnection()
						.sendKeyEvent(
								new KeyEvent(KeyEvent.ACTION_UP,
										KeyEvent.KEYCODE_ENTER));*/
				Utils.simulateKeystroke(KeyEvent.KEYCODE_ENTER);	//简单粗暴，直接模拟一个回车键完事
			}
		}
		
		if (cmdName.equals(KEEP_ALIVE)) {
			//收到心跳包，回复一个心跳包
			try {
				sendJson(KEEP_ALIVE, "");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		// PC端发过来的复制文本，置入剪切板
		if (text.indexOf(CLIP_TEXT) != -1) {
			ClipboardPaste(text);
			return;
		}

		// 判断是否是Ctrl + C
		if (text.indexOf(COPY_TEXT) != -1) {
			ClipboardCopy();
			return;
		}

		// 判断是否是Ctrl + X
		if (text.indexOf(CUT_TEXT) != -1) {
			ClipboardCut();
			return;
		}

		// 判断是否是Ctrl + V
		if (text.equals(PASTE_TEXT)) {
			if (clipboardManager.hasPrimaryClip()) {
				String ClipText = clipboardManager.getPrimaryClip()
						.getItemAt(0).getText().toString();
				setInputText(ClipText);
			}
			return;
		}
	}

	public IME() {
		this.m_BroadCast = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {

				// 判断是否修改输入法，若修改将停止线程，防止出现多个连接
				if (intent.getAction().equals(
						Intent.ACTION_INPUT_METHOD_CHANGED)) {
					imeThread.stop();
				}
			}
		};

	}

	public void setInputText(String inputContent) {
		// 将文本中的回车键替换成\n
		inputContent = inputContent.replaceAll(CLIP_ENTER, "\n");
		this.getCurrentInputConnection().commitText(
				((CharSequence) inputContent), 1);
	}

	@Override
	public void onDestroy() {
		mHandler.removeMessages(1);
		super.onDestroy();
	}

	public void onCreate() {
		super.onCreate();
		Log.d(TAG, "onCreate");
		mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		// ToastUtil.showToast(this,
		// "..................IME已启动.................");
		handler.removeMessages(1);
		handler.sendEmptyMessageDelayed(1, 10000);
		mHandler.sendEmptyMessage(1);
		// 监视剪切板改动
		final ClipboardManager cb = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
		cb.addPrimaryClipChangedListener(new OnPrimaryClipChangedListener() {
			@Override
			public void onPrimaryClipChanged() {
				ClipData clipData = cb.getPrimaryClip();
				try {
					String str = clipData.getItemAt(0).getText().toString();
					if (!str.equals(SendText)) {
						send(str);
					}
				} catch (Exception e) {
				}
			}
		});

		IntentFilter v0 = new IntentFilter();
		v0.addAction(Intent.ACTION_INPUT_METHOD_CHANGED);
		this.registerReceiver(this.m_BroadCast, v0);

		// 打开我们自己的输入法
		String inputMethodName = this.getPackageName() + "/.IME";
		Log.d(LogTag, "Input Method is changed to " + inputMethodName);
		InputChangeManager.getInstance().changeInput(inputMethodName);

		// 开启接收windows消息的线程
		imeThread = new Thread(new Runnable() {
			@Override
			public void run() {
				recv = new recvThread();
				start();
			}
		});
		imeThread.start();
		clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
	}

	private void start() {
		boolean bStart = true;
		try {
			this.serverSocket = new ServerSocket(SERVER_PORT);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		while (bStart) {
			try {
				// this.socketClient = new Socket(SERVER_IP, SERVER_PORT);
				// socketClient.setSoTimeout(3000);

				// 开启服务端
				this.socket = serverSocket.accept();
				socket.setSoTimeout(3000);
				Log.d(LogTag, "start success");
				this.buffer = new BufferedReader(new InputStreamReader(
						this.socket.getInputStream(), "GBK"));
				new Thread(recv).start();
				oStream = socket.getOutputStream();

				// bStart = false;

			} catch (final IOException e) {
				Log.d(LogTag, "start fail! error = " + e.getMessage());
				try {
					reStartSocket();
					Thread.sleep(3000);
				} catch (InterruptedException ie) {
				}
			}

		}

	}

	/**
	 * 发送消息
	 * 
	 * @param msg
	 * @throws UnsupportedEncodingException
	 * @throws IOException
	 */
	public void send(String msg) throws UnsupportedEncodingException,
			IOException {
		try {
			Log.d(LogTag, msg);
			String strSend = DATA_BEGIN + msg + DATA_END;
			oStream.write(strSend.getBytes("GBK"));
			oStream.flush();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * 发送json（协议）的消息
	 * 
	 * @param cmd
	 *            ָ指令
	 * @param data
	 *            数据
	 * @throws UnsupportedEncodingException
	 * @throws IOException
	 */
	public void sendJson(String cmd, String data)
			throws UnsupportedEncodingException, IOException {

		try {

			Log.d(LogTag, "sending " + cmd + " : " + data);
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("cmd", cmd);
			jsonObject.put("data", data);
			String strSend = DATA_BEGIN + jsonObject + DATA_END;
			oStream.write(strSend.getBytes("GBK"));
			oStream.flush();
		} catch (JSONException e) {
			e.printStackTrace();
		}

	}

	// 全屏时不弹出输入窗口
	public boolean onEvaluateFullscreenMode() {
		return false;
	}

	//
	// private class DelayHandle extends TimerTask {
	// int inputType;
	// boolean isCH;
	//
	// @Override
	// public void run() {
	// try {
	// delayFlag = false;
	// // 发送输入法关闭指令
	// sendJson(IME_STATUS, "false");
	// ToastUtil.showToast(IME.this, "我关闭输入法" + isCH);
	// if (inputType != 0) {
	// isCH = true;
	// } else {
	// isCH = false;
	// }
	// // ToastUtil.showToast(IME.this, "isCH:"+isCH);
	// if (isCH) {
	// // ���뷨��ָ��
	// sendJson(IME_STATUS, "true");
	// ToastUtil.showToast(IME.this,
	// "我已经发送true给你。。。请把他切换到输入状态。。。。isCH:" + isCH);

	// }
	//
	// } catch (Exception e) {
	// e.printStackTrace();
	// }
	// }
	// }

	// 该函数用于判断是否处于输入状态，处于输入状态即发IME=True来提示PC窗口激活输入法
	public void onStartInput(EditorInfo attribute, boolean restarting) {
		// ToastUtil.showToast(this, "onStartInput");
		super.onStartInput(attribute, restarting);
		if (delayFlag == true) {
			return;
		}
		try {
			// ToastUtil.showToast(this, "attribute.inputType..." +
			// attribute.inputType);
			// delayFlag = true;
			boolean isCH = false;
			// Timer delayTimer = new Timer();
			// DelayHandle delayHandle = new DelayHandle();
			// delayHandle.inputType = attribute.inputType;
			// delayTimer.schedule(delayHandle, 200);
			sendJson(IME_STATUS, "false");
			// ToastUtil.showToast(IME.this, "我关闭输入法" + isCH);
			if (attribute.inputType != 0) {
				isCH = true;
			} else {
				isCH = false;
			}
			// ToastUtil.showToast(IME.this, "isCH:"+isCH);
			if (isCH) {
				// 输入法打开指令
				sendJson(IME_STATUS, "true");
				// ToastUtil.showToast(IME.this,
				// "我已经发送true给你。。。请把他切换到输入状态。。。。isCH:" + isCH);
			}
		} catch (Exception e) {
		}

	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		// 判断是否是Ctrl + V ，若是则直接返回，防止重复粘贴
		if (event.isCtrlPressed()) {
			if (event.getKeyCode() == KeyEvent.KEYCODE_V) {
				return true;
			} else {
				if (keyCode <= 0xff && isFilterKeyCode(keyCode)) {
					return true;
				}
			}
		}

		return super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		return super.onKeyUp(keyCode, event);
	}

	public void ClipboardPaste(String text) {
		if (text.length() >= CLIP_TEXT.length()) {
			text = text.substring(CLIP_TEXT.length());
			SendText = text;
			clipboardManager.setPrimaryClip(ClipData.newPlainText(null,
					SendText));
		}
	}

	public void ClipboardCopy() {
		try {

			clipText = getCurrentInputConnection().getSelectedText(0);
			String CopyText = clipText.toString();
			SendText = CopyText;
			clipboardManager.setPrimaryClip(ClipData.newPlainText(null,
					CopyText));
			send(CopyText);
		} catch (Exception e) {
		}
	}

	public void ClipboardCut() {
		try {
			clipText = getCurrentInputConnection().getSelectedText(0);
			String CopyText = clipText.toString();
			this.getCurrentInputConnection().commitText("", 1);
			SendText = CopyText;
			clipboardManager.setPrimaryClip(ClipData.newPlainText(null,
					CopyText));
			send(CopyText);
		} catch (UnsupportedEncodingException e) {

		} catch (IOException e) {

		}
	}

}
