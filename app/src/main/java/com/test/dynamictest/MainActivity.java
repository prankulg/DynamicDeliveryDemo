package com.test.dynamictest;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.test.dynamictest.test.DynamicModuleTestActivity;
import com.test.dynamictest.test.DynamicModuleHelper;
import com.test.dynamictest.weexsdk.WeexDeeplinkHandler;

import java.lang.ref.WeakReference;

public class MainActivity extends AppCompatActivity {

    private static String TAG = "InstallDynamicModule";

    private static String DYNAMIC_MODULE_ACTIVITY = "com.test.dynamic_feature.DynamicInitActivity";
    private static String DYNAMIC_MODULE_WEEX_ACTIVITY = "net.one97.paytm.weexsdk.WeexActivity";

    private static String DYNAMIC_MODULE_NAME = "dynamic_feature";
    private static String DYNAMIC_MODULE_WEEX_NAME = "weexsdk";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.btn_launch_download_modules_controller).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, DynamicModuleTestActivity.class);
                startActivity(intent);
            }
        });

        Button btnTap = findViewById(R.id.btn_tap);
        btnTap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "button clicked");
                DynamicModuleHelper.loadAndLaunchModule(MainActivity.this, DYNAMIC_MODULE_NAME, DYNAMIC_MODULE_ACTIVITY);
            }
        });

        findViewById(R.id.btn_weex).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "button clicked");
                DynamicModuleHelper.loadAndLaunchModule(MainActivity.this, DYNAMIC_MODULE_WEEX_NAME, DYNAMIC_MODULE_WEEX_ACTIVITY);
            }
        });

        findViewById(R.id.btn_listener_1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Log.i(TAG, " onClick Display dialog");
                if (WeexDeeplinkHandler.getInstance().isInstalled(MainActivity.this,DYNAMIC_MODULE_WEEX_NAME)){
                    Button btnShow = new Button(MainActivity.this);
                    btnShow.setText("Button from base app");
                    WeexDeeplinkHandler.getInstance().sendSingletonViewToDDF(btnShow, new WeakReference<Activity>(MainActivity.this));
                }else{
                    WeexDeeplinkHandler.getInstance().loadAndLaunchModule(MainActivity.this, DYNAMIC_MODULE_WEEX_NAME, DYNAMIC_MODULE_WEEX_ACTIVITY);
                }
            }
        });

        findViewById(R.id.btn_listener_2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, " onClick Toast display");
                if (WeexDeeplinkHandler.getInstance().isInstalled(MainActivity.this,DYNAMIC_MODULE_WEEX_NAME)){
                    WeexDeeplinkHandler.getInstance().sendSingletonMessageToDDF("Hey weexsdk DD i am from base app", MainActivity.this);
                }else{
                    WeexDeeplinkHandler.getInstance().loadAndLaunchModule(MainActivity.this, DYNAMIC_MODULE_WEEX_NAME, DYNAMIC_MODULE_WEEX_ACTIVITY);
                }
            }
        });

        findViewById(R.id.btn_listener_3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, " onClick Activity passing ");
                if (WeexDeeplinkHandler.getInstance().isInstalled(MainActivity.this,DYNAMIC_MODULE_WEEX_NAME)){
                    WeexDeeplinkHandler.getInstance().sendSingletonActivityInstance(new WeakReference<Activity>(MainActivity.this));
                }else{
                    WeexDeeplinkHandler.getInstance().loadAndLaunchModule(MainActivity.this, DYNAMIC_MODULE_WEEX_NAME, DYNAMIC_MODULE_WEEX_ACTIVITY);
                }
            }
        });
    }
}
