package com.cyjh.rcs;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.cyjh.library.Utilities;
import com.cyjh.rcs.service.ServiceRemoteCommand;
import com.cyjh.rcs.service.ServiceRemoteController;

public class Launcher extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Utilities.log(this, "system boot completed...");
        intent = new Intent(context, ServiceRemoteController.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startService(intent);
        intent = new Intent(context, ServiceRemoteCommand.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startService(intent);
    }
}
