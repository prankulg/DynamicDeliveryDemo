package com.test.dynamictest;

import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.play.core.splitinstall.SplitInstallHelper;
import com.google.android.play.core.splitinstall.SplitInstallManager;
import com.google.android.play.core.splitinstall.SplitInstallManagerFactory;
import com.google.android.play.core.splitinstall.SplitInstallRequest;
import com.google.android.play.core.splitinstall.SplitInstallSessionState;
import com.google.android.play.core.splitinstall.SplitInstallStateUpdatedListener;
import com.google.android.play.core.splitinstall.model.SplitInstallSessionStatus;
import com.google.android.play.core.tasks.OnCompleteListener;
import com.google.android.play.core.tasks.OnFailureListener;
import com.google.android.play.core.tasks.OnSuccessListener;
import com.google.android.play.core.tasks.Task;


public class CommonDynamicLoaderActivity extends AppCompatActivity implements SplitInstallStateUpdatedListener, OnCompleteListener, OnFailureListener, OnSuccessListener {

    private static String TAG = "CommonDynamicLoaderActivity";
    public static String EXTRA_INIT_ACTIVITY = "EXTRA_INIT_ACTIVITY";
    public static String EXTRA_INIT_MODULE = "EXTRA_INIT_MODULE";
    private static boolean active = false;

    private LottieAnimationView mainLoaderView;
    private TextView txtProgress;
    private Context mContext;
    private Intent resultIntent;
    private SplitInstallManager mInstallManager;
    private String initActivity;
    private String initModule;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dynamic_loader);

        mainLoaderView = findViewById(R.id.mainLoaderView);
        txtProgress = findViewById(R.id.progress);
        mContext = CommonDynamicLoaderActivity.this;
        mInstallManager = SplitInstallManagerFactory.create(this);
        resultIntent = getIntent();
        initActivity = resultIntent.getStringExtra(EXTRA_INIT_ACTIVITY);
        initModule = resultIntent.getStringExtra(EXTRA_INIT_MODULE);

        //initiating module installation
        installModule();

    }

    private void installModule() {
        SplitInstallRequest request = SplitInstallRequest.newBuilder()
                .addModule(initModule)
                .build();
        mInstallManager.registerListener(this);
        mInstallManager.startInstall(request).addOnFailureListener(this).addOnSuccessListener(this).addOnCompleteListener(this);
    }

    @Override
    public void onStateUpdate(SplitInstallSessionState splitInstallSessionState) {
        Log.i(TAG, "onStateUpdate " + splitInstallSessionState.status());
        if (splitInstallSessionState.moduleNames().contains(initModule)) {
            switch (splitInstallSessionState.status()) {
                case SplitInstallSessionStatus.DOWNLOADING:
                    Log.i(TAG, "onStateUpdate initModule " + initModule + " downloading");
                    displayLoadingState(splitInstallSessionState, "Downloading ");
                    break;
                case SplitInstallSessionStatus.REQUIRES_USER_CONFIRMATION:
                    Log.i(TAG, "onStateUpdate initModule " + initModule + " REQUIRES_USER_CONFIRMATION");
                    try {
                        mContext.startIntentSender(splitInstallSessionState.resolutionIntent().getIntentSender(), null, 0, 0, 0);
                    } catch (IntentSender.SendIntentException e) {
                        e.printStackTrace();
                    }
                    break;
                case SplitInstallSessionStatus.INSTALLED:
                    Log.i(TAG, "onStateUpdate installed " + Build.VERSION.SDK_INT);
                    if (26 <= Build.VERSION.SDK_INT) {
                        SplitInstallHelper.updateAppInfo(mContext.getApplicationContext());
                    }

                    // installation done, hiding progress
                    if (active) {
                        startModuleActivity();
                        Log.i(TAG, "module activity started");
                    } else {
                        Log.i(TAG, "loader activity not active");

                    }
                    break;
                case SplitInstallSessionStatus.INSTALLING:
                    Log.i(TAG, "onStateUpdate initModule " + initModule + " installing");
                    displayLoadingState(splitInstallSessionState, "Downloading ");
                    break;
                case SplitInstallSessionStatus.FAILED:
                    Log.i(TAG, "onStateUpdate initModule " + initModule + " failed");
                    Log.e(TAG, "Error " + splitInstallSessionState.errorCode() + " for module ");
                    break;
            }
        }

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        mInstallManager.unregisterListener(this);
        finish();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            onBackPressed();
        }
        return super.onKeyDown(keyCode, event);
    }

    private void startModuleActivity() {
        mInstallManager.unregisterListener(this);
        hideProgress();
        // navigating to module init activity
        resultIntent.setClassName(BuildConfig.APPLICATION_ID, initActivity);
        startActivity(resultIntent);
        finish();
    }

    private void hideProgress() {
        AnimationFactory.stopWalletLoader(mainLoaderView);

    }

    private void displayLoadingState(SplitInstallSessionState state, String message) {
        AnimationFactory.startWalletLoader(mainLoaderView);
        int per = (int) (100 * state.bytesDownloaded() / state.totalBytesToDownload());
        Log.i(TAG, "displayLoadingState \n totalBytesToDownload" + state.totalBytesToDownload());
        String percentage = String.valueOf(per);
        txtProgress.setText(getString(R.string.dynamic_hoho_progress, percentage));
    }

    @Override
    public void onComplete(Task task) {
    }

    @Override
    public void onFailure(Exception e) {
        txtProgress.setText(getString(R.string.dynamic_hoho_progress_failed));
    }

    @Override
    public void onSuccess(Object o) {
    }

    @Override
    public void onStart() {
        super.onStart();
        active = true;
    }

    @Override
    public void onStop() {
        super.onStop();
        active = false;
    }
}
