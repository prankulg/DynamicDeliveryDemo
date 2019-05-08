package com.test.dynamictest;

import android.content.Context;
import android.content.IntentSender;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.play.core.splitinstall.SplitInstallException;
import com.google.android.play.core.splitinstall.SplitInstallHelper;
import com.google.android.play.core.splitinstall.SplitInstallManager;
import com.google.android.play.core.splitinstall.SplitInstallManagerFactory;
import com.google.android.play.core.splitinstall.SplitInstallRequest;
import com.google.android.play.core.splitinstall.SplitInstallSessionState;
import com.google.android.play.core.splitinstall.model.SplitInstallErrorCode;
import com.google.android.play.core.splitinstall.model.SplitInstallSessionStatus;
import com.google.android.play.core.tasks.OnSuccessListener;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Set;

/**
 * Created by prankul.garg on 26/04/19.
 */
public class DynamicModuleManager {
    private static final String TAG = "PlayCore-DynamicManager";

    private Context mContext;
    private Listener mListener;              //Client listener
    private String mModuleName = "";         //Module for which client want to listen
    private String mActiveModuleName = null; //Current downloading module
    private boolean isAnyActiveSession;      //if any module is downloading
    private ArrayDeque<String> mDeque;    //Queue of all requested modules

    private SplitInstallManager mSplitInstallManager;

    private static DynamicModuleManager sInstance;

    public static DynamicModuleManager getInstance() {
        if (sInstance == null) {
            synchronized (DynamicModuleManager.class){
                if (sInstance == null){
                    sInstance = new DynamicModuleManager();
                }
            }
        }
        return sInstance;
    }

    private DynamicModuleManager() {
        mContext = DynamicApplication.getAppContext();
        mDeque = new ArrayDeque<>();
        init();
    }

    private void init() {
        mSplitInstallManager = SplitInstallManagerFactory.create(mContext);
        mSplitInstallManager.registerListener(splitInstallSessionState -> {
            log("onStateUpdate: " + splitInstallSessionState.toString());
            switch (splitInstallSessionState.status()) {
                case SplitInstallSessionStatus.DOWNLOADING:
                    if (isAnyoneToListen()) {
                        int percent = (int) (100 * splitInstallSessionState.bytesDownloaded() / splitInstallSessionState.totalBytesToDownload());
                        mListener.onDownloading(percent);
                    }
                    break;

                case SplitInstallSessionStatus.DOWNLOADED:
                    if (isAnyoneToListen()) {
                        mListener.onDownloaded();
                    }
                    break;

                case SplitInstallSessionStatus.REQUIRES_USER_CONFIRMATION:
                    if (isAnyoneToListen()) {
                        try {
                            mContext.startIntentSender(splitInstallSessionState.resolutionIntent().getIntentSender(), null, 0, 0, 0);
                        } catch (IntentSender.SendIntentException e) {
                            e.printStackTrace();
                        }
                    } else {
                        //This state will come only when client fire and forget request
                        //that means the client doesn't want to show anything to user
                        checkAndCancelExistingSessions();
                    }
                    break;

                case SplitInstallSessionStatus.INSTALLING:
                    if (isAnyoneToListen()) {
                        mListener.onInstalling();
                    }
                    break;

                case SplitInstallSessionStatus.INSTALLED:
                    SplitInstallHelper.updateAppInfo(mContext.getApplicationContext());
                    if (isAnyoneToListen()) {
                        mListener.onInstalled();
                    }

                    resetSessionAndCheckForNext();
                    break;

                case SplitInstallSessionStatus.FAILED:
                    if (isAnyoneToListen()) {
                        mListener.onFailed();
                    }

                    resetSessionAndCheckForNext();
                    break;

                case SplitInstallSessionStatus.CANCELING:
                    if (isAnyoneToListen()) {
                        mListener.onCancelling();
                    }
                    break;

                case SplitInstallSessionStatus.CANCELED:
                    if (isAnyoneToListen()) {
                        mListener.onCancelled();
                    }

                    resetSessionAndCheckForNext();
                    break;
            }
        });
    }

    private void resetSessionAndCheckForNext() {
        isAnyActiveSession = false;
        mActiveModuleName = null;
        installModuleIfPending();
    }

    public void startInstall(ArrayList<String> modulesArrayList) {
        if (!Utils.isNetworkAvailable(mContext)) return;

        for (String moduleName : modulesArrayList) {
            addInQueue(moduleName, false);
        }
        installModuleIfPending();
    }

    public void startInstall(String name) {
        if (!Utils.isNetworkAvailable(mContext)){
            log("isNetworkAvailable: " + false);
            if (isAnyoneToListen()){
                mListener.onNetworkError();
            }
            return;
        }

        //If some other module is getting downloaded then notify client
        if (isAnyActiveSession && mListener != null && !TextUtils.isEmpty(mModuleName) && !TextUtils.isEmpty(mActiveModuleName) && !mModuleName.equalsIgnoreCase(mActiveModuleName)) {
            mListener.onAlreadyActiveSession(mActiveModuleName);
        }

        addInQueue(name, true);
        installModuleIfPending();
    }

    private void addInQueue(String moduleName, boolean isOnDemand) {
        if (!mSplitInstallManager.getInstalledModules().contains(moduleName)
                && !mDeque.contains(moduleName)
                && !moduleName.equalsIgnoreCase(mActiveModuleName)) {
            if (isOnDemand){
                mDeque.addFirst(moduleName);
            } else {
                mDeque.addLast(moduleName);
            }
        }
    }

