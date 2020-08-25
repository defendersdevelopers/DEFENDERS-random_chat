package com.kyle.random.chat;
import android.annotation.SuppressLint;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.hardware.Camera;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.kyle.random.KEYS;
import com.kyle.random.R;
import com.kyle.random.deeparAiVedioCall.AIAdapter;
import com.kyle.random.deeparAiVedioCall.AIFace;
import com.kyle.random.deeparAiVedioCall.CameraGrabber;
import com.kyle.random.deeparAiVedioCall.CameraGrabberListener;
import com.kyle.random.deeparAiVedioCall.DeepARRenderer;
import com.kyle.random.deeparAiVedioCall.Filter;
import com.kyle.random.deeparAiVedioCall.FiltersAdapter;
import com.kyle.random.deeparAiVedioCall.OnAIFaceChangeListener;
import com.kyle.random.deeparAiVedioCall.OnFilterChangeListener;
import com.kyle.random.other.BaseActivity;
import com.kyle.random.other.Country;
import com.kyle.random.other.Libs;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import ai.deepar.ar.AREventListener;
import ai.deepar.ar.DeepAR;
import de.hdodenhof.circleimageview.CircleImageView;
import es.dmoral.toasty.Toasty;
import io.agora.rtc.Constants;
import io.agora.rtc.IRtcEngineEventHandler;
import io.agora.rtc.RtcEngine;
import io.agora.rtc.video.VideoCanvas;
import io.agora.rtc.video.VideoEncoderConfiguration;


