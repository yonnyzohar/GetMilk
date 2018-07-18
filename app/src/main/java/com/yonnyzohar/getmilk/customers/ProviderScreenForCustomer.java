package com.yonnyzohar.getmilk.customers;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.Set;

import com.yonnyzohar.getmilk.GetProfilePicService;
import com.yonnyzohar.getmilk.Model;
import com.yonnyzohar.getmilk.R;
import com.yonnyzohar.getmilk.eventDispatcher.Event;
import com.yonnyzohar.getmilk.eventDispatcher.EventListener;

public class ProviderScreenForCustomer extends AppCompatActivity {

    public String aboutMe;
    public String name;
    public Boolean isIbclc;
    public int price;
    public double providerRatings = 0;
    public String providerId;


    int starsArr[];


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_provider_screen_for_customer);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Bundle extras = getIntent().getExtras();
        if (extras != null)
        {

            aboutMe = extras.getString("aboutMe");
            name = extras.getString("name");
            isIbclc = extras.getBoolean("isIbclc");
            price = extras.getInt("price");
            providerRatings = extras.getDouble("providerRatings");
            providerId = extras.getString("providerId");

            starsArr = new int[]{R.id.star_1, R.id.star_2, R.id.star_3, R.id.star_4, R.id.star_5};

            for(int i = 0; i < starsArr.length; i++)
            {
                ImageView star = findViewById( starsArr[i] );
                star.setImageResource(R.drawable.empty_star);
            }

            for(int i = 0; i < providerRatings; i++)
            {
                ImageView star = findViewById( starsArr[i] );
                star.setImageResource(R.drawable.star);
            }

            //provider name
            TextView nameTXT = findViewById(R.id.nameTXT);
            nameTXT.setText( name );

            TextView bioTXT = findViewById(R.id.bio);
            bioTXT.setText( aboutMe);

            //provider price
            TextView priceTXT = findViewById(R.id.priceTXT);
            priceTXT.setText( Integer.toString(price) + "₪");

            TextView ibclcTXT = findViewById( R.id.is_ibclc );
            if( isIbclc == true )
            {
                ibclcTXT.setText( getResources().getString(R.string.ibclc_certified) );
            }
            else
            {
                ibclcTXT.setText( getResources().getString(R.string.not_ibclc_certified)  );
            }

            //image
            final ImageView profile_image = findViewById(R.id.profile_image);
            final GetProfilePicService getProfilePicService = new GetProfilePicService(getApplicationContext());
            getProfilePicService.getImageFromDB(providerId, "profilePic");
            getProfilePicService.addListener("PROFILE_PIC_RETRIEVED", new EventListener()
            {
                @Override
                public void onEvent(Event event) {
                    Glide.with(ProviderScreenForCustomer.this).load(getProfilePicService.uri).into(profile_image);

                }
            });
        }
    }

}
