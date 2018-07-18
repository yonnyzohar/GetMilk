package com.yonnyzohar.getmilk.sharedScreens;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import com.yonnyzohar.getmilk.Model;
import com.yonnyzohar.getmilk.R;
import com.yonnyzohar.getmilk.customers.OrderConsultant;

public class CitiesSingleChoice extends AppCompatActivity {

    EditText chooseLocTXT;
    List<String> currCities ;
    ListView citiesListView;
    ListItemController listItemController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cities_single_choice);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        chooseLocTXT = findViewById(R.id.chooseLocTXT);
        citiesListView = findViewById(R.id.cities_list_view);
        listItemController = new ListItemController();
        placeCitiesInTempArr("");

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

    class ListItemController extends BaseAdapter {

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

            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

                @Override
                public void onCheckedChanged(CompoundButton buttonView,boolean isChecked) {
                    if(isChecked)
                    {
                        Model.userData.residence = cityStr;
                        finish();
                        //citiesDict.put(cityStr,true);
                    }
                    /*else
                    {
                        if(citiesDict.containsKey(cityStr))
                        {
                            citiesDict.remove(cityStr);
                        }

                    }*/
                }

            });

            return convertView;
        }
    }

}
