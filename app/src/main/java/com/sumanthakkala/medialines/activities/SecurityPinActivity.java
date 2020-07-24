package com.sumanthakkala.medialines.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.omadahealth.lollipin.lib.managers.AppLock;
import com.github.omadahealth.lollipin.lib.managers.AppLockActivity;
import com.github.omadahealth.lollipin.lib.managers.FingerprintUiHelper;
import com.github.omadahealth.lollipin.lib.managers.LockManager;
import com.sumanthakkala.medialines.R;
import com.sumanthakkala.medialines.constants.Constants;
import com.sumanthakkala.medialines.entities.NoteWithData;

public class SecurityPinActivity extends AppLockActivity {

    private boolean isChangePin = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LockManager<SecurityPinActivity> lockManager = LockManager.getInstance();
        lockManager.getAppLock().setLogoId(R.drawable.logo_hq);
        lockManager.getAppLock().setTimeout(100);
//        setContentView(R.layout.activity_security_pin);

        if (getIntent().getBooleanExtra("isChangePin", false)) {
            isChangePin = true;
        }

    }

    @Override
    public void showForgotDialog() {

    }

    @Override
    public void onPinFailure(int attempts) {

    }

    @Override
    public void onPinSuccess(int attempts) {

    }

    @Override
    public void onBackPressed() {
        if(isChangePin){
            finish();
        }
        else {
            this.finishAffinity();
        }
    }
}