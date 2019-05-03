package com.test.dynamictest;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.BatteryManager;
import android.os.Environment;
import android.os.StatFs;
import android.telephony.TelephonyManager;
import android.util.Log;

/**
 * Created by prankul.garg on 2019-05-03.
 */
public class DynamicModuleUtils {
    private static final String TAG = "PlayCore-DFMUtils";

    public static long getAvailableInternalMemorySizeInMB() {
        StatFs stat = new StatFs(Environment.getDataDirectory().getPath());
        long blockSize = stat.getBlockSizeLong();
        long availableBlocks = stat.getAvailableBlocksLong();
        long sizeInMB = (availableBlocks * blockSize) / (1024 * 1024);
        Log.i(TAG, "AvailableInternalMemorySizeInMB: " + sizeInMB);
        return sizeInMB;
    }

    public static int getBatteryPercentage(Context context) {
        IntentFilter iFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = context.registerReceiver(null, iFilter);

        int level = batteryStatus != null ? batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) : -1;
        int scale = batteryStatus != null ? batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1) : -1;
        double batteryPct = (double) level / (double) scale;
        int batteryPercent = (int) (batteryPct * 100D);
        Log.i(TAG, "BatteryPercentage: " + batteryPercent);
        return batteryPercent;
    }

    public static boolean isBatteryCharging(Context context) {
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = context.registerReceiver(null, ifilter);

        int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL;
        Log.i(TAG, "isCharging: " + isCharging);
        return isCharging;
    }

    public static boolean isFastInternetConnection(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = connectivityManager.getActiveNetworkInfo();
        boolean isFastInternet = false;
        if (info != null && info.isConnected()) {
            if (info.getType() == ConnectivityManager.TYPE_WIFI) {
                isFastInternet = true;
            } else if (info.getType() == ConnectivityManager.TYPE_MOBILE) {
                switch (info.getSubtype()) {
                    case TelephonyManager.NETWORK_TYPE_HSDPA:// ~ 2-14 Mbps
                    case TelephonyManager.NETWORK_TYPE_HSUPA:// ~ 1-23 Mbps
                    case TelephonyManager.NETWORK_TYPE_EHRPD:// ~ 1-2 Mbps // API level 11
                    case TelephonyManager.NETWORK_TYPE_EVDO_B:// ~ 5 Mbps // API level 9
                    case TelephonyManager.NETWORK_TYPE_HSPAP:// ~ 10-20 Mbps // API level 13
                    case TelephonyManager.NETWORK_TYPE_LTE:// ~ 10+ Mbps // API level 11
                        isFastInternet = true;
                        break;

                    default:
                        isFastInternet = false;
                        break;
                }
            }
        }
        Log.i(TAG, "isFastInternet: " + isFastInternet);
        return isFastInternet;
    }
}
