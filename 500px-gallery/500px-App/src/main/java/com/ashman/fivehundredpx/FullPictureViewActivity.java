package com.ashman.fivehundredpx;

import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.ashman.fivehundredpx.net.json.replies.Photo;

public class FullPictureViewActivity extends BaseFragmentActivity {
    public static final String BUNDLE_PHOTO = ".photo";

    private Bitmap bitmap;
    private FullImageViewContainer container;
    private TextView errorText;
    private View loadingLayout;
    private Photo photo;

    private Response.Listener<Bitmap> requestListener = new Response.Listener<Bitmap>() {
        @Override
        public void onResponse(Bitmap response) {
            setImage(response);
        }
    };

    private Response.ErrorListener normalErrorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            errorText.setText(getString(R.string.fullpic_error));
            showView(errorText);
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_full_picture_view);

        String packageName = FiveHundredApplication.get().getPackageName();
        Bundle bundle = getIntent().getExtras();
        if(bundle == null)
            throw new IllegalStateException("Bundle with photo object required by this activity");
        photo = bundle.getParcelable(packageName + BUNDLE_PHOTO);
        if(photo == null)
            throw new IllegalStateException("Bundle with photo object required by this activity");
        container = (FullImageViewContainer) findViewById(R.id.fullpic_container);
        loadingLayout = findViewById(R.id.fullpic_loadingLayout);
        errorText = (TextView) findViewById(R.id.fullpic_errorText);
    }

    @Override
    public void onResume() {
        super.onResume();

        if(bitmap == null)
            downloadImage(photo);
        else
            setImage(bitmap);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if(bitmap != null)
            setImage(bitmap);
        else
            downloadImage(photo);
    }

    private void downloadImage(final Photo photoToDownload) {
        showView(loadingLayout);
        final String largestUrl = photoToDownload.getLargestSizeUrl();
        if(largestUrl.length() > 5 &&
                largestUrl.endsWith("1.jpg") ||
                largestUrl.endsWith("2.jpg") ||
                largestUrl.endsWith("3.jpg") ||
                largestUrl.endsWith("4.jpg")) {

            //I found out that there exists a 5.jpg image on the same photo which
            // is higher res, so try to download that one first
            String highResUrl = largestUrl.substring(0, largestUrl.length() - 5) + "5.jpg";

            Response.ErrorListener highResErrorListener = new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    downloadImage(largestUrl, requestListener, normalErrorListener);
                }
            };
            downloadImage(highResUrl, requestListener, highResErrorListener);
        } else {
            downloadImage(largestUrl, requestListener, normalErrorListener);
        }
    }

    private void downloadImage(String url, Response.Listener<Bitmap> listener,
                               Response.ErrorListener errorListener) {
        ImageRequest request = new ImageRequest(url, listener, 0, 0,
                Bitmap.Config.ARGB_8888, errorListener);
        getRequestQueue().add(request).setTag(getRequestQueue());
    }

    private void showView(View view) {
        if(view != errorText && view != loadingLayout && view != container)
            throw new IllegalArgumentException(
                    "Must be one of errorText, loadingLayout or container");
        errorText.setVisibility(View.GONE);
        loadingLayout.setVisibility(View.GONE);
        container.setVisibility(View.GONE);

        view.setVisibility(View.VISIBLE);
    }

    private void setImage(Bitmap bitmap) {
        this.bitmap = bitmap;
        int width, height;
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            Point point = new Point();
            getWindowManager().getDefaultDisplay().getSize(point);
            width = point.x;
            height = point.y;
        } else {
            width = getWindowManager().getDefaultDisplay().getWidth();
            height = getWindowManager().getDefaultDisplay().getHeight();
        }

        float scaleWidth = (float)width / (float)bitmap.getWidth();
        float scaleHeight = (float)height / (float)bitmap.getHeight();
        float minZoom = (scaleHeight > scaleWidth) ? scaleWidth : scaleHeight;

        container.setImage(bitmap, minZoom, 50);
        showView(container);
    }
}
