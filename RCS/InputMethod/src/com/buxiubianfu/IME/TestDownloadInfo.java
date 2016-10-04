package com.buxiubianfu.IME;

import android.os.Parcel;
import android.os.Parcelable;

import com.kaopu.download.kernel.BaseDownloadInfo;

public class TestDownloadInfo extends BaseDownloadInfo {

	private int index;

	public TestDownloadInfo() {
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		super.writeToParcel(dest, flags);
		dest.writeInt(index);
	}

	public TestDownloadInfo(Parcel p) {
		super.readFromParcel(p);
		readFromParcel(p);
	}

	public final static Parcelable.Creator<TestDownloadInfo> CREATOR = new Parcelable.Creator<TestDownloadInfo>() {
		// 重写Creator

		@Override
		public TestDownloadInfo createFromParcel(Parcel source) {
			TestDownloadInfo s = new TestDownloadInfo(source);
			return s;
		}

		@Override
		public TestDownloadInfo[] newArray(int size) {
			return new TestDownloadInfo[size]; 
		}
	};

	public void readFromParcel(Parcel p) {
		setIndex(p.readInt());
	}

}
