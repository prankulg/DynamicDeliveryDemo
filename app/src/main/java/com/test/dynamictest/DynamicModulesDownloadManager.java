package com.test.dynamictest;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.google.android.play.core.splitinstall.SplitInstallManager;
import com.google.android.play.core.splitinstall.SplitInstallManagerFactory;
import com.google.android.play.core.splitinstall.SplitInstallRequest;

import java.util.ArrayList;
import java.util.Set;

/**
 * Created by prankul.garg on 26/04/19.
 */
public class DynamicModulesDownloadManager {
    private static final String TAG = "split_porsa_";

    private ArrayList<ModuleItem> modulesArrayList = new ArrayList<>();
    private boolean isDefferedInstallEnabled;
    private Context mContext;

    private static DynamicModulesDownloadManager sInstance;

    private DynamicModulesDownloadManager(Context context) {
        mContext = context;
        modulesArrayList.add(getModuleItem("dynamic_feature"));
        modulesArrayList.add(getModuleItem("dynamic_feature1"));
        modulesArrayList.add(getModuleItem("dynamic_feature2"));
    }

    public static DynamicModulesDownloadManager getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new DynamicModulesDownloadManager(context);
        }
        return sInstance;
    }

    public ArrayList<ModuleItem> getModulesArrayList() {
        Set<String> installedModules = getSplitInstallManager(mContext).getInstalledModules();
        Log.i(TAG, TAG + "installedModules size: " + installedModules.size());

        for (ModuleItem moduleItem : modulesArrayList) {
            if (installedModules.contains(moduleItem.getName())) {
                Log.i(TAG, TAG + "install status of " + moduleItem.getName() + " true");
                moduleItem.setInstalled(true);
            } else {
                Log.i(TAG, TAG + "install status of " + moduleItem.getName() + " false");
                moduleItem.setInstalled(false);
            }
        }
        return modulesArrayList;
    }

    public void installAllModules() {
        try {
            SplitInstallManager splitInstallManager = getSplitInstallManager(mContext);
            if (isDefferedInstallEnabled()) {
                ArrayList<String> list = new ArrayList<>();
                for (ModuleItem moduleItem : modulesArrayList) {
                    list.add(moduleItem.getName());
                }
                splitInstallManager.deferredInstall(list);
            } else {
                SplitInstallRequest.Builder requestBuilder = SplitInstallRequest.newBuilder();
                for (ModuleItem moduleItem : modulesArrayList) {
                    if (!splitInstallManager.getInstalledModules().contains(moduleItem.getName())) {
                        requestBuilder.addModule(moduleItem.getName());
                    }
                }
                splitInstallManager.startInstall(requestBuilder.build());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void installModule(String name) {
        try {
            Log.i(TAG, TAG + "isDefferedInstallEnabled: " + isDefferedInstallEnabled);
            SplitInstallManager splitInstallManager = getSplitInstallManager(mContext);
            if (!splitInstallManager.getInstalledModules().contains(name)) {
                if (isDefferedInstallEnabled()) {
                    ArrayList<String> list = new ArrayList<>();
                    list.add(name);
                    splitInstallManager.deferredInstall(list);
                } else {
                    SplitInstallRequest request = SplitInstallRequest.newBuilder()
                            .addModule(name)
                            .build();
                    splitInstallManager.startInstall(request);
                }
            } else {
                Toast.makeText(mContext, "Already Installed", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void unInstallAllModules() {
        try {
            ArrayList<String> list = new ArrayList<>();
            for (ModuleItem moduleItem : modulesArrayList) {
                list.add(moduleItem.getName());
            }

            SplitInstallManager splitInstallManager = getSplitInstallManager(mContext);
            splitInstallManager.deferredUninstall(list);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void unInstallModule(String name) {
        try {
            SplitInstallManager splitInstallManager = getSplitInstallManager(mContext);
            if (splitInstallManager.getInstalledModules().contains(name)) {
                ArrayList<String> list = new ArrayList<>();
                list.add(name);
                splitInstallManager.deferredUninstall(list);
            } else {
                Toast.makeText(mContext, "Already UnInstalled", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private ModuleItem getModuleItem(String name) {
        ModuleItem moduleItem = new ModuleItem();
        moduleItem.setName(name);
        moduleItem.setInstalled(false);
        return moduleItem;
    }

    public SplitInstallManager getSplitInstallManager(Context context) {
        return SplitInstallManagerFactory.create(context);
    }

    public boolean isDefferedInstallEnabled() {
        return isDefferedInstallEnabled;
    }

    public void setDefferedInstallEnabled(boolean isChecked) {
        isDefferedInstallEnabled = isChecked;
    }
}
