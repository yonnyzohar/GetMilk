package com.yonnyzohar.getmilk.services;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.yonnyzohar.getmilk.data.Model;
import com.yonnyzohar.getmilk.data.ReviewerData;
import com.yonnyzohar.getmilk.eventDispatcher.EventDispatcher;
import com.yonnyzohar.getmilk.eventDispatcher.SimpleEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

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
    public DatabaseReference phoneNumberNode;

    public List<ReviewerData> contactsArr;



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
        public String phoneNumber;
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
        phoneNumberNode         = providerNode.child("phoneNumber");

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

                    DataSnapshot phoneNumber = dataSnapshot.child("phoneNumber");

                    if(phoneNumber.exists())
                    {
                        dataObj.phoneNumber = phoneNumber.getValue(String.class);
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

                    DataSnapshot contacts = dataSnapshot.child("contacts");
                    if(contacts.exists())
                    {
                        contactsArr = new ArrayList<ReviewerData>();


                        //children should be an array
                        for(DataSnapshot child : contacts.getChildren() ){

                            ReviewerData data = child.getValue(ReviewerData.class);
                            contactsArr.add( data );
                        }
                    }



                }
                dispatchEvent(new SimpleEvent("PROVIDER_DATA_RETRIEVED"));

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    public View getConvertView() {
        return this.convertView;
    }

    public void setConvertView(View convertView) {
        this.convertView = convertView;
    }

    public static class GetProfilePicService extends EventDispatcher {

        Context applicationContext;
        public Uri uri;

        public GetProfilePicService(Context _applicationContext) {

            super();
            applicationContext = _applicationContext;

        }

        public void getImageFromDB(String uid, String imgName) {
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageRef = storage.getReference();
            StorageReference picRef = storageRef.child("images/"+uid+"/"+imgName+".jpg");
            picRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri _uri) {
                    uri = _uri;
                    // Got the download URL for 'users/me/profile.png'
                    //clsInstance.onImgRetreivedFromDB(uri);
                    dispatchEvent(new SimpleEvent("PROFILE_PIC_RETRIEVED"));


                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Handle any errors
                    Boolean bob = true;
                }
            });
        }
    }
}
