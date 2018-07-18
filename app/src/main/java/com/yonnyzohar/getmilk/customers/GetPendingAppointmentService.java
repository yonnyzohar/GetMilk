package com.yonnyzohar.getmilk.customers;

import android.content.Context;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.yonnyzohar.getmilk.Model;
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

    public GetPendingAppointmentService(Context _applicationContext) {

        super();
        applicationContext = _applicationContext;

        database = FirebaseDatabase.getInstance();
    }

    public void getPendingAppointment()
    {
        numUnSelectedResponders = 0;
        numSelectedResponders = 0;
        appointmentNode = database.getReference("data").child(Model.DBRefs.APPOINTMENTS_IN_PROCESS).child(Model.userData.uid);
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
                            unselectedRespondersArr = new ArrayList<String>();
                            selectedRespondersArr = new ArrayList<String>();
                            for(DataSnapshot child : responders.getChildren() ){

                                String responderId = child.getKey().toString();
                                String selectedByMother = child.getValue().toString();
                                Log.d(Model.TAG, "key " + responderId + " val " + selectedByMother);
                                if(selectedByMother == "false")
                                {
                                    numUnSelectedResponders++;
                                    unselectedRespondersArr.add(responderId );
                                }
                                if(selectedByMother == "true")
                                {
                                    numSelectedResponders++;
                                    selectedRespondersArr.add(responderId );
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
}
