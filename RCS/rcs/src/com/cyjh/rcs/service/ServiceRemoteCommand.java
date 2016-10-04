package com.cyjh.rcs.service;

import com.cyjh.library.Processor;
import com.cyjh.library.Utilities;

public class ServiceRemoteCommand extends ServiceRemoteAbstract {

    private Processor mProcessor;

    @Override
    public void onCreate() {
        super.onCreate();
        Utilities.log(this, "start process thread");
        mProcessor = new Processor(this, this, ExecuteRemoteCommand.class);
        new Thread(mProcessor).start();
    }

}
