package com.test.dynamictest;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.google.android.play.core.splitcompat.SplitCompat;

public class DynamicDeliveryControlActivity extends AppCompatActivity {
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
                // TODO: 26/04/19 Save in preferences
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
}
