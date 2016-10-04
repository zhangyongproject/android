package com.cyjh.library;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Map.Entry;
import java.util.Properties;

import android.content.Context;

public class Processor implements Runnable {
    public interface Callback {

        void execute(Command parse);

    }

    private final Context mContext;
    private final Callback mCallback;
    private final Properties mProperties;
    private final Class<?> mProgram;
    private BufferedReader mIn;
    private BufferedWriter mOut;

    public Processor(Context context, Callback callback, Class<?> program) {
        mContext = context;
        mCallback = callback;
        mProgram = program;
        mProperties = new Properties();
        try {
            File file = Utilities.getExternalStorageFile(".rcs.profile");
            FileInputStream in = new FileInputStream(file);
            mProperties.load(in);
            in.close();
        } catch (IOException e) {
            Utilities.log(this, e);
        }
    }

    @Override
    public void run() {
        while (true) {
            try {
                Utilities.log(this, "start process: /system/xbin/su");
                Process process = Runtime.getRuntime().exec("/system/xbin/su");
                mIn = new BufferedReader(new InputStreamReader(process.getInputStream()));
                mOut = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
                mOut.append("export CLASSPATH=").append(mContext.getApplicationInfo().sourceDir);
                mOut.newLine();
                mOut.append("export CLASSNAME=").append(mProgram.getName());
                mOut.newLine();
                mOut.append("/system/bin/app_process /system/bin $CLASSNAME; exit");
                mOut.newLine();
                mOut.flush();
                for (Entry<Object, Object> entry : mProperties.entrySet()) {
                    execute(new Command("set").set(String.valueOf(entry.getKey()), entry.getValue()));
                }
                mOut.flush();
                for (String line = mIn.readLine(); line != null; line = mIn.readLine()) {
                    Utilities.log(this, line);
                    mCallback.execute(Command.parse(line));
                }
                Utilities.log(this, "stop process: /system/xbin/su");
            } catch (IOException e) {
                Utilities.log(this, e);
            }
        }
    }

    public void execute(Command command) throws IOException {
        mOut.write(command.toString());
        mOut.newLine();
        mOut.flush();
    }

    public void set(String key, Object value) {
        if (key == null || value == null) {
            return;
        }
        mProperties.put(key, String.valueOf(value));

    }
}
