package com.test.dynamictest.test;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import com.google.android.play.core.splitcompat.SplitCompat;
import com.test.dynamictest.DynamicModuleHelper;
import com.test.dynamictest.DynamicModuleManager;
import com.test.dynamictest.R;

import java.util.ArrayList;

public class DynamicModuleTestActivity extends AppCompatActivity implements DynamicModuleManager.Listener {
    private static final String TAG = "PlayCore";
    private DynamicModulesAdapter dynamicModulesAdapter;
    private DynamicModuleManager dynamicModuleManager;
    private boolean isDefferedInstallEnabled;
    private TextView tvStatus;
    private ArrayList<DynamicModuleItem> modulesItemArrayList;
    private ArrayList<String> modulesStringArrayList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dynamic_module_test);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("DD Modules Controller");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        RecyclerView recyclerView = findViewById(R.id.rv_modules);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(llm);
        tvStatus = findViewById(R.id.tv_status);

        modulesItemArrayList = getModulesArrayList();
        modulesStringArrayList = new ArrayList<>();
        for (DynamicModuleItem dynamicModuleItem : modulesItemArrayList) {
            modulesStringArrayList.add(dynamicModuleItem.getName());
        }

        dynamicModuleManager = DynamicModuleManager.getInstance();
        dynamicModulesAdapter = new DynamicModulesAdapter(this, modulesItemArrayList, new DynamicModulesAdapter.ItemClickListener() {
            @Override
            public void onCheckedChangeListener(boolean isChecked, String moduleName) {
                if (isChecked) {
                    if (isDefferedInstallEnabled) {
                        dynamicModuleManager.deferredInstall(moduleName);
                    } else {
                        tvStatus.setText("Status");
                        dynamicModuleManager.registerListener(DynamicModuleTestActivity.this, moduleName);
                        dynamicModuleManager.startInstall(moduleName);
                    }
                } else {
                    dynamicModuleManager.deferredUninstall(moduleName);
                }
            }
        });
        recyclerView.setAdapter(dynamicModulesAdapter);

        Switch toggleDeferredInstall = findViewById(R.id.sw_toggle_deffered_install);
        toggleDeferredInstall.setChecked(isDefferedInstallEnabled);
        toggleDeferredInstall.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                isDefferedInstallEnabled = isChecked;
            }
        });

        findViewById(R.id.btn_toggle_install_all).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isDefferedInstallEnabled) {
                    dynamicModuleManager.deferredInstall(modulesStringArrayList);
                } else {
                    tvStatus.setText("Starting install without client update");
                    dynamicModuleManager.startInstall(modulesStringArrayList);
                }
            }
        });

        findViewById(R.id.btn_toggle_install_multiple).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<String> arrayList = new ArrayList<>();
                ArrayList<DynamicModuleItem> dynamicModuleItemArrayList = modulesItemArrayList;

                for (int i = 0; i < dynamicModuleItemArrayList.size(); i++) {
                    arrayList.add(dynamicModuleItemArrayList.get(i).getName());

                    if (arrayList.size() == 2) {
                        break;
                    }
                }

                if (isDefferedInstallEnabled) {
                    dynamicModuleManager.deferredInstall(arrayList);
                } else {
                    tvStatus.setText("Starting install without client update");
                    dynamicModuleManager.startInstall(arrayList);
                }
            }
        });

        findViewById(R.id.btn_refresh_status).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tvStatus.setText("Status");
                modulesItemArrayList = getModulesArrayList();
                dynamicModulesAdapter.setNewData(modulesItemArrayList);
            }
        });
    }

    public static ArrayList<DynamicModuleItem> getModulesArrayList() {
        ArrayList<String> modulesStringArrayList = DynamicModuleHelper.getAllDynamicModulesList();
        ArrayList<DynamicModuleItem> modulesArrayList = new ArrayList<>();
        for (String moduleName: modulesStringArrayList) {
            DynamicModuleItem dynamicModuleItem = new DynamicModuleItem(moduleName);
            dynamicModuleItem.setInstalled(DynamicModuleManager.getInstance().isInstalled(moduleName));
            modulesArrayList.add(dynamicModuleItem);
        }
        return modulesArrayList;
    }

    private void toastAndLog(String message) {
        tvStatus.setText(message);
        log(message);
    }

    private void log(String message) {
        Log.i(TAG, message);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(newBase);
        SplitCompat.install(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        dynamicModuleManager.unRegisterListener();
    }

    @Override
    public void onDownloading(int downloadedPercentage) {
        toastAndLog("onDownloading: " + downloadedPercentage + "%");
    }

    @Override
    public void onDownloaded() {
        toastAndLog("onDownloaded");
    }

    @Override
    public void onInstalling() {
        toastAndLog("onInstalling");
    }

    @Override
    public void onInstalled() {
        toastAndLog("onInstalled");
    }

    @Override
    public void onCancelling() {
        toastAndLog("onCancelling");
    }

    @Override
    public void onCancelled() {
        toastAndLog("onCancelled");
    }

    @Override
    public void onFailed() {
        toastAndLog("onFailed");
    }

    @Override
    public void onRequestSuccess() {
        toastAndLog("onRequestSuccess");
    }

    @Override
    public void onAlreadyActiveSession(String currentDownloadingModuleName) {
        toastAndLog("onAlreadyActiveSession: " + currentDownloadingModuleName);
    }

    @Override
    public void onRequestFailed(int splitInstallErrorCode) {
        toastAndLog("onRequestFailed: " + splitInstallErrorCode);
    }

    @Override
    public void onNetworkError() {
        toastAndLog("onNetworkError");
    }

    @Override
    public void onInsufficientStorage() {
        toastAndLog("onInsufficientStorage");
    }
}
