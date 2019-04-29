package com.test.dynamictest;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.support.v4.content.LocalBroadcastManager;
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
import java.util.HashSet;
import java.util.Set;

/**
 * Created by prankul.garg on 26/04/19.
 */
public class DynamicModulesDownloadManager {
    private static final String TAG = "PlayCore";

    public static String INTENT_ACTION_DFM_MODULE_INSTALLED = "action_dfm_module_installed";
    public static String EXTRA_MODULE_NAMES = "module_names";

    public static String EXTRA_MODULE_STATUS = "module_status";
    public static String MODULE_STATUS_FAILED = "Failed";
    public static String MODULE_STATUS_SUCCESS = "Success";
    public static String MODULE_STATUS_CANCELED = "Canceled";

    public static String EXTRA_MODULE_ERROR = "module_error";
    public static String MODULE_ERROR_SESSION = "session";
    public static String MODULE_ERROR_NETWORK = "network";
    public static String MODULE_ERROR_INSUFFICIENT_STORAGE = "insufficient_storage";

    public static String EXTRA_INIT_ACTIVITY = "EXTRA_INIT_ACTIVITY";
    public static String EXTRA_INIT_MODULE = "EXTRA_INIT_MODULE";

    private Context mContext;
    private ArrayList<ModuleItem> modulesArrayList = new ArrayList<>();

    private boolean isDefferedInstallEnabled;

    private SplitInstallManager mSplitInstallManager;
    private OnSuccessListener onSuccessListener;
    private OnFailureListener onFailureListener;
    private SplitInstallStateUpdatedListener splitInstallStateUpdatedListener;


    private static DynamicModulesDownloadManager sInstance;

