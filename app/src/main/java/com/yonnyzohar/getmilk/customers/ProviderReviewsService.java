package com.yonnyzohar.getmilk.customers;

import android.content.Context;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.yonnyzohar.getmilk.Model;
import com.yonnyzohar.getmilk.ReviewerData;
import com.yonnyzohar.getmilk.eventDispatcher.EventDispatcher;
import com.yonnyzohar.getmilk.eventDispatcher.SimpleEvent;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ProviderReviewsService extends EventDispatcher {

    Context applicationContext;
    FirebaseDatabase database;
    DatabaseReference reviewstNode;
    public int numReviews = 0;
    public List<ReviewerData> reviewsArr;





    public ProviderReviewsService(Context _applicationContext) {

        super();
        applicationContext = _applicationContext;
        database = FirebaseDatabase.getInstance();
    }

    public void getReviews(String providerId) {
        numReviews = 0;

        reviewstNode = database.getReference("data").child(Model.DBRefs.REVIEWS).child(providerId);
        reviewstNode.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.



                if(dataSnapshot.exists()) {

                    reviewsArr = new ArrayList<ReviewerData>();


                    //children should be an array
                    for(DataSnapshot child : dataSnapshot.getChildren() ){

                        ReviewerData data = child.getValue(ReviewerData.class);



                        numReviews++;
                        reviewsArr.add( data );



                    }

                }




                dispatchEvent(new SimpleEvent("REVIEWS_RETRIEVED"));



            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }
}
