package com.test.dynamictest;

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
import android.widget.Toast;

import com.google.android.play.core.splitcompat.SplitCompat;

import java.util.HashSet;

public class DynamicDeliveryControlActivity extends AppCompatActivity implements DynamicModuleInstaller.Listener {
    private static final String TAG = "PlayCore";
    private ModulesAdapter modulesAdapter;
    private DynamicModuleInstaller dynamicModuleInstaller;
    private boolean isDefferedInstallEnabled;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dynamic_delivery_control);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("DD Modules Controller");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        RecyclerView recyclerView = findViewById(R.id.rv_modules);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(llm);

        dynamicModuleInstaller = new DynamicModuleInstaller(this);
        modulesAdapter = new ModulesAdapter(this, dynamicModuleInstaller.getModulesArrayList(), new ModulesAdapter.ItemClickListener() {
            @Override
            public void onCheckedChangeListener(boolean isChecked, String moduleName) {
                if (isChecked) {
                    if (isDefferedInstallEnabled){
                        dynamicModuleInstaller.deferredInstall(moduleName);
                    } else {
                        dynamicModuleInstaller.startInstall(moduleName);
                    }
                } else {
                    dynamicModuleInstaller.deferredUninstall(moduleName);
                }
            }
        });
        recyclerView.setAdapter(modulesAdapter);

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
                if (isDefferedInstallEnabled){
                    dynamicModuleInstaller.deferredInstallAll();
                } else {
                    dynamicModuleInstaller.startInstallAll();
                }
            }
        });

        findViewById(R.id.btn_refresh_status).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                modulesAdapter.setNewData(dynamicModuleInstaller.getModulesArrayList());
            }
        });

        findViewById(R.id.btn_toggle_uninstall_all).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dynamicModuleInstaller.deferredUninstallAll();
            }
        });
    }

    private void toastAndLog(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
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
        dynamicModuleInstaller.unRegisterListener();
    }

    @Override
    protected void onResume() {
        super.onResume();
        dynamicModuleInstaller.registerListener(this);
    }

    @Override
    public void onDownloading(int downloadedPercentage) {
        toastAndLog("Downloading... " + downloadedPercentage + "%");
    }

    @Override
    public void onInstalled(HashSet<String> modules) {
        toastAndLog("Installed: " + modules.toString());
    }

    @Override
    public void onFailed(int splitInstallErrorCode) {
        toastAndLog("Failed: " + splitInstallErrorCode);
    }

    @Override
    public void onCancelled() {
        toastAndLog("onCancelled");
    }
}
