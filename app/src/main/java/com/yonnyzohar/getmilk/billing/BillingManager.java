package com.yonnyzohar.getmilk.billing;

import android.app.Activity;
import android.support.annotation.Nullable;
import android.util.Log;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.ConsumeResponseListener;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.android.billingclient.api.SkuDetailsResponseListener;
import com.yonnyzohar.getmilk.eventDispatcher.EventDispatcher;
import com.yonnyzohar.getmilk.eventDispatcher.SimpleEvent;

import java.util.ArrayList;
import java.util.List;


        /*Prerequisites:

        AndroidManifest must include "com.android.vending.BILLING" permission.
        APK is built in release mode.
        APK is signed with the release certificate(s).
        APK is uploaded to alpha/beta distribution channel (previously - as a draft) to the developer console at least once. (takes some time ~2h-24h).
        IAB products are published and their status set to active.
        Test account(s) is added in developer console.
        Testing requirements:

        Test APK has the same versionCode as the one uploaded to developer console.
        Test APK is signed with the same certificate(s) as the one uploaded to dev.console.
        Test account (not developer) - is the main account on the device.
        Test account is opted-in as a tester and it's linked to a valid payment method. (@Kyone)
        P.S: Debugging with release certificate: https://stackoverflow.com/a/15754187/1321401 (Thnx @dipp for the link)

        P.P.S: Wanted to make this list for a long time already.*/



public class BillingManager extends EventDispatcher implements PurchasesUpdatedListener {

    private static BillingManager single_instance = null;

    Activity activity;
    BillingClient billingClient;
    boolean isServiceConnected = false;
    int billingClientResponseCode;
    private static final String TAG = "BillingManager";

    private BillingManager()
    {

    }

    public static BillingManager getInstance()
    {
        if (single_instance == null)
            single_instance = new BillingManager();

        return single_instance;
    }

    public void init(Activity activity) {

        this.activity = activity;
        billingClient = BillingClient.newBuilder(activity).setListener(this).build();
        startServiceConnection(new Runnable() {
            @Override
            public void run() {

                dispatchEvent(new SimpleEvent("BILLING_CLIENT_SETUP_FINISHED") );

            }
        });


    }

    //find out what i have already bought and not consumed
    public void queryPurchases() {
        Runnable queryToExecute = new Runnable() {
            @Override
            public void run() {
                long time = System.currentTimeMillis();
                Purchase.PurchasesResult purchasesResult = billingClient.queryPurchases(BillingClient.SkuType.INAPP);
                if (areSubscriptionsSupported()) {
                    Purchase.PurchasesResult subscriptionResult
                            = billingClient.queryPurchases(BillingClient.SkuType.SUBS);
                    if (subscriptionResult.getResponseCode() == BillingClient.BillingResponse.OK) {
                        purchasesResult.getPurchasesList().addAll(
                                subscriptionResult.getPurchasesList());
                    } else {
                        // Handle any error response codes.
                    }
                } else if (purchasesResult.getResponseCode() == BillingClient.BillingResponse.OK) {
                    // Skip subscription purchases query as they are not supported.
                } else {
                    // Handle any other error response codes.
                }
                onQueryPurchasesFinished(purchasesResult);
            }
        };
        executeServiceRequest(queryToExecute);
    }

    private void onQueryPurchasesFinished(Purchase.PurchasesResult result) {
        // Have we been disposed of in the meantime? If so, or bad result code, then quit
        if (billingClient == null || result.getResponseCode() != BillingClient.BillingResponse.OK) {
            Log.w(TAG, "Billing client was null or result code (" + result.getResponseCode()
                    + ") was bad – quitting");
            return;
        }

        Log.d(TAG, "Query inventory was successful.");

        // Update the UI and purchases inventory with new list of purchases
        // mPurchases.clear();
        onPurchasesUpdated(BillingClient.BillingResponse.OK, result.getPurchasesList());
    }

