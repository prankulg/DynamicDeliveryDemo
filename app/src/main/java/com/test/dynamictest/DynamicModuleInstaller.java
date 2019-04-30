package com.test.dynamictest;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
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
public class DynamicModuleInstaller {
    private static final String TAG = "PlayCore-DFMInstaller";

    public static String EXTRA_INIT_ACTIVITY = "EXTRA_INIT_ACTIVITY";
    public static String EXTRA_INIT_MODULE = "EXTRA_INIT_MODULE";

    private Context mContext;
    private Listener mListener;
    private ArrayList<ModuleItem> modulesArrayList = new ArrayList<>();

    private SplitInstallManager mSplitInstallManager;
    private OnSuccessListener onSuccessListener;
    private OnFailureListener onFailureListener;
    private SplitInstallStateUpdatedListener splitInstallStateUpdatedListener;

    public DynamicModuleInstaller(Context context) {
        mContext = context;
        mSplitInstallManager = SplitInstallManagerFactory.create(context);
        modulesArrayList.add(new ModuleItem("dynamic_feature"));
        modulesArrayList.add(new ModuleItem("dynamic_feature1"));
        modulesArrayList.add(new ModuleItem("dynamic_feature2"));
        modulesArrayList.add(new ModuleItem("dynamic_feature3"));

        initListeners();


    }

    private void initListeners() {
        //Install Request (Start - In-progress - Finish)
        splitInstallStateUpdatedListener = splitInstallSessionState -> {
            log("onStateUpdate: " + splitInstallSessionState.status());
            switch (splitInstallSessionState.status()) {
                case SplitInstallSessionStatus.DOWNLOADING:
                    if (mListener != null) {
                        int percent = (int) (100 * splitInstallSessionState.bytesDownloaded() / splitInstallSessionState.totalBytesToDownload());
                        mListener.onDownloading(percent);
                    }
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
                    if (mListener != null) {
                        mListener.onInstalled(modules);
                    }
                    break;

                case SplitInstallSessionStatus.FAILED:
                    if (mListener != null) {
                        mListener.onFailed(SplitInstallSessionStatus.FAILED);
                    }
                    break;

                case SplitInstallSessionStatus.CANCELED:
                    if (mListener != null) {
                        mListener.onCancelled();
                    }
                    break;
            }
        };

        //Install Request Accepted
        onSuccessListener = o -> toastAndLog("onSuccess");

        //Install Request Rejected
        onFailureListener = exception -> {
            if (mListener != null) {
                int errorCode = ((SplitInstallException) exception).getErrorCode();
                toastAndLog("onFailure: " + errorCode);

                switch (errorCode) {
                    case SplitInstallErrorCode.ACTIVE_SESSIONS_LIMIT_EXCEEDED:
                    case SplitInstallErrorCode.INCOMPATIBLE_WITH_EXISTING_SESSION:
                        checkAndCancelExistingSessions();
                        break;

                    default:
                        mListener.onFailed(errorCode);
                        break;
                }
            }
        };
    }

    private void checkAndCancelExistingSessions() {
        mSplitInstallManager
                .getSessionStates()  // Returns a SplitInstallSessionState object for each active session as a List.
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        toastAndLog("Active sessions count: " + task.getResult().size());
                        // Check for active sessions.
                        for (SplitInstallSessionState state : task.getResult()) {
                            toastAndLog("checkAndCancelExistingSessions - onSuccess: status: " + state.status() + " sessionId: " + state.sessionId());
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
            moduleItem.setInstalled(installedModules.contains(moduleItem.getName()));
            log("install status of " + moduleItem.getName() + " :" + moduleItem.isInstalled());
        }
        return modulesArrayList;
    }

    public void deferredInstallAll() {
        ArrayList<String> list = new ArrayList<>();
        for (ModuleItem moduleItem : modulesArrayList) {
            list.add(moduleItem.getName());
        }
        mSplitInstallManager.deferredInstall(list).addOnSuccessListener(onSuccessListener).addOnFailureListener(onFailureListener);
        mSplitInstallManager.registerListener(splitInstallStateUpdatedListener);
    }

    public void startInstallAll() {
        SplitInstallRequest.Builder requestBuilder = SplitInstallRequest.newBuilder();
        for (ModuleItem moduleItem : modulesArrayList) {
            if (!mSplitInstallManager.getInstalledModules().contains(moduleItem.getName())) {
                requestBuilder.addModule(moduleItem.getName());
            }
        }
        mSplitInstallManager.startInstall(requestBuilder.build()).addOnSuccessListener(onSuccessListener).addOnFailureListener(onFailureListener);
        mSplitInstallManager.registerListener(splitInstallStateUpdatedListener);
    }

    public void deferredInstall(String name) {
        if (!mSplitInstallManager.getInstalledModules().contains(name)) {
            ArrayList<String> list = new ArrayList<>();
            list.add(name);
            mSplitInstallManager.deferredInstall(list).addOnSuccessListener(onSuccessListener).addOnFailureListener(onFailureListener);
        } else {
            toastAndLog("Already Installed");
        }
    }

    public void startInstall(String name) {
        if (!mSplitInstallManager.getInstalledModules().contains(name)) {
            SplitInstallRequest request = SplitInstallRequest.newBuilder().addModule(name).build();
            mSplitInstallManager.startInstall(request).addOnSuccessListener(onSuccessListener).addOnFailureListener(onFailureListener);
            mSplitInstallManager.registerListener(splitInstallStateUpdatedListener);
        } else {
            toastAndLog("Already Installed");
        }
    }

    public void deferredUninstallAll() {
        ArrayList<String> list = new ArrayList<>();
        for (ModuleItem moduleItem : modulesArrayList) {
            list.add(moduleItem.getName());
        }
        mSplitInstallManager.deferredUninstall(list).addOnSuccessListener(onSuccessListener).addOnFailureListener(onFailureListener);
    }

    public void deferredUninstall(String name) {
        if (mSplitInstallManager.getInstalledModules().contains(name)) {
            ArrayList<String> list = new ArrayList<>();
            list.add(name);
            mSplitInstallManager.deferredUninstall(list);
        } else {
            toastAndLog("Not Installed");
        }
    }

    public void loadAndLaunchModule(Activity activity, String moduleName, String moduleActivity) {
        if (mSplitInstallManager.getInstalledModules().contains(moduleName)) {
            log("Module already installed");
            Intent intent = new Intent();
            intent.setClassName(activity, moduleActivity);
            activity.startActivity(intent);
        } else {
            log("Module not installed, So need to install it first!");
            Intent intent = new Intent(activity, CommonDynamicLoaderActivity.class);
            intent.putExtra(EXTRA_INIT_ACTIVITY, moduleActivity);
            intent.putExtra(EXTRA_INIT_MODULE, moduleName);
            activity.startActivity(intent);
        }
    }

    private void toastAndLog(String message) {
        Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
        log(message);
    }

    private void log(String message) {
        Log.i(TAG, message);
    }

    public void registerListener(Listener listener) {
        mListener = listener;
    }

    public void unRegisterListener() {
        mListener = null;
    }

    public interface Listener {
        void onDownloading(int downloadedPercentage);

        void onInstalled(HashSet<String> hashSet);

        void onFailed(int splitInstallErrorCode);

        void onCancelled();
    }
}
