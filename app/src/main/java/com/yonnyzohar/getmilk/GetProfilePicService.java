package com.yonnyzohar.getmilk;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import com.yonnyzohar.getmilk.eventDispatcher.EventDispatcher;
import com.yonnyzohar.getmilk.eventDispatcher.SimpleEvent;

public class GetProfilePicService extends EventDispatcher {

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
