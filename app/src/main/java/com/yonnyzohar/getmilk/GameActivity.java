package com.yonnyzohar.getmilk;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.yonnyzohar.getmilk.data.Model;

/**
 * Created by yonny on 17/04/2018.
 */

public class GameActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }


    public void getMessagingToken()
    {
        FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener( GameActivity.this,  new OnSuccessListener<InstanceIdResult>() {
            @Override
            public void onSuccess(InstanceIdResult instanceIdResult) {
                Model.fireBaseMessagingToken = instanceIdResult.getToken();

            }
        });
    }
}
