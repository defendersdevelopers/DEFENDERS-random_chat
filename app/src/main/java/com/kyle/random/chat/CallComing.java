package com.kyle.random.chat;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.annotations.NotNull;
import com.kyle.random.R;
import com.kyle.random.other.BaseActivity;
import com.ncorti.slidetoact.SlideToActView;
import com.squareup.picasso.Picasso;


import java.util.Timer;
import java.util.TimerTask;

import static com.kyle.random.other.Libs.getUserId;
public class CallComing extends BaseActivity {
    String myUid;
    SlideToActView example_gray_on_green;
    private Timer timer = new Timer();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.call_coming);

        final boolean isVideo = getIntent().getStringExtra("kind").equals("video_call");
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
        example_gray_on_green = findViewById(R.id.example_gray_on_green);
        example_gray_on_green.setText(isVideo ? "Slide to Video Call" : "Slide to Audio Call");
        example_gray_on_green.setOnSlideCompleteListener(new SlideToActView.OnSlideCompleteListener() {
            @Override
            public void onSlideComplete(@NotNull SlideToActView slideToActView) {
                String room_id = getIntent().getStringExtra("room_id");
                String friend = getIntent().getStringExtra("user");
                String message_id = getIntent().getStringExtra("message_id");
                Intent mIntent = new Intent(CallComing.this, InCall.class);
                mIntent.putExtra("video_room_id", room_id);
                mIntent.putExtra("friendUid", friend);
                mIntent.putExtra("message_id", message_id);
                mIntent.putExtra("isIamTheCreator", false);
                mIntent.putExtra("isVideo", isVideo);
                mIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(mIntent);
                FirebaseDatabase.getInstance().getReference("users").child(myUid).child("friends").child(friend).child("chat").child(message_id).child("statue").setValue("accepted");
                FirebaseDatabase.getInstance().getReference("users").child(friend).child("friends").child(myUid).child("chat").child(message_id).child("statue").setValue("accepted");
                finish();
            }
        });
        String user = getIntent().getStringExtra("user");
        String message_id = getIntent().getStringExtra("message_id");
        myUid = getUserId();
        loadFriendDate(user);
        FirebaseDatabase.getInstance().getReference("users").child(myUid).child("friends").child(user).child("chat").child(message_id).child("statue").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String statue = String.valueOf(dataSnapshot.getValue());
                if (statue.equals("rejected") || statue.equals("accepted") || statue.equals("finished")) {
                    finish();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
        Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
        ringtone = RingtoneManager.getRingtone(this, uri);
        ringtone.play();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                reject(null);
            }
        };
        timer.schedule(task, 15000);
    }
    public void loadFriendDate(String userId) {
        final ImageView imageView = findViewById(R.id.image);
        final TextView nameViewer = findViewById(R.id.name);
        FirebaseDatabase.getInstance().getReference("users").child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot user) {
                String image = String.valueOf(user.child("profileImage").getValue());
                String name = String.valueOf(user.child("name").getValue());
                Picasso.get().load(image).placeholder(R.drawable.acc_blue).error(R.drawable.acc_blue).into(imageView);
                nameViewer.setText(name);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }
    Ringtone ringtone;
    @Override
    protected void onDestroy() {
        ringtone.stop();
        timer.cancel();
        super.onDestroy();
    }
    public void reject(View view) {

        String friend = getIntent().getStringExtra("user");
        String message_id = getIntent().getStringExtra("message_id");
        FirebaseDatabase.getInstance().getReference("users").child(myUid).child("friends").child(friend).child("chat").child(message_id).child("statue").setValue("rejected");
        FirebaseDatabase.getInstance().getReference("users").child(friend).child("friends").child(myUid).child("chat").child(message_id).child("statue").setValue("rejected");
        finish();
    }
}
