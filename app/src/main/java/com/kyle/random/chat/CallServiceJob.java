package com.kyle.random.chat;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.PowerManager;

import androidx.annotation.NonNull;
import androidx.core.app.JobIntentService;
import androidx.core.app.NotificationCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.kyle.random.other.Libs;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static android.app.Notification.DEFAULT_SOUND;
import static android.app.Notification.DEFAULT_VIBRATE;
import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
public class CallServiceJob extends JobIntentService {
    public static void enqueueWork(Context context, Intent intent) {
        enqueueWork(context, CallServiceJob.class, 2, intent);
    }
    public boolean isForeground() {
        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> runningTaskInfo = manager.getRunningTasks(1);
        ComponentName componentInfo = runningTaskInfo.get(0).topActivity;
        return componentInfo.getPackageName().equals(getApplicationContext().getOpPackageName());
    }
    @Override
    @SuppressLint("InvalidWakeLockTag")
    protected void onHandleWork(@NonNull Intent intent) {
        String kind = intent.getStringExtra("kind");
        String user = intent.getStringExtra("user");
        String room_id = intent.getStringExtra("room_id");
        String message_id = intent.getStringExtra("message_id");


        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

            if(isForeground()) {
                Intent mIntent = new Intent(this, CallComing.class);
                mIntent.putExtra("kind", kind);
                mIntent.putExtra("user", user);
                mIntent.putExtra("room_id", room_id);
                mIntent.putExtra("message_id", message_id);
                mIntent.setFlags(FLAG_ACTIVITY_NEW_TASK);
                startActivity(mIntent);
            }else{

                Intent mIntent = new Intent(this, HeadsUpNotificationService.class);
                mIntent.putExtra("kind", kind);
                mIntent.putExtra("user", user);
                mIntent.putExtra("room_id", room_id);
                mIntent.putExtra("message_id", message_id);


                startForegroundService(mIntent);
            }



        } else {

            Intent mIntent = new Intent(this, CallComing.class);
            mIntent.putExtra("kind", kind);
            mIntent.putExtra("user", user);
            mIntent.putExtra("room_id", room_id);
            mIntent.putExtra("message_id", message_id);
            mIntent.setFlags(FLAG_ACTIVITY_NEW_TASK);
            startActivity(mIntent);
        }
    }



}
