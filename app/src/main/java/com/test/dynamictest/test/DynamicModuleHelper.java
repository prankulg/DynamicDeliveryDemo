package com.test.dynamictest.test;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;

import com.test.dynamictest.CommonDynamicLoaderActivity;
import com.test.dynamictest.DynamicModuleManager;

import java.util.ArrayList;

/**
 * Created by prankul.garg on 2019-05-01.
 */
public class DynamicModuleHelper {

    public static ArrayList<DynamicModuleItem> getModulesArrayList() {
        ArrayList<DynamicModuleItem> modulesArrayList = new ArrayList<>();
        //Add all DD modules here
        modulesArrayList.add(getDynamicModuleItem("dynamic_feature"));
        modulesArrayList.add(getDynamicModuleItem("weexsdk"));
        modulesArrayList.add(getDynamicModuleItem("dynamic_feature1"));
        modulesArrayList.add(getDynamicModuleItem("dynamic_feature2"));
        modulesArrayList.add(getDynamicModuleItem("dynamic_feature3"));
        return modulesArrayList;
    }

    @NonNull
    private static DynamicModuleItem getDynamicModuleItem(String moduleName) {
        DynamicModuleItem dynamicModuleItem = new DynamicModuleItem(moduleName);
        dynamicModuleItem.setInstalled(DynamicModuleManager.getInstance().isInstalled(moduleName));
        return dynamicModuleItem;
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
}
