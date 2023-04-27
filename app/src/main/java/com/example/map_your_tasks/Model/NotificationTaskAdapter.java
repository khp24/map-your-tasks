package com.example.map_your_tasks.Model;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Paint;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.map_your_tasks.R;
import com.example.map_your_tasks.fragments.NotificationFragment;
import com.example.map_your_tasks.notification.NotificationPublisher;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class NotificationTaskAdapter extends RecyclerView.Adapter<NotificationTaskAdapter.TaskViewHolder> {

    private List<Task> mTasks;
    private Activity activity;
    private Context context;
    private NotificationManagerCompat notificationManager;

    public NotificationTaskAdapter(List<Task> mTasks, Activity activity, Context context,
                                   Map<Task, Integer> taskIdMap) {
        this.mTasks = mTasks;
        this.activity = activity;
        this.context = context;
        this.notificationManager = NotificationManagerCompat.from(activity);
    }

    @NonNull
    @Override
    public NotificationTaskAdapter.TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.notification_list_item, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationTaskAdapter.TaskViewHolder holder, int position) {
        Task task = mTasks.get(position);
        Integer taskId = task.getId().hashCode();
        holder.bind(task, activity, context, notificationManager, taskId);
    }

    @Override
    public int getItemCount() {
        return mTasks.size();
    }

    public static class TaskViewHolder extends RecyclerView.ViewHolder {

        private TextView mNameTextView;
        private TextView mTimeTextView;
        private ToggleButton mNotificationSetButton;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            mNameTextView = itemView.findViewById(R.id.notification_task_name);
            mTimeTextView = itemView.findViewById(R.id.notification_task_time);
            mNotificationSetButton = itemView.findViewById(R.id.notification_set_button);
        }

        public void bind(Task task, Activity activity, Context context,
                         NotificationManagerCompat notificationManager, Integer taskId) {

            // Find the due date of the task
            final Date dueDate = parseDueDate(context, task.getDueDate());
            if (dueDate == null) {
                // Stop everything if we can't parse the date
                return;
            }

            // Get permission to send notifications
            if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                // Permission is not granted, request it
                ActivityCompat.requestPermissions(activity,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS}, 0);
            } else {
                // Permission is already granted, proceed with app logic
            }

            mNameTextView.setText(task.getName());
            mTimeTextView.setText(task.getDueDate());

            // When initializing the button, check if there is a notification currently active
            // for this task
            if (isNotificationActive(context, taskId)) {
                mNotificationSetButton.setChecked(true);
                mNotificationSetButton.setButtonDrawable(R.drawable.notification_on);
            }
            else {
                mNotificationSetButton.setChecked(false);
                mNotificationSetButton.setButtonDrawable(R.drawable.notification_off);
            }

            // Make the notification builder
            final NotificationCompat.Builder builder = new NotificationCompat.Builder(activity,
                     NotificationFragment.CHANNEL_ID)
                    .setSmallIcon(R.drawable.notification_on)
                    .setContentTitle("Task Happening Now: " + task.getName())
                    .setContentText(task.getDescription())
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT);

            mNotificationSetButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @SuppressLint("MissingPermission") // We've already checked permissions earlier in this method
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                    if (isChecked) {
                        // Set the icon to let the user know that the notification is on
                        mNotificationSetButton.setButtonDrawable(R.drawable.notification_on);

                        final Notification notification = builder.build();

                        final PendingIntent pendingIntent = buildPendingIntent(context, taskId,
                                notification);

                        long alarmDelay = SystemClock.elapsedRealtime() +
                                dueDate.getTime() - System.currentTimeMillis();
                        // If alarm is in the past, return without setting the intent
                        if (alarmDelay < 0) {
                            return;
                        }

                        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, alarmDelay, pendingIntent);
                    }
                    else {
                        // Set the icon to let the user know that the notification is off
                        mNotificationSetButton.setButtonDrawable(R.drawable.notification_off);

                        cancelNotificationIfPresent(context, taskId);
                    }
                }
            });
        }

        public static void cancelNotificationIfPresent(Context context, int taskId) {
            // Cancels the pending notification for a given taskId if it exists
            final PendingIntent pendingIntent = buildPendingIntent(context, taskId, null);
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            alarmManager.cancel(pendingIntent);
            // If this pendingIntent isn't cancelled, it will still be active and the
            // checkbox will be set to true when the user comes back to this fragment
            pendingIntent.cancel();
        }

        public static void updateNotificationIfPresent(Activity activity, Context context, Task task) {
            // Updates a notification with the specified due date
            // If there is no notification active for the taskId, does nothing
            // If newDueDate is in the past, cancels any active notification
            final int taskId = task.getId().hashCode();
            final Intent notificationIntent = new Intent(context, NotificationPublisher.class);
            // With the NO_CREATE flag this will be null if no notification is currently active
            final PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    context, taskId, notificationIntent, PendingIntent.FLAG_NO_CREATE);
            if (pendingIntent == null) {
                return;
            }

            // Find the due date of the task
            final Date dueDate = parseDueDate(context, task.getDueDate());
            if (dueDate == null) {
                // Stop everything if we can't parse the date
                return;
            }

            // Make the notification builder
            final NotificationCompat.Builder builder = new NotificationCompat.Builder(activity,
                    NotificationFragment.CHANNEL_ID)
                    .setSmallIcon(R.drawable.notification_on)
                    .setContentTitle("Task Happening Now: " + task.getName())
                    .setContentText(task.getDescription())
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT);

            // Set the notification
            final Notification notification = builder.build();

            final PendingIntent newPendingIntent = buildPendingIntent(context, taskId,
                    notification);

            final long timeDiff = dueDate.getTime() - System.currentTimeMillis();
            // If alarm is in the past, return without setting the intent
            if (timeDiff < 0) {
                return;
            }
            long alarmDelay = SystemClock.elapsedRealtime() + timeDiff;

            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, alarmDelay, newPendingIntent);

        }

        private static PendingIntent buildPendingIntent(final Context context, final int taskId,
                                                 final Notification notification) {
            // Since the AlarmManager has a cancel method which requires that you pass in the
            // PendingIntent, it is very important that the notification on and off methods
            // build a PendingIntent in exactly the same way, so that they pass the
            // PendingIntent#filterEquals check that the AlarmManager will make when cancelling
            final Intent notificationIntent = new Intent(context, NotificationPublisher.class);

            // If notification is not null, it indicates that this method is being called from the
            // notification on method, add it to the intent
            // Note that extras are explicitly not checked in PendingIntent#filterEquals
            if (notification != null) {
                notificationIntent.putExtra(NotificationPublisher.NOTIFICATION_ID_KEY, taskId);
                notificationIntent.putExtra(NotificationPublisher.NOTIFICATION_KEY, notification);
            }

            return PendingIntent.getBroadcast(context, taskId, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        }

        private boolean isNotificationActive(final Context context, final int taskId) {
            final Intent notificationIntent = new Intent(context, NotificationPublisher.class);
            // Using the no create flag, if the pending intent does not exist, null will be returned
            // So a non-null return means that the notification is already active
            return PendingIntent.getBroadcast(context, taskId, notificationIntent, PendingIntent.FLAG_NO_CREATE) != null;
        }

        private static Date parseDueDate(final Context context, final String dueDate) {
            Date date;
            try {
                return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(dueDate);
            }
            catch (ParseException e) {
                // Continue on to the next format
            }
            try {
                return new SimpleDateFormat("yyyy-MM-dd").parse(dueDate);
            }
            catch(ParseException e) {
                // Neither format worked, let the user know
                Toast.makeText(context, "Unable to parse due date of task",
                        Toast.LENGTH_SHORT).show();
                return null;
            }
        }

    }
}
