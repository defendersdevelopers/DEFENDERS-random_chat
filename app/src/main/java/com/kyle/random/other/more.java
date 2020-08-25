package com.kyle.random.other;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.cardview.widget.CardView;

import com.kyle.random.KEYS;
import com.kyle.random.R;


import com.kyle.random.friends.blockedList;
import com.kyle.random.friends.friendRequests;
import com.kyle.random.gems.gems_store;
import com.kyle.random.matchPlus.people_who_like_me_list;
import com.google.firebase.database.FirebaseDatabase;
import com.sucho.placepicker.AddressData;
import com.sucho.placepicker.Constants;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import es.dmoral.toasty.Toasty;

import static com.kyle.random.other.Libs.getUserId;
public class more extends BaseActivity {
    TextView gemsViewer;
    String myUid;
    int gems;
    Libs libs;
    CardView matchPlusView, VIPView;
    String match_plus;
    Switch hideAgeSwitch, hideLocationSwitch, VIPFrameSwitch, VIPLogoSwitch;
    String user_lat = null, user_lng = null;


    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);
        gemsViewer = findViewById(R.id.gemsViewer);
        libs = new Libs(this);
        myUid = getUserId();

        matchPlusView = findViewById(R.id.matchPlusView);
        match_plus = libs.getUserData("match_plus");
        matchPlusView.setVisibility(match_plus.equals("true") ? View.VISIBLE : View.GONE);
        VIPView = findViewById(R.id.VIPView);
        String VIP = libs.getUserData("VIP");
        VIPView.setVisibility(VIP.equals("true") ? View.VISIBLE : View.GONE);
        hideAgeSwitch = findViewById(R.id.hideAgeSwitch);
        hideAgeSwitch.setChecked(libs.getUserData("hideAgeSwitch").equals("true"));
        hideAgeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                libs.saveData("hideAgeSwitch", String.valueOf(isChecked));
                FirebaseDatabase.getInstance().getReference("users").child(myUid).child("hideAge").setValue(isChecked);
                Toasty.success(more.this, R.string.id_348, Toast.LENGTH_SHORT, true).show();
            }
        });
        hideLocationSwitch = findViewById(R.id.hideLocationSwitch);
        hideLocationSwitch.setChecked(libs.getUserData("hideLocationSwitch").equals("true"));
        hideLocationSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                libs.saveData("hideLocationSwitch", String.valueOf(isChecked));
                FirebaseDatabase.getInstance().getReference("users").child(myUid).child("hideLocation").setValue(isChecked);
                Toasty.success(more.this, R.string.id_348, Toast.LENGTH_SHORT, true).show();
            }
        });
        VIPFrameSwitch = findViewById(R.id.VIPFrameSwitch);
        VIPFrameSwitch.setChecked(libs.getUserData("VIPFrameSwitch").equals("true"));
        VIPFrameSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                libs.saveData("VIPFrameSwitch", String.valueOf(isChecked));
                FirebaseDatabase.getInstance().getReference("users").child(myUid).child("VIPFrame").setValue(isChecked);
                Toasty.success(more.this, R.string.id_348, Toast.LENGTH_SHORT, true).show();

            }
        });
        VIPLogoSwitch = findViewById(R.id.VIPLogoSwitch);
        VIPLogoSwitch.setChecked(libs.getUserData("VIPLogoSwitch").equals("true"));
        VIPLogoSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                libs.saveData("VIPLogoSwitch", String.valueOf(isChecked));
                // change app logo
                libs.setVipMode(isChecked);
                Toasty.success(more.this, R.string.id_348, Toast.LENGTH_SHORT, true).show();
            }
        });


    }

    @Override
    protected void onStart() {
        super.onStart();
        loadMyData();
    }
    public void loadMyData() {
        gems = Integer.parseInt(libs.getUserData("gems"));
        gemsViewer.setText(String.valueOf(gems));
    }
    public void gemsStore(View view) {
        startActivity(new Intent(this, gems_store.class));
    }
    public void blockedFriends(View view) {
        startActivity(new Intent(this, blockedList.class));
    }
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void deleteSaves(View view) {


        LayoutInflater factory = LayoutInflater.from(more.this);
        final View dialogView = factory.inflate(R.layout.alert_remove_history, null);
        final androidx.appcompat.app.AlertDialog dialog = new androidx.appcompat.app.AlertDialog.Builder(more.this).create();
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawableResource(android.R.color.transparent);
        TextView ok = dialogView.findViewById(R.id.ok);
        TextView cancel = dialogView.findViewById(R.id.cancel);
        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseDatabase.getInstance().getReference("users").child(getUserId()).child("history").removeValue();
                Toasty.success(more.this, R.string.id_347, Toast.LENGTH_SHORT, true).show();

                dialog.dismiss();
            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.setView(dialogView);
        dialog.show();
    }

    public void aboutUs(View view) {
        startActivity(new Intent(this, about_us.class));
    }
    public void help(View view) {
      new AlertDialog.Builder(this).setTitle(R.string.help).setMessage(getString(R.string.id_634)  +" "+ libs.getUserData("support_email")).show();
    }
    public void share(View view) {
        try {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name));
            String shareMessage = "\n"+getString(R.string.id_349)+"\n\n";
            shareMessage = shareMessage + "https://play.google.com/store/apps/details?id=" + KEYS.APPLICATION_ID + "\n\n";
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);
            startActivity(Intent.createChooser(shareIntent, "choose one"));
        } catch (Exception e) {
            //e.toString();
        }
    }
    public void logOut(View view) {
logout();
    }
    public void back(View view) {
        finish();
    }
    public void friend_requests(View view) {
        startActivity(new Intent(this, friendRequests.class));
    }

    public void people_who_like_me_list(View view) {
        startActivity(new Intent(this, people_who_like_me_list.class));
    }
    public void changeMyLocation(View view) {
        user_lat = null ;
        user_lng = null;
        setupLocationManager(new OnGetLocationListener() {
            @Override
            public void OnGetLocation(String lat, String lng) {
                if (user_lat == null || user_lng==null){
                    user_lat = String.valueOf(lat);
                    user_lng = String.valueOf(lng);
                    BaseActivity.detectLocation(more.this,user_lat,user_lng);
                }

            }
        });



    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK && data != null) {

                AddressData addressData = data.getParcelableExtra(Constants.ADDRESS_INTENT);


            List<Address> addresses = addressData==null ? null : addressData.getAddressList();
            if (addresses != null && addresses.size() > 0) {



                 HashMap<String,Object> newAddress = new HashMap<>();
                newAddress.put("fakeCountry" ,  addresses.get(0).getCountryName());
                newAddress.put("fakeCity" , addresses.get(0).getLocality() == null ? "" :addresses.get(0).getLocality());

                newAddress.put("fakeCountryCode" ,  addresses.get(0).getCountryCode().toUpperCase());




                FirebaseDatabase.getInstance().getReference("users").child(getUserId()).updateChildren(newAddress);


                Toasty.success(this, R.string.id_278, Toast.LENGTH_SHORT, true).show();




                finish();
            }else{
                Toasty.info(more.this, R.string.id_633, Toast.LENGTH_SHORT, true).show();

            }


        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }




    }
    public void add_fake_name(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.id_350);
// Set up the input
        final EditText input = new EditText(this);
// Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);
// Set up the buttons
        builder.setPositiveButton(R.string.id_351, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String m_Text = input.getText().toString();
                if (m_Text.isEmpty()) {
    Toasty.info(more.this, R.string.id_352, Toast.LENGTH_SHORT, true).show();

                } else {
                    FirebaseDatabase.getInstance().getReference("users").child(getUserId()).child("fakeName").setValue(m_Text);
                    Toasty.success(more.this, R.string.id_353, Toast.LENGTH_SHORT, true).show();
                }
            }
        });
        builder.setNegativeButton(R.string.id_72, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }
}
