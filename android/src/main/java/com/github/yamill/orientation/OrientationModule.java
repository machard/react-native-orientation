package com.github.yamill.orientation;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.hardware.SensorManager;
import android.util.Log;
import android.view.OrientationEventListener;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;

public class OrientationModule extends ReactContextBaseJavaModule implements LifecycleEventListener {
    final private Activity mActivity;
    private OrientationEventListener mOrientationEventListener;
    private String mLastOrientation;
    private final ReactContext mReactContext;

    private String TAG = "Orientation";

    public OrientationModule(ReactApplicationContext reactContext, final Activity activity) {
        super(reactContext);

        mActivity = activity;
        mReactContext = reactContext;

        final ReactApplicationContext ctx = reactContext;

        // we use this technic instead of Configuration changes because
        // we want to listen to orientation changes even when the screen
        // is locked in a particuliar position.
        mOrientationEventListener = new OrientationEventListener(reactContext, SensorManager.SENSOR_DELAY_NORMAL) {
            @Override
            public void onOrientationChanged(int orientation) {
                Log.d(TAG, String.valueOf(orientation));

                String newOrientation;

                if (350 < orientation || orientation < 10) {
                    newOrientation = "PORTRAIT";
                } else if (260 < orientation && orientation < 280) {
                    newOrientation = "LANDSCAPELEFT";
                } else if (170 < orientation && orientation < 190) {
                    newOrientation = "PORTRAITUPSIDEDOWN";
                } else if (80 < orientation && orientation < 100) {
                    newOrientation = "LANDSCAPERIGHT";
                } else {
                    newOrientation = "PORTRAIT";
                }

                if (!newOrientation.equals(mLastOrientation)) {
                    mLastOrientation = newOrientation;

                    WritableMap params = Arguments.createMap();
                    params.putString("orientation", mLastOrientation);

                    if (mReactContext.hasActiveCatalystInstance()) {
                        mReactContext
                                .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                                .emit("orientationDidChange", params);
                    }
                }
            }
        };

        mOrientationEventListener.enable();

    }

    @Override
    public String getName() {
        return "Orientation";
    }

    @Override
    public void onHostResume() {
        mOrientationEventListener.enable();
    }

    @Override
    public void onHostPause() {
        mOrientationEventListener.disable();
    }

    @Override
    public void onHostDestroy() {
        mOrientationEventListener.disable();
    }

    @ReactMethod
    public void getOrientation(Callback callback) {
        callback.invoke(mLastOrientation, null);
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
