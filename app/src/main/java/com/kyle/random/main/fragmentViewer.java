package com.kyle.random.main;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.kyle.random.R;
import com.kyle.random.chat.ChatFriends;
import com.kyle.random.chat.ChatRoom;
import com.kyle.random.history.History;
import com.kyle.random.matches.matchesActivtiy;
import com.kyle.random.other.BaseActivity;
import com.kyle.random.other.Libs;

import java.util.Objects;

import static com.kyle.random.other.Libs.getUserId;
public class fragmentViewer extends BaseActivity {
    public LayoutOne layoutOne;
    public LayoutTwo layoutTwo;
    public Fragment history;
    public Fragment matches;
    public Fragment chat;
    View bottom_bar;
    public FrameLayout openMain;
    String myUid;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_viewer);
        bottom_bar = findViewById(R.id.bottom_bar);
        myUid = getUserId();
        //upload Online
        layoutOne = new LayoutOne();
        layoutTwo = new LayoutTwo();
        history = new History();
        matches = new matchesActivtiy();
        chat = new ChatFriends();
        openMain = findViewById(R.id.openMain);
        openMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeFrag(layoutOne);
            }
        });
        changeFrag(layoutOne);
        if (new Libs(this).isLoggedDone()) {
            FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                @Override
                public void onComplete(@NonNull Task<InstanceIdResult> task) {
                    if (!task.isSuccessful() || task.getResult() == null) {
                        return;
                    }
                    // Get new Instance ID token
                    String token = task.getResult().getToken();
                    FirebaseDatabase.getInstance().getReference("users").child(getUserId()).child("tokens").child(token).setValue(token);
                }
            });
        }
        String chat_with = libs.getUserData("chat_with");
        if (!chat_with.isEmpty()) {
            libs.saveData("chat_with", "");
            startActivity(new Intent(this, ChatRoom.class).putExtra("friendUid", chat_with));
        }
    }
    @Override
    public void onBillingInitDone() {
        super.onBillingInitDone();
        purchaseHistoryAsync();
    }
    public Fragment oldFragment = null;
    @Override
    protected void onStart() {
        super.onStart();
        if (waitForResumeToBack) {
            waitForResumeToBack = false;
            changeFrag(layoutOne);
            return;
        }
        getSharedPreferences("data", 0).edit()
                .putString("last_friend", "")
                .putString("favoriteGender", "noPreferences")
                .putString("favoriteCountry", "noPreferences")
                .apply();
        if (oldFragment == layoutOne) {
            layoutOne.start();
        }
    }
    @Override
    protected void onStop() {
        if (oldFragment == layoutOne) {
            layoutOne.stop();
        }
        if (oldFragment == layoutTwo) {
            waitForResumeToBack = true;
            layoutTwo.StopNow(true);
        }
        super.onStop();
    }
    @Override
    protected void onResume() {
        super.onStart();
        if (waitForResumeToBack) {
            waitForResumeToBack = false;
            changeFrag(layoutOne);
            return;
        }
        if (oldFragment == layoutOne) {
            layoutOne.start();
        }
        super.onResume();
    }
    @Override
    protected void onPause() {
        if (oldFragment == layoutOne) {
            layoutOne.stop();
        }
        if (oldFragment == layoutTwo) {
            waitForResumeToBack = true;
            layoutTwo.StopNow(true);
        }
        super.onPause();
    }
    boolean waitForResumeToBack = false;
    public void changeFrag(androidx.fragment.app.Fragment frag) {
        if (frag == oldFragment) {
            return;
        }
        getSharedPreferences("data", 0).edit().putString("last_friend", "").apply();
        openMain.setVisibility(View.GONE);
        if (frag == layoutTwo) {
            bottom_bar.setVisibility(View.GONE);
        }
        if (oldFragment != null) {
            if (oldFragment == layoutOne) {
                layoutOne.stop();
                layoutOne.distroy();
            }
            if (frag == layoutOne) {
                layoutOne.start();
            }
            getSupportFragmentManager().beginTransaction().remove(oldFragment).commit();
        }
        oldFragment = frag;
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment, frag).addToBackStack(null).commit();
    }
    @Override
    public void onBackPressed() {
        if (oldFragment == layoutTwo) {
            layoutTwo.onBackPressed();
            return;
        }
        if (oldFragment == layoutOne) {
            boolean AIVisible = layoutOne.EffectsView.getVisibility() == View.VISIBLE;
            if (AIVisible) {
                layoutOne.showEffectsAndFiltersView.performClick();
                return;
            }
        }
        if (oldFragment != layoutOne) {
            changeFrag(layoutOne);
            return;
        }
        LayoutInflater factory = LayoutInflater.from(fragmentViewer.this);
        final View dialogView = factory.inflate(R.layout.alert_exit, null);
        final AlertDialog dialog = new AlertDialog.Builder(fragmentViewer.this).create();
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawableResource(android.R.color.transparent);
        TextView ok = dialogView.findViewById(R.id.ok);
        TextView cancel = dialogView.findViewById(R.id.cancel);
        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moveTaskToBack(true);
                android.os.Process.killProcess(android.os.Process.myPid());
                System.exit(1);
                dialog.dismiss();
            }
        });
        dialog.setView(dialogView);
        dialog.show();
    }
    public void mainFragment(View view) {
        YoYo.with(Techniques.Pulse)
                .duration(1000)
                .playOn(findViewById(R.id.mainFragment));
        changeFrag(layoutOne);
    }
    public void historyFragment(View view) {
        YoYo.with(Techniques.Pulse)
                .duration(1000)
                .playOn(findViewById(R.id.historyFragment));
        changeFrag(history);
    }
    public void matchesFragment(View view) {
        YoYo.with(Techniques.Pulse)
                .duration(1000)
                .playOn(findViewById(R.id.matchesFragment));
        changeFrag(matches);
    }
    public void chatFragment(View view) {
        YoYo.with(Techniques.Pulse)
                .duration(1000)
                .playOn(findViewById(R.id.chatFragment));
        changeFrag(chat);
    }
}