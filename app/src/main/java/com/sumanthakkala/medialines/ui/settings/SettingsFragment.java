package com.sumanthakkala.medialines.ui.settings;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreferenceCompat;

import com.github.omadahealth.lollipin.lib.managers.AppLock;
import com.github.omadahealth.lollipin.lib.managers.LockManager;
import com.sumanthakkala.medialines.R;
import com.sumanthakkala.medialines.activities.MainActivity;
import com.sumanthakkala.medialines.activities.SecurityPinActivity;

public class SettingsFragment extends PreferenceFragmentCompat {

    public static final int REQUEST_CODE_SETUP_PIN = 1;

    LockManager<SecurityPinActivity> lockManager;
    private SwitchPreferenceCompat themePreference;
    private SwitchPreferenceCompat securityStatus;
    private SwitchPreferenceCompat fingerprintAuthSwitch;
    private Preference setupPin;
    private Preference securityQuestion;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey);
        getActivity().setTheme(R.style.SettingsFragmentStyle);
        lockManager = LockManager.getInstance();
        themePreference = findPreference("theme");
        themePreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                if((boolean) newValue){
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                }
                else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                }
                return true;
            }
        });

        setupPin = findPreference("setupPin");
        setupPin.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent intent = new Intent(getActivity().getApplicationContext(), SecurityPinActivity.class);
                intent.putExtra(AppLock.EXTRA_TYPE, AppLock.ENABLE_PINLOCK);
                intent.putExtra("isChangePin", true);
//                lockManager.getAppLock().setLastActiveMillis();
                startActivityForResult(intent, REQUEST_CODE_SETUP_PIN);
                return true;
            }
        });

        securityQuestion = findPreference("security_question");

        fingerprintAuthSwitch = findPreference("fingerprint_switch");
        fingerprintAuthSwitch.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                if((boolean) newValue){
                    lockManager.getAppLock().setFingerprintAuthEnabled(true);
                }
                else {
                    lockManager.getAppLock().setFingerprintAuthEnabled(false);
                }
                return true;
            }
        });

        securityStatus = findPreference("security_status");
        securityStatus.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                if((boolean) newValue){
                    lockManager.enableAppLock(getActivity().getApplicationContext(), SecurityPinActivity.class);
                    lockManager.getAppLock().setTimeout(100);
                    checkAndEnableSecurityCategoryPrefs();
                }
                else {
                    LockManager<SecurityPinActivity> lockManager = LockManager.getInstance();
                    lockManager.disableAppLock();

                    setupPin.setShouldDisableView(true);
                    securityQuestion.setShouldDisableView(true);
                    fingerprintAuthSwitch.setShouldDisableView(true);

                    setupPin.setEnabled(false);
                    securityQuestion.setEnabled(false);
                    fingerprintAuthSwitch.setEnabled(false);
                }
                return true;
            }
        });
        checkAndEnableSecurityCategoryPrefs();

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        switch (requestCode){
            case REQUEST_CODE_SETUP_PIN:
                checkAndEnableSecurityCategoryPrefs();

                securityStatus.setChecked(true);
                fingerprintAuthSwitch.setChecked(true);
                break;
        }
    }

    private void checkAndEnableSecurityCategoryPrefs(){
        if(lockManager.getAppLock() != null){
            if(lockManager.getAppLock().isPasscodeSet()){
                setupPin.setShouldDisableView(false);
                securityQuestion.setShouldDisableView(false);
                securityStatus.setShouldDisableView(false);
                fingerprintAuthSwitch.setShouldDisableView(false);

                setupPin.setEnabled(true);
                securityQuestion.setEnabled(true);
                securityStatus.setEnabled(true);
                fingerprintAuthSwitch.setEnabled(true);
            }
            else {
                setupPin.setShouldDisableView(false);
                setupPin.setEnabled(true);
            }
        }
        if(!lockManager.isAppLockEnabled()){
            securityStatus.setShouldDisableView(false);
            securityStatus.setEnabled(true);
        }
    }
}