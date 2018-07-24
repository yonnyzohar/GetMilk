package com.yonnyzohar.getmilk.customers;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.yonnyzohar.getmilk.R;
import com.yonnyzohar.getmilk.data.HistoryData;
import com.yonnyzohar.getmilk.data.Model;
import com.yonnyzohar.getmilk.eventDispatcher.Event;
import com.yonnyzohar.getmilk.eventDispatcher.EventListener;
import com.yonnyzohar.getmilk.providers.InterestedCustmersActivity;
import com.yonnyzohar.getmilk.services.GetCustomerService;
import com.yonnyzohar.getmilk.services.GetProviderService;
import com.yonnyzohar.getmilk.services.InterestedCustomersService;

import org.json.JSONException;
import org.json.JSONObject;

public class CustomerHistoryActivity extends AppCompatActivity {

    ListView avaliableListView;
    CustomerHistoryActivity.ListItemController listItemController;
    TextView noListings;

    GetCustomerService getCustomerService;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_history);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        avaliableListView = findViewById(R.id.avaliable_list_view);
        listItemController = new CustomerHistoryActivity.ListItemController();

        noListings = findViewById(R.id.no_listings);
        noListings.setVisibility(View.VISIBLE);

        getCustomerService = new GetCustomerService(getApplicationContext());



    }

    @Override
    public void onStart() {
        super.onStart();
        getCustomerService.getCustomerData(Model.userData.uid);
        getCustomerService.addListener("CUSTOMER_RETRIEVED", onCustomerRetrieved);
    }

    private EventListener onCustomerRetrieved = new EventListener() {
        @Override
        public void onEvent(Event event) {
            getCustomerService.removeListener("CUSTOMER_RETRIEVED", onCustomerRetrieved);
            if (getCustomerService.dataObj.historyArr != null && getCustomerService.dataObj.historyArr.size() > 0) {

                noListings.setVisibility(View.GONE);
            } else {
                noListings.setVisibility(View.VISIBLE);
            }
            avaliableListView.setAdapter(listItemController);
        }
    };


    @Override
    public void onStop() {
        super.onStop();
        getCustomerService.removeListener("CUSTOMER_RETRIEVED", onCustomerRetrieved);
    }





    class ListItemController extends BaseAdapter {

        @Override
        public int getCount() {
            return getCustomerService.dataObj.historyArr.size();
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

            HistoryData obj = getCustomerService.dataObj.historyArr.get(position);

            convertView = getLayoutInflater().inflate(R.layout.customer_history_line, null);
            TextView phoneTXT = convertView.findViewById(R.id.phoneTXT);
            TextView dateTXT = convertView.findViewById(R.id.dateTXT);
            TextView nameTXT = convertView.findViewById(R.id.nameTXT);

            dateTXT.setText( obj.getDate() );
            phoneTXT.setText( obj.getPhoneNumber() );
            nameTXT.setText( obj.getProviderName() );

            final String number = obj.getPhoneNumber();

            ImageView callBTN = convertView.findViewById(R.id.callBTN);

            ImageView editBTN = convertView.findViewById(R.id.editBTN);

            callBTN.setOnClickListener(new View.OnClickListener(){

                @Override
                public void onClick(View view) {
                    makeCall(number);
                    return;
                }
            });

            editBTN.setOnClickListener(new View.OnClickListener(){

                @Override
                public void onClick(View view) {
                    return;
                }
            });

            final ImageView profile_image = convertView.findViewById(R.id.profile_image);
            final GetProviderService.GetProfilePicService getProfilePicService = new GetProviderService.GetProfilePicService(getApplicationContext());
            getProfilePicService.getImageFromDB(obj.getProviderId(), "profilePic");
            getProfilePicService.addListener("PROFILE_PIC_RETRIEVED", new EventListener()
            {
                @Override
                public void onEvent(Event event) {
                    Glide.with(CustomerHistoryActivity.this).load(getProfilePicService.uri).into(profile_image);

                }
            });

            return convertView;
        }
    }


    private void makeCall(String phoneNumber) {

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

}