    @Override
    public void onPurchasesUpdated(int responseCode, @Nullable List<Purchase> purchases) {

        if (responseCode == BillingClient.BillingResponse.OK) {

            onPurchasesUpdated(purchases);

        } else if (responseCode == BillingClient.BillingResponse.USER_CANCELED) {
            Log.i(TAG, "onPurchasesUpdated() – user cancelled the purchase flow – skipping");
        } else {
            Log.w(TAG, "onPurchasesUpdated() got unknown resultCode: " + responseCode);
        }

    }


    public void onPurchasesUpdated(List<Purchase> purchases) {

        for (Purchase p : purchases) {

            //update ui

        }
        dispatchEvent(new SimpleEvent("PURCHASES_UPDATED"));
    }

    public void consumeAsync(final String purchaseToken) {
        // If we’ve already scheduled to consume this token – no action is needed (this could happen
        // if you received the token when querying purchases inside onReceive() and later from
        // onActivityResult()
        // Generating Consume Response listener
        final ConsumeResponseListener onConsumeListener = new ConsumeResponseListener() {
            @Override
            public void onConsumeResponse(@BillingClient.BillingResponse int responseCode, String purchaseToken) {
                // If billing service was disconnected, we try to reconnect 1 time
                // (feel free to introduce your retry policy here).
               onConsumeFinished(purchaseToken, responseCode);
            }
        };

        // Creating a runnable from the request to use it inside our connection retry policy below
        Runnable consumeRequest = new Runnable() {
            @Override
            public void run() {
                // Consume the purchase async
                billingClient.consumeAsync(purchaseToken, onConsumeListener);
            }
        };

        executeServiceRequest(consumeRequest);
    }

    void onConsumeFinished(String token, int result) {

        if (result == BillingClient.BillingResponse.OK) {
        }
    }

    public void querySkuDetailsAsync(final List<String> skuList, final SkuDetailsResponseListener skuListener) {
        // Create a runnable from the request to use inside the connection retry policy.
        Runnable queryRequest = new Runnable() {
            @Override
            public void run() {
                // Create the SkuDetailParams object
                SkuDetailsParams.Builder params = SkuDetailsParams.newBuilder();
                params.setSkusList(skuList).setType(BillingClient.SkuType.INAPP);
                // Run the query asynchronously.
                billingClient.querySkuDetailsAsync(params.build(), new SkuDetailsResponseListener() {
                    @Override
                    public void onSkuDetailsResponse(int responseCode, List<SkuDetails> skuDetailsList) {
                        skuListener.onSkuDetailsResponse(responseCode, skuDetailsList);
                    }

                });
            }
        };

        executeServiceRequest(queryRequest);
    }


    ///////////////////////////////////////////


    private void executeServiceRequest(Runnable runnable) {
        if (isServiceConnected) {
            runnable.run();
        } else {
            // If the billing service disconnects, try to reconnect once.
            startServiceConnection(runnable);
        }
    }

    public void startServiceConnection(final Runnable runnable) {
        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(int responseCode) {

                if (responseCode == BillingClient.BillingResponse.OK) {
                    isServiceConnected = true;
                    if (runnable != null) {
                        runnable.run();
                    }
                }
                billingClientResponseCode = responseCode;

            }

            @Override
            public void onBillingServiceDisconnected() {

                isServiceConnected = false;
            }
        });

    }





    public boolean areSubscriptionsSupported() {
        int responseCode = billingClient.isFeatureSupported(BillingClient.FeatureType.SUBSCRIPTIONS);
        if (responseCode != BillingClient.BillingResponse.OK) {
            Log.w(TAG, "areSubscriptionsSupported() got an error response: " + responseCode);
        }
        return responseCode == BillingClient.BillingResponse.OK;
    }

    public void initiatePurchaseFlow(final String skuId)
    {
        final ArrayList<String> oldSkus = null;

        final @BillingClient.SkuType String billingType =  BillingClient.SkuType.INAPP;

        Runnable purchaseFlowRequest = new Runnable() {
            @Override
            public void run() {
                BillingFlowParams mParams = BillingFlowParams.newBuilder().
                        setSku(skuId).setType(billingType).setOldSkus(oldSkus).build();
                billingClient.launchBillingFlow(activity, mParams);
            }
        };
        executeServiceRequest(purchaseFlowRequest);

    }


}
