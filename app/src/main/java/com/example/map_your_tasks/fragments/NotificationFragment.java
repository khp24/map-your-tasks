package com.example.map_your_tasks.fragments;

import android.Manifest;
import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.map_your_tasks.Model.NotificationTaskAdapter;
import com.example.map_your_tasks.Model.Task;
import com.example.map_your_tasks.Model.TaskAdapter;
import com.example.map_your_tasks.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NotificationFragment extends Fragment {

    public final static String CHANNEL_ID = "mapYourTasks";

    private RecyclerView mRecyclerView;
    private NotificationTaskAdapter mAdapter;

    private DatabaseReference firebaseDatabase;
    private FirebaseAuth firebaseAuth;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final Activity activity = getActivity();
        final Context context = getContext();

        // Create and register the notification channel, needed to send notifications
        createNotificationChannel();

        View rootView = inflater.inflate(R.layout.fragment_notification, container, false);

        mRecyclerView = rootView.findViewById(R.id.notification_recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(activity));

        // Load the database for the current user
        firebaseAuth = FirebaseAuth.getInstance();
        final String uid = firebaseAuth.getUid();
        firebaseDatabase = FirebaseDatabase.getInstance().getReference("tasks").child(uid);
        // Set listener for the database to update the recycler view when data changes
        firebaseDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                final List<Task> taskList = new ArrayList<>();
                // This map stores tasks grouped by the hash of their ID in the database
                // This value will be the ID of the notification for the particular task
                final Map<Task, Integer> taskIdMap = new HashMap<>();

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    final String key = dataSnapshot.getKey();
                    final Task task = dataSnapshot.getValue(Task.class);
                    if (task.getDueDate() != null && !task.getDueDate().isEmpty()) {
                        taskList.add(task);
                    }
                    taskIdMap.put(task, key.hashCode());
                }
                mAdapter = new NotificationTaskAdapter(taskList, activity, context, taskIdMap);
                mRecyclerView.setAdapter(mAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Fragment_list", "Error retrieving tasks from database", error.toException());
            }
        });

        return rootView;
    }

    private void createNotificationChannel() {
        final String channelName = "mapYourTasks";
        final String description = "Notifications for the Map Your Tasks App";
        final NotificationChannel channel = new NotificationChannel(CHANNEL_ID, channelName,
                NotificationManager.IMPORTANCE_DEFAULT);
        channel.setDescription(description);
        // Register the channel with the system. You can't change the importance
        // or other notification behaviors after this.
        final NotificationManager notificationManager =
                getActivity().getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
    }
}
