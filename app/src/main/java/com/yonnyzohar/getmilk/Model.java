package com.yonnyzohar.getmilk;

import com.google.android.gms.auth.api.signin.GoogleSignInClient;

import java.util.List;

/**
 * Created by yonny on 20/01/2018.
 */

public class Model {

    public static class UserData{
        public String name;
        public String uid;
        public String email;
        public String phoneNumber;
        public String photoUrl;
        public String residence;
    }

   /* public static class ProviderData{
        public String[] cities;
        public int priceNIS;
        public String description;
        public Boolean ibclc;
        public String[] photos;
    }*/

    public static class AppointmentTypes{

        public static int PENDING = 0;
        public static int CONFIRMEND = 1;
        public static int COMPLETE = 2;

    }

    public static class DBRefs{
        public static String PROVIDERS = "providers";
        public static String CUSTOMERS = "customers";
        public static String APPOINTMENTS_IN_PROCESS = "appointmentsInProcess";
        public static String APPOINTMENTS_HISTORY = "appointmentsHistory";
        public static String REVIEWS = "reviews";

    }


    public static final String TAG = "GetMilk";


    public static String userType = "";

    public static GoogleSignInClient mGoogleSignInClient;

    public static UserData userData = new UserData();

    public static List<String> citiesArr;

    public static String fireBaseMessagingToken;




}
