package com.yonnyzohar.getmilk.providers;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


import com.yonnyzohar.getmilk.GameActivity;
import com.yonnyzohar.getmilk.services.GetAvaliableWorkService;
import com.yonnyzohar.getmilk.services.GetProviderService;
import com.yonnyzohar.getmilk.data.Model;
import com.yonnyzohar.getmilk.R;
import com.yonnyzohar.getmilk.eventDispatcher.Event;
import com.yonnyzohar.getmilk.eventDispatcher.EventListener;
import com.yonnyzohar.getmilk.services.InterestedCustomersService;

//if provider has finished signup- send to
public class ProviderMain extends GameActivity {

    Button appointmentsHistoryBTN;
    Button seeReviewsBTN ;
    Button editDetailsBTN;
    Button availableWorkBTN ;
    Button interestedMothersBTN;

    GameActivity self;
    Switch availabilityToggleBTN ;
    ImageView profile_image;

    TextView reqNotifications;
    TextView interestedMothersNotifications;

    GetAvaliableWorkService avaliableWorkService;
    GetProviderService getProviderService;
    GetProviderService.GetProfilePicService getProfilePicService;

    InterestedCustomersService interestedCustomersService;

    int[] starsArr;


    /*
    mContext = profile_image.getContext();

    // ----------------------------------------------------------------
    // apply rounding to image
    // see: https://github.com/vinc3m1/RoundedImageView
    // ----------------------------------------------------------------
    Transformation transformation = new RoundedTransformationBuilder()
            .borderColor(getResources().getColor(R.color.my_special_orange))
            .borderWidthDp(5)
            .cornerRadiusDp(50)
            .oval(false)
            .build();

        Picasso.with(mContext)
            .load("http://{some_url}.jpg")
                .fit()
                .transform(transformation)
                .into(profile_image);*/


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_provider_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        profile_image= findViewById(R.id.profile_image);

