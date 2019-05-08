package net.one97.paytm.weexsdk;

import android.content.Context;
import android.util.Log;

import com.google.android.play.core.splitinstall.SplitInstallHelper;
import com.taobao.weex.WXEnvironment;
import com.taobao.weex.adapter.IWXSoLoaderAdapter;

import java.io.File;

public class SoLoader implements IWXSoLoaderAdapter {

    Context mContex;

    SoLoader(Context context) {
        mContex = context;
    }

    @Override
    public void doLoadLibrary(String shortName) {
        try {
            Log.i("POC", "before getFileCount  shortName : " + shortName);
            getFileCount();
            Log.i("POC", "before doLoadLibrary  shortName : " + shortName);
            SplitInstallHelper.loadLibrary(mContex, shortName);
            Log.i("POC", "after doLoadLibrary: shortName " + shortName);
        } catch (Exception ex) {
            Log.e("POC", "Exception:" + ex.getMessage());
            ex.printStackTrace();
        }


    }

    @Override
    public void doLoad(String name) {
        Log.i("POC", "doLoad " + name);

    }

    private void getFileCount() {
        Log.i("POC", "getFileCount  called ");
        String pkgName = WXEnvironment.getApplication().getPackageName();

        String path = "/data/data/" + pkgName + "/lib";
        Log.i("POC", "getFileCount  File pathe: " + path);

        File folder = new File(path);
        File[] listOfFiles = folder.listFiles();
        if (listOfFiles != null) {
            Log.i("POC", "SO File count:" + listOfFiles.length);

            for (int i = 0; i < listOfFiles.length; i++) {
                if (listOfFiles[i].isFile()) {
                    Log.i("POC", "SO File:" + listOfFiles[i].getName() + "  Size:" + (listOfFiles[i].length() / 1024) + " kb");
                }
            }
        } else {
            Log.i("POC", "SO File count: is null");
        }

    }
}
