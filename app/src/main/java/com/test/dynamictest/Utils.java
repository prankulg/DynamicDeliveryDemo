package com.test.dynamictest;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by prankul.garg on 2019-05-03.
 */
public class Utils {
    public static boolean isNetworkAvailable(Context context) {
        if (context == null) return false;
        ConnectivityManager ConnectMgr = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (ConnectMgr == null)
            return false;
        NetworkInfo NetInfo = ConnectMgr.getActiveNetworkInfo();
        if (NetInfo == null)
            return false;

        return NetInfo.isConnected();
    }
}