    private void installModuleIfPending() {
        log("mDeque size: " + mDeque.size());
        log("isAnyActiveSession: " + isAnyActiveSession);

        if (mDeque.isEmpty() || isAnyActiveSession) return;

        isAnyActiveSession = true;
        mActiveModuleName = mDeque.poll();
        SplitInstallRequest splitInstallRequest = SplitInstallRequest.newBuilder().addModule(mActiveModuleName).build();
        mSplitInstallManager.startInstall(splitInstallRequest)
                .addOnSuccessListener(o -> {
                    log("onSuccess");
                    if (isAnyoneToListen()) {
                        mListener.onRequestSuccess();
                    }
                })
                .addOnFailureListener(exception -> {
                    int errorCode = SplitInstallErrorCode.NO_ERROR;
                    if (exception instanceof SplitInstallException){
                        errorCode = ((SplitInstallException) exception).getErrorCode();
                    }
                    log("onFailure:: errorCode: " + errorCode);

                    switch (errorCode) {
                        case SplitInstallErrorCode.NETWORK_ERROR:
                            if (isAnyoneToListen()) {
                                mListener.onNetworkError();
                            }
                            break;


                        case SplitInstallErrorCode.INSUFFICIENT_STORAGE:
                            if (isAnyoneToListen()) {
                                mListener.onInsufficientStorage();
                            }
                            break;

                        default:
                            if (isAnyoneToListen()) {
                                mListener.onRequestFailed(errorCode);
                            }
                            break;
                    }

                    resetSessionAndCheckForNext();
                });
    }

    public void deferredInstall(ArrayList<String> modulesArrayList) {
        mSplitInstallManager.deferredInstall(modulesArrayList);
    }

    public void deferredInstall(String name) {
        if (!mSplitInstallManager.getInstalledModules().contains(name)) {
            ArrayList<String> list = new ArrayList<>();
            list.add(name);
            mSplitInstallManager.deferredInstall(list);
        }
    }

    public void deferredUninstall(String name) {
        if (mSplitInstallManager.getInstalledModules().contains(name)) {
            ArrayList<String> list = new ArrayList<>();
            list.add(name);
            mSplitInstallManager.deferredUninstall(list);
        }
    }

    public Set<String> getInstalledModules() {
        return mSplitInstallManager.getInstalledModules();
    }

    public boolean isInstalled(String moduleName) {
        return mSplitInstallManager.getInstalledModules().contains(moduleName);
    }

    private void checkAndCancelExistingSessions() {
        log("checkAndCancelExistingSessions");
        mSplitInstallManager
                .getSessionStates()  // Returns a SplitInstallSessionState object for each active session as a List.
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        log("Active sessions count: " + task.getResult().size());
                        // Check for active sessions.
                        for (SplitInstallSessionState state : task.getResult()) {
                            log("Modules: " + state.moduleNames().toString() + " status: " + state.status() + " sessionId: " + state.sessionId());

                            if (state.status() == SplitInstallSessionStatus.REQUIRES_USER_CONFIRMATION) {
                                // Cancel the request, or request a deferred installation.
                                mSplitInstallManager.cancelInstall(state.sessionId()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        log("cancelInstall - onSuccess: Modules: " + state.moduleNames().toString());
                                        resetSessionAndCheckForNext();
                                    }
                                });
                            }
                        }
                    }
                });
    }

    private void log(String message) {
        Log.i(TAG, message);
    }

    private boolean isAnyoneToListen() {
//        Don't have 'splitInstallSessionState' in case of 'requestFailure'
//        HashSet<String> modules = new HashSet<>(splitInstallSessionState.moduleNames());
//        return (mListener != null && modules.contains(mModuleName));
        log("mListener != null: " + (mListener != null));
        log("mModuleName: " + mModuleName);
        log("mActiveModuleName: " + mActiveModuleName);
        return (mListener != null && !TextUtils.isEmpty(mModuleName) && (TextUtils.isEmpty(mActiveModuleName) || mModuleName.equalsIgnoreCase(mActiveModuleName)));
    }

    public void registerListener(Listener listener, String moduleName) {
        mListener = listener;
        mModuleName = moduleName;
    }

    public void unRegisterListener() {
        mListener = null;
        mModuleName = "";
    }

    public interface Listener {
        /**
         * Request accepted and downloading starts
         */
        void onDownloading(int downloadedPercentage);

        /**
         * Download completed
         */
        void onDownloaded();

        /**
         * installation starts
         */
        void onInstalling();

        /**
         * Installation completed
         */
        void onInstalled();

        /**
         * User cancelled the request from notification or by some other medium
         */
        void onCancelling();

        /**
         * Download cancelled
         */
        void onCancelled();

        /**
         * Request accepted but failed to download
         */
        void onFailed();

        /**
         * Request accepted
         */
        void onRequestSuccess();

        /**
         * Request accepted and added in queue
         */
        void onAlreadyActiveSession(String currentDownloadingModuleName);

        /**
         * Request rejected
         */
        void onRequestFailed(int splitInstallErrorCode);

        /**
         * Request rejected
         */
        void onNetworkError();

        /**
         * Request rejected
         */
        void onInsufficientStorage();
    }
}
