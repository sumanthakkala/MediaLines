package com.sumanthakkala.medialines.activities;

import androidx.appcompat.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.github.omadahealth.lollipin.lib.managers.AppLock;
import com.github.omadahealth.lollipin.lib.managers.AppLockActivity;
import com.github.omadahealth.lollipin.lib.managers.LockManager;
import com.sumanthakkala.medialines.R;

public class SecurityPinActivity extends AppLockActivity {

    public static final int REQUEST_CODE_RESET_PIN = 9;

    private AlertDialog securityQuestionsDialog;
    LockManager<SecurityPinActivity> lockManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        lockManager = LockManager.getInstance();
        lockManager.getAppLock().setLogoId(R.drawable.logo_hq);
        lockManager.getAppLock().setTimeout(100);

    }

    @Override
    public void showForgotDialog() {
        showSecurityQuestionsDialog();
    }

    @Override
    public void onPinFailure(int attempts) {

    }

    @Override
    public void onPinSuccess(int attempts) {

    }

    @Override
    public void onBackPressed() {
        this.finishAffinity();
    }

    private void showSecurityQuestionsDialog(){
        if(securityQuestionsDialog == null){
            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.AppTheme_NoActionBar));
            View view = LayoutInflater.from(getApplicationContext()).inflate(
                    R.layout.forgot_pin_dialog,
                    (ViewGroup) this.findViewById(R.id.forgotPinDialogContainer)
            );
            builder.setView(view);
            securityQuestionsDialog = builder.create();

            if(securityQuestionsDialog.getWindow() != null){
                securityQuestionsDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
            }

            TextView savedQuestionTV = view.findViewById(R.id.savedSecurityQuestionTV);

            SharedPreferences sharedPref = getSharedPreferences("Security_Prefs", MODE_PRIVATE);
            String savedQuestionStr = sharedPref.getString(getString(R.string.security_question_key_in_prefs), "NA");
            final String savedAnswerStr = sharedPref.getString(getString(R.string.security_answer_key_in_prefs), "NA");

            //Answer
            final EditText answer = view.findViewById(R.id.answer_edittext);

            TextView validateTV = view.findViewById(R.id.validateSecurityAnswerTV);


            if(savedQuestionStr.equals("NA") || savedAnswerStr.equals("NA")){
                savedQuestionTV.setText("Pin recovery setup is not configured. Cannot reset PIN in this situation.");
                validateTV.setText("OK");
                answer.setVisibility(View.GONE);
                validateTV.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        securityQuestionsDialog.dismiss();
                    }
                });
            }
            else {
                savedQuestionTV.setText(savedQuestionStr);
                validateTV.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(answer.getText().toString().isEmpty()){
                            Toast.makeText(getApplicationContext(), "Answer cannot be empty.", Toast.LENGTH_LONG).show();
                        }
                        else {
                            if(answer.getText().toString().equals(savedAnswerStr)){
                                lockManager.getAppLock().disableAndRemoveConfiguration();
                                Intent intent = new Intent(getApplicationContext(), SecurityPinActivity.class);
                                intent.putExtra(AppLock.EXTRA_TYPE, AppLock.ENABLE_PINLOCK);
                                startActivityForResult(intent, REQUEST_CODE_RESET_PIN);
                                Toast.makeText(getApplicationContext(), "For security reasons, please restart your app after resetting PIN.", Toast.LENGTH_LONG).show();
                                securityQuestionsDialog.dismiss();
                                finish();
                            }
                            else {
                                Toast.makeText(getApplicationContext(), "Answer incorrect. Please try again.", Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                });
            }
        }
        securityQuestionsDialog.show();
    }
}