package com.example.map_your_tasks.Model;

import android.location.Address;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.time.LocalDateTime;

public class Task implements Parcelable {

    private boolean isComplete;
    private String name;
    private String description;
    private LocalDateTime dueDate;
    private Address address;

    public Task(boolean isComplete, String name, String description, LocalDateTime dueDate, Address address) {
        this.isComplete = isComplete;
        this.name = name;
        this.description = description;
        this.dueDate = dueDate;
        this.address = address;
    }

    protected Task(Parcel in) {
        isComplete = in.readByte() != 0;
        name = in.readString();
        description = in.readString();
        dueDate = (LocalDateTime) in.readSerializable();
        address = in.readParcelable(Address.class.getClassLoader());
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

    public LocalDateTime getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDateTime dueDate) {
        this.dueDate = dueDate;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
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
        parcel.writeSerializable(dueDate);
        parcel.writeParcelable(address, i);
    }
}
