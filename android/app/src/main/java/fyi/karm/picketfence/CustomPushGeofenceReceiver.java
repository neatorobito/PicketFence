package fyi.karm.picketfence;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.getcapacitor.JSObject;

import org.json.JSONArray;

import java.util.ArrayList;

import fyi.karm.perimeter.PerimeterReceiver;

public class CustomPushGeofenceReceiver extends PerimeterReceiver {

    private NotificationManager notificationManager;
    private NotificationChannel notificationChannel;

    @Override
    public void onFenceTriggered(Context context, ArrayList<JSObject> triggeredJSFences, long triggerTime, int transitionType) {
        if(transitionType == fyi.karm.perimeter.Constants.MONITOR_ENTER)
        {
            handleEntrance(context, triggeredJSFences, triggerTime);
        }
        else
        {
            handleExit(context, triggeredJSFences, triggerTime);
        }
    }

    @Override
    public void onError(Context context, int errorCode, String errorMessage) {

    }

    public void handleEntrance(Context context, ArrayList<JSObject> triggeredJSFences, long triggerTime) {

        String entranceMessage = "";

        if(triggeredJSFences.size() == 1)
        {
            JSObject fence = triggeredJSFences.get(0);
            String fenceName = fence.getString("name");
            String extraData = fence.getString("payload");
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

        Notification entranceNotif = getEntranceNotification(context, triggeredJSFences, triggerTime);
        sendFenceEventNotification(context, entranceNotif);
        Log.d("MyReceiver", entranceMessage);

    }

    public void handleExit(Context context, ArrayList<JSObject> triggeredJSFences, long triggerTime) {

        String exitMessage = "";

        if(triggeredJSFences.size() == 1)
        {
            JSObject fence = (JSObject) triggeredJSFences.get(0);
            exitMessage = "Looks like you've left " + fence.getString("name");
            exitMessage += "!";
        }
        else if(triggeredJSFences.size() > 1)
        {
            exitMessage = "Looks like you're no longer near a few different buildings: ";
            String listOfFences = "";

            for(JSObject fence : triggeredJSFences)
            {
                listOfFences += fence.getString("name") + ",";
            }

            exitMessage += (" at " + listOfFences + ".");
        }

        Notification entranceNotif = getExitNotification(context, triggeredJSFences, triggerTime);
        sendFenceEventNotification(context, entranceNotif);
        Log.d("MyReceiver", exitMessage);
    }

    Notification getEntranceNotification(Context context, ArrayList<JSObject> triggeredJSFences, long triggerTime)
    {
        String notificationTitle = "";
        String notificationContent = "";
        String expandedNotificationContent = "";

        if(triggeredJSFences.size() == 1)
        {
            JSObject fence = triggeredJSFences.get(0);
            notificationTitle = "Looks like you're near " + fence.getString("name");
            notificationContent = fence.getString("payload");
            expandedNotificationContent = "Be sure to check out " + fence.getString("payload") + ". Tap to find out more about this place.";
        }
        else if(triggeredJSFences.size() > 1)
        {
            notificationTitle = "Looks like you're near a few different places.";
            notificationContent = "There's a lot going on.";
            String listOfFences = "";

            for(JSObject triggeredFence : triggeredJSFences)
            {
                listOfFences += triggeredFence.getString("name") + ",";
            }

            expandedNotificationContent = "We're reminding you near " + listOfFences + ". There's lots going on. Tap to find out more.";
        }

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(Constants.DEEP_LINK_BASE_URI +
                "?type=" + Constants.FENCE_NOTIFICATION +
                "&data=" + Uri.encode((new JSONArray(triggeredJSFences)).toString())));

        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent,
            PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);

        return (new NotificationCompat.Builder(context, Constants.NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(notificationTitle)
                .setContentText(notificationContent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(expandedNotificationContent))
                .setAutoCancel(true)
                .build());
    }

    Notification getExitNotification(Context context, ArrayList<JSObject> triggeredJSFences, long triggerTime)
    {
        String notificationTitle = "";
        String notificationContent = "Hope you had a blast";

        if(triggeredJSFences.size() == 1)
        {
            JSObject fence = (JSObject) triggeredJSFences.get(0);
            notificationTitle = "Looks like you've left " + fence.getString("name");
            notificationContent += "!";
        }
        else if(triggeredJSFences.size() > 1)
        {
            notificationTitle = "Looks like you're no longer near a few different buildings on campus.";
            String listOfFences = "";

            for(JSObject triggered : triggeredJSFences)
            {
                listOfFences += triggered.getString("name") + ",";
            }

            notificationContent += (" at " + listOfFences + ".");
        }

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(Constants.DEEP_LINK_BASE_URI +
                "?type=" + Constants.FENCE_NOTIFICATION +
                "&data=" + Uri.encode((new JSONArray(triggeredJSFences)).toString())));

        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent,
                PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);

        return (new NotificationCompat.Builder(context, Constants.NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(notificationTitle)
                .setContentText(notificationContent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build());
    }

    private void sendFenceEventNotification(Context context, Notification notif) {

        if(notificationManager == null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                notificationManager = context.getSystemService(NotificationManager.class);
            }
        }

        if(notificationChannel == null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                int importance = NotificationManager.IMPORTANCE_DEFAULT;
                notificationChannel = new NotificationChannel(Constants.NOTIFICATION_CHANNEL_ID, Constants.NOTIFICATION_CHANNEL_NAME, importance);
                notificationChannel.setDescription(Constants.NOTIFICATION_CHANNEL_DESC);
                // Register the channel with the system; you can't change the importance
                // or other notification behaviors after this
                notificationManager.createNotificationChannel(notificationChannel);
            }
        }

        notificationManager.notify(0, notif);
    }
}
