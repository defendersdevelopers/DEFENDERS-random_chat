package com.kyle.random.other;
import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;

import com.android.billingclient.api.AcknowledgePurchaseParams;
import com.android.billingclient.api.AcknowledgePurchaseResponseListener;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.ConsumeParams;
import com.android.billingclient.api.ConsumeResponseListener;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchaseHistoryRecord;
import com.android.billingclient.api.PurchaseHistoryResponseListener;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.android.billingclient.api.SkuDetailsResponseListener;
import com.facebook.login.LoginManager;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.kyle.random.R;
import com.kyle.random.chat.ChatRoom;
import com.kyle.random.friends.friendRequests;
import com.kyle.random.gems.gems_store;
import com.kyle.random.getStarted.get_started;
import com.kyle.random.main.fragmentViewer;
import com.kyle.random.other.helpers.PermissionsActivity;
import com.shasin.notificationbanner.Banner;
import com.sucho.placepicker.Constants;
import com.sucho.placepicker.MapType;
import com.sucho.placepicker.PlacePicker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;

import es.dmoral.toasty.Toasty;

import static com.kyle.random.other.Libs.getUserId;
public class BaseActivity extends PermissionsActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener, PurchasesUpdatedListener {
    public ProgressDialog progressDialog;
    public static void detectLocation(Activity activity, String user_lat, String user_lng) {
        try {
            PlacePicker.IntentBuilder intent = new PlacePicker.IntentBuilder()
                    .showLatLong(true)  // Show Coordinates in the Activity
                    .setMapZoom(12.0f)  // Map Zoom Level. Default: 14.0
                    .setAddressRequired(true) // Set If return only Coordinates if cannot fetch Address for the coordinates. Default: True
                    .hideMarkerShadow(true) // Hides the shadow under the map marker. Default: False
                    // .setMarkerDrawable(R.drawable.location) // todo Change the default Marker Image
                    .setMarkerImageImageColor(R.color.colorPrimary)
                    .setFabColor(R.color.colorAccent)
                    .setPrimaryTextColor(R.color.colorPrimary) // Change text color of Shortened Address
                    .setSecondaryTextColor(R.color.colorPrimaryDark) // Change text color of full Address
                    .setMapType(MapType.NORMAL);
            if (user_lat != null && user_lng != null) {
                intent.setLatLong(Double.parseDouble(user_lat), Double.parseDouble(user_lng));  // Initial Latitude and Longitude the Map will load into
            } else {
                Toast.makeText(activity, "لم يتم تحديد موقعك اتوماتيكيا حيث انك لم تقم بتفعيل ال gps", Toast.LENGTH_SHORT).show();
            }
            activity.startActivityForResult(intent.build(activity), Constants.PLACE_PICKER_REQUEST);
        } catch (Exception e) {
            Toast.makeText(activity, "getting your location... , please waite some seconds and try again", Toast.LENGTH_SHORT).show();
        }
    }
    public Libs libs;
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        libs = new Libs(this);
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });
        FirebaseDatabase.getInstance().getReference("data").child("email").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String email = snapshot.exists() ? String.valueOf(snapshot.getValue()) : "";
                libs.saveData("support_email", email);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
        billingClient = BillingClient.newBuilder(BaseActivity.this)
                .setListener(BaseActivity.this)
                .enablePendingPurchases()
                .build();
        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(BillingResult billingResult) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    // The BillingClient is ready. You can query purchases here.
                    onBillingInitDone();
                }
            }
            @Override
            public void onBillingServiceDisconnected() {
                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.
            }
        });
        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage(getString(R.string.id_247));
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1);
        mLocationRequest.setFastestInterval(1);
        mLocationRequest.setSmallestDisplacement(1);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        InAppNotifyReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getExtras() == null) {
                    return;
                }
                String title = intent.getStringExtra("title");
                String body = intent.getStringExtra("body");
                String activity = intent.getExtras().getString("activity", "");
                final ViewGroup viewGroup = (ViewGroup) ((ViewGroup) findViewById(android.R.id.content)).getChildAt(0);
                Banner banner = Banner.make(viewGroup, BaseActivity.this, Banner.SUCCESS, body, Banner.TOP);
                banner.setDuration(3000);
                ((TextView) banner.getBannerView().findViewById(R.id.labelSuccess)).setText(title);
                if (activity.equals("friendRequest")) {
                    banner.getBannerView().setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            startActivity(new Intent(getApplicationContext(), friendRequests.class));
                        }
                    });
                }
                if (activity.startsWith("chat")) {
                    final String chatWith = activity.split("~")[1];
                    if (BaseActivity.this instanceof ChatRoom) {
                        ChatRoom chatRoom = (ChatRoom) BaseActivity.this;
                        if (chatWith.equals(chatRoom.friendUid)) {
                            return;
                        }
                    }
                    banner.getBannerView().setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            startActivity(new Intent(getApplicationContext(), ChatRoom.class).putExtra("friendUid", chatWith));
                        }
                    });
                }
                if (BaseActivity.this instanceof fragmentViewer) {
                    fragmentViewer fragmentViewer = (fragmentViewer) BaseActivity.this;
                    if (fragmentViewer.oldFragment == fragmentViewer.layoutTwo) {
                        return;
                    }
                }
                banner.show();
            }
        };
        checkSuspended();
        SyncData();
    }
    public static String getHideName(String name) {
        StringBuilder phrase = new StringBuilder(name);
        try {
            Integer[] toDelete;
            if (name.length() == 0 || name.length() == 1) {
                toDelete = new Integer[]{};
            } else if (name.length() == 2 || name.length() == 3) {
                toDelete = new Integer[]{1};
            } else if (name.length() == 4 || name.length() == 5) {
                toDelete = new Integer[]{1, 3};
            } else {
                toDelete = new Integer[]{1, 4};
            }
            boolean lastIsX = false;
            for (int i : toDelete) {
                if (lastIsX) {
                    phrase.setCharAt(i, '*');
                    lastIsX = false;
                } else {
                    phrase.setCharAt(i, 'X');
                    lastIsX = true;
                }
            }
        } catch (Exception e) {
        }
        return phrase.toString();
    }
    public void logout() {
        LoginManager.getInstance().logOut();
        GoogleSignInOptions gso = new GoogleSignInOptions.
                Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).
                build();
        GoogleSignIn.getClient(this, gso).signOut();
        getSharedPreferences("data", MODE_PRIVATE).edit().clear().apply();
        FirebaseAuth.getInstance().signOut();
        finish();
        Intent intent = new Intent(getApplicationContext(), get_started.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 7172;
    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;
    @Override
    protected void onStart() {
        super.onStart();
        if (mGoogleApiClient != null)
            mGoogleApiClient.connect();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("InAppNotify");
        registerReceiver(InAppNotifyReceiver, intentFilter);
    }
    @Override
    protected void onStop() {
        if (mGoogleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }
        try {
            unregisterReceiver(InAppNotifyReceiver);
        } catch (Exception e) {
        }
        super.onStop();
    }
    private synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();
        mGoogleApiClient.connect();
    }
    private boolean checkPlayServices() {
        int resultCode = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this, PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Toasty.info(BaseActivity.this, "This device is not supported", Toast.LENGTH_SHORT, true).show();
                finish();
            }
            return false;
        }
        return true;
    }
    @SuppressLint("MissingPermission")
    private void startLocationUpdates() {
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }
    private void stopLocationUpdates() {
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected())
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopLocationUpdates();
    }
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        //To check whether location settings are good to proceed or not.
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(mLocationRequest);
        builder.setAlwaysShow(true);
        Task<LocationSettingsResponse> task = LocationServices.getSettingsClient(this).checkLocationSettings(builder.build());
        task.addOnCompleteListener(new OnCompleteListener<LocationSettingsResponse>() {
            @Override
            public void onComplete(Task<LocationSettingsResponse> task) {
                try {
                    LocationSettingsResponse response = task.getResult(ApiException.class);
                    startGettingLocation();
                } catch (ApiException exception) {
                    switch (exception.getStatusCode()) {
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                            // Location settings are not satisfied. But could be fixed by showing the
                            // user a dialog.
                            try {
                                // Cast to a resolvable exception.
                                ResolvableApiException resolvable = (ResolvableApiException) exception;
                                // Show the dialog by calling startResolutionForResult(),
                                // and check the result in onActivityResult().
                                resolvable.startResolutionForResult(
                                        BaseActivity.this,
                                        1001);
                            } catch (IntentSender.SendIntentException e) {
                                // Ignore the error.
                            } catch (ClassCastException e) {
                                // Ignore, should be an impossible error.
                            }
                            break;
                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                            break;
                    }
                }
            }
        });
    }
    @SuppressLint("MissingPermission")
    public void startGettingLocation() {
        setLocation(LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient));
        startLocationUpdates();
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 1001:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        startGettingLocation();
                        break;
                    case Activity.RESULT_CANCELED:
                        dismissProgress();
                        setupLocationManager(onGetLocationListener);
                        Toasty.info(BaseActivity.this, R.string.id_272, Toast.LENGTH_SHORT, true).show();
                        break;
                    default:
                        break;
                }
                break;
        }
    }
    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }
    @Override
    public void onLocationChanged(Location location) {
        setLocation(location);
    }
    public interface OnGetLocationListener {
        void OnGetLocation(String lat, String lng);
    }
    BroadcastReceiver InAppNotifyReceiver;
    public BillingClient billingClient;
    @Override
    public void onPurchasesUpdated(BillingResult billingResult, List<Purchase> purchases) {
        int responseCode = billingResult.getResponseCode();
        if (responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            for (Purchase purchase : purchases) {
                boolean canProcess =  purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED &&  !purchase.isAcknowledged();



                handlePurchase(purchase.getSku(), purchase.getPurchaseToken(), purchase.getPurchaseTime(), true, canProcess);
            }
        } else if (responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
            Toasty.info(getApplicationContext(), R.string.id_273, Toast.LENGTH_SHORT, true).show();
        } else if (responseCode == BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED) {
            purchaseHistoryAsync();
        }
    }
    private void handlePurchase(final String sku, final String token, long purchaseTime, final boolean showError, boolean canProcess) {
        boolean isGems = sku.startsWith("gems_package");
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("packageName", getPackageName());
        hashMap.put("productId", sku);
        hashMap.put("purchaseToken", token);
        hashMap.put("userId", getUserId());
        FirebaseDatabase.getInstance().getReference().child("purchases").child(String.valueOf(purchaseTime)).updateChildren(hashMap);
        if (isGems) {
            billingClient.consumeAsync(ConsumeParams.newBuilder().setPurchaseToken(token).build(), new ConsumeResponseListener() {
                @Override
                public void onConsumeResponse(@NonNull BillingResult billingResult, @NonNull String s) {
                    HandelResponse(billingResult, sku, token, showError);
                }
            });
        } else {
            if (canProcess) {
                AcknowledgePurchaseParams acknowledgePurchaseParams =
                        AcknowledgePurchaseParams.newBuilder()
                                .setPurchaseToken(token)
                                .build();
                billingClient.acknowledgePurchase(acknowledgePurchaseParams, new AcknowledgePurchaseResponseListener() {
                    @Override
                    public void onAcknowledgePurchaseResponse(@NonNull BillingResult billingResult) {
                        HandelResponse(billingResult, sku, token, showError);
                    }
                });
            }
        }
    }
    public void HandelResponse(BillingResult billingResult, final String sku, final String token, final boolean showError) {
        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
            if (sku.startsWith("gems_package")) {
                showProgress();
                List<String> skuList = new ArrayList<>();
                skuList.add(sku);
                billingClient.querySkuDetailsAsync(SkuDetailsParams.newBuilder().setSkusList(skuList).setType(BillingClient.SkuType.INAPP).build(),
                        new SkuDetailsResponseListener() {
                            @Override
                            public void onSkuDetailsResponse(BillingResult billingResult,
                                                             List<SkuDetails> skuDetailsList) {
                                dismissProgress();
                                if (skuDetailsList == null) {
                                    Toasty.info(getApplicationContext(), R.string.id_255, Toast.LENGTH_SHORT, true).show();
                                    return;
                                }
                                try {
                                    libs.addGemsToUser(Integer.parseInt(skuDetailsList.get(0).getDescription()), getUserId());
                                    if (showError) {
                                        Toasty.success(getApplicationContext(), R.string.id_274, Toast.LENGTH_SHORT, true).show();
                                    }
                                } catch (Exception e) {
                                    if (showError) {
                                        Toasty.error(BaseActivity.this, R.string.id_222, Toast.LENGTH_SHORT, true).show();
                                    }
                                }
                                onBillingUpdateUI(token, sku);
                            }
                        });
            } else {
                if (showError) {
                    Toasty.success(getApplicationContext(), R.string.id_274, Toast.LENGTH_SHORT, true).show();
                }
                onBillingUpdateUI(token, sku);
            }
        } else {
            if (showError) {
                Toasty.error(getApplicationContext(), getString(R.string.id_275) + billingResult.getResponseCode(), Toast.LENGTH_SHORT, true).show();
            }
        }
    }
    public void onBillingInitDone() {
    }
    public void onBillingUpdateUI(String token, String sku) {
    }
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void checkSuspended() {
        if (getUserId().isEmpty()) {
            return;
        }
        LayoutInflater factory = LayoutInflater.from(this);
        final View dialogView = factory.inflate(R.layout.alert_suspended, null);
        final AlertDialog dialog = new AlertDialog.Builder(this).create();
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawableResource(android.R.color.transparent);
        ((TextView) dialogView.findViewById(R.id.email)).setText(libs.getUserData("support_email"));
        TextView logout = dialogView.findViewById(R.id.logout);
        TextView tryAgain = dialogView.findViewById(R.id.tryAgain);
        dialog.setCancelable(false);
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                logout();
            }
        });
        tryAgain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Toasty.error(getApplicationContext(), R.string.id_276, Toast.LENGTH_SHORT, true).show();
                    }
                }, 500);
            }
        });
        dialog.setView(dialogView);
        FirebaseDatabase.getInstance().getReference("users").child(getUserId()).child("suspended").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean suspended = snapshot.exists() && (boolean) snapshot.getValue();
                if (suspended) {
                    if (!dialog.isShowing()) {
                        dialog.show();
                    }
                } else {
                    if (dialog.isShowing()) {
                        dialog.dismiss();
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }
    public void purchaseHistoryAsync() {
        billingClient.queryPurchaseHistoryAsync(BillingClient.SkuType.SUBS, new PurchaseHistoryResponseListener() {
            @Override
            public void onPurchaseHistoryResponse(@NonNull BillingResult billingResult, @Nullable List<PurchaseHistoryRecord> purchases) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && purchases != null) {
                    for (PurchaseHistoryRecord purchase : purchases) {




                        handlePurchase(purchase.getSku(), purchase.getPurchaseToken(), purchase.getPurchaseTime(), false,true);
                    }
                }
            }
        });
        billingClient.queryPurchaseHistoryAsync(BillingClient.SkuType.INAPP, new PurchaseHistoryResponseListener() {
            @Override
            public void onPurchaseHistoryResponse(@NonNull BillingResult billingResult, @Nullable List<PurchaseHistoryRecord> purchases) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && purchases != null) {
                    for (PurchaseHistoryRecord purchase : purchases) {
                        handlePurchase(purchase.getSku(), purchase.getPurchaseToken(), purchase.getPurchaseTime(), false,true);
                    }
                }
            }
        });
    }
    boolean requestLocation = true;
    boolean getLocation = true;
    public void SyncData() {
        FirebaseDatabase.getInstance().getReference("users").child(getUserId()).addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot user) {
                if (!user.exists() || getUserId().isEmpty()) {
                    return;
                }
                try {
                    String myInviteCode = "";
                    try {
                        myInviteCode = String.valueOf(user.child("myInviteCode").getValue());
                    } catch (Exception e) {
                        myInviteCode = "";
                    }
                    String favoriteCountry = String.valueOf(user.child("favoriteCountry").getValue());
                    libs.saveData("favoriteCountry", favoriteCountry);
                    libs.saveData("myInviteCode", myInviteCode);
                    String age = libs.getAge(user);
                    String brief = String.valueOf(user.child("brief").getValue());
                    String city = String.valueOf(user.child("city").getValue());
                    String country = String.valueOf(user.child("country").getValue());
                    String username = String.valueOf(user.child("userName").getValue());
                    int gems = Integer.parseInt(String.valueOf(user.child("gems").getValue()));
                    boolean match_plus = (boolean) user.child("match_plus").getValue();
                    boolean gems_plus = !String.valueOf(user.child("gems_plus").getValue()).equals("0");
                    boolean VIP = (boolean) user.child("VIP").getValue();
                    double lat;
                    double lng;
                    try {
                        lat = (double) user.child("lat").getValue();
                        lng = (double) user.child("lng").getValue();
                    } catch (Exception e) {
                        lat = 0;
                        lng = 0;
                        if (requestLocation) {
                            requestLocation = false;
                            getAddress(new OnGetAddressListener() {
                                @Override
                                public void OnGetAddress(String country, String city, double lat, double lng) {
                                    if (getLocation) {
                                        getLocation = false;
                                        HashMap<String, Object> hashMap = new HashMap<>();
                                        hashMap.put("lat", lat);
                                        hashMap.put("lng", lng);
                                        FirebaseDatabase.getInstance().getReference("users").child(getUserId()).updateChildren(hashMap);
                                    }
                                }
                            });
                        }
                    }
                    String name = String.valueOf(user.child("name").getValue());
                    String favoriteGender = String.valueOf(user.child("favoriteGender").getValue());
                    String profileImage = String.valueOf(user.child("profileImage").getValue());
                    String likes = String.valueOf(user.child("likes").getChildrenCount());
                    String love = String.valueOf(user.child("love").getChildrenCount());
                    String birthdayDate = String.valueOf(user.child("birthdayDate").getValue());
                    //
                    libs.saveData("gems", String.valueOf(gems));
                    libs.saveData("age", age);
                    libs.saveData("brief", brief);
                    libs.saveData("city", "city");
                    libs.saveData("country", country);
                    libs.saveData("myInviteCode", myInviteCode);
                    libs.saveData("profileImage", profileImage);
                    libs.saveData("favoriteCountry", favoriteCountry);
                    libs.saveData("favoriteGender", favoriteGender);
                    libs.saveData("likes", likes);
                    libs.saveData("love", love);
                    libs.saveData("match_plus", String.valueOf(match_plus));
                    libs.saveData("gems_plus", String.valueOf(gems_plus));
                    libs.saveData("VIP", String.valueOf(VIP));
                    libs.saveData("name", name);
                    libs.saveData("username", username);
                    libs.saveData("birthdayDate", birthdayDate);
                    libs.saveData("lat", String.valueOf(lat));
                    libs.saveData("lng", String.valueOf(lng));
                    if (BaseActivity.this instanceof gems_store) {
                        ((gems_store) BaseActivity.this).loadBuyButtons();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    // user didn't signed yet
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }
    public static boolean isValidEmail(String email) {
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\." +
                "[a-zA-Z0-9_+&*-]+)*@" +
                "(?:[a-zA-Z0-9-]+\\.)+[a-z" +
                "A-Z]{2,7}$";
        Pattern pat = Pattern.compile(emailRegex);
        if (email == null)
            return false;
        return pat.matcher(email).matches();
    }
    OnGetLocationListener onGetLocationListener;
    @SuppressLint("MissingPermission")
    public void setupLocationManager(final OnGetLocationListener onGetLocationListener) {
        new Handler(getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                requestAppPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, new BaseActivity.OnPermissionsGrantedListener() {
                    @Override
                    public void onPermissionsGranted() {
                        try {
                            LocationManager locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
                            if (locationManager != null) {
                                Location bestLocation = null;
                                try {
                                    Criteria mFineCriteria = new Criteria();
                                    mFineCriteria.setAccuracy(Criteria.ACCURACY_FINE);
                                    mFineCriteria.setHorizontalAccuracy(Criteria.ACCURACY_HIGH);
                                    mFineCriteria.setBearingAccuracy(Criteria.ACCURACY_HIGH);
                                    mFineCriteria.setBearingRequired(true);
                                    String provider = locationManager.getBestProvider(mFineCriteria, true);
                                    if (provider != null) {
                                        bestLocation = locationManager.getLastKnownLocation(provider);
                                    }
                                } catch (Exception e) {
                                }
                                if (bestLocation == null) {
                                    List<String> list = locationManager.getAllProviders();
                                    if (list.isEmpty()) {
                                        Toasty.info(BaseActivity.this, R.string.id_277, Toast.LENGTH_SHORT, true).show();
                                    } else {
                                        for (String provider : list) {
                                            Location i = locationManager.getLastKnownLocation(provider);
                                            if (i == null) {
                                                continue;
                                            }
                                            if (bestLocation == null || i.getAccuracy() < bestLocation.getAccuracy()) {
                                                bestLocation = i;
                                            }
                                        }
                                    }
                                }
                                if (bestLocation == null) {
                                    BaseActivity.this.onGetLocationListener = onGetLocationListener;
                                    showProgress();
                                    if (checkPlayServices()) {
                                        buildGoogleApiClient();
                                    }
                                } else {
                                    onGetLocationListener.OnGetLocation(String.valueOf(bestLocation.getLatitude()), String.valueOf(bestLocation.getLongitude()));
                                }
                            }
                        } catch (Exception e) {
                            setupLocationManager(onGetLocationListener);
                        }
                    }
                });
            }
        }, 500);
    }
    @Override
    protected void onResume() {
        super.onResume();
        checkPlayServices();
    }
    public void showProgress() {
        if (!progressDialog.isShowing()) {
            progressDialog.show();
        }
    }
    boolean showLocatedToast = true;
    public void setLocation(Location location) {
        if (location != null) {
            dismissProgress();
            if (showLocatedToast) {
                showLocatedToast = false;
            }
            onGetLocationListener.OnGetLocation(String.valueOf(location.getLatitude()), String.valueOf(location.getLongitude()));
        }
    }
    public void dismissProgress() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }
    public interface OnGetAddressListener {
        void OnGetAddress(String country, String city, double lat, double lng);
    }
    public void getAddress(final OnGetAddressListener onGetAddressListener) {
        setupLocationManager(new OnGetLocationListener() {
            @Override
            public void OnGetLocation(String lat, String lng) {
                double latD = Double.parseDouble(lat);
                double lngD = Double.parseDouble(lng);
                Geocoder geoCoder = new Geocoder(BaseActivity.this, Locale.getDefault());
                if (Geocoder.isPresent()) {
                    try {
                        List<Address> addresses = geoCoder.getFromLocation(Double.parseDouble(lat), Double.parseDouble(lng), 1);
                        if (addresses.size() > 0) {
                            String country = addresses.get(0).getCountryName();
                            String city = addresses.get(0).getAdminArea();
                            onGetAddressListener.OnGetAddress(country, city, latD, lngD);
                        } else {
                            String country = libs.getCountryCodeLib();
                            String city = "Unknown";
                            onGetAddressListener.OnGetAddress(country, city, latD, lngD);
                        }
                    } catch (Exception e) {
                    }
                }
            }
        });
    }
}
