package com.example.map_your_tasks.model;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.map_your_tasks.R;
import com.example.map_your_tasks.fragments.MapFragment;

import java.util.List;

/**
 * Adapter to display selectable tasks in the map view
 */
public class MapTaskAdapter extends RecyclerView.Adapter<MapTaskAdapter.TaskViewHolder> {

    private List<Task> tasks;
    // Need access to the fragment in order to set the visibility of markers
    private MapFragment mapFragment;

    public MapTaskAdapter(List<Task> tasks, final MapFragment mapFragment) {
        this.tasks = tasks;
        this.mapFragment = mapFragment;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.map_task_item, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        final Task task = tasks.get(position);
        holder.mNameView.setText(task.getName());
        holder.mAddressView.setText(task.getAddress());

        holder.mSelectBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                if (checked) {
                    mapFragment.makeTaskVisible(task);
                }
                else {
                    mapFragment.makeTaskInvisible(task);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    public List<Task> getTasks() {
        return tasks;
    }

    public void ensureAllTasksArePresent(final List<Task> tasksToAdd) {
        // Given a list of tasks, ensures that they are present in the recycler view
        for (final Task task : tasksToAdd) {
            if (!tasks.contains(task)) {
                // It's not in the current list, add it
                tasks.add(task);
                notifyItemInserted(tasks.size() - 1);
                notifyDataSetChanged();
            }
        }
    }

    public void ensureAllTasksAreAbsent(final List<Task> tasksToRemove) {
        // Given a list of tasks, ensures that they are NOT present in the recycler view
        for (final Task task : tasksToRemove) {
            if (tasks.contains(task)) {
                // It's not in the current list, remove it
                final int index = tasks.indexOf(task);
                tasks.remove(task);
                notifyItemRemoved(index);
                notifyDataSetChanged();
            }
        }
    }

    public static class TaskViewHolder extends RecyclerView.ViewHolder {
        private CheckBox mSelectBox;
        private TextView mNameView;
        private TextView mAddressView;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            mSelectBox = itemView.findViewById(R.id.map_task_checkbox);
            // Make sure the select box starts unchecked
            mSelectBox.setChecked(false);
            mNameView = itemView.findViewById(R.id.map_task_name);
            mAddressView = itemView.findViewById(R.id.map_task_address);
        }

        public void setCheckedState(final boolean checked) {
            mSelectBox.setChecked(checked);
        }
    }
}
