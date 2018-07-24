package com.yonnyzohar.getmilk.services;

import android.content.Context;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.yonnyzohar.getmilk.data.ContactData;
import com.yonnyzohar.getmilk.data.Model;
import com.yonnyzohar.getmilk.data.ReviewerData;
import com.yonnyzohar.getmilk.eventDispatcher.EventDispatcher;
import com.yonnyzohar.getmilk.eventDispatcher.SimpleEvent;

import java.util.ArrayList;
import java.util.List;

public class ProviderContactsService extends EventDispatcher{

    Context applicationContext;
    FirebaseDatabase database;
    DatabaseReference contactsNode;
    public int numContacts = 0;
    public List<ContactData> contactsArr;

    public ProviderContactsService(Context _applicationContext) {

        super();
        applicationContext = _applicationContext;
        database = FirebaseDatabase.getInstance();
    }

    public void getContacts(String providerId) {
        numContacts = 0;
        contactsArr = new ArrayList<ContactData>();

        contactsNode = database.getReference("data").child(Model.DBRefs.PROVIDERS).child(providerId).child("contacts");
        contactsNode.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.



                if(dataSnapshot.exists()) {




                    //children should be an array
                    for(DataSnapshot child : dataSnapshot.getChildren() ){

                        ContactData data = child.getValue(ContactData.class);

                        numContacts++;
                        contactsArr.add( data );
                    }

                }

                dispatchEvent(new SimpleEvent("CONTACTS_RETRIEVED"));



            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }
}
