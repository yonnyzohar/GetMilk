package com.yonnyzohar.getmilk.providers;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.yonnyzohar.getmilk.R;
import com.yonnyzohar.getmilk.eventDispatcher.Event;
import com.yonnyzohar.getmilk.eventDispatcher.EventListener;

import org.json.JSONException;
import org.json.JSONObject;

public class InterestedCustmersActivity extends AppCompatActivity {

    InterestedCustomersService interestedCustomersService;
    ListView avaliableListView;
    InterestedCustmersActivity.ListItemController listItemController;
    TextView noListings;
    String  customerId;

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

    }

    private EventListener onInterestedCustomersRetreived = new EventListener() {
        @Override
        public void onEvent(Event event) {
            interestedCustomersService.removeListener("INTERESTED_CUSTOMERS_RETRIEVED", onInterestedCustomersRetreived);
            if(interestedCustomersService.count == 0)
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
        interestedCustomersService.addListener("INTERESTED_CUSTOMERS_RETRIEVED", onInterestedCustomersRetreived);
        interestedCustomersService.getInterestedCustemers();
    }

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

            convertView.setOnClickListener(new View.OnClickListener(){

                @Override
                public void onClick(View view) {
                    onLineClicked(customerId);
                    return;
                }
            });

            return convertView;
        }
    }

    private void onLineClicked(String customerId) {

    }

}
