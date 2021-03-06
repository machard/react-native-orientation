package com.github.yamill.orientation;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.hardware.SensorManager;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.OrientationEventListener;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.facebook.react.bridge.LifecycleEventListener;

import java.util.HashMap;
import java.util.Map;

public class OrientationModule extends ReactContextBaseJavaModule {
    final private Activity mActivity;
    final private OrientationEventListener mOrientationListener;

    public OrientationModule(ReactApplicationContext reactContext, Activity activity) {
        super(reactContext);

        mActivity = activity;

        final ReactApplicationContext ctx = reactContext;

        mOrientationListener = new OrientationEventListener(reactContext, SensorManager.SENSOR_DELAY_NORMAL) {
            @Override
            public void onOrientationChanged(int orientation) {
                final String orientationValue = orientation == 0
                    ? "PORTRAIT"
                    : "LANDSCAPE";

                WritableMap params = Arguments.createMap();
                params.putString("orientation", orientationValue);

                ctx
                  .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                  .emit("orientationDidChange", params);
            }
        };

        LifecycleEventListener listener = new LifecycleEventListener() {
            @Override
            public void onHostResume() {
                if (mOrientationListener.canDetectOrientation() == true) {
                    mOrientationListener.enable();
                } else {
                    mOrientationListener.disable();
                }
            }

            @Override
            public void onHostPause() {
            }

            @Override
            public void onHostDestroy() {
            }
        };

        reactContext.addLifecycleEventListener(listener);
    }

    @Override
    public String getName() {
        return "Orientation";
    }

    @ReactMethod
    public void getOrientation(Callback callback) {
        final int orientation = getReactApplicationContext().getResources().getConfiguration().orientation;

        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            callback.invoke(null, "LANDSCAPE");
        } else if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            callback.invoke(null, "PORTRAIT");
        } else if (orientation == Configuration.ORIENTATION_UNDEFINED) {
            callback.invoke(null, "UNKNOWN");
        } else {
            callback.invoke(orientation, null);
        }
    }

    @ReactMethod
    public void lockToPortrait() {
      mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    @ReactMethod
    public void lockToLandscape() {
      mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
    }

    @ReactMethod
    public void unlockAllOrientations() {
      mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
    }
}



