package com.kyle.random;
import android.app.Application;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.multidex.MultiDexApplication;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.kyle.random.other.Libs;

import java.util.Objects;

import static com.kyle.random.other.Libs.getTimeNow;
import static com.kyle.random.other.Libs.getUserId;
public class App extends MultiDexApplication {
    @Override
    public void onCreate() {
        super.onCreate();
        getSharedPreferences("data", 0).edit().putString("last_friend", "").apply();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createCallsChannel();
            createNotificationChannel();
        }
        Uid = getSharedPreferences("data", 0).getString("Uid", "");
        if (new Libs(this).isLoggedDone()) {
            FirebaseCrashlytics.getInstance().setUserId( Libs.getUserId());
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
        timerHandler.postDelayed(timerRunnable, Refresh_Time);

    }
    String Uid;
    int Refresh_Time = 5 * 1000;
    Handler timerHandler = new Handler();
    Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            if (Uid != null && !Uid.isEmpty()) {
                if (!getUserId().isEmpty()) {
                    FirebaseDatabase.getInstance().getReference("users").child(getUserId()).child("last_active").setValue(getTimeNow());
                }
            }
            timerHandler.postDelayed(this, Refresh_Time);
        }
    };
    @RequiresApi(api = Build.VERSION_CODES.O)
    public void createNotificationChannel() {
        String channelId = "KYLIE";
        String channelName = "KYLIE";
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        if (notificationManager != null) {
            notificationManager.createNotificationChannel(new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_LOW));
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void createCallsChannel() {
        Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
        NotificationChannel channel = new NotificationChannel("Calls", "Calls", NotificationManager.IMPORTANCE_HIGH);
        channel.setDescription("Calls");
        channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);

        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .build();
        channel.setSound(uri, audioAttributes);

        Objects.requireNonNull(getSystemService(NotificationManager.class)).createNotificationChannel(channel);
    }
}
