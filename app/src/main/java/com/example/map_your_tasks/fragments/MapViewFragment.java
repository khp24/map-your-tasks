package com.example.map_your_tasks.fragments;

import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;

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

import com.example.map_your_tasks.Model.MapTaskAdapter;

/**
 * This fragment describes the entire map view, it has a {@link MapFragment} as a subfragment
 */
public class MapViewFragment extends Fragment {

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

        // Wire components
        mRecyclerView = rootView.findViewById(R.id.tasks_for_map_recycler);

        // Setup Recycler View
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

        return rootView;
    }

    //TODO: Get this from the database or an intent, this is just test data
    private List<Task> createTestTasks() {
        final Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
        final List<Task> tasks = new ArrayList<>();
        try {
            final Address add1 = new Address(Locale.getDefault());
            add1.setAddressLine(0, "Address1");
            add1.setLatitude(40.1);
            add1.setLongitude(-75.5);

            final Address add2 = new Address(Locale.getDefault());
            add2.setAddressLine(0, "Address1");
            add2.setLatitude(40.5);
            add2.setLongitude(-75.3);

            final Address add3 = new Address(Locale.getDefault());
            add3.setAddressLine(0, "Address1");
            add3.setLatitude(40.3);
            add3.setLongitude(-75.1);

            tasks.add(new Task(false, "Task1", "Do Task1", null,
                    add1));
            tasks.add(new Task(false, "Task2", "Do Task2", null,
                    add2));
            tasks.add(new Task(false, "Task3", "Do Task3", null,
                    add3));
        } catch (Exception e) {
            return null;
        }
        return tasks;
    }
}
