package com.ashman.fivehundredpx.net.json.replies;


public class PhotoStreamReply {
    private int current_page;
    private int total_pages;
    private int total_items;
    private Photo[] photos;

    public int getCurrent_page() {
        return current_page;
    }

    public int getTotal_pages() {
        return total_pages;
    }

    public int getTotal_items() {
        return total_items;
    }

    public Photo[] getPhotos() {
        return photos;
    }
}
