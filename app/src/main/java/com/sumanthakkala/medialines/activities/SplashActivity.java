package com.sumanthakkala.medialines.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.PreferenceManager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;

import com.sumanthakkala.medialines.R;

public class SplashActivity extends AppCompatActivity {

    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        boolean isLightMode = sharedPreferences.getBoolean("theme", false);
        if(isLightMode){
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            setSystemControlDecorsByCurrentTheme();
        }
        else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            setSystemControlDecorsByCurrentTheme();
        }
    }

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if(!isFinishing()){
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                finish();
            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        handler.postDelayed(runnable, 1000); //This will display splash screen for real loading time + 1 seconds
    }

    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacks(runnable);
    }

    private void setSystemControlDecorsByCurrentTheme(){
        if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.M) {
            final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            boolean isLightMode = sharedPreferences.getBoolean("theme", false);
            if(isLightMode){
                getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            }
            else {
                getWindow().getDecorView().setSystemUiVisibility(0);
            }
        }
        else {
            if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.LOLLIPOP){
                getWindow().setStatusBarColor(Color.parseColor("#000000"));
            }
        }
    }
}