package com.yonnyzohar.getmilk;

import android.content.Context;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import com.yonnyzohar.getmilk.customers.GetCustomerService;
import com.yonnyzohar.getmilk.eventDispatcher.EventDispatcher;
import com.yonnyzohar.getmilk.eventDispatcher.SimpleEvent;
import android.view.View;

import java.util.HashMap;
import java.util.Map;

public class GetProviderService extends EventDispatcher{

    Context applicationContext;

    FirebaseDatabase database;

    DatabaseReference providerNode;
    public DatabaseReference acceptingBidsNode;
    public DatabaseReference ratingsNode;
    public DatabaseReference fireBaseMessagingTokenNode;

    public DatabaseReference priceNisNode;
    public DatabaseReference aboutMeNode;
    public DatabaseReference imgURLNode;
    public DatabaseReference ibclcNode;
    public DatabaseReference displayNameNode;
    public DatabaseReference numConsultationsNode;
    public DatabaseReference numFreeLeadsLeftNode;



    private View convertView;
    public Boolean showEditProfileScreen = false;




    public class ProviderData extends Object{
        public String aboutMe;
        public String name;
        public Boolean isIbclc;
        public int price;
        public double providerRatings = 0;
        public String providerId;
        public Boolean acceptingBids;
        public int numFreeLeadsLeft;
    }

    public ProviderData dataObj;



    public GetProviderService( Context _applicationContext) {

        // Exists only to defeat instantiation.
        super();
        applicationContext = _applicationContext;
        database = FirebaseDatabase.getInstance();
        dataObj = new ProviderData();
    }



    public void getProviderData(String _providerId)
    {
        dataObj.providerId = _providerId;
        providerNode        = database.getReference("data").child(Model.DBRefs.PROVIDERS).child( _providerId );
        fireBaseMessagingTokenNode = providerNode.child("fireBaseMessagingToken");
        acceptingBidsNode       = providerNode.child("acceptingBids");
        ratingsNode             = providerNode.child("ratings");
        priceNisNode            = providerNode.child("priceNIS");
        aboutMeNode             = providerNode.child("aboutMe");
        imgURLNode              = providerNode.child("imgURL");
        numConsultationsNode    = providerNode.child("numConsultations");
        acceptingBidsNode       = providerNode.child("acceptingBids");
        displayNameNode         = providerNode.child("displayName");
        ibclcNode               = providerNode.child("ibclc");
        numFreeLeadsLeftNode    = providerNode.child("numFreeLeadsLeft");

        providerNode.addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                Object value = dataSnapshot.getValue();


                if(Boolean.class.isAssignableFrom(value.getClass()))
                {
                    Log.d(Model.TAG, "Value is a bool - > go to provider registration page");

                    showEditProfileScreen = true;

                }
                else{

                    if(Model.fireBaseMessagingToken != null)
                    {
                        fireBaseMessagingTokenNode.setValue(Model.fireBaseMessagingToken);
                    }

                    DataSnapshot numFreeLeadsLeft = dataSnapshot.child("numFreeLeadsLeft");

                    if(numFreeLeadsLeft.exists())
                    {
                        dataObj.numFreeLeadsLeft = numFreeLeadsLeft.getValue(int.class);
                    }

                    DataSnapshot aboutMeNode = dataSnapshot.child("aboutMe");

                    if(aboutMeNode.exists())
                    {
                        dataObj.aboutMe = aboutMeNode.getValue(String.class);
                    }


                    DataSnapshot ratings = dataSnapshot.child("ratings");
                    if(!ratings.exists())
                    {
                        ratingsNode.setValue(0);
                    }
                    else
                    {
                        dataObj.providerRatings = ratings.getValue(double.class);
                    }

                    DataSnapshot acceptingBidNodes = dataSnapshot.child("acceptingBids");
                    if(acceptingBidNodes.exists())
                    {
                        Object val = acceptingBidNodes.getValue();
                        dataObj.acceptingBids = (Boolean) val == true;
                    }

                    DataSnapshot priceNIS = dataSnapshot.child("priceNIS");
                    if(!priceNIS.exists())
                    {
                        showEditProfileScreen = true;
                    }
                    else
                    {
                        dataObj.price = priceNIS.getValue(int.class);

                    }

                    DataSnapshot ibclc = dataSnapshot.child("ibclc");
                    if(!ibclc.exists())
                    {
                        showEditProfileScreen = true;
                        ibclcNode.setValue(false);
                    }
                    else
                    {
                        Object val = ibclc.getValue();
                        dataObj.isIbclc = (Boolean) val == true;
                    }

                    DataSnapshot cities = dataSnapshot.child("cities");
                    if(!cities.exists())
                    {
                        showEditProfileScreen = true;
                    }

                    DataSnapshot numConsultations = dataSnapshot.child("numConsultations");
                    if(!numConsultations.exists())
                    {
                        numConsultationsNode.setValue(0);
                    }

                    DataSnapshot acceptingBids = dataSnapshot.child("acceptingBids");
                    if(!acceptingBids.exists())
                    {
                        acceptingBidsNode.setValue(false);
                    }


                    DataSnapshot displayName = dataSnapshot.child("displayName");
                    if(displayName.exists())
                    {
                        dataObj.name = displayName.getValue().toString();
                    }
                    else
                    {
                        showEditProfileScreen = true;
                        displayNameNode.setValue(Model.userData.name);
                    }



                }
                dispatchEvent(new SimpleEvent("PROVIDER_DATA_RETRIEVED"));

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    //redo this in node!!!
    public void callMade(String providerId, GetCustomerService.CustomerData dataObj, int numFreeLeadsLeft) {

        Map userData = new HashMap();
        userData.put("uid" , Model.userData.name);
        userData.put( "displayName", dataObj.displayName);
        userData.put("phoneNumber", dataObj.phoneNumber);

        Map data = new HashMap();
        data.put(dataObj.uid , userData);

        providerNode        = database.getReference("data").child(Model.DBRefs.PROVIDERS).child( providerId );
        DatabaseReference retrievedNumbersNode = providerNode.child("retrievedNumbers");
        retrievedNumbersNode.updateChildren(data);

        numFreeLeadsLeftNode    = providerNode.child("numFreeLeadsLeft");
        numFreeLeadsLeftNode.setValue(numFreeLeadsLeft);

    }

    public View getConvertView() {
        return this.convertView;
    }

    public void setConvertView(View convertView) {
        this.convertView = convertView;
    }
}
