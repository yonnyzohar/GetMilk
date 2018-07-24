package com.yonnyzohar.getmilk.providers;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.yonnyzohar.getmilk.services.GetProviderService;
import com.yonnyzohar.getmilk.data.Model;
import com.yonnyzohar.getmilk.R;
import com.yonnyzohar.getmilk.services.GetCustomerService;
import com.yonnyzohar.getmilk.eventDispatcher.Event;
import com.yonnyzohar.getmilk.eventDispatcher.EventListener;
import com.yonnyzohar.getmilk.services.InterestedCustomersService;

import org.json.JSONException;
import org.json.JSONObject;

public class InterestedCustmersActivity extends AppCompatActivity {

    InterestedCustomersService interestedCustomersService;
    ListView avaliableListView;
    InterestedCustmersActivity.ListItemController listItemController;
    TextView noListings;

    int numFreeLeadsLeft;
    GetCustomerService getCustomerService;
    GetProviderService getProviderService;

    String selectedCustomer = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_interested_custmers);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        avaliableListView = findViewById(R.id.avaliable_list_view);
        listItemController = new InterestedCustmersActivity.ListItemController();

        noListings = findViewById(R.id.no_listings);
        noListings.setVisibility(View.VISIBLE);

        interestedCustomersService = new InterestedCustomersService(getApplicationContext());
        getProviderService = new GetProviderService(getApplicationContext());

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            numFreeLeadsLeft = extras.getInt("numFreeLeadsLeft");
        }

    }

    @Override
    public void onStart() {
        super.onStart();
        interestedCustomersService.addListener("INTERESTED_CUSTOMERS_RETRIEVED", onInterestedCustomersRetreived);
        interestedCustomersService.getInterestedCustemers();
    }

    private EventListener onInterestedCustomersRetreived = new EventListener() {
        @Override
        public void onEvent(Event event) {
            interestedCustomersService.removeListener("INTERESTED_CUSTOMERS_RETRIEVED", onInterestedCustomersRetreived);
            if (interestedCustomersService.count == 0) {
                noListings.setVisibility(View.VISIBLE);
            } else {
                noListings.setVisibility(View.GONE);
            }
            avaliableListView.setAdapter(listItemController);
        }
    };


    @Override
    public void onStop() {
        super.onStop();
        interestedCustomersService.removeListener("INTERESTED_CUSTOMERS_RETRIEVED", onInterestedCustomersRetreived);
    }





    class ListItemController extends BaseAdapter {

        @Override
        public int getCount() {
            return interestedCustomersService.appointmentsArr.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }


        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            JSONObject obj = interestedCustomersService.appointmentsArr.get(position);

            convertView = getLayoutInflater().inflate(R.layout.interested_customer_line, null);
            TextView cityTXT = convertView.findViewById(R.id.cityTXT);
            TextView dateTXT = convertView.findViewById(R.id.dateTXT);
            TextView timeTXT = convertView.findViewById(R.id.timeTXT);

            String cityStr = "";
            String dateStr = "";
            String timeStr = "";
            String customerId ="";


            try {
                cityStr = obj.getString("location");
                dateStr = obj.getString("date");
                timeStr = obj.getString("time");
                customerId = obj.getString("costumerId");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            cityTXT.setText( cityStr );
            dateTXT.setText( dateStr );
            timeTXT.setText( timeStr );

            final String localCustomer = customerId;

            convertView.setOnClickListener(new View.OnClickListener(){

                @Override
                public void onClick(View view) {
                    selectedCustomer = localCustomer;
                    onLineClicked();
                    return;
                }
            });

            return convertView;
        }
    }

    private void onLineClicked() {

        getCustomerService = new GetCustomerService(getApplicationContext());
        getCustomerService.getCustomerData(selectedCustomer);
        getCustomerService.addListener("CUSTOMER_RETRIEVED", onCustomerRetrieved);
    }

    private EventListener onCustomerRetrieved = new EventListener() {

        @Override
        public void onEvent(Event event) {
            getCustomerService.removeListener("CUSTOMER_RETRIEVED", onCustomerRetrieved);

            if(getCustomerService.customerExists == true)
            {
                if(numFreeLeadsLeft > 0)
                {
                    numFreeLeadsLeft--;
                    makeCall(getCustomerService.dataObj.phoneNumber);
                }
                else
                {
                    //pay dialouge
                }

            }
            else
            {
                //something went wrong, no customer
            }

        }

    };

    private void makeCall(String phoneNumber) {

        callMade(Model.userData.uid, getCustomerService.dataObj);

        Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + phoneNumber));
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phoneNumber));
        }
        startActivity(intent);
    }

    private void callMade(String uid, GetCustomerService.CustomerData dataObj) {


        String str = "?customerId="+dataObj.uid+
                     "&providerId="+Model.userData.uid +
                     "&displayName=" +dataObj.displayName +
                     "&phoneNumber=" + dataObj.phoneNumber+
                     "&residence="+dataObj.residence;
        RequestQueue queue = Volley.newRequestQueue(this);

        String url = Model.reqPrefix + "onProviderGotCustomerNumber" + str;

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(Model.TAG, response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(Model.TAG,"That didn't work!");
            }
        });

        queue.add(stringRequest);
    }


}
