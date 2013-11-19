package com.ashman.fivehundredpx.net.json.replies;


import android.os.Parcel;
import android.os.Parcelable;

public class Image implements Parcelable {
    public static Creator<Image> CREATOR = new Creator<Image>() {
        @Override
        public Image createFromParcel(Parcel parcel) {
            return new Image(parcel);
        }

        @Override
        public Image[] newArray(int size) {
            return new Image[size];
        }
    };

    private String size;
    private String url;

    public Image() {

    }

    public Image(Parcel in) {
        size = in.readString();
        url = in.readString();
    }

    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeString(size);
        parcel.writeString(url);
    }

    public int describeContents() {
        return 0;
    }

    public String getSize() {
        return size;
    }

    public String getUrl() {
        return url;
    }
}
