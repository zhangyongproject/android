//package com.buxiubianfu.IME;
//
//import com.kaopu.download.util.DownloadStringUtil;
//
//import android.content.BroadcastReceiver;
//import android.content.Context;
//import android.content.Intent;
//import android.widget.Toast;
//
//public class DownloadBroadcastReceiver extends BroadcastReceiver{
//
//	@Override
//	public void onReceive(Context context, Intent intent) {
//
//        String action = intent.getAction();
//        if (action.equals(Utils.ACTION_NAME)) {
//            if (intent.getStringExtra(Utils.INTENT_NAME).equals("downloadapp")) {
//                String url = intent.getStringExtra(Utils.START_DOWNLOAD_DATA);
//                try {
//                   DownloadManger.getInstance().startDown(url);
//                } catch (Exception e) {
//                    Toast.makeText(context, "ÏÂÔØ³ö´í£¡", Toast.LENGTH_SHORT).show();
//                    e.printStackTrace();
//                }
//            } else if (intent.getStringExtra(Utils.INTENT_NAME).equals("getdownPause")) {
//                downloadView.pause();
//            }
//        }
//    
//		
//	}
//	 
//
//}
