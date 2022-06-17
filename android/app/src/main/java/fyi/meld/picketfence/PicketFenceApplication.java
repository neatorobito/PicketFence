package fyi.meld.picketfence;

import android.app.Application;

import fyi.meld.perimeter.PerimeterApplicationHooks;
import fyi.meld.perimeter.PerimeterReceiver;

public class PicketFenceApplication extends Application implements PerimeterApplicationHooks {

    @Override
    public Class<? extends PerimeterReceiver> GetCustomReceiverClass() {
        return MyGeofenceReceiver.class;
    }
}
