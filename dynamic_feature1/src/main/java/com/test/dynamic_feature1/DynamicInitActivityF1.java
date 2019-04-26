package com.test.dynamic_feature1;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.android.play.core.splitcompat.SplitCompat;

public class DynamicInitActivityF1 extends AppCompatActivity {

    public static final String TAG = "InstallDynamicModule";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dynamic_init1);

        Log.i(TAG, "inside DynamicInitActivity");
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(newBase);
        SplitCompat.install(this);
    }
}
