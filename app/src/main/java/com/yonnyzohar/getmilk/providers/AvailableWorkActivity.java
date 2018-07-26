package com.yonnyzohar.getmilk.providers;

import android.os.Bundle;

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


import org.json.JSONException;
import org.json.JSONObject;

import com.yonnyzohar.getmilk.Methods;
import com.yonnyzohar.getmilk.data.Model;
import com.yonnyzohar.getmilk.R;
import com.yonnyzohar.getmilk.eventDispatcher.Event;
import com.yonnyzohar.getmilk.eventDispatcher.EventListener;
import com.yonnyzohar.getmilk.services.GetAvaliableWorkService;

public class AvailableWorkActivity extends AppCompatActivity {



    String  customerId;
    TextView noListings;


    GetAvaliableWorkService avaliableWorkService;


    ListView avaliableListView;
    AvailableWorkActivity.ListItemController listItemController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_available_work);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);



        avaliableListView = findViewById(R.id.avaliable_list_view);
        listItemController = new AvailableWorkActivity.ListItemController();

        noListings = findViewById(R.id.no_listings);
        noListings.setVisibility(View.VISIBLE);

        avaliableWorkService = new GetAvaliableWorkService(getApplicationContext());


    }

    private EventListener onAvaliableWorkRetreived = new EventListener() {
        @Override
        public void onEvent(Event event) {

            //

            if(avaliableWorkService.count == 0)
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

    @Override
    public void onStart() {
        super.onStart();
        avaliableWorkService.addListener("AVALIABLE_WORK_RETREIVED", onAvaliableWorkRetreived);
        avaliableWorkService.getAvaliableWork();
    }

    @Override
    public void onStop() {
        super.onStop();
        avaliableWorkService.removeListener("AVALIABLE_WORK_RETREIVED", onAvaliableWorkRetreived);
    }



    class ListItemController extends BaseAdapter {

        @Override
        public int getCount() {
            return avaliableWorkService.appointmentsArr.size();
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

            JSONObject obj = avaliableWorkService.appointmentsArr.get(position);

            convertView = getLayoutInflater().inflate(R.layout.avaliableappointment, null);
            TextView cityTXT = convertView.findViewById(R.id.cityTXT);
            TextView dateTXT = convertView.findViewById(R.id.dateTXT);
            TextView timeTXT = convertView.findViewById(R.id.timeTXT);

            String cityStr = "";
            String dateStr = "";
            String timeStr = "";


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

            final View con = convertView;

            convertView.setOnClickListener(new View.OnClickListener(){

                @Override
                public void onClick(View view) {
                    con.setOnClickListener(null);
                    onProviderBidsForJob(customerId);
                    return;
                }
            });

            return convertView;
        }
    }

    private void onProviderBidsForJob(String customerId) {
        RequestQueue queue = Volley.newRequestQueue(this);
        String str = "?customerId="+customerId+"&providerId="+Model.userData.uid;

        String url = Model.reqPrefix + "onProviderBidsForJob" + str;

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Display the first 500 characters of the response string.
                        Methods.log(Model.TAG, response);

                        String str = getResources().getString(R.string.notifying_mother_of_your_interest);
                        Toast.makeText(getApplicationContext(), str, Toast.LENGTH_SHORT).show();
                        avaliableWorkService.sendAvaliableWorkRequest();

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Methods.log(Model.TAG,"That didn't work!");
            }
        });

        queue.add(stringRequest);
    }

}
