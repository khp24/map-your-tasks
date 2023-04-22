package com.example.map_your_tasks.fragments;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
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

import com.example.map_your_tasks.Model.TaskAdapter;
import com.example.map_your_tasks.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.example.map_your_tasks.Model.Task;


import java.sql.Time;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.time.LocalDateTime;

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

    private TaskAdapter mTaskAdapter;

    private Button confirmButton;
    private Button clearButton;

    private double longitude;
    private double latitude;
    private String confirmedAddString;

    private Date confirmedDate;
    private Date confirmedTime;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

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

        confirmButton = rootView.findViewById(R.id.button_confirm);
        confirmButton.setOnClickListener(this);

        clearButton = rootView.findViewById(R.id.button_clear);
        clearButton.setOnClickListener(this);

        return rootView;
    }


    private void updateDateLabel(){
        String myFormat="MM/dd/yy";
        SimpleDateFormat dateFormat = new SimpleDateFormat(myFormat, Locale.US);
        mEditDate.setText(dateFormat.format(calendar.getTime()));
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
                mValidatedAddress.setText(addressString);
                longitude = address.getLongitude();
                latitude = address.getLatitude();
                confirmedAddString = addressString;
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

                confirmedDate = calendar.getTime();
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
                mEditTime.setText(String.format("%02d:%02d", hourOfDay, minute));
                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                calendar.set(Calendar.MINUTE, minute);
                confirmedTime = calendar.getTime();
            }
        };
        // Show the time picker
        new TimePickerDialog(getContext(), time,
                12, 0, true).show();
    }

    public void confirm() {
        String name = mEditName.getText().toString().trim();
        String description = mEditDescription.getText().toString().trim();

        String formattedDate = null;

        if ((confirmedDate != null) && (confirmedTime != null)) {
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date datetime = new Date(confirmedDate.getTime() + confirmedTime.getTime());
            formattedDate = df.format(datetime);
        } else if((confirmedDate !=null)&&(confirmedTime == null)) {
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
            Date datetime = new Date(confirmedDate.getTime());
            formattedDate = df.format(datetime);
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
            String taskID = firebaseDatabase.push().getKey();
            Task  newTask = new Task(  taskID,false,  name,  description, formattedDate, longitude, latitude, confirmedAddString);
            firebaseDatabase.child(taskID).setValue(newTask);
            Toast.makeText(getContext(), "New Task added", Toast.LENGTH_SHORT).show();
            clearFields();
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