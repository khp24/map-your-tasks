package com.example.map_your_tasks.activity;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.map_your_tasks.R;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * Activity responsible for letting a user add a new task
 */
public class AddTaskActivity extends AppCompatActivity implements View.OnClickListener  {

    private Geocoder geocoder;
    private EditText mEditName;
    private EditText mEditDescription;
    private EditText mEditAddress;
    private Button mValidateAddress;
    private TextView mValidatedAddress;
    private EditText mEditDate;
    private EditText mEditTime;
    private Calendar calendar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);

        geocoder = new Geocoder(this, Locale.getDefault());

        // Wire components
        mEditName = findViewById(R.id.add_task_edit_name);
        mEditDescription = findViewById(R.id.add_task_edit_desc);
        mEditAddress = findViewById(R.id.add_task_edit_addr);
        mValidateAddress = findViewById(R.id.add_task_validate_addr);
        mValidatedAddress = findViewById(R.id.add_task_validated_addr);

        mValidateAddress.setOnClickListener(this);

        // Set up editDate
        mEditDate = findViewById(R.id.add_task_date);
        calendar = Calendar.getInstance();
        mEditDate.setOnClickListener(this);

        // Set up editTime
        mEditTime = findViewById(R.id.add_task_time);
        mEditTime.setOnClickListener(this);
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
            Toast.makeText(AddTaskActivity.this, "Unable to find Matching Address",
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

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Suggested Address");
        builder.setMessage("Does this address look right: " + addressString);
        builder.setPositiveButton("Yes, use this address", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                mValidatedAddress.setText(addressString);
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
        new DatePickerDialog(AddTaskActivity.this, date,
                calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void setupTimePicker() {
        // This will listen for the time being set on our time picker
        TimePickerDialog.OnTimeSetListener time = new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int hourOfDay, int minute) {
                mEditTime.setText(String.format("%02d:%02d", hourOfDay, minute));
            }
        };
        // Show the time picker
        new TimePickerDialog(AddTaskActivity.this, time,
                12, 0, true).show();
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
            default:
                break;
        }
    }
}