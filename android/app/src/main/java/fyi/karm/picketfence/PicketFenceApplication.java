package fyi.karm.picketfence;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import fyi.karm.perimeter.PerimeterApplicationHooks;
import fyi.karm.perimeter.PerimeterReceiver;

public class PicketFenceApplication extends Application implements PerimeterApplicationHooks {

    @Override
    public Class<? extends PerimeterReceiver> GetGeoFenceReceiverClass() {
        return SimpleGeofenceReceiver.class;
    }

}
