package com.ashman.fivehundredpx.net;


import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

public class GsonRequest<T> extends Request<T> {
    private Map<String, String> addedParams;
    private Response.Listener<T> listener;
    private String body;
    private Class<T> clazz;

    public GsonRequest(int method, String url, Class<T> clazz,
                       String body, Response.Listener<T> listener,
                       Response.ErrorListener errorListener) {
        super(method, url, errorListener);
        this.clazz = clazz;
        this.body = body;
        this.listener = listener;
    }

    @Override
    protected void deliverResponse(T response) {
        listener.onResponse(response);
    }

    @Override
    protected Response<T> parseNetworkResponse(NetworkResponse response) {
        String dataString;
        try {
            dataString = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
        } catch (UnsupportedEncodingException e) {
            dataString = new String(response.data);
        }
        if(dataString == null)
            return Response.error(new VolleyError("Unable to parse response string"));

        T parsedObject;
        try {
            parsedObject = new Gson().fromJson(dataString, clazz);
        } catch (JsonSyntaxException e ){
            return Response.error(new VolleyError("Unable to parse response", e));
        }

        return Response.success(parsedObject, HttpHeaderParser.parseCacheHeaders(response));
    }

    @Override
    public byte[] getBody() {
        if(body == null)
            return null;
        return body.getBytes();
    }

    @Override
    public Map<String, String> getHeaders() {
        Map<String, String> params = null;
        try {
            params = super.getParams();
        } catch (AuthFailureError e) {
            //Ignore
        }
        if(params == null)
            params = new HashMap<String, String>();

        if(addedParams != null)
            params.putAll(addedParams);
        return params;
    }

    public void addHeader(String key, String value) {
        if(addedParams == null)
            addedParams = new HashMap<String, String>();
        addedParams.put(key, value);
    }
}
