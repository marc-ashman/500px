package com.ashman.fivehundredpx.net.json.requests;


import com.ashman.fivehundredpx.net.CustomPxApi;
import com.ashman.fivehundredpx.net.json.GetRequest;

public class PhotoRequest implements GetRequest {
    private int id;

    public PhotoRequest(int photoId) {
        this.id = photoId;
    }

    public String getQuery() {
        return "";
    }

    public String getUrl() {
        return CustomPxApi.PHOTOS_URL + "/" + id;
    }
}
