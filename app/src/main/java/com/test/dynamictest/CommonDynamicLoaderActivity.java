package com.test.dynamictest;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;


public class CommonDynamicLoaderActivity extends AppCompatActivity implements DynamicModuleManager.Listener {

    private static final String TAG = "PlayCore";
    public static String EXTRA_INIT_ACTIVITY = "EXTRA_INIT_ACTIVITY";
    public static String EXTRA_INIT_MODULE = "EXTRA_INIT_MODULE";

    private LottieAnimationView mainLoaderView;
    private TextView txtProgress;
    private Intent resultIntent;
    private String initActivity;
    private String initModule;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dynamic_loader);

        mainLoaderView = findViewById(R.id.mainLoaderView);
        txtProgress = findViewById(R.id.progress);
        resultIntent = getIntent();
        initActivity = resultIntent.getStringExtra(EXTRA_INIT_ACTIVITY);
        initModule = resultIntent.getStringExtra(EXTRA_INIT_MODULE);

        DynamicModuleManager.getInstance().registerListener(this, initModule);
        DynamicModuleManager.getInstance().startInstall(initModule);
    }

    private void startModuleActivity() {
        String listener = resultIntent.getStringExtra("listener");
        if (!TextUtils.isEmpty(listener)) {
            initializeListener(listener);
        }

        AnimationFactory.stopWalletLoader(mainLoaderView);
        // navigating to module init activity
        resultIntent.setClassName(BuildConfig.APPLICATION_ID, initActivity);
        startActivity(resultIntent);
        finish();
    }

    private void log(String message) {
        Log.i(TAG, message);
        txtProgress.setText(message);
    }

    @Override
    protected void onResume() {
        DynamicModuleManager.getInstance().registerListener(this, initModule);
        super.onResume();
    }

    @Override
    protected void onPause() {
        DynamicModuleManager.getInstance().unRegisterListener();
        super.onPause();
    }

    /***
     *
     * @param listenerClasspath
     */
    private void initializeListener(String listenerClasspath) {
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

    @Override
    public void onDownloading(int downloadedPercentage) {
        AnimationFactory.startWalletLoader(mainLoaderView);
        log("onDownloading: " + downloadedPercentage + "%");
    }

    @Override
    public void onDownloaded() {
        log("onDownloaded");
    }

    @Override
    public void onInstalling() {
        log("onInstalling");
    }

    @Override
    public void onInstalled() {
        log("onInstalled");
        startModuleActivity();
    }

    @Override
    public void onCancelling() {
        log("onCancelling");
    }

    @Override
    public void onCancelled() {
        log("onCancelled");
    }

    @Override
    public void onFailed() {
        log("onFailed");
        showRetry("onFailed");
    }

    @Override
    public void onRequestSuccess() {
        log("onRequestSuccess");
    }

    @Override
    public void onAlreadyActiveSession(String currentDownloadingModuleName) {
        log("Currently downloading " + currentDownloadingModuleName + "feature, so adding it in a queue");
    }

    @Override
    public void onRequestFailed(int splitInstallErrorCode) {
        log("onRequestFailed: " + splitInstallErrorCode);
        showRetry("Something went wrong! " + splitInstallErrorCode);
    }

    @Override
    public void onNetworkError() {
        log("onNetworkError");
        showRetry("Check your internet connection.");
    }

    @Override
    public void onInsufficientStorage() {
        log("onInsufficientStorage");
        showRetry("Insufficient Storage.");
    }

    private void showRetry(String message) {
        Snackbar.make(findViewById(R.id.root), message, Snackbar.LENGTH_INDEFINITE)
                .setAction("Retry", v -> DynamicModuleManager.getInstance().startInstall(initModule))
                .show();
    }
}
