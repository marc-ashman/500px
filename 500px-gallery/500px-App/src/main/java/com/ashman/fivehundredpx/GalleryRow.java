package com.ashman.fivehundredpx;


import com.ashman.fivehundredpx.net.json.replies.Photo;

public class GalleryRow {
    public static final int IMAGES_PER_ROW = 3;

    private Photo photo1;
    private Photo photo2;
    private Photo photo3;

    private int addingTo = 1;
    private int photoNumber;

    public GalleryRow(int photoNumber) {
        this.photoNumber = photoNumber;
    }

    public boolean add(Photo photo) {
        switch (addingTo) {
            case 1:
                photo1 = photo;
                break;
            case 2:
                photo2 = photo;
                break;
            case 3:
                photo3 = photo;
                break;
            default:
                return false;
        }
        addingTo++;
        return true;
    }

    public Photo getPhoto1() {
        return photo1;
    }

    public Photo getPhoto2() {
        return photo2;
    }

    public Photo getPhoto3() {
        return photo3;
    }

    public String getImageUrl1() {
        if(photo1 == null)
            return null;
        return photo1.getUrl(photoNumber);
    }

    public String getImageUrl2() {
        if(photo2 == null)
            return null;
        return photo2.getUrl(photoNumber);
    }

    public String getImageUrl3() {
        if(photo3 == null)
            return null;
        return photo3.getUrl(photoNumber);
    }
}
