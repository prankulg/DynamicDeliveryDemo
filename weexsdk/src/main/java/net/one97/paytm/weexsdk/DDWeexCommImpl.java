package net.one97.paytm.weexsdk;

import android.app.Activity;
import android.content.Context;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.test.dynamictest.weexsdk.DDWeexCommunicator;
import com.test.dynamictest.weexsdk.WeexDeeplinkHandler;

import java.lang.ref.WeakReference;

public class DDWeexCommImpl implements DDWeexCommunicator {

    public static final String TAG = "DDWeexCommImpl";
    private static DDWeexCommImpl ourInstance = null;

    static {
        Log.i(TAG, " start of static block ");
        ourInstance = new DDWeexCommImpl();
        WeexDeeplinkHandler.getInstance().registerWeexsdkListener(ourInstance);
        Log.i(TAG, " -- weex sdk communicator is registered successfully -- ");
    }


    public static DDWeexCommImpl getInstance() {
        return ourInstance;
    }


    @Override
    public void sendMessageToDDF(String message) {

    }

    @Override
    public void sendMessageToDDF(String message, Context context) {
        Toast.makeText(context, "sendMessageToDDF : " + message, Toast.LENGTH_SHORT).show();

        Log.i(TAG, " sendMessageToDDF " + " toast displayed " + message);
    }

    @Override
    public void sendViewToDDF(View view) {

    }

    @Override
    public void sendActivityInstance(WeakReference<Activity> activity) {
        Toast.makeText(activity.get(), "DDWeexCommImpl : " + activity.get().getClass().getCanonicalName(), Toast.LENGTH_SHORT).show();

        Log.i(TAG, " sendActivityInstance " + " toast displayed " + activity.get().getClass().getCanonicalName());
    }

    @Override
    public void sendViewToDDF(View view, WeakReference<Activity> activity) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity.get());
        // Set the custom layout as alert dialog view
        builder.setTitle("Alert");
        builder.setView(view);
        AlertDialog dialog = builder.create();
        dialog.show();

        Log.i(TAG, " sendViewToDDF " + " dialog displayed");
    }
}
