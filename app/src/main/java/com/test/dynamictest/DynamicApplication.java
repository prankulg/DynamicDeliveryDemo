package com.test.dynamictest;

import android.content.Context;
import android.support.multidex.MultiDexApplication;

import com.google.android.play.core.splitcompat.SplitCompat;

public class DynamicApplication extends MultiDexApplication {

    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
    }

    public static Context getAppContext() {
        return context;
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        SplitCompat.install(this);
    }
}
