package Model;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.map_your_tasks.R;
import com.example.map_your_tasks.activity.MapFragment;

import java.util.List;

/**
 * Adapter to display selectable tasks in the map view
 */
public class MapTaskAdapter extends RecyclerView.Adapter<MapTaskAdapter.TaskViewHolder> {

    private List<MapTaskItem> tasks;
    // Need access to the fragment in order to set the visibility of markers
    private MapFragment mapFragment;

    public MapTaskAdapter(List<MapTaskItem> tasks, final MapFragment mapFragment) {
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
        final MapTaskItem task = tasks.get(position);
        holder.mNameView.setText(task.getTitle());
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

    public List<MapTaskItem> getTasks() {
        return tasks;
    }

    public static class TaskViewHolder extends RecyclerView.ViewHolder {
        private CheckBox mSelectBox;
        private TextView mNameView;
        private TextView mAddressView;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            mSelectBox = itemView.findViewById(R.id.map_task_checkbox);
            mNameView = itemView.findViewById(R.id.map_task_name);
            mAddressView = itemView.findViewById(R.id.map_task_address);
        }

        public void setCheckedState(final boolean checked) {
            mSelectBox.setChecked(checked);
        }
    }
}
