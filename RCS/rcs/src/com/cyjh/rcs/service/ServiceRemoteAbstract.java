package com.cyjh.rcs.service;

import java.util.Locale;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.os.IBinder;
import android.text.TextUtils;

import com.cyjh.library.Command;
import com.cyjh.library.Processor.Callback;
import com.cyjh.library.Utilities;

public abstract class ServiceRemoteAbstract extends Service implements Callback {

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    public Intent getIntent(Command command) {
        Intent intent = new Intent();
        String parameter = command.get("action");
        if (parameter != null) {
            Utilities.log(this, "set action: %s", parameter);
            intent.setAction(parameter);
        }
        parameter = command.get("component");
        if (parameter != null) {
            ComponentName component = ComponentName.unflattenFromString(parameter);
            if (component != null) {
                Utilities.log(this, "set component: %s", component);
                intent.setComponent(component);
            }
        }
        parameter = command.get("category");
        if (parameter != null) {
            intent.addCategory(parameter);
        }
        parameter = command.get("flags");
        if (parameter != null) {
            parameter = parameter.toLowerCase(Locale.ENGLISH);
            if (parameter.matches("0x[0-9a-f]+")) {
                int flags = Integer.parseInt(parameter.substring(2), 16);
                Utilities.log(this, "set flags: 0x%08x", flags);
                intent.setFlags(flags);
            } else if (parameter.matches("\\d+")) {
                int flags = Integer.parseInt(parameter);
                Utilities.log(this, "set flags: 0x%08x", flags);
                intent.setFlags(flags);
            }
        }
        for (String key : command.keys()) {
            if (key.startsWith("s:")) {
                String value = command.get(key);
                key = key.substring(2);
                Utilities.log(this, "set string extra: %s = '%s'", key, value);
                intent.putExtra(key, value);
            }
            if (key.startsWith("i:")) {
                String value = command.get(key);
                key = key.substring(2);
                Utilities.log(this, "set integer extra: %s = '%s'", key, value);
                try {
                    intent.putExtra(key.substring(2), Integer.parseInt(command.get(key)));
                } catch (Exception e) {
                    Utilities.log(this, e);
                }

            }
        }
        return intent;
    }

    @Override
    public void execute(Command command) {
        if (command.getName().equals("start")) {
            if (TextUtils.equals(command.get("type"), "activity")) {
                startActivity(getIntent(command));
            }
        } else if (TextUtils.equals(command.getName(), "send")) {
            if (TextUtils.equals(command.get("type"), "broadcast")) {
                sendBroadcast(getIntent(command));
            }
        }
    }
}
