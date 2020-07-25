package com.sumanthakkala.medialines.activities;

import androidx.annotation.Nullable;
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
import android.widget.Toast;

import com.github.omadahealth.lollipin.lib.managers.AppLock;
import com.github.omadahealth.lollipin.lib.managers.LockManager;
import com.sumanthakkala.medialines.R;

public class SplashActivity extends AppCompatActivity {

    public static final int REQUEST_CODE_UNLOCK_PIN = 1;
    LockManager<SecurityPinActivity> lockManager;
    private Handler handler = new Handler();
    private SharedPreferences sharedPreferences;
    SharedPreferences securitySharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        securitySharedPref = getSharedPreferences("Security_Prefs", MODE_PRIVATE);
        lockManager = LockManager.getInstance();

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
        boolean isLockEnabled = (sharedPreferences.getBoolean("security_status", false));
        if(isLockEnabled){
            lockManager.enableAppLock(getApplicationContext(), SecurityPinActivity.class);
            lockManager.getAppLock().setTimeout(100);
            if(lockManager.getAppLock() != null && lockManager.getAppLock().isPasscodeSet()){
                Intent intent = new Intent(getApplicationContext(), SecurityPinActivity.class);
                intent.putExtra(AppLock.EXTRA_TYPE, AppLock.UNLOCK_PIN);
                startActivityForResult(intent, REQUEST_CODE_UNLOCK_PIN);
            }
            else {
                handler.postDelayed(runnable, 1000); //This will display splash screen for real loading time + 1 seconds
            }
        }
        else {
            handler.postDelayed(runnable, 1000); //This will display splash screen for real loading time + 1 seconds
        }
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CODE_UNLOCK_PIN:
//                if(resultCode == 0){
//                    this.finishAffinity();
//                }
//                else {
                    if(!securitySharedPref.contains(getString(R.string.security_question_key_in_prefs)) || !securitySharedPref.contains(getString(R.string.security_answer_key_in_prefs))){
                        Toast.makeText(this, "PIN recovery setup is not configured. Please complete it by adding a security question in settings.", Toast.LENGTH_LONG).show();
                    }
                    startActivity(new Intent(getApplicationContext(), MainActivity.class));
                    finish();
//                }
                break;
        }
    }
}