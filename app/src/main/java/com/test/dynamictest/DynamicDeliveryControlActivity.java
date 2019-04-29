package com.test.dynamictest;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
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

import java.io.Serializable;
import java.util.HashSet;

public class DynamicDeliveryControlActivity extends AppCompatActivity {
    private static final String TAG = "PlayCore";
    private ModulesAdapter modulesAdapter;

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
        modulesAdapter = new ModulesAdapter(this, DynamicModulesDownloadManager.getInstance(this).getModulesArrayList());
        recyclerView.setAdapter(modulesAdapter);

        Switch toggleDeferredInstall = findViewById(R.id.sw_toggle_deffered_install);
        toggleDeferredInstall.setChecked(DynamicModulesDownloadManager.getInstance(DynamicDeliveryControlActivity.this).isDefferedInstallEnabled());
        toggleDeferredInstall.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                DynamicModulesDownloadManager.getInstance(DynamicDeliveryControlActivity.this).setDefferedInstallEnabled(isChecked);
            }
        });

        findViewById(R.id.btn_toggle_install_all).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DynamicModulesDownloadManager.getInstance(DynamicDeliveryControlActivity.this).installAllModules();
            }
        });

        findViewById(R.id.btn_refresh_status).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                modulesAdapter.setNewData(DynamicModulesDownloadManager.getInstance(DynamicDeliveryControlActivity.this).getModulesArrayList());
            }
        });

        findViewById(R.id.btn_toggle_uninstall_all).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DynamicModulesDownloadManager.getInstance(DynamicDeliveryControlActivity.this).unInstallAllModules();
            }
        });
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
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, new IntentFilter(DynamicModulesDownloadManager.INTENT_ACTION_DFM_MODULE_INSTALLED));
    }

    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String status = intent.getStringExtra(DynamicModulesDownloadManager.EXTRA_MODULE_STATUS);
            toastAndLog("Broadcast listened to calling activity: Status: " + status);

            Serializable serializable = intent.getSerializableExtra(DynamicModulesDownloadManager.EXTRA_MODULE_NAMES);
            if (serializable instanceof HashSet){
                HashSet<String> modules = (HashSet<String>) serializable;
                toastAndLog(modules.toString());
            }
        }
    };

    private void toastAndLog(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        log(message);
    }

    private void log(String message) {
        Log.i(TAG, message);
    }
}
