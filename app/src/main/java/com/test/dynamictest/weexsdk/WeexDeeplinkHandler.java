package com.test.dynamictest.weexsdk;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Button;

import com.google.android.play.core.splitinstall.SplitInstallManager;
import com.google.android.play.core.splitinstall.SplitInstallManagerFactory;
import com.test.dynamictest.CommonDynamicLoaderActivity;

import java.lang.ref.WeakReference;


/**
 * Created by vikas.rathour
 */
public class WeexDeeplinkHandler {
    private static  WeexDeeplinkHandler ourInstance = null;



    DDWeexCommunicator commonMallInterface;

    public static String EXTRA_INIT_ACTIVITY = "EXTRA_INIT_ACTIVITY";
    public static String EXTRA_INIT_MODULE = "EXTRA_INIT_MODULE";
    private static final String LISTENER="net.one97.paytm.weexsdk.DDWeexCommImpl";


    static {
        ourInstance = new WeexDeeplinkHandler();
        initializeListener(LISTENER);
    }

    public static WeexDeeplinkHandler getInstance() {
        return ourInstance;
    }

    private WeexDeeplinkHandler() {

    }



    private static void initializeListener(String listenerClasspath){
        try {
            Class.forName(listenerClasspath).newInstance();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }
    }

    public void loadAndLaunchModule(Activity activity, String moduleName, String moduleActivity) {
        SplitInstallManager mSplitInstallManager = SplitInstallManagerFactory.create(activity.getApplicationContext());
        if (mSplitInstallManager.getInstalledModules().contains(moduleName)) {
            initializeListener(LISTENER);
            Log.i("POC","module " + moduleName +" is installed");
            Intent intent = new Intent();
            intent.setClassName(activity, moduleActivity);
            activity.startActivity(intent);
        } else {
            Log.i("POC","module " + moduleName +" not installed");
            Intent intent = new Intent(activity, CommonDynamicLoaderActivity.class);
            intent.putExtra(EXTRA_INIT_ACTIVITY, moduleActivity);
            intent.putExtra(EXTRA_INIT_MODULE, moduleName);
            intent.putExtra("listener", LISTENER);
            activity.startActivity(intent);
        }
    }

    public boolean isInstalled(Context context,String moduleName){
        SplitInstallManager mSplitInstallManager = SplitInstallManagerFactory.create(context);
        return mSplitInstallManager.getInstalledModules().contains(moduleName);
    }


    public void registerWeexsdkListener(DDWeexCommunicator commonInterface) {
        this.commonMallInterface = commonInterface;
    }

    public void unRegisterWeexsdkListener() {
        this.commonMallInterface = null;
    }


    public void sendSingletonActivityInstance(WeakReference<Activity> activityWeakReference) {
        if (commonMallInterface != null) {
            commonMallInterface.sendActivityInstance(activityWeakReference);
        }
    }

    public void sendSingletonMessageToDDF(String s,Context context) {
        if (commonMallInterface != null) {
            commonMallInterface.sendMessageToDDF(s,context);
        }
    }

    public void sendSingletonViewToDDF(Button btnShow,WeakReference<Activity> activityWeakReference) {
        if (commonMallInterface != null) {
            commonMallInterface.sendViewToDDF(btnShow,activityWeakReference);
        }
    }
}
