package com.yonnyzohar.getmilk.services;

import android.content.Context;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.yonnyzohar.getmilk.data.Model;
import com.yonnyzohar.getmilk.eventDispatcher.EventDispatcher;
import com.yonnyzohar.getmilk.eventDispatcher.SimpleEvent;

import java.util.Map;


public class GetCustomerService  extends EventDispatcher {

    Context applicationContext;
    FirebaseDatabase database;
    DatabaseReference customerNode;
    DatabaseReference fireBaseMessagingTokenNode;


    public Boolean customerExists = false;


    public class CustomerData extends Object{
        public String residence;
        public String displayName;
        public String email;
        public String photoURL;
        public String uid;
        public String phoneNumber;
    }

    public GetCustomerService.CustomerData dataObj;


    public GetCustomerService(Context _applicationContext) {

        super();
        applicationContext = _applicationContext;

        database = FirebaseDatabase.getInstance();
        dataObj = new GetCustomerService.CustomerData();
    }

    public void getCustomerData(String customerId)
    {

        customerNode = database.getReference("data").child(Model.DBRefs.CUSTOMERS).child(customerId);
        fireBaseMessagingTokenNode = customerNode.child("fireBaseMessagingToken");
        customerNode.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.

                if(dataSnapshot.exists()) {

                    Object value = dataSnapshot.getValue();
                    if(Boolean.class.isAssignableFrom(value.getClass()))
                    {

                    }
                    else
                    {
                        customerExists = true;

                        DataSnapshot phoneNumberNode = dataSnapshot.child("phoneNumber");
                        if(phoneNumberNode.exists())
                        {
                            dataObj.phoneNumber = phoneNumberNode.getValue(String.class);
                        }

                        DataSnapshot residenceNode = dataSnapshot.child("residence");
                        if(residenceNode.exists())
                        {
                            dataObj.residence = residenceNode.getValue(String.class);
                        }

                        DataSnapshot emailNode = dataSnapshot.child("email");
                        if(residenceNode.exists())
                        {
                            dataObj. email = emailNode.getValue(String.class);
                        }

                        DataSnapshot displayNameNode = dataSnapshot.child("displayName");
                        if(displayNameNode.exists())
                        {
                            dataObj.displayName = displayNameNode.getValue(String.class);
                        }

                        DataSnapshot photoURLNode = dataSnapshot.child("photoURL");
                        if(photoURLNode.exists())
                        {
                            dataObj.photoURL = photoURLNode.getValue(String.class);
                        }

                    }


                }

                dispatchEvent(new SimpleEvent("CUSTOMER_RETRIEVED"));



            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void setCustomerNode(Map data) {
        customerNode.setValue(data);
    }

    public void setMessagingToken(String messagingToken) {
        fireBaseMessagingTokenNode.setValue(messagingToken);
    }
}
