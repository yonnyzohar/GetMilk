package com.yonnyzohar.getmilk.services;

import android.content.Context;
import android.util.Log;

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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.yonnyzohar.getmilk.data.Model;
import com.yonnyzohar.getmilk.eventDispatcher.EventDispatcher;
import com.yonnyzohar.getmilk.eventDispatcher.SimpleEvent;

public class GetPendingAppointmentService extends EventDispatcher {

    Context applicationContext;
    FirebaseDatabase database;
    DatabaseReference appointmentNode;
    public Boolean appointmentSet = false;
    public String setDate;
    public String setLocation;

    public List<String> unselectedRespondersArr;
    public int numUnSelectedResponders = 0;

    public List<String> selectedRespondersArr;
    public int numSelectedResponders = 0;

    public String chosenProviderId;

    public GetPendingAppointmentService(Context _applicationContext) {

        super();
        applicationContext = _applicationContext;

        database = FirebaseDatabase.getInstance();
    }

    public void getPendingAppointment(String customerId)
    {
        numUnSelectedResponders = 0;
        numSelectedResponders = 0;
        chosenProviderId = null;
        unselectedRespondersArr = new ArrayList<String>();
        selectedRespondersArr = new ArrayList<String>();
        appointmentNode = database.getReference("data").child(Model.DBRefs.APPOINTMENTS_IN_PROCESS).child(customerId);
        appointmentNode.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.

                if(dataSnapshot.exists())
                {
                    Object value = dataSnapshot.getValue();


                    if(Boolean.class.isAssignableFrom(value.getClass()))
                    {

                    }
                    else{

                        String date = dataSnapshot.child("dateEpoch").getValue().toString();
                        long appointmentEpoch = Long.parseLong(date);

                        Date currentDate = new Date();
                        Long currentEpoch = currentDate.getTime();

                        if(currentEpoch > appointmentEpoch)
                        {
                            appointmentSet = false;
                            appointmentNode.removeValue();
                        }
                        else
                        {
                            appointmentSet = true;
                            setDate     = dataSnapshot.child("date").getValue().toString();
                            setLocation = dataSnapshot.child("location").getValue().toString();
                        }


                        DataSnapshot responders = dataSnapshot.child("responders");
                        if(responders.exists())
                        {

                            for(DataSnapshot child : responders.getChildren() ){

                                String responderId = child.getKey().toString();
                                String selectedByMother = child.getValue().toString();
                                Log.d(Model.TAG, "key " + responderId + " val " + selectedByMother);
                                if(selectedByMother == "false" || "false".equals(selectedByMother))
                                {
                                    numUnSelectedResponders++;
                                    unselectedRespondersArr.add(responderId );
                                }
                                if(selectedByMother == "true" || "true".equals(selectedByMother))
                                {
                                    numSelectedResponders++;
                                    selectedRespondersArr.add(responderId );
                                }
                                if(selectedByMother == "done" || "done".equals(selectedByMother))
                                {
                                    chosenProviderId = responderId;
                                }


                            }

                        }
                    }
                }
                else
                {
                    appointmentSet = false;
                }

                dispatchEvent(new SimpleEvent("PENDING_APPOINTMENT_RETRIEVED"));



            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void deletePendingAppointment() {
        appointmentNode.removeValue();
        appointmentSet = false;
        setDate = null;
        setLocation = null;
        numUnSelectedResponders = 0;
        numSelectedResponders = 0;
    }

    public void resolveAppointment(GetProviderService.ProviderData providerData, String customerId) {

        String str = "?providerName=" +providerData.name;
        str += "&phoneNumber=" +providerData.phoneNumber;
        str += "&providerId=" +providerData.providerId;
        str += "&date=" +setDate;
        str += "&location=" +setLocation;
        str += "&customerId=" +customerId;

        RequestQueue queue = Volley.newRequestQueue(applicationContext);

        String url = Model.reqPrefix + "onCustomerMarkedAppointmentResolved" + str;
        Log.d(Model.TAG, url);

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Display the first 500 characters of the response string.
                        Log.d(Model.TAG, response);
                        dispatchEvent(new SimpleEvent("PENDING_APPOINTMENT_RESOLVED"));
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(Model.TAG,"That didn't work!");
                dispatchEvent(new SimpleEvent("PENDING_APPOINTMENT_RESOLVED"));
            }
        });

        queue.add(stringRequest);
        deletePendingAppointment();


    }
}
