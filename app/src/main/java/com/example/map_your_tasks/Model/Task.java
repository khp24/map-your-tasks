package com.example.map_your_tasks.Model;

import android.icu.text.SimpleDateFormat;
import android.location.Address;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import org.w3c.dom.Text;

import java.text.ParseException;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Objects;

public class Task implements Parcelable {

    private boolean isComplete;
    private String name;
    private String description;
    private String dueDate;
    private double latitude;
    private double longitude;
    private String address;

    public Task() {}

    public Task(boolean isComplete, String name, String description, String dueDate, double latitude, double longitude, String address) {
        this.isComplete = isComplete;
        this.name = name;
        this.description = description;
        this.dueDate = dueDate;
        this.latitude = latitude;
        this.longitude = longitude;
        this.address = address;
    }

    protected Task(Parcel in) {
        isComplete = in.readByte() != 0;
        name = in.readString();
        description = in.readString();
        dueDate = in.readString();
        latitude = in.readDouble();
        longitude = in.readDouble();
        address = in.readString();
    }

    public static final Creator<Task> CREATOR = new Creator<Task>() {
        @Override
        public Task createFromParcel(Parcel parcel) {
            return new Task(parcel);
        }

        @Override
        public Task[] newArray(int i) {
            return new Task[i];
        }
    };

    public boolean isComplete() {
        return isComplete;
    }

    public void setComplete(boolean complete) {
        isComplete = complete;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDueDate() {
        return dueDate;
    }

    public void setDueDate(String dueDate) {
        this.dueDate = dueDate;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Date getDate() {
        String dueDate = this.getDueDate();
        Date date = null;
        if (!TextUtils.isEmpty(dueDate)) {
            SimpleDateFormat df = null;
            if (dueDate.length() > 11) {
                df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            } else {
                df = new SimpleDateFormat("yyyy-MM-dd");
            }

            try {
                date = df.parse(dueDate);
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
        }
        return date;
    }

    public Date getTime() {
        Date time = null;
        if (!TextUtils.isEmpty(dueDate) && dueDate.length() > 11) {
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            try {
                time = df.parse(dueDate);
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
        }
        return time;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel parcel, int i) {
        parcel.writeByte((byte) (isComplete ? 1 : 0));
        parcel.writeString(name);
        parcel.writeString(description);
        parcel.writeString(dueDate);
        parcel.writeDouble(latitude);
        parcel.writeDouble(longitude);
        parcel.writeString(address);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return isComplete == task.isComplete &&
                Double.compare(task.latitude, latitude) == 0 &&
                Double.compare(task.longitude, longitude) == 0 &&
                Objects.equals(name, task.name) &&
                Objects.equals(description, task.description) &&
                Objects.equals(dueDate, task.dueDate) &&
                Objects.equals(address, task.address);
    }

    @Override
    public int hashCode() {
        return Objects.hash(isComplete, name, description, dueDate, latitude, longitude, address);
    }
}
