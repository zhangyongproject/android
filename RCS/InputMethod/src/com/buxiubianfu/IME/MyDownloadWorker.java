package com.buxiubianfu.IME;

import android.content.Context;

import com.kaopu.download.BaseDownloadWorker;

public class MyDownloadWorker extends BaseDownloadWorker<TestDownloadInfo> {

	public MyDownloadWorker(Context context, TestDownloadInfo info) {
		super(context, info);
	}

}
