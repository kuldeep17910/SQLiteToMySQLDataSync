package com.suriyal.sqlsyncmysql1;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class NetworkMonitor extends BroadcastReceiver
{
    // addToRequestQueue() method is used in this class and defined in MySingleton class
    @Override
    public void onReceive(final Context context, Intent intent)
    {
        Log.e("error","ok");

        if (checkNetworkConnection(context))
        {
            final DbHelper dbHelper = new DbHelper(context);
            final SQLiteDatabase database = dbHelper.getWritableDatabase();
            Cursor cursor = dbHelper.readFromLocalDatabase(database);

            while (cursor.moveToNext())
            {
                int sync_status = cursor.getInt(cursor.getColumnIndex(DbContact.SYNC_STATUS));
                //if sync_status == 1 (failed)
                if (sync_status == DbContact.SYNC_STATUS_FAILED)
                {
                    //get name from local db
                    final String Name = cursor.getString(cursor.getColumnIndex(DbContact.NAME));

                    StringRequest stringRequest = new StringRequest(Request.Method.POST, DbContact.Server_url,
                            new Response.Listener<String>()
                            {
                                @Override
                                public void onResponse(String response) {

                                    try {
                                        JSONObject jsonObject = new JSONObject(response);
                                        String Response = jsonObject.getString("response");

                                        if (Response.equals("OK"))
                                        {
                                            dbHelper.updateLocalDatabase(Name, DbContact.SYNC_STATUS_OK, database);

                                            //****p-p-p-******send broadcast to update UI & UI_UPDATE_BROADCAST is defined DbContact class
                                            context.sendBroadcast(new Intent(DbContact.UI_UPDATE_BROADCAST));
                                        }
                                    }
                                    catch (JSONException e)
                                    {
                                        e.printStackTrace();
                                    }
                                }
                            }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error)
                        {
                            Toast.makeText(context, "******Error***************", Toast.LENGTH_SHORT).show();
                        }
                    })

                    //init block start
                    {
                        @Override
                        protected Map<String, String> getParams() throws AuthFailureError
                        {
                            Map<String, String> params = new HashMap<>();
                            params.put("name", Name);
                            return params;
                        }
                    };
                    // addToRequestQueue() method is defined in MySingleton class
                    MySingleton.getInstance(context).addToRequestQueue(stringRequest);
                }
            }
            dbHelper.close();
        }
    }

    // check if the internet is available
    public boolean checkNetworkConnection(Context context)
    {
        //https://developer.android.com/training/monitoring-device-state/connectivity-status-type

        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        //kk
        if (null != networkInfo) {
            if (networkInfo.getType() == ConnectivityManager.TYPE_MOBILE)
            {
                Toast.makeText(context, "NM--- Internet is available now", Toast.LENGTH_SHORT).show();
            }
            else if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI)
            {
                Toast.makeText(context, "NM--- Wifi is enabled", Toast.LENGTH_SHORT).show();
            }
        }

        else
        {
            Toast.makeText(context, "NM--- No Internet connection", Toast.LENGTH_SHORT).show();
        }
        //kk

        return (networkInfo != null && networkInfo.isConnected());
       // return (networkInfo != null && networkInfo.isConnectedOrConnecting());
    }

}
