package com.yonnyzohar.getmilk.billing;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.Purchase;

import java.util.List;

public class MyBillingUpdateListener implements BillingUpdatesListener {

    BillingManager billingManager;


    @Override
    public void onBillingClientSetupFinished() {

        billingManager.queryPurchases();
    }

    @Override
    public void onConsumeFinished(String token, int result) {

        if (result == BillingClient.BillingResponse.OK) {
        }
    }

    @Override
    public void onPurchasesUpdated(List<Purchase> purchases) {

        for (Purchase p : purchases) {

            //update ui

        }



    }

    @Override
    public void setSelf(BillingManager _billingManager) {
        billingManager = _billingManager;
    }
}