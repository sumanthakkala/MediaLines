package com.sumanthakkala.medialines.ui.settings;
import android.app.KeyguardManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.hardware.biometrics.BiometricManager;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

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
import com.sumanthakkala.medialines.activities.SecurityPinActivity;

import static android.content.Context.KEYGUARD_SERVICE;
import static android.content.Context.MODE_PRIVATE;

public class SettingsFragment extends PreferenceFragmentCompat {

    public static final int REQUEST_CODE_SETUP_PIN = 1;

    LockManager<SecurityPinActivity> lockManager;
    private SwitchPreferenceCompat themePreference;
    private SwitchPreferenceCompat securityStatus;
    private SwitchPreferenceCompat fingerprintAuthSwitch;
    private Preference setupPin;
    private Preference securityQuestion;
    private Preference removePin;
    SharedPreferences securitySharedPref;

    private boolean isLockEnabled;
    String selectedQuestion = "";
    private AlertDialog removePinDialog;
    private AlertDialog securityQuestionsDialog;
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey);
        getActivity().setTheme(R.style.SettingsFragmentStyle);
        isLockEnabled = PreferenceManager.getDefaultSharedPreferences(getContext()).getBoolean("security_status", false);
        securitySharedPref = requireContext().getSharedPreferences("Security_Prefs", MODE_PRIVATE);
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
                startActivityForResult(intent, REQUEST_CODE_SETUP_PIN);
                return true;
            }
        });

        securityQuestion = findPreference("security_question");
        securityQuestion.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                showSecurityQuestionsDialog(false);
                return true;
            }
        });

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
                    securityStatus.setChecked(true);
                    fingerprintAuthSwitch.setChecked(true);
                    checkAndEnableSecurityCategoryPrefs();

                    if(!securitySharedPref.contains(getString(R.string.security_question_key_in_prefs)) || !securitySharedPref.contains(getString(R.string.security_answer_key_in_prefs))){
                        showSecurityQuestionsDialog(true);
                    }
                    else {
                        Toast.makeText(getContext(), "PIN changed successfully!", Toast.LENGTH_SHORT).show();
                    }
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

        if(!checkForBiometrics()){
            fingerprintAuthSwitch.setChecked(false);
            fingerprintAuthSwitch.setEnabled(false);
            fingerprintAuthSwitch.setShouldDisableView(true);
        }
    }

    private void showRemovePinDialog(){
        if(removePinDialog == null){
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            View view = LayoutInflater.from(getActivity()).inflate(
                    R.layout.on_remove_pin,
                    (ViewGroup) getView().findViewById(R.id.onRemovePinDialogContainer)
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
                    securitySharedPref.edit().clear().commit();
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

    private void showSecurityQuestionsDialog(final boolean isMandatory){
        if(securityQuestionsDialog == null){
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            View view = LayoutInflater.from(getActivity()).inflate(
                    R.layout.security_questions_setup_dialog,
                    (ViewGroup) getView().findViewById(R.id.securityQuestionsSetupDialogContainer)
            );
            builder.setView(view);
            securityQuestionsDialog = builder.create();
            securityQuestionsDialog.setCancelable(false);
            securityQuestionsDialog.setCanceledOnTouchOutside(false);
            if(securityQuestionsDialog.getWindow() != null){
                securityQuestionsDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
            }

            //Spinner
            Spinner questionsSpinner = view.findViewById(R.id.questions_spinner);
            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
                    R.array.security_questions_array, android.R.layout.simple_spinner_item);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            questionsSpinner.setAdapter(adapter);
            questionsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    selectedQuestion = adapterView.getItemAtPosition(i).toString();
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });

            //Answer
            final EditText answer = view.findViewById(R.id.answer_edittext);

            view.findViewById(R.id.saveAnswerTV).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if(answer.getText().toString().isEmpty()){
                        Toast.makeText(getContext(), "Answer cannot be empty.", Toast.LENGTH_LONG).show();
                    }
                    else {
                        SharedPreferences.Editor editor = securitySharedPref.edit();
                        editor.putString(getString(R.string.security_question_key_in_prefs), selectedQuestion);
                        editor.putString(getString(R.string.security_answer_key_in_prefs), answer.getText().toString());
                        editor.apply();
                        if(isMandatory){
                            Toast.makeText(getContext(), "PIN created successfully!", Toast.LENGTH_SHORT).show();
                        }
                        else {
                            Toast.makeText(getContext(), "Security question updated successfully.", Toast.LENGTH_LONG).show();
                        }
                        securityQuestionsDialog.dismiss();
                        securityQuestionsDialog = null;
                    }
                }
            });

            if(isMandatory){
                view.findViewById(R.id.cancelGoBackTV).setVisibility(View.GONE);
            } else {
                view.findViewById(R.id.cancelGoBackTV).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        answer.setText("");
                        securityQuestionsDialog.dismiss();
                        securityQuestionsDialog = null;
                    }
                });
            }


        }
        securityQuestionsDialog.show();
    }


    private boolean checkForBiometrics(){
        boolean canAuthenticate = true;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (Build.VERSION.SDK_INT < 29) {
                KeyguardManager keyguardManager  = (KeyguardManager) requireContext().getSystemService(KEYGUARD_SERVICE);
                PackageManager packageManager   = requireContext().getPackageManager();
                if(!packageManager.hasSystemFeature(PackageManager.FEATURE_FINGERPRINT)) {
                    canAuthenticate = false;
                }
                if (!keyguardManager.isKeyguardSecure()) {
                    canAuthenticate = false;
                }
            } else {
                BiometricManager biometricManager = getActivity().getSystemService(BiometricManager.class);
                if(biometricManager.canAuthenticate() != BiometricManager.BIOMETRIC_SUCCESS){
                    canAuthenticate = false;
                }
            }
        }else{
            canAuthenticate = false;
        }
        return canAuthenticate;
    }
}