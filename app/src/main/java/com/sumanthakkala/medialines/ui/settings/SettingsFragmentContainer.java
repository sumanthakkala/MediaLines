package com.sumanthakkala.medialines.ui.settings;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.RequestConfiguration;
import com.google.android.gms.ads.formats.UnifiedNativeAd;
import com.google.android.gms.ads.formats.UnifiedNativeAdView;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.sumanthakkala.medialines.R;
import com.sumanthakkala.medialines.database.MediaLinesDatabase;
import com.sumanthakkala.medialines.entities.NoteWithData;
import com.sumanthakkala.medialines.services.BillingService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SettingsFragmentContainer extends Fragment {
    private UnifiedNativeAd nativeAd;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View root = inflater.inflate(R.layout.fragment_settings_container, container, false);
        setHasOptionsMenu(true);
//        getActivity().getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
//            @Override
//            public void handleOnBackPressed() {
//                setEnabled(false);
//                remove();
//                NavController navController = Navigation.findNavController(root);
//                navController.popBackStack(R.id.nav_home, false);
//            }
//        });



        @SuppressLint("StaticFieldLeak")
        class LoadAdsTask extends AsyncTask<Void, Void, Void> {
            @Override
            protected Void doInBackground(Void... voids) {
                new RequestConfiguration.Builder().setTestDeviceIds(Arrays.asList("BA8E6970B742B98B35535F884CA6919A"));
                MobileAds.initialize(getContext(), new OnInitializationCompleteListener() {
                    @Override
                    public void onInitializationComplete(InitializationStatus initializationStatus) {

                    }
                });

                AdLoader.Builder builder = new AdLoader.Builder(getContext(), "ca-app-pub-3940256099942544/2247696110");
                builder.forUnifiedNativeAd(new UnifiedNativeAd.OnUnifiedNativeAdLoadedListener() {
                    @Override
                    public void onUnifiedNativeAdLoaded(UnifiedNativeAd unifiedNativeAd) {
                        if(unifiedNativeAd != null){
                            nativeAd = unifiedNativeAd;
                        }
                        CardView cardView = root.findViewById(R.id.settings_ad_container);
                        UnifiedNativeAdView adView = (UnifiedNativeAdView) getLayoutInflater().inflate(R.layout.custom_banner_ad_view, null);
                        populateAd(unifiedNativeAd, adView);
                        cardView.addView(adView);
                    }
                });

                AdLoader adLoader = builder.withAdListener(new AdListener(){
                    @Override
                    public void onAdFailedToLoad(LoadAdError loadAdError) {
                        super.onAdFailedToLoad(loadAdError);
                    }

                    @Override
                    public void onAdClosed() {
                        super.onAdClosed();

                    }
                }).build();

                adLoader.loadAd(new AdRequest.Builder().build());
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);

            }
        }
        if(!new BillingService(requireContext()).isPurchased()){
            new LoadAdsTask().execute();
        }

        return root;
    }

    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.findItem(R.id.search_view).setVisible(false);
    }

    private void populateAd(UnifiedNativeAd nativeAd, UnifiedNativeAdView adView){

        adView.setHeadlineView(adView.findViewById(R.id.primary));
        adView.setCallToActionView(adView.findViewById(R.id.cta));
        adView.setStarRatingView(adView.findViewById(R.id.rating_bar));
        adView.setIconView(adView.findViewById(R.id.icon));
        adView.setAdvertiserView(adView.findViewById(R.id.secondary));

        //Headline
        if(nativeAd.getHeadline() != null){
            ((TextView) adView.getHeadlineView()).setText(nativeAd.getHeadline());
            adView.getHeadlineView().setVisibility(View.VISIBLE);
        }
        else {
            adView.getHeadlineView().setVisibility(View.INVISIBLE);
        }

        //secondary
        if(nativeAd.getAdvertiser() != null){
            ((TextView) adView.getAdvertiserView()).setText(nativeAd.getAdvertiser());
            adView.getAdvertiserView().setVisibility(View.VISIBLE);
        }
        else {
            adView.getAdvertiserView().setVisibility(View.INVISIBLE);
        }

        //icon
        if(nativeAd.getIcon() != null){
            ((ImageView) adView.getIconView()).setImageDrawable(nativeAd.getIcon().getDrawable());
            adView.getIconView().setVisibility(View.VISIBLE);
        }
        else {
            adView.getIconView().setVisibility(View.INVISIBLE);
        }

        //rating
        if(nativeAd.getStarRating() != null){
            ((RatingBar) adView.getStarRatingView()).setRating(nativeAd.getStarRating().floatValue());
            adView.getStarRatingView().setVisibility(View.VISIBLE);
        }
        else {
            adView.getStarRatingView().setVisibility(View.GONE);
        }

        //cta
        if(nativeAd.getHeadline() != null){
            ((Button) adView.getCallToActionView()).setText(nativeAd.getCallToAction());
            adView.getCallToActionView().setVisibility(View.VISIBLE);
        }
        else {
            adView.getCallToActionView().setVisibility(View.INVISIBLE);
        }

        adView.setNativeAd(nativeAd);
    }

    @Override
    public void onDestroy() {
        if(nativeAd != null){
            nativeAd.destroy();
        }
        super.onDestroy();
    }

    @Override
    public void onAttachFragment(@NonNull Fragment childFragment) {
        super.onAttachFragment(childFragment);
    }
}