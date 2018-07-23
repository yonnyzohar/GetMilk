package com.yonnyzohar.getmilk;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Set;

import com.yonnyzohar.getmilk.customers.CustomerMain;
import com.yonnyzohar.getmilk.providers.ProviderMain;

public class HomeActivity extends AppCompatActivity {

    private Button consultantBTN;
    private Button customerBTN;
    private FirebaseAuth mAuth;
    private FirebaseUser currUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        consultantBTN = findViewById(R.id.consultantBTN);
        customerBTN = findViewById(R.id.customerBTN);

        customerBTN.setVisibility(View.GONE);
        consultantBTN.setVisibility(View.GONE);

        // Check if user is signed in (non-null) and update UI accordingly.
        findViewById(R.id.sign_in_button).setVisibility(View.GONE);


        Intent intent = getIntent();
        Boolean message = intent.getBooleanExtra("signUp", false);

        if(message)
        {
            customerBTN.setVisibility(View.VISIBLE);
            consultantBTN.setVisibility(View.VISIBLE);


            customerBTN.setOnClickListener(new View.OnClickListener(){

                @Override
                public void onClick(View view) {
                    //set the user of type customer
                    //create an entry in the db under the uuid

                    Model.userType = Model.DBRefs.CUSTOMERS;

                    showAlert();


                    return;
                }
            });

            consultantBTN.setOnClickListener(new View.OnClickListener(){

                @Override
                public void onClick(View view) {
                    Log.d(Model.TAG, "CONSTULTANT CLICKED");
                    Model.userType = Model.DBRefs.PROVIDERS;
                    showAlert();

                    return;
                }
            });

        }
        else
        {
            launchNextScreen();
        }



    }

    private void showAlert() {
        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert);
        } else {
            builder = new AlertDialog.Builder(this);
        }
        builder.setTitle(R.string.approve_choice);

        if(Model.userType == Model.DBRefs.CUSTOMERS)
        {
            builder.setMessage(R.string.user_is_customer);
        }
        else
        {
            builder.setMessage(R.string.user_is_consultant);
        }

        builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                consultantBTN.setOnClickListener(null);
                customerBTN.setOnClickListener(null);
                createDBEntryForUserIfNonExitent();
            }
        });
        builder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // do nothing
            }
        });
        builder.setIcon(android.R.drawable.ic_dialog_alert);
        builder.show();
    }

    private void createDBEntryForUserIfNonExitent() {

        mAuth = FirebaseAuth.getInstance();
        currUser = mAuth.getCurrentUser();

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference myRef = database.getReference("data").child(Model.userType).child(currUser.getUid());
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                String value = dataSnapshot.getValue(String.class);
                if(value == null)
                {
                    myRef.setValue(true);
                }
                else
                {
                    //user entry already exists in db
                    Log.d(Model.TAG, "Value is: " + value);
                }

                launchNextScreen();



            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(Model.TAG, "Failed to read value.", error.toException());
            }
        });


    }


    private void launchNextScreen() {

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();


        String phoneNumber  = user.getPhoneNumber();
        Boolean launchPhoneActivity = false;

        if(Methods.isEmulator())
        {
            launchPhoneActivity = false;
        }
        else
        {
            if(phoneNumber == null || "".equals(phoneNumber))
            {
                launchPhoneActivity = true;
            }
        }

        if ( launchPhoneActivity  )
        {
            Intent intent = new Intent(HomeActivity.this, VerifyPhone.class);
            startActivity(intent);
            finish();
        }
        else
        {
            if(Model.userType == Model.DBRefs.CUSTOMERS)
            {
                Intent intent = new Intent(HomeActivity.this, CustomerMain.class);//OrderConsultant
                //intent.putExtra("userName", "fucking moron");
                startActivity(intent);
            }
            else
            {
                Intent intent = new Intent(HomeActivity.this, ProviderMain.class);

                //check if we got here through a notification
                Bundle extras = getIntent().getExtras();
                 if (extras != null)
                 {
                     Set<String> keys = extras.keySet();
                     for (String key : keys) {
                         Log.d(Model.TAG, "Bundle Contains: key=" + key);
                     }
                 }

                //intent.putExtra("seeActivity", true); -- firebase messages dont give me intent extras so this always goes to seeJobs
                startActivity(intent);
            }
        }
    }

}
