package com.sumanthakkala.medialines.ui.settings;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreferenceCompat;

import com.github.omadahealth.lollipin.lib.managers.AppLock;
import com.github.omadahealth.lollipin.lib.managers.LockManager;
import com.sumanthakkala.medialines.R;
import com.sumanthakkala.medialines.activities.CreateNoteActivity;
import com.sumanthakkala.medialines.activities.MainActivity;
import com.sumanthakkala.medialines.activities.SecurityPinActivity;
import com.sumanthakkala.medialines.viewmodels.NoteAudioViewModel;
import com.sumanthakkala.medialines.viewmodels.NoteImageViewModel;

import java.io.File;

public class SettingsFragment extends PreferenceFragmentCompat {

    public static final int REQUEST_CODE_SETUP_PIN = 1;

    LockManager<SecurityPinActivity> lockManager;
    private SwitchPreferenceCompat themePreference;
    private SwitchPreferenceCompat securityStatus;
    private SwitchPreferenceCompat fingerprintAuthSwitch;
    private Preference setupPin;
    private Preference securityQuestion;
    private Preference removePin;

    private boolean isLockEnabled;

    private AlertDialog removePinDialog;
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey);
        getActivity().setTheme(R.style.SettingsFragmentStyle);
        isLockEnabled = PreferenceManager.getDefaultSharedPreferences(getContext()).getBoolean("security_status", false);
        lockManager = LockManager.getInstance();
        lockManager.enableAppLock(getActivity().getApplicationContext(), SecurityPinActivity.class);
        lockManager.getAppLock().setTimeout(100);
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
                    isLockEnabled = true;
                    checkAndEnableSecurityCategoryPrefs();
                }
                else {
                    isLockEnabled = false;
                    lockManager.disableAppLock();
                    setupPin.setShouldDisableView(true);
                    securityQuestion.setShouldDisableView(true);
                    fingerprintAuthSwitch.setShouldDisableView(true);
                    removePin.setShouldDisableView(true);

                    setupPin.setEnabled(false);
                    securityQuestion.setEnabled(false);
                    fingerprintAuthSwitch.setEnabled(false);
                    removePin.setEnabled(false);

                }
                return true;
            }
        });

        removePin = findPreference("remove_pin");
        removePin.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                showRemovePinDialog();
                return true;
            }
        });

        checkAndEnableSecurityCategoryPrefs();

    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        switch (requestCode){
            case REQUEST_CODE_SETUP_PIN:
                if(resultCode == -1){
                    checkAndEnableSecurityCategoryPrefs();

                    securityStatus.setChecked(true);
                    fingerprintAuthSwitch.setChecked(true);
                }
                break;
        }
    }

    private void checkAndEnableSecurityCategoryPrefs(){
        if(lockManager.getAppLock() != null){
            if(lockManager.getAppLock().isPasscodeSet() && isLockEnabled){
                setupPin.setShouldDisableView(false);
                securityQuestion.setShouldDisableView(false);
                securityStatus.setShouldDisableView(false);
                fingerprintAuthSwitch.setShouldDisableView(false);
                removePin.setShouldDisableView(false);

                setupPin.setEnabled(true);
                securityQuestion.setEnabled(true);
                securityStatus.setEnabled(true);
                fingerprintAuthSwitch.setEnabled(true);
                removePin.setEnabled(true);
            }
            else {
                if(isLockEnabled){
                    setupPin.setShouldDisableView(false);
                    setupPin.setEnabled(true);

                }
                else {
                    setupPin.setShouldDisableView(true);
                    setupPin.setEnabled(false);
                }

                securityQuestion.setShouldDisableView(true);
                fingerprintAuthSwitch.setShouldDisableView(true);
                removePin.setShouldDisableView(true);

                securityQuestion.setEnabled(false);
                fingerprintAuthSwitch.setEnabled(false);
                removePin.setEnabled(false);
            }
        }
        if(!lockManager.isAppLockEnabled()){
            securityStatus.setShouldDisableView(false);
            securityStatus.setEnabled(true);
        }

    }

    private void showRemovePinDialog(){
        if(removePinDialog == null){
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            View view = LayoutInflater.from(getActivity()).inflate(
                    R.layout.on_remove_pin,
                    (ViewGroup) getView().findViewById(R.id.onBackPressedDialogContainer)
            );
            builder.setView(view);
            removePinDialog = builder.create();

            if(removePinDialog.getWindow() != null){
                removePinDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
            }
            view.findViewById(R.id.yesRemovePinTV).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    lockManager.getAppLock().disableAndRemoveConfiguration();
                    securityStatus.setChecked(false);
                    fingerprintAuthSwitch.setChecked(false);
                    isLockEnabled = false;
                    checkAndEnableSecurityCategoryPrefs();
                    removePinDialog.dismiss();
                }
            });

            view.findViewById(R.id.cancelGoBackTV).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    removePinDialog.dismiss();
                }
            });
        }
        removePinDialog.show();
    }
}