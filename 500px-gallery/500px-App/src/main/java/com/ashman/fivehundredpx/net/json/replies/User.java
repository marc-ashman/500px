package com.ashman.fivehundredpx.net.json.replies;


import android.os.Parcel;
import android.os.Parcelable;

public class User implements Parcelable {
    public static Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel parcel) {
            return new User(parcel);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };

    private int id;
    private String username;

    public User() {

    }

    public User(Parcel in) {
        id = in.readInt();
        username = in.readString();
    }

    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeInt(id);
        parcel.writeString(username);
    }

    public int describeContents() {
        return 0;
    }

    public int getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }
}
