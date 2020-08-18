package com.sumanthakkala.medialines.ui.settings;
import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.KeyguardManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.hardware.biometrics.BiometricManager;
import android.net.Uri;
import android.os.AsyncTask;
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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreferenceCompat;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;

import com.github.omadahealth.lollipin.lib.managers.AppLock;
import com.github.omadahealth.lollipin.lib.managers.LockManager;
import com.sumanthakkala.medialines.R;
import com.sumanthakkala.medialines.activities.SecurityPinActivity;
import com.sumanthakkala.medialines.activities.SplashActivity;
import com.sumanthakkala.medialines.services.BackupRestoreService;
import com.sumanthakkala.medialines.workers.AutoBackupWorker;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import static android.content.Context.KEYGUARD_SERVICE;
import static android.content.Context.MODE_PRIVATE;

public class SettingsFragment extends PreferenceFragmentCompat implements ActivityCompat.OnRequestPermissionsResultCallback {

    public static final int REQUEST_CODE_SETUP_PIN = 1;
    public static final int REQUEST_CODE_RESTORE_BACKUP = 2;
    public static final int REQUEST_STORAGE_PERMISSIONS_FOR_BACKUP = 3;
    public static final int REQUEST_STORAGE_PERMISSIONS_FOR_RESTORE = 4;

    LockManager<SecurityPinActivity> lockManager;
    private SwitchPreferenceCompat themePreference;
    private SwitchPreferenceCompat securityStatus;
    private SwitchPreferenceCompat fingerprintAuthSwitch;
    private SwitchPreferenceCompat autoBackupSwitch;
    private Preference setupPin;
    private Preference securityQuestion;
    private Preference removePin;
    private Preference backupNow;
    private Preference restoreNow;
    private PreferenceCategory backupRestoreCategory;
    SharedPreferences securitySharedPref;
    SharedPreferences backupSharedPref;

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
        backupSharedPref = requireContext().getSharedPreferences("Backup_Prefs", MODE_PRIVATE);
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

        backupRestoreCategory = findPreference("backup_restore");
        if(backupSharedPref.contains(getString(R.string.latest_backup_timestamp_key_in_prefs))){
            Calendar calendar = Calendar.getInstance();
            long latestBackupTimeInMillis = backupSharedPref.getLong(getString(R.string.latest_backup_timestamp_key_in_prefs), -1);

            if(latestBackupTimeInMillis != -1){
                calendar.setTimeInMillis(latestBackupTimeInMillis);
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMMM YYYY  HH:mm:ss");
                backupRestoreCategory.setSummary("Latest backup: " + dateFormat.format(calendar.getTime()));
            }
            else {
                backupRestoreCategory.setSummary("Latest backup: NA");
            }
        }

