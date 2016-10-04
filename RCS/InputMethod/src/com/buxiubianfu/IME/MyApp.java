package com.buxiubianfu.IME;

import android.app.Application;

public class MyApp extends Application{
    @Override
    public void onCreate() {
        super.onCreate();
        SharedData.getInstance().init(this);
    }
}
