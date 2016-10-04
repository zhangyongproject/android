package com.buxiubianfu.IME;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import com.ugame.upgrade.aidl.IUpgradeService;

public class UpgradeService extends Service{

    private static final String TAG  = UpgradeService.class.getSimpleName();

    IUpgradeService.Stub stub = new IUpgradeService.Stub(){

        @Override
        public void onResult(String result) throws RemoteException {
            Log.d(TAG,"onResult :"+result);
            Intent intent = new Intent();
            intent.setClass(UpgradeService.this,IME.class);
            intent.putExtra(IME.SERVICE_CMD, IME.ANDROID_TO_PC_UPGRADEINFO);
            intent.putExtra(IME.SERVICE_SENDJSON_TO_PC_DATA,result);
            startService(intent);
        }

        @Override
        public void onUpgradeFinish(String versionName) throws RemoteException {
            Log.d(TAG,"onUpgradeFinish :"+versionName);
            Intent intent = new Intent();
            intent.setClass(UpgradeService.this,IME.class);
            intent.putExtra(IME.SERVICE_CMD, IME.ANDROID_TO_PC_UPGRADEFINISH);
            intent.putExtra(IME.SERVICE_SENDJSON_TO_PC_DATA,versionName);
            startService(intent);
        }

        @Override
        public String getPcVersion() throws RemoteException {
            String pc_version = SharedData.getInstance().readData("pc_version", "");
            Log.d(TAG,"getPcVersion :"+pc_version);
            return pc_version;
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind ");
        return stub;
    }
}
