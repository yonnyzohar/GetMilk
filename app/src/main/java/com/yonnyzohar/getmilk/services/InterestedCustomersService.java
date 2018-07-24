package com.yonnyzohar.getmilk.services;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.yonnyzohar.getmilk.data.Model;
import com.yonnyzohar.getmilk.eventDispatcher.EventDispatcher;
import com.yonnyzohar.getmilk.eventDispatcher.SimpleEvent;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class InterestedCustomersService extends EventDispatcher{

    public List<JSONObject> appointmentsArr;
    public int count = 0;
    Context applicationContext;

    public InterestedCustomersService(Context _applicationContext) {

        super();
        applicationContext = _applicationContext;

    }

    public void getInterestedCustemers() {
        count = 0;
        RequestQueue queue = Volley.newRequestQueue(applicationContext);

        String url ="https://us-central1-testproject-103c6.cloudfunctions.net/getInterestedCustomers?providerId=" +Model.userData.uid;
        Log.d(Model.TAG, url);

        // Request a string response from the provided URL.
         StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
            new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    // Display the first 500 characters of the response string.
                    Log.d(Model.TAG, response);
                    parseResponse(response);
                }
            }, new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            Log.d(Model.TAG,"That didn't work!");
        }
    });

        queue.add(stringRequest);
}


    private void parseResponse(String response)
    {
        appointmentsArr = new ArrayList<JSONObject>();
        try {
            JSONObject obj = new JSONObject(response);
            JSONArray appointmentsArrJSON = obj.getJSONArray("appointments");


            for (int i=0; i<appointmentsArrJSON.length(); i++) {
                count++;
                appointmentsArr.add( appointmentsArrJSON.getJSONObject(i));
            }

            dispatchEvent(new SimpleEvent("INTERESTED_CUSTOMERS_RETRIEVED"));


        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
