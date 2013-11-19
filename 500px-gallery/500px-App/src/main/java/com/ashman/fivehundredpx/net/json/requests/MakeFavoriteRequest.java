package com.ashman.fivehundredpx.net.json.requests;


import com.ashman.fivehundredpx.net.CustomPxApi;
import com.ashman.fivehundredpx.net.json.PostRequest;

public class MakeFavoriteRequest implements PostRequest {
    private int id;

    public MakeFavoriteRequest(int id) {
        this.id = id;
    }

    public String getUrl() {
        return CustomPxApi.PHOTOS_URL + "/" + id + "/favorite";
    }

    public String getBody() {
        return null;
    }
}
