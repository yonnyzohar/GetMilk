package com.yonnyzohar.getmilk.providers;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.FileNotFoundException;
import java.io.InputStream;

import com.yonnyzohar.getmilk.GameActivity;
import com.yonnyzohar.getmilk.services.GetProviderService;
import com.yonnyzohar.getmilk.Methods;
import com.yonnyzohar.getmilk.data.Model;
import com.yonnyzohar.getmilk.R;
import com.yonnyzohar.getmilk.eventDispatcher.Event;
import com.yonnyzohar.getmilk.eventDispatcher.EventListener;
import com.yonnyzohar.getmilk.sharedScreens.CitiesMultipleChoice;


public class ProviderRegistration extends GameActivity
{
    Boolean startListeningForPrice = false;
    Boolean startListeningForBio = false;
    Boolean startListeningForName = false;

    EditText priceTXT;
    EditText bioTXT;
    EditText displayNameTXT;
    Button edit_cities_btn;
    Button confirmBTN;
    Switch ibclcEntToggleBTN;

    GetProviderService.GetProfilePicService getProfilePicService;
    GetProviderService getProviderService;


    ImageView profile_image;
    private static final int PICK_PHOTO_FOR_AVATAR = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_provider_registration);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        displayNameTXT = findViewById(R.id.profile_name);
        priceTXT = findViewById(R.id.price);
        bioTXT  = findViewById(R.id.bio);
        edit_cities_btn = findViewById(R.id.edit_cities_btn);
        confirmBTN = findViewById(R.id.confirm_btn);
        ibclcEntToggleBTN = findViewById(R.id.ibclcEnt);
        profile_image = findViewById(R.id.profile_image);




        priceTXT.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    // code to execute when EditText loses focus
                    if(startListeningForPrice)
                    {
                        String amount = priceTXT.getText().toString();
                        startListeningForPrice = false;
                        int myNum = Integer.parseInt(amount);
                        if(myNum != 0)
                        {
                            setPrice(myNum);
                        }

                    }
                }
                else
                {
                    startListeningForPrice = true;
                }
            }
        });

        bioTXT.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    // code to execute when EditText loses focus
                    if(startListeningForBio)
                    {
                        String bioStr = bioTXT.getText().toString();
                        startListeningForBio = false;
                        setBio(bioStr);
                    }
                }
                else
                {
                    startListeningForBio = true;
                }
            }
        });

        displayNameTXT.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    // code to execute when EditText loses focus
                    if(startListeningForName)
                    {
                        String nameStr = displayNameTXT.getText().toString();
                        startListeningForName = false;
                        setName(nameStr);
                    }
                }
                else
                {
                    startListeningForName = true;
                }
            }
        });


        edit_cities_btn.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                updateData();
                Intent intent = new Intent(getApplicationContext(), CitiesMultipleChoice.class );
                startActivity(intent);
                return;
            }
        });

        confirmBTN.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {

                updateData();
                Intent intent = new Intent(getApplicationContext(), ProviderMain.class );
                startActivity(intent);
                return;
            }
        });



    }

    private void updateData() {
        String bioStr = bioTXT.getText().toString();
        startListeningForBio = false;
        setBio(bioStr);

        String amount = priceTXT.getText().toString();
        startListeningForPrice = false;
        int myNum = Integer.parseInt(amount);
        setPrice(myNum);

        String nameStr = displayNameTXT.getText().toString();
        startListeningForName = false;
        setName(nameStr);
    }

    @Override
    public void onStart() {
        super.onStart();


        getProviderService = new GetProviderService(getApplicationContext());
        getProviderService.getProviderData(Model.userData.uid);
        getProviderService.addListener("PROVIDER_DATA_RETRIEVED", onProviderDataRetrieved);


        profile_image.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                getProfilePic();
            }
        });


        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();


    }

    private EventListener onProviderDataRetrieved = new EventListener() {
        @Override
        public void onEvent(Event event) {
            getProviderService.removeListener("PROVIDER_DATA_RETRIEVED", onProviderDataRetrieved);
            GetProviderService.ProviderData dataObj = getProviderService.dataObj;

            ibclcEntToggleBTN.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if(isChecked)
                    {
                        getProviderService.ibclcNode.setValue(true);
                    }
                    else
                    {
                        getProviderService.ibclcNode.setValue(false);
                    }
                }
            });

            if (Model.userData.phoneNumber == null || "".equals(Model.userData.phoneNumber))
            {

            }
            else
            {
                if(dataObj.phoneNumber == null)
                {
                    getProviderService.phoneNumberNode.setValue(Model.userData.phoneNumber);
                }
            }

            if(Model.fireBaseMessagingToken != null)
            {
                getProviderService.fireBaseMessagingTokenNode.setValue(Model.fireBaseMessagingToken);
            }



            if(dataObj.name == null)
            {
                displayNameTXT.setText(Model.userData.name);
            }
            else
            {
                displayNameTXT.setText(dataObj.name);
            }

            if(dataObj.price != 0)
            {
                priceTXT.setText(Integer.toString(dataObj.price));
            }


            if(dataObj.isIbclc == null)
            {
                ibclcEntToggleBTN.setChecked(false);

            }
            else
            {
                if( dataObj.isIbclc == true )
                {
                    ibclcEntToggleBTN.setChecked(true);
                }
                else
                {
                    ibclcEntToggleBTN.setChecked(false);
                }
            }

            bioTXT.setText( dataObj.aboutMe );

            getProfilePicService = new GetProviderService.GetProfilePicService(getApplicationContext());
            getProfilePicService.getImageFromDB(Model.userData.uid, "profilePic");
            getProfilePicService.addListener("PROFILE_PIC_RETRIEVED", onProfilePicRetreived);

        }
    };




    private EventListener onProfilePicRetreived = new EventListener() {
        @Override
        public void onEvent(Event event) {
            getProfilePicService.removeListener("PROFILE_PIC_RETRIEVED", onProfilePicRetreived);
            Glide.with(ProviderRegistration.this).load(getProfilePicService.uri).into(profile_image);

        }
    };



    private void getProfilePic()
    {
        Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
        getIntent.setType("image/*");

        Intent pickIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickIntent.setType("image/*");

        Intent chooserIntent = Intent.createChooser(getIntent, "Select Image");
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[] {pickIntent});

        startActivityForResult(chooserIntent, PICK_PHOTO_FOR_AVATAR);

    }




    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == PICK_PHOTO_FOR_AVATAR && resultCode == Activity.RESULT_OK) {
            if (data == null) {
                //Display an error
                return;
            }

            setImage(data.getData(), true);

        }
    }

    private void setImage(final Uri uri, Boolean writeToDB) {
        try {
            InputStream inputStream = this.getContentResolver().openInputStream( uri );
            Bitmap bitmap =  BitmapFactory.decodeStream(inputStream);
            profile_image.setImageBitmap(bitmap);

            Methods.saveImageToDB(bitmap, Model.userData.uid, "profilePic");

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void setPrice(final int amount) {
        getProviderService.priceNisNode.setValue(amount);
    }

    private void setName(final String nameStr) {
        getProviderService.displayNameNode.setValue(nameStr);
    }

    private void setBio(final String bioStr) {
        getProviderService.aboutMeNode.setValue(bioStr);
    }

}
