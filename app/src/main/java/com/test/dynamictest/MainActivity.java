package com.test.dynamictest;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import static com.test.dynamictest.CommonDynamicLoaderActivity.EXTRA_INIT_ACTIVITY;
import static com.test.dynamictest.CommonDynamicLoaderActivity.EXTRA_INIT_MODULE;

public class MainActivity extends AppCompatActivity {

    private static String TAG = "InstallDynamicModule";

    private static String DYNAMIC_MODULE_ACTIVITY = "com.test.dynamic_feature.DynamicInitActivity";
    private static String DYNAMIC_MODULE_NAME = "dynamic_feature";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnTap = findViewById(R.id.btn_tap);
        btnTap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "button clicked");
                DynamicModulesDownloadManager.getInstance(MainActivity.this).loadAndLaunchModule(MainActivity.this, DYNAMIC_MODULE_NAME, DYNAMIC_MODULE_ACTIVITY);
            }
        });

        findViewById(R.id.btn_launch_download_modules_controller).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, DynamicDeliveryControlActivity.class);
                startActivity(intent);
            }
        });
    }
}
