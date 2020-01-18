package com.xyoye.smbplayhelper.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.xyoye.smbplayhelper.IApplication;

public class SPUtils {
    private SharedPreferences sharedPreferences;

    private static class Holder {
        static SPUtils instance = new SPUtils();
    }

    private SPUtils() {
        sharedPreferences = IApplication._getContext()
                .getSharedPreferences("date", Context.MODE_PRIVATE);
    }

    public static SPUtils getInstance() {
        return Holder.instance;
    }

    public void putString(String key, String value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public String getString(String key) {
        return sharedPreferences.getString(key, "");
    }

    public void putBoolean(String key, boolean value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

    public boolean getBoolean(String key) {
        return sharedPreferences.getBoolean(key, false);
    }

    public boolean getBoolean(String key, boolean defValue) {
        return sharedPreferences.getBoolean(key, defValue);
    }
}