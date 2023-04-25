package com.example.map_your_tasks.notification;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class NotificationPublisher extends BroadcastReceiver {

    public static final String NOTIFICATION_ID_KEY = "notification_id";
    public static final String NOTIFICATION_KEY = "notification";

    @Override
    public void onReceive(final Context context, Intent intent) {

        // Get the notification manager
        final NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Retrieve the notification and its ID from the intent
        final Notification notification = intent.getParcelableExtra(NOTIFICATION_KEY);
        final int notificationId = intent.getIntExtra(NOTIFICATION_ID_KEY, 0);

        // Send the notification
        notificationManager.notify(notificationId, notification);
    }
}
