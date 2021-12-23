package fyi.meld.picketfence;

import android.app.Application;

import fyi.meld.perimeter.PerimeterApplication;
import fyi.meld.perimeter.PerimeterReceiver;

public class PicketFenceApplication extends Application implements PerimeterApplication {
    @Override
    public Class<? extends PerimeterReceiver> GetGeofenceReceiverClass() {
        return MyGeofenceReceiver.class;
    }
}
