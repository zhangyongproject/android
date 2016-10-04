package com.cyjh.rcs.activity;

import com.cyjh.rcs.service.ExecuteRemoteController;
import com.cyjh.rcs.service.ServiceRemoteController;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;

public class ActivityLandscape extends Activity implements Runnable {
    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        new Handler().postDelayed(this, 2000);
    }

    @Override
    public void run() {
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getRealMetrics(metrics);
        Intent intent = new Intent(this, ServiceRemoteController.class);
        intent.putExtra(ExecuteRemoteController.SYS_RCS_LANDSCAPE_WIDTH, metrics.widthPixels);
        intent.putExtra(ExecuteRemoteController.SYS_RCS_LANDSCAPE_HEIGHT, metrics.heightPixels);
        startService(intent);
        finish();
    }
}
