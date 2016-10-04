package com.cyjh.screenmirror;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BootReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context arg0, Intent arg1) {
		// TODO Auto-generated method stub

		Log.i("MirrorDisplay", "receive boot...");
		Intent intent = new Intent(arg0, BroadcastService.class);
		intent.setAction("com.cyjh.screenmirror.LIST_DEVICE");		
		arg0.startService(intent);
		
		MirrorManager.getInstance();
	}
}