    public static DynamicModulesDownloadManager getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new DynamicModulesDownloadManager(context);
        }
        return sInstance;
    }

    private DynamicModulesDownloadManager(Context context) {
        mContext = context;
        mSplitInstallManager = SplitInstallManagerFactory.create(context);
        modulesArrayList.add(new ModuleItem("dynamic_feature"));
        modulesArrayList.add(new ModuleItem("dynamic_feature1"));
        modulesArrayList.add(new ModuleItem("dynamic_feature2"));
        modulesArrayList.add(new ModuleItem("dynamic_feature3"));

        Intent intent = new Intent();
        intent.setAction(INTENT_ACTION_DFM_MODULE_INSTALLED);
        splitInstallStateUpdatedListener = splitInstallSessionState -> {
            toastAndLog("onStateUpdate: " + splitInstallSessionState.status());
            switch (splitInstallSessionState.status()) {
                case SplitInstallSessionStatus.DOWNLOADING:
                    // TODO: 2019-04-29 Calculate download progress
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
                    HashSet<String> modules = new HashSet<>(splitInstallSessionState.moduleNames());
                    intent.putExtra(EXTRA_MODULE_NAMES, modules);
                    intent.putExtra(EXTRA_MODULE_STATUS, MODULE_STATUS_SUCCESS);
                    LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
                    break;

                case SplitInstallSessionStatus.FAILED:
                    intent.putExtra(EXTRA_MODULE_STATUS, MODULE_STATUS_FAILED);
                    LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
                    break;

                case SplitInstallSessionStatus.CANCELED:
                    intent.putExtra(EXTRA_MODULE_STATUS, MODULE_STATUS_CANCELED);
                    LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
                    break;
            }
        };

        onSuccessListener = o -> toastAndLog("onSuccess: ");

        onFailureListener = exception -> {
            int errorCode = ((SplitInstallException) exception).getErrorCode();
            toastAndLog("onFailure: " + errorCode);
            switch (errorCode) {
                case SplitInstallErrorCode.NETWORK_ERROR:
                    intent.putExtra(EXTRA_MODULE_ERROR, MODULE_ERROR_NETWORK);
                    break;

                case SplitInstallErrorCode.ACTIVE_SESSIONS_LIMIT_EXCEEDED:
                case SplitInstallErrorCode.INCOMPATIBLE_WITH_EXISTING_SESSION:
                    intent.putExtra(EXTRA_MODULE_ERROR, MODULE_ERROR_SESSION);
                    checkForActiveDownloads();
                    break;

                case SplitInstallErrorCode.INSUFFICIENT_STORAGE:
                    intent.putExtra(EXTRA_MODULE_ERROR, MODULE_ERROR_INSUFFICIENT_STORAGE);
                    break;

                default:
                    break;
            }
            intent.putExtra(EXTRA_MODULE_STATUS, MODULE_STATUS_FAILED);
            LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
        };
    }

    private void checkForActiveDownloads() {
        mSplitInstallManager
                .getSessionStates()  // Returns a SplitInstallSessionState object for each active session as a List.
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        toastAndLog("Active sessions count: " + task.getResult().size());
                        // Check for active sessions.
                        for (SplitInstallSessionState state : task.getResult()) {
                            toastAndLog("checkForActiveDownloads - onSuccess: status: " + state.status() + " sessionId: " + state.sessionId());
                            // TODO: 2019-04-29 Add only intended error codes here
                            if (state.status() == SplitInstallSessionStatus.REQUIRES_USER_CONFIRMATION) {
                                // Cancel the request, or request a deferred installation.
                                mSplitInstallManager.cancelInstall(state.sessionId()).addOnSuccessListener(new OnSuccessListener<Void>() {
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

    public ArrayList<ModuleItem> getModulesArrayList() {
        Set<String> installedModules = mSplitInstallManager.getInstalledModules();
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
            if (isDefferedInstallEnabled()) {
                ArrayList<String> list = new ArrayList<>();
                for (ModuleItem moduleItem : modulesArrayList) {
                    list.add(moduleItem.getName());
                }
                mSplitInstallManager.deferredInstall(list).addOnSuccessListener(onSuccessListener).addOnFailureListener(onFailureListener);
                mSplitInstallManager.registerListener(splitInstallStateUpdatedListener);
            } else {
                SplitInstallRequest.Builder requestBuilder = SplitInstallRequest.newBuilder();
                for (ModuleItem moduleItem : modulesArrayList) {
                    if (!mSplitInstallManager.getInstalledModules().contains(moduleItem.getName())) {
                        requestBuilder.addModule(moduleItem.getName());
                    }
                }
                mSplitInstallManager.startInstall(requestBuilder.build()).addOnSuccessListener(onSuccessListener).addOnFailureListener(onFailureListener);
                mSplitInstallManager.registerListener(splitInstallStateUpdatedListener);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void installModule(String name) {
        try {
            toastAndLog("isDefferedInstallEnabled: " + isDefferedInstallEnabled);
            if (!mSplitInstallManager.getInstalledModules().contains(name)) {
                if (isDefferedInstallEnabled()) {
                    ArrayList<String> list = new ArrayList<>();
                    list.add(name);
                    mSplitInstallManager.deferredInstall(list).addOnSuccessListener(onSuccessListener).addOnFailureListener(onFailureListener);
                    mSplitInstallManager.registerListener(splitInstallStateUpdatedListener);
                } else {
                    SplitInstallRequest request = SplitInstallRequest.newBuilder()
                            .addModule(name)
                            .build();
                    mSplitInstallManager.startInstall(request).addOnSuccessListener(onSuccessListener).addOnFailureListener(onFailureListener);
                    mSplitInstallManager.registerListener(splitInstallStateUpdatedListener);
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

            mSplitInstallManager.deferredUninstall(list).addOnSuccessListener(onSuccessListener).addOnFailureListener(onFailureListener);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void unInstallModule(String name) {
        try {
            if (mSplitInstallManager.getInstalledModules().contains(name)) {
                ArrayList<String> list = new ArrayList<>();
                list.add(name);
                mSplitInstallManager.deferredUninstall(list);
            } else {
                toastAndLog("Not Installed");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void loadAndLaunchModule(Activity activity, String moduleName, String moduleActivity) {
        if (isInstalled(moduleName)) {
            log("Module already installed");
            Intent intent = new Intent();
            intent.setClassName(activity, moduleActivity);
            activity.startActivity(intent);
        } else {
            log("Module not already installed");
            Intent intent = new Intent(activity, CommonDynamicLoaderActivity.class);
            intent.putExtra(EXTRA_INIT_ACTIVITY, moduleActivity);
            intent.putExtra(EXTRA_INIT_MODULE, moduleName);
            activity.startActivity(intent);
        }
    }

    private boolean isInstalled(String moduleName) {
        Set<String> installedModules = mSplitInstallManager.getInstalledModules();
        return installedModules.contains(moduleName);
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
