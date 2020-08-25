package com.kyle.random.main;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.kyle.random.R;
import com.kyle.random.deeparAiVedioCall.AIAdapter;
import com.kyle.random.deeparAiVedioCall.AIFace;
import com.kyle.random.deeparAiVedioCall.CameraGrabber;
import com.kyle.random.deeparAiVedioCall.CameraGrabberListener;
import com.kyle.random.deeparAiVedioCall.CameraPreview;
import com.kyle.random.deeparAiVedioCall.Filter;
import com.kyle.random.deeparAiVedioCall.FiltersAdapter;
import com.kyle.random.deeparAiVedioCall.OnAIFaceChangeListener;
import com.kyle.random.deeparAiVedioCall.OnFilterChangeListener;
import com.kyle.random.friends.meModel;
import com.kyle.random.gems.gems_store;
import com.kyle.random.history.DataAndUid;
import com.kyle.random.other.BaseFragment;
import com.kyle.random.other.Country;
import com.kyle.random.other.Libs;
import com.squareup.picasso.Picasso;
import com.zerobranch.layout.SwipeLayout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import ai.deepar.ar.AREventListener;
import ai.deepar.ar.DeepAR;
import de.hdodenhof.circleimageview.CircleImageView;
import es.dmoral.toasty.Toasty;
import mehdi.sakout.fancybuttons.FancyButton;

