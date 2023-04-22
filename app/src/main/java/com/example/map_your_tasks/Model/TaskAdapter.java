package com.example.map_your_tasks.Model;

import android.graphics.Paint;
import android.icu.text.SimpleDateFormat;
import android.icu.util.TimeZone;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.map_your_tasks.R;

import java.text.DateFormat;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    private List<Task> mTasks;

    public TaskAdapter(List<Task> mTasks) {
        this.mTasks = mTasks;
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
    }

    @Override
    public int getItemCount() {
        return mTasks.size();
    }

    static class TaskViewHolder extends RecyclerView.ViewHolder {

        private TextView mNameTextView;
        private TextView mDateTextView;
        private TextView mTimeTextView;
        private TextView mLocationTextView;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            mNameTextView = itemView.findViewById(R.id.task_name);
            mDateTextView = itemView.findViewById(R.id.task_date);
            mTimeTextView = itemView.findViewById(R.id.task_time);
            mLocationTextView = itemView.findViewById(R.id.task_location);
        }

        public void bind(Task task) {
            mNameTextView.setText(task.getName());
            mLocationTextView.setText(task.getAddress());

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

            if (task.isComplete()) {
                mNameTextView.setPaintFlags(Paint.STRIKE_THRU_TEXT_FLAG);
                mDateTextView.setVisibility(View.GONE);
                mTimeTextView.setVisibility(View.GONE);
                mLocationTextView.setVisibility(View.GONE);
            }
        }

    }
}
