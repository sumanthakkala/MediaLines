package com.sumanthakkala.medialines.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.viewpager2.widget.ViewPager2;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.divyanshu.draw.widget.Line;
import com.google.android.material.button.MaterialButton;
import com.sumanthakkala.medialines.R;
import com.sumanthakkala.medialines.adapters.OnboardingAdapter;
import com.sumanthakkala.medialines.viewmodels.OnboardingItem;

import java.util.ArrayList;
import java.util.List;

public class OnboardingActivity extends AppCompatActivity {

    private OnboardingAdapter onboardingAdapter;
    private LinearLayout onboardingIndicatorsLayout;
    private MaterialButton onBoardingActionButton;

    SharedPreferences appInstallSharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);
        appInstallSharedPref = getSharedPreferences("App_Install_Prefs", MODE_PRIVATE);
        onboardingIndicatorsLayout = findViewById(R.id.onboardingIndicatorLayout);
        onBoardingActionButton = findViewById(R.id.onboardingActionButton);
        setupOnboardingItems();

        final ViewPager2 onboardingViewPager = findViewById(R.id.onboardingViewPager);
        onboardingViewPager.setAdapter(onboardingAdapter);

        setupOnboardingIndicators();
        setupCurrentOnboardingIndicator(0);

        onboardingViewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                setupCurrentOnboardingIndicator(position);
            }
        });

        onBoardingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(onboardingViewPager.getCurrentItem() + 1 < onboardingAdapter.getItemCount()){
                    onboardingViewPager.setCurrentItem(onboardingViewPager.getCurrentItem() + 1);
                }
                else {
                    SharedPreferences.Editor editor = appInstallSharedPref.edit();
                    editor.putBoolean(getString(R.string.is_first_ever_start_after_install), false);
                    editor.apply();
                    startActivity(new Intent(getApplicationContext(), MainActivity.class));
                    finish();
                }
            }
        });
    }

    private void setupOnboardingItems(){
        List<OnboardingItem> onboardingItems = new ArrayList<>();

        OnboardingItem itemOne = new OnboardingItem();
        itemOne.setImage(R.drawable.undraw_organise_notes);
        itemOne.setTitle("Modern Notes Organiser For This Modern World");
        itemOne.setDescription("Create and organise notes in a clean & beautiful way that meets modern standards.");
        onboardingItems.add(itemOne);

        OnboardingItem itemTwo = new OnboardingItem();
        itemTwo.setImage(R.drawable.undraw_multiple_inputs);
        itemTwo.setTitle("Multi Format Inputs");
        itemTwo.setDescription("Text | Images | Audio | Sketch | Speech | URLs | Tasks, what not? Add anything you wish to your notes.");
        onboardingItems.add(itemTwo);

        OnboardingItem itemThree = new OnboardingItem();
        itemThree.setImage(R.drawable.undraw_multi_color);
        itemThree.setTitle("Let Your Notes Wear The Colour Of Your Memory");
        itemThree.setDescription("Choose a color of your choice from colour palette to organise your notes.");
        onboardingItems.add(itemThree);

        OnboardingItem itemFOur = new OnboardingItem();
        itemFOur.setImage(R.drawable.undraw_export_pdf);
        itemFOur.setTitle("PDFs On The Go");
        itemFOur.setDescription("Export your notes to PDFs in just one click.");
        onboardingItems.add(itemFOur);

        OnboardingItem itemFive = new OnboardingItem();
        itemFive.setImage(R.drawable.undraw_security);
        itemFive.setTitle("Safe And Secure");
        itemFive.setDescription("Secure your notes with your fingerprint or a 4-digit PIN of your choice.");
        onboardingItems.add(itemFive);

        onboardingAdapter = new OnboardingAdapter(onboardingItems);
    }

    private void setupOnboardingIndicators(){
        ImageView[] indicators = new ImageView[onboardingAdapter.getItemCount()];
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
        );
        layoutParams.setMargins(8,0,8,0);
        for(int i = 0; i < indicators.length; i++){
            indicators[i] = new ImageView(getApplicationContext());
            indicators[i].setImageDrawable(ContextCompat.getDrawable(
                    getApplicationContext(),
                    R.drawable.onboarding_indicator_inactive
            ));
            indicators[i].setLayoutParams(layoutParams);
            onboardingIndicatorsLayout.addView(indicators[i]);
        }
    }

    private void setupCurrentOnboardingIndicator(int index){
        int childCount = onboardingIndicatorsLayout.getChildCount();
        for(int i = 0; i < childCount; i++){
            ImageView imageView = (ImageView) onboardingIndicatorsLayout.getChildAt(i);
            if(i == index){
                imageView.setImageDrawable((
                        ContextCompat.getDrawable(getApplicationContext(), R.drawable.onboarding_indicator_active)
                        ));
            }
            else {
                imageView.setImageDrawable((
                        ContextCompat.getDrawable(getApplicationContext(), R.drawable.onboarding_indicator_inactive)
                ));
            }
        }
        if(index == onboardingAdapter.getItemCount() - 1){
            onBoardingActionButton.setText("Start");
        }
        else {
            onBoardingActionButton.setText("Next");
        }
    }
}