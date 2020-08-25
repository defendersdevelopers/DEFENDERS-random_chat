package com.kyle.random.gems;
import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.android.billingclient.api.SkuDetailsResponseListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.reward.RewardItem;
import com.google.android.gms.ads.reward.RewardedVideoAd;
import com.google.android.gms.ads.reward.RewardedVideoAdListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.kyle.random.KEYS;
import com.kyle.random.R;
import com.kyle.random.other.BaseActivity;
import com.kyle.random.other.Libs;

import java.util.ArrayList;
import java.util.List;

import es.dmoral.toasty.Toasty;

import static com.kyle.random.other.Libs.getUserId;
public class gems_store extends BaseActivity {
    TextView gemsViewer;
    int gems;
    String myUid;
    Libs libs;
    View adContainer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gems_store);
        mRewardedVideoAd = MobileAds.getRewardedVideoAdInstance(this);
        mRewardedVideoAd.setRewardedVideoAdListener(new RewardedVideoAdListener() {
            @Override
            public void onRewardedVideoAdLoaded() {
            }
            @Override
            public void onRewardedVideoAdOpened() {
            }
            @Override
            public void onRewardedVideoStarted() {
            }
            @Override
            public void onRewardedVideoAdClosed() {
                mRewardedVideoAd.loadAd(getString(R.string.rewardedId),new AdRequest.Builder().build());

            }
            @Override
            public void onRewarded(RewardItem rewardItem) {
                new Libs(gems_store.this).addGemsToUser(rewardItem.getAmount() , getUserId());
            }
            @Override
            public void onRewardedVideoAdLeftApplication() {
            }
            @Override
            public void onRewardedVideoAdFailedToLoad(int i) {

                Toasty.info(getApplicationContext(), R.string.id_193, Toast.LENGTH_SHORT, true).show();
            }
            @Override
            public void onRewardedVideoCompleted() {
            }
        });
        mRewardedVideoAd.loadAd(getString(R.string.rewardedId),new AdRequest.Builder().build());



        gemsViewer = findViewById(R.id.gemsViewer);
        myUid = getUserId();
        libs = new Libs(this);
        loadMyData();
        listenToGems();
        createAdDialog();
        showProgress();


    }
    @Override
    public void onResume() {
        mRewardedVideoAd.resume(this);
        super.onResume();
    }

    @Override
    public void onPause() {
        mRewardedVideoAd.pause(this);
        super.onPause();
    }

    @Override
    public void onDestroy() {
        mRewardedVideoAd.destroy(this);
        super.onDestroy();
    }
    private RewardedVideoAd mRewardedVideoAd;

    List<SkuDetails> skuDetailsList;
    @Override
    public void onBillingInitDone() {
        super.onBillingInitDone();
        skuDetailsList = new ArrayList<>();

        List<String> skuList = new ArrayList<>();
        skuList.add("match_plus");
        skuList.add("gems_plus");
        skuList.add("vip_membership");
        skuList.add("gems_package_1");
        skuList.add("gems_package_2");
        skuList.add("gems_package_3");
        skuList.add("gems_package_4");
        skuList.add("gems_package_5");
        skuList.add("gems_package_6");
        final SkuDetailsParams.Builder params = SkuDetailsParams.newBuilder().setSkusList(skuList);

        billingClient.querySkuDetailsAsync(params.setType(BillingClient.SkuType.SUBS).build(),
                new SkuDetailsResponseListener() {
                    @Override
                    public void onSkuDetailsResponse(BillingResult billingResult,
                                                     List<SkuDetails> skuDetailsList) {
                        gems_store.this.skuDetailsList.addAll(skuDetailsList);
                        billingClient.querySkuDetailsAsync(params.setType(BillingClient.SkuType.INAPP).build(),
                                new SkuDetailsResponseListener() {
                                    @Override
                                    public void onSkuDetailsResponse(BillingResult billingResult,
                                                                     List<SkuDetails> skuDetailsList) {
                                        gems_store.this.skuDetailsList.addAll(skuDetailsList);
                                        dismissProgress();
                                        loadBuyButtons();
                                    }
                                });
                    }
                });
    }
    boolean isUserVip , isUserMatchPlus,isUserGemsPlus;
    public void loadBuyButtons(){
        if(skuDetailsList==null||skuDetailsList.size() ==0){
            return;
        }
        SkuDetails   vip = getSkuDetails("vip_membership");
        SkuDetails   match_plus = getSkuDetails("match_plus");
        SkuDetails   gems_plus = getSkuDetails("gems_plus");

        TextView vip_price = findViewById(R.id.vip_price);
        TextView match_plus_price = findViewById(R.id.match_plus_price);
        TextView gems_plus_price = findViewById(R.id.gems_plus_price);


      isUserVip = Boolean.parseBoolean(libs.getUserData("VIP"));
         isUserMatchPlus = Boolean.parseBoolean(libs.getUserData("match_plus"));
         isUserGemsPlus = Boolean.parseBoolean(libs.getUserData("gems_plus"));




        vip_price.setText(isUserVip ? getString(R.string.id_221) : vip.getPrice());
        match_plus_price.setText(isUserMatchPlus ?  getString(R.string.id_221) : match_plus.getPrice());
        gems_plus_price.setText(isUserGemsPlus ? getString(R.string.id_221) : gems_plus.getPrice());




        ///////////

        TextView gems_package_1_price = findViewById(R.id.gems_package_1_price);
        TextView gems_package_2_price = findViewById(R.id.gems_package_2_price);
        TextView gems_package_3_price = findViewById(R.id.gems_package_3_price);
        TextView gems_package_4_price = findViewById(R.id.gems_package_4_price);
        TextView gems_package_5_price = findViewById(R.id.gems_package_5_price);
        TextView gems_package_6_price = findViewById(R.id.gems_package_6_price);

        ///////////




        TextView gems_package_1_count = findViewById(R.id.gems_package_1_count);
        TextView gems_package_2_count = findViewById(R.id.gems_package_2_count);
        TextView gems_package_3_count = findViewById(R.id.gems_package_3_count);
        TextView gems_package_4_count = findViewById(R.id.gems_package_4_count);
        TextView gems_package_5_count = findViewById(R.id.gems_package_5_count);
        TextView gems_package_6_count = findViewById(R.id.gems_package_6_count);
        ///////////




        SkuDetails   gems_package_1 = getSkuDetails("gems_package_1");
        SkuDetails   gems_package_2 = getSkuDetails("gems_package_2");
        SkuDetails   gems_package_3 = getSkuDetails("gems_package_3");
        SkuDetails   gems_package_4 = getSkuDetails("gems_package_4");
        SkuDetails   gems_package_5 = getSkuDetails("gems_package_5");
        SkuDetails   gems_package_6 = getSkuDetails("gems_package_6");
        ///////////

// gems_package_1.getPriceAmountMicros()/1000000 + " "+ gems_package_1.getPriceCurrencyCode()
        gems_package_1_price.setText( gems_package_1.getPrice());
        gems_package_1_count.setText(gems_package_1.getDescription());

        gems_package_2_price.setText(gems_package_2.getPrice());
        gems_package_2_count.setText(gems_package_2.getDescription());

        gems_package_3_price.setText(gems_package_3.getPrice());
        gems_package_3_count.setText(gems_package_3.getDescription());

        gems_package_4_price.setText(gems_package_4.getPrice());
        gems_package_4_count.setText(gems_package_4.getDescription());

        gems_package_5_price.setText(gems_package_5.getPrice());
        gems_package_5_count.setText(gems_package_5.getDescription());

        gems_package_6_price.setText(gems_package_6.getPrice());
        gems_package_6_count.setText(gems_package_6.getDescription());


        findViewById(R.id.loadingPrices).setVisibility(View.GONE);


    }
    public SkuDetails getSkuDetails(String id) {
        for (SkuDetails skuDetails : skuDetailsList) {
            if (skuDetails.getSku().equals(id)) {
                return skuDetails;
            }
        }
        return null;
    }
    public void buySku(String id) {
        SkuDetails skuDetails = getSkuDetails(id);
        if (skuDetails != null) {
            BillingFlowParams billingFlowParams = BillingFlowParams.newBuilder()
                    .setSkuDetails(skuDetails)
                    .build();
            billingClient.launchBillingFlow(gems_store.this, billingFlowParams);
            return;
        } else {
            Toasty.error(this, R.string.id_222, Toast.LENGTH_SHORT, true).show();
        }
    }
    @Override
    public void onBillingUpdateUI(String token, String sku) {
        super.onBillingUpdateUI(token, sku);
    }
    @SuppressLint("MissingPermission")
    public void createAdDialog() {
        adContainer = findViewById(R.id.adContainer);
        AdView adView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);
    }
    public void loadMyData() {
        gems = Integer.parseInt(libs.getUserData("gems"));
        gemsViewer.setText(String.valueOf(gems));
    }
    public void VIP_Dialog(View view) {
        if(isUserVip){
            endSubscriptions("vip_membership");
            return;
        }
        SkuDetails   vip = getSkuDetails("vip_membership");
        libs.dialog_VIP(this ,vip);
    }

    public void match_plus(View view) {
        if(isUserMatchPlus){
            endSubscriptions("match_plus");
            return;
        }
        SkuDetails   match_plus = getSkuDetails("match_plus");
        libs.dialog_match_plus(this,match_plus);
    }
    public void gems_plus(View view) {
        if(isUserGemsPlus){
            endSubscriptions("gems_plus");
            return;
        }
        SkuDetails   gems_plus = getSkuDetails("gems_plus");
        libs.dialog_gems_plus(gems_plus.getPrice());
    }

    public void endSubscriptions(String sku){
        try {
            Intent myIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/account/subscriptions?sku="+sku+"&package=" + getPackageName()));
            startActivity(myIntent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, R.string.id_223,  Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }
    public void close(View view) {
        finish();
    }
    public void gems_package_1(View view) {
        buySku("gems_package_1");

    }
    public void gems_package_2(View view) {
        buySku("gems_package_2");

    }
    public void gems_package_3(View view) {
        buySku("gems_package_3");

    }
    public void gems_package_4(View view) {
        buySku("gems_package_4");

    }
    public void gems_package_5(View view) {
        buySku("gems_package_5");

    }
    public void gems_package_6(View view) {
        buySku("gems_package_6");

    }
    public void listenToGems() {
        FirebaseDatabase.getInstance().getReference("users").child(myUid).addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot user) {
                String gems = String.valueOf(user.child("gems").getValue());
                gemsViewer.setText(gems);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }
    public void earnGems(View view) {
        LayoutInflater factory = LayoutInflater.from(this);
        final View dialogView = factory.inflate(R.layout.dialog_earn_gems, null);
        final AlertDialog dialog = new AlertDialog.Builder(this).create();

        dialogView.findViewById(R.id.rate).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                adContainer.setVisibility(View.VISIBLE);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        adContainer.setVisibility(View.GONE);
                        if (libs.getUserData("rate").equals("false")) {
                            libs.addGemsToUser(5, getUserId());
                            libs.saveData("rate", "true");
                        }
                        Uri uri = Uri.parse("market://details?id=" + getApplication().getPackageName());
                        Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
                        goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                                Intent.FLAG_ACTIVITY_NEW_DOCUMENT |
                                Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                        try {
                            startActivity(goToMarket);
                        } catch (ActivityNotFoundException e) {
                            startActivity(new Intent(Intent.ACTION_VIEW,
                                    Uri.parse("http://play.google.com/store/apps/details?id=" + getApplication().getPackageName())));
                        }
                    }
                }, 2000);
            }
        });

        dialogView.findViewById(R.id.share).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                adContainer.setVisibility(View.VISIBLE);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        adContainer.setVisibility(View.GONE);
                        try {
                            Intent shareIntent = new Intent(Intent.ACTION_SEND);
                            shareIntent.setType("text/plain");
                            shareIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name));
                            String shareMessage = "\n" + getString(R.string.id_224)+"\n\n";
                            shareMessage = shareMessage + "https://play.google.com/store/apps/details?id=" + KEYS.APPLICATION_ID + "\n\n";
                            shareMessage += "\n"+getString(R.string.id_225)+" " + libs.getUserData("myInviteCode");
                            shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);
                            startActivity(Intent.createChooser(shareIntent, "choose one"));
                            startActivity(Intent.createChooser(shareIntent, "choose one"));
                        } catch (Exception e) {
                        }
                    }
                }, 2000);
            }
        });

        dialogView.findViewById(R.id.invite).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                adContainer.setVisibility(View.VISIBLE);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        adContainer.setVisibility(View.GONE);
                        String myInviteCode = libs.getUserData("myInviteCode");
                        libs.dialog_invite_friends(myInviteCode);
                    }
                }, 2000);
            }
        });

        dialogView.findViewById(R.id.video).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                adContainer.setVisibility(View.VISIBLE);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        adContainer.setVisibility(View.GONE);
                        if (mRewardedVideoAd.isLoaded()) {
                            mRewardedVideoAd.show();
                        }else{
                            Toasty.info(getApplicationContext(), R.string.id_247, Toast.LENGTH_SHORT, true).show();
                            dialog.show();
                        }
                    }
                }, 2000);


            }
        });

        dialogView.findViewById(R.id.close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.setView(dialogView);
        dialog.show();
    }
}
