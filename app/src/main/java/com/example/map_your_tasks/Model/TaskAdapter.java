package com.example.map_your_tasks.model;

import android.graphics.Paint;
import android.icu.text.SimpleDateFormat;
import android.os.Build;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.map_your_tasks.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    public interface TaskAdapterListener {

        /**
         * Listener for requests to update a task
         * @param task The task to update
         */
        public void onTaskUpdateClick(Task task);
    }

    /**
     * The class that is listening for clicks
     */
    private TaskAdapterListener listener;

    /**
     * List of tasks for display
     */
    private List<Task> mTasks;

    /**
     * The Firebase database
     */
    private DatabaseReference mDatabaseReference;

    /**
     * Firebase authentication for user information
     */
    private FirebaseAuth mFirebaseAuth;

    public TaskAdapter() {
        mFirebaseAuth = FirebaseAuth.getInstance();
        mDatabaseReference = FirebaseDatabase.getInstance().getReference("tasks").child(mFirebaseAuth.getUid());
        this.listener = null; // Set the listener to null since nothing is listening yet
        this.mTasks = new ArrayList<>();
    }

    public TaskAdapter(List<Task> mTasks) {
        this();
        this.mTasks = mTasks;
    }

    /**
     * Get the list of tasks
     * @return The displayed task list
     */
    public List<Task> getTasks() {
        return mTasks;
    }

    /**
     * Set the task list for display
     * @param tasks The tasks to display
     */
    public void setTasks(List<Task> tasks) {
        this.mTasks = tasks;
    }

    @NonNull
    @Override
    public TaskAdapter.TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.task_item, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskAdapter.TaskViewHolder holder, int position) {
        Task task = mTasks.get(position);
        holder.bind(task);

        // Listen for long clicks on task cards
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                // When clicked show the popup menu
                showPopupMenu(v, task);
                return true;
            }
        });
    }

    @Override
    public int getItemCount() {
        return mTasks.size();
    }

    /**
     * Set the listener to the provided class
     * @param listener The class that is listening for events
     */
    public void setTaskAdapterListener(TaskAdapterListener listener) {
        this.listener = listener;
    }

    static class TaskViewHolder extends RecyclerView.ViewHolder {

        /**
         * The task name text view
         */
        private TextView mNameTextView;

        /**
         * The date text view
         */
        private TextView mDateTextView;

        /**
         * The time text view
         */
        private TextView mTimeTextView;

        /**
         * The location text view
         */
        private TextView mLocationTextView;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            // Find all text views
            mNameTextView = itemView.findViewById(R.id.task_name);
            mDateTextView = itemView.findViewById(R.id.task_date);
            mTimeTextView = itemView.findViewById(R.id.task_time);
            mLocationTextView = itemView.findViewById(R.id.task_location);
        }

        public void bind(Task task) {
            // Set text views
            mNameTextView.setText(task.getName());
            mLocationTextView.setText(task.getAddress());

            // Get, format and set date/time
            // Only format and set the value if a date/time is provided
            Date dueDate = task.getDate();
            Date dueTime = task.getTime();
            if (dueDate != null) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("eee, MMM d");
                mDateTextView.setText(dateFormat.format(dueDate));
            }

            if (dueTime != null) {
                SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a");
                mTimeTextView.setText(timeFormat.format(dueDate));
            }

            // Apply complete styling, hiding some fields
            if (task.isComplete()) {
                mNameTextView.setPaintFlags(Paint.STRIKE_THRU_TEXT_FLAG);
                mDateTextView.setVisibility(View.GONE);
                mTimeTextView.setVisibility(View.GONE);
                mLocationTextView.setVisibility(View.GONE);
            }
        }

    }

    /**
     * Show the popup menu when a task is long clicked
     * @param view The current view
     * @param task The task that was clicked
     */
    private void showPopupMenu(View view, Task task) {
        PopupMenu popupMenu = new PopupMenu(view.getContext(), view);
        popupMenu.setGravity(Gravity.END); // Set the popup menu to show below the task

        if(task.isComplete()== false){
            // Set the custom layout
            popupMenu.getMenuInflater().inflate(R.menu.popup_menu, popupMenu.getMenu());
            if (Build.VERSION.SDK_INT >= 29) {
                popupMenu.setForceShowIcon(true);
            }

            // Set click listeners for each option
            popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(android.view.MenuItem item) {
                    switch (item.getItemId()) {
                        case R.id.popup_menu_complete:
                            // Handle competing a task, including updating the database
                            task.setComplete(true);
                            mDatabaseReference.child(task.getId()).setValue(task);
                            Toast.makeText(view.getContext(), "Task completed", Toast.LENGTH_SHORT).show();
                            break;
                        case R.id.popup_menu_update:
                            // Call the listener for updating a task
                            if (listener != null) {
                                listener.onTaskUpdateClick(task);
                            }
                            break;
                        case R.id.popup_menu_delete:
                            // Delete a task from the database
                            mDatabaseReference.child(task.getId()).removeValue();
                            // Make sure there's no pending notification for this task
                            NotificationTaskAdapter.TaskViewHolder.cancelNotificationIfPresent(
                                    view.getContext(), task.getId().hashCode());
                            Toast.makeText(view.getContext(), "Task deleted", Toast.LENGTH_SHORT).show();
                            break;
                    }
                    return true;
                }
            });
        }else{
            // Set the custom layout
            popupMenu.getMenuInflater().inflate(R.menu.popup_menu_completed, popupMenu.getMenu());
            if (Build.VERSION.SDK_INT >= 29) {
                popupMenu.setForceShowIcon(true);
            }

            // Set click listeners for each option
            popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(android.view.MenuItem item) {
                    switch (item.getItemId()) {
                        case R.id.popup_menu_undo:
                            // Uncomplete a task and update the database
                            task.setComplete(false);
                            mDatabaseReference.child(task.getId()).setValue(task);
                            Toast.makeText(view.getContext(), "Undo completion", Toast.LENGTH_SHORT).show();
                            break;
                        case R.id.popup_menu_delete2:
                            // Delete a task from the database
                            mDatabaseReference.child(task.getId()).removeValue();
                            Toast.makeText(view.getContext(), "Task deleted", Toast.LENGTH_SHORT).show();
                            break;
                    }
                    return true;
                }
            });
        }

        popupMenu.show();
    }
}
