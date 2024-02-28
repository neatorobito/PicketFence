package fyi.karm.picketfence;

import android.content.Context;
import android.util.Log;

import com.getcapacitor.JSObject;

import java.util.ArrayList;

import fyi.karm.perimeter.PerimeterReceiver;

public class SimpleGeofenceReceiver extends PerimeterReceiver {
    @Override
    public void onFenceTriggered(Context context, ArrayList<JSObject> triggeredJSFences, long triggerTime, int transitionType) {

    }

    @Override
    public void onError(Context context, int errorCode, String errorMessage) {

    }
}
