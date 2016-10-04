package com.buxiubianfu.IME;

import android.os.Parcel;
import android.os.Parcelable;

import com.kaopu.download.BaseDownloadWorker.DownloadCallBack;

public class DownloadCallBackImpl implements DownloadCallBack {

	@Override
	public void onDownloadPaused(String url) {
	}

	@Override
	public void onDownloadWorking(String url, long totalSize,
			long downloadSize, int progress) {

		// DownInfo mInfo = (DownInfo)
		// DownloadWorkerSupervisor.getDownloadInfo(url);
		// mInfo.setdSize(totalSize);
		// mInfo.setdSize(downloadSize);
		// mInfo.setState(BaseDownloadStateFactory.getDownloadingState());

	}

	@Override
	public void onDownloadCompleted(String url, String file, long totalSize) {
	}

	@Override
	public void onDownloadStart(String url, long downloadSize) {
	}

	@Override
	public void onDownloadWait(String url) {
		// DownInfo mInfo = (DownInfo)
		// DownloadWorkerSupervisor.getDownloadInfo(url);
		// mInfo.setState(BaseDownloadStateFactory.getDownloadWaitState());
	}

	@Override
	public void onDownloadPausing(String url) {
		// DownInfo mInfo = (DownInfo)
		// DownloadWorkerSupervisor.getDownloadInfo(url);
		// mInfo.setState(BaseDownloadStateFactory.getDownloadPausingState());
		// mInfo.setSpeed(0);
	}

	@Override
	public void onDownloadCanceling(String url) {

	}

	@Override
	public void onDownloadFailed(String url) {

	}

	public DownloadCallBackImpl(Parcel p) {

	}

	public DownloadCallBackImpl() {

	}

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub

		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onDownloadCanceled(String url) {
		// TODO Auto-generated method stub

	}

	public final static Parcelable.Creator<DownloadCallBackImpl> CREATOR = new Parcelable.Creator<DownloadCallBackImpl>() {
		// 重写Creator
		@Override
		public DownloadCallBackImpl createFromParcel(Parcel source) {
			DownloadCallBackImpl s = new DownloadCallBackImpl(source);
			return s;
		}

		@Override
		public DownloadCallBackImpl[] newArray(int size) {
			return new DownloadCallBackImpl[size];
		}
	};

}
