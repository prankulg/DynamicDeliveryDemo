package com.test.dynamictest;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;


public class CommonDynamicLoaderActivity extends AppCompatActivity implements DynamicModuleManager.Listener {

    private static final String TAG = "PlayCore";

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
        initActivity = resultIntent.getStringExtra(DynamicModuleManager.EXTRA_INIT_ACTIVITY);
        initModule = resultIntent.getStringExtra(DynamicModuleManager.EXTRA_INIT_MODULE);

        DynamicModuleManager.getInstance(this).startInstall(initModule);
    }

    private void startModuleActivity() {
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
        super.onResume();
        DynamicModuleManager.getInstance(this).registerListener(this, initModule);
    }

    @Override
    protected void onPause() {
        DynamicModuleManager.getInstance(this).unRegisterListener();
        super.onPause();

                    String listener=resultIntent.getStringExtra("listener");
                    if (!TextUtils.isEmpty(listener)){
                        initializeListener(listener);
                    }

    }

    @Override
    public void onRequestSuccess() {
        log("onRequestSuccess");
    }

    /***
     *
     * @param listenerClasspath
     */
    private void initializeListener(String listenerClasspath){
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
    public void onRequestFailed(int splitInstallErrorCode) {
        log("onRequestFailed: " + splitInstallErrorCode);
    }

    @Override
    public void onDownloading(int downloadedPercentage) {
        AnimationFactory.startWalletLoader(mainLoaderView);
        log("onDownloading: " + downloadedPercentage);
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
    }

    @Override
    public void onAlreadyActiveSession(String currentDownloadingModuleName) {
        log("onAlreadyActiveSession" + currentDownloadingModuleName);
    }

    @Override
    public void onNetworkError() {
        log("onNetworkError");
    }

    @Override
    public void onInsufficientStorage() {
        log("onInsufficientStorage");
    }
}
