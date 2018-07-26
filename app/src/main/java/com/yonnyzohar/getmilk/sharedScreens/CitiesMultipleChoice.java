package com.yonnyzohar.getmilk.sharedScreens;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.yonnyzohar.getmilk.Methods;
import com.yonnyzohar.getmilk.data.Model;
import com.yonnyzohar.getmilk.R;

public class CitiesMultipleChoice extends AppCompatActivity {

    DatabaseReference citiesNode;
    FirebaseDatabase database;
    DatabaseReference myRef;
    Map<String, Boolean> citiesDict ;
    EditText chooseLocTXT;
    List<String> currCities ;
    ListView citiesListView;
    ListItemController listItemController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cities);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        citiesDict = new HashMap<String, Boolean>();

        chooseLocTXT = findViewById(R.id.chooseLocTXT);

        citiesListView = findViewById(R.id.cities_list_view);
        listItemController = new ListItemController();


        placeCitiesInTempArr("");

        database = FirebaseDatabase.getInstance();
        myRef = database.getReference("data").child(Model.DBRefs.PROVIDERS).child(Model.userData.uid);
        citiesNode = myRef.child("cities");

        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                Object value = dataSnapshot.getValue();

                if(Boolean.class.isAssignableFrom(value.getClass()))
                {

                }
                else{
                    DataSnapshot cities = dataSnapshot.child("cities");
                    if(cities.exists())
                    {

                        for (DataSnapshot postSnapshot: cities.getChildren()) {
                            String cityStr = postSnapshot.getValue(String.class);
                            Methods.log(Model.TAG, cityStr);
                            citiesDict.put(cityStr, true);
                        }
                    }

                    citiesListView.setAdapter(listItemController);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        chooseLocTXT.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s) {}

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String currCityStr = chooseLocTXT.getText().toString();
                placeCitiesInTempArr(currCityStr);
            }
        });
    }

    private void placeCitiesInTempArr(String str) {
        currCities = new ArrayList<String>();

        if( "".equals(str))
        {
            for (int i = 0; i < Model.citiesArr.size(); i++) {
                currCities.add(Model.citiesArr.get(i));
            }
        }
        else
        {
            for (int i = 0; i < Model.citiesArr.size(); i++) {
                String city = Model.citiesArr.get(i);

                if( city.contains( str ))
                {
                    currCities.add( city );
                }

            }


        }

        citiesListView.setAdapter(listItemController);


    }

    @Override
    public void onBackPressed() {

        ArrayList<String> ar = new ArrayList<String>();

        for (String key : citiesDict.keySet()) {
            ar.add(key);
        }
        citiesNode.setValue(ar);
        super.onBackPressed();

    }

    class ListItemController extends BaseAdapter{

        @Override
        public int getCount() {
            return currCities.size();
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
            convertView = getLayoutInflater().inflate(R.layout.cities_list_item, null);
            CheckBox checkBox = convertView.findViewById(R.id.city_check_box);
            TextView cityName = convertView.findViewById(R.id.city_name);
            final String cityStr = currCities.get(position);
            cityName.setText( cityStr );

            if(citiesDict.containsKey(cityStr))
            {
                checkBox.setChecked(true);
            }

            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

                @Override
                public void onCheckedChanged(CompoundButton buttonView,boolean isChecked) {
                    if(isChecked)
                    {
                        citiesDict.put(cityStr,true);
                    }
                    else
                    {
                        if(citiesDict.containsKey(cityStr))
                        {
                            citiesDict.remove(cityStr);
                        }

                    }
                }

            });

            return convertView;
        }
    }



}
