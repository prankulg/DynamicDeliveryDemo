package com.test.dynamictest.weexsdk;

import android.app.Activity;
import android.content.Context;
import android.view.View;

import com.test.dynamictest.DDCommunicator;

import java.lang.ref.WeakReference;

public interface DDWeexCommunicator extends DDCommunicator {
    void sendMessageToDDF(String message);
    void sendMessageToDDF(String message, Context context);
    void sendViewToDDF(View view);
    void sendActivityInstance(WeakReference<Activity> activity);
    void sendViewToDDF(View view,WeakReference<Activity> activity);
}
