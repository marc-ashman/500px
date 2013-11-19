package com.ashman.fivehundredpx;


import android.graphics.Bitmap;
import android.support.v4.util.LruCache;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.ashman.fivehundredpx.net.json.replies.Photo;

import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;

public class GalleryData {
    private LruCache<String, Bitmap> bitmapCache;
    private RequestQueue requestQueue;
    private List<GalleryRow> galleryRows;
    private GalleryRow currentRow;

    private int cacheSize;

    //private Vector<String> downloading = new Vector<String>();
    private Hashtable<String, BitmapLoadedListener> downloading =
            new Hashtable<String, BitmapLoadedListener>();

    public GalleryData(RequestQueue requestQueue, int cacheSizeInMB) {
        this.requestQueue = requestQueue;
        galleryRows = new LinkedList<GalleryRow>();
        cacheSize = cacheSizeInMB * 1024 * 1024;
        bitmapCache = new LruCache<String, Bitmap>(cacheSize) {
            protected int sizeOf(String key, Bitmap value) {
                return value.getRowBytes() * value.getHeight();
            }
        };
    }

    public void addToRow(Photo photo) {
        if(currentRow == null || !currentRow.add(photo)) {
            int sizeToUse = FiveHundredApplication.get().getDefaultPhotoSize().getSize();
            currentRow = new GalleryRow(sizeToUse);
            currentRow.add(photo);
            galleryRows.add(currentRow);
        }
    }

    public void download(final String url, final BitmapLoadedListener listener) {
        if(downloading.contains(url)) {
            if(listener != null)
                downloading.put(url, listener);
            return;
        }

        Response.Listener<Bitmap> requestListener = new Response.Listener<Bitmap>() {
            @Override
            public void onResponse(Bitmap response) {
                if(bitmapCache.get(url) == null)
                    bitmapCache.put(url, response);
                if(listener != null)
                    listener.onBitmapLoaded(response, url);
                BitmapLoadedListener loadedListener = downloading.get(url);
                if(loadedListener != null)
                    loadedListener.onBitmapLoaded(response, url);
            }
        };

        Response.ErrorListener errorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //TODO: me
            }
        };

        ImageRequest request = new ImageRequest(url, requestListener, 0, 0,
                Bitmap.Config.ARGB_8888, errorListener);
        requestQueue.add(request).setTag(requestQueue);
    }

    public Bitmap getCached(String urlKey) {
        if(urlKey == null)
            return null;
        return bitmapCache.get(urlKey);
    }

    public interface BitmapLoadedListener {
        public void onBitmapLoaded(Bitmap bitmap, String url);
    }

    public List<GalleryRow> getGalleryRows() {
        return galleryRows;
    }

    public void clear() {
        galleryRows.clear();
        currentRow = null;
    }

    public void reset() {
        galleryRows.clear();
        currentRow = null;
        bitmapCache = new LruCache<String, Bitmap>(cacheSize) {
            protected int sizeOf(String key, Bitmap value) {
                return value.getRowBytes() * value.getHeight();
            }
        };
    }
}
