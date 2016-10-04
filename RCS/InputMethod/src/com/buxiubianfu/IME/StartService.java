package com.buxiubianfu.IME;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.kaopu.download.kernel.DownloadServiceConnection;
import com.kaopu.download.util.DownloadStringUtil;

public class StartService extends Activity {

	private TestDownloadButton downloadView;

	private int downProgress = 0;

	private TestDownloadInfo info;

	public TestDownloadButton getDownloadView() {
		return downloadView;
	}

	public void setDownloadView(TestDownloadButton downloadView) {
		this.downloadView = downloadView;
	}

	private final static String DOWNLOAD_DIR = Environment
			.getExternalStorageDirectory().getPath() + "/test/";

	public void setInfo(TestDownloadInfo info) {
		this.info = info;
	}

	public void setDownProgress(int downProgress) {
		this.downProgress = downProgress;
	}

	private DownloadServiceConnection connection;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_layout);
		connection = new DownloadServiceConnection(this);
		initConnection();
		registerBoradcastReceiver();
		Button btn = (Button) findViewById(R.id.start_service);

		// startService(new Intent("com.buxiubianfu.action.INPUT"));
		// downloadView = (TestDownloadButton)
		// findViewById(R.id.list_item_download_view);
		btn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				try {
					Intent mIntent = new Intent("ACTION_NAME_2");
					sendBroadcast(mIntent);
				} catch (Exception e) {
					e.printStackTrace();
				}

			}
		});
	}

	private void initConnection() {
		try {
			connection.bindDownloadService(null);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private void initData(String url) {
		info = new TestDownloadInfo();
		info.setSaveDir(DOWNLOAD_DIR);
		info.setDownloadWorkerClassName(MyDownloadWorker.class);
		info.setIdentification(url);
		info.setSaveName(DownloadStringUtil.getFileName(url, true));
		info.setUrl(url);
		info.setIndex(1);
		info.setCallBack(new DownloadCallBackImpl());
		downloadView.setDownloadInfo(info);
	}

	public void registerBoradcastReceiver() {
		IntentFilter myIntentFilter = new IntentFilter();
		myIntentFilter.addAction(Utils.ACTION_NAME);
		// ע��㲥
		registerReceiver(mBroadcastReceiver, myIntentFilter);
	}

	private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals(Utils.ACTION_NAME)) {
				if (intent.getStringExtra(Utils.INTENT_NAME).equals(
						"downloadapp")) {
					String url = intent
							.getStringExtra(Utils.START_DOWNLOAD_DATA);
					try {
						initData(url);
						downloadView.start();
					} catch (Exception e) {
						Toast.makeText(StartService.this, "���س���",
								Toast.LENGTH_SHORT).show();
						e.printStackTrace();
					}
				} else if (intent.getStringExtra(Utils.INTENT_NAME).equals(
						"getdownPause")) {
					downloadView.pause();
				}
			}
		}

	};

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(mBroadcastReceiver);
		connection.unBindDownloadService();
	}

}
