package com.buxiubianfu.IME.command;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import com.buxiubianfu.IME.SharedData;
import com.buxiubianfu.IME.command.Data.CommandData;
import org.json.JSONException;
import org.json.JSONObject;


public class GetUpdateInfoCommand extends BaseCommand {

    private static final String TAG = GetUpdateInfoCommand.class.getSimpleName();
    public GetUpdateInfoCommand(Context context){
        super(context);
    }
    @Override
    public String Do(CommandData commandData) {
        String cmdData = commandData.getCmdData();
        if(!TextUtils.isEmpty(cmdData))
        {
            try {
                JSONObject jsonObject = new JSONObject(cmdData);
                String version = jsonObject.getString("version");
                SharedData.getInstance().saveData("pc_version",version);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        Intent intent = new Intent("com.ugame.upgrade.broadcast.ACTION");
        intent.putExtra("keyword","check");
        Log.d("GetUpdateInfoCommand","发送检测系统升级广播");
        get_Context().sendBroadcast(intent);
        return null;
    }
}
