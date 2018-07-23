package com.yonnyzohar.getmilk;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;

import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;


//buy this:
//https://www.shutterstock.com/image-vector/milk-labels-vector-set-splash-blot-781454341?src=zmBWrzjcaUdfeYP7VXXqbA-1-14&drawer=open
//android:background="@drawable/baby_face_24dp"
//http://www.londatiga.net/it/how-to-create-custom-window-title-in-android/ - for custom activity header( heb text align)
//http://blog.supenta.com/2014/07/02/how-to-style-alertdialogs-like-a-pro/ custom dialougs
public class MainActivity extends GameActivity{


    // [END declare_auth]

    private static final int RC_SIGN_IN = 9001;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener firebaseAuthstateListener;
    private FirebaseUser currUser;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_main);
       // Toolbar toolbar = findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);


        if (Model.citiesArr == null )
        {
            try {
                JSONObject obj = new JSONObject(loadJSONFromAsset());
                JSONArray citiesArrJSON = obj.getJSONArray("cities");
                Model.citiesArr = new ArrayList<String>();

                for (int i=0; i<citiesArrJSON.length(); i++) {
                    Model.citiesArr.add( citiesArrJSON.getString(i) );
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

        // [START initialize_auth]
        mAuth = FirebaseAuth.getInstance();
        currUser = mAuth.getCurrentUser();

        getMessagingToken();

        if(currUser == null)
        {
            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(getString(R.string.default_web_client_id))
                    .requestEmail()
                    .build();
            // [END config_signin]

            Model.mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

            Log.w(Model.TAG, "User is not currently logged in to firebase");

            //sign the user in with google
            createSignInntent();
        }
        else
        {
            Log.w(Model.TAG, "User Logged in to firebase");

            //get uuid and make sure there is an entry for the user
            //create the right intent for the saved user
            //if user is neither a customer or a provider- let him choose
            findOutIfUserIsConsultantOrCustomer();
        }

    }

    private String loadJSONFromAsset() {
        String json = null;
        try {
            InputStream is = this.getAssets().open("cities.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }



    private void findOutIfUserIsConsultantOrCustomer() {
        final AppCompatActivity self = this;
        //Model.userType

        currUser = mAuth.getCurrentUser();
        final String uuid = currUser.getUid();

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference myRef = database.getReference("data").child(Model.DBRefs.CUSTOMERS).child(uuid);


        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot1) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.

                //user is not a customer- check whether user is a supplier
                if(!dataSnapshot1.exists())
                {
                    DatabaseReference myRef2 = FirebaseDatabase.getInstance().getReference("data").child(Model.DBRefs.PROVIDERS).child(uuid);
                    myRef2.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot2) {
                            // This method is called once with the initial value and again
                            // whenever data at this location is updated.
                            if(!dataSnapshot2.exists())
                            {
                                Log.w(Model.TAG, "FUCK! User is not provider or customer");
                                launchNextScreen(true);
                            }
                            else
                            {
                                Model.userType = Model.DBRefs.PROVIDERS;
                                launchNextScreen(false);
                            }

                        }

                        @Override
                        public void onCancelled(DatabaseError error) {
                            // Failed to read value
                            Log.w(Model.TAG, "Failed to read value.", error.toException());
                        }
                    });
                }
                else
                {
                    Model.userType = Model.DBRefs.CUSTOMERS;
                    launchNextScreen(false);
                }

            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(Model.TAG, "Failed to read value.", error.toException());
            }
        });
    }

    private void launchNextScreen(Boolean signUp) {
        Intent intent = new Intent(MainActivity.this, HomeActivity.class);
        intent.putExtra("signUp", signUp);
        startActivity(intent);
        finish();
    }


    public void createSignInntent()
    {
        Intent signInIntent = Model.mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            firebaseAuthWithGoogle(account);

        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w(Model.TAG, "signInResult:failed code=" + e.getStatusCode());
        }
    }



    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(Model.TAG, "firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            //google sign in is a success - > now let the user choose for the first time what he is
                            //in case this is not the first time let's check if the user is alreadt registered aas a provider or a customer and enter the app
                            findOutIfUserIsConsultantOrCustomer();

                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(Model.TAG, "signInWithCredential:failure", task.getException());
                            Model.mGoogleSignInClient.signOut();
                            FirebaseAuth.getInstance().signOut();
                            createSignInntent();
                            //Snackbar.make(findViewById(R.id.main_layout), "Authentication Failed.", Snackbar.LENGTH_SHORT).show();
                        }

                    }

                });
    }









    //////
    private void updateLoop() {
        final Handler handler = new Handler();
        int i = 1;
        class MyRunnable implements Runnable {
            private Handler handler;
            private int i ;
            //private TextView textView;
            public MyRunnable(Handler handler, int i) {
                this.handler = handler;
                this.i = i;
                //this.textView = textView;
            }
            @Override
            public void run() {
                FirebaseUser currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser() ;

                if (currentFirebaseUser == null)
                {
                    this.handler.postDelayed(this, 500);
                    this.i++;
                    Log.d(Model.TAG, ""+this.i);
                }
                else
                {
                    Log.d(Model.TAG,  "" + currentFirebaseUser.getUid());
                }

            }
        }
        handler.post(new MyRunnable(handler, i));
    }
}
