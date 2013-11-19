package com.ashman.fivehundredpx.net.json.replies;


import android.os.Parcel;
import android.os.Parcelable;

public class Photo implements Parcelable {
    public static Creator<Photo> CREATOR = new Creator<Photo>() {
        @Override
        public Photo createFromParcel(Parcel parcel) {
            return new Photo(parcel);
        }

        @Override
        public Photo[] newArray(int size) {
            return new Photo[size];
        }
    };

    private int id;
    private String name;
    private String description;
    private float rating;
    private int favorites_count;
    private boolean nsfw;
    private Image[] images;
    private User user;
    private boolean favorited;

    public Photo() {

    }

    public Photo(Parcel in) {
        id = in.readInt();
        name = in.readString();
        description = in.readString();
        rating = in.readFloat();
        favorites_count = in.readInt();
        nsfw = in.readInt() == 1 ? true : false;
        Parcelable[] parcelableImages = in.readParcelableArray(Image.class.getClassLoader());
        images = new Image[parcelableImages.length];
        for(int i=0; i < parcelableImages.length; i++) {
            images[i] = (Image) parcelableImages[i];
        }
        user = new User(in);
    }

    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeInt(id);
        parcel.writeString(name);
        parcel.writeString(description);
        parcel.writeFloat(rating);
        parcel.writeInt(favorites_count);
        parcel.writeInt(nsfw ? 1 : 0);
        parcel.writeParcelableArray(images, flags);
        user.writeToParcel(parcel, flags);
        //parcel.writeParcelable(user, flags);
    }

    public int describeContents() {
        return 0;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public float getRating() {
        return rating;
    }

    public int getFavoritesCount() {
        return favorites_count;
    }

    public boolean isNsfw() {
        return nsfw;
    }

    public void setNsfw(boolean nsfw) {
        this.nsfw = nsfw;
    }

    public Image[] getImages() {
        return images;
    }

    public User getUser() {
        return user;
    }

    public String getUrl(int size) {
        return getUrl(size + "");
    }

    public String getUrl(String size) {
        for(Image image : images) {
            if(image.getSize().equals(size))
                return image.getUrl();
        }
        return null;
    }

    public String getLargestSizeUrl() {
        Image largest = null;
        int largestNum = -1;
        for(Image image : images) {
            int size = -1;
            try {
                size = Integer.parseInt(image.getSize());
            } catch (Exception e) {
                //can't parse? weird size, ignore.
            }
            if(size > largestNum) {
                largestNum = size;
                largest = image;
            }
        }
        if(largest == null)
            return null;
        return largest.getUrl();
    }

    public boolean isFavorited() {
        return favorited;
    }
}
