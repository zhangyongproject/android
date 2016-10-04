package com.buxiubianfu.IME;

import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.kaopu.download.BaseDownloadClickHelper;
import com.kaopu.download.BaseDownloadOperate;
import com.kaopu.download.BaseDownloadStateFactory.State;
import com.kaopu.download.abst.ADownloadDisplayHelper;
import com.kaopu.download.intf.IDownloadView;

/**
 * 下载TextView
 */
public class TestDownloadButton extends Button implements IDownloadView<TestDownloadInfo>, OnClickListener {

    /**
     * 下载信息
     */
    private TestDownloadInfo mDownloadInfo;

    private StartService mActivity;

    /**
     * 显示帮助
     */
    private TestDownloadButtonDisplayHelper mDisplayHelper = new TestDownloadButtonDisplayHelper(this);

    /**
     * 点击帮助
     */
    private BaseDownloadClickHelper<TestDownloadInfo> mClickHelper = new BaseDownloadClickHelper<TestDownloadInfo>(this) {

        @Override
        public void onDownloadedClick() {
        }

    };

    public TestDownloadButton(Context context) {
        super(context);
        init(context);
    }

    public TestDownloadButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public TestDownloadButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        mActivity = (StartService) context;
        setOnClickListener(this);
    }

    @Override
    protected void onAttachedToWindow() {
        mDisplayHelper.registerDownloadReceiver();
        super.onAttachedToWindow();
    }

    @Override
    protected void onDetachedFromWindow() {
        mDisplayHelper.unregisterDownloadReceiver();
        super.onDetachedFromWindow();
    }

    @Override
    public TestDownloadInfo getDownloadInfo() {
        return mDownloadInfo;
    }

    @Override
    public void setDownloadInfo(TestDownloadInfo info) {
        mDownloadInfo = info;
        mDisplayHelper.setDownloadInfo(info);
        mClickHelper.setDownloadInfo(info);
        mDownloadInfo.display(mDisplayHelper);
    }

    @Override
    public void setOnClickListener(OnClickListener l) {
        super.setOnClickListener(l);
    }

    @Override
    public void onClick(View v) {
        mDownloadInfo.onClick(mClickHelper);
    }

    @Override
    public boolean checkDownloadState(TestDownloadInfo info) {
        if (mDownloadInfo == null) {
            return false;
        }
        return mDownloadInfo.getIdentification().equals(info.getIdentification());
    }

    public class TestDownloadButtonDisplayHelper extends ADownloadDisplayHelper<TestDownloadInfo> {

        private TestDownloadButton mDownloadButton;

        private TestDownloadInfo mInfo;

        public TestDownloadButtonDisplayHelper(TestDownloadButton downloadButton) {
            super(downloadButton);
            mDownloadButton = downloadButton;
        }

        @Override
        public void onDownloadNewDisplay() {
            mDownloadButton.setText("下载");
            
        }

        @Override
        public void onDownloadWaitDisplay() {
            mDownloadButton.setText("等待");
        }

        @Override
        public void onDownloadingDisplay() {
            mDownloadButton.setText(mInfo.getdSize() * 100.0f / mInfo.getfSize() + "%");
            ToastUtil.showToast(mActivity, mInfo.getdSize() * 100.0f / mInfo.getfSize() + "%");
        }

        @Override
        public void onDownloadedDisplay() {
            Intent mIntent = new Intent(Utils.ACTION_NAME_1);
            mIntent.putExtra(Utils.FINISH_DOWNLOAD, "downloadFinish");
            mActivity.sendBroadcast(mIntent);
            mDownloadButton.setText("下载完成");
        }

        @Override
        public void onDownloadPausingDisplay() {
            mDownloadButton.setText("暂停");
        }

        @Override
        public void onDownloadPausedDisplay() {
            // mActivity.setInfo(mInfo);
            mDownloadButton.setText("已暂");
        }

        @Override
        public void onDownloadCancelingDisplay() {
            // mActivity.setInfo(mInfo);
            mDownloadButton.setText("取消");
        }

        @Override
        public void onDownloadFailedDisplay() {
            mDownloadButton.setText("下载失败");
        }

        @Override
        public void onDownloadNoneDisplay() {
            mDownloadButton.setText("未知错误");
        }

        @Override
        public void onDownloadConnectDisplay() {
            mDownloadButton.setText("连接");
        }

        @Override
        public void setDownloadInfo(TestDownloadInfo downloadInfo) {
            this.mInfo = downloadInfo;
        }

        @Override
        public TestDownloadInfo getDownloadInfo() {
            return mInfo;
        }

    }

    @Override
    public void cancel() {
        BaseDownloadOperate.cancelDownloadTask(getContext(), mDownloadInfo);
    }

    public void start() {
        BaseDownloadOperate.addNewDownloadTask(getContext(), mDownloadInfo);
    }

    @Override
    public void pause() {
        BaseDownloadOperate.pauseDownloadTask(getContext(), mDownloadInfo);
    }

    @Override
    public State getState() {
        return mDownloadInfo.getState().getState();
    }

}
