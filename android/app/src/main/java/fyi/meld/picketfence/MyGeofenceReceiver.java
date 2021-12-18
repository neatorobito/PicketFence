package fyi.meld.picketfence;

import android.util.Log;

import com.getcapacitor.JSObject;

import java.util.ArrayList;

import fyi.meld.perimeter.PerimeterReceiver;

public class MyGeofenceReceiver extends PerimeterReceiver {
    @Override
    public void onEntrance(ArrayList<JSObject> triggeredJSFences, long triggerTime) {

        String entranceMessage = "";

        if(triggeredJSFences.size() == 1)
        {
            JSObject fence = triggeredJSFences.get(0);
            String fenceName = "Looks like you're near " + fence.getString("fenceName");
            String extraData = fence.getString("interests");
            entranceMessage = "Be sure to check out " + extraData + " near " + fenceName;
        }
        else if(triggeredJSFences.size() > 1)
        {
            String listOfFences = "";

            for(JSObject triggeredFence : triggeredJSFences)
            {
                listOfFences += triggeredFence.getString("fenceName") + ",";
            }

            entranceMessage = "We're reminding you near " + listOfFences + ". There's lots going on. Tap to find out more.";
        }

        Log.d("MyReceiver", entranceMessage);

    }

    @Override
    public void onExit(ArrayList<JSObject> triggeredJSFences, long triggerTime) {

        String exitMessage = "";

        if(triggeredJSFences.size() == 1)
        {
            JSObject fence = (JSObject) triggeredJSFences.get(0);
            //TODO Refactor : These strings should be defined in Constants.
            exitMessage = "Looks like you've left " + fence.getString("fenceName");
            exitMessage += "!";
        }
        else if(triggeredJSFences.size() > 1)
        {
            exitMessage = "Looks like you're no longer near a few different buildings: ";
            String listOfFences = "";

            for(JSObject triggered : triggeredJSFences)
            {
                listOfFences += triggered.getString("fenceName") + ",";
            }

            exitMessage += (" at " + listOfFences + ".");
        }

        Log.d("MyReceiver", exitMessage);
    }
}
