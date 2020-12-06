package com.example.firstappad;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

public class ApiCall {
    private static ApiCall mInstance;
    private RequestQueue mRequestQueue;
    private static Context mCtx;
    public ApiCall(Context ctx) {
        mCtx = ctx;
        mRequestQueue = getRequestQueue();
    }
    public static synchronized ApiCall getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new ApiCall(context);
        }
        return mInstance;
    }
    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(mCtx.getApplicationContext());
        }
        return mRequestQueue;
    }
    public <T> void addToRequestQueue(Request<T> req) {
        getRequestQueue().add(req);
    }
    public static void make(Context ctx, String query, Response.Listener<String>
            listener, Response.ErrorListener errorListener) {
        String url = "https://akansha-hw9-backend.wm.r.appspot.com/autoComplete?term="+query;
        //String url = "https://api.tiingo.com/tiingo/utilities/search?query=" + query + "&token=0d4d0b16d5d78a6e4e7a93f93aa219b215827d30";//"http://localhost:3000/autocomplete?term=aaa";

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                listener, errorListener);

        ApiCall.getInstance(ctx).addToRequestQueue(stringRequest);

    }
}