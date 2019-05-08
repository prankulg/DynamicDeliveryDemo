package com.test.dynamictest;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.test.dynamictest.test.DynamicModuleTestActivity;
import com.test.dynamictest.weexsdk.WeexDeeplinkHandler;

import java.io.File;
import java.lang.ref.WeakReference;

public class MainActivity extends AppCompatActivity {

    private static String TAG = "InstallDynamicModule";

    private static String DYNAMIC_MODULE_ACTIVITY = "com.test.dynamic_feature.DynamicInitActivity";
    private static String DYNAMIC_MODULE_WEEX_ACTIVITY = "net.one97.paytm.weexsdk.WeexActivity";
    private static String DYNAMIC_MODULE_NESTED_ACTIVITY = "net.one97.paytm.dynamic.nested.NestedActivity";

    private static String DYNAMIC_MODULE_NAME = "dynamic_feature";
    private static String DYNAMIC_MODULE_WEEX_NAME = "weexsdk";

    private static String DYNAMIC_MODULE_PICASO = "dynamic_picaso";
    private static String DYNAMIC_MODULE_NESTED = "dynamic_nested";

    private TextView txtViewSoFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtViewSoFile = (TextView) findViewById(R.id.txt_so_file);

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
                if (WeexDeeplinkHandler.getInstance().isInstalled(MainActivity.this, DYNAMIC_MODULE_WEEX_NAME)) {
                    Button btnShow = new Button(MainActivity.this);
                    btnShow.setText("Button from base app");
                    WeexDeeplinkHandler.getInstance().sendSingletonViewToDDF(btnShow, new WeakReference<Activity>(MainActivity.this));
                } else {
                    WeexDeeplinkHandler.getInstance().loadAndLaunchModule(MainActivity.this, DYNAMIC_MODULE_WEEX_NAME, DYNAMIC_MODULE_WEEX_ACTIVITY);
                }
            }
        });

        findViewById(R.id.btn_listener_2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, " onClick Toast display");
                if (WeexDeeplinkHandler.getInstance().isInstalled(MainActivity.this, DYNAMIC_MODULE_WEEX_NAME)) {
                    WeexDeeplinkHandler.getInstance().sendSingletonMessageToDDF("Hey weexsdk DD i am from base app", MainActivity.this);
                } else {
                    WeexDeeplinkHandler.getInstance().loadAndLaunchModule(MainActivity.this, DYNAMIC_MODULE_WEEX_NAME, DYNAMIC_MODULE_WEEX_ACTIVITY);
                }
            }
        });

        findViewById(R.id.btn_listener_3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, " onClick Activity passing ");
                if (WeexDeeplinkHandler.getInstance().isInstalled(MainActivity.this, DYNAMIC_MODULE_WEEX_NAME)) {
                    WeexDeeplinkHandler.getInstance().sendSingletonActivityInstance(new WeakReference<Activity>(MainActivity.this));
                } else {
                    WeexDeeplinkHandler.getInstance().loadAndLaunchModule(MainActivity.this, DYNAMIC_MODULE_WEEX_NAME, DYNAMIC_MODULE_WEEX_ACTIVITY);
                }
            }
        });

        findViewById(R.id.picso_dd).setOnClickListener(v -> {
            Log.i(TAG, " onClick picso_dd ");
            if (WeexDeeplinkHandler.getInstance().isInstalled(MainActivity.this, DYNAMIC_MODULE_PICASO)) {
                Toast.makeText(MainActivity.this, DYNAMIC_MODULE_PICASO + "is available", Toast.LENGTH_SHORT).show();
            } else {
                WeexDeeplinkHandler.getInstance().loadModule(MainActivity.this, DYNAMIC_MODULE_PICASO);
            }
        });

        findViewById(R.id.nested_dd).setOnClickListener(v -> {
            Log.i(TAG, " onClick nested_dd ");
            if (WeexDeeplinkHandler.getInstance().isInstalled(MainActivity.this, DYNAMIC_MODULE_PICASO)) {
                WeexDeeplinkHandler.getInstance().loadAndLaunchModule(MainActivity.this, DYNAMIC_MODULE_NESTED, DYNAMIC_MODULE_NESTED_ACTIVITY);
            } else {
                WeexDeeplinkHandler.getInstance().loadModule(MainActivity.this, DYNAMIC_MODULE_PICASO);
            }
        });
        getFileCount();
    }


    private void getFileCount() {
        String pkgName = getApplicationContext().getPackageName();

        String path = "/data/data/" + pkgName + "/lib";

        File folder = new File(path);
        File[] listOfFiles = folder.listFiles();
        if (listOfFiles != null) {
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("SO File:");

            for (int i = 0; i < listOfFiles.length; i++) {
                if (listOfFiles[i].isFile()) {
                    stringBuffer.append("\n" + listOfFiles[i].getName()+"  Size:"+(listOfFiles[i].length()/ 1024)+" kb");
                }
            }
            txtViewSoFile.setText(stringBuffer);
        } else {
            txtViewSoFile.setText("SO File count: is null");
        }

    }
}
