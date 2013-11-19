package com.ashman.fivehundredpx.net.json.requests;


import com.ashman.fivehundredpx.enums.GalleryCategory;
import com.ashman.fivehundredpx.net.CustomPxApi;
import com.ashman.fivehundredpx.net.json.GetRequest;

import java.net.URLEncoder;


public class PhotoStreamRequest implements GetRequest {

    private String feature = "popular";
    private String only;
    private String sort = "rating";
    private int page;
    private int rpp;
    private int image_size[];

    public PhotoStreamRequest(GalleryCategory category, int page,
                              int picturesPerPage, int[] imageSize) {
        this.only = category.getName();
        this.page = page;
        this.rpp = picturesPerPage;
        this.image_size = imageSize;
    }

    public String getQuery() {
        String query = "?feature=" + encode(feature) + "&only=" + encode(only) + "&sort=" +
                encode(sort) + "&page=" + page + "&rpp=" + rpp;
        if(image_size.length == 1)
            return query + "&image_size=" + image_size[0];
        else {
            for(int size : image_size) {
                query += "&image_size[]=" + size;
            }
            return query;
        }
    }

    public String getUrl() {
        return CustomPxApi.PHOTOS_URL;
    }

    private String encode(String string) {
        try {
            return URLEncoder.encode(string, "utf-8");
        } catch (Exception e) {
            return string;
        }
    }
}