import static com.kyle.random.KEYS.AGORA_KEY;
import static com.kyle.random.KEYS.DEEP_AR;
public class InCall extends BaseActivity implements AREventListener {
    String friendUid = "";
    private static final String TAG = "vedioChatRoom";
    private CameraGrabber cameraGrabber;
    private DeepAR deepAR;
    private GLSurfaceView surfaceView;
    private DeepARRenderer renderer;
    ImageView big_filterViewer;
    RecyclerView filtersRecyclerView;
    View otherView, showFilters, AIButton;
    ImageView matchPlusGoldenBorder;
    ImageView switchCamera;
    TextView nameViewer, nameViewerVoice, countryViewer, likesViewer, counter;
    CircleImageView profile_image_viewer, profile_image_voice;
    View viewActiveAiFaces, viewActiveEffect;
    ImageView report;
    LinearLayout LinearLayout;
    ImageView addFriend, flag;
    String theRandomGems, theRandomGenderPreferences, theRandomCountryPreferences;
    TextView GemsViewer, GenderPreferencesViewer, CountryPreferencesViewer;
    ImageView imageGenderPreferencesViewer, imageCountryPreferencesViewer;
    private String video_room_id;
    private RtcEngine mRtcEngine;
    private FrameLayout mLocalContainer, mRemoteContainer;
    ImageView VIPView;
    private String myUserID, message_id;
    boolean isIamTheCreator;
    View voiceCallView;
    boolean isVideo;
    MediaPlayer mediaPlayer;
    public void setSpeaker(boolean speakerOn) {
        AudioManager mAudioMgr = (AudioManager) getSystemService(AUDIO_SERVICE);
        if (speakerOn) {
            mAudioMgr = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            mAudioMgr.setMode(AudioManager.MODE_NORMAL);
            mAudioMgr.setSpeakerphoneOn(true);
        } else {
            mAudioMgr.setMode(AudioManager.MODE_IN_COMMUNICATION);
            mAudioMgr.setSpeakerphoneOn(false);
        }
    }
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.vadio_call);
        speakerBtn = findViewById(R.id.speaker);
        Bundle extras = getIntent().getExtras();
        isIamTheCreator = extras.getBoolean("isIamTheCreator");
        video_room_id = extras.getString("video_room_id");
        friendUid = extras.getString("friendUid");
        message_id = extras.getString("message_id");
        isVideo = extras.getBoolean("isVideo");
        speakerIsOn = isVideo;
        if (isVideo) {
            speakerBtn.setVisibility(View.GONE);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true);
            setTurnScreenOn(true);
            ((KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE)).requestDismissKeyguard(this, null);
        } else {
            getWindow().addFlags(
                    WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                            WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            );
        }
        myUserID = Libs.getUserId();
        Context context = InCall.this;
        deepAR = new DeepAR(context);
        deepAR.setLicenseKey(DEEP_AR);
        deepAR.initialize(context, this);
        mLocalContainer = findViewById(R.id.localPreview);
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
        initUiAndButtons();
        counter = findViewById(R.id.counter);
        voiceCallView.setVisibility(View.VISIBLE);
        startTheCall();
        setupFilters();
        setupAIFaces();
        if (surfaceView != null) {
            surfaceView.onResume();
        }
        FirebaseDatabase.getInstance().getReference("users").child(myUserID).child("friends").child(friendUid).child("chat").child(message_id).child("statue").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String statue = String.valueOf(dataSnapshot.getValue());
                if (statue.equals("rejected") || statue.equals("finished")) {
                    StopNow();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
        isSearch = false;
        myUserID = Libs.getUserId();
        new Handler(getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                mediaPlayer = new MediaPlayer();
                mediaPlayer.setLooping(true);
                if (!isVideo) {
                    setSpeaker(false);
                    mediaPlayer.setAudioStreamType(AudioManager.STREAM_VOICE_CALL);
                }
                AssetFileDescriptor afd = getResources().openRawResourceFd(R.raw.phone);
                try {
                    mediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
                    afd.close();
                    mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                        @Override
                        public void onPrepared(MediaPlayer mediaPlayer) {
                            if (isIamTheCreator) {
                                mediaPlayer.start();
                            }
                        }
                    });
                    mediaPlayer.prepare();
                } catch (Exception e) {
                }
            }
        }, 500);
        mRemoteContainer.removeAllViews();
    }
    @Override
    protected void onDestroy() {
        StopNow();
        super.onDestroy();
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
                Log.e(TAG, "error: " + err);
            }
            @Override
            public void onJoinChannelSuccess(final String channelName, final int uid, int elapsed) {
                InCall.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        renderer.setCallInProgress(true);
                    }
                });
            }
            @Override
            public void onFirstRemoteVideoDecoded(final int uid, int width, int height, int elapsed) {
                InCall.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.e(TAG, "onFirstRemoteVideoDecoded");
                        setupRemoteVideo(uid);
                    }
                });
            }
            @Override
            public void onUserOffline(final int uid, int reason) {
                InCall.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        removeRemoteVideo();
                        StopNow();
                    }
                });
            }
        };
        try {
            mRtcEngine = RtcEngine.create(this, AGORA_KEY, mRtcEventHandler);
        } catch (Exception e) {
            Log.e(TAG, Log.getStackTraceString(e));
            throw new RuntimeException("NEED TO check rtc sdk init fatal error\n" + Log.getStackTraceString(e));
        }
        mRtcEngine.setChannelProfile(Constants.CHANNEL_PROFILE_LIVE_BROADCASTING);
    }
    private void setupVideoConfig() {
        mRtcEngine.enableVideo();
        mRtcEngine.setExternalVideoSource(true, true, true);
        mRtcEngine.setVideoEncoderConfiguration(new VideoEncoderConfiguration(
                KEYS.VIDEO_QUALITY,
                VideoEncoderConfiguration.FRAME_RATE.FRAME_RATE_FPS_15,
                VideoEncoderConfiguration.STANDARD_BITRATE,
                VideoEncoderConfiguration.ORIENTATION_MODE.ORIENTATION_MODE_FIXED_PORTRAIT));
    }
    private void joinChannel() {
        mRtcEngine.setClientRole(Constants.CLIENT_ROLE_BROADCASTER);
        mRtcEngine.joinChannel(null, video_room_id, "Extra Optional Data", 0);
    }
    private void setupRemoteVideo(int uid) {
        if (mRemoteContainer.getChildCount() >= 1) {
            return;
        }
        final Handler handler
                = new Handler();
        final int[] seconds = {0};
        final boolean running = true;
        handler.post(new Runnable() {
            @Override

            public void run()
            {
                int hours = seconds[0] / 3600;
                int minutes = (seconds[0] % 3600) / 60;
                int secs = seconds[0] % 60;

                // Format the seconds into hours, minutes,
                // and seconds.
                String time
                        = String
                        .format(Locale.getDefault(),
                                "%d:%02d:%02d", hours,
                                minutes, secs);

                // Set the text view text.
                counter.setText(time);
                if (running) {
                    seconds[0]++;
                }

                handler.postDelayed(this, 1000);
            }
        });
       /* Stopwatch stopwatch = new StopwatchBuilder()
                .startFormat("MM:SS")
                .onTick(new TimeTickListener() {
                    @Override
                    public void onTick(String time) {
                        counter.setText(time);
                    }
                })
                .changeFormatWhen(1, TimeUnit.HOURS, "HH:MM:SS")
                .build();
        stopwatch.start();

        */
        voiceCallView.setVisibility(isVideo ? View.GONE : View.VISIBLE);
        SurfaceView surfaceView = RtcEngine.CreateRendererView(this);
        mRemoteContainer.addView(surfaceView);
        mRtcEngine.setupRemoteVideo(new VideoCanvas(surfaceView, VideoCanvas.RENDER_MODE_HIDDEN, uid));
        surfaceView.setTag(uid);
        if (mediaPlayer != null) {
            mediaPlayer.stop();
        }
    }
    private void removeRemoteVideo() {
        mRemoteContainer.removeAllViews();
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
    boolean iamTheBiggerOne = false;
    com.kyle.random.deeparAiVedioCall.AIAdapter AIAdapter;
    String myCurrentFilter = "noFilter";
    FiltersAdapter filtersAdapter;
    ArrayList<Filter> filterArrayList;
    RecyclerView AIRecyclerView;
    ArrayList<AIFace> AIArrayList;
    String myCurrentAIFace;
    boolean VIP;
    ImageView small_filterViewer;
    boolean frontCam = true;
    boolean isSearch = false;
    private void startTheCall() {
        joinChannel();
        ;
        ListenToMyFriendFilterChange(friendUid);
        loadTheRandomUserData(friendUid);
    }
    public void setupFilters() {
        LinearLayoutManager horizontalLayoutManagaer = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        horizontalLayoutManagaer.setReverseLayout(true);
        filtersRecyclerView.setLayoutManager(horizontalLayoutManagaer);
        filterArrayList = new ArrayList<>();
        filtersAdapter = new FiltersAdapter(this, filterArrayList, new OnFilterChangeListener() {
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
        LinearLayoutManager horizontalLayoutManagaer = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        horizontalLayoutManagaer.setReverseLayout(true);
        AIRecyclerView.setLayoutManager(horizontalLayoutManagaer);
        AIArrayList = new ArrayList<>();
        // you can load online
        AIArrayList.addAll(libs.loadAIFaces());
        AIAdapter = new AIAdapter(this, AIArrayList, new OnAIFaceChangeListener() {
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
    public void initUiAndButtons() {
        voiceCallView = findViewById(R.id.voiceCallView);
        nameViewer = findViewById(R.id.nameViewer);
        nameViewerVoice = findViewById(R.id.name2);
        profile_image_viewer = findViewById(R.id.profile_image);
        profile_image_voice = findViewById(R.id.image2);
        countryViewer = findViewById(R.id.countryViewer);
        flag = findViewById(R.id.flag);
        likesViewer = findViewById(R.id.likesViewer);
        AIButton = findViewById(R.id.loadAiFaces);
        matchPlusGoldenBorder = findViewById(R.id.matchPlusGoldenBorder);
        VIPView = findViewById(R.id.VIPView);
        libs = new Libs(this);
        VIP = Boolean.parseBoolean(libs.getUserData("VIP"));
        imageGenderPreferencesViewer = findViewById(R.id.imageGenderPreferencesViewer);
        imageCountryPreferencesViewer = findViewById(R.id.imageCountryPreferencesViewer);
        GemsViewer = findViewById(R.id.GemsViewer);
        GenderPreferencesViewer = findViewById(R.id.GenderPreferencesViewer);
        CountryPreferencesViewer = findViewById(R.id.CountryPreferencesViewer);
        viewActiveAiFaces = findViewById(R.id.viewActiveAiFaces);
        viewActiveEffect = findViewById(R.id.viewActiveEffect);
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
                if (VIP) {
                    frontCam = !frontCam;
                    cameraGrabber.changeCameraDevice(frontCam ? Camera.CameraInfo.CAMERA_FACING_FRONT : Camera.CameraInfo.CAMERA_FACING_BACK);
                } else {
                    Toasty.info(getApplicationContext(), R.string.id_356, Toast.LENGTH_SHORT, true).show();
                }
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
            }
        });
        showFilters = findViewById(R.id.loadEffects);
        showFilters.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean FiltersVisible = filtersRecyclerView.getVisibility() == View.VISIBLE;
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
                if (!friendUid.isEmpty()) {
                    //todo friend
                    HashMap<Object, Object> HashMap = new HashMap<>();
                    HashMap.put("uid", myUserID);
                    HashMap.put("addWay", "random");
                    FirebaseDatabase.getInstance().getReference("users").child(friendUid).child("friendRequests").child(myUserID).setValue(HashMap);
                    Toasty.success(InCall.this, R.string.id_194, Toast.LENGTH_SHORT, true).show();
                }
            }
        });
        ImageView like = findViewById(R.id.like);
        like.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!friendUid.isEmpty()) {
                    libs.dialog_like(friendUid);
                }
            }
        });
        ImageView so_like = findViewById(R.id.so_like);
        so_like.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!friendUid.isEmpty()) {
                    libs.dialog_so_like(friendUid);
                }
            }
        });
        report = findViewById(R.id.report);
        report.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!friendUid.isEmpty()) {
                    libs.report(friendUid, InCall.this);
                }
            }
        });
        mRemoteContainer = findViewById(R.id.remote_video_view_container);
        filtersRecyclerView = findViewById(R.id.filtersRecyclerView);
        big_filterViewer = findViewById(R.id.big_filterViewer);
        small_filterViewer = findViewById(R.id.small_filterViewer);
        AIRecyclerView = findViewById(R.id.AIRecyclerView);
    }
    public void ListenToMyFriendFilterChange(String myFriendUid) {
        //listen to the path that my friend will upload the filter to
        if (myFriendUid != null && !myFriendUid.isEmpty()) {
            FirebaseDatabase.getInstance().getReference("users").child(myFriendUid).child("currentFilter").addValueEventListener(new ValueEventListener() {
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
            });
        }
    }
    public void StopNow() {
        if (cameraGrabber != null) {
            cameraGrabber.setFrameReceiver(null);
            cameraGrabber.stopPreview();
            cameraGrabber.releaseCamera();
            cameraGrabber = null;
            mRtcEngine.leaveChannel();
            mRemoteContainer.removeAllViews();
            RtcEngine.destroy();
            deepAR.release();
        }
        if (mediaPlayer != null) {
            mediaPlayer.stop();
        }
        FirebaseDatabase.getInstance().getReference("users").child(myUserID).child("currentFilter").removeValue();
        FirebaseDatabase.getInstance().getReference("users").child(myUserID).child("friends").child(friendUid).child("chat").child(message_id).child("statue").setValue("rejected");
        FirebaseDatabase.getInstance().getReference("users").child(friendUid).child("friends").child(myUserID).child("chat").child(message_id).child("statue").setValue("rejected");
        finish();
    }
    @Override
    public void onBackPressed() {
        boolean AIVisible = LinearLayout.getVisibility() == View.VISIBLE;
        if (AIVisible) {
            LinearLayout.setVisibility(View.GONE);
            otherView.setVisibility(View.VISIBLE);
            AIRecyclerView.setVisibility(View.VISIBLE);
            filtersRecyclerView.setVisibility(View.GONE);
            viewActiveAiFaces.setBackgroundColor(Color.parseColor("#ffffff"));
            viewActiveEffect.setBackgroundColor(Color.parseColor("#40FFFFFF"));
            return;
        }
        StopNow();
    }
    String userCountryCode = "";
    public void loadTheRandomUserData(String Uid) {
        FirebaseDatabase.getInstance().getReference("users").child(Uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull final DataSnapshot user) {
                final String city = String.valueOf(user.child("city").getValue());
                final String country = String.valueOf(user.child("country").getValue());
                String name = String.valueOf(user.child("name").getValue());
                String profileImage = String.valueOf(user.child("profileImage").getValue());
                String likes = String.valueOf(user.child("likes").getChildrenCount());
                theRandomGems = String.valueOf(user.child("gems").getValue());
                theRandomGenderPreferences = String.valueOf(user.child("favoriteGender").getValue());
                theRandomCountryPreferences = String.valueOf(user.child("favoriteCountry").getValue());
                boolean heIsUsingMatchPlus = (boolean) user.child("match_plus").getValue();
                final boolean heIsVIP = user.hasChild("VIP") && (boolean) user.child("VIP").getValue();
                VIPView.setVisibility(heIsVIP ? View.VISIBLE : View.GONE);
                matchPlusGoldenBorder.setVisibility(heIsUsingMatchPlus ? View.VISIBLE : View.GONE);
                try {
                    userCountryCode = String.valueOf(user.child("CountryCode").getValue());
                } catch (Exception e) {
                    userCountryCode = "";
                }
                boolean hideLocation = false;
                try {
                    hideLocation = (boolean) user.child("hideLocation").getValue();
                } catch (Exception e) {
                    hideLocation = false;
                }
                if (user.hasChild("fakeName")) {
                    String fakeName = String.valueOf(user.child("fakeName").getValue());
                    nameViewer.setText(fakeName);
                    nameViewerVoice.setText(fakeName);
                } else {
                    nameViewer.setText(name);
                    nameViewerVoice.setText(name);
                }
                Picasso.get().load(profileImage).into(profile_image_viewer);
                Picasso.get().load(profileImage).into(profile_image_voice);
                libs.loadCountries(new Libs.OnLoadListener() {
                    @Override
                    public void OnLoad(ArrayList<Country> countriesList) {
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
                    }
                });
                countryViewer.setVisibility(hideLocation ? View.GONE : View.VISIBLE);
                flag.setVisibility(hideLocation ? View.GONE : View.VISIBLE);
                likesViewer.setText(likes);
                GemsViewer.setText(theRandomGems);
                if (theRandomCountryPreferences.equals("noPreferences") || theRandomCountryPreferences == null || theRandomCountryPreferences.isEmpty() || theRandomCountryPreferences.equals("null")) {
                    theRandomCountryPreferences = "noPreferences";
                    imageCountryPreferencesViewer.setImageDrawable(getResources().getDrawable(R.drawable.world));
                } else {
                    switch (theRandomCountryPreferences) {
                        case "noPreferences":
                            imageCountryPreferencesViewer.setImageDrawable(getResources().getDrawable(R.drawable.world));
                            break;
                        default:
                            imageCountryPreferencesViewer.setImageDrawable(getResources().getDrawable(libs.getCountryFlagByCountryName(theRandomCountryPreferences)));
                            break;
                    }
                }
                CountryPreferencesViewer.setText(theRandomCountryPreferences);
                String GenderPreferencesName = "";
                switch (theRandomGenderPreferences) {
                    case "female":
                        GenderPreferencesName = getString(R.string.id_67);
                        imageGenderPreferencesViewer.setImageDrawable(getResources().getDrawable(R.drawable.female_azar));
                        break;
                    case "male":
                        GenderPreferencesName = getString(R.string.id_68);
                        imageGenderPreferencesViewer.setImageDrawable(getResources().getDrawable(R.drawable.male_azar));
                        break;
                    case "noPreferences":
                        GenderPreferencesName = getString(R.string.id_118);
                        imageGenderPreferencesViewer.setImageDrawable(getResources().getDrawable(R.drawable.gender_azar));
                        break;
                }
                GenderPreferencesViewer.setText(GenderPreferencesName);
                //todo لو كان محظور
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }
    public void end_call(View view) {
        StopNow();
    }
    ImageView speakerBtn;
    boolean speakerIsOn;
    public void speaker(View view) {
        speakerIsOn = !speakerIsOn;
        setSpeaker(speakerIsOn);
        speakerBtn.setImageResource(speakerIsOn ? R.drawable.speaker : R.drawable.speaker_off);
    }
}