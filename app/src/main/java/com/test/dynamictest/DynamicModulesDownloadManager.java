package com.test.dynamictest;

import android.content.Context;
import android.content.IntentSender;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import com.google.android.play.core.splitinstall.SplitInstallException;
import com.google.android.play.core.splitinstall.SplitInstallHelper;
import com.google.android.play.core.splitinstall.SplitInstallManager;
import com.google.android.play.core.splitinstall.SplitInstallManagerFactory;
import com.google.android.play.core.splitinstall.SplitInstallRequest;
import com.google.android.play.core.splitinstall.SplitInstallSessionState;
import com.google.android.play.core.splitinstall.SplitInstallStateUpdatedListener;
import com.google.android.play.core.splitinstall.model.SplitInstallErrorCode;
import com.google.android.play.core.splitinstall.model.SplitInstallSessionStatus;
import com.google.android.play.core.tasks.OnFailureListener;
import com.google.android.play.core.tasks.OnSuccessListener;

import java.util.ArrayList;
import java.util.Set;

/**
 * Created by prankul.garg on 26/04/19.
 */
public class DynamicModulesDownloadManager {
    private static final String TAG = "PlayCore";

    private ArrayList<ModuleItem> modulesArrayList = new ArrayList<>();
    private boolean isDefferedInstallEnabled;
    private Context mContext;
    private SplitInstallStateUpdatedListener splitInstallStateUpdatedListener;
    private OnSuccessListener onSuccessListener;
    private OnFailureListener onFailureListener;

    private static DynamicModulesDownloadManager sInstance;

    private DynamicModulesDownloadManager(Context context) {
        mContext = context;
        modulesArrayList.add(getModuleItem("dynamic_feature"));
        modulesArrayList.add(getModuleItem("dynamic_feature1"));
        modulesArrayList.add(getModuleItem("dynamic_feature2"));
        modulesArrayList.add(getModuleItem("dynamic_feature3"));

        splitInstallStateUpdatedListener = new SplitInstallStateUpdatedListener() {
            @Override
            public void onStateUpdate(SplitInstallSessionState splitInstallSessionState) {
                toastAndLog("onStateUpdate: " + splitInstallSessionState.status());

                switch (splitInstallSessionState.status()) {
                    case SplitInstallSessionStatus.DOWNLOADING:
                        log("Start downloading...");
                        break;
                    case SplitInstallSessionStatus.REQUIRES_USER_CONFIRMATION:
                        try {
                            mContext.startIntentSender(splitInstallSessionState.resolutionIntent().getIntentSender(), null, 0, 0, 0);
                        } catch (IntentSender.SendIntentException e) {
                            e.printStackTrace();
                        }
                        break;
                    case SplitInstallSessionStatus.INSTALLED:
                        SplitInstallHelper.updateAppInfo(mContext.getApplicationContext());
                        break;
                }
            }
        };

        onSuccessListener = new OnSuccessListener() {
            @Override
            public void onSuccess(Object o) {
                toastAndLog("onSuccess: ");
            }
        };

        onFailureListener = new OnFailureListener() {
            @Override
            public void onFailure(Exception exception) {
                toastAndLog("onFailure: " + ((SplitInstallException) exception).getErrorCode());
                /*switch (((SplitInstallException) exception).getErrorCode()) {
                    case SplitInstallErrorCode.NETWORK_ERROR:
                        // Display a message that requests the user to establish a
                        // network connection.
                        break;
                    case SplitInstallErrorCode.ACTIVE_SESSIONS_LIMIT_EXCEEDED:
                        checkForActiveDownloads();
                        break;

                    case SplitInstallErrorCode.INCOMPATIBLE_WITH_EXISTING_SESSION:
                        checkForActiveDownloads();
                        break;
                }*/
                checkForActiveDownloads();
            }
        };
    }

    private void checkForActiveDownloads() {
        SplitInstallManager splitInstallManager = getSplitInstallManager(mContext);
        splitInstallManager
                .getSessionStates()  // Returns a SplitInstallSessionState object for each active session as a List.
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        toastAndLog("Active sessions count: " + task.getResult().size());
                        // Check for active sessions.
                        for (SplitInstallSessionState state : task.getResult()) {
                            toastAndLog("checkForActiveDownloads - onSuccess: status: " + state.status() + " sessionId: " + state.sessionId());
                            if (state.status() == SplitInstallSessionStatus.DOWNLOADING) {
                                // Cancel the request, or request a deferred installation.
                                splitInstallManager.cancelInstall(state.sessionId()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        toastAndLog("cancelInstall - onSuccess: ");
                                    }
                                });
                            }
                        }
                    }
                });
    }

    public static DynamicModulesDownloadManager getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new DynamicModulesDownloadManager(context);
        }
        return sInstance;
    }

    public ArrayList<ModuleItem> getModulesArrayList() {
        Set<String> installedModules = getSplitInstallManager(mContext).getInstalledModules();
        toastAndLog("installedModules size: " + installedModules.size());

        for (ModuleItem moduleItem : modulesArrayList) {
            if (installedModules.contains(moduleItem.getName())) {
                log("install status of " + moduleItem.getName() + " true");
                moduleItem.setInstalled(true);
            } else {
                log("install status of " + moduleItem.getName() + " false");
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
                splitInstallManager.deferredInstall(list).addOnSuccessListener(onSuccessListener).addOnFailureListener(onFailureListener);
                splitInstallManager.registerListener(splitInstallStateUpdatedListener);
            } else {
                SplitInstallRequest.Builder requestBuilder = SplitInstallRequest.newBuilder();
                for (ModuleItem moduleItem : modulesArrayList) {
                    if (!splitInstallManager.getInstalledModules().contains(moduleItem.getName())) {
                        requestBuilder.addModule(moduleItem.getName());
                    }
                }
                splitInstallManager.startInstall(requestBuilder.build()).addOnSuccessListener(onSuccessListener).addOnFailureListener(onFailureListener);
                splitInstallManager.registerListener(splitInstallStateUpdatedListener);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void installModule(String name) {
        try {
            toastAndLog("isDefferedInstallEnabled: " + isDefferedInstallEnabled);
            SplitInstallManager splitInstallManager = getSplitInstallManager(mContext);
            if (!splitInstallManager.getInstalledModules().contains(name)) {
                if (isDefferedInstallEnabled()) {
                    ArrayList<String> list = new ArrayList<>();
                    list.add(name);
                    splitInstallManager.deferredInstall(list).addOnSuccessListener(onSuccessListener).addOnFailureListener(onFailureListener);
                    splitInstallManager.registerListener(splitInstallStateUpdatedListener);
                } else {
                    SplitInstallRequest request = SplitInstallRequest.newBuilder()
                            .addModule(name)
                            .build();
                    splitInstallManager.startInstall(request).addOnSuccessListener(onSuccessListener).addOnFailureListener(onFailureListener);
                    splitInstallManager.registerListener(splitInstallStateUpdatedListener);
                }
            } else {
                toastAndLog("Already Installed");
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
            splitInstallManager.deferredUninstall(list).addOnSuccessListener(onSuccessListener).addOnFailureListener(onFailureListener);
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
                toastAndLog("Not Installed");
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

    private void toastAndLog(String message) {
        Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
        log(message);
    }

    private void log(String message) {
        Log.i(TAG, message);
    }
}
