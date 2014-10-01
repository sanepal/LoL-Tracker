package com.gta0004.loltracker.urlrequests;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

import android.content.Context;

public class ApiRequestQueue {
  private static ApiRequestQueue mInstance;
  private static Context mContext;
  private RequestQueue mRequestQueue;
  
  private ApiRequestQueue(Context context) {
    mContext = context;
  }
  
  public static synchronized ApiRequestQueue getInstance(Context context) {
    if (mInstance == null) {
      mInstance = new ApiRequestQueue(context);
    }
    return mInstance;
  }
  
  public RequestQueue getRequestQueue() {
    if (mRequestQueue == null) {
        // getApplicationContext() is key, it keeps you from leaking the
        // Activity or BroadcastReceiver if someone passes one in.
        mRequestQueue = Volley.newRequestQueue(mContext.getApplicationContext());
    }
    return mRequestQueue;
  }

  public <T> void addToRequestQueue(Request<T> req) {
      getRequestQueue().add(req);
  }

}
