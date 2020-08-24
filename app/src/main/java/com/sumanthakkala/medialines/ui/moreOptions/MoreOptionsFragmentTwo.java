package com.sumanthakkala.medialines.ui.moreOptions;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
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
import com.google.android.gms.ads.formats.UnifiedNativeAd;
import com.google.android.gms.ads.formats.UnifiedNativeAdView;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.sumanthakkala.medialines.R;
import com.sumanthakkala.medialines.listeners.MoreOptionsListener;
import com.sumanthakkala.medialines.services.BillingService;

public class MoreOptionsFragmentTwo extends Fragment {
    Boolean isExistingNote;
    MoreOptionsListener moreOptionsListener;
    private UnifiedNativeAd nativeAd;

    public MoreOptionsFragmentTwo(Boolean isExistingNoteVal, MoreOptionsListener listener) {
        this.moreOptionsListener = listener;
        this.isExistingNote = isExistingNoteVal;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View root = inflater.inflate(R.layout.fragment_more_options_two, container, false);


//        if(isExistingNote){
//            root.findViewById(R.id.exportPdfLayout).setVisibility(View.VISIBLE);
//            root.findViewById(R.id.exportPdfLayout).setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    moreOptionsListener.onOptionCLicked(R.id.exportPdfLayout);
//                }
//            });
//        }
//        else {
//            root.findViewById(R.id.exportPdfLayout).setVisibility(View.GONE);
//        }


        root.findViewById(R.id.exportPdfLayout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                moreOptionsListener.onOptionCLicked(R.id.exportPdfLayout);
            }
        });

        root.findViewById(R.id.checkboxesOptionLayout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                moreOptionsListener.onOptionCLicked(R.id.checkboxesOptionLayout);
            }
        });

        root.findViewById(R.id.sketchOptionLayout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                moreOptionsListener.onOptionCLicked(R.id.sketchOptionLayout);
            }
        });

        if(isExistingNote){
            root.findViewById(R.id.infoNoteOptionLayout).setVisibility(View.VISIBLE);
            root.findViewById(R.id.infoNoteOptionLayout).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    moreOptionsListener.onOptionCLicked(R.id.infoNoteOptionLayout);
                }
            });
        }
        else {
            root.findViewById(R.id.infoNoteOptionLayout).setVisibility(View.GONE);
        }

        @SuppressLint("StaticFieldLeak")
        class LoadAdsTask extends AsyncTask<Void, Void, Void> {
            @Override
            protected Void doInBackground(Void... voids) {
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
                        CardView cardView = root.findViewById(R.id.more_options_ad_container);
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

}