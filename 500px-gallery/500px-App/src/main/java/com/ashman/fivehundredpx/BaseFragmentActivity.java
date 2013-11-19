package com.ashman.fivehundredpx;


import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.ashman.fivehundredpx.api.auth.AccessToken;
import com.ashman.fivehundredpx.net.CustomPxApi;
import com.ashman.fivehundredpx.util.ViewServer;

public class BaseFragmentActivity extends ActionBarActivity {
    private RequestQueue requestQueue;
    private boolean isActivityActive;
    private CustomPxApi pxApi;

    public RequestQueue getRequestQueue() {
        if(requestQueue == null)
            requestQueue = Volley.newRequestQueue(this);

        return requestQueue;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(FiveHundredApplication.get().isDebug()) {
            ViewServer.get(this).addWindow(this);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if(FiveHundredApplication.get().isDebug()) {
            ViewServer.get(this).setFocusedWindow(this);
        }

        isActivityActive = true;

        if(pxApi != null) {
            //In case user logged in/out since the fragment was paused, update the access token
            AccessToken accessToken = FiveHundredApplication.get().getAccessToken();
            pxApi.setAccessToken(accessToken);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if(FiveHundredApplication.get().isDebug()) {
            ViewServer.get(this).removeWindow(this);
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        if(requestQueue != null)
            requestQueue.cancelAll(requestQueue);

        isActivityActive = false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public CustomPxApi getPxApi() {
        if(pxApi == null)
            pxApi = CustomPxApi.newInstance(this);
        return pxApi;
    }

    public boolean isActivityActive() {
        return isActivityActive;
    }
}
