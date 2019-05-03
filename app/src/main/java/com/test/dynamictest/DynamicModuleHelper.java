package com.test.dynamictest;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import java.util.ArrayList;

/**
 * Created by prankul.garg on 2019-05-01.
 */
public class DynamicModuleHelper {

    public static ArrayList<String> getAllDynamicModulesList() {
        //Add all DD modules here
        ArrayList<String> modulesArrayList = new ArrayList<>();
        modulesArrayList.add("dynamic_feature");
        modulesArrayList.add("weexsdk");
        modulesArrayList.add("dynamic_feature1");
        modulesArrayList.add("dynamic_feature2");
        modulesArrayList.add("dynamic_feature3");
        return modulesArrayList;
    }

    public static void loadAndLaunchModule(Activity activity, String moduleName, String moduleActivity) {
        if (DynamicModuleManager.getInstance().isInstalled(moduleName)) {
            Intent intent = new Intent();
            intent.setClassName(activity, moduleActivity);
            activity.startActivity(intent);
        } else {
            Intent intent = new Intent(activity, CommonDynamicLoaderActivity.class);
            intent.putExtra(CommonDynamicLoaderActivity.EXTRA_INIT_ACTIVITY, moduleActivity);
            intent.putExtra(CommonDynamicLoaderActivity.EXTRA_INIT_MODULE, moduleName);
            activity.startActivity(intent);
        }
    }

    public static boolean isEligibleToDownloadInBackground(Context context){
        long availableInternalMemorySizeInMB = DynamicModuleUtils.getAvailableInternalMemorySizeInMB();
        int batteryPercentage = DynamicModuleUtils.getBatteryPercentage(context);
        boolean isBatteryCharging = DynamicModuleUtils.isBatteryCharging(context);
        boolean isFastInternet = DynamicModuleUtils.isFastInternetConnection(context);

        return (availableInternalMemorySizeInMB >= 200 && (batteryPercentage >= 30 || isBatteryCharging) && isFastInternet);
    }
}