        // Capture the layout's TextView and set the string as its text
        self = this;


        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            // User is signed in
            Model.userData.name        = user.getDisplayName();
            Model.userData.uid         = user.getUid();
            Model.userData.email       = user.getEmail();
            Model.userData.phoneNumber = user.getPhoneNumber();
            Model.userData.photoUrl    = user.getPhotoUrl().toString();

        }

        appointmentsHistoryBTN  = findViewById(R.id.appointments_history_btn);
        seeReviewsBTN           = findViewById(R.id.see_reviews_btn);
        editDetailsBTN          = findViewById(R.id.edit_details_btn);
        interestedMothersBTN    = findViewById(R.id.interested_mothers_btn);
        interestedMothersNotifications = findViewById(R.id.interestedMothersNotifications);
        editDetailsBTN.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                //set the user of type customer
                //create an entry in the db under the uuid
                //edit_cities_btn.setOnClickListener(null);
                Intent intent = new Intent(getApplicationContext(), ProviderRegistration.class);
                startActivity(intent);
                return;
            }
        });


        reqNotifications        = findViewById(R.id.reqNotifications);
        availableWorkBTN        = findViewById(R.id.consultations_request_btn);



        availabilityToggleBTN   = findViewById(R.id.switch_ent);
        interestedMothersNotifications.setVisibility(View.INVISIBLE);
        reqNotifications.setVisibility(View.INVISIBLE);
        starsArr = new int[]{R.id.star_1, R.id.star_2, R.id.star_3, R.id.star_4, R.id.star_5};
        showButtons();

    }

    @Override
    public void onStart() {
        super.onStart();

        getProviderService =  new GetProviderService(getApplicationContext());
        getProviderService.getProviderData(Model.userData.uid );
        getProviderService.addListener("PROVIDER_DATA_RETRIEVED", onProviderDataRetrieved);



        for(int i = 0; i < starsArr.length; i++)
        {
            ImageView star = findViewById( starsArr[i] );
            star.setImageResource(R.drawable.empty_star);
        }


    }

    private EventListener onProviderDataRetrieved = new EventListener() {
        @Override
        public void onEvent(Event event) {
            getProviderService.removeListener("PROVIDER_DATA_RETRIEVED", onProviderDataRetrieved);

            GetProviderService.ProviderData dataObj = getProviderService.dataObj;

            if(getProviderService.showEditProfileScreen)
            {
                Intent intent = new Intent(getApplicationContext(), ProviderRegistration.class);
                startActivity(intent);
            }
            else
            {

                appointmentsHistoryBTN.setOnClickListener(new View.OnClickListener(){

                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(getApplicationContext(), ContactsActivity.class);
                        startActivity(intent);
                        return;
                    }
                });

                availabilityToggleBTN.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if(isChecked)
                        {
                            getProviderService.acceptingBidsNode.setValue(true);
                        }
                        else
                        {
                            getProviderService.acceptingBidsNode.setValue(false);
                        }
                    }
                });

                for(int i = 0; i < dataObj.providerRatings; i++)
                {
                    ImageView star = findViewById( starsArr[i] );
                    star.setImageResource(R.drawable.star);
                }

                TextView displayNameTXT = findViewById(R.id.profile_name );
                displayNameTXT.setText(dataObj.name);

                if(dataObj.acceptingBids == true)
                {
                    availabilityToggleBTN.setChecked(true);
                }
                else
                {
                    availabilityToggleBTN.setChecked(false);
                }

                TextView ibclcTXT = findViewById( R.id.is_ibclc );
                if( dataObj.isIbclc == true )
                {
                    ibclcTXT.setText( getResources().getString(R.string.ibclc_certified) );
                }
                else
                {
                    ibclcTXT.setText( getResources().getString(R.string.not_ibclc_certified)  );
                }

                TextView priceTXT = findViewById( R.id.priceTXT );
                priceTXT.setText( Integer.toString(dataObj.price) + "₪");

                getProfilePicService = new GetProviderService.GetProfilePicService(getApplicationContext());
                getProfilePicService.getImageFromDB(Model.userData.uid, "profilePic");
                getProfilePicService.addListener("PROFILE_PIC_RETRIEVED", onProfilePicRetreived);


                avaliableWorkService = new GetAvaliableWorkService(getApplicationContext());
                avaliableWorkService.addListener("AVALIABLE_WORK_RETREIVED", onAvaliableWorkRetreived);
                avaliableWorkService.getAvaliableWork();


                interestedCustomersService = new InterestedCustomersService(getApplicationContext());
                interestedCustomersService.addListener("INTERESTED_CUSTOMERS_RETRIEVED", onInterestedCustomersRetreived);
                interestedCustomersService.getInterestedCustemers();

            }

        }
    };



    private EventListener onProfilePicRetreived = new EventListener() {
        @Override
        public void onEvent(Event event) {
            getProfilePicService.removeListener("PROFILE_PIC_RETRIEVED", onProfilePicRetreived);
            Glide.with(ProviderMain.this).load(getProfilePicService.uri).into(profile_image);

        }
    };


    @Override
    public void onStop() {
        super.onStop();
        if(avaliableWorkService != null )
        {
            avaliableWorkService.removeListener("AVALIABLE_WORK_RETREIVED", onAvaliableWorkRetreived);
            avaliableWorkService = null;
        }

    }

    private EventListener onInterestedCustomersRetreived = new EventListener() {
        @Override
        public void onEvent(Event event) {
            interestedCustomersService.removeListener("INTERESTED_CUSTOMERS_RETRIEVED", onInterestedCustomersRetreived);
            if(interestedCustomersService.count == 0)
            {
                interestedMothersNotifications.setVisibility(View.INVISIBLE);
                interestedMothersBTN.setOnClickListener(null);
            }
            else
            {
                interestedMothersNotifications.setVisibility(View.VISIBLE);
                interestedMothersNotifications.setText(Integer.toString(interestedCustomersService.count));
                interestedMothersBTN.setOnClickListener(new View.OnClickListener(){

                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(getApplicationContext(), InterestedCustmersActivity.class);
                        intent.putExtra("numFreeLeadsLeft",getProviderService.dataObj.numFreeLeadsLeft);
                        startActivity(intent);
                        return;
                    }
                });
            }
        }
    };

    private EventListener onAvaliableWorkRetreived = new EventListener() {
        @Override
        public void onEvent(Event event) {
            avaliableWorkService.removeListener("AVALIABLE_WORK_RETREIVED", onAvaliableWorkRetreived);
            if(avaliableWorkService.count == 0)
            {
                reqNotifications.setVisibility(View.INVISIBLE);
                availableWorkBTN.setOnClickListener(null);

            }
            else
            {
                reqNotifications.setVisibility(View.VISIBLE);
                reqNotifications.setText(Integer.toString(avaliableWorkService.count));
                availableWorkBTN.setOnClickListener(new View.OnClickListener(){

                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(getApplicationContext(), AvailableWorkActivity.class);
                        startActivity(intent);
                        return;
                    }
                });
            }

        }
    };

    private void showButtons() {
        appointmentsHistoryBTN.setVisibility(View.VISIBLE);
        seeReviewsBTN.setVisibility(View.VISIBLE);
        editDetailsBTN.setVisibility(View.VISIBLE);
        availableWorkBTN.setVisibility(View.VISIBLE);
        availabilityToggleBTN.setVisibility(View.VISIBLE);
    }



}
