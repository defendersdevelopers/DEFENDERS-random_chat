package com.kyle.random.getStarted;
import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.Spanned;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;

import com.kyle.random.R;
import com.kyle.random.other.BaseActivity;
import com.kyle.random.other.Libs;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.philliphsu.bottomsheetpickers.date.DatePickerDialog;

import java.io.IOException;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import co.ceryle.radiorealbutton.RadioRealButton;
import co.ceryle.radiorealbutton.RadioRealButtonGroup;
import es.dmoral.toasty.Toasty;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static com.kyle.random.other.Libs.getTimeNow;
import static org.apache.commons.lang3.StringUtils.capitalize;
public class create_account extends BaseActivity implements com.philliphsu.bottomsheetpickers.date.DatePickerDialog.OnDateSetListener {
    FirebaseUser user;
    String userName = "", gender = "", city = "", country = "", profileImageUrl = "";
    long birthdayDate = 0;
    String login_method;
    TextView choseBirthdayTextView;
    EditText edit_name;

    @SuppressLint("MissingPermission")
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_account);
        user = FirebaseAuth.getInstance().getCurrentUser();
        //birthDay listener
        RadioGroup group = findViewById(R.id.group);
        choseBirthdayTextView = findViewById(R.id.choseBirthday);
        edit_name = findViewById(R.id.edit_name);
        edit_name.setFilters(new InputFilter[] {
                new InputFilter.AllCaps() {
                    @Override
                    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                        return String.valueOf(source).toLowerCase();
                    }
                }
        });

        group.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                switch (radioGroup.getCheckedRadioButtonId()){
                    case R.id.male:
                        gender = "male";
                        break;
                    case R.id.female:
                        gender = "female";
                        break;
                }
            }
        });


        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            login_method = extras.getString("login_method");
            profileImageUrl = extras.getString("profileImageUrl", "");
        }
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        LocationListener locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                Geocoder geocoder = new Geocoder(create_account.this, Locale.getDefault());
                try {
                    ArrayList<Address> addresses = (ArrayList<Address>) geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                    if (!addresses.isEmpty()) {
                        city = addresses.get(0).getAddressLine(0);
                        country = addresses.get(0).getAddressLine(2);
                    }
                    // String stateName = addresses.getAddressLine(1);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            public void onStatusChanged(String provider, int status, Bundle extras) {
            }
            public void onProviderEnabled(String provider) {
            }
            public void onProviderDisabled(String provider) {
            }
        };
        if (ActivityCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);


    }


    public void choseBirthday(View view) {
        Calendar now = Calendar.getInstance();
        DatePickerDialog dateDialog = new DatePickerDialog.Builder(
                create_account.this,
                now.get(Calendar.YEAR),
                now.get(Calendar.MONTH),
                now.get(Calendar.DAY_OF_MONTH))
                .build();
        dateDialog.show(getSupportFragmentManager(), "TAG");
    }
    @Override
    public void onDateSet(DatePickerDialog dialog, int year, int monthOfYear, int dayOfMonth) {
        Calendar cal = new java.util.GregorianCalendar();
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, monthOfYear);
        cal.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        //
        int age = new Libs(create_account.this).getAge(year, monthOfYear, dayOfMonth);
        if (age < 18) {
            Toasty.info(create_account.this, R.string.id_226, Toast.LENGTH_SHORT, true).show();
        } else {
            birthdayDate = cal.getTimeInMillis();
            choseBirthdayTextView.setText(year + "/" + monthOfYear + "/" + dayOfMonth);
        }
    }
    public void createAccountButton(View view) {
        userName = edit_name.getText().toString().toLowerCase();
        if (gender.isEmpty() || birthdayDate == 0 || userName.isEmpty()) {
            if (userName.isEmpty()) {
                edit_name.setError(getString(R.string.id_227));
            } else if (!userName.matches("\\b[a-zA-Z][a-zA-Z0-9\\-._]{3,}\\b")) {
                edit_name.setError(getString(R.string.id_228));
            } else if (birthdayDate == 0) {
                Toasty.info(create_account.this, R.string.id_229, Toast.LENGTH_SHORT, true).show();
            } else {
                Toasty.info(create_account.this, R.string.id_230, Toast.LENGTH_SHORT, true).show();
            }
        } else {
            CheckIfUsernameIsUnice();
        }
    }
    public void CheckIfUsernameIsUnice() {
        progressDialog.show();

        final String myInviteCode =  new SimpleDateFormat("HHmmss", Locale.ENGLISH).format(new Date());
        FirebaseDatabase.getInstance().getReference("users").orderByChild("userName").equalTo(userName).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot data) {
                if (data.exists()) {
                    progressDialog.dismiss();
                    Toasty.info(create_account.this, R.string.id_231, Toast.LENGTH_SHORT, true).show();
                } else {
                    getAddress(new OnGetAddressListener() {
                        @Override
                        public void OnGetAddress(String country, String city , double lat , double lng) {
                            getSharedPreferences("data", MODE_PRIVATE).edit().putString("Uid", Libs.getUserId()).apply();
                            String image = profileImageUrl.isEmpty() ? String.valueOf(user.getPhotoUrl()) : profileImageUrl;
                            HashMap<Object, Object> HashMap = new HashMap<>();
                            HashMap.put("VIP", false);
                            HashMap.put("match_plus", false);
                            HashMap.put("suspended", false);



                            HashMap.put("android_version",Build.VERSION.SDK_INT);

                            HashMap.put("gems_plus", 0);

                            HashMap.put("birthdayDate", birthdayDate);
                            HashMap.put("city", city);
                            HashMap.put("country", country);
                            HashMap.put("lat", lat);
                            HashMap.put("lng", lng);


                            HashMap.put("CountryCode", new Libs(create_account.this).getCountryCodeLib());
                            HashMap.put("myInviteCode", myInviteCode);
                            HashMap.put("gems", 0);
                            HashMap.put("profileImage", image);
                            HashMap.put("brief", "i'm a new user !");
                            HashMap.put("login_method", login_method);
                            HashMap.put("name", user.getDisplayName());
                            HashMap.put("userName", userName);
                            HashMap.put("hobbies", "");
                            HashMap.put("gender", gender);
                            HashMap.put("last_active", getTimeNow());
                            HashMap.put("favoriteGender", "noPreferences");
                            HashMap.put("favoriteCountry", "noPreferences");
                            FirebaseDatabase.getInstance().getReference("users").child(Libs.getUserId()).setValue(HashMap);
                            startActivity(new Intent(create_account.this, enter_invite_code.class));
                            progressDialog.dismiss();
                        }
                    });
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });


    }
}
