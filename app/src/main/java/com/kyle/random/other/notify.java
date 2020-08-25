package com.kyle.random.other;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.kyle.random.R;
import com.kyle.random.chat.CallServiceJob;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Date;

import static com.kyle.random.other.Libs.getUserId;
public class notify extends FirebaseMessagingService {
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        if (getUserId().isEmpty()) {
            return;
        }
        String kind = remoteMessage.getData().get("kind");
        if (kind != null) {
            if (kind.equals("voice_call") || kind.equals("video_call")) {
                Intent mIntent = new Intent(this, CallServiceJob.class);
                mIntent.putExtra("kind", kind);
                mIntent.putExtra("user", remoteMessage.getData().get("user"));
                mIntent.putExtra("room_id", remoteMessage.getData().get("room_id"));
                mIntent.putExtra("message_id", remoteMessage.getData().get("message_id"));
                CallServiceJob.enqueueWork(this, mIntent);
            } else if (kind.equals("friendRequests")) {
                try {
                    String request_from = remoteMessage.getData().get("request_from");
                    String last_friend = getSharedPreferences("data", 0).getString("last_friend", "");
                    if (request_from == null) request_from = "";
                    if (last_friend == null) last_friend = "";
                    if (last_friend.equals(request_from)) {
                        sendBroadcast(new Intent("friendRequest").putExtra("request_from", request_from));
                    } else {
                        notifyNow(remoteMessage, "friendRequest");
                    }
                } catch (Exception e) {
                }
            } else if (kind.equals("InCallLikes")) {
                try {
                    String like_from = remoteMessage.getData().get("like_from");
                    String last_friend = getSharedPreferences("data", 0).getString("last_friend", "");
                    if (like_from == null) like_from = "";
                    if (last_friend == null) last_friend = "";
                    if (last_friend.equals(like_from)) {
                        sendBroadcast(new Intent("InCallLikes").putExtra("like_from", like_from));
                    } else {
                        notifyNow(remoteMessage);
                    }
                } catch (Exception e) {
                }
            } else if (kind.equals("RequestAccepted")) {
                try {
                    String new_friend = remoteMessage.getData().get("new_friend");
                    String last_friend = getSharedPreferences("data", 0).getString("last_friend", "");
                    if (new_friend == null) new_friend = "";
                    if (last_friend == null) last_friend = "";
                    if (last_friend.equals(new_friend)) {
                        sendBroadcast(new Intent("RequestAccepted").putExtra("new_friend", new_friend));
                    } else {
                        notifyNow(remoteMessage);
                    }
                } catch (Exception e) {
                }
            } else if (kind.equals("image") || kind.equals("text")|| kind.equals("voice")) {
               String  user =  remoteMessage.getData().get("user");
                notifyNow(remoteMessage , "chat~" + user);

            } else {
                notifyNow(remoteMessage);
            }
        } else {
            notifyNow(remoteMessage);
        }
    }

    public void notifyNow(RemoteMessage remoteMessage) {

        notifyNow(remoteMessage, "");
    }
    public void notifyNow(RemoteMessage remoteMessage, String activity) {
        try {
            final RemoteMessage.Notification notification = remoteMessage.getNotification();
            if (notification != null) {
                sendBroadcast(new Intent("InAppNotify")
                        .putExtra("title", notification.getTitle())
                        .putExtra("body", notification.getBody())
                        .putExtra("activity", activity)
                );
                //  sendNotification(notification.getTitle(), notification.getBody(), intent);
            }
        } catch (Exception e) {
        }
    }
    @Override
    public void onNewToken(@NonNull String s) {
        if (!getUserId().isEmpty()) {
            FirebaseDatabase.getInstance().getReference("users").child(getUserId()).child("tokens").child(s).setValue(s);
        }
        super.onNewToken(s);
    }
}
