package com.kyle.random.main;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.hardware.Camera;
import android.media.MediaPlayer;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.kyle.random.R;
import com.kyle.random.chat.inCallChatAdapter;
import com.kyle.random.deeparAiVedioCall.AIAdapter;
import com.kyle.random.deeparAiVedioCall.AIFace;
import com.kyle.random.deeparAiVedioCall.CameraGrabber;
import com.kyle.random.deeparAiVedioCall.CameraGrabberListener;
import com.kyle.random.deeparAiVedioCall.DeepARRenderer;
import com.kyle.random.deeparAiVedioCall.Filter;
import com.kyle.random.deeparAiVedioCall.FiltersAdapter;
import com.kyle.random.deeparAiVedioCall.OnAIFaceChangeListener;
import com.kyle.random.deeparAiVedioCall.OnFilterChangeListener;
import com.kyle.random.friends.Message;
import com.kyle.random.other.Country;
import com.kyle.random.other.Libs;
import com.squareup.picasso.Picasso;
import com.zerobranch.layout.SwipeLayout;

import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent;
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;

import ai.deepar.ar.AREventListener;
import ai.deepar.ar.DeepAR;
import de.hdodenhof.circleimageview.CircleImageView;
import es.dmoral.toasty.Toasty;
import io.agora.rtc.Constants;
import io.agora.rtc.IRtcEngineEventHandler;
import io.agora.rtc.RtcEngine;
import io.agora.rtc.video.VideoCanvas;
import io.agora.rtc.video.VideoEncoderConfiguration;
import io.netopen.hotbitmapgg.library.view.RingProgressBar;

import static com.kyle.random.KEYS.AGORA_KEY;
import static com.kyle.random.KEYS.DEEP_AR;
import static com.kyle.random.KEYS.VIDEO_QUALITY;
import static com.kyle.random.other.BaseActivity.getHideName;
import static com.kyle.random.other.Libs.getTimeNow;
import static com.kyle.random.other.Libs.getUserId;
public class LayoutTwo extends Fragment implements AREventListener {
    String friendId = "";
    String lastFriendId = "";
    private static final String TAG = "MainActivity";
    private CameraGrabber cameraGrabber;
    private DeepAR deepAR;
    private GLSurfaceView surfaceView;
    private DeepARRenderer renderer;
    private boolean callInProgress;
    ImageView big_filterViewer;
    RecyclerView filtersRecyclerView;
    boolean iamTheCreator = false;
    View otherView, showFilters, AIButton;
    ImageView matchPlusGoldenBorder;
    ImageView switchCamera;
    //  Button another;
    TextView nameViewer, countryViewer, likesViewer;
    CircleImageView profile_image_viewer;
    View viewActiveAiFaces, viewActiveEffect, searching;
    ImageView report;
    LinearLayout LinearLayout;
    ImageView addFriend, flag;
    String statue = "";
    TextView GemsViewer, GenderPreferencesViewer, CountryPreferencesViewer;
    ImageView imageGenderPreferencesViewer, imageCountryPreferencesViewer;
    private String video_room_id;
    private RtcEngine mRtcEngine;
    private FrameLayout mLocalContainer, mRemoteContainer;
    ImageView VIPView;
    RelativeLayout friendRequestView;
    private String myUserID;
    public String wanted_gender, myCountry, myGender, wanted_country;
    ArrayList<Country> countriesList;
    TextView accept, decline;
    boolean canSwipe = false;
    boolean canLeave = false;
    RingProgressBar RingProgressBar;
    SwipeLayout swipeLayout;
    ImageView leave;
    public final <T extends View> T findViewById(int id) {
        return view.findViewById(id);
    }
    fragmentViewer context;
    View view,exit_wait;
    BroadcastReceiver friendsRevicer;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        myUserID = getUserId();
        view = getLayoutInflater().inflate(R.layout.layout_two, container, false);
        exit_wait =  findViewById(R.id.exit_wait);
        exit_wait.setVisibility(View.GONE);

