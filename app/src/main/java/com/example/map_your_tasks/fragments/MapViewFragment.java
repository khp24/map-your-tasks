package com.example.map_your_tasks.fragments;

import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.map_your_tasks.Model.Task;
import com.example.map_your_tasks.Model.TaskAdapter;
import com.example.map_your_tasks.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

import com.example.map_your_tasks.Model.MapTaskAdapter;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

/**
 * This fragment describes the entire map view, it has a {@link MapFragment} as a subfragment
 */
public class MapViewFragment extends Fragment {

    private EditText mFilterDistance;
    private Button mFilterButton;
    private CheckBox mCheckAll;
    private RecyclerView mRecyclerView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        final View rootView = inflater.inflate(R.layout.fragment_map_view, container, false);
        final FragmentActivity activity = getActivity();

        // Initialize map fragment
        final MapFragment fragment = new MapFragment();
        fragment.addUserLocation(activity);

        // Open map fragment
        activity.getSupportFragmentManager().beginTransaction().replace(R.id.map, fragment).commit();

        // Setup Recycler View
        mRecyclerView = rootView.findViewById(R.id.tasks_for_map_recycler);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(activity,
                LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(layoutManager);

        loadTasksAndFragment(rootView, fragment);
        return rootView;
    }

    private List<Task> createTestTasks() {
        final Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
        final List<Task> tasks = new ArrayList<>();
        try {
            tasks.add(new Task("id1", false, "Task1", "Do Task1", null,
                    40.1, -75.5, "Address1"));
            tasks.add(new Task("id2", false, "Task2", "Do Task2", null,
                    40.5, -75.3, "Address2"));
            tasks.add(new Task("id3", false, "Task3", "Do Task3", null,
                    40.3, -75.1, "Address3"));
        } catch (Exception e) {
            return null;
        }
        return tasks;
    }

    private void loadTasksAndFragment(final View rootView, final MapFragment fragment) {
        final FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        final String uid = firebaseAuth.getUid();

        final Query firebaseQuery = FirebaseDatabase.getInstance().getReference("tasks")
                .child(uid).orderByChild("complete");

        final List<Task> taskList = new ArrayList<>();
        firebaseQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Task> taskList = new ArrayList<>();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Task task = dataSnapshot.getValue(Task.class);
                    task.setId(dataSnapshot.getKey());
                    if (task.getAddress() != null && !task.isComplete()) {
                        taskList.add(task);
                    }
                }
                // Do the rest of the fragment loading
                loadFragment(rootView, fragment, taskList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Fragment_list", "Error retrieving countries from database", error.toException());
            }
        });
    }

    private void loadFragment(final View rootView, final MapFragment fragment,
                              final List<Task> tasks) {
        // Pass a copy of the tasks list into the adapter, we don't want operations on the adapter
        // to modify the original list
        final List<Task> copiedTasks = new ArrayList<>(tasks);
        final MapTaskAdapter adapter = new MapTaskAdapter(copiedTasks, fragment);
        mRecyclerView.setAdapter(adapter);

        // Setup checkAll box, it will change visibility of all markers on the map and change
        // the state of all checkboxes in the recycler view
        mCheckAll = rootView.findViewById(R.id.map_all_tasks_box);
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

        // Setup filter tasks button
        mFilterDistance = rootView.findViewById(R.id.dist_filter_edit);
        mFilterButton = rootView.findViewById(R.id.dist_filter_button);
        mFilterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Get the filter distance from the edit text field
                final double maxDist;
                try {
                    maxDist = Double.parseDouble(mFilterDistance.getText().toString());
                }
                catch (NumberFormatException e) {
                    // Let the user know if they didn't enter something right
                    Toast.makeText(getContext(), "Enter a number for maximum distance",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                filterTasksWithinDistance(fragment, mRecyclerView, tasks, maxDist);
            }
        });
    }

    private void filterTasksWithinDistance(final MapFragment mapFragment,
                                               final RecyclerView mRecyclerView,
                                               final List<Task> tasks, final double maxDist) {
        // Store all tasks which are within maxDistance miles of the current location
        final List<Task> closeTasks = new ArrayList<>();
        // Store all tasks which are farther than maxDistance miles of the current location
        final List<Task> farTasks = new ArrayList<>();

        // Get current location and use it to filter tasks
        final Consumer<Location> applyFilter = location -> {
            final LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());
            for (final Task task : tasks) {
                final LatLng taskLocation = new LatLng(task.getLatitude(),
                        task.getLongitude());
                // Add this task to the filtered list if its distance to the user is less than the max
                if (getHaversineDistance(userLocation, taskLocation) < maxDist) {
                    closeTasks.add(task);
                }
                else {
                    farTasks.add(task);
                }
            }

            final MapTaskAdapter currentAdapter = (MapTaskAdapter) mRecyclerView.getAdapter();
            // Need to make sure any close tasks are present in the recycler view
            currentAdapter.ensureAllTasksArePresent(closeTasks);
            // Need to make sure any far tasks are absent in the recycler view
            currentAdapter.ensureAllTasksAreAbsent(farTasks);

            // Design decision, selecting this button will also remove all markers and de-select
            // all checkboxes. Preserving the state would require a lot of potentially fragile code
            // Ideally the user only uses this button a few times
            mapFragment.makeAllTasksInvisible();
            mCheckAll.setChecked(false);
        };
        final Runnable informUserOfLocationFailure = () -> {
            Toast.makeText(getContext(), "Unable to load user location",
                    Toast.LENGTH_SHORT).show();
        };

        // Apply the filter using the user's location
        mapFragment.operateOnLocation(getActivity(), applyFilter, informUserOfLocationFailure);
    }

    private double getHaversineDistance(final LatLng posn1, final LatLng posn2) {
        // Return distance in miles between two LatLngs
        // ref: https://cloud.google.com/blog/products/maps-platform/how-calculate-distances-map-maps-javascript-api

        final double rEarth = 3558.8; // radius of Earth in miles
        final double lat1 = posn1.latitude * Math.PI / 180.0;
        final double lat2 = posn2.latitude * Math.PI / 180.0;
        final double latDiff = lat2 - lat1;
        final double long1 = posn1.longitude * Math.PI / 180.0;
        final double long2 = posn2.longitude * Math.PI / 180.0;
        final double longDiff = long2 - long1;
        return 2 * rEarth * Math.asin(Math.sqrt(Math.sin(latDiff / 2) * Math.sin(latDiff / 2) + Math.cos(lat1)
        * Math.cos(lat2) * Math.sin(longDiff / 2) * Math.sin(longDiff / 2)));
    }

}
