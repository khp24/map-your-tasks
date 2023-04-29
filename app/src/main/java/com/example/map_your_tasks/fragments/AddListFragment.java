package com.example.map_your_tasks.fragments;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.example.map_your_tasks.model.NotificationTaskAdapter;
import com.example.map_your_tasks.model.TaskAdapter;
import com.example.map_your_tasks.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.example.map_your_tasks.model.Task;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AddListFragment extends Fragment implements View.OnClickListener {

    private Geocoder geocoder;
    private EditText mEditName;
    private EditText mEditDescription;
    private EditText mEditAddress;
    private Button mValidateAddress;
    private TextView mValidatedAddress;
    private EditText mEditDate;
    private EditText mEditTime;
    private Calendar calendar;
    private FirebaseAuth firebaseAuth;

    private DatabaseReference firebaseDatabase;

    private Button confirmButton;
    private Button clearButton;

    private double longitude;
    private double latitude;
    private String confirmedAddString;

    private String confirmedDate;
    private String confirmedTime;
    private String taskId;

    private Task editTask;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        Bundle args = getArguments();
        taskId = null;

        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_add_task, container, false);

        geocoder = new Geocoder(getContext(), Locale.getDefault());

        // Wire components
        mEditName = rootView.findViewById(R.id.add_task_edit_name);
        mEditDescription = rootView.findViewById(R.id.add_task_edit_desc);
        mEditAddress = rootView.findViewById(R.id.add_task_edit_addr);
        mValidateAddress = rootView.findViewById(R.id.add_task_validate_addr);
        mValidatedAddress = rootView.findViewById(R.id.add_task_validated_addr);

        mValidateAddress.setOnClickListener(this);

        // Set up editDate
        mEditDate = rootView.findViewById(R.id.add_task_date);
        calendar = Calendar.getInstance();
        mEditDate.setOnClickListener(this);

        // Set up editTime
        mEditTime = rootView.findViewById(R.id.add_task_time);
        mEditTime.setOnClickListener(this);

        firebaseAuth = FirebaseAuth.getInstance();

        // Restore button visibility and add listeners
        confirmButton = rootView.findViewById(R.id.button_confirm);
        confirmButton.setText(R.string.frag_button_confirm);
        confirmButton.setOnClickListener(this);

        clearButton = rootView.findViewById(R.id.button_clear);
        clearButton.setVisibility(View.VISIBLE);
        clearButton.setOnClickListener(this);

        // Get task argument if it was passed
        if (args != null) {
            editTask = args.getParcelable("task");

            // Get and populate task information on display
            taskId = editTask.getId();
            confirmButton.setText(R.string.frag_button_update);
            mEditName.setText(editTask.getName());
            mEditDescription.setText(editTask.getDescription());
            if(editTask.getAddress() != null){
                mEditAddress.setText(editTask.getAddress());
            }

            // Update calendar date and text
            if(editTask.getDate() != null){
                calendar.setTime(editTask.getDate());
            }
            updateDateLabel();

            // Update calendar time and text
            if(editTask.getDate() != null){
                Date taskTime = editTask.getTime();
                updateTime(taskTime.getHours(), taskTime.getMinutes());
            }

            // Hide the clear button
            clearButton.setVisibility(View.GONE);
        }

        return rootView;
    }


    private void updateDateLabel(){
        String myFormat="MM/dd/yy";
        SimpleDateFormat dateFormat = new SimpleDateFormat(myFormat, Locale.US);
        mEditDate.setText(dateFormat.format(calendar.getTime()));
        SimpleDateFormat dateFormat2 = new SimpleDateFormat("YYYY-MM-dd", Locale.US);
        confirmedDate = dateFormat2.format(calendar.getTime());
    }

    private void updateTime(int hourOfDay, int minute) {
        mEditTime.setText(String.format("%02d:%02d", hourOfDay, minute));
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
        calendar.set(Calendar.MINUTE, minute);
        confirmedTime = String.format("%02d:%02d:00", hourOfDay, minute);
    }

    private void validateAddress() {
        final String enteredAddress = mEditAddress.getText().toString();

        final List<Address> possibleAddresses;
        final Address foundAddress;
        try {
            possibleAddresses = geocoder.getFromLocationName(enteredAddress, 1);
            foundAddress = possibleAddresses.get(0);
        } catch (Exception e) {
            Toast.makeText(getContext(), "Unable to find Matching Address",
                    Toast.LENGTH_LONG).show();
            return;
        }

        askForValidationConfirmation(foundAddress);
    }

    public void askForValidationConfirmation(final Address address) {
        // After the Geocoder has found an address, confirm with the user that its right

        final StringBuilder addressTextBuilder = new StringBuilder();
        for (int i = 0; i <= address.getMaxAddressLineIndex(); i++) {
            addressTextBuilder.append(address.getAddressLine(i));
        }
        final String addressString = addressTextBuilder.toString();

        final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Suggested Address");
        builder.setMessage("Does this address look right: " + addressString);
        builder.setPositiveButton("Yes, use this address", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                confirmedAddString = addressString;
                mValidatedAddress.setText(addressString);
                longitude = address.getLongitude();
                latitude = address.getLatitude();
            }
        });
        builder.setNegativeButton("No, enter another address", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // Clear the validated address
                mValidatedAddress.setText("");
                return;
            }
        });
        builder.create().show();

    }

    private void setupDatePicker() {
        // This will listen for the date being set on our date picker
        DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int day) {
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH,month);
                calendar.set(Calendar.DAY_OF_MONTH,day);
                updateDateLabel();
            }
        };
        // Show the date picker
        new DatePickerDialog(getContext(), date,
                calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void setupTimePicker() {
        // This will listen for the time being set on our time picker
        TimePickerDialog.OnTimeSetListener time = new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int hourOfDay, int minute) {
                updateTime(hourOfDay, minute);
            }
        };
        // Show the time picker
        new TimePickerDialog(getContext(), time,
                12, 0, true).show();
    }

    public void confirm() {
        Boolean update = false;
        String name = mEditName.getText().toString().trim();
        String description = mEditDescription.getText().toString().trim();

        //Case where user updated other attributes and not pressed validate address
        if((confirmedAddString ==null) && (this.taskId != null) && (editTask != null)){
            confirmedAddString = editTask.getAddress();
            longitude = editTask.getLongitude();
            latitude = editTask.getLatitude();
        }

        String formattedDate = null;

        if ((confirmedDate != null) && (confirmedTime != null)) {
            formattedDate = confirmedDate + " " + confirmedTime;;
        } else if((confirmedDate !=null)&&(confirmedTime == null)) {
            formattedDate = confirmedDate;
        }else if((confirmedDate ==null)&&(confirmedTime != null)){
            Toast.makeText(getContext(), "Please Provide due date with time", Toast.LENGTH_SHORT).show();
        }else{
            formattedDate = null;
        }

        if (name.isEmpty() || description.isEmpty()) {
            Toast.makeText(getContext(), "Please fill task name and description", Toast.LENGTH_SHORT).show();
        } else {
            String uid = firebaseAuth.getUid();
            firebaseDatabase = FirebaseDatabase.getInstance().getReference("tasks").child(uid);

            String toastText;
            String taskId;
            if (this.taskId == null) {
                toastText = "New Task added";
                taskId = firebaseDatabase.push().getKey();
            } else {
                toastText = "Task Updated";
                taskId = this.taskId;
                update = true;
            }
            Task  newTask = new Task(taskId,false,  name,  description, formattedDate, latitude, longitude, confirmedAddString);
            firebaseDatabase.child(taskId).setValue(newTask);
            Toast.makeText(getContext(), toastText, Toast.LENGTH_SHORT).show();
            clearFields();

            if(update == true) {
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.fragment_container,new TaskListFragment()).commit();
            }

            // Update the notification for this task, if one was already set
            NotificationTaskAdapter.TaskViewHolder.updateNotificationIfPresent(getActivity(),
                    getContext(), newTask);
        }
    }

    private void clearFields() {
        mEditName.setText("");
        mEditDescription.setText("");
        mEditAddress.setText("");
        mValidatedAddress.setText("");
        confirmedAddString = null;
        mEditDate.setText("");
        mEditTime.setText("");
        confirmedDate = null;
        confirmedTime = null;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.add_task_validate_addr:
                validateAddress();
                break;
            case R.id.add_task_date:
                setupDatePicker();
                break;
            case R.id.add_task_time:
                setupTimePicker();
                break;
            case R.id.button_confirm:
                confirm();
                break;
            case R.id.button_clear:
                clearFields();
                break;
            default:
                break;
        }
    }
}