package fyi.karm.picketfence;

import android.app.Application;

import fyi.karm.perimeter.PerimeterApplicationHooks;
import fyi.karm.perimeter.PerimeterReceiver;

public class PicketFenceApplication extends Application implements PerimeterApplicationHooks {

    @Override
    public Class<? extends PerimeterReceiver> GetCustomReceiverClass() {
        return MyGeofenceReceiver.class;
    }
}
