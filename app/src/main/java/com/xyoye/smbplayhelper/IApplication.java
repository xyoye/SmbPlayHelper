package com.xyoye.smbplayhelper;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;

/**
 * Created by xyoye on 2019/7/15.
 */

public class IApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }
}
