package com.ashman.fivehundredpx;


import android.app.Activity;
import android.app.ActivityManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.ashman.fivehundredpx.enums.GalleryCategory;
import com.ashman.fivehundredpx.enums.PhotoSize;
import com.ashman.fivehundredpx.net.json.replies.Image;
import com.ashman.fivehundredpx.net.json.replies.Photo;
import com.ashman.fivehundredpx.net.json.replies.PhotoStreamReply;
import com.ashman.fivehundredpx.net.json.requests.PhotoStreamRequest;

public class GalleryFragment extends Fragment {
    private static final String TAG = "GalleryFragment";
    private static final int PICS_PER_PAGE_MULTIPLIER = 10;
    private static final int PICS_PER_PAGE = GalleryRow.IMAGES_PER_ROW * PICS_PER_PAGE_MULTIPLIER;
    private static final int DEFAULT_CURRENT_PAGE = 1;
    private static final int DEFAULT_NEXT_PAGE = -1;

    private ListView list;
    private TextView errorText;
    private TextView downloadingMoreText;
    private View loadingLayout;
    private View listLayout;
    private ArrayAdapter<GalleryRow> listAdapter;

    private GalleryData galleryData;
    private GalleryCategory currentCategory = FiveHundredApplication.DEFAULT_CATEGORY;
    private int currentPage;
    private int nextPageToDownload;
    private boolean isEndOfList;

    public static GalleryFragment findOrCreateNewInstance(FragmentManager fragmentManager) {
        GalleryFragment fragment = (GalleryFragment) fragmentManager.findFragmentByTag(TAG);
        if(fragment == null)
            return new GalleryFragment();
        return fragment;
    }

    public GalleryFragment() {
        reset();
    }

    private void reset() {
        if(galleryData != null)
            galleryData.clear();
        currentPage = DEFAULT_CURRENT_PAGE;
        nextPageToDownload = DEFAULT_NEXT_PAGE;
        isEndOfList = false;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        if(galleryData == null) {
            ActivityManager activityManager = (ActivityManager)
                    activity.getSystemService(Activity.ACTIVITY_SERVICE);
            int maxCache = activityManager.getMemoryClass() / 4;
            galleryData = new GalleryData(((BaseFragmentActivity)activity).getRequestQueue(), maxCache);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_gallery, container, false);
        errorText = (TextView) root.findViewById(R.id.gallery_errorText);
        loadingLayout = root.findViewById(R.id.gallery_loadingLayout);
        listLayout = root.findViewById(R.id.gallery_listLayout);
        downloadingMoreText = (TextView) root.findViewById(R.id.gallery_listDownloadingMore);
        list = (ListView) root.findViewById(R.id.gallery_list);
        list.setAdapter(createListAdapter());
        list.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView listView, int i) {
            }

            @Override
            public void onScroll(AbsListView listView, int firstVisibleItem,
                                 int visibleItemCount, int totalItemCount) {
                if((firstVisibleItem + visibleItemCount) == totalItemCount) {
                    if(isEndOfList) {
                        downloadingMoreText.setVisibility(View.VISIBLE);
                        downloadingMoreText.setText(R.string.gallery_noMoreImages);
                    } else {
                        getNextGalleryPage();
                    }
                }
            }
        });

        photoSize = FiveHundredApplication.get().getDefaultPhotoSize();

        if(nextPageToDownload == nextPageToDownload)
            getNextGalleryPage();
        return root;
    }

    private void getNextGalleryPage() {
        //TODO: handle 'last' page more gracefully
        if(nextPageToDownload > currentPage)
            return;
        if(isEndOfList)
            return;
        nextPageToDownload = currentPage + 1;

        RequestQueue requestQueue = ((BaseFragmentActivity)getActivity()).getRequestQueue();
        Response.Listener<PhotoStreamReply> listener = new Response.Listener<PhotoStreamReply>() {
            @Override
            public void onResponse(PhotoStreamReply response) {
                Photo[] photos = response.getPhotos();
                for(Photo photo : photos) {
                    Image image = photo.getImages()[0];
                    galleryData.download(image.getUrl(), null);
                    galleryData.addToRow(photo);
                }
                if(currentPage == DEFAULT_CURRENT_PAGE)
                    showView(listLayout);
                downloadingMoreText.setVisibility(View.GONE);
                currentPage++;
                listAdapter.notifyDataSetChanged();

                if(response.getCurrent_page() == response.getTotal_pages())
                    isEndOfList = true;
            }
        };
        Response.ErrorListener errorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if(currentPage == DEFAULT_CURRENT_PAGE)
                    showView(errorText);
                else {
                    downloadingMoreText.setText(getString(R.string.gallery_errorGettingPage));
                    isEndOfList = true;
                }
            }
        };

        PhotoStreamRequest request = new PhotoStreamRequest(currentCategory,
                currentPage, PICS_PER_PAGE, new int[] { 1, 2, 3, 4 });

        ((BaseFragmentActivity)getActivity()).getPxApi().get(
                requestQueue, PhotoStreamReply.class, request, listener, errorListener);
        downloadingMoreText.setText(R.string.gallery_downloadingMore);
        downloadingMoreText.setVisibility(View.VISIBLE);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Retain instance so the cache and listview don't download destroyed in configuration changes
        setRetainInstance(true);
    }

    @Override
    public void onPause() {
        super.onPause();

        //If request is still pending, it has been cancelled in super.onPause()
        //reset next page counter so it is redownloaded on resume
        if(nextPageToDownload == currentPage + 1)
            nextPageToDownload -= 1;
    }

    @Override
    public void onResume() {
        super.onResume();

        PhotoSize newPhotoSize = FiveHundredApplication.get().getDefaultPhotoSize();
        if(photoSize != newPhotoSize) {
            photoSize= newPhotoSize;
            galleryData.reset();
            listAdapter.notifyDataSetChanged();
            reset();
        }

        if(galleryData.getGalleryRows().size() > 0) {
            showView(listLayout);
            listAdapter.notifyDataSetChanged();
        } else {
            getNextGalleryPage();
            showView(loadingLayout);
        }
    }

    private PhotoSize photoSize;

    public void showView(View view) {
        if(view != errorText && view != loadingLayout && view != listLayout)
            throw new IllegalArgumentException(
                    "Must be one of errorText, loadingLayout or list");
        errorText.setVisibility(View.GONE);
        loadingLayout.setVisibility(View.GONE);
        listLayout.setVisibility(View.GONE);

        view.setVisibility(View.VISIBLE);
    }

    public ArrayAdapter<GalleryRow> createListAdapter() {
        listAdapter = new GalleryListAdapter((BaseFragmentActivity)getActivity(),
                0, galleryData);
        return listAdapter;
    }

    public void setCurrentCategory(GalleryCategory category) {
        if(currentCategory != category) {
            currentCategory = category;
            showView(loadingLayout);
            reset();
            listAdapter.notifyDataSetChanged();
            getNextGalleryPage();
        }
    }
}
