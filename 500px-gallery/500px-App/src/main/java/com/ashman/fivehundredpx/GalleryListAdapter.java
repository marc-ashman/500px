package com.ashman.fivehundredpx;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.ashman.fivehundredpx.net.json.replies.Photo;
import com.ashman.fivehundredpx.util.Util;

import java.io.File;

public class GalleryListAdapter extends ArrayAdapter<GalleryRow> {
    private BaseFragmentActivity activity;
    private GalleryData galleryData;
    private String packageName;

    public GalleryListAdapter(BaseFragmentActivity activity,
                              int resourceId, GalleryData galleryData) {
        super(activity, resourceId, galleryData.getGalleryRows());
        this.activity = activity;
        this.galleryData = galleryData;
        packageName = FiveHundredApplication.get().getPackageName();
    }

    private void populateImageField(Photo photo, Bitmap cachedBitmap,
                                    ImageView imageView, ProgressBar progressBar) {
        if(photo == null)
            cachedBitmap = BitmapFactory.decodeResource(
                    activity.getResources(), R.drawable.no_more);
        imageView.setTag(photo);
        if(cachedBitmap == null) {
            // Don't have bitmap cached yet/anymore
            // show the progress bar and start downloading
            imageView.setVisibility(View.INVISIBLE);
            progressBar.setVisibility(View.VISIBLE);
            downloadBitmap(photo, imageView, progressBar);
        } else {
            setImageBitmap(photo, cachedBitmap, imageView, progressBar);
        }
    }

    @SuppressLint("NewApi")
    private void setImageBitmap(final Photo photo, final Bitmap bitmap,
                                final ImageView imageView, ProgressBar progressBar) {
        progressBar.setVisibility(View.GONE);
        imageView.setVisibility(View.VISIBLE);

        if(photo != null && photo.isNsfw()) {
            imageView.setImageResource(R.drawable.nsfw);
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    photo.setNsfw(false);
                    imageView.setImageBitmap(bitmap);
                    setImageClickable(photo, imageView, bitmap);
                }
            });
        } else {
            imageView.setImageBitmap(bitmap);
            if(photo != null)
                setImageClickable(photo, imageView, bitmap);
        }

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if(convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(
                    R.layout.gallery_listitem, null, false);
            //Set layout params so that initial list items are not squished.
            //Returned images are square, so it's easy to set the required height
            int height = parent.getWidth() / 3;
            ListView.LayoutParams params = new ListView.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, height);
            convertView.setLayoutParams(params);
            holder = new ViewHolder();
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        GalleryRow rowData = getItem(position);

        //Cached Bitmaps
        Bitmap cachedBitmap1 = galleryData.getCached(rowData.getImageUrl1());
        Bitmap cachedBitmap2 = galleryData.getCached(rowData.getImageUrl2());
        Bitmap cachedBitmap3 = galleryData.getCached(rowData.getImageUrl3());

        //Image Views
        holder.image1 = (ImageView) convertView.findViewById(R.id.gallery_listitem_image1);
        holder.image2 = (ImageView) convertView.findViewById(R.id.gallery_listitem_image2);
        holder.image3 = (ImageView) convertView.findViewById(R.id.gallery_listitem_image3);

        //And Finally Progress Bars
        holder.progressBar1 = (ProgressBar) convertView.findViewById(
                R.id.gallery_listitem_progressbar1);
        holder.progressBar2 = (ProgressBar) convertView.findViewById(
                R.id.gallery_listitem_progressbar2);
        holder.progressBar3 = (ProgressBar) convertView.findViewById(
                R.id.gallery_listitem_progressbar3);

        //Populate all 3 image fields
        populateImageField(rowData.getPhoto1(), cachedBitmap1, holder.image1, holder.progressBar1);
        populateImageField(rowData.getPhoto2(), cachedBitmap2, holder.image2, holder.progressBar2);
        populateImageField(rowData.getPhoto3(), cachedBitmap3, holder.image3, holder.progressBar3);

        return convertView;
    }

    private void downloadBitmap(final Photo photo, final ImageView imageView,
                                final ProgressBar progressBar) {
        int sizeToUse = FiveHundredApplication.get().getDefaultPhotoSize().getSize();
        galleryData.download(photo.getUrl(sizeToUse),
                new GalleryData.BitmapLoadedListener() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, String url) {
                if(photo.equals(imageView.getTag()))
                    setImageBitmap(photo, bitmap, imageView, progressBar);
            }
        });
    }

    public void setImageClickable(final Photo photo, final ImageView imageView,
                                  final Bitmap bitmap) {
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                File imageFile = Util.saveBitmapToLocalFile(
                            getContext().getFilesDir().getAbsolutePath(),
                            "temp", bitmap);
                int[] screenLocation = new int[2];
                view.getLocationOnScreen(screenLocation);

                final Intent subActivity = new Intent(getContext(),
                        PictureViewActivity.class);
                int orientation = getContext().getResources().getConfiguration().orientation;
                subActivity.
                        putExtra(packageName + ".orientation", orientation).
                        putExtra(packageName + ".bitmapFilename", imageFile.getName()).
                        putExtra(packageName + ".bitmapConfig", bitmap.getConfig().name()).
                        putExtra(packageName + ".left", screenLocation[0]).
                        putExtra(packageName + ".top", screenLocation[1]).
                        putExtra(packageName + ".width", view.getWidth()).
                        putExtra(packageName + ".height", view.getHeight()).
                        putExtra(packageName + ".bitmapHeight", bitmap.getHeight()).
                        putExtra(packageName + ".bitmapWidth", bitmap.getWidth()).
                        putExtra(packageName + ".photoData", photo);
                getContext().startActivity(subActivity);
                activity.overridePendingTransition(0, 0);
            }
        });
    }

    private static class ViewHolder {
        ProgressBar progressBar1;
        ProgressBar progressBar2;
        ProgressBar progressBar3;
        ImageView image1;
        ImageView image2;
        ImageView image3;
    }
}
