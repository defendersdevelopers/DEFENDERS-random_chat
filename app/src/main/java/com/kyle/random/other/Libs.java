package com.kyle.random.other;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.telephony.TelephonyManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.android.billingclient.api.SkuDetailsResponseListener;
import com.azoft.carousellayoutmanager.CenterScrollListener;
import com.github.chrisbanes.photoview.PhotoView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.kyle.random.KEYS;
import com.kyle.random.R;
import com.kyle.random.deeparAiVedioCall.AIFace;
import com.kyle.random.friends.meModel;
import com.kyle.random.gems.gems_store;
import com.kyle.random.getStarted.edit_acc_page;
import com.kyle.random.matches.matchesActivtiy;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import es.dmoral.toasty.Toasty;

import static android.content.Context.MODE_PRIVATE;
public class Libs {
    Context context;
    public SharedPreferences prefs;
    public Libs(Context context) {
        this.context = context;
        prefs = context.getSharedPreferences("data", MODE_PRIVATE);
    }
    public boolean isLoggedDone() {
        boolean b1 = !getUserId().isEmpty();
        boolean b2 = !getUserData("username").isEmpty();
        boolean b3 = !getUserData("birthdayDate").isEmpty();
        boolean b4 = !getUserData("Uid").isEmpty();
        return b1 && b2 && b3 && b4;
    }
    public static void setClipboard(Context context, String text) {
        android.content.ClipboardManager clipboard = (android.content.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        android.content.ClipData clip = android.content.ClipData.newPlainText("Copied Text", text);
        clipboard.setPrimaryClip(clip);
    }
    public static String getHobby(Context context, String id) {
        ArrayList<Hobby> hobbies = KEYS.getAllHobbies(context);
        for (Hobby hobby : hobbies) {
            if (hobby.key.equals(id)) {
                return hobby.name;
            }
        }
        return null;
    }
    public static int pxToDp(int px) {
        return (int) (px / Resources.getSystem().getDisplayMetrics().density);
    }
    public static int dpToPx(int dp) {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }
    public void saveData(String name, String value) {
        prefs.edit().putString(name, value).apply();
    }
    public String getUserData(String key) {
        return prefs.getString(key, "");
    }
    public static String getUserId() {
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            return FirebaseAuth.getInstance().getCurrentUser().getUid();
        }
        return "";
    }
    private static final String arabic = "\u06f0\u06f1\u06f2\u06f3\u06f4\u06f5\u06f6\u06f7\u06f8\u06f9";
    public static String arabicToDecimal(String number) {
        char[] chars = new char[number.length()];
        for (int i = 0; i < number.length(); i++) {
            char ch = number.charAt(i);
            if (ch >= 0x0660 && ch <= 0x0669)
                ch -= 0x0660 - '0';
            else if (ch >= 0x06f0 && ch <= 0x06F9)
                ch -= 0x06f0 - '0';
            chars[i] = ch;
        }
        return new String(chars);
    }
    public void setVipMode(Boolean vipMode) {
        ShowActivity("vip", vipMode);
        ShowActivity("classic", !vipMode);
    }
    public static String getTimeAgo(long lastActive) {
        String time_ago = "";
        try {
            long now = getTimeNow();
            long seconds = TimeUnit.MILLISECONDS.toSeconds(now - lastActive);
            long minutes = TimeUnit.MILLISECONDS.toMinutes(now - lastActive);
            long hours = TimeUnit.MILLISECONDS.toHours(now - lastActive);
            long days = TimeUnit.MILLISECONDS.toDays(now - lastActive);
            if (seconds < 60) {
                time_ago = seconds + " seconds ago";
            } else if (minutes < 60) {
                time_ago = minutes + " minutes ago";
            } else if (hours < 24) {
                time_ago = hours + " hours ago";
            } else {
                time_ago = days + " days ago";
            }
            if (seconds < 7) {
                return "Online";
            }
        } catch (Exception j) {
            j.printStackTrace();
            time_ago = "";
        }
        return "Online . " + time_ago;
    }
    public void ShowActivity(String activity_name, Boolean show) {
        context.getPackageManager().setComponentEnabledSetting(new ComponentName(context.getPackageName(), context.getPackageName() + "." + activity_name), show ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED : PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
    }
    public static long getTimeNow() {
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        cal.setTimeZone(TimeZone.getTimeZone("UTC"));
        return cal.getTimeInMillis();
    }
    public void addGemsToUser(final int gems, final String UID) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users").child(UID).child("gems");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int oldGems = Integer.parseInt(String.valueOf(dataSnapshot.getValue()));
                int newGems = gems + oldGems;
                FirebaseDatabase.getInstance().getReference("users").child(UID).child("gems").setValue(newGems);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }
    public void dialog_match_plus() {
        if (Boolean.parseBoolean(getUserData("match_plus"))) {
            Toasty.info(context, R.string.id_279, Toast.LENGTH_SHORT, true).show();
            return;
        }
        final BaseActivity baseActivity = ((BaseActivity) context);
        baseActivity.showProgress();
        ;
        List<String> skuList = new ArrayList<>();
        skuList.add("match_plus");
        baseActivity.billingClient.querySkuDetailsAsync(SkuDetailsParams.newBuilder().setSkusList(skuList).setType(BillingClient.SkuType.SUBS).build(),
                new SkuDetailsResponseListener() {
                    @Override
                    public void onSkuDetailsResponse(BillingResult billingResult,
                                                     List<SkuDetails> skuDetailsList) {
                        baseActivity.dismissProgress();
                        if (skuDetailsList == null) {
                            Toasty.info(context, R.string.id_255, Toast.LENGTH_SHORT, true).show();
                            return;
                        }
                        dialog_match_plus(baseActivity, skuDetailsList.get(0));
                    }
                });
    }
    public void dialog_match_plus(final BaseActivity baseActivity, final SkuDetails skuDetails) {
        if (Boolean.parseBoolean(getUserData("match_plus"))) {
            Toasty.info(context, R.string.id_279, Toast.LENGTH_SHORT, true).show();
            return;
        }
        LayoutInflater factory = LayoutInflater.from(context);
        final View myView = factory.inflate(R.layout.dialog_match_plus, null);
        final AlertDialog dialog_match_plus = new AlertDialog.Builder(context).create();
        TextView close = myView.findViewById(R.id.close);
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog_match_plus.cancel();
            }
        });
        TextView priceText = myView.findViewById(R.id.price);
        // PRICE per month
        priceText.setText(skuDetails.getPrice() + " / " + baseActivity.getString(R.string.id_280));
        FrameLayout next = myView.findViewById(R.id.next);
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog_match_plus.cancel();
                BillingFlowParams billingFlowParams = BillingFlowParams.newBuilder()
                        .setSkuDetails(skuDetails)
                        .build();
                baseActivity.billingClient.launchBillingFlow(baseActivity, billingFlowParams);
            }
        });
        //RecyclerView
        //  final CarouselLayoutManager layoutManager = new CarouselLayoutManager(CarouselLayoutManager.HORIZONTAL,true);
        final RecyclerView recyclerView = myView.findViewById(R.id.recycler_view);
        recyclerView.addOnScrollListener(new CenterScrollListener());
        //  layoutManager.setPostLayoutListener(new CarouselZoomPostLayoutListener());
        recyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, true));
        //   recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
        ArrayList<matchesActivtiy.item> images = new ArrayList<>();
        images.add(new matchesActivtiy.item(baseActivity.getString(R.string.id_281), baseActivity.getString(R.string.id_282), R.drawable.meme_search));
        images.add(new matchesActivtiy.item(baseActivity.getString(R.string.id_283), baseActivity.getString(R.string.id_284), R.drawable.meme_profiles));
        images.add(new matchesActivtiy.item(baseActivity.getString(R.string.id_285), baseActivity.getString(R.string.id_286), R.drawable.meme_calendar));
        images.add(new matchesActivtiy.item(baseActivity.getString(R.string.id_287), baseActivity.getString(R.string.id_288), R.drawable.meme_star));
        images.add(new matchesActivtiy.item(baseActivity.getString(R.string.id_289), baseActivity.getString(R.string.id_290), R.drawable.meme_gps));
        images.add(new matchesActivtiy.item(baseActivity.getString(R.string.id_291), baseActivity.getString(R.string.id_292), R.drawable.meme_so_like));
        images.add(new matchesActivtiy.item(baseActivity.getString(R.string.id_293), baseActivity.getString(R.string.id_294), R.drawable.meme_chat));
        recyclerView.setAdapter(new matchesActivtiy.myCuteAdapter(context, images));
        //  dialog_match_plus.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog_match_plus.setView(myView);
        dialog_match_plus.show();
    }
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void dialog_edit_account(final Activity activity, final meModel meModel) {
        LayoutInflater factory = LayoutInflater.from(activity);
        final View dialogView = factory.inflate(R.layout.view_my_info_dialog, null);
        final AlertDialog dialog = new AlertDialog.Builder(activity).create();
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawableResource(android.R.color.transparent);
        //setMyDataIntoTheDialog
        //findById
        FloatingActionButton edit = dialogView.findViewById(R.id.edit);
        ImageView profileImageViewer = dialogView.findViewById(R.id.profileImage);
        ImageView vipImage = dialogView.findViewById(R.id.vipImage);
        ImageView gemsPlusImage = dialogView.findViewById(R.id.gemsPlusImage);
        TextView inter_layout = dialogView.findViewById(R.id.inter_layout);
        final ImageView countryFlagViewer = dialogView.findViewById(R.id.countryFlag);
        final ImageView countryFlag2Viewer = dialogView.findViewById(R.id.countryFlag2);
        TextView nameViewer = dialogView.findViewById(R.id.name);
        TextView briefViewer = dialogView.findViewById(R.id.bio);
        TextView countryViewer = dialogView.findViewById(R.id.country);
        TextView localTime = dialogView.findViewById(R.id.localTime);
        String currentTime = new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(new Date());
        localTime.setText(currentTime);
        TextView country2Viewer = dialogView.findViewById(R.id.country2);
        TextView interestsViewer = dialogView.findViewById(R.id.interests);
        TextView likesViewer = dialogView.findViewById(R.id.likes);
        TextView loveViewer = dialogView.findViewById(R.id.love_viewer);
        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                activity.startActivity(new Intent(activity, edit_acc_page.class));
            }
        });
        RecyclerView imagesViewer = dialogView.findViewById(R.id.imagesViewer);
        imagesViewer.setLayoutManager(new LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false));
        imagesViewer.setHasFixedSize(true);
        imagesViewer.setAdapter(new RecyclerView.Adapter() {
            @NonNull
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                return new RecyclerView.ViewHolder(inflater.inflate(R.layout.item_image_no_rmove, parent, false)) {
                    @Override
                    public String toString() {
                        return super.toString();
                    }
                };
            }
            @Override
            public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {
                ImageView imageView = holder.itemView.findViewById(R.id.image);
                Picasso.get().load(meModel.images.get(position)).placeholder(R.drawable.loading).into(imageView);
                imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog_show_image(meModel.images.get(position));
                    }
                });
            }
            @Override
            public int getItemCount() {
                return meModel.images.size();
            }
        });
        if (meModel.images.isEmpty()) {
            imagesViewer.setVisibility(View.GONE);
        }
        //setData
        nameViewer.setText(meModel.name + "/" + meModel.age);
        Picasso.get().load(meModel.profileImage).into(profileImageViewer);
        briefViewer.setText(meModel.brief);
        countryViewer.setText(meModel.country);
        country2Viewer.setText(meModel.country);
        interestsViewer.setText(String.valueOf(meModel.interests));
        if (String.valueOf(meModel.interests).isEmpty()) {
            inter_layout.setVisibility(View.GONE);
        }
        loadCountries(new OnLoadListener() {
            @Override
            public void OnLoad(ArrayList<Country> countriesList) {
                Picasso.get().load(getCountryFlagWithCountryCode(countriesList, getUserData("userCountryCode"))).into(countryFlag2Viewer);
                Picasso.get().load(getCountryFlagWithCountryCode(countriesList, getUserData("userCountryCode"))).into(countryFlagViewer);
            }
        });
        likesViewer.setText(meModel.likes);
        loveViewer.setText(meModel.love);
        boolean VIP = !getUserData("VIP").equals("false");
        boolean gems_plus = !getUserData("gems_plus").equals("0");
        vipImage.setImageResource(VIP ? R.drawable.vip_azar : R.drawable.disabled_vip_azar);
        gemsPlusImage.setImageResource(gems_plus ? R.drawable.gems_plus_azar : R.drawable.disabled_gems_plus_azar);
        dialog.setView(dialogView);
        dialog.show();
    }
    public void dialog_view_profile(final Context activity, String uid) {
        //loadData
        FirebaseDatabase.getInstance().getReference("users").child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onDataChange(@NonNull final DataSnapshot user) {
                //todo getData
                String age = getAge(user);
                String brief = String.valueOf(user.child("brief").getValue());
                String country = String.valueOf(user.child("country").getValue());
                boolean gems_plus = String.valueOf(user.child("gems_plus").getValue()).equals("0");
                boolean VIP = (boolean) user.child("VIP").getValue();
                String name = String.valueOf(user.child("name").getValue());
                String profileImage = String.valueOf(user.child("profileImage").getValue());
                String likes = String.valueOf(user.child("likes").getChildrenCount());
                String love = String.valueOf(user.child("love").getChildrenCount());
                //todo show dialog
                LayoutInflater factory = LayoutInflater.from(activity);
                final View dialogView = factory.inflate(R.layout.view_profile_dialog, null);
                final AlertDialog dialog = new AlertDialog.Builder(activity).create();
                StringBuilder interests = new StringBuilder();
                for (DataSnapshot interest : user.child("hobbies").getChildren()) {
                    interests.append(" #");
                    interests.append(Libs.getHobby(activity, String.valueOf(interest.getValue())));
                }
                Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawableResource(android.R.color.transparent);
                //setMyDataIntoTheDialog
                //findById
                ImageView profileImageViewer = dialogView.findViewById(R.id.profileImage);
                ImageView vipImage = dialogView.findViewById(R.id.vipImage);
                ImageView gemsPlusImage = dialogView.findViewById(R.id.gemsPlusImage);
                TextView inter_layout = dialogView.findViewById(R.id.inter_layout);
                final ImageView countryFlagViewer = dialogView.findViewById(R.id.countryFlag);
                final ImageView countryFlag2Viewer = dialogView.findViewById(R.id.countryFlag2);
                TextView nameViewer = dialogView.findViewById(R.id.name);
                TextView briefViewer = dialogView.findViewById(R.id.bio);
                TextView countryViewer = dialogView.findViewById(R.id.country);
                TextView localTime = dialogView.findViewById(R.id.localTime);
                String currentTime = new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(new Date());
                localTime.setText(currentTime);
                TextView country2Viewer = dialogView.findViewById(R.id.country2);
                TextView interestsViewer = dialogView.findViewById(R.id.interests);
                TextView likesViewer = dialogView.findViewById(R.id.likes);
                TextView loveViewer = dialogView.findViewById(R.id.love_viewer);
                nameViewer.setText(name + "/" + age);
                Picasso.get().load(profileImage).into(profileImageViewer);
                briefViewer.setText(brief);
                countryViewer.setText(country);
                country2Viewer.setText(country);
                interestsViewer.setText(String.valueOf(interests));
                if (String.valueOf(interests).isEmpty()) {
                    inter_layout.setVisibility(View.GONE);
                }
                loadCountries(new OnLoadListener() {
                    @Override
                    public void OnLoad(ArrayList<Country> countriesList) {
                        String userCountryCode;
                        try {
                            userCountryCode = String.valueOf(user.child("CountryCode").getValue());
                        } catch (Exception e) {
                            userCountryCode = "";
                        }
                        Picasso.get().load(getCountryFlagWithCountryCode(countriesList, userCountryCode)).into(countryFlag2Viewer);
                        Picasso.get().load(getCountryFlagWithCountryCode(countriesList, userCountryCode)).into(countryFlagViewer);
                    }
                });
                final ArrayList<String> images = new ArrayList<>();
                for (DataSnapshot image : user.child("images").getChildren()) {
                    images.add(String.valueOf(image.getValue()));
                }
                RecyclerView imagesViewer = dialogView.findViewById(R.id.imagesViewer);
                imagesViewer.setLayoutManager(new LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false));
                imagesViewer.setHasFixedSize(true);
                imagesViewer.setAdapter(new RecyclerView.Adapter() {
                    @NonNull
                    @Override
                    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        return new RecyclerView.ViewHolder(inflater.inflate(R.layout.item_image_no_rmove, parent, false)) {
                            @Override
                            public String toString() {
                                return super.toString();
                            }
                        };
                    }
                    @Override
                    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {
                        ImageView imageView = holder.itemView.findViewById(R.id.image);
                        Picasso.get().load(images.get(position)).placeholder(R.drawable.loading).into(imageView);
                        imageView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                dialog_show_image(images.get(position));
                            }
                        });
                    }
                    @Override
                    public int getItemCount() {
                        return images.size();
                    }
                });
                if (images.isEmpty()) {
                    imagesViewer.setVisibility(View.GONE);
                }
                likesViewer.setText(likes);
                loveViewer.setText(love);
                vipImage.setImageResource(VIP ? R.drawable.vip_azar : R.drawable.disabled_vip_azar);
                gemsPlusImage.setImageResource(gems_plus ? R.drawable.gems_plus_azar : R.drawable.disabled_gems_plus_azar);
                dialog.setView(dialogView);
                dialog.show();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }
    public String getAge(DataSnapshot user) {
        return String.valueOf(getAge(Long.parseLong(String.valueOf(user.child("birthdayDate").getValue()))));
    }
    public Integer getAge(long time) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date(time));
        int year, month, day;
        year = cal.get(Calendar.YEAR);
        month = cal.get(Calendar.MONTH);
        day = cal.get(Calendar.DAY_OF_MONTH);
        return getAge(year, month, day);
    }
    public Integer getAge(int year, int month, int day) {
        Calendar dob = Calendar.getInstance();
        Calendar today = Calendar.getInstance();
        dob.set(year, month, day);
        int age = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR);
        if (today.get(Calendar.DAY_OF_YEAR) < dob.get(Calendar.DAY_OF_YEAR)) {
            age--;
        }
        return age;
    }
    public void dialog_VIP() {
        if (Boolean.parseBoolean(getUserData("VIP"))) {
            Toasty.info(context, R.string.id_279, Toast.LENGTH_SHORT, true).show();
            return;
        }
        final BaseActivity baseActivity = ((BaseActivity) context);
        baseActivity.showProgress();
        ;
        List<String> skuList = new ArrayList<>();
        skuList.add("vip_membership");
        baseActivity.billingClient.querySkuDetailsAsync(SkuDetailsParams.newBuilder().setSkusList(skuList).setType(BillingClient.SkuType.SUBS).build(),
                new SkuDetailsResponseListener() {
                    @Override
                    public void onSkuDetailsResponse(BillingResult billingResult,
                                                     List<SkuDetails> skuDetailsList) {
                        baseActivity.dismissProgress();
                        if (skuDetailsList == null) {
                            Toasty.info(context, R.string.id_255, Toast.LENGTH_SHORT, true).show();
                            return;
                        }
                        dialog_VIP(baseActivity, skuDetailsList.get(0));
                    }
                });
    }
    @SuppressLint("SetTextI18n")
    public void dialog_VIP(final BaseActivity baseActivity, final SkuDetails skuDetails) {
        if (Boolean.parseBoolean(getUserData("VIP"))) {
            Toasty.info(context, R.string.id_279, Toast.LENGTH_SHORT, true).show();
            return;
        }
        LayoutInflater factory = LayoutInflater.from(context);
        final View dialogView = factory.inflate(R.layout.vip_membership, null);
        final AlertDialog dialog = new AlertDialog.Builder(context).create();
        FrameLayout pay = dialogView.findViewById(R.id.pay);
        TextView vip_price_before = dialogView.findViewById(R.id.vip_price_before);
        pay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                BillingFlowParams billingFlowParams = BillingFlowParams.newBuilder()
                        .setSkuDetails(skuDetails)
                        .build();
                baseActivity.billingClient.launchBillingFlow(baseActivity, billingFlowParams);
            }
        });
        ImageView close = dialogView.findViewById(R.id.close);
        //price / month
        TextView priceText = dialogView.findViewById(R.id.price);
        String longvalue = " 1 " + context.getResources().getString(R.string.month);
        priceText.setText(skuDetails.getPrice() + " / " + longvalue);
        double priceAfter = skuDetails.getPriceAmountMicros() / 1000000f;
        float before = (float) ((100 * priceAfter) / 18);
        vip_price_before.setText(before + " " + skuDetails.getPriceCurrencyCode());
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.setView(dialogView);
        dialog.show();
    }
    public void dialog_invite_friends(String code) {
        LayoutInflater factory = LayoutInflater.from(context);
        final View dialogView = factory.inflate(R.layout.dialog_invite_friends, null);
        final AlertDialog dialog = new AlertDialog.Builder(context).create();
        TextView codeViewer = dialogView.findViewById(R.id.codeViewer);
        codeViewer.setText(code);
        ImageView close = dialogView.findViewById(R.id.close);
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //todo nextStep
                dialog.dismiss();
            }
        });
        dialog.setView(dialogView);
        dialog.show();
    }
    public void dialog_gems_plus(String price) {
        if (Boolean.parseBoolean(getUserData("gems_plus"))) {
            Toasty.info(context, R.string.id_279, Toast.LENGTH_SHORT, true).show();
            return;
        }
        LayoutInflater factory = LayoutInflater.from(context);
        final View dialogView = factory.inflate(R.layout.dialog_gems_plus, null);
        final AlertDialog dialog = new AlertDialog.Builder(context).create();
        FrameLayout pay = dialogView.findViewById(R.id.pay);
        pay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                if (context instanceof gems_store) {
                    gems_store gs = (gems_store) context;
                    gs.buySku("gems_plus");
                } else {
                    context.startActivity(new Intent(context, gems_store.class));
                }
            }
        });
        TextView priceText = dialogView.findViewById(R.id.price);
        priceText.setText(price + " / " + context.getString(R.string.id_295));
        TextView description = dialogView.findViewById(R.id.description);
        description.setText(context.getString(R.string.gems_plus).replace("PRICE_HERE", price));
        ImageView close = dialogView.findViewById(R.id.close);
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.setView(dialogView);
        dialog.show();
    }
    public void dialog_so_like(final String currentMatchUid) {
        final MediaPlayer mp = MediaPlayer.create(context, R.raw.like);
        FirebaseDatabase.getInstance().getReference("users").child(currentMatchUid).child("love").addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot love) {
                iLoveHim = false;
                for (DataSnapshot uid : love.getChildren()) {
                    if (getUserId().equals(uid.getValue())) {
                        iLoveHim = true;
                    }
                }
                //load people who like me list
                final ArrayList<String> people_who_like_me_list = new ArrayList<>();
                FirebaseDatabase.getInstance().getReference("users").child(getUserId()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onDataChange(@NonNull DataSnapshot user) {
                        people_who_like_me_list.clear();
                        for (DataSnapshot friendUid : user.child("love").getChildren()) {
                            people_who_like_me_list.add(String.valueOf(friendUid.getValue()));
                        }
                        for (DataSnapshot friendUid : user.child("likes").getChildren()) {
                            people_who_like_me_list.add(String.valueOf(friendUid.getValue()));
                        }
                        //send love
                        String match_plus = getUserData("match_plus");
                        if (match_plus.equals("true")) {
                            if (iLoveHim) {
                                FirebaseDatabase.getInstance().getReference("users").child(currentMatchUid).child("love").child(getUserId()).removeValue();
                                Toasty.success(context, R.string.id_296, Toast.LENGTH_SHORT, true).show();
                            } else {
                                FirebaseDatabase.getInstance().getReference("users").child(currentMatchUid).child("love").child(getUserId()).setValue(getUserId());
                                if (people_who_like_me_list.contains(currentMatchUid)) {
                                    //add to friends
                                    makeFriends(currentMatchUid, getUserId(), "match", currentMatchUid);
                                    Toasty.success(context, R.string.id_297, Toast.LENGTH_SHORT, true).show();
                                } else {
                                    mp.start();
                                    Toasty.success(context, R.string.id_298, Toast.LENGTH_SHORT, true).show();
                                }
                            }
                        } else {
                            dialog_match_plus();
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                    }
                });
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }
    boolean iLikeHim = false;
    boolean iLoveHim = false;
    public void dialog_like(final String currentMatchUid) {
        final MediaPlayer mp = MediaPlayer.create(context, R.raw.like);
        FirebaseDatabase.getInstance().getReference("users").child(currentMatchUid).child("likes").addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot likes) {
                iLikeHim = false;
                for (DataSnapshot uid : likes.getChildren()) {
                    if (getUserId().equals(uid.getValue())) {
                        iLikeHim = true;
                    }
                }
                //54444656545455464/
                //load people who like me list
                final ArrayList<String> people_who_like_me_list = new ArrayList<>();
                FirebaseDatabase.getInstance().getReference("users").child(getUserId()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onDataChange(@NonNull DataSnapshot user) {
                        people_who_like_me_list.clear();
                        for (DataSnapshot friendUid : user.child("love").getChildren()) {
                            people_who_like_me_list.add(String.valueOf(friendUid.getValue()));
                        }
                        for (DataSnapshot friendUid : user.child("likes").getChildren()) {
                            people_who_like_me_list.add(String.valueOf(friendUid.getValue()));
                        }
                        //send love
                        if (iLikeHim) {
                            FirebaseDatabase.getInstance().getReference("users").child(currentMatchUid).child("likes").child(getUserId()).removeValue();
                            Toasty.success(context, R.string.id_299, Toast.LENGTH_SHORT, true).show();
                        } else {
                            FirebaseDatabase.getInstance().getReference("users").child(currentMatchUid).child("likes").child(getUserId()).setValue(getUserId());
                            if (people_who_like_me_list.contains(currentMatchUid)) {
                                //add to friends
                                makeFriends(currentMatchUid, getUserId(), "match", currentMatchUid);
                                Toasty.success(context, R.string.id_300, Toast.LENGTH_SHORT, true).show();
                            } else {
                                mp.start();
                                Toasty.success(context, R.string.id_301, Toast.LENGTH_SHORT, true).show();
                            }
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                    }
                });
                ///546544554
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }
    public void makeFriends(String id1, String id2, String addWay, String requester) {
        HashMap<String, Object> id1Hash = new HashMap<>();
        id1Hash.put("addWay", addWay);
        id1Hash.put("requester", requester);
        FirebaseDatabase.getInstance().getReference("users").child(id1).child("friends").child(id2).updateChildren(id1Hash);
        FirebaseDatabase.getInstance().getReference("users").child(id2).child("friends").child(id1).updateChildren(id1Hash);
    }
    public int getCountryFlagByCountryName(String Country) {
        int result = R.drawable.world;
        return result;
    }
    public interface OnLoadListener {
        public void OnLoad(ArrayList<Country> countriesList);
    }
    public void loadCountries(final OnLoadListener onLoadListener) {
        final ArrayList<Country> countriesList = new ArrayList<>();
        FirebaseDatabase.getInstance().getReference("countriesList").addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot countriesListDataSnapshot) {
                for (DataSnapshot currentC : countriesListDataSnapshot.getChildren()) {
                    String code, name, flag;
                    code = currentC.child("code").getValue().toString();
                    if (currentC.child("localized").child(getLanguage()).exists()) {
                        name = String.valueOf(currentC.child("localized").child(getLanguage()).getValue());
                    } else {
                        name = String.valueOf(currentC.child("localized").child("Default").getValue());
                    }
                    flag = currentC.child("flag").getValue().toString();
                    countriesList.add(new Country(code, name, flag));
                }
                onLoadListener.OnLoad(countriesList);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }
    public String getLanguage() {
        String str = Locale.getDefault().getLanguage();
        return str.toUpperCase();
    }
    public String getCountryCodeLib() {
        String result;
        try {
            TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            result = tm.getNetworkCountryIso();
        } catch (Exception e) {
            result = Locale.getDefault().getCountry();
        }
        if (result == null || result.equals("")) {
            result = Locale.getDefault().getCountry();
        }
        return result.toUpperCase();
    }
    public String getCountryFlagWithCountryCode(ArrayList<Country> countriesList, String CountryCode) {
        if (CountryCode != null) {
            for (Country country : countriesList) {
                if (country.CountryCode.equals(CountryCode)) {
                    return country.CountryFlag;
                }
            }
        }
        return "https://i.pinimg.com/originals/9c/94/60/9c94604be48438303581d788f06267e1.jpg";
    }
    public String getCountryNameWithCountryCode(ArrayList<Country> countriesList, String code) {
        for (Country country : countriesList) {
            if (country.CountryCode.equals(code)) {
                return country.CountryName;
            }
        }
        return "Other";
    }
    public ArrayList<AIFace> loadAIFaces() {
        ArrayList<AIFace> AIFaces = new ArrayList<>();
        //  AIFaces.add(new AIFace("remove", context.getString(R.string.id_302), R.drawable.png, null, true));
        AIFaces.add(new AIFace(context.getString(R.string.id_302), R.drawable.png, "https://firebasestorage.googleapis.com/v0/b/vediochat-7914e.appspot.com/o/ARfilters%2Fbeauty?alt=media&token=eae609a4-dc43-4c15-a255-c8a586539731", true));
        AIFaces.add(new AIFace(context.getString(R.string.id_303), R.drawable.alien_v2, "https://firebasestorage.googleapis.com/v0/b/vediochat-7914e.appspot.com/o/ARfilters%2Falien_v2?alt=media&token=d785af7b-d723-4d6f-a363-3d1f1cb03937", false));
        AIFaces.add(new AIFace(context.getString(R.string.id_304), R.drawable.baby_chewbacca_sound, "https://firebasestorage.googleapis.com/v0/b/vediochat-7914e.appspot.com/o/ARfilters%2Fbaby_chewbacca_sound?alt=media&token=5077e04d-e873-469c-9b82-46f57d1ffff0", false));
        AIFaces.add(new AIFace(context.getString(R.string.id_305), R.drawable.black_mud, "https://firebasestorage.googleapis.com/v0/b/vediochat-7914e.appspot.com/o/ARfilters%2Fblack_mud?alt=media&token=71d2f85d-6323-4166-985d-6f96db509ff8", false));
        AIFaces.add(new AIFace("Makeup", R.drawable.beauty, "https://firebasestorage.googleapis.com/v0/b/vediochat-7914e.appspot.com/o/ARfilters%2Fmakeup?alt=media&token=48494e11-0317-4024-8533-22da3be27dfd", false));
        AIFaces.add(new AIFace(context.getString(R.string.id_306), R.drawable.blizzard, "https://firebasestorage.googleapis.com/v0/b/vediochat-7914e.appspot.com/o/ARfilters%2Fblizzard?alt=media&token=09f365f3-07bf-422f-9827-b210be7e84d0", false));
        AIFaces.add(new AIFace(context.getString(R.string.id_307), R.drawable.bubble, "https://firebasestorage.googleapis.com/v0/b/vediochat-7914e.appspot.com/o/ARfilters%2Fbubble?alt=media&token=fa481455-2428-4885-85d4-9a9ee9ab8d99", false));
        AIFaces.add(new AIFace(context.getString(R.string.id_308), R.drawable.candle_face, "https://firebasestorage.googleapis.com/v0/b/vediochat-7914e.appspot.com/o/ARfilters%2Fcandle_face?alt=media&token=ea3edf0a-80f4-4569-95f3-cb52f129c102", false));
        AIFaces.add(new AIFace(context.getString(R.string.id_309), R.drawable.colored_cloud, "https://firebasestorage.googleapis.com/v0/b/vediochat-7914e.appspot.com/o/ARfilters%2Fcolored_cloud?alt=media&token=2e6e81a9-8802-4323-8345-8c299582107a", false));
        AIFaces.add(new AIFace(context.getString(R.string.id_310), R.drawable.creepy_painted_face, "https://firebasestorage.googleapis.com/v0/b/vediochat-7914e.appspot.com/o/ARfilters%2Fcreepy_painted_face?alt=media&token=df939dc6-1cbb-46d2-bcef-513f21ef0cd6", false));
        AIFaces.add(new AIFace(context.getString(R.string.id_311), R.drawable.diver, "https://firebasestorage.googleapis.com/v0/b/vediochat-7914e.appspot.com/o/ARfilters%2Fdiver?alt=media&token=7dbe37a7-cbcf-490f-8fb1-105f53ca221a", false));
        AIFaces.add(new AIFace(context.getString(R.string.id_312), R.drawable.face_bubble, "https://firebasestorage.googleapis.com/v0/b/vediochat-7914e.appspot.com/o/ARfilters%2Fface_bubble?alt=media&token=cc4d77d3-1edd-4f81-bcee-23a36fb11106", false));
        AIFaces.add(new AIFace(context.getString(R.string.id_313), R.drawable.fire, "https://firebasestorage.googleapis.com/v0/b/vediochat-7914e.appspot.com/o/ARfilters%2Ffire?alt=media&token=07dad9fc-a9e2-457b-a05d-9453c15d2752", false));
        AIFaces.add(new AIFace(context.getString(R.string.id_314), R.drawable.football, "https://firebasestorage.googleapis.com/v0/b/vediochat-7914e.appspot.com/o/ARfilters%2Ffootball?alt=media&token=a91527d9-6ec1-4e3d-85ff-136bbc8a93e5", false));
        AIFaces.add(new AIFace(context.getString(R.string.id_315), R.drawable.heart, "https://firebasestorage.googleapis.com/v0/b/vediochat-7914e.appspot.com/o/ARfilters%2Fheart?alt=media&token=6b0efd99-ff19-47b5-b381-2045d492ca22", false));
        AIFaces.add(new AIFace(context.getString(R.string.id_316), R.drawable.humanoid, "https://firebasestorage.googleapis.com/v0/b/vediochat-7914e.appspot.com/o/ARfilters%2Fhumanoid?alt=media&token=bbebf906-b90b-4d2b-bbde-456f2c7dbdf5", false));
        AIFaces.add(new AIFace(context.getString(R.string.id_317), R.drawable.hunterst, "https://firebasestorage.googleapis.com/v0/b/vediochat-7914e.appspot.com/o/ARfilters%2FhunterST?alt=media&token=65a4eac5-b7a5-425c-bde8-d633edd0ef92", false));
        AIFaces.add(new AIFace(context.getString(R.string.id_318), R.drawable.jarhead, "https://firebasestorage.googleapis.com/v0/b/vediochat-7914e.appspot.com/o/ARfilters%2Fjarhead?alt=media&token=b91f7671-44c4-45f0-91bb-5d2eadb83794", false));
        AIFaces.add(new AIFace(context.getString(R.string.id_319), R.drawable.jelly_cube, "https://firebasestorage.googleapis.com/v0/b/vediochat-7914e.appspot.com/o/ARfilters%2Fjelly_cube?alt=media&token=3b6af70c-9872-43aa-baaa-8dee09e46707", false));
        AIFaces.add(new AIFace(context.getString(R.string.id_320), R.drawable.joker, "https://firebasestorage.googleapis.com/v0/b/vediochat-7914e.appspot.com/o/ARfilters%2Fjoker?alt=media&token=0694d84b-e765-4043-8ba3-519c18014128", false));
        AIFaces.add(new AIFace(context.getString(R.string.id_321), R.drawable.mummy, "https://firebasestorage.googleapis.com/v0/b/vediochat-7914e.appspot.com/o/ARfilters%2Fmummy?alt=media&token=51ccf94d-81e9-4bf0-a3ff-2191094c3739", false));
        AIFaces.add(new AIFace(context.getString(R.string.id_322), R.drawable.plasticine, "https://firebasestorage.googleapis.com/v0/b/vediochat-7914e.appspot.com/o/ARfilters%2Fplasticine?alt=media&token=7f0cf606-7729-486a-afbd-733e14a614a9", false));
        AIFaces.add(new AIFace(context.getString(R.string.id_323), R.drawable.puberty2, "https://firebasestorage.googleapis.com/v0/b/vediochat-7914e.appspot.com/o/ARfilters%2Fpuberty2?alt=media&token=3b434088-93a3-4380-9612-17aed062f622", false));
        AIFaces.add(new AIFace(context.getString(R.string.id_324), R.drawable.pumpkin, "https://firebasestorage.googleapis.com/v0/b/vediochat-7914e.appspot.com/o/ARfilters%2Fpumpkin?alt=media&token=59fe55b5-4ccb-4d41-b248-24320ed6bbec", false));
        AIFaces.add(new AIFace(context.getString(R.string.id_325), R.drawable.queen, "https://firebasestorage.googleapis.com/v0/b/vediochat-7914e.appspot.com/o/ARfilters%2Fqueen?alt=media&token=06e5a91f-aba9-44ae-a264-ed0ee632e509", false));
        AIFaces.add(new AIFace(context.getString(R.string.id_326), R.drawable.rain, "https://firebasestorage.googleapis.com/v0/b/vediochat-7914e.appspot.com/o/ARfilters%2Frain?alt=media&token=18ec4ba1-9721-4429-95c4-e4cde830d6ac", false));
        AIFaces.add(new AIFace(context.getString(R.string.id_327), R.drawable.rainbow, "https://firebasestorage.googleapis.com/v0/b/vediochat-7914e.appspot.com/o/ARfilters%2Frainbow?alt=media&token=75be7468-0fd4-48b0-b530-0184449bc613", false));
        AIFaces.add(new AIFace(context.getString(R.string.id_328), R.drawable.roulette, "https://firebasestorage.googleapis.com/v0/b/vediochat-7914e.appspot.com/o/ARfilters%2Froulette?alt=media&token=3995cc56-3048-4dee-a949-818d57b80956", false));
        AIFaces.add(new AIFace(context.getString(R.string.id_329), R.drawable.santa, "https://firebasestorage.googleapis.com/v0/b/vediochat-7914e.appspot.com/o/ARfilters%2Fsanta?alt=media&token=91c8d867-e037-4103-8d9b-ff802282981a", false));
        AIFaces.add(new AIFace(context.getString(R.string.id_330), R.drawable.shattered_face, "https://firebasestorage.googleapis.com/v0/b/vediochat-7914e.appspot.com/o/ARfilters%2Fshattered_face?alt=media&token=74671cd6-681f-4e02-b7f1-d6c819ffca55", false));
        AIFaces.add(new AIFace(context.getString(R.string.id_331), R.drawable.sketch_face, "https://firebasestorage.googleapis.com/v0/b/vediochat-7914e.appspot.com/o/ARfilters%2Fsketch_face?alt=media&token=1455ac71-3613-44f6-8c14-7a6976329a7e", false));
        AIFaces.add(new AIFace(context.getString(R.string.id_332), R.drawable.snowman, "https://firebasestorage.googleapis.com/v0/b/vediochat-7914e.appspot.com/o/ARfilters%2Fsnowman?alt=media&token=f1a0f2dd-a23a-4b73-8f0a-7b635e19a4ed", false));
        AIFaces.add(new AIFace(context.getString(R.string.id_333), R.drawable.toon, "https://firebasestorage.googleapis.com/v0/b/vediochat-7914e.appspot.com/o/ARfilters%2Ftoon?alt=media&token=704f831f-8a1e-4eda-bdd2-41dad078eebb", false));
        AIFaces.add(new AIFace(context.getString(R.string.id_334), R.drawable.topology, "https://firebasestorage.googleapis.com/v0/b/vediochat-7914e.appspot.com/o/ARfilters%2Ftopology?alt=media&token=975162d8-1d97-4176-a940-1e5a55359c7d", false));
        AIFaces.add(new AIFace(context.getString(R.string.id_335), R.drawable.vinyl_face, "https://firebasestorage.googleapis.com/v0/b/vediochat-7914e.appspot.com/o/ARfilters%2Fvinyl_face?alt=media&token=b4394655-7639-4a3c-a2f7-70a2fb3c9136", false));
        AIFaces.add(new AIFace(context.getString(R.string.id_336), R.drawable.werewolf, "https://firebasestorage.googleapis.com/v0/b/vediochat-7914e.appspot.com/o/ARfilters%2Fwerewolf?alt=media&token=c027ff2d-f3ed-4861-9de6-000fa85218b0", false));
        AIFaces.add(new AIFace(context.getString(R.string.id_337), R.drawable.wool_beard, "https://firebasestorage.googleapis.com/v0/b/vediochat-7914e.appspot.com/o/ARfilters%2Fwool_beard?alt=media&token=5026dab3-ce8c-49f4-9300-8612c2f1e7c1", false));
        AIFaces.add(new AIFace(context.getString(R.string.id_338), R.drawable.wayfarer_habana, "https://firebasestorage.googleapis.com/v0/b/vediochat-7914e.appspot.com/o/ARfilters%2Fwayfarer_habana?alt=media&token=265d2f5a-e7f0-437f-bd3a-47bd0e8df4be", false));
        AIFaces.add(new AIFace(context.getString(R.string.id_339), R.drawable.wayfarer_reading, "https://firebasestorage.googleapis.com/v0/b/vediochat-7914e.appspot.com/o/ARfilters%2Fwayfarer_reading?alt=media&token=248dd7af-6d20-4c6a-8bee-8ddf5b631261", false));
        AIFaces.add(new AIFace(context.getString(R.string.id_340), R.drawable.wayfarer_transparent, "https://firebasestorage.googleapis.com/v0/b/vediochat-7914e.appspot.com/o/ARfilters%2Fwayfarer_transparent?alt=media&token=84e121a1-9108-48ab-85da-921385521298", false));
        AIFaces.add(new AIFace(context.getString(R.string.id_341), R.drawable.chanel_butterfly, "https://firebasestorage.googleapis.com/v0/b/vediochat-7914e.appspot.com/o/ARfilters%2Fchanel_butterfly?alt=media&token=5c5abc40-762b-4038-b523-b16239119a29", false));
        AIFaces.add(new AIFace(context.getString(R.string.id_342), R.drawable.barner_honey, "https://firebasestorage.googleapis.com/v0/b/vediochat-7914e.appspot.com/o/ARfilters%2Fbarner_honey?alt=media&token=a338c87d-6f2a-42f2-a7ba-96be73dc5cbd", false));
        AIFaces.add(new AIFace(context.getString(R.string.id_343), R.drawable.round_metal, "https://firebasestorage.googleapis.com/v0/b/vediochat-7914e.appspot.com/o/ARfilters%2Fround_metal?alt=media&token=3b9de776-6a8c-429c-9d8f-86265423fe53", false));
        AIFaces.add(new AIFace(context.getString(R.string.id_344), R.drawable.wayfarer_black, "https://firebasestorage.googleapis.com/v0/b/vediochat-7914e.appspot.com/o/ARfilters%2Fwayfarer_black?alt=media&token=bf3add63-2c92-4d3a-84b3-5d7e25ad08b1", false));
        return AIFaces;
    }
    File imageFile;
    Bitmap bitmap;
    public void takeScreenshot(Activity activity) {
        Date now = new Date();
        android.text.format.DateFormat.format("yyyy-MM-dd_hh:mm:ss", now);
        try {
            // image naming and path  to include sd card  appending name you choose for file
            String mPath = Environment.getExternalStorageDirectory().toString() + "/" + now + ".jpg";
            // create bitmap screen capture
            View v1 = activity.getWindow().getDecorView().getRootView();
            v1.setDrawingCacheEnabled(true);
            bitmap = Bitmap.createBitmap(v1.getDrawingCache());
            v1.setDrawingCacheEnabled(false);
            imageFile = new File(mPath);
            FileOutputStream outputStream = new FileOutputStream(imageFile);
            int quality = 100;
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream);
            outputStream.flush();
            outputStream.close();
        } catch (Throwable e) {
            // Several error may come out with file handling or DOM
            e.printStackTrace();
        }
    }
    public void uploadImageFromTheFile(final String uid, final String type) {
        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        String random = FirebaseDatabase.getInstance().getReference().push().getKey();
        StorageReference mountainImagesRef = storageRef.child("reports").child(random + ".jpg");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 20, baos);
        byte[] data = baos.toByteArray();
        UploadTask uploadTask = mountainImagesRef.putBytes(data);
        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                taskSnapshot.getStorage().getDownloadUrl().addOnCompleteListener(
                        new OnCompleteListener<Uri>() {
                            @Override
                            public void onComplete(@NonNull Task<Uri> task) {
                                String fileLink = task.getResult().toString();
                                HashMap<String, Object> hashMap = new HashMap<>();
                                hashMap.put("type", type);
                                hashMap.put("image", fileLink);
                                FirebaseDatabase.getInstance().getReference().child("reports").child(uid).push().updateChildren(hashMap);
                                Toasty.success(context, R.string.id_346, Toast.LENGTH_SHORT, true).show();
                            }
                        });
            }
        });
    }
    ///////////
    public void report(final String uid, Activity activity) {
        //todo take screenshot
        takeScreenshot(activity);
        LayoutInflater factory = LayoutInflater.from(context);
        final View dialogView = factory.inflate(R.layout.dialog_report, null);
        final AlertDialog dialog = new AlertDialog.Builder(context).create();
        LinearLayout improperBehavior = dialogView.findViewById(R.id.improperBehavior);
        improperBehavior.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (imageFile != null && bitmap != null) {
                    uploadImageFromTheFile(uid, "improperBehavior");
                }
                dialog.dismiss();
            }
        });
        LinearLayout improperLanguage = dialogView.findViewById(R.id.improperLanguage);
        improperLanguage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (imageFile != null && bitmap != null) {
                    uploadImageFromTheFile(uid, "improperLanguage");
                }
                dialog.dismiss();
            }
        });
        TextView close = dialogView.findViewById(R.id.close);
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.setView(dialogView);
        dialog.show();
    }
    public void dialog_show_image(String path) {
        AlertDialog.Builder bmneu = new AlertDialog.Builder(context, R.style.full_screen_dialog);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View dialogView = inflater.inflate(R.layout.dialog_bigger_image, null);
        //todo load the ad here
        PhotoView photoView = (PhotoView) dialogView.findViewById(R.id.photo_view);
        Picasso.get().load(path).placeholder(R.drawable.loading).into(photoView);
        ImageView close = dialogView.findViewById(R.id.close);
        bmneu.setView(dialogView);
        final AlertDialog ad_dialog = bmneu.create();
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ad_dialog.dismiss();
            }
        });
        ad_dialog.show();
        ScrollView.LayoutParams params = (ScrollView.LayoutParams) dialogView.getLayoutParams();
        params.height = context.getResources().getDisplayMetrics().heightPixels;
        params.width = context.getResources().getDisplayMetrics().widthPixels;
        dialogView.setLayoutParams(params);




        /*
        LayoutInflater factory = LayoutInflater.from(context);
        final View dialogView = factory.inflate(R.layout.dialog_bigger_image, null);

        final AlertDialog dialog = new AlertDialog.Builder(context).create();
        PhotoView photoView = (PhotoView) dialogView.findViewById(R.id.photo_view);
     Picasso.get().load(path).placeholder(R.drawable.loading).into(photoView);
        ImageView close = dialogView.findViewById(R.id.close);
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.setView(dialogView);
        dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT);
        dialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        dialog.show();

         */
    }
}