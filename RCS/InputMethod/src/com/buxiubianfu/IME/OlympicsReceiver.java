package com.buxiubianfu.IME;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class OlympicsReceiver extends BroadcastReceiver {

    static final String ACTION = "android.intent.action.BOOT_COMPLETED";

    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(ACTION)) {
            try {
                context.startService(new Intent(context, IME.class));
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                context.startService(new Intent("com.blue.uyou.inputservice.InputService"));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
