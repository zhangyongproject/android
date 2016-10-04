package com.cyjh.rcs.service;

import java.io.IOException;

import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.os.Handler;

import com.cyjh.library.Command;
import com.cyjh.library.Processor;
import com.cyjh.library.Utilities;
import com.cyjh.rcs.activity.ActivityLandscape;
import com.cyjh.rcs.activity.ActivityPortrait;

public class ServiceRemoteController extends ServiceRemoteAbstract {

    private class OrientationUpdate implements Runnable {

        private int mScreenOrientation;

        @Override
        public void run() {
            mHandler.removeCallbacks(this);
            mHandler.postDelayed(this, 3000);
            int orientation = getResources().getConfiguration().orientation;
            if (orientation == mScreenOrientation) {
                return;
            }
            Processor processor = mProcessor;
            if (processor == null) {
                return;
            }
            mScreenOrientation = orientation;
            mScreenOrientation = getResources().getConfiguration().orientation;
            Command command = new Command("set").set(ExecuteRemoteController.SYS_RCS_SCREEN_ORIENTATION, mScreenOrientation);
            try {
                processor.execute(command);
            } catch (IOException e) {
                Utilities.log(this, e);
            }
        }

        private void update() {
            mHandler.removeCallbacks(this);
            mHandler.post(this);
        }

    }

    private final Handler mHandler = new Handler();
    private final OrientationUpdate mOrientationUpdate = new OrientationUpdate();
    private Processor mProcessor;
    private int mLandscapeWidth;
    private int mLandscapeHeight;
    private int mPortraitWidth;
    private int mPortraitHeight;

    @Override
    public synchronized int onStartCommand(Intent intent, int flags, int start) {
        Utilities.log(this, "onStartCommand()");
        if (intent != null) {
            if (intent.hasExtra(ExecuteRemoteController.SYS_RCS_LANDSCAPE_WIDTH)) {
                mLandscapeWidth = intent.getIntExtra(ExecuteRemoteController.SYS_RCS_LANDSCAPE_WIDTH, 0);
            }
            if (intent.hasExtra(ExecuteRemoteController.SYS_RCS_LANDSCAPE_HEIGHT)) {
                mLandscapeHeight = intent.getIntExtra(ExecuteRemoteController.SYS_RCS_LANDSCAPE_HEIGHT, 0);
            }
            if (intent.hasExtra(ExecuteRemoteController.SYS_RCS_PORTRAIT_WIDTH)) {
                mPortraitWidth = intent.getIntExtra(ExecuteRemoteController.SYS_RCS_PORTRAIT_WIDTH, 0);
            }
            if (intent.hasExtra(ExecuteRemoteController.SYS_RCS_PORTRAIT_HEIGHT)) {
                mPortraitHeight = intent.getIntExtra(ExecuteRemoteController.SYS_RCS_PORTRAIT_HEIGHT, 0);
            }
        }
        if (mLandscapeWidth == 0 || mLandscapeHeight == 0) {
            intent = new Intent(this, ActivityLandscape.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
            startActivity(intent);
        } else if (mPortraitWidth == 0 || mPortraitHeight == 0) {
            intent = new Intent(this, ActivityPortrait.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
            startActivity(intent);
        } else if (mProcessor == null) {
            Utilities.log(this, "start process thread");
            Utilities.log(this, "landscape: %s x %s, portrait: %s x %s", mLandscapeWidth, mLandscapeHeight, mPortraitWidth, mPortraitHeight);
            mProcessor = new Processor(this, this, ExecuteRemoteController.class);
            mProcessor.set(ExecuteRemoteController.SYS_RCS_LANDSCAPE_WIDTH, mLandscapeWidth);
            mProcessor.set(ExecuteRemoteController.SYS_RCS_LANDSCAPE_HEIGHT, mLandscapeHeight);
            mProcessor.set(ExecuteRemoteController.SYS_RCS_PORTRAIT_WIDTH, mPortraitWidth);
            mProcessor.set(ExecuteRemoteController.SYS_RCS_PORTRAIT_HEIGHT, mPortraitHeight);
            mProcessor.set(ExecuteRemoteController.SYS_RCS_VERSION, getVersionString());
            new Thread(mProcessor).start();
        }

        return super.onStartCommand(intent, flags, start);
    }

    private String getVersionString() {
        try {
            return getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        } catch (NameNotFoundException e) {
            return "0.0.0.0";
        }
    }

    @Override
    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        mOrientationUpdate.update();
    }

}
