package com.suriyal.sqlsyncmysql1;

import android.content.Context;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

// using this class in saveToAppserver() method in main activity
// and also using in onReceive() method in NetworkMonitor class which extends BroadcastReceiver
//https://developer.android.com/training/volley/requestqueue?authuser=1
public class MySingleton
{
    private static MySingleton mInstance;
    private RequestQueue requestQueue;
    private static Context mCtx;

    private MySingleton(Context context)
    {
        mCtx = context;
        requestQueue = getRequestQueue();
    }

    private RequestQueue getRequestQueue()
    {
        if (requestQueue == null)
        {
            // getApplicationContext() is key, it keeps you from leaking the
            // Activity or BroadcastReceiver if someone passes one in.
            requestQueue = Volley.newRequestQueue(mCtx.getApplicationContext());
        }
        return requestQueue;
    }
    public static synchronized MySingleton getInstance(Context context)
    {

        if (mInstance == null)
        {
            mInstance = new MySingleton(context);
        }
        return mInstance;
    }


    public <T> void addToRequestQueue(Request<T> request)
    {
        getRequestQueue().add(request);
    }

}