import static com.kyle.random.KEYS.DEEP_AR;
import static com.kyle.random.other.Libs.dpToPx;
import static com.kyle.random.other.Libs.getUserId;
public class LayoutOne extends BaseFragment implements AREventListener {
    ArrayList<String> myInterests;
    String myUid;
    String name, brief, country, likes, love, username, age, profileImage, city;
    String favoriteGender, favoriteCountry;
    StringBuilder interests;
    ImageView countryFlagViewer;
    CircleImageView profileImageViewer;
    TextView nameViewer, countryViewer, likesViewer;
    boolean match_plus, gems_plus, VIP;
    LinearLayout gender;
    TextView gemsViewer;
    int gems;
    View filters;
    LinearLayout geographicalRegions;
    String myGender = "";
    ArrayList<Country> countriesList;
    private static final String TAG = "mLocalContainer";
    ViewGroup view;
    private DeepAR deepAR;
    View open_vip_dialog;
    com.kyle.random.deeparAiVedioCall.AIAdapter AIAdapter;
    private SurfaceView arView;
    private CameraGrabber cameraGrabber;
    FiltersAdapter filtersAdapter;
    ArrayList<Filter> filterArrayList;
    RecyclerView filtersRecyclerView;
    RecyclerView AIRecyclerView;
    ArrayList<AIFace> AIArrayList;
    ImageView VIPView;
    String myCurrentAIFace;
    fragmentViewer fragmentViewer;
    ImageView small_filterViewer;
    String myCurrentFilter = "https://firebasestorage.googleapis.com/v0/b/vediochat-7914e.appspot.com/o/png.png?alt=media&token=2b2f5299-4c9f-4237-b17c-21b553d79236";
    public void stop() {
        if (cameraGrabber != null) {
            cameraGrabber.setFrameReceiver(null);
            cameraGrabber.stopPreview();
            cameraGrabber.releaseCamera();
            cameraGrabber = null;
        }
    }
    public void distroy() {
        if (deepAR != null) {
            deepAR.release();
        }
    }
    boolean frontCam = true;
    public void start() {
        cameraGrabber = new CameraGrabber(Camera.CameraInfo.CAMERA_FACING_FRONT);
        cameraGrabber.initCamera(new CameraGrabberListener() {
            @Override
            public void onCameraInitialized() {
                cameraGrabber.setFrameReceiver(deepAR);
                cameraGrabber.startPreview();
            }
            @Override
            public void onCameraError(String errorMsg) {
                Log.e("Error", errorMsg);
            }
        });
    }
    public void updateFavUI() {
        try {
            switch (favoriteCountry) {
                case "noPreferences":
                    country_text.setText(R.string.id_144);
                    country_img.setImageDrawable(getResources().getDrawable(R.drawable.location));
                    break;
                case "global":
                    country_text.setText(R.string.id_249);
                    country_img.setImageDrawable(getResources().getDrawable(R.drawable.world));
                    break;
                default:
                    country_text.setText(libs.getCountryNameWithCountryCode(countriesList, favoriteCountry));
                    Picasso.get().load(libs.getCountryFlagWithCountryCode(countriesList, favoriteCountry)).into(country_img);
                    break;
            }
            switch (favoriteGender) {
                case "female":
                    gender_text.setText(R.string.id_67);
                    gender_img.setImageDrawable(getResources().getDrawable(R.drawable.female_azar));
                    break;
                case "male":
                    gender_text.setText(R.string.id_68);
                    gender_img.setImageDrawable(getResources().getDrawable(R.drawable.male_azar));
                    break;
                case "noPreferences":
                    gender_text.setText(R.string.id_252);
                    gender_img.setImageDrawable(getResources().getDrawable(R.drawable.gender_img));
                    break;
            }
        } catch (Exception e) {
        }
    }
    TextView country_text;
    ImageView country_img;
    TextView gender_text;
    ImageView gender_img;
    Libs libs;
    View helpView, openHistory,EffectsView;
    ImageView lastMatchImage;
    ImageView showEffectsAndFiltersView;
    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = (ViewGroup) inflater.inflate(R.layout.layout_one, null);
        fragmentViewer = (fragmentViewer) getActivity();
        libs = new Libs(fragmentViewer);
        fragmentViewer.bottom_bar.setVisibility(View.VISIBLE);
        lastMatchImage = view.findViewById(R.id.lastMatchImage);
        countriesList = new ArrayList<>();
        openHistory = view.findViewById(R.id.openHistory);
        openHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fragmentViewer.changeFrag(fragmentViewer.history);
            }
        });
        view.findViewById(R.id.openEditProfile).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (interests == null) {
                    Toasty.info(fragmentViewer, R.string.id_247, Toast.LENGTH_SHORT, true).show();
                    return;
                }
                meModel meModel = new meModel(name, age, profileImage, brief, country, interests.toString(), likes, love, images);
                libs.dialog_edit_account(getActivity(), meModel);
            }
        });
        view.findViewById(R.id.switchCamera).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (VIP) {
                    frontCam = !frontCam;
                    cameraGrabber.changeCameraDevice(frontCam ? Camera.CameraInfo.CAMERA_FACING_FRONT : Camera.CameraInfo.CAMERA_FACING_BACK);
                } else {
                    Toasty.info(LayoutOne.this.getContext(), R.string.id_253, Toast.LENGTH_SHORT, true).show();
                }
            }
        });
        LinearLayout not_vip = view.findViewById(R.id.not_vip);
        FrameLayout vip = view.findViewById(R.id.vip);
        FancyButton open_gems_store = view.findViewById(R.id.open_gems_store);
        open_vip_dialog = view.findViewById(R.id.open_vip_dialog);
        open_vip_dialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                libs.dialog_VIP();
            }
        });
        View cancel = view.findViewById(R.id.cancel);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view2) {
                YoYo.with(Techniques.FadeOut)
                        .duration(1000)
                        .playOn(view.findViewById(R.id.offer));
            }
        });
        open_gems_store.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

               startActivity(new Intent(LayoutOne.this.getActivity(), gems_store.class));

            }
        });
        boolean theUserIsVIP = Boolean.parseBoolean(libs.getUserData("VIP"));
        if (theUserIsVIP) {
            not_vip.setVisibility(View.GONE);
            vip.setVisibility(View.VISIBLE);
        } else {
            not_vip.setVisibility(View.VISIBLE);
            vip.setVisibility(View.GONE);
        }
        images = new ArrayList<>();
        arView = view.findViewById(R.id.surface);
        myUid = getUserId();
        FirebaseDatabase.getInstance().getReference("users").child(myUid).child("currentFilter").setValue(myCurrentFilter);
        deepAR = new DeepAR(getActivity());
        deepAR.setLicenseKey(DEEP_AR);
        deepAR.initialize(getActivity(), this);
        start();
        arView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder surfaceHolder) {
            }
            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                deepAR.setRenderSurface(holder.getSurface(), width, height);
            }
            @Override
            public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
                if (deepAR != null) {
                    deepAR.setRenderSurface(null, 0, 0);
                }
            }
        });
        filtersRecyclerView = view.findViewById(R.id.filtersRecyclerView);
        filters = view.findViewById(R.id.filters);
        small_filterViewer = view.findViewById(R.id.small_filterViewer);
        AIRecyclerView = view.findViewById(R.id.AIRecyclerView);
        VIPView = view.findViewById(R.id.VIPView);
        view.findViewById(R.id.openGemsStore).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                   startActivity(new Intent(LayoutOne.this.getActivity(), gems_store.class));
            }
        });
        helpView = view.findViewById(R.id.helpView);
        final View viewActiveAiFaces, viewActiveEffect;
        viewActiveAiFaces = view.findViewById(R.id.viewActiveAiFaces);
        viewActiveEffect = view.findViewById(R.id.viewActiveEffect);
         EffectsView = view.findViewById(R.id.LinearLayout);
        final SwipeLayout swipeLayout = view.findViewById(R.id.swipe_layout);
        LinearLayout AIButton = view.findViewById(R.id.loadAiFaces);
        AIButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AIRecyclerView.setVisibility(View.VISIBLE);
                filtersRecyclerView.setVisibility(View.GONE);
                viewActiveAiFaces.setBackgroundColor(Color.parseColor("#ffffff"));
                viewActiveEffect.setBackgroundColor(Color.parseColor("#40FFFFFF"));
            }
        });
         showEffectsAndFiltersView = view.findViewById(R.id.showEffectsAndFiltersView);
        final View otherView = view.findViewById(R.id.otherView);
        showEffectsAndFiltersView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean AIVisible = EffectsView.getVisibility() == View.VISIBLE;
                EffectsView.setVisibility(AIVisible ? View.GONE : View.VISIBLE);
                otherView.setVisibility(AIVisible ? View.VISIBLE : View.GONE);
                fragmentViewer.bottom_bar.setVisibility(AIVisible ? View.VISIBLE : View.GONE);
                helpView.setVisibility(AIVisible ? View.VISIBLE : View.GONE);
                showEffectsAndFiltersView.setVisibility(View.GONE);
                filters.setVisibility(View.GONE);
                AIRecyclerView.setVisibility(View.VISIBLE);
                filtersRecyclerView.setVisibility(View.GONE);
                viewActiveAiFaces.setBackgroundColor(Color.parseColor("#ffffff"));
                viewActiveEffect.setBackgroundColor(Color.parseColor("#40FFFFFF"));
                swipeLayout.setEnabledSwipe(AIVisible);
            }
        });
        final FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) fragmentViewer.bottom_bar.getLayoutParams();
        final int originalMargin = params.bottomMargin;
        swipeLayout.setOnDragPercentListener(new SwipeLayout.onDragPercentListener() {
            @Override
            public void onDragPercent(float value, float min, float max) {
                // 5sp / - swipeLayout.getHight
                float map = map(value, min, max, originalMargin, (float) fragmentViewer.bottom_bar.getHeight());
                params.bottomMargin = Math.round(-map);
                fragmentViewer.bottom_bar.setLayoutParams(params);
            }
        });
        swipeLayout.setOnActionsListener(new SwipeLayout.SwipeActionsListener() {
            @Override
            public void onOpen(int direction, boolean isContinuous) {
                if (direction == swipeLayout.LEFT) {
                    params.bottomMargin = 20;
                    fragmentViewer.bottom_bar.setLayoutParams(params);
                    stop();
                    distroy();
                    deepAR = null;
                    Bundle bundle = new Bundle();
                    bundle.putString("myCurrentAIFace", myCurrentAIFace);
                    bundle.putString("myCurrentFilter", myCurrentFilter);
                    bundle.putString("myFavGender", favoriteGender);
                    bundle.putString("myCountry", myCountryCode);
                    bundle.putString("myGender", myGender);
                    bundle.putString("myFavCountry", favoriteCountry);
                    bundle.putBoolean("frontCam", frontCam);
                    fragmentViewer.layoutTwo.setArguments(bundle);
                    fragmentViewer.changeFrag(fragmentViewer.layoutTwo);
                }
            }
            @Override
            public void onClose() {
                // the main view has returned to the default state
            }
        });
        swipeLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (fragmentViewer.oldFragment == fragmentViewer.layoutOne) {
                    EffectsView.setVisibility(View.GONE);
                    showEffectsAndFiltersView.setVisibility(View.VISIBLE);
                    filters.setVisibility(View.VISIBLE);
                    otherView.setVisibility(View.VISIBLE);
                    swipeLayout.setEnabledSwipe(true);
                    getActivity().findViewById(R.id.bottom_bar).setVisibility(View.VISIBLE);
                }
                return false;
            }
        });
        LinearLayout showFilters = view.findViewById(R.id.loadEffects);
        showFilters.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filtersRecyclerView.setVisibility(View.VISIBLE);
                AIRecyclerView.setVisibility(View.GONE);
                viewActiveEffect.setBackgroundColor(Color.parseColor("#ffffff"));
                viewActiveAiFaces.setBackgroundColor(Color.parseColor("#40FFFFFF"));
            }
        });
        setupFilters();
        setupAIFaces();
        //
        country_text = view.findViewById(R.id.country_text);
        country_img = view.findViewById(R.id.country_img);
        gender_text = view.findViewById(R.id.gender_text);
        gender_img = view.findViewById(R.id.gender_img);
        libs.loadCountries(new Libs.OnLoadListener() {
            @Override
            public void OnLoad(ArrayList<Country> countriesListLoaded) {
                countriesList = countriesListLoaded;
                loadMyData();
            }
        });
        profileImageViewer = view.findViewById(R.id.profileImage);
        countryFlagViewer = view.findViewById(R.id.countryFlag);
        nameViewer = view.findViewById(R.id.name);
        countryViewer = view.findViewById(R.id.country);
        gemsViewer = view.findViewById(R.id.gemsViewer);
        likesViewer = view.findViewById(R.id.likes);
        //other
        myInterests = new ArrayList<>();
        //Listeners
        gender = view.findViewById(R.id.gender);
        gender.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LayoutInflater factory = LayoutInflater.from(getActivity());
                final View dialogView = factory.inflate(R.layout.dialog_type_preferences, null);
                final AlertDialog dialog = new AlertDialog.Builder(getActivity()).create();
                //setMyDataIntoTheDialog
                //findById
                final RadioGroup rGroup = dialogView.findViewById(R.id.radioGroup);
                RadioButton radioButtonMale = dialogView.findViewById(R.id.radioButtonMale);
                RadioButton radioButtonFemale = dialogView.findViewById(R.id.radioButtonFemale);
                RadioButton radioButtonNoPre = dialogView.findViewById(R.id.radioButtonNoPre);
                switch (favoriteGender) {
                    case "noPreferences":
                        rGroup.check(radioButtonNoPre.getId());
                        break;
                    case "female":
                        rGroup.check(radioButtonFemale.getId());
                        break;
                    case "male":
                        rGroup.check(radioButtonMale.getId());
                        break;
                }
                TextView ok = dialogView.findViewById(R.id.ok);
                ok.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        RadioButton checkedRadioButton = (RadioButton) rGroup.findViewById(rGroup.getCheckedRadioButtonId());
                        if (checkedRadioButton.getText().toString().equals(getString(R.string.id_118))) {
                            changeGenderPreferences("noPreferences");
                        }
                        if (checkedRadioButton.getText().toString().equals(getString(R.string.id_67))) {
                            if (favoriteGender.equals("female") || match_plus || VIP) {
                                changeGenderPreferences("female");
                            } else {
                                if (gems < 13) {
                                    showBuyGemsDialog(getString(R.string.id_254));
                                } else {
                                    changeGenderPreferences("female");
                                }
                            }
                        }
                        if (checkedRadioButton.getText().toString().equals(getString(R.string.id_68))) {
                            if (favoriteGender.equals("male") || match_plus || VIP) {
                                changeGenderPreferences("male");
                            } else {
                                if (gems < 13) {
                                    showBuyGemsDialog(getString(R.string.id_254));
                                } else {
                                    changeGenderPreferences("male");
                                }
                            }
                        }
                        dialog.dismiss();
                    }
                });
                dialog.setView(dialogView);
                dialog.show();
            }
        });
        geographicalRegions = view.findViewById(R.id.geographicalRegions);
        geographicalRegions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (countriesList == null || countriesList.isEmpty()) {
                    Toasty.info(fragmentViewer, R.string.id_255, Toast.LENGTH_SHORT, true).show();
                    return;
                }
                final View dialogView = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_geographic_regions_preferences, null);
                final AlertDialog dialog = new AlertDialog.Builder(getActivity()).create();
                final RadioGroup rGroup = dialogView.findViewById(R.id.radioGroup);
                RadioButton myCountry = dialogView.findViewById(R.id.myCountry);
                myCountry.setText(country);
                myCountry.setTag(myCountryCode);
                if (favoriteCountry.equals(myCountryCode)) {
                    myCountry.setChecked(true);
                }
                RadioButton global = dialogView.findViewById(R.id.global);
                if (favoriteCountry.equals("global")) {
                    global.setChecked(true);
                }
                RadioButton noPreferences = dialogView.findViewById(R.id.noPreferences);
                if (favoriteCountry.equals("noPreferences")) {
                    noPreferences.setChecked(true);
                }
                int current = -1;
                for (int i = 0; i < countriesList.size(); i++) {
                    Country country = countriesList.get(i);
                    if (!myCountryCode.equals(country.CountryCode)) {
                        RadioButton radioButton = new RadioButton(LayoutOne.this.getContext());
                        RadioGroup.LayoutParams params = new RadioGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                        params.topMargin = dpToPx(4);
                        radioButton.setLayoutParams(params);
                        radioButton.setText(country.CountryName);
                        radioButton.setTag(country.CountryCode);
                        radioButton.setId(View.generateViewId());
                        if (favoriteCountry.equals(country.CountryCode)) {
                            current = radioButton.getId();
                        }
                        rGroup.addView(radioButton);
                    }
                }
                if (current != -1) {
                    rGroup.check(current);
                }
                TextView ok = dialogView.findViewById(R.id.ok);
                ok.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            RadioButton checkedRadioButton = (RadioButton) rGroup.findViewById(rGroup.getCheckedRadioButtonId());
                            changeCountryPreferences(checkedRadioButton.getTag().toString());
                        } catch (Exception e) {
                        }
                        dialog.dismiss();
                    }
                });
                dialog.setView(dialogView);
                dialog.show();
            }
        });
        return view;
    }
    public void setupFilters() {
        LinearLayoutManager horizontalLayoutManagaer = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        horizontalLayoutManagaer.setReverseLayout(true);
        filtersRecyclerView.setLayoutManager(horizontalLayoutManagaer);
        filterArrayList = new ArrayList<>();
        filtersAdapter = new FiltersAdapter(getActivity(), filterArrayList, new OnFilterChangeListener() {
            @Override
            public void onFilterChange(String newFilter) {
                if (!newFilter.equals("noFilter")) {
                    Picasso.get().load(newFilter).into(small_filterViewer);
                }
                myCurrentFilter = newFilter;
                FirebaseDatabase.getInstance().getReference("users").child(myUid).child("currentFilter").setValue(myCurrentFilter);
                filtersAdapter.notifyDataSetChanged();
            }
        });
        filtersRecyclerView.setAdapter(filtersAdapter);
        loadFilters();
    }
    public void setupAIFaces() {
        LinearLayoutManager horizontalLayoutManagaer = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        horizontalLayoutManagaer.setReverseLayout(true);
        AIRecyclerView.setLayoutManager(horizontalLayoutManagaer);
        AIArrayList = new ArrayList<>();
        // you can load online
        AIArrayList.addAll(libs.loadAIFaces());
        AIAdapter = new AIAdapter(getActivity(), AIArrayList, new OnAIFaceChangeListener() {
            @Override
            public void onAIFaceChange(String path) {
                myCurrentAIFace = path;
                deepAR.switchEffect("mask", path);
            }
        });
        AIRecyclerView.setAdapter(AIAdapter);
        AIAdapter.notifyDataSetChanged();
    }
    float map(float x, float in_min, float in_max, float out_min, float out_max) {
        return (x - in_min) * (out_max - out_min) / (in_max - in_min) + out_min;
    }
    public void loadFilters() {
        FirebaseDatabase.getInstance().getReference("filters").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot filters) {
                for (DataSnapshot filter : filters.getChildren()) {
                    String image = String.valueOf(filter.child("image").getValue());
                    String name = String.valueOf(filter.child("name").getValue());
                    filterArrayList.add(new Filter(filter.getKey(), name, image, name.equals("لا شئ")));
                }
                filtersAdapter.notifyDataSetChanged();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }
    public void changeCountryPreferences(String countryCode) {
        if (VIP || countryCode.equals(myCountryCode) || countryCode.equals(favoriteCountry) || countryCode.equals("noPreferences")) {
            LayoutOne.this.favoriteCountry = countryCode;
            FirebaseDatabase.getInstance().getReference("users").child(myUid).child("favoriteCountry").setValue(countryCode);
            libs.saveData("favoriteCountry", countryCode);
            updateFavUI();
        } else {
            //the user must Pay 13 gems in return
            // pay dialog
            showCountryPreferencesDialog(countryCode);
        }
    }
    public void showCountryPreferencesDialog(final String countryCode) {
        LayoutInflater factory = LayoutInflater.from(getActivity());
        final View dialogView = factory.inflate(R.layout.dialog_geographic_regions_preferences_plus, null);
        final AlertDialog dialog = new AlertDialog.Builder(getActivity()).create();
        FrameLayout ok = dialogView.findViewById(R.id.ok);
        ImageView cancel = dialogView.findViewById(R.id.cancel);
        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (gems < 13) {
                    showBuyGemsDialog(getString(R.string.id_256));
                } else {
                    dialog.dismiss();
                    showPayCountryDialog(countryCode);
                }
            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        FrameLayout vip = dialogView.findViewById(R.id.vip);
        vip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                libs.dialog_VIP();
            }
        });
        dialog.setView(dialogView);
        dialog.show();
    }
    public void changeGenderPreferences(String favoriteGender) {
        if (LayoutOne.this.favoriteGender.equals(favoriteGender) || favoriteGender.equals("noPreferences") || VIP) {
            FirebaseDatabase.getInstance().getReference("users").child(myUid).child("favoriteGender").setValue(favoriteGender);
            LayoutOne.this.favoriteGender = favoriteGender;
            updateFavUI();
        } else {
            //the user must Pay 13 gems in return
            // pay dialog
            showPayDialog(favoriteGender);
        }
    }
    public void showPayCountryDialog(final String countryCode) {
        LayoutInflater factory = LayoutInflater.from(getActivity());
        final View dialogView = factory.inflate(R.layout.dialog_pay_for_country, null);
        final AlertDialog dialog = new AlertDialog.Builder(getActivity()).create();
        TextView ok = dialogView.findViewById(R.id.ok);
        TextView cancel = dialogView.findViewById(R.id.cancel);
        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int gemsNumberAfterPaying = gems - 13;
                FirebaseDatabase.getInstance().getReference("users").child(myUid).child("favoriteCountry").setValue(countryCode);
                FirebaseDatabase.getInstance().getReference("users").child(myUid).child("gems").setValue(gemsNumberAfterPaying);
                Toasty.success(getActivity(), R.string.id_257, Toast.LENGTH_SHORT, true).show();
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
    public void showPayDialog(final String theFavoriteGender) {
        LayoutInflater factory = LayoutInflater.from(getActivity());
        final View dialogView = factory.inflate(R.layout.dialog_pay, null);
        final AlertDialog dialog = new AlertDialog.Builder(getActivity()).create();
        TextView ok = dialogView.findViewById(R.id.ok);
        TextView cancel = dialogView.findViewById(R.id.cancel);
        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int gemsNumberAfterPaying = gems - 13;
                FirebaseDatabase.getInstance().getReference("users").child(myUid).child("favoriteGender").setValue(theFavoriteGender);
                FirebaseDatabase.getInstance().getReference("users").child(myUid).child("gems").setValue(gemsNumberAfterPaying);
                Toasty.success(LayoutOne.this.getContext(), R.string.id_258, Toast.LENGTH_SHORT, true).show();
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
    public void showBuyGemsDialog(String textMsg) {
        LayoutInflater factory = LayoutInflater.from(getActivity());
        final View dialogView = factory.inflate(R.layout.dialog_buy_gems, null);
        final AlertDialog dialog = new AlertDialog.Builder(getActivity()).create();
        TextView text = dialogView.findViewById(R.id.text);
        text.setText(textMsg);
        TextView ok = dialogView.findViewById(R.id.ok);
        TextView cancel = dialogView.findViewById(R.id.cancel);
        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), gems_store.class));
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
    String myCountryCode = "";
    ArrayList<String> images;
    public void loadMyData() {
        FirebaseDatabase.getInstance().getReference("users").child(myUid).addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot user) {
                try {
                    ArrayList<String> myBlockedList = new ArrayList<>();
                    for (DataSnapshot friendUid : user.child("blockedList").getChildren()) {
                        myBlockedList.add(String.valueOf(friendUid.getKey()));
                    }
                    ArrayList<DataAndUid> history_list = new ArrayList<>();
                    for (DataSnapshot friend : user.child("history").getChildren()) {
                        history_list.add(new DataAndUid(friend.getKey(), String.valueOf(friend.getValue())));
                    }
                    Collections.sort(history_list, new Comparator<DataAndUid>() {
                        @Override
                        public int compare(DataAndUid t2, DataAndUid t1) {
                            return t2 .date.compareTo(t1.date);
                        }
                    });

                    String lastUserId = "";
                    for (DataAndUid id : history_list) {
                        if (!myBlockedList.contains(id.uid)) {
                            lastUserId = id.uid;
                        }
                    }
                    if (lastUserId.isEmpty()) {
                        openHistory.setVisibility(View.GONE);
                    } else {
                        openHistory.setVisibility(View.VISIBLE);
                        FirebaseDatabase.getInstance().getReference("users").child(lastUserId).child("profileImage").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                Picasso.get().load(String.valueOf(snapshot.getValue())).placeholder(R.color.loading_blue).into(lastMatchImage);
                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                            }
                        });
                    }
                    interests = new StringBuilder();
                    for (DataSnapshot interest : user.child("hobbies").getChildren()) {
                        interests.append(" #");
                        interests.append(Libs.getHobby(fragmentViewer, String.valueOf(interest.getValue())));
                    }
                    images.clear();
                    for (DataSnapshot image : user.child("images").getChildren()) {
                        images.add(String.valueOf(image.getValue()));
                    }
                    try {
                        myCountryCode = String.valueOf(user.child("CountryCode").getValue());
                        libs.saveData("userCountryCode", myCountryCode);
                    } catch (Exception e) {
                        myCountryCode = "";
                    }
                    String myInviteCode = String.valueOf(user.child("myInviteCode").getValue());
                    libs.saveData("myInviteCode", myInviteCode);
                    try {
                        myGender = String.valueOf(user.child("gender").getValue());
                        libs.saveData("gender", myGender);
                    } catch (Exception e) {
                        myGender = "";
                    }
                    favoriteCountry = String.valueOf(user.child("favoriteCountry").getValue());
                    age = libs.getAge(user);
                    brief = String.valueOf(user.child("brief").getValue());
                    city = String.valueOf(user.child("city").getValue());
                    country = String.valueOf(user.child("country").getValue());
                    username = String.valueOf(user.child("userName").getValue());
                    gems = Integer.parseInt(String.valueOf(user.child("gems").getValue()));
                    match_plus = (boolean) user.child("match_plus").getValue();
                    gems_plus = String.valueOf(user.child("gems_plus").getValue()).equals("0");
                    VIP = (boolean) user.child("VIP").getValue();
                    open_vip_dialog.setVisibility(VIP ? View.GONE : View.VISIBLE);
                    name = String.valueOf(user.child("name").getValue());
                    favoriteGender = String.valueOf(user.child("favoriteGender").getValue());
                    VIPView.setVisibility(VIP ? View.VISIBLE : View.GONE);
                    profileImage = String.valueOf(user.child("profileImage").getValue());
                    likes = String.valueOf(user.child("likes").getChildrenCount());
                    love = String.valueOf(user.child("love").getChildrenCount());
                    gemsViewer.setText(String.valueOf(gems));
                    nameViewer.setText(name + "/" + age);
                    Picasso.get().load(profileImage).placeholder(R.drawable.loading).into(profileImageViewer);
                    countryViewer.setText(country + " , " + city);
                    likesViewer.setText(likes);
                    Picasso.get().load(libs.getCountryFlagWithCountryCode(countriesList, myCountryCode)).into(countryFlagViewer);
                    updateFavUI();
                    progressDialog.dismiss();
                } catch (Exception e) {
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                progressDialog.dismiss();
            }
        });
    }
    RelativeLayout mLayout;
    CameraPreview mPreview;
    public void setupCamera() {
        new Handler(getActivity().getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                mPreview = new CameraPreview(getActivity(), 1, CameraPreview.LayoutMode.FitToParent);
                RelativeLayout.LayoutParams previewLayoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
                mLayout = view.findViewById(R.id.frame);
                mLayout.addView(mPreview, 0, previewLayoutParams);
            }
        }, 200);
    }
    @Override
    public void screenshotTaken(Bitmap bitmap) {
    }
    @Override
    public void videoRecordingStarted() {
    }
    @Override
    public void videoRecordingFinished() {
    }
    @Override
    public void videoRecordingFailed() {
    }
    @Override
    public void videoRecordingPrepared() {
    }
    @Override
    public void shutdownFinished() {
    }
    @Override
    public void initialized() {
    }
    @Override
    public void faceVisibilityChanged(boolean b) {
    }
    @Override
    public void imageVisibilityChanged(String s, boolean b) {
    }
    @Override
    public void error(String s) {
    }
    @Override
    public void effectSwitched(String s) {
    }
}