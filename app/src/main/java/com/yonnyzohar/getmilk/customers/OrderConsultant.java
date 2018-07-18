package com.yonnyzohar.getmilk.customers;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.widget.Toolbar;
import android.location.Address;
import android.location.Geocoder;
import android.os.Build;
import android.os.Bundle;

import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.yonnyzohar.getmilk.GameActivity;
import com.yonnyzohar.getmilk.Model;
import com.yonnyzohar.getmilk.R;
import com.yonnyzohar.getmilk.sharedScreens.CitiesSingleChoice;

public class OrderConsultant extends GameActivity {

    //private PlaceAutocompleteFragment autocompleteFragment;


    private Button orderConsultantBTN;
    DatabaseReference customerNode;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    private Date appointmentDate;

    TimePicker timePicker;

    private int hour = -1;
    private int minute = -1;
    TextView selectedCityTXT;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_order_consultant);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        customerNode = database.getReference("data").child(Model.DBRefs.CUSTOMERS).child(Model.userData.uid);



        // Get the Intent that started this activity and extract the string
        Intent intent = getIntent();
        String message = intent.getStringExtra("userName");

        selectedCityTXT = findViewById(R.id.selectedCityTXT);



    }



    @Override
    public void onStop(){
        super.onStop();
    }

    @Override
        public void onStart() {
            super.onStart();

        /*autocompleteFragment = (PlaceAutocompleteFragment) getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {

                String address = place.getAddress().toString();

                if ("".equals(address))
                {
                    return;
                }

                Model.userData.address = address;
                ((EditText)autocompleteFragment.getView().findViewById(R.id.place_autocomplete_search_input)).setText(Model.userData.address);


                ((EditText)autocompleteFragment.getView().findViewById(R.id.place_autocomplete_search_input)).setText(Model.userData.address);

                try{
                    String cityName = getCityNameByCoordinates(place.getLatLng().latitude, place.getLatLng().longitude);
                    Model.userData.city = cityName;

                    Map location = new HashMap();
                    location.put("address", Model.userData.address );
                    location.put("vicinity", cityName);
                    location.put("lat", place.getLatLng().latitude);
                    location.put("long", place.getLatLng().longitude);


                    setLocationObjectOnUser(location);

                }catch (IOException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onError(Status status) {
                Log.i(Model.TAG, "An error occurred: " + status);
            }
        });*/



        orderConsultantBTN = findViewById(R.id.oder_consultant_btn);
        orderConsultantBTN.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {

                verifyOrderDetails();
                return;
            }
        });

        timePicker = findViewById(R.id.timePicker);
        timePicker.setCurrentHour(Calendar.getInstance().get(Calendar.HOUR_OF_DAY));
        timePicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
            @Override
            public void onTimeChanged(TimePicker view, int hourOfDay, int minuteOfHour) {
                hour = hourOfDay;
                minute = minuteOfHour;
            }
        });

        if(Model.userData.residence != null)
        {
            selectedCityTXT.setText(Model.userData.residence);
            DatabaseReference residence = customerNode.child("residence");
            residence.setValue(Model.userData.residence);
        }

        selectedCityTXT.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                Intent intent = new Intent(OrderConsultant.this, CitiesSingleChoice.class);//OrderConsultant
                startActivity(intent);
                return;
            }
        });

    }

    private void setAppointment(String dateStr, long epochTime) {

        Map appointmentReq = new HashMap();
        appointmentReq.put("costumerId", Model.userData.uid );
        appointmentReq.put("date", dateStr);
        appointmentReq.put("dateEpoch", epochTime);
        appointmentReq.put("location", Model.userData.residence);

        String minuteStr = String.valueOf(minute);
        if(minute < 10)
        {
            minuteStr = "0"+minute;
        }

        String timeStr = hour + ":" + minuteStr;

        appointmentReq.put("time", timeStr);
        appointmentReq.put("status", Model.AppointmentTypes.PENDING);//pending / confirmed / complete(move to history)

        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date currentDate = new Date();

        appointmentReq.put("creationTime", currentDate.getTime());



        DatabaseReference appointmentNode = database.getReference("data").child(Model.DBRefs.APPOINTMENTS_IN_PROCESS).child(Model.userData.uid);
        appointmentNode.setValue(appointmentReq);

        Intent intent = new Intent(getApplicationContext(), CustomerMain.class);
        startActivity(intent);

    }

    private void verifyOrderDetails() {

        Boolean allGood = true;
        String errorMessage = null;


        if (Model.userData.residence == null || "".equals(Model.userData.residence))
        {
            errorMessage = "residence not selected";
            allGood = false;
        }
        if(appointmentDate == null)
        {
            errorMessage = "Date not selected";
            allGood = false;
        }

        if(hour == -1 || minute == -1)
        {
            errorMessage = "Time not selected";
            allGood = false;
        }
        else
        {
            Calendar cal = Calendar.getInstance();
            int curHour = cal.get(Calendar.HOUR_OF_DAY);
            int curMinute = cal.get(Calendar.MINUTE);
            int curDate = cal.get(Calendar.DATE);

            if(appointmentDate != null)
            {


                int date = appointmentDate.getDate();
                if(date == curDate)
                {
                    if(hour < curHour || (hour == curHour && minute <= curMinute))
                    {
                        errorMessage = " Invalid Time";
                        allGood = false;

                    }
                }
            }
        }

        if(!allGood && errorMessage != null)
        {
            Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
        }
        else
        {
            AlertDialog.Builder builder;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                builder = new AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert);
            } else {
                builder = new AlertDialog.Builder(this);
            }
            builder.setTitle(R.string.approve_appointment);

            String minuteStr = String.valueOf(minute);
            if(minute < 10)
            {
                minuteStr = "0"+minute;
            }

            DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
            final String dateStr = df.format(appointmentDate);

            String appointMentDetails = dateStr + ", " + Model.userData.residence + ", at " + hour + ":" + minuteStr;

            builder.setMessage(appointMentDetails);

            builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    setAppointment(dateStr, appointmentDate.getTime());
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

    }



    public void onRadioButtonClicked(View view) {
        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();

        Date dt = new Date();

        Calendar calendar = Calendar.getInstance();

        long secondsSinceEpoch = calendar.getTimeInMillis() / 1000L;


        // Check which radio button was clicked
        switch(view.getId()) {
            case R.id.today_btn:
                if (checked)
                    // Pirates are the best
                    break;
            case R.id.tomorrow_btn:
                if (checked)
                    calendar.add(Calendar.DATE, 1);
                    // Ninjas rule
                    break;
            case R.id.after_tomorrow_btn:
                if (checked)
                    // Pirates are the best
                    calendar.add(Calendar.DATE, 2);
                    break;
        }


        appointmentDate = calendar.getTime();



    }

    private void setLocationObjectOnUser(Map location) {

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("data").child(Model.DBRefs.CUSTOMERS).child(Model.userData.uid).child("location");
        myRef.setValue(location);
    }

    String getCityNameByCoordinates(double lat, double lon) throws IOException {

        Locale lHebrew = new Locale("he");
        Geocoder mGeocoder = new Geocoder(this, lHebrew);
        List<Address> addresses = mGeocoder.getFromLocation(lat, lon, 1);
        if (addresses != null && addresses.size() > 0) {
            return addresses.get(0).getLocality();
        }
        return null;
    }



}
