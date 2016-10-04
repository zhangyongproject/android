package com.cyjh.screenmirror;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.util.Iterator;
import java.util.Map;

import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.cyjh.screenmirror.MirrorServer;

@SuppressLint("HandlerLeak")
public class MainActivity extends Activity implements OnClickListener{

	public final static String LOGTAG = "MirrorDisplay";
	
	public static final int SERVICE_IS_RUNNING = 10000;
	public static final int SERVICE_IS_NOT_RUNNING = 10010;
	
	enum CONNECT_STATE{
		UNKNOWN_STATE(100),
		STARTED_STATE (200),
		STARTING_STATE(300),
		STOPPED_STATE(400),
		STOPPING_STATE(500);
		
		int state;
		private CONNECT_STATE(int index) {
			state = index;
		}
		public String toString() {
			switch(state) {
			default:
				return "UNKNOWN_STATE";
			case 200:
				return "STARTED_STATE";
			case 300:
				return "STARTING_STATE";
			case 400:	
				return "STOPPED_STATE";
			case 500:
				return "STOPPING_STATE";
			}
		}
	};
	
	public CONNECT_STATE connectState = CONNECT_STATE.UNKNOWN_STATE;
	
	private TextView viewIP;
	private Button btMirrorStart;
	private Button btMirrorStop;
	
	private Process mirrorProc;
	private String apkPath;

	protected CONNECT_STATE getMirrorState() {
		return connectState;
	}
	
	private boolean initMirrorPath() {
			
		apkPath = getApkPath();
				
		if(apkPath != "") {
			Log.e(LOGTAG, "apkPath "+ apkPath);
			return true;
		} else {
			return false;
		}
	}
	
	private String getApkPath()
	{
		String cmdline = "pm path com.cyjh.screenmirror\n";
		try {
			Process proc = Runtime.getRuntime().exec(cmdline);
			BufferedReader br =new BufferedReader(new InputStreamReader(proc.getInputStream()));
			apkPath = br.readLine();
			return apkPath.substring("package:".length());
			
		}catch(IOException e) {
			return "";
		}
	}
	
	public String[] getEnvironment() {
		Map<String,String> map = System.getenv();
		String myEnv[] = new String[map.size()];
		int i=0;
		for(Iterator<String> it=map.keySet().iterator(); it.hasNext();i++) {
			String key=it.next();
			String value=map.get(key);
			myEnv[i]=key+"="+value;
			
		}
		return myEnv;
	}
	
	private void startMirrorService() {
		if(connectState != CONNECT_STATE.STOPPED_STATE)
			return;
		
		Log.i(LOGTAG, "start Mirror Service");
		try {
			
			String cmdline[] = new String[]{
					"su",
					"root",
					"/system/bin/sh",
					"-c",
					"export CLASSPATH="+apkPath+";"+
					"/system/bin/app_process /system/bin com.cyjh.screenmirror.Main"
			};
			
			String oldEnv[]  = getEnvironment();
			String env[] = new String[oldEnv.length+1];
			System.arraycopy(oldEnv, 0, env, 0, oldEnv.length);
			env[env.length-1] = "CLASSPATH="+apkPath;
		
			mirrorProc= Runtime.getRuntime().exec(cmdline,env,new File("/system/bin"));
			handler.sendEmptyMessage(SERVICE_IS_RUNNING);
		}catch(IOException e) {
			Toast.makeText(this, "启动服务失败", Toast.LENGTH_LONG).show();
			e.printStackTrace();
		}
	}
	
	
	
	public String intToIP(int ip) 
	{
		return (ip & 0xFF) + "." +
			   ((ip >>8) &0xFF) + "." +
			   ((ip >>16)&0xff)	+ "." +
			   ((ip >>24)&0xff);
	}
	
	public void checkService() {
		Thread t = new Thread() {
			public void run() {
				if(MirrorServer.checkMirrorServer())
					handler.sendEmptyMessage(SERVICE_IS_RUNNING);
				else
					handler.sendEmptyMessage(SERVICE_IS_NOT_RUNNING);
			}
		};
		t.start();
	}
	
	public void checkAndStopService() {
		Thread t = new Thread() {
			public void run() {
				if(MirrorServer.checkMirrorServer()) {
					MirrorServer.requestStop();
					handler.sendEmptyMessage(SERVICE_IS_NOT_RUNNING);
				}
				if(mirrorProc != null) {
					mirrorProc.destroy();
					mirrorProc = null;
				}
			}
		};
		t.start();
	};
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.i(LOGTAG, "MirrorDisplay Activity started");
        
        viewIP = (TextView)findViewById(R.id.ipAddress);
        btMirrorStart = (Button)findViewById(R.id.btStart);
        btMirrorStop = (Button)findViewById(R.id.button1);
        btMirrorStop.setEnabled(false);
        
        WifiManager wifiManager = (WifiManager)getSystemService(Context.WIFI_SERVICE);
        if(!wifiManager.isWifiEnabled()) {
        	wifiManager.setWifiEnabled(true);
        }
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        int ipAddress = wifiInfo.getIpAddress();
        
        String ip = intToIP(ipAddress);
        viewIP.setText(ip);
        
        initMirrorPath();
        
        btMirrorStart.setOnClickListener(this);
        btMirrorStop.setOnClickListener(this);
        
        checkService();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }


    public void startUDPService() {
    	Intent intent = new Intent(this, BroadcastService.class);
    	intent.setAction("com.cyjh.screenmirror.LIST_DEVICE");
    	startService(intent);
    }
    
    void stopUDPService() {
    	Intent intent = new Intent(this, BroadcastService.class);
    	intent.setAction("com.cyjh.screenmirror.LIST_DEVICE");
    	stopService(intent);
    }
    
    private Handler handler = new Handler() {
    	public void handleMessage(Message msg) {
    		switch (msg.what) {
    		case SERVICE_IS_RUNNING:
    			Log.e("MirrorDisplay", "handle is running");
    			btMirrorStart.setEnabled(false);
    			btMirrorStop.setEnabled(true);
    			connectState = CONNECT_STATE.STARTED_STATE;
    				break;
    		case SERVICE_IS_NOT_RUNNING:
    			Log.e("MirrorDisplay", "handle is not running");
    			btMirrorStart.setEnabled(true);
    			btMirrorStop.setEnabled(false);
    			connectState = CONNECT_STATE.STOPPED_STATE;
    				break;
    		}
    	}
    };
    
	@Override
	public void onClick(View arg0) {
		// TODO Auto-generated method stub
		switch(arg0.getId()) {
		case R.id.btStart:
			if(connectState == CONNECT_STATE.STOPPED_STATE) {
				Log.e("MirrorDisplay", "start it");
				startMirrorService();
				startUDPService();	
			} else {
				Toast.makeText(this, "Service: "+connectState, Toast.LENGTH_LONG).show();
			}
			break;
		
		case R.id.button1:
			if(connectState == CONNECT_STATE.STARTED_STATE){
				checkAndStopService();
				stopUDPService();
			} else {
				Toast.makeText(this, "Service: "+connectState, Toast.LENGTH_LONG).show();
			}
		}
	}
    
}