        context = (fragmentViewer) getActivity();
        libs = new Libs(context);
        deepAR = new DeepAR(context);
        deepAR.setLicenseKey(DEEP_AR);
        deepAR.initialize(context, this);
        mLocalContainer = findViewById(R.id.localPreview);
        callInProgress = false;
        initUiAndButtons();
        showSearchDialog();
        Bundle extras = getArguments();
        if (extras != null) {
            myCurrentAIFace = extras.getString("myCurrentAIFace", "");
            myCurrentFilter = extras.getString("myCurrentFilter");
            wanted_gender = extras.getString("myFavGender", "noPreferences");
            myCountry = extras.getString("myCountry");
            myGender = extras.getString("myGender");
            wanted_country = extras.getString("myFavCountry", "noPreferences");
            frontCam = extras.getBoolean("frontCam", true);
            VIP = Boolean.parseBoolean(libs.getUserData("VIP"));
            if (myCurrentFilter != null) {
                try {
                    Picasso.get().load(myCurrentFilter).into(iamTheBiggerOne ? big_filterViewer : small_filterViewer);
                } catch (Exception e) {
                }
            } else {
                myCurrentFilter = "";
            }
        }
        setupFilters();
        setupAIFaces();
        cameraGrabber = new CameraGrabber(frontCam ? Camera.CameraInfo.CAMERA_FACING_FRONT : Camera.CameraInfo.CAMERA_FACING_BACK);
        cameraGrabber.initCamera(new CameraGrabberListener() {
            @Override
            public void onCameraInitialized() {
                if (context.oldFragment == context.layoutTwo) {
                    cameraGrabber.setFrameReceiver(deepAR);
                    cameraGrabber.startPreview();
                }
            }
            @Override
            public void onCameraError(String errorMsg) {
                Log.e("Error", errorMsg);
            }
        });
        initializeEngine();
        setupVideoConfig();
        surfaceView = new GLSurfaceView(context);
        surfaceView.setEGLContextClientVersion(2);
        surfaceView.setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        renderer = new DeepARRenderer(deepAR, mRtcEngine);
        surfaceView.setEGLContextFactory(new DeepARRenderer.MyContextFactory(renderer));
        surfaceView.setRenderer(renderer);
        surfaceView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
        surfaceView.setZOrderMediaOverlay(true);
        mLocalContainer.addView(surfaceView);
        mRtcEngine.setupLocalVideo(new VideoCanvas(surfaceView, VideoCanvas.RENDER_MODE_HIDDEN, 0));
        surfaceView.onResume();
        if (!callInProgress) {
            libs.loadCountries(new Libs.OnLoadListener() {
                @Override
                public void OnLoad(ArrayList<Country> countriesList) {
                    LayoutTwo.this.countriesList = countriesList;
                    switch (wanted_country) {
                        case "noPreferences":
                            CountryPreferencesViewer.setText(R.string.id_259);
                            imageCountryPreferencesViewer.setImageDrawable(getResources().getDrawable(R.drawable.world));
                            break;
                        case "global":
                            CountryPreferencesViewer.setText(R.string.id_249);
                            imageCountryPreferencesViewer.setImageDrawable(getResources().getDrawable(R.drawable.world));
                            break;
                        default:
                            CountryPreferencesViewer.setText(libs.getCountryNameWithCountryCode(countriesList, wanted_country));
                            Picasso.get().load(libs.getCountryFlagWithCountryCode(countriesList, wanted_country)).into(imageCountryPreferencesViewer);
                            break;
                    }
                    switch (wanted_gender) {
                        case "female":
                            GenderPreferencesViewer.setText(R.string.id_67);
                            imageGenderPreferencesViewer.setImageDrawable(getResources().getDrawable(R.drawable.female_azar));
                            break;
                        case "male":
                            GenderPreferencesViewer.setText(R.string.id_68);
                            imageGenderPreferencesViewer.setImageDrawable(getResources().getDrawable(R.drawable.male_azar));
                            break;
                        case "noPreferences":
                            GenderPreferencesViewer.setText(R.string.id_118);
                            imageGenderPreferencesViewer.setImageDrawable(getResources().getDrawable(R.drawable.gender_azar));
                            break;
                    }
                    endCurrentCallAndStartANewOne();
                }
            });
        }
        FirebaseDatabase.getInstance().getReference("rooms").child(myUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot room) {
                if (room.hasChildren()) {
                    friendId = String.valueOf(room.child("friendId").getValue());
                    context.getSharedPreferences("data", 0).edit().putString("last_friend", friendId).apply();
                    video_room_id = String.valueOf(room.child("video_room_id").getValue());
                    long created_at = Long.parseLong(String.valueOf(room.child("created_at").getValue()));
                    joinChannel();
                    ListenToMyFriendFilterChange(friendId);
                    loadTheRandomUserData(friendId);
                    room.getRef().removeValue();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
        friendsRevicer = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                switch (intent.getAction()) {
                    case "friendRequest":
                        Toasty.custom(context, R.string.id_261, R.drawable.ic_friend_req,
                                R.color.colorPrimary, Toast.LENGTH_SHORT, true,
                                true).show();
                        friendRequestView.setVisibility(View.VISIBLE);
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                friendRequestView.setVisibility(View.INVISIBLE);
                            }
                        }, 7000);
                        break;
                    case "RequestAccepted":
                        Toasty.success(context, R.string.id_262, Toast.LENGTH_SHORT, true).show();
                        //todo KKK
                        addFriend.setImageResource(R.drawable.accepted);
                        break;
                    case "InCallLikes":
                        try {
                            likesViewer.setText(String.valueOf(Integer.parseInt(likesViewer.getText().toString()) + 1));
                        } catch (Exception e) {
                        }
                        startAnimation();
                        break;
                }
            }
        };
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("friendRequest");
        intentFilter.addAction("RequestAccepted");
        intentFilter.addAction("InCallLikes");
        context.registerReceiver(friendsRevicer, intentFilter);
        return view;
    }
    public void startAnimation() {
        View likeLayout = findViewById(R.id.likeLayout);
        likeLayout.setVisibility(View.VISIBLE);
        Toasty.custom(context, R.string.id_263, R.drawable.like_azar,
                R.color.colorPrimary, Toast.LENGTH_SHORT, true,
                true).show();
        YoYo.with(Techniques.TakingOff)
                .duration(1200)
                .playOn(findViewById(R.id.like_1));
        YoYo.with(Techniques.TakingOff)
                .duration(1700)
                .playOn(findViewById(R.id.like_2));
        YoYo.with(Techniques.TakingOff)
                .duration(3700)
                .playOn(findViewById(R.id.like_3));
        final MediaPlayer mp = MediaPlayer.create(context, R.raw.like);
        mp.start();
        YoYo.with(Techniques.TakingOff)
                .duration(3700)
                .playOn(findViewById(R.id.like_4));
        YoYo.with(Techniques.TakingOff)
                .duration(3200)
                .playOn(findViewById(R.id.plus_1));
    }
    Libs libs;
    private void initializeEngine() {
        IRtcEngineEventHandler mRtcEventHandler = new IRtcEngineEventHandler() {
            @Override
            public void onWarning(int warn) {
                Log.e(TAG, "warning: " + warn);
            }
            @Override
            public void onError(int err) {
                // todo for more info
                //  https://docs.agora.io/en/Video/API%20Reference/java/classio_1_1agora_1_1rtc_1_1_i_rtc_engine_event_handler_1_1_error_code.html
                Log.e(TAG, "error: " + err);
            }
            @Override
            public void onJoinChannelSuccess(final String channelName, final int uid, int elapsed) {
                context.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        callInProgress = true;
                        renderer.setCallInProgress(true);
                    }
                });
            }
            @Override
            public void onFirstRemoteVideoDecoded(final int uid, int width, int height, int elapsed) {
                context.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.e(TAG, "onFirstRemoteVideoDecoded");
                        setupRemoteVideo(uid);
                    }
                });
            }
            @Override
            public void onUserOffline(final int uid, int reason) {
                context.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.e(TAG, "onUserOffline");
                        showSearchDialog();
                        new Handler(context.getMainLooper()).postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                endCurrentCallAndStartANewOne();
                            }
                        }, 5000);
                    }
                });
            }
        };
        try {
            mRtcEngine = RtcEngine.create(context, AGORA_KEY, mRtcEventHandler);
        } catch (Exception e) {
            Log.e(TAG, Log.getStackTraceString(e));
            Toasty.error(context, Log.getStackTraceString(e), Toast.LENGTH_SHORT, true).show();
            throw new RuntimeException("NEED TO check rtc sdk init fatal error\n" + Log.getStackTraceString(e));
        }
        mRtcEngine.setChannelProfile(Constants.CHANNEL_PROFILE_LIVE_BROADCASTING);
    }
    private void setupVideoConfig() {
        mRtcEngine.enableVideo();
        mRtcEngine.setExternalVideoSource(true, true, true);
        mRtcEngine.setVideoEncoderConfiguration(new VideoEncoderConfiguration(
                VIDEO_QUALITY,
                VideoEncoderConfiguration.FRAME_RATE.FRAME_RATE_FPS_15,
                VideoEncoderConfiguration.STANDARD_BITRATE,
                VideoEncoderConfiguration.ORIENTATION_MODE.ORIENTATION_MODE_FIXED_PORTRAIT));
    }
    private void joinChannel() {
        mRtcEngine.setClientRole(Constants.CLIENT_ROLE_BROADCASTER);
        mRtcEngine.joinChannel(null, video_room_id, "Extra Optional Data", 0);
    }
    private void setupRemoteVideo(int uid) {
        if (context.oldFragment == context.layoutTwo) {
            if (mRemoteContainer.getChildCount() >= 1) {
                return;
            }
            SurfaceView surfaceView = RtcEngine.CreateRendererView(context);
            mRemoteContainer.addView(surfaceView);
            callInProgress = true;
            search = false;
            mRtcEngine.setupRemoteVideo(new VideoCanvas(surfaceView, VideoCanvas.RENDER_MODE_HIDDEN, uid));
            surfaceView.setTag(uid);
        }
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
        if (!myCurrentAIFace.isEmpty()) {
            try {
                deepAR.switchEffect("mask", myCurrentAIFace);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    @Override
    public void faceVisibilityChanged(boolean b) {
    }
    @Override
    public void imageVisibilityChanged(String s, boolean b) {
    }
    @Override
    public void error(String s) {
        Toasty.error(context, s, Toast.LENGTH_SHORT, true).show();
    }
    @Override
    public void effectSwitched(String s) {
    }
    boolean iamTheBiggerOne = false;
    com.kyle.random.deeparAiVedioCall.AIAdapter AIAdapter;
    String myCurrentFilter = "noFilter";
    FiltersAdapter filtersAdapter;
    ArrayList<Filter> filterArrayList;
    RecyclerView AIRecyclerView;
    ArrayList<AIFace> AIArrayList;
    String myCurrentAIFace = "";
    boolean VIP;
    ImageView small_filterViewer;
    // String MyFriendUid = "";
    boolean frontCam = true;
    String getReomId(String uid1, String uid2) {
        ArrayList<String> arrayList = new ArrayList<>();
        arrayList.add(uid1);
        arrayList.add(uid2);
        Collections.sort(arrayList, new Comparator<String>() {
            @Override
            public int compare(String s, String t1) {
                return s.compareTo(t1);
            }
        });
        return arrayList.get(0) + "~" + arrayList.get(1);
    }
    ValueEventListener valueEventListener = null;
    int Refresh_Time = 5 * 1000;
    Handler timerHandler = new Handler();
    Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            FirebaseDatabase.getInstance().getReference("search").child(getUserId()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.child("country").exists()) {
                        FirebaseDatabase.getInstance().getReference("search").child(getUserId()).child("created_at").setValue(getTimeNow());
                    } else if (dataSnapshot.hasChild("created_at")) {
                        FirebaseDatabase.getInstance().getReference("search").child(getUserId()).removeValue();
                    } else {
                        if (search) {
                            // endCurrentCallAndStartANewOne();
                        }
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                }
            });
            timerHandler.postDelayed(this, Refresh_Time);
        }
    };
    @Override
    public void onStart() {
        super.onStart();
        timerHandler.postDelayed(timerRunnable, Refresh_Time);
        if (search) {
            endCurrentCallAndStartANewOne();
        }
    }
    LinearLayout inputMessageView;
    RecyclerView mChatRecycler, mChatRecycler2;
    EditText editMessage;
    CircleImageView send;
    String message;
    ArrayList<Message> messagesList;
    AdView adView;
    @SuppressLint("MissingPermission")
    public void initUiAndButtons() {
        nameViewer = findViewById(R.id.nameViewer);
        //    another = findViewById(R.id.anotherOne);
        swipeLayout = findViewById(R.id.swipe_layout);
        profile_image_viewer = findViewById(R.id.profile_image);
        countryViewer = findViewById(R.id.countryViewer);
        searching = findViewById(R.id.searching);
        flag = findViewById(R.id.flag);
        RingProgressBar = findViewById(R.id.RingProgressBar);
        likesViewer = findViewById(R.id.likesViewer);
        AIButton = findViewById(R.id.loadAiFaces);
        friendRequestView = findViewById(R.id.friendRequestView);
        matchPlusGoldenBorder = findViewById(R.id.matchPlusGoldenBorder);
        VIPView = findViewById(R.id.VIPView);
        imageGenderPreferencesViewer = findViewById(R.id.imageGenderPreferencesViewer);
        imageCountryPreferencesViewer = findViewById(R.id.imageCountryPreferencesViewer);
        GemsViewer = findViewById(R.id.GemsViewer);
        GemsViewer.setText(libs.getUserData("gems"));
        GenderPreferencesViewer = findViewById(R.id.GenderPreferencesViewer);
        CountryPreferencesViewer = findViewById(R.id.CountryPreferencesViewer);
        viewActiveAiFaces = findViewById(R.id.viewActiveAiFaces);
        accept = findViewById(R.id.accept);
        decline = findViewById(R.id.decline);
        viewActiveEffect = findViewById(R.id.viewActiveEffect);
        //////chat//////////////99
        //todo chat
        inputMessageView = findViewById(R.id.inputMessageView);
        editMessage = findViewById(R.id.editMessage);
        send = findViewById(R.id.send);
        mChatRecycler = findViewById(R.id.list);
        mChatRecycler2 = findViewById(R.id.list2);

        mChatRecycler.setLayoutManager(new LinearLayoutManager(context , RecyclerView.VERTICAL , true));
        mChatRecycler.setHasFixedSize(true);

        mChatRecycler2.setLayoutManager(new LinearLayoutManager(context , RecyclerView.VERTICAL , true));
        mChatRecycler2.setHasFixedSize(true);
        messagesList = new ArrayList<>();
        //send
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                message = editMessage.getText().toString();
                if (!message.isEmpty()) {
                    Message msg = new Message("", "text", message, "me", new Date().getTime(), false);
                    sendMessage(msg);
                    editMessage.setText("");
                }
            }
        });
        ////////////////////////99
        LinearLayout = findViewById(R.id.LinearLayout);
        AIButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean AIVisible = AIRecyclerView.getVisibility() == View.VISIBLE;
                AIRecyclerView.setVisibility(View.VISIBLE);
                filtersRecyclerView.setVisibility(View.GONE);
                viewActiveAiFaces.setBackgroundColor(Color.parseColor("#ffffff"));
                viewActiveEffect.setBackgroundColor(Color.parseColor("#40FFFFFF"));
            }
        });
        switchCamera = findViewById(R.id.switchCamera);
        switchCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // show ad then switch camera
                if (VIP) {
                    frontCam = !frontCam;
                    cameraGrabber.changeCameraDevice(frontCam ? Camera.CameraInfo.CAMERA_FACING_FRONT : Camera.CameraInfo.CAMERA_FACING_BACK);
                } else {
                    Toasty.info(context, R.string.id_264, Toast.LENGTH_SHORT, true).show();
                }
            }
        });
        //////////
        boolean theUserIsVIP = Boolean.parseBoolean(libs.getUserData("VIP"));
        view.findViewById(R.id.not_vip).setVisibility(theUserIsVIP ? View.GONE : View.VISIBLE);
        view.findViewById(R.id.vip).setVisibility(theUserIsVIP ? View.VISIBLE : View.GONE);
        view.findViewById(R.id.not_vip_scroll).setVisibility(theUserIsVIP ? View.GONE : View.VISIBLE);
        view.findViewById(R.id.vip_scroll).setVisibility(theUserIsVIP ? View.VISIBLE : View.GONE);
        view.findViewById(R.id.remove_ads).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                libs.dialog_VIP();
            }
        });
        view.findViewById(R.id.remove_ads_scroll).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                libs.dialog_VIP();
            }
        });
        adView = findViewById(R.id.adView);
        adView.loadAd(new AdRequest.Builder().build());
        ////////
        leave = findViewById(R.id.leave);
        leave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (canLeave) {
                    try {
                        StopNow(false);
                    } catch (Exception e) {
                    }
                }
            }
        });
        final FrameLayout view_1_to_hide = findViewById(R.id.view_1_to_hide);
        final FrameLayout view_2_to_hide = findViewById(R.id.view_2_to_hide);

        ImageView chat = findViewById(R.id.chat);
        chat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!friendId.isEmpty()) {
                    //todo chat
                    boolean visibe = inputMessageView.getVisibility() == View.VISIBLE;
                    inputMessageView.setVisibility(visibe ? View.GONE : View.VISIBLE);
                    mChatRecycler.setVisibility(visibe ? View.GONE : View.VISIBLE);
                    mChatRecycler2.setVisibility(visibe ? View.GONE : View.VISIBLE);
                    if (!visibe) {
                        editMessage.requestFocus();
                        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.showSoftInput(editMessage, InputMethodManager.SHOW_IMPLICIT);
                        //Ui
                        mChatRecycler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    mChatRecycler.scrollToPosition(0);
                                } catch (Exception e) {
                                }
                            }
                        }, 100);
                        mChatRecycler2.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    mChatRecycler2.scrollToPosition(01);
                                } catch (Exception e) {
                                }
                            }
                        }, 100);
                    }
                }
            }
        });
        swipeLayout.setOnActionsListener(new SwipeLayout.SwipeActionsListener() {
            @Override
            public void onOpen(int direction, boolean isContinuous) {
                if (direction == swipeLayout.LEFT) {
                    swipeLayout.close(false);
                    if (search) {
                        return;
                    }
                    if (canSwipe) {
                        endCurrentCallAndStartANewOne();
                    }
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
                LinearLayout.setVisibility(View.GONE);
                otherView.setVisibility(View.VISIBLE);
                return false;
            }
        });
        ImageView showEffectsAndFiltersView = findViewById(R.id.showEffectsAndFiltersView);
        otherView = findViewById(R.id.otherView);
        showEffectsAndFiltersView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean AIVisible = LinearLayout.getVisibility() == View.VISIBLE;
                LinearLayout.setVisibility(AIVisible ? View.GONE : View.VISIBLE);
                otherView.setVisibility(AIVisible ? View.VISIBLE : View.GONE);
                AIRecyclerView.setVisibility(View.VISIBLE);
                filtersRecyclerView.setVisibility(View.GONE);
                viewActiveAiFaces.setBackgroundColor(Color.parseColor("#ffffff"));
                viewActiveEffect.setBackgroundColor(Color.parseColor("#40FFFFFF"));
                swipeLayout.setEnabledSwipe(AIVisible);
            }
        });
        showFilters = findViewById(R.id.loadEffects);
        showFilters.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                swipeLayout.setEnabledSwipe(false);
                filtersRecyclerView.setVisibility(View.VISIBLE);
                AIRecyclerView.setVisibility(View.GONE);
                viewActiveEffect.setBackgroundColor(Color.parseColor("#ffffff"));
                viewActiveAiFaces.setBackgroundColor(Color.parseColor("#40FFFFFF"));
            }
        });
        addFriend = findViewById(R.id.addFriend);
        addFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!friendId.isEmpty()) {
                    FirebaseDatabase.getInstance().getReference("users").child(friendId).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot DataSnapshot) {
                            statue = "default";
                            if (DataSnapshot.child("friendRequests").child(myUserID).exists()) {
                                statue = "alreadySentRequestsBefore";
                            }
                            if (DataSnapshot.child("friends").child(myUserID).exists()) {
                                statue = "weAreFriendsAlready";
                            }
                            FirebaseDatabase.getInstance().getReference("users").child(myUserID).child("friendRequests").child(friendId).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot DataSnapshot) {
                                    if (DataSnapshot.exists()) {
                                        statue = "heAlreadySentMeARequests";
                                    }
                                    switch (statue) {
                                        case "alreadySentRequestsBefore":
                                            Toasty.info(context, R.string.id_265, Toast.LENGTH_SHORT, true).show();
                                            break;
                                        case "weAreFriendsAlready":
                                            Toasty.info(context, R.string.id_266, Toast.LENGTH_SHORT, true).show();
                                            break;
                                        case "heAlreadySentMeARequests":
                                            acceptFriendRequest();
                                            Toasty.custom(context, R.string.id_267, R.drawable.ic_friend_req,
                                                    R.color.colorPrimary, Toast.LENGTH_SHORT, true,
                                                    true).show();
                                            break;
                                        case "default":
                                            HashMap<Object, Object> HashMap = new HashMap<>();
                                            HashMap.put("uid", myUserID);
                                            HashMap.put("addWay", "random");
                                            FirebaseDatabase.getInstance().getReference("users").child(friendId).child("friendRequests").child(myUserID).setValue(HashMap);
                                            Toasty.success(context, R.string.id_268, Toast.LENGTH_SHORT, true).show();
                                            break;
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
            }
        });
        ImageView like = findViewById(R.id.like);
        like.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!friendId.isEmpty()) {
                    FirebaseDatabase.getInstance().getReference("users").child(friendId).child("likes_in_call").push().setValue(myUserID);
                    Toasty.success(context, R.string.id_269, Toast.LENGTH_SHORT, true).show();
                }
            }
        });
        ImageView love = findViewById(R.id.love);
        love.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!friendId.isEmpty()) {
                    libs.dialog_like(friendId);
                }
            }
        });
        report = findViewById(R.id.report);
        report.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!friendId.isEmpty()) {
                    libs.report(friendId,LayoutTwo.this.getActivity());
                }
            }
        });
        mRemoteContainer = findViewById(R.id.remote_video_view_container);
        filtersRecyclerView = findViewById(R.id.filtersRecyclerView);
        big_filterViewer = findViewById(R.id.big_filterViewer);
        small_filterViewer = findViewById(R.id.small_filterViewer);
        AIRecyclerView = findViewById(R.id.AIRecyclerView);
        decline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseDatabase.getInstance().getReference("users").child(myUserID).child("friendRequests").child(friendId).removeValue();
                Toasty.success(context, R.string.id_270, Toast.LENGTH_SHORT, true).show();
                friendRequestView.setVisibility(View.INVISIBLE);
            }
        });
        accept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                acceptFriendRequest();
            }
        });
        KeyboardVisibilityEvent.setEventListener(
                context,
                new KeyboardVisibilityEventListener() {
                    @Override
                    public void onVisibilityChanged(boolean isOpen) {
                        //todo
                        if (isOpen) {
                            view_1_to_hide.setVisibility(View.GONE);
                            view_2_to_hide.setVisibility(View.GONE);

                            mChatRecycler2.setVisibility(View.VISIBLE);
                            mChatRecycler.setVisibility(View.GONE);
                            swipeLayout.setEnabledSwipe(false);
                        } else {
                            view_1_to_hide.setVisibility(View.VISIBLE);
                            view_2_to_hide.setVisibility(View.VISIBLE);

                            mChatRecycler2.setVisibility(View.GONE);
                            mChatRecycler.setVisibility(View.VISIBLE);
                            inputMessageView.setVisibility(View.GONE);
                            swipeLayout.setEnabledSwipe(true);
                        }
                    }
                });
    }
    public void acceptFriendRequest() {
        FirebaseDatabase.getInstance().getReference("users").child(myUserID).child("friendRequests").child(friendId).removeValue();
        new Libs(context).makeFriends(myUserID, friendId, "random", friendId);
        //todo KKK
        addFriend.setImageResource(R.drawable.accepted);
        Toasty.success(context, R.string.id_271, Toast.LENGTH_SHORT, true).show();
        friendRequestView.setVisibility(View.INVISIBLE);
    }
    public void clearDatabse() {
        FirebaseDatabase.getInstance().getReference("search").child(myUserID).removeValue();
    }
    public void StopNow(boolean waitForResumeToBack) {

        exit_wait.setVisibility(View.VISIBLE);

        HashMap<String, Object> defaults = new HashMap<>();
        defaults.put("favoriteGender", "noPreferences");
        defaults.put("favoriteCountry", "noPreferences");
        FirebaseDatabase.getInstance().getReference("users").child(getUserId()).updateChildren(defaults);
        messagesList.clear();
        try {
            context.unregisterReceiver(friendsRevicer);
        } catch (Exception e) {
        }
        try {
            mRtcEngine.leaveChannel();
        } catch (Exception e) {
        }
        if (cameraGrabber != null) {
            timerHandler.removeCallbacks(timerRunnable);
            clearDatabse();
            cameraGrabber.setFrameReceiver(null);
            cameraGrabber.stopPreview();
            cameraGrabber.releaseCamera();
            cameraGrabber = null;
            callInProgress = false;
            FirebaseDatabase.getInstance().getReference("users").child(myUserID).child("currentFilter").removeValue();
            mRemoteContainer.removeAllViews();
            mLocalContainer.removeAllViews();
            RtcEngine.destroy();
            deepAR.release();
            if (!waitForResumeToBack) {
                context.changeFrag(context.layoutOne);
            }
        }
    }
    public void onBackPressed() {
        if (!canLeave) {
            return;
        }
        boolean AIVisible = LinearLayout.getVisibility() == View.VISIBLE;
        if (AIVisible) {
            LinearLayout.setVisibility(AIVisible ? View.GONE : View.VISIBLE);
            otherView.setVisibility(AIVisible ? View.VISIBLE : View.GONE);
            AIRecyclerView.setVisibility(View.VISIBLE);
            filtersRecyclerView.setVisibility(View.GONE);
            viewActiveAiFaces.setBackgroundColor(Color.parseColor("#ffffff"));
            viewActiveEffect.setBackgroundColor(Color.parseColor("#40FFFFFF"));
            return;
        }
        StopNow(false);
    }
    public void endCurrentCallAndStartANewOne() {
        messagesList.clear();
        mRemoteContainer.removeAllViews();
        mRtcEngine.leaveChannel();
        lastFriendId = friendId;
        friendId = "";
        context.getSharedPreferences("data", 0).edit().putString("last_friend", "").apply();
        nameViewer.setText(getString(R.string.id_247));
        countryViewer.setText("...");
        profile_image_viewer.setImageDrawable(this.getResources().getDrawable(R.drawable.loading));
        addFriend.setImageResource(R.drawable.add_f_bold);
        likesViewer.setText("...");
        // new search
        callInProgress = true;
        showSearchDialog();
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("country", myCountry);
        hashMap.put("gender", myGender);
        hashMap.put("wanted_gender", wanted_gender);
        hashMap.put("wanted_country", wanted_country);
        hashMap.put("created_at", getTimeNow());
        FirebaseDatabase.getInstance().getReference("search").child(myUserID).updateChildren(hashMap);
    }
    String userCountryCode = "";
    //todo chat
    String friendImage;
    boolean meLoaded = false, heLoaded = false, messagesLoaded = false;
    public String sendMessage(Message message) {
        /* image , text , video , voice , video_call , audio_call */
        DatabaseReference myMessagesDatabaseReference = FirebaseDatabase.getInstance().getReference("users").child(myUserID).child("friends").child(friendId).child("chat").push();
        DatabaseReference myFriendDatabaseReference = FirebaseDatabase.getInstance().getReference("users").child(friendId).child("friends").child(myUserID).child("chat").push();
        message.key = myFriendDatabaseReference.getKey();
        message.who = "me";
        myMessagesDatabaseReference.setValue(message);
        message.who = "him";
        myFriendDatabaseReference.setValue(message);
        return message.key;
    }
    inCallChatAdapter chatAdapter;
    public void loadMyImage() {
        FirebaseDatabase.getInstance().getReference("users").child(myUserID).child("profileImage").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot DsenderImage) {
                String myImage = String.valueOf(DsenderImage.getValue());
                chatAdapter = new inCallChatAdapter(context, messagesList, profileImage, myImage);
                mChatRecycler.setAdapter(chatAdapter);
                mChatRecycler2.setAdapter(chatAdapter);
                meLoaded = true;
                loadMessages();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }
    boolean firstTimeloadMessages = true;
    public void loadMessages() {
        if (!meLoaded && messagesLoaded) {
            return;
        }
        messagesLoaded = true;
        FirebaseDatabase.getInstance().getReference("users").child(myUserID).child("friends").child(friendId).child("chat").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot messages) {
                final boolean firstTime = messagesList.isEmpty();
                messagesList.clear();
                for (DataSnapshot messageDataSnapshot : messages.getChildren()) {
                    Message message = messageDataSnapshot.getValue(Message.class);
                    if (message != null && message.who != null) {
                        message.key = messageDataSnapshot.getKey();
                        messagesList.add(message);
                    }
                }
                 Collections.reverse(messagesList);
                mChatRecycler.setVisibility(firstTimeloadMessages ? View.GONE : View.VISIBLE);
                mChatRecycler2.setVisibility(firstTimeloadMessages ? View.GONE : View.VISIBLE);
                firstTimeloadMessages = false;
                chatAdapter.notifyDataSetChanged();



                mChatRecycler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            if (firstTime) {
                                mChatRecycler.scrollToPosition(0);
                            } else {
                                mChatRecycler.smoothScrollToPosition(0);
                            }
                        } catch (Exception e) {
                        }
                    }
                }, 100);
                mChatRecycler2.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            if (firstTime) {
                                mChatRecycler2.scrollToPosition(0);
                            } else {
                                mChatRecycler2.smoothScrollToPosition(0);
                            }
                        } catch (Exception e) {
                        }
                    }
                }, 100);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }
    //end
    public void setupFilters() {
        LinearLayoutManager horizontalLayoutManagaer = new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false);
        horizontalLayoutManagaer.setReverseLayout(true);
        filtersRecyclerView.setLayoutManager(horizontalLayoutManagaer);
        filterArrayList = new ArrayList<>();
        filtersAdapter = new FiltersAdapter(context, filterArrayList, new OnFilterChangeListener() {
            @Override
            public void onFilterChange(String newFilter) {
                if (!newFilter.equals("noFilter")) {
                    Picasso.get().load(newFilter).into(iamTheBiggerOne ? big_filterViewer : small_filterViewer);
                }
                myCurrentFilter = newFilter;
                //upload Filter
                if (video_room_id != null && !video_room_id.isEmpty()) {
                    FirebaseDatabase.getInstance().getReference("users").child(myUserID).child("currentFilter").setValue(myCurrentFilter);
                }
                filtersAdapter.notifyDataSetChanged();
            }
        });
        filtersRecyclerView.setAdapter(filtersAdapter);
        loadFilters();
    }
    public void setupAIFaces() {
        LinearLayoutManager horizontalLayoutManagaer = new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false);
        horizontalLayoutManagaer.setReverseLayout(true);
        AIRecyclerView.setLayoutManager(horizontalLayoutManagaer);
        AIArrayList = new ArrayList<>();
        // you can load online
        AIArrayList.addAll(libs.loadAIFaces());


        AIAdapter = new AIAdapter(context, AIArrayList, new OnAIFaceChangeListener() {
            @Override
            public void onAIFaceChange(String path) {
                myCurrentAIFace = path;
                deepAR.switchEffect("mask", path);
            }
        });
        AIRecyclerView.setAdapter(AIAdapter);
        AIAdapter.notifyDataSetChanged();
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
    DatabaseReference oldFilterRef;
    ValueEventListener olfFilterRefListener;
    public void ListenToMyFriendFilterChange(String myFriendUid) {
        //listen to the path that my friend will upload the filter to
        if (myFriendUid != null && !myFriendUid.isEmpty()) {
            if (oldFilterRef != null && olfFilterRefListener != null) {
                oldFilterRef.removeEventListener(olfFilterRefListener);
            }
            oldFilterRef = FirebaseDatabase.getInstance().getReference("users").child(myFriendUid).child("currentFilter");
            olfFilterRefListener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot currentFilterData) {
                    String myFriendFilter;
                    if (currentFilterData.exists()) {
                        myFriendFilter = currentFilterData.getValue().toString();
                    } else {
                        myFriendFilter = "https://firebasestorage.googleapis.com/v0/b/vediochat-7914e.appspot.com/o/png.png?alt=media&token=2b2f5299-4c9f-4237-b17c-21b553d79236";
                    }
                    if (!myFriendFilter.equals("noFilter")) {
                        Picasso.get().load(myFriendFilter).into(iamTheBiggerOne ? small_filterViewer : big_filterViewer);
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                }
            };
            oldFilterRef.addValueEventListener(olfFilterRefListener);
        }
    }
    boolean search;
    public void showSearchDialog() {
        search = true;
        canLeave = true;
        searching.setVisibility(View.VISIBLE);
        swipeLayout.setEnabledSwipe(false);
        swipeLayout.setEnabled(false);
    }
    @SuppressLint("MissingPermission")
    public void hideSearchDialog() {
        searching.setVisibility(View.GONE);
        adView.loadAd(new AdRequest.Builder().build());
    }
    String profileImage;
    public void loadTheRandomUserData(String Uid) {
        //todo timer
        canSwipe = false;
        canLeave = false;
        RingProgressBar.setVisibility(View.VISIBLE);
        leave.setVisibility(View.GONE);
        swipeLayout.setEnabledSwipe(false);
        swipeLayout.setEnabled(false);
        FirebaseDatabase.getInstance().getReference("users").child(Uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot user) {
                if (user.child("friends").child(myUserID).exists()) {
                    //todo KKK
                    addFriend.setImageResource(R.drawable.accepted);
                }
                search = false;
                final boolean theUserIsVIP = Boolean.parseBoolean(libs.getUserData("VIP"));
                if (theUserIsVIP) {
                    hideSearchDialog();
                } else {
                    new Handler(context.getMainLooper()).postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            hideSearchDialog();
                        }
                    }, 2000);
                }
                //todo timer
                RingProgressBar.setMax(theUserIsVIP ? 5 : 7);
                new CountDownTimer(theUserIsVIP ? 5000 : 7000, 1000) {
                    @Override
                    public void onTick(long millisUntilFinished) {
                        //this will be done every 1000 milliseconds ( 1 seconds )
                        int progress = (int) ((theUserIsVIP ? 5000 : 7000 - millisUntilFinished) / 1000);
                        RingProgressBar.setProgress(progress);
                    }
                    @Override
                    public void onFinish() {
                        swipeLayout.setEnabledSwipe(true);
                        swipeLayout.setEnabled(true);
                        canSwipe = true;
                        RingProgressBar.setVisibility(View.GONE);
                        leave.setVisibility(View.VISIBLE);
                        canLeave = true;
                    }
                }.start();
                String city = String.valueOf(user.child("city").getValue());
                String country = String.valueOf(user.child("country").getValue());
                String name = String.valueOf(user.child("name").getValue());
                profileImage = String.valueOf(user.child("profileImage").getValue());
                String likes = String.valueOf(user.child("likes_in_call").getChildrenCount());
                boolean heIsUsingMatchPlus = (boolean) user.child("match_plus").getValue();
                boolean heIsVIP = false;
                try {
                    heIsVIP = (boolean) user.child("VIP").getValue();
                } catch (Exception e) {
                    heIsVIP = false;
                }
                VIPView.setVisibility(heIsVIP ? View.VISIBLE : View.GONE);
                matchPlusGoldenBorder.setVisibility(heIsUsingMatchPlus ? View.VISIBLE : View.GONE);
                try {
                    userCountryCode = String.valueOf(user.child("CountryCode").getValue());
                } catch (Exception e) {
                    userCountryCode = "";
                }
                if (name != null) {
                    name = getHideName(name);
                }
                boolean hideLocation = false;
                try {
                    hideLocation = (boolean) user.child("hideLocation").getValue();
                } catch (Exception e) {
                    hideLocation = false;
                }
                String fakeName = String.valueOf(user.child("fakeName").getValue());
                if (fakeName != null) {
                    nameViewer.setText(getHideName(fakeName));
                } else {
                    nameViewer.setText(getHideName(name));
                }

                Picasso.get().load(profileImage).into(profile_image_viewer);
                if (heIsVIP && user.hasChild("fakeCountry") && user.hasChild("fakeCity") && user.hasChild("fakeCountryCode")) {
                    String fakeCountry = String.valueOf(user.child("fakeCountry").getValue());
                    String fakeCountryCode = String.valueOf(user.child("fakeCountryCode").getValue());
                    String fakeCity = String.valueOf(user.child("fakeCity").getValue());
                    countryViewer.setText(fakeCountry + " , " + fakeCity);
                    String flagString = libs.getCountryFlagWithCountryCode(countriesList, fakeCountryCode);
                    Picasso.get().load(flagString).into(flag);
                } else {
                    countryViewer.setText(country + " , " + city);
                    String flagString = libs.getCountryFlagWithCountryCode(countriesList, userCountryCode);
                    Picasso.get().load(flagString).into(flag);
                }
                countryViewer.setVisibility(hideLocation ? View.GONE : View.VISIBLE);
                flag.setVisibility(hideLocation ? View.GONE : View.VISIBLE);
                likesViewer.setText(likes);
                FirebaseDatabase.getInstance().getReference("users").child(friendId).child("history").child(myUserID).setValue(new Date().getTime());
                FirebaseDatabase.getInstance().getReference("users").child(myUserID).child("history").child(friendId).setValue(new Date().getTime());
                loadMyImage();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                search = false;
                hideSearchDialog();
                swipeLayout.setEnabledSwipe(true);
                swipeLayout.setEnabled(true);
            }
        });
    }
}