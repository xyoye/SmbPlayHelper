package com.xyoye.smbplayhelper;

import android.app.Application;
import android.content.Context;

/**
 * Created by xyoye on 2019/12/24.
 */

public class IApplication extends Application {

    private static Application application;

    @Override
    public void onCreate() {
        super.onCreate();
        application = this;
    }

    public static Context getApplictionContext(){
        return application.getApplicationContext();
    }
}
