package com.kyle.random.getStarted;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.internal.ImageRequest;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.kyle.random.R;
import com.kyle.random.main.fragmentViewer;
import com.kyle.random.other.BaseActivity;
import com.kyle.random.other.Libs;
import com.yqritc.scalablevideoview.ScalableVideoView;

import org.json.JSONObject;

import java.io.IOException;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import es.dmoral.toasty.Toasty;

import static com.kyle.random.other.Libs.getUserId;
public class get_started extends BaseActivity {
    GoogleSignInOptions gso;
    FirebaseAuth mAuth;
    int RC_SIGN_IN = 200;
    LoginButton loginButton;
    private GoogleSignInClient mGoogleSignInClient;
    CallbackManager mCallbackManager;

    public void setSpeaker(boolean speakerOn) {
        AudioManager mAudioMgr = (AudioManager) getSystemService(AUDIO_SERVICE);

        if(speakerOn){
            mAudioMgr = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
            mAudioMgr.setMode(AudioManager.MODE_NORMAL);
            mAudioMgr.setSpeakerphoneOn(true);


            Toast.makeText(getApplicationContext(), "SpeakerPhone On", Toast.LENGTH_LONG).show();
        }else{
            mAudioMgr.setMode(AudioManager.MODE_IN_COMMUNICATION);
            mAudioMgr.setSpeakerphoneOn(false);

            Toast.makeText(getApplicationContext(), "Wired Headset On", Toast.LENGTH_LONG).show();
        }
    }



    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        GetKeyHash();
        final Libs libs = new Libs(get_started.this);
        if (libs.isLoggedDone()) {
            setContentView(R.layout.get_started_splash);
            Bundle extras = new Bundle();
            if (getIntent().getExtras() != null) {
                extras = getIntent().getExtras();
            }
            if (extras.containsKey("kind")) {
                String kind = extras.getString("kind", "");
                if (kind.equals("image") || kind.equals("text") || kind.equals("voice")) {
                    String user = extras.getString("user", "");
                    libs.saveData("chat_with", user);
                }
            }
         //todo ddd   ((TextView)findViewById(R.id.year)).setText(new SimpleDateFormat( "YYYY" , Locale.ENGLISH).format(new Date()));
        } else {
            setContentView(R.layout.get_started);
            mAuth = FirebaseAuth.getInstance();
            gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(getString(R.string.default_web_client_id))
                    .requestEmail()
                    .build();
            // Build a GoogleSignInClient with the options specified by gso.
            mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
            FirebaseMessaging.getInstance().subscribeToTopic("notifications");
            //  getWindow().setFormat(PixelFormat.TRANSLUCENT);
            final ScalableVideoView mVideoView = findViewById(R.id.video);
//if you want the controls to appear
            try {
                mVideoView.setRawData(R.raw.video);
                mVideoView.setVolume(0, 0);
                mVideoView.setLooping(true);
                mVideoView.prepare(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mp) {
                        mVideoView.start();
                    }
                });
            } catch (IOException ioe) {
                //ignore
            }
            findViewById(R.id.google).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (!onPermissionsGranted) {
                        Toasty.info(get_started.this, getString(R.string.id_619), Toast.LENGTH_SHORT, true).show();
                        return;
                    }
                    Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                    startActivityForResult(signInIntent, RC_SIGN_IN);
                }
            });
            findViewById(R.id.facebook_login).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (loginButton == null) {
                        Toasty.info(get_started.this, getString(R.string.id_619), Toast.LENGTH_SHORT, true).show();
                        return;
                    }
                    loginButton.performClick();
                }
            });
        }
        requestAppPermissions(new String[]{
                android.Manifest.permission.CAMERA,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                android.Manifest.permission.RECORD_AUDIO,
                android.Manifest.permission.ACCESS_NETWORK_STATE,
                android.Manifest.permission.READ_CONTACTS,
                android.Manifest.permission.WRITE_CONTACTS,
                android.Manifest.permission.ACCESS_FINE_LOCATION
        }, new OnPermissionsGrantedListener() {
            @Override
            public void onPermissionsGranted() {
                if (libs.isLoggedDone()) {
                    // todo set time to zero to stop splash screen
                    new Handler(getMainLooper()).postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            //set favoriteGender & favoriteCountry to default
                            FirebaseDatabase.getInstance().getReference("users").child(getUserId()).child("favoriteGender").setValue("noPreferences");
                            FirebaseDatabase.getInstance().getReference("users").child(getUserId()).child("favoriteCountry").setValue("noPreferences");
                            startActivity(new Intent(get_started.this, fragmentViewer.class));
                        }
                    }, 2000);
                } else {
                    onPermissionsGranted = true;
                    setup_login_methods();
                }
            }
        });
    }
    boolean onPermissionsGranted = false;
    public void setup_login_methods() {
        FacebookSdk.sdkInitialize(get_started.this);
        //AppEventsLogger.activateApp(getApplicationContext());
        mCallbackManager = CallbackManager.Factory.create();
        // FacebookButton fb = findViewById(R.id.fb);
        loginButton = findViewById(R.id.login_button);
        loginButton.registerCallback(mCallbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        System.out.println("onSuccess");
                        final String accessToken = loginResult.getAccessToken().getToken();
                        GraphRequest request = GraphRequest.newMeRequest(
                                AccessToken.getCurrentAccessToken(), new GraphRequest.GraphJSONObjectCallback() {
                                    @Override
                                    public void onCompleted(JSONObject me, GraphResponse response) {
                                        if (AccessToken.getCurrentAccessToken() != null) {
                                            if (me != null) {
                                                profileImageUrl = ImageRequest.getProfilePictureUri(me.optString("id"), 500, 500).toString();
                                                handleFacebookAccessToken(accessToken);
                                            }
                                        }
                                    }
                                });
                        GraphRequest.executeBatchAsync(request);
                    }
                    @Override
                    public void onCancel() {
                        System.out.println("onCancel");
                    }
                    @Override
                    public void onError(FacebookException exception) {
                        System.out.println("onError");
                        Log.v("LoginActivity", exception.getCause().toString());
                    }
                });
    }
    String profileImageUrl = "";
    private void handleFacebookAccessToken(String token) {
        AuthCredential credential = FacebookAuthProvider.getCredential(token);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            nextPage(Libs.getUserId(), "facebook");
                        }
                    }
                });
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            //google
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                Toasty.error(this, "Google sign in failed "+e.toString(), Toast.LENGTH_SHORT, true).show();
            }
        } else {
            //facebook
            mCallbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }
    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                getSharedPreferences("data", MODE_PRIVATE).edit().putString("Uid",Libs.getUserId()).apply();
                                nextPage(Libs.getUserId(), "google");
                            }
                        } else {
                            Snackbar.make(findViewById(android.R.id.content), "Authentication Failed.", Snackbar.LENGTH_SHORT).show();
                        }
                    }
                });
    }
    public void nextPage(final String uid, final String login_method) {
        FirebaseDatabase.getInstance().getReference("users").child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot != null && dataSnapshot.exists() && dataSnapshot.hasChild("name")) {
                    //login
                    SharedPreferences.Editor editor = getSharedPreferences("data", MODE_PRIVATE).edit();
                    editor.putString("Uid", uid);
                    editor.apply();
                    startActivity(new Intent(get_started.this, fragmentViewer.class));
                } else {
                    //create new account
                    startActivity(new Intent(get_started.this, create_account.class).putExtra("login_method", login_method).putExtra("profileImageUrl", profileImageUrl));
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }
    public void openLink(View view) {
        Toasty.info(this, getString(R.string.id_634)  + " "+ libs.getUserData("support_email"), Toast.LENGTH_LONG, true).show();
    }
    public void terms(View view) {
        startActivity(new Intent(this, terms.class));
    }
    public void privacy(View view) {
        startActivity(new Intent(this, privacy.class));
    }
    private void GetKeyHash() {
        try {
            PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = (MessageDigest.getInstance("SHA"));
                md.update(signature.toByteArray());
                String hashkey_value = new String(Base64.encode(md.digest(), 0));
                Log.e("hash key", hashkey_value);

            }
        }catch (Exception e) {
            Log.e("exception", e.toString());
        }
    }

}
