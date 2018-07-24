package com.yonnyzohar.getmilk.providers;

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
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.yonnyzohar.getmilk.R;
import com.yonnyzohar.getmilk.data.ContactData;
import com.yonnyzohar.getmilk.data.Model;
import com.yonnyzohar.getmilk.eventDispatcher.Event;
import com.yonnyzohar.getmilk.eventDispatcher.EventListener;
import com.yonnyzohar.getmilk.services.GetAvaliableWorkService;
import com.yonnyzohar.getmilk.services.ProviderContactsService;

import org.json.JSONException;
import org.json.JSONObject;

public class ContactsActivity extends AppCompatActivity {

    TextView noListings;


    ProviderContactsService providerContactsService;
    ListView avaliableListView;
    ContactsActivity.ListItemController listItemController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        avaliableListView = findViewById(R.id.avaliable_list_view);
        listItemController = new ContactsActivity.ListItemController();

        noListings = findViewById(R.id.no_listings);
        noListings.setVisibility(View.VISIBLE);

        providerContactsService = new ProviderContactsService(getApplicationContext());
    }

    @Override
    public void onStart() {
        super.onStart();
        providerContactsService.addListener("CONTACTS_RETRIEVED", onContactsRetreived);
        providerContactsService.getContacts(Model.userData.uid);
    }

    @Override
    public void onStop() {
        super.onStop();
        providerContactsService.removeListener("CONTACTS_RETRIEVED", onContactsRetreived);
    }

    private EventListener onContactsRetreived = new EventListener() {
        @Override
        public void onEvent(Event event) {

            if(providerContactsService.numContacts == 0)
            {
                noListings.setVisibility(View.VISIBLE);
            }
            else
            {
                noListings.setVisibility(View.GONE);
            }

            avaliableListView.setAdapter(listItemController);


        }
    };





    class ListItemController extends BaseAdapter {

        @Override
        public int getCount() {
            return providerContactsService.contactsArr.size();
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

            final ContactData obj = providerContactsService.contactsArr.get(position);

            convertView = getLayoutInflater().inflate(R.layout.interested_customer_line, null);
            TextView cityTXT = convertView.findViewById(R.id.cityTXT);
            TextView telTXT = convertView.findViewById(R.id.dateTXT);
            TextView nameTXT = convertView.findViewById(R.id.timeTXT);


            cityTXT.setText( obj.getResidence() );
            telTXT.setText( obj.getPhoneNumber() );
            nameTXT.setText( obj.getDisplayName() );


            convertView.setOnClickListener(new View.OnClickListener(){

                @Override
                public void onClick(View view) {
                    makeCall(obj.getPhoneNumber());
                    return;
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
