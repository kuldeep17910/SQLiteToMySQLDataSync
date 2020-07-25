package com.suriyal.sqlsyncmysql1;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    EditText Name;
    RecyclerView.LayoutManager layoutManager;
    RecyclerAdapter adapter;
    ArrayList<Contact> arrayList = new ArrayList<>();

    BroadcastReceiver broadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recyclerView);
        Name = findViewById(R.id.name);

        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
        adapter = new RecyclerAdapter(arrayList);


        recyclerView.setAdapter(adapter);

        // this method will load data from local db
        readFromLocalStorage();

        broadcastReceiver = new BroadcastReceiver()
        {
            @Override
            public void onReceive(Context context, Intent intent)
            {
                Toast.makeText(context, "Intent********Detected.", Toast.LENGTH_LONG).show();

                //when we send broadcast from network monitor class this method will get invoked
                readFromLocalStorage();

            }
        };
//broadcastReceiver=new NetworkMonitor();
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        //register broadcast   ***//https://developer.android.com/guide/components/broadcasts

        registerReceiver(broadcastReceiver, new IntentFilter(DbContact.UI_UPDATE_BROADCAST));

        // IntentFilter filter = new IntentFilter(DbContact.UI_UPDATE_BROADCAST);
        //registerReceiver(broadcastReceiver, filter, Manifest.permission.INTERNET, null );
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        //unregister broadcast
        unregisterReceiver(broadcastReceiver);
    }

    /*@Override
    protected void onResume() {
        super.onResume();
        registerReceiver(broadcastReceiver, new IntentFilter(DbContact.UI_UPDATE_BROADCAST));
        Toast.makeText(this, "on resume --------------", Toast.LENGTH_SHORT).show();
    }*/

    // Taking input from user and button click event
    public void submitName(View view) {

        String name = Name.getText().toString();
        saveToAppserver(name);
        Name.setText("");
    }

    private void saveToAppserver(final String name) {

        //if internet is available
        if (checkNetworkConnection()) {

            StringRequest stringRequest = new StringRequest(Request.Method.POST, DbContact.Server_url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response)
                        {
                            try {
                                JSONObject jsonObject = new JSONObject(response);
                                String Response = jsonObject.getString("response");
                                if (Response.equals("OK"))
                                {
                                    //save to local & cloud server if internet is available
                                    saveToLocalStorage(name, DbContact.SYNC_STATUS_OK);
                                }
                                else
                                    {
                                    //save to local storage
                                    saveToLocalStorage(name, DbContact.SYNC_STATUS_FAILED);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    //save to local storage
                    saveToLocalStorage(name, DbContact.SYNC_STATUS_FAILED);//1
                }
            }) {
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    Map<String, String> params = new HashMap<>();
                    params.put("name", name);
                    return params;
                }
            };

            // addToRequestQueue() is in MySingleton class
            MySingleton.getInstance(MainActivity.this).addToRequestQueue(stringRequest);


        }//if close

        //if internet is not available save to local server or device
        else
            {
            saveToLocalStorage(name, DbContact.SYNC_STATUS_FAILED);//1
            }

    }

    public void saveToLocalStorage(String name, int sync) {
        DbHelper dbHelper = new DbHelper(this);
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        dbHelper.saveToLocalDatabase(name, sync, database);

        Toast.makeText(this, "M--Locally Saved----Now reading from Local Device", Toast.LENGTH_SHORT).show();

        readFromLocalStorage();
        dbHelper.close();
    }


    private void readFromLocalStorage()
    {
        arrayList.clear();
        DbHelper dbHelper = new DbHelper(this);
        SQLiteDatabase database = dbHelper.getReadableDatabase();

        Cursor cursor = dbHelper.readFromLocalDatabase(database);

        while (cursor.moveToNext())
        {
            String name = cursor.getString(cursor.getColumnIndex(DbContact.NAME));
            int sync_status = cursor.getInt(cursor.getColumnIndex(DbContact.SYNC_STATUS));
            arrayList.add(new Contact(name, sync_status)); //Contact class role...............

        }
        //refresh recycler view
        adapter.notifyDataSetChanged();
        Toast.makeText(this, "***M***refresh recycler view--", Toast.LENGTH_SHORT).show();
        cursor.close();
        dbHelper.close();
    }


    public boolean checkNetworkConnection() {
        ConnectivityManager connectivityManager = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        //kk
        if (null != networkInfo) {
            if (networkInfo.getType() == ConnectivityManager.TYPE_MOBILE)
            {
                Toast.makeText(this, "Internet is available now!", Toast.LENGTH_SHORT).show();
            }
            else if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI)
            {
                Toast.makeText(this, "Wifi is enabled now", Toast.LENGTH_SHORT).show();
            }
        }

        else
        {
            Toast.makeText(this, "No Internet connection...please check", Toast.LENGTH_SHORT).show();

        }
        //kk

        return (networkInfo != null && networkInfo.isConnected());

        //return (networkInfo != null && networkInfo.isConnectedOrConnecting());
    }



}