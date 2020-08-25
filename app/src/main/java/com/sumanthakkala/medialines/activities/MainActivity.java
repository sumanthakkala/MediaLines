package com.sumanthakkala.medialines.activities;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Person;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.Menu;
import android.view.View;
import android.view.ViewParent;
import android.widget.Toast;

import com.android.billingclient.api.AcknowledgePurchaseParams;
import com.android.billingclient.api.AcknowledgePurchaseResponseListener;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchaseHistoryRecord;
import com.android.billingclient.api.PurchaseHistoryResponseListener;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.android.billingclient.api.SkuDetailsResponseListener;
import com.github.omadahealth.lollipin.lib.PinCompatActivity;
import com.sumanthakkala.medialines.R;
import com.google.android.material.navigation.NavigationView;
import com.sumanthakkala.medialines.services.BillingService;
import com.sumanthakkala.medialines.ui.about.AboutFragment;
import com.sumanthakkala.medialines.ui.home.HomeFragment;
import com.sumanthakkala.medialines.workers.AutoBackupWorker;
import com.sumanthakkala.medialines.workers.ResetAlarmsWorker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.core.view.MenuItemCompat;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.PreferenceManager;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private Menu actionBarMenu;
    private DrawerLayout drawer;
    SharedPreferences backupSharedPref;
    SharedPreferences purchasePrefs;
    private BillingClient billingClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        purchasePrefs = getSharedPreferences("Purchase_Prefs", MODE_PRIVATE);
        setContentView(R.layout.activity_main);
        setSystemControlDecorsByCurrentTheme();
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawer = findViewById(R.id.drawer_layout);
        final NavigationView navigationView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_archive, R.id.nav_settings, R.id.nav_buy_pro, R.id.nav_restore_purchase, R.id.nav_share, R.id.nav_about)
                .setDrawerLayout(drawer)
                .build();
        final NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                drawer.closeDrawer(GravityCompat.START);
                // You need this line to handle the navigation
                boolean handled = NavigationUI.onNavDestinationSelected(item, navController);
                if (!handled) {

                    switch (item.getItemId()){
                        case R.id.nav_buy_pro:
                            initiatePurchase();
                            break;
                        case R.id.nav_restore_purchase:
                            restorePurchase();
                            break;
                        case R.id.nav_share:
                            Intent shareIntent = new Intent();
                            shareIntent.putExtra(Intent.EXTRA_TEXT, "Hey, Is your notes app not up to the modern standards? \uD835\uDDE0\uD835\uDDF2\uD835\uDDF1\uD835\uDDF6\uD835\uDDEE \uD835\uDDDF\uD835\uDDF6\uD835\uDDFB\uD835\uDDF2\uD835\uDE00 got you covered! \nI am using this app and I would love to recommend this to you. \nDownload here \uD83D\uDC47 \nhttps://play.google.com/store/apps/details?id=com.sumanthakkala.medialines");
                            shareIntent.setType("text/plain");
                            shareIntent.setAction(Intent.ACTION_SEND);
                            startActivity(Intent.createChooser(shareIntent, "Share app to.."));
                            break;
                        case R.id.nav_review:
                            final String appPackageName = getPackageName(); // getPackageName() from Context or Activity object
                            try {
                                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                            } catch (android.content.ActivityNotFoundException anfe) {
                                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                            }
                            break;
                    }
                }

                return handled;


            }
        });

        //checking if user owns pro
        billingClient = BillingClient.newBuilder(this)
                .setListener(new PurchasesUpdatedListener() {
                    @Override
                    public void onPurchasesUpdated(@NonNull BillingResult billingResult, @Nullable List<Purchase> list) {
                        if(billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK){
                            handlePurchase(list.get(0));
                        }
                    }
                })
                .enablePendingPurchases()
                .build();

        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(BillingResult billingResult) {
                if (billingResult.getResponseCode() ==  BillingClient.BillingResponseCode.OK) {
                    // The BillingClient is ready. You can query purchases here.
//                    billingClient.queryPurchaseHistoryAsync(BillingClient.SkuType.INAPP, new PurchaseHistoryResponseListener() {
//                        @Override
//                        public void onPurchaseHistoryResponse(@NonNull BillingResult billingResult, @Nullable List<PurchaseHistoryRecord> list) {
//                            navigationView.getMenu().setGroupVisible(R.id.nav_purchase_group, false);
//                        }
//                    });
                    Purchase.PurchasesResult purchasesResult = billingClient.queryPurchases(BillingClient.SkuType.INAPP);
                    List<Purchase> purchaseList = purchasesResult.getPurchasesList();
                    if(purchaseList != null && purchaseList.size() != 0 && purchaseList.get(0).getPurchaseState() == Purchase.PurchaseState.PURCHASED){
                        SharedPreferences.Editor editor = purchasePrefs.edit();
                        editor.putBoolean(getString(R.string.is_pro_purchased), true);
                        editor.apply();
                        navigationView.getMenu().setGroupVisible(R.id.nav_purchase_group, false);
                    }
                    else {
                        SharedPreferences.Editor editor = purchasePrefs.edit();
                        editor.putBoolean(getString(R.string.is_pro_purchased), false);
                        editor.apply();
                    }
                }
                else {
                    SharedPreferences.Editor editor = purchasePrefs.edit();
                    editor.putBoolean(getString(R.string.is_pro_purchased), false);
                    editor.apply();
                }
            }
            @Override
            public void onBillingServiceDisconnected() {
                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.
            }
        });
        performBackupJobs();

        PeriodicWorkRequest periodicWorkRequest = new PeriodicWorkRequest.Builder(
                ResetAlarmsWorker.class,
                15,
                TimeUnit.MINUTES
        ).addTag("medialines_reset_alarms").build();
        WorkManager.getInstance(this).enqueueUniquePeriodicWork("medialines_reset_alarms", ExistingPeriodicWorkPolicy.KEEP, periodicWorkRequest);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        actionBarMenu = menu;
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    private void initiatePurchase(){
        billingClient = BillingClient.newBuilder(this)
                .setListener(new PurchasesUpdatedListener() {
                    @Override
                    public void onPurchasesUpdated(@NonNull BillingResult billingResult, @Nullable List<Purchase> list) {
                        if(billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK){
                            handlePurchase(list.get(0));
                        }
                    }
                })
                .enablePendingPurchases()
                .build();

        //Start connection to billing client
        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(BillingResult billingResult) {
                if (billingResult.getResponseCode() ==  BillingClient.BillingResponseCode.OK) {
                    // The BillingClient is ready. You can query purchases here.
                    //init available products
                    List<String> skuList = new ArrayList<>();
                    skuList.add("purchase_pro");
                    SkuDetailsParams.Builder params = SkuDetailsParams.newBuilder();
                    params.setSkusList(skuList).setType(BillingClient.SkuType.INAPP);
                    billingClient.querySkuDetailsAsync(params.build(),
                            new SkuDetailsResponseListener() {
                                @Override
                                public void onSkuDetailsResponse(BillingResult billingResult,
                                                                 List<SkuDetails> skuDetailsList) {
                                    // Process the result.
                                    if(billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK){
                                        BillingFlowParams billingFlowParams = BillingFlowParams.newBuilder()
                                                .setSkuDetails(skuDetailsList.get(0))
                                                .build();
                                        int responseCode = billingClient.launchBillingFlow(MainActivity.this, billingFlowParams).getResponseCode();
                                    }
                                }
                            });
                }
                if(billingResult.getResponseCode() == BillingClient.BillingResponseCode.BILLING_UNAVAILABLE || billingResult.getResponseCode() == BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE){
                    Toast.makeText(getApplicationContext(), "Billing service unavailable", Toast.LENGTH_LONG).show();
                }
            }
            @Override
            public void onBillingServiceDisconnected() {
                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.
            }
        });

    }

    private void handlePurchase(Purchase purchase) {
        if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
            if (!purchase.isAcknowledged()) {
                AcknowledgePurchaseParams acknowledgePurchaseParams =
                        AcknowledgePurchaseParams.newBuilder()
                                .setPurchaseToken(purchase.getPurchaseToken())
                                .build();
                billingClient.acknowledgePurchase(acknowledgePurchaseParams, new AcknowledgePurchaseResponseListener() {
                    @Override
                    public void onAcknowledgePurchaseResponse(@NonNull BillingResult billingResult) {
                        if(billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK){

                        }
                    }
                });
                SharedPreferences.Editor editor = purchasePrefs.edit();
                editor.putBoolean(getString(R.string.is_pro_purchased), true);
                editor.apply();
                Toast.makeText(getApplicationContext(), "Thank you for purchasing Media Lines Pro.", Toast.LENGTH_LONG).show();
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                finish();
            }

        }
    }

    private void restorePurchase(){
        billingClient = BillingClient.newBuilder(this)
                .setListener(new PurchasesUpdatedListener() {
                    @Override
                    public void onPurchasesUpdated(@NonNull BillingResult billingResult, @Nullable List<Purchase> list) {
                        if(billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK){
                            handlePurchase(list.get(0));
                        }
                    }
                })
                .enablePendingPurchases()
                .build();

        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(BillingResult billingResult) {
                if (billingResult.getResponseCode() ==  BillingClient.BillingResponseCode.OK) {
                    // The BillingClient is ready. You can query purchases here.
                    Purchase.PurchasesResult purchasesResult = billingClient.queryPurchases(BillingClient.SkuType.INAPP);
                    List<Purchase> purchaseList = purchasesResult.getPurchasesList();
                    if(purchaseList != null && purchaseList.size() != 0 && purchaseList.get(0).getPurchaseState() == Purchase.PurchaseState.PURCHASED){
                        SharedPreferences.Editor editor = purchasePrefs.edit();
                        editor.putBoolean(getString(R.string.is_pro_purchased), true);
                        editor.apply();
                        Toast.makeText(getApplicationContext(), "Purchase restored successfully.", Toast.LENGTH_LONG).show();
                        startActivity(new Intent(getApplicationContext(), MainActivity.class));
                        finish();
                    }
                    else {
                        Toast.makeText(getApplicationContext(), "You have't purchased the product yet.", Toast.LENGTH_LONG).show();
                        SharedPreferences.Editor editor = purchasePrefs.edit();
                        editor.putBoolean(getString(R.string.is_pro_purchased), false);
                        editor.apply();
                    }
                }
                if(billingResult.getResponseCode() == BillingClient.BillingResponseCode.BILLING_UNAVAILABLE || billingResult.getResponseCode() == BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE){
                    Toast.makeText(getApplicationContext(), "Billing service unavailable", Toast.LENGTH_LONG).show();
                }
            }
            @Override
            public void onBillingServiceDisconnected() {
                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.
            }
        });


    }

    private void performBackupJobs(){
        backupSharedPref = getSharedPreferences("Backup_Prefs", MODE_PRIVATE);
        boolean isAutoBackupEnabled = backupSharedPref.getBoolean(getString(R.string.is_auto_backup_enabled_key_in_prefs), false);
        if(isAutoBackupEnabled){
            long latestBackupTimeInMillis = backupSharedPref.getLong(getString(R.string.latest_backup_timestamp_key_in_prefs), -1);
            if(latestBackupTimeInMillis != -1){
                Calendar lastBackupTimeStamp = Calendar.getInstance();
                lastBackupTimeStamp.setTimeInMillis(latestBackupTimeInMillis);

                Calendar pastDueTimeStamp = Calendar.getInstance();
                pastDueTimeStamp.set(Calendar.HOUR, 2);
                pastDueTimeStamp.set(Calendar.MINUTE, 0);
                pastDueTimeStamp.set(Calendar.SECOND, 0);
                pastDueTimeStamp.set(Calendar.MILLISECOND, 0);
                pastDueTimeStamp.set(Calendar.AM_PM, Calendar.AM);
                if(lastBackupTimeStamp.before(pastDueTimeStamp)){
                    //immedeiatly perform backup & reschedule job
                    WorkRequest immediateBackupRequest =
                            new OneTimeWorkRequest.Builder(AutoBackupWorker.class)
                                    .build();
                    WorkManager
                            .getInstance(this)
                            .enqueue(immediateBackupRequest);
                }
            }

            //Scheduling backups at 2AM daily
            Calendar currentTimeStamp = Calendar.getInstance();
            Calendar dueTimeStamp = Calendar.getInstance();
            dueTimeStamp.set(Calendar.HOUR, 2);
            dueTimeStamp.set(Calendar.MINUTE, 0);
            dueTimeStamp.set(Calendar.SECOND, 0);
            dueTimeStamp.set(Calendar.MILLISECOND, 0);
            dueTimeStamp.set(Calendar.AM_PM, Calendar.AM);
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
            WorkManager.getInstance(this).enqueueUniquePeriodicWork("medialines_auto_backup", ExistingPeriodicWorkPolicy.KEEP, periodicAutoBackupWorkRequest);
        }
    }

    public void openBrowser(View view){

        //Get url from tag
        String url = (String)view.getTag();

        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.addCategory(Intent.CATEGORY_BROWSABLE);

        //pass the url to intent data
        intent.setData(Uri.parse(url));

        startActivity(intent);
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
    public void onBackPressed() {
        if(drawer.isDrawerOpen(GravityCompat.START)){
            drawer.closeDrawer(GravityCompat.START);
        }
        else {
            super.onBackPressed();
        }
    }
}
