package net.one97.paytm.weexsdk;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.taobao.weex.IWXRenderListener;
import com.taobao.weex.InitConfig;
import com.taobao.weex.WXSDKEngine;
import com.taobao.weex.WXSDKInstance;
import com.taobao.weex.common.WXRenderStrategy;
import com.taobao.weex.ui.view.WXEditText;
import com.taobao.weex.utils.WXFileUtils;

import java.util.HashMap;

public class WeexActivity extends AppCompatActivity implements IWXRenderListener {
    private WXSDKInstance mWXSDKInstance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i("POC", "WeexSdkInitActivity");

        setContentView(R.layout.weexsdk_activity_main);

        InitConfig config = new InitConfig.Builder()
                .build();
        WXSDKEngine.initialize(this.getApplication(), config);

        super.onCreate(savedInstanceState);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        initSDK(this);
        loadUrl("", this);
    }


    public void initSDK(Context context) {
        mWXSDKInstance = new WXSDKInstance(context);
        mWXSDKInstance.registerRenderListener(this);
    }

    public void loadUrl(String url, final Context context) {

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                HashMap<String, Object> map = new HashMap<>();
                Log.i("POC", "before render");
                mWXSDKInstance.render("pdp", WXFileUtils.loadFileOrAsset("test.js", context.getApplicationContext()), null, null, WXRenderStrategy.APPEND_ASYNC);
                Log.i("POC", "after render");

            }
        }, 3000);

    }

    private void initJSService() {

        HashMap<String, Object> options = new HashMap<>();
        String SERVICE_NAME = "COMMON_SERVICE";
        String SERVICE_JS_CODE = WXFileUtils.loadAsset("common-service.js", this);
        options.put("serviceName", SERVICE_NAME);
        WXSDKEngine.registerService(SERVICE_NAME, SERVICE_JS_CODE, options);

    }

    @Override
    public void onViewCreated(WXSDKInstance instance, View view) {
        Log.i("POC", "before onViewCreated");
        setContentView(view);
        Log.i("POC", "after onViewCreated");
    }


    @Override
    public void onRenderSuccess(WXSDKInstance instance, int width, int height) {
        Log.i("POC", "onRenderSuccess");
    }

    @Override
    public void onRefreshSuccess(WXSDKInstance instance, int width, int height) {
        Log.i("POC", "onRefreshSuccess");
    }

    @Override
    public void onException(WXSDKInstance instance, String errCode, String msg) {
        Log.i("POC", "onException" + "errCode " + errCode + " msg " + msg);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mWXSDKInstance != null) {
            mWXSDKInstance.onActivityResume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mWXSDKInstance != null) {
            mWXSDKInstance.onActivityPause();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mWXSDKInstance != null) {
            mWXSDKInstance.onActivityStop();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mWXSDKInstance != null) {
            mWXSDKInstance.onActivityDestroy();
        }
    }

}
