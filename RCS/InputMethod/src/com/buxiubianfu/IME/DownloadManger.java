//package com.buxiubianfu.IME;
//
//import android.os.Environment;
//
//import com.kaopu.download.util.DownloadStringUtil;
//
//public class DownloadManger {
//	   private TestDownloadInfo info;
//	   private static DownloadManger manger;
//	   private final static String DOWNLOAD_DIR = Environment.getExternalStorageDirectory().getPath() + "/test/";
//	   private DownloadManger(){};
//	   public static DownloadManger getInstance(){
//		   if(manger==null){
//			   manger = new DownloadManger();
//		   }
//		   return manger;
//	   }
//	private void initData(String url) {
//        info = new TestDownloadInfo();
//        info.setSaveDir(DOWNLOAD_DIR);
//        info.setDownloadWorkerClassName(MyDownloadWorker.class);
//        info.setIdentification(url);
//        info.setSaveName(DownloadStringUtil.getFileName(url, true));
//        info.setUrl(url);
//        info.setIndex(1);
//        info.setCallBack(new DownloadCallBackImpl());
//    }
//	public void startDown(String url){
//		initData(url);
//	}
//	
//	
//}
