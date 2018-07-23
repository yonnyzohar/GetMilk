package com.yonnyzohar.getmilk.customers;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Set;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.yonnyzohar.getmilk.GetProfilePicService;
import com.yonnyzohar.getmilk.Model;
import com.yonnyzohar.getmilk.R;
import com.yonnyzohar.getmilk.ReviewerData;
import com.yonnyzohar.getmilk.eventDispatcher.Event;
import com.yonnyzohar.getmilk.eventDispatcher.EventListener;
import com.yonnyzohar.getmilk.providers.AvailableWorkActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ProviderScreenForCustomer extends AppCompatActivity {

    public String aboutMe;
    public String name;
    public Boolean isIbclc;
    public int price;
    public double providerRatings = 0;
    public String providerId;


    LoginButton loginButton;
    CallbackManager callbackManager;
    ProviderReviewsService providerReviewsService;
    Button orderConsultantBTN;


    int starsArr[];


    ListView providerReviewsView;
    ProviderScreenForCustomer.ListItemController listItemController;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_provider_screen_for_customer);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        providerReviewsView = findViewById(R.id.provider_reviews);
        listItemController = new ProviderScreenForCustomer.ListItemController();
        callbackManager = CallbackManager.Factory.create();

        Bundle extras = getIntent().getExtras();
        if (extras != null)
        {
            price = extras.getInt("price");
            aboutMe = extras.getString("aboutMe");
            name = extras.getString("name");
            isIbclc = extras.getBoolean("isIbclc");
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

            AccessToken accessToken = AccessToken.getCurrentAccessToken();
            boolean isLoggedIn = accessToken != null && !accessToken.isExpired();

            if(isLoggedIn == false)
            {
                showFBLoginButton();
            }
            else
            {
                fetchFriends( accessToken);
            }

            providerReviewsService = new ProviderReviewsService(getApplicationContext());
            providerReviewsService.getReviews(providerId);
            providerReviewsService.addListener("REVIEWS_RETRIEVED", onReviewsRetrieved);


        }
    }

    private EventListener onReviewsRetrieved = new EventListener() {
        @Override
        public void onEvent(Event event) {
            providerReviewsService.removeListener("REVIEWS_RETRIEVED", onReviewsRetrieved);

            if(providerReviewsService.numReviews != 0)
            {
                providerReviewsView.setAdapter(listItemController);
                providerReviewsView.setOnTouchListener(new ListView.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        int action = event.getAction();
                        switch (action) {
                            case MotionEvent.ACTION_DOWN:
                                // Disallow ScrollView to intercept touch events.
                                v.getParent().requestDisallowInterceptTouchEvent(true);
                                break;

                            case MotionEvent.ACTION_UP:
                                // Allow ScrollView to intercept touch events.
                                v.getParent().requestDisallowInterceptTouchEvent(false);
                                break;
                        }

                        // Handle ListView touch events.
                        v.onTouchEvent(event);
                        return true;
                    }
                });

                orderConsultantBTN = findViewById(R.id.order_consultant_btn);
                orderConsultantBTN.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View view) {
                        orderConsultantBTN.setOnClickListener(null);
                        onProviderSelected(providerId, Model.userData.uid);
                        return;
                    }
                });

            }
        }
    };

    private void onProviderSelected(String providerId, String customerId) {
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        String url ="https://us-central1-testproject-103c6.cloudfunctions.net/onCustomerWantsProvider?providerId="+providerId+"&customerId="+customerId;
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Display the first 500 characters of the response string.
                        Log.d(Model.TAG, response);

                        String str = getResources().getString(R.string.notifying_provider_of_your_interest);
                        Toast.makeText(getApplicationContext(), str, Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent(getApplicationContext(), RespondingProviders.class);
                        startActivity(intent);
                        return;

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(Model.TAG,"That didn't work!");
            }
        });

        queue.add(stringRequest);
    }

    class ListItemController extends BaseAdapter {

        @Override
        public int getCount() {
            return providerReviewsService.reviewsArr.size();
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

            convertView = getLayoutInflater().inflate(R.layout.review_line, null);
            ReviewerData obj = providerReviewsService.reviewsArr.get(position);


            String customerId = obj.getCustomerId();
            String reviewerName = obj.getReviewerName();
            String date = obj.getDate();
            String desc = obj.getDesc();
            int rating = obj.getRating();


            TextView nameTXT = convertView.findViewById(R.id.nameTXT);
            TextView review_date = convertView.findViewById(R.id.review_date);
            TextView review_txt = convertView.findViewById(R.id.review_txt);
            TextView rating_txt = convertView.findViewById(R.id.rating_txt);

            nameTXT.setText( reviewerName );
            review_date.setText( date );
            review_txt.setText( desc );
            rating_txt.setText( Integer.toString(rating) );



            return convertView;
        }
    }

    private void fetchFriends(AccessToken accessToken) {
        String userId = accessToken.getUserId();

        //this request only returns facebook friends who have INSTALLED GETMILK.
        GraphRequest request = GraphRequest.newGraphPathRequest(
                accessToken,
                "/" + userId + "/friends",
                new GraphRequest.Callback() {
                    @Override
                    public void onCompleted(GraphResponse response) {
                        // Insert your code here

                        JSONObject object = response.getJSONObject();
                        try {
                            if (object !=null){


                                JSONArray data = object.getJSONArray("data");
                                int len = data.length();


                                for (int i = 0; i < len; i++) {
                                    String id = data.getJSONObject(i).getString("id");
                                    String name = data.getJSONObject(i).getString("name");
                                    //ListOfFriendsIDS.add(id);
                                    //ListOfFriendsNames.add(name);
                                    Log.d(Model.TAG, name + " " + id);
                                }
                            }
                        } catch (JSONException e) {
                            Log.d(Model.TAG, "FUCK");
                        }
                    }
                });

        request.executeAsync();





    }

    private void showFBLoginButton() {

        loginButton = (LoginButton) findViewById(R.id.login_button);
        loginButton.setReadPermissions(Arrays.asList("email","user_friends"));

        // LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("public_profile"));

        // If you are using in a fragment, call loginButton.setFragment(this);

        // Callback registration
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                AccessToken accessToken = AccessToken.getCurrentAccessToken();

                fetchFriends(accessToken);
                // App code
            }

            @Override
            public void onCancel() {
                // App code
            }

            @Override
            public void onError(FacebookException exception) {
                // App code
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        callbackManager.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }

}
