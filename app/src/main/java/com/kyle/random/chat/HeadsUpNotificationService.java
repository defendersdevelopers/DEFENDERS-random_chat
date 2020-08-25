package com.kyle.random.chat;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.kyle.random.R;

import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

import static android.app.Notification.DEFAULT_SOUND;
import static android.app.Notification.DEFAULT_VIBRATE;
import static androidx.core.app.ServiceCompat.START_STICKY;
public class HeadsUpNotificationService extends Service {
    private Timer timer = new Timer();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    @SuppressLint("InvalidWakeLockTag")
    public int onStartCommand(Intent intent, int flags, int startId) {
        String kind = intent.getStringExtra("kind");
        String user = intent.getStringExtra("user");
        String room_id = intent.getStringExtra("room_id");
        String message_id = intent.getStringExtra("message_id");

        boolean isVideo = kind.equals("video_call");
        try {
            Intent receiveCallAction = new Intent(getApplication(), HeadsUpNotificationActionReceiver.class);
            receiveCallAction.putExtra(CALL_RESPONSE_ACTION_KEY, CALL_RECEIVE_ACTION);

            receiveCallAction.setAction("RECEIVE_CALL");

            receiveCallAction.putExtra("kind", kind);
            receiveCallAction.putExtra("user", user);
            receiveCallAction.putExtra("room_id", room_id);
            receiveCallAction.putExtra("message_id", message_id);
            Intent cancelCallAction = new Intent(getApplication(), HeadsUpNotificationActionReceiver.class);
            cancelCallAction.putExtra(CALL_RESPONSE_ACTION_KEY, CALL_CANCEL_ACTION);

            cancelCallAction.putExtra("kind", kind);
            cancelCallAction.putExtra("user", user);
            cancelCallAction.putExtra("room_id", room_id);
            cancelCallAction.putExtra("message_id", message_id);
            cancelCallAction.setAction("CANCEL_CALL");

            PendingIntent receiveCallPendingIntent = PendingIntent.getBroadcast(getApplication(), 1200, receiveCallAction, PendingIntent.FLAG_UPDATE_CURRENT);
            PendingIntent cancelCallPendingIntent = PendingIntent.getBroadcast(getApplication(), 1201, cancelCallAction, PendingIntent.FLAG_UPDATE_CURRENT);


            createChannel();
            // setFullScreenIntent
            // https://developer.android.com/reference/android/support/v4/app/NotificationCompat.Builder.html#setFullScreenIntent(android.app.PendingIntent,%20boolean)

            Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);

            NotificationCompat.Builder   notificationBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)

                    .setContentText(isVideo ?   getString(R.string.id_615): getString(R.string.id_616))
                    .setSmallIcon(android.R.drawable.sym_call_incoming)
                    .setPriority(NotificationCompat.PRIORITY_MAX)
                    .setCategory(NotificationCompat.CATEGORY_CALL)
                    .addAction(android.R.drawable.sym_action_call, getString(R.string.id_617), receiveCallPendingIntent)
                    .addAction(android.R.drawable.ic_menu_close_clear_cancel, getString(R.string.id_618), cancelCallPendingIntent)
                    .setAutoCancel(true)
                    .setOngoing(true)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .setDefaults(DEFAULT_SOUND | DEFAULT_VIBRATE)
                    .setSound(uri);
            //.setFullScreenIntent(fullScreenPendingIntent, false);

            Notification incomingCallNotification = null;
            if (notificationBuilder != null) {
                incomingCallNotification = notificationBuilder.build();
            }
            PowerManager pm = (PowerManager) getApplicationContext().getSystemService(Context.POWER_SERVICE);
            boolean isScreenOn = false;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
                isScreenOn = pm.isInteractive();
            }

            if (!isScreenOn) {
                PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.ON_AFTER_RELEASE, "MyLock");
                wl.acquire(10000);
            PowerManager.WakeLock wl_cpu = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyCpuLock");

                wl_cpu.acquire(10000);
            }
            startForeground(9999, incomingCallNotification);
            TimerTask task = new TimerTask() {

                @Override
                public void run() {
                    NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
                    notificationManager.cancel(9999);
                    Intent it = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
                    sendBroadcast(it);
                    stopSelf();

                }
            };

            timer.schedule(task, 15000);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return START_STICKY;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        timer.cancel();
    }
    static final String CHANNEL_ID = "VoipChannel";
    static final String CHANNEL_NAME = "Voip Channel";



    static final String CALL_RESPONSE_ACTION_KEY = "CALL_RESPONSE_ACTION_KEY";
    static final String CALL_CANCEL_ACTION = " CALL_CANCEL_ACTION";
    static final String CALL_RECEIVE_ACTION = " CALL_RECEIVE_ACTION";


    /*
      Create noticiation channel if OS version is greater than or eqaul to Oreo
    */
    public void createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);

            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription(CHANNEL_NAME);
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            channel.setSound(uri,
                    new AudioAttributes.Builder().setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .setLegacyStreamType(AudioManager.STREAM_RING)
                            .setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION).build());
            Objects.requireNonNull(getApplication().getSystemService(NotificationManager.class)).createNotificationChannel(channel);
        }
    }
}