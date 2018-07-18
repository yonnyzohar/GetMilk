package com.yonnyzohar.getmilk.providers;

import android.content.Context;
import android.util.Log;
import android.view.View;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import com.yonnyzohar.getmilk.Model;
import com.yonnyzohar.getmilk.eventDispatcher.Event;
import com.yonnyzohar.getmilk.eventDispatcher.EventDispatcher;
import com.yonnyzohar.getmilk.eventDispatcher.SimpleEvent;

public class GetAvaliableWorkService extends EventDispatcher {


    DatabaseReference appointmentsNode;
    DatabaseReference citiesNode;
    FirebaseDatabase database;
    DatabaseReference providerNode;
    String citiesReq = "?cities=";
    Context applicationContext;
    public List<JSONObject> appointmentsArr;
    public int count = 0;


    public GetAvaliableWorkService(Context _applicationContext) {

        super();
        applicationContext = _applicationContext;


    }

    public void getAvaliableWork()
    {
        count = 0;
        database = FirebaseDatabase.getInstance();
        appointmentsNode = database.getReference("data").child(Model.DBRefs.PROVIDERS).child(Model.userData.uid);
        providerNode = database.getReference("data").child(Model.DBRefs.PROVIDERS).child(Model.userData.uid);
        citiesNode = providerNode.child("cities");
        providerNode.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                Object value = dataSnapshot.getValue();


                if(Boolean.class.isAssignableFrom(value.getClass()))
                {

                }
                else{
                    DataSnapshot cities = dataSnapshot.child("cities");
                    if(cities.exists())
                    {

                        for (DataSnapshot postSnapshot: cities.getChildren()) {
                            String cityStr = postSnapshot.getValue(String.class);
                            citiesReq += cityStr + ",";
                        }

                        citiesReq = trimLastComma(citiesReq);
                        citiesReq += "&providerId="+Model.userData.uid;
                        citiesReq += "&rnd="+ (Math.random() * 100);

                        sendAvaliableWorkRequest();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private String trimLastComma(String str) {
        if (str != null && str.length() > 0 && str.charAt(str.length() - 1) == ',') {
            str = str.substring(0, str.length() - 1);
        }
        return str;
    }

    public void sendAvaliableWorkRequest() {
        RequestQueue queue = Volley.newRequestQueue(applicationContext);

        //"https://us-central1-testproject-103c6.cloudfunctions.net/getAppointments?cities=חולון,תל אביב"
        String url ="https://us-central1-testproject-103c6.cloudfunctions.net/getAppointments" + citiesReq;
        Log.d(Model.TAG, url);

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Display the first 500 characters of the response string.
                        Log.d(Model.TAG, response);
                        parseAvaliableWorkResponse(response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(Model.TAG,"That didn't work!");
            }
        });

        queue.add(stringRequest);
    }


    private void parseAvaliableWorkResponse(String response)
    {
        //{"appointments":[{"costumerId":"7Mql2T5LtTV3bYXyQKbm3Juv4622","date":"Sun, Mar 4, '18","location":"חולון","recipients":true,"status":0,"time":"22:34"}]}

        try {
            JSONObject obj = new JSONObject(response);
            JSONArray appointmentsArrJSON = obj.getJSONArray("appointments");
            appointmentsArr = new ArrayList<JSONObject>();

            for (int i=0; i<appointmentsArrJSON.length(); i++) {
                count++;
                appointmentsArr.add( appointmentsArrJSON.getJSONObject(i));
            }

            dispatchEvent(new SimpleEvent("AVALIABLE_WORK_RETREIVED"));


        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
