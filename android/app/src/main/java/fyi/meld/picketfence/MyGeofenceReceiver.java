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
            JSObject event = triggeredJSFences.get(0);
            String fenceName = "Looks like you're near " + event.fence.getString("name");
            String extraData = event.fence.getString("interests");
            entranceMessage = "Be sure to check out " + extraData + " near " + fenceName;
        }
        else if(triggeredJSFences.size() > 1)
        {
            String listOfFences = "";

            for(JSObject triggeredFence : triggeredJSFences)
            {
                listOfFences += triggeredFence.getString("name") + ",";
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
            JSObject event = (JSObject) triggeredJSFences.get(0);
            //TODO Refactor : These strings should be defined in Constants.
            exitMessage = "Looks like you've left " + event.fence.getString("name");
            exitMessage += "!";
        }
        else if(triggeredJSFences.size() > 1)
        {
            exitMessage = "Looks like you're no longer near a few different buildings: ";
            String listOfFences = "";

            for(JSObject event : triggeredJSFences)
            {
                listOfFences += event.fence.getString("name") + ",";
            }

            exitMessage += (" at " + listOfFences + ".");
        }

        Log.d("MyReceiver", exitMessage);
    }

    @Override
    public void onError(int errorCode, String errorMessage) {
        JSObject errorInfo = new JSObject();
        errorInfo.put("message", errorMessage);
        errorInfo.put("code", errorCode);
    }
}
