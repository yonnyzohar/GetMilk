package com.yonnyzohar.getmilk.customers;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


import java.util.HashMap;
import java.util.Map;

import com.yonnyzohar.getmilk.GameActivity;
import com.yonnyzohar.getmilk.HomeActivity;
import com.yonnyzohar.getmilk.Methods;
import com.yonnyzohar.getmilk.Model;
import com.yonnyzohar.getmilk.R;
import com.yonnyzohar.getmilk.VerifyPhone;
import com.yonnyzohar.getmilk.eventDispatcher.Event;
import com.yonnyzohar.getmilk.eventDispatcher.EventListener;

public class CustomerMain extends GameActivity {



    Button orderConsultantBTN;
    Button respondingConsultantsBTN;

    GetPendingAppointmentService pendingAppointmentService;
    TextView respondingConsultantsTXT;
    GetCustomerService getCustomerService;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            // User is signed in

            Model.userData.name = user.getDisplayName();
            Model.userData.uid = user.getUid();
            Model.userData.email = user.getEmail();
            Model.userData.phoneNumber = user.getPhoneNumber();
            Model.userData.photoUrl  = user.getPhotoUrl().toString();

        }
        orderConsultantBTN = findViewById(R.id.order_consultant_btn);
        respondingConsultantsTXT = findViewById(R.id.respondingConsultantsTXT);


        respondingConsultantsBTN        = findViewById(R.id.available_consultants_btn);
        respondingConsultantsBTN.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                //set the user of type customer
                //create an entry in the db under the uuid
                //edit_cities_btn.setOnClickListener(null);
                Intent intent = new Intent(getApplicationContext(), RespondingProviders.class);
                startActivity(intent);
                return;
            }
        });


        getInitialValues();

    }

    @Override
    public void onStart() {
        super.onStart();
        pendingAppointmentService = new GetPendingAppointmentService(getApplicationContext());
        pendingAppointmentService.getPendingAppointment();
        pendingAppointmentService.addListener("PENDING_APPOINTMENT_RETRIEVED", onPendingAppointmentRetrieved);

    }

    @Override
    public void onStop() {
        super.onStop();
        pendingAppointmentService.removeListener("PENDING_APPOINTMENT_RETRIEVED", onPendingAppointmentRetrieved);

    }

    private EventListener onPendingAppointmentRetrieved = new EventListener() {

        @Override
        public void onEvent(Event event) {
            pendingAppointmentService.removeListener("PENDING_APPOINTMENT_RETRIEVED", onPendingAppointmentRetrieved);

            if(pendingAppointmentService.appointmentSet)
            {
                orderConsultantBTN.setText( getResources().getString(R.string.cancel_consultation) );
            }
            else
            {
                orderConsultantBTN.setText( getResources().getString(R.string.order_consultation) );
            }


            orderConsultantBTN.setOnClickListener(new View.OnClickListener(){

                @Override
                public void onClick(View view) {

                  if(pendingAppointmentService.appointmentSet)
                  {
                      appointmentExists(pendingAppointmentService.setLocation, pendingAppointmentService.setDate);
                  }
                  else
                  {
                      goToAppointmentScreen();
                  }
                }
            });

            if(pendingAppointmentService.numUnSelectedResponders == 0)
            {
                respondingConsultantsTXT.setVisibility(View.INVISIBLE);
            }
            else
            {
                respondingConsultantsTXT.setVisibility(View.VISIBLE);
                respondingConsultantsTXT.setText(Integer.toString(pendingAppointmentService.numUnSelectedResponders));
            }

        }
    };



    private void getInitialValues() {

        getCustomerService = new GetCustomerService(getApplicationContext());
        getCustomerService.getCustomerData(Model.userData.uid);
        getCustomerService.addListener("CUSTOMER_RETRIEVED", onCustomerRetrieved);


    }

    private EventListener onCustomerRetrieved = new EventListener() {

        @Override
        public void onEvent(Event event) {
            getCustomerService.removeListener("CUSTOMER_RETRIEVED", onCustomerRetrieved);

            if(getCustomerService.customerExists == false)
            {
                Map data = new HashMap();
                data.put("displayName",Model.userData.name);
                data.put("email",Model.userData.email);
                data.put("photoURL", Model.userData.photoUrl);
                data.put("uid", Model.userData.uid);

                if(Model.fireBaseMessagingToken != null)
                {
                    data.put("fireBaseMessagingToken", Model.fireBaseMessagingToken);
                }


                if (Model.userData.phoneNumber == null || "".equals(Model.userData.phoneNumber))
                {
                    Boolean launchPhoneActivity = false;

                    if(Methods.isEmulator())
                    {
                        launchPhoneActivity = false;
                    }
                    else
                    {
                        launchPhoneActivity = true;
                    }

                    if ( launchPhoneActivity  )
                    {
                        Intent intent = new Intent(CustomerMain.this, VerifyPhone.class);
                        startActivity(intent);
                        finish();
                        return;
                    }
                }
                else
                {
                    data.put("phoneNumber", Model.userData.phoneNumber);
                }

                getCustomerService.setCustomerNode(data);

            }
            else
            {
                if(Model.fireBaseMessagingToken != null)
                {
                    getCustomerService.setMessagingToken(Model.fireBaseMessagingToken);

                }


                if(getCustomerService.dataObj.residence != null)
                {
                    Model.userData.residence = getCustomerService.dataObj.residence;
                }
            }

        }

    };




    private void goToAppointmentScreen() {

        Intent intent = new Intent(CustomerMain.this, OrderConsultant.class);//OrderConsultant
        startActivity(intent);
    }

    private void appointmentExists(String location, String date) {

        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert);
        } else {
            builder = new AlertDialog.Builder(this);
        }
        builder.setTitle(R.string.erase_appointment);

        String str2 = location + " " + date+ " ";
        String str3 = getResources().getString(R.string.appointment_exists_details_end) ;

        String msg =  str2 +str3;

        builder.setMessage(msg);

        builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                deleteAppointment();

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

    private void deleteAppointment() {
        pendingAppointmentService.deletePendingAppointment();
        orderConsultantBTN.setText( getResources().getString(R.string.order_consultation) );
        //goToAppointmentScreen();
    }

}
