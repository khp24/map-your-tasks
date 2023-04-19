package com.example.map_your_tasks.fragments;

import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
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
import com.example.map_your_tasks.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

import com.example.map_your_tasks.Model.MapTaskAdapter;
import com.google.android.gms.maps.model.LatLng;

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
        final List<Task> tasks = createTestTasks();
        final MapTaskAdapter adapter = new MapTaskAdapter(tasks, fragment);
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
                    maxDist = Double.parseDouble(mFilterButton.getText().toString());
                }
                catch (NumberFormatException e) {
                    // Let the user know if they didn't enter something right
                    Toast.makeText(getContext(), "Enter a number for maximum distance",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                final List<Task> filteredTasks = findTasksWithinDistance(fragment, tasks, maxDist);
            }
        });


        return rootView;
    }

    //TODO: Get this from the database or an intent, this is just test data
    private List<Task> createTestTasks() {
        final Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
        final List<Task> tasks = new ArrayList<>();
        try {
            final List<Address> address1 = geocoder.getFromLocationName(
                    "30 East Swedesford Rd, Malvern, PA 19355", 1);
            tasks.add(new Task(false, "Task1", "Do Task1", null,
                    address1.get(0).getLatitude(), address1.get(0).getLongitude(), address1.get(0).getAddressLine(0)));
            final List<Address> address2 = geocoder.getFromLocationName(
                    "52 W Lancaster Ave, Paoli, PA 19301", 1);
            tasks.add(new Task(false, "Task2", "Do Task2", null,
                    address2.get(0).getLatitude(), address2.get(0).getLongitude(), address2.get(0).getAddressLine(0)));
            final List<Address> address3 = geocoder.getFromLocationName(
                    "550 E Lancaster Ave Ste C, St Davids, PA 19087", 1);
            tasks.add(new Task(false, "Task3", "Do Task3", null,
                    address3.get(0).getLatitude(), address3.get(0).getLongitude(), address3.get(0).getAddressLine(0)));
        } catch (Exception e) {
            return null;
        }
        return tasks;
    }

    private List<Task> findTasksWithinDistance(final MapFragment mapFragment,
                                               final List<Task> tasks, final double maxDist) {
        // Return all tasks which are within maxDistance miles of the current location
        final List<Task> filteredTasks = new ArrayList<>();

        // Get current location and use it to filter tasks
        final Consumer<Location> applyFilter = location -> {
            final LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());
            for (final Task task : tasks) {
                final LatLng taskLocation = new LatLng(task.getAddress().getLatitude(),
                        task.getAddress().getLongitude());
                if (getHaversineDistance(userLocation, taskLocation) < maxDist) {
                    filteredTasks.add(task);
                }
            }
        };
        final Runnable informUserOfLocationFailure = () -> {
            Toast.makeText(getContext(), "Unable to load user location",
                    Toast.LENGTH_SHORT).show();
        };
        // Apply the filter using the found location
        mapFragment.operateOnLocation(getActivity(), applyFilter, informUserOfLocationFailure);

        return filteredTasks;
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