        autoBackupSwitch = findPreference("auto_backup_switch");
        autoBackupSwitch.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                if((boolean) newValue){
                    //Auto backup is enabled
                    enableAutoBackup();
                }
                else {
                    //Auto backup is disabled
                    disableAutoBackup();
                }
                return true;
            }
        });

        backupNow = findPreference("local_backup");
        backupNow.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                initBackup();
                return true;
            }
        });

        restoreNow = findPreference("local_restore");
        restoreNow.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                initRestore();
                return true;
            }
        });

        checkAndEnableSecurityCategoryPrefs();

    }
    @Override
    public void onActivityResult(final int requestCode, int resultCode, @Nullable final Intent data) {
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
            case REQUEST_CODE_RESTORE_BACKUP:
                if (requestCode == REQUEST_CODE_RESTORE_BACKUP && data != null) {
                    final Toast toast = Toast.makeText(requireContext(), "Restore process started.", Toast.LENGTH_SHORT);
                    @SuppressLint("StaticFieldLeak")
                    class RestoreNotesTask extends AsyncTask<Void, Void, Integer> {
                        @Override
                        protected Integer doInBackground(Void... voids) {
                            Uri fileUri = data.getData();
                            int result = new BackupRestoreService(requireContext()).restoreBackup(fileUri);
                            return result;
                        }

                        @Override
                        protected void onPostExecute(final Integer result) {
                            super.onPostExecute(result);
                            getActivity().runOnUiThread(new Runnable() {
                                public void run() {
                                    switch (result){
                                        case BackupRestoreService.RESTORE_SUCCESS:
                                            toast.cancel();
                                            Toast.makeText(requireContext(), "Restore Successful. Please restart the application.", Toast.LENGTH_LONG).show();
                                            requireActivity().finishAffinity();
                                            //restartApplication();
                                            break;
                                        case BackupRestoreService.RESTORE_FAILED:
                                            toast.cancel();
                                            Toast.makeText(requireContext(), "Restore failed. Please try again.", Toast.LENGTH_LONG).show();
                                            break;
                                        case BackupRestoreService.INVALID_BACKUP_FILE:
                                            toast.cancel();
                                            Toast.makeText(requireContext(), "Invalid backup file. Please try again.", Toast.LENGTH_LONG).show();
                                            break;
                                    }
                                }
                            });
                        }
                    }
                    toast.show();
                    new RestoreNotesTask().execute();
                }
                break;
        }
    }

    private void enableAutoBackup(){
        //Initially taking current backup
        initBackup();

        //Scheduling backups at 2 AM daily
        Calendar currentTimeStamp = Calendar.getInstance();
        Calendar dueTimeStamp = Calendar.getInstance();
        dueTimeStamp.set(Calendar.HOUR, 2);
        dueTimeStamp.set(Calendar.MINUTE, 0);
        dueTimeStamp.set(Calendar.SECOND, 0);
        dueTimeStamp.set(Calendar.MILLISECOND, 0);
        if(dueTimeStamp.before(currentTimeStamp)){
            dueTimeStamp.add(Calendar.HOUR, 24);
        }
        long timeDiff = dueTimeStamp.getTimeInMillis() - currentTimeStamp.getTimeInMillis();
        PeriodicWorkRequest periodicAutoBackupWorkRequest = new PeriodicWorkRequest.Builder(
                AutoBackupWorker.class,
                24,
                TimeUnit.HOURS
        ).addTag("medialines_auto_backup")
                .setInitialDelay(timeDiff, TimeUnit.MILLISECONDS)
                .build();
        WorkManager.getInstance(requireContext()).enqueueUniquePeriodicWork("medialines_auto_backup", ExistingPeriodicWorkPolicy.KEEP, periodicAutoBackupWorkRequest);

        Toast.makeText(requireContext(), "Auto backups are scheduled daily at 2 AM.", Toast.LENGTH_SHORT).show();
    }

    private void disableAutoBackup(){
        WorkManager.getInstance(requireContext()).cancelUniqueWork("medialines_auto_backup");
        Toast.makeText(requireContext(), "Auto backups are disabled.", Toast.LENGTH_SHORT).show();
    }

    private void restartApplication(){
        Intent mStartActivity = new Intent(requireContext(), SplashActivity.class);
        int mPendingIntentId = 123456;
        PendingIntent mPendingIntent = PendingIntent.getActivity(requireContext(), mPendingIntentId, mStartActivity, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager mgr = (AlarmManager) requireContext().getSystemService(Context.ALARM_SERVICE);
        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 10, mPendingIntent);
        System.exit(0);
    }

    public void initBackup(){
        if(ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
            final Toast toast = Toast.makeText(requireContext(), "Backup process started.", Toast.LENGTH_SHORT);
            @SuppressLint("StaticFieldLeak")
            class BackupNotesTask extends AsyncTask<Void, Void, Integer> {
                @Override
                protected Integer doInBackground(Void... voids) {
                    int result = new BackupRestoreService(requireContext()).initBackup();
                    return result;
                }

                @Override
                protected void onPostExecute(final Integer result) {
                    super.onPostExecute(result);
                    getActivity().runOnUiThread(new Runnable() {
                        public void run() {
                            switch (result){
                                case BackupRestoreService.BACKUP_SUCCESS:
                                    toast.cancel();
                                    updateBackupSharedPrefs();
                                    Toast.makeText(requireContext(), "Backup successful. File saved at \"Storage/MediaLines/Backups\"", Toast.LENGTH_LONG).show();
                                    break;
                                case BackupRestoreService.BACKUP_FAILED:
                                    toast.cancel();
                                    Toast.makeText(requireContext(), "Backup failed. Please try again.", Toast.LENGTH_LONG).show();
                                    break;
                            }
                        }
                    });
                }
            }
            toast.show();
            new BackupNotesTask().execute();
        }
        else {
            requestPermissions(
                    new String[]{
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                    },
                    REQUEST_STORAGE_PERMISSIONS_FOR_BACKUP
            );
        }
    }

    public void initRestore(){
        if(ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
            Intent i = new Intent(Intent.ACTION_GET_CONTENT);
            i.setType("*/*");
            startActivityForResult(Intent.createChooser(i, "Select DB File"), REQUEST_CODE_RESTORE_BACKUP);
        }
        else {
            requestPermissions(
                    new String[]{
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                    },
                    REQUEST_STORAGE_PERMISSIONS_FOR_RESTORE
            );
        }

    }

    private void updateBackupSharedPrefs(){
        Calendar calendar = Calendar.getInstance();
        SharedPreferences.Editor editor = backupSharedPref.edit();
        editor.putLong(requireContext().getString(R.string.latest_backup_timestamp_key_in_prefs), calendar.getTimeInMillis());
        editor.apply();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMMM YYYY  HH:mm:ss");

        backupRestoreCategory.setSummary("Latest backup: " + dateFormat.format(calendar.getTime()));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == REQUEST_STORAGE_PERMISSIONS_FOR_BACKUP && grantResults.length > 0){
            if(ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
                initBackup();
            }
            else {
                Toast.makeText(requireContext(), "Permission Denied.", Toast.LENGTH_SHORT).show();
            }
        }


        if(requestCode == REQUEST_STORAGE_PERMISSIONS_FOR_RESTORE && grantResults.length > 0){
            if(ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
                initRestore();
            }
            else {
                Toast.makeText(requireContext(), "Permission Denied.", Toast.LENGTH_SHORT).show();
            }
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