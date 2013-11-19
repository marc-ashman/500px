package com.ashman.fivehundredpx.enums;


import android.os.Parcel;
import android.os.Parcelable;

public enum PhotoSize implements Parcelable {
    SMALL (1),
    MEDIUM (2),
    LARGE (3),
    LARGEST (4);

    private int size;

    PhotoSize(int size) {
        this.size = size;
    }

    public int getSize() {
        return size;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(final Parcel dest, final int flags) {
        dest.writeInt(ordinal());
    }

    public static final Creator<PhotoSize> CREATOR = new Creator<PhotoSize>() {
        @Override
        public PhotoSize createFromParcel(final Parcel source) {
            return PhotoSize.values()[source.readInt()];
        }

        @Override
        public PhotoSize[] newArray(final int size) {
            return new PhotoSize[size];
        }
    };
}
