package com.sumanthakkala.medialines.services;

import android.content.Context;
import android.content.SharedPreferences;

import com.sumanthakkala.medialines.R;

import static android.content.Context.MODE_PRIVATE;

public class BillingService {
    private Context context;
    private SharedPreferences billingPrefs;

    public BillingService(Context c){
        context = c;
        billingPrefs = context.getSharedPreferences("Purchase_Prefs", MODE_PRIVATE);
    }

    public boolean isPurchased(){
        return billingPrefs.getBoolean(context.getString(R.string.is_pro_purchased), false);
    }
}
