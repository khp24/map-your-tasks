package com.example.map_your_tasks.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.map_your_tasks.Model.Task;
import com.example.map_your_tasks.Model.TaskAdapter;
import com.example.map_your_tasks.R;
import com.example.map_your_tasks.activity.SecondActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class TaskListFragment extends Fragment implements TaskAdapter.TaskAdapterListener {

    private RecyclerView mRecyclerView;
    private FirebaseAuth firebaseAuth;
    private TaskAdapter mTaskAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_task_list, container, false);
        mRecyclerView = rootView.findViewById(R.id.recyclerView);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mTaskAdapter = new TaskAdapter();
        mTaskAdapter.setTaskAdapterListener(this);
        mRecyclerView.setAdapter(mTaskAdapter);

        firebaseAuth = FirebaseAuth.getInstance();
        String uid = firebaseAuth.getUid();
        Log.d("uid", uid);

        Query firebaseQuery = FirebaseDatabase.getInstance().getReference("tasks")
                .child(uid).orderByChild("complete");
        firebaseQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Task> taskList = new ArrayList<>();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Task task = dataSnapshot.getValue(Task.class);
                    task.setId(dataSnapshot.getKey());
                    taskList.add(task);
                }
                mTaskAdapter.setTasks(taskList);
                mTaskAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Fragment_list", "Error retrieving countries from database", error.toException());
            }
        });
        return rootView;
    }

    @Override
    public void onTaskUpdateClick(Task task) {
        ((SecondActivity) getActivity()).editTask(task);
    }
}