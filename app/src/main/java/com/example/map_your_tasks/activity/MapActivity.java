package com.example.map_your_tasks.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;

import com.example.map_your_tasks.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.List;

import Model.MapTaskAdapter;
import Model.MapTaskItem;

public class MapActivity extends AppCompatActivity {

    private CheckBox mCheckAll;
    private RecyclerView mRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        // Initialize map fragment
        MapFragment fragment = new MapFragment();
        fragment.addUserLocation(this);

        // Open map fragment
        getSupportFragmentManager().beginTransaction().replace(R.id.map, fragment).commit();

        // Wire components
        mRecyclerView = findViewById(R.id.tasks_for_map_recycler);

        // Setup Recycler View
        final LinearLayoutManager layoutManager = new LinearLayoutManager(this,
                LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(layoutManager);
        final List<MapTaskItem> tasks = createTestTasks();
        final MapTaskAdapter adapter = new MapTaskAdapter(tasks, fragment);
        mRecyclerView.setAdapter(adapter);

        // Setup checkAll box, it will change visibility of all markers on the map and change
        // the state of all checkboxes in the recycler view
        mCheckAll = findViewById(R.id.map_all_tasks_box);
        mCheckAll.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                final int numTasks = mRecyclerView.getChildCount();
                // Iterate over all view holders and set the checked state of their checkboxes
                for (int i = 0; i < numTasks; i++) {
                    final MapTaskAdapter.TaskViewHolder holder =
                            (MapTaskAdapter.TaskViewHolder) mRecyclerView.getChildViewHolder(mRecyclerView.getChildAt(i));
                    holder.setCheckedState(checked);
                }
                // Update the map fragment to show the tasks or not
                if (checked) {
                    final MapTaskAdapter adapter = (MapTaskAdapter) mRecyclerView.getAdapter();
                    fragment.makeAllTasksVisible(adapter.getTasks());
                }
                else {
                    fragment.makeAllTasksInvisible();
                }
            }
        });
    }

    //TODO: Get this from the database or an intent
    private List<MapTaskItem> createTestTasks() {
        final List<MapTaskItem> tasks = new ArrayList<>();
        tasks.add(new MapTaskItem("Task1", "333 Street Rd",
                new LatLng(40, -75)));
        tasks.add(new MapTaskItem("Task2", "334 Street Rd",
                new LatLng(40.2, -75)));
        tasks.add(new MapTaskItem("Task3", "335 Street Rd",
                new LatLng(40, -75.2)));
        tasks.add(new MapTaskItem("Task4", "336 Street Rd, Malvern PA 19355 United States",
                new LatLng(40.2, -75.2)));
        return tasks;
    }
}