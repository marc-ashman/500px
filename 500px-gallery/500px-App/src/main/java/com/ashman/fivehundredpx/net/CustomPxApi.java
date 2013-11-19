package com.ashman.fivehundredpx.net;


import android.app.Activity;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.ashman.fivehundredpx.FiveHundredApplication;
import com.ashman.fivehundredpx.R;
import com.ashman.fivehundredpx.api.auth.AccessToken;
import com.ashman.fivehundredpx.net.json.GetRequest;
import com.ashman.fivehundredpx.net.json.PostRequest;

import org.apache.http.Header;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.methods.HttpUriRequest;

import oauth.signpost.OAuth;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;

public class CustomPxApi {
    private static final String TAG = "PxApiHelper";
    private static String HOST = "https://api.500px.com/v1";

    public static final String PHOTOS_URL = "/photos";


    private AccessToken accessToken;
    private String consumerKey;
    private String consumerSecret;

    //convenience function
    public static CustomPxApi newInstance(Activity activity) {
        return new CustomPxApi(FiveHundredApplication.get().getAccessToken(),
                activity.getString(R.string.oauth_consumer_key),
                activity.getString(R.string.oauth_consumer_secret));
    }

    public CustomPxApi(AccessToken accessToken, String consumerKey,
                 String consumerSecret) {
        super();
        if(consumerKey == null || consumerSecret == null)
            throw new IllegalArgumentException("consumerKey and consumerSecret cannot be null");
        this.accessToken = accessToken;
        this.consumerKey = consumerKey;
        this.consumerSecret = consumerSecret;
    }

    public void setAccessToken(AccessToken accessToken) {
        this.accessToken = accessToken;
    }

    public boolean get(RequestQueue requestQueue, Class clazz,
                      GetRequest requestObject, Response.Listener listener,
                      Response.ErrorListener errorListener) {

        String finalUrl = HOST + requestObject.getUrl() + requestObject.getQuery();
        Header[] oauthHeaders = null;

        if(accessToken != null) {
            //Create HttpGet for the purpose of using signpost lib to acquire oauth headers.
            // Then fetch header and attach to volley request instead
            HttpRequestBase fakeRequest = new HttpGet(finalUrl);
            if(!signRequest(fakeRequest))
                return false;
            oauthHeaders = fakeRequest.getHeaders(OAuth.HTTP_AUTHORIZATION_HEADER);
        } else {
            //Bug exists in PxApi.java
            boolean hasQuery = finalUrl.contains("?");
            if(hasQuery)
                finalUrl = String.format("%s&consumer_key=%s", finalUrl, this.consumerKey);
            else
                finalUrl = String.format("%s?consumer_key=%s", finalUrl, this.consumerKey);
        }

        GsonRequest<?> request = new GsonRequest(Request.Method.GET, finalUrl,
                clazz, null, listener, errorListener);
        if(oauthHeaders != null) {
            for(Header header : oauthHeaders) {
                request.addHeader(header.getName(), header.getValue());
            }
        }
        requestQueue.add(request).setTag(requestQueue);

        return true;
    }

    public boolean post(RequestQueue requestQueue, Class clazz,
                        PostRequest requestObject, Response.Listener listener,
                        Response.ErrorListener errorListener) {
        if(accessToken == null)
            return false;

        String finalUrl = HOST + requestObject.getUrl();
        //Create HttpGet for the purpose of using signpost lib to acquire oauth headers.
        // Then fetch header and attach to volley request instead
        HttpRequestBase fakeRequest = new HttpPost(finalUrl);
        if(!signRequest(fakeRequest))
            return false;
        Header[] oauthHeaders = fakeRequest.getHeaders(OAuth.HTTP_AUTHORIZATION_HEADER);

        GsonRequest<?> request = new GsonRequest(Request.Method.POST, finalUrl,
                clazz, requestObject.getBody(), listener, errorListener);
        if(oauthHeaders != null) {
            for(Header header : oauthHeaders) {
                request.addHeader(header.getName(), header.getValue());
            }
        }
        requestQueue.add(request).setTag(requestQueue);

        return true;
    }

//    public JSONObject post(String url, List<? extends NameValuePair> params) {
//        HttpPost request = new HttpPost(HOST + url);
//        try {
//            request.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
//        } catch (UnsupportedEncodingException e) {
//            Log.e(TAG, "Parameters in post are invalid", e);
//        }
//        return handleSigned(request);
//    }

    //TODO: handle exceptions better, see if token expiration is handled here
    private boolean signRequest(HttpUriRequest request) {
        try {
            CommonsHttpOAuthConsumer consumer = new CommonsHttpOAuthConsumer(
                    consumerKey, consumerSecret);
            consumer.setTokenWithSecret(accessToken.getToken(),
                    accessToken.getTokenSecret());
            consumer.sign(request);
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error trying to sign the request.", e);
            return false;
        }
    }
}
