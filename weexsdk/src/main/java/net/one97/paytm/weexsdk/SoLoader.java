package net.one97.paytm.weexsdk;

import android.content.Context;
import android.util.Log;

import com.google.android.play.core.splitinstall.SplitInstallHelper;
import com.taobao.weex.WXEnvironment;
import com.taobao.weex.adapter.IWXSoLoaderAdapter;

public class SoLoader implements IWXSoLoaderAdapter {

    Context mContex;

    SoLoader(Context context) {
        mContex = context;
    }

    @Override
    public void doLoadLibrary(String shortName) {
        String pkgName = WXEnvironment.getApplication().getPackageName();
        String name = "/data/data/" + pkgName + "/lib/" + shortName;


        Log.i("POC", "before doLoadLibrary  shortName : " + shortName);
        SplitInstallHelper.loadLibrary(mContex, shortName);
        Log.i("POC", "after doLoadLibrary: shortName " + shortName);

    }

    @Override
    public void doLoad(String name) {
        Log.i("POC", "doLoad " + name);

    }
}
