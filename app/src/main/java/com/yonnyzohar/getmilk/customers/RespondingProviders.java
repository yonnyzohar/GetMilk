package com.yonnyzohar.getmilk.customers;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;


import com.bumptech.glide.Glide;

import com.yonnyzohar.getmilk.GameActivity;
import com.yonnyzohar.getmilk.data.Model;
import com.yonnyzohar.getmilk.services.GetPendingAppointmentService;
import com.yonnyzohar.getmilk.services.GetProviderService;
import com.yonnyzohar.getmilk.R;
import com.yonnyzohar.getmilk.eventDispatcher.Event;
import com.yonnyzohar.getmilk.eventDispatcher.EventListener;


public class RespondingProviders extends GameActivity {

    GetPendingAppointmentService pendingAppointmentService;

    ListView avaliableListView;
    RespondingProviders.ListItemController listItemController;
    TextView noListings;
    int starsArr[];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_responding_providers);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        noListings = findViewById(R.id.no_listings);
        noListings.setVisibility(View.VISIBLE);
        avaliableListView = findViewById(R.id.responders_list_view);
        listItemController = new RespondingProviders.ListItemController();


    }

    @Override
    public void onStart() {
        super.onStart();
        pendingAppointmentService = new GetPendingAppointmentService(getApplicationContext());
        pendingAppointmentService.getPendingAppointment(Model.userData.uid);
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

            if(pendingAppointmentService.numUnSelectedResponders == 0)
            {
                noListings.setVisibility(View.VISIBLE);

            }
            else
            {
                noListings.setVisibility(View.GONE);
                avaliableListView.setAdapter(listItemController);
            }


        }
    };



    class ListItemController extends BaseAdapter {

        @Override
        public int getCount() {

            if(pendingAppointmentService.unselectedRespondersArr != null)
            {
                return pendingAppointmentService.unselectedRespondersArr.size();
            }
            else
            {
                return 0;
            }


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

            convertView = getLayoutInflater().inflate(R.layout.provider_line, null);

            String providerId = pendingAppointmentService.unselectedRespondersArr.get(position);

            GetProviderService getProviderService = new GetProviderService(getApplicationContext());
            getProviderService.setConvertView(convertView);
            getProviderService.getProviderData(providerId );
            getProviderService.addListener("PROVIDER_DATA_RETRIEVED", onProviderDataRetrieved);

            return convertView;
        }

        private EventListener onProviderDataRetrieved = new EventListener() {
            @Override
            public void onEvent(Event event) {

                GetProviderService getProviderService = (GetProviderService)event.getSource();

                final GetProviderService.ProviderData dataObj = getProviderService.dataObj;

                getProviderService.removeListener("PROVIDER_DATA_RETRIEVED", onProviderDataRetrieved);
                View convertView = getProviderService.getConvertView();

                if(getProviderService.showEditProfileScreen)
                {

                }
                else
                {



                  starsArr = new int[]{R.id.star_1, R.id.star_2, R.id.star_3, R.id.star_4, R.id.star_5};

                  for(int i = 0; i < starsArr.length; i++)
                  {
                      ImageView star = convertView.findViewById( starsArr[i] );
                      star.setImageResource(R.drawable.empty_star);
                  }

                  for(int i = 0; i < dataObj.providerRatings; i++)
                  {
                      ImageView star = convertView.findViewById( starsArr[i] );
                      star.setImageResource(R.drawable.star);
                  }

                  //provider name
                  TextView nameTXT = convertView.findViewById(R.id.nameTXT);
                  nameTXT.setText( dataObj.name );

                  //provider price
                  TextView priceTXT = convertView.findViewById(R.id.priceTXT);
                  priceTXT.setText( Integer.toString(dataObj.price) + "₪");

                    TextView ibclcTXT = convertView.findViewById( R.id.is_ibclc );
                    if( dataObj.isIbclc == true )
                    {
                        ibclcTXT.setText( getResources().getString(R.string.ibclc_certified) );
                    }
                    else
                    {
                        ibclcTXT.setText( getResources().getString(R.string.not_ibclc_certified)  );
                    }

                    //image
                    final ImageView profile_image = convertView.findViewById(R.id.profile_image);
                    final GetProviderService.GetProfilePicService getProfilePicService = new GetProviderService.GetProfilePicService(getApplicationContext());
                    getProfilePicService.getImageFromDB(dataObj.providerId, "profilePic");
                    getProfilePicService.addListener("PROFILE_PIC_RETRIEVED", new EventListener()
                    {
                        @Override
                        public void onEvent(Event event) {
                            Glide.with(RespondingProviders.this).load(getProfilePicService.uri).into(profile_image);

                        }
                    });
                  //provider rating

                  //read more
                    convertView.setOnClickListener(new View.OnClickListener(){

                      @Override
                      public void onClick(View view) {
                          Intent intent = new Intent(getApplicationContext(), ProviderScreenForCustomer.class);
                          intent.putExtra("name",dataObj.name);
                          intent.putExtra("aboutMe",dataObj.aboutMe);
                          intent.putExtra("isIbclc",dataObj.isIbclc);
                          intent.putExtra("price",dataObj.price);
                          intent.putExtra("providerRatings",dataObj.providerRatings);
                          intent.putExtra("providerId",dataObj.providerId);
                          startActivity(intent);
                          return;
                      }
                  });

                }

            }
        };


    }

}
