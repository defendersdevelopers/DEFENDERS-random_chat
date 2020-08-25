package com.kyle.random.chat;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.google.firebase.database.FirebaseDatabase;
import com.kyle.random.other.Libs;
public class HeadsUpNotificationActionReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null && intent.getExtras() != null) {
            String action = intent.getAction();
            String room_id = intent.getStringExtra("room_id");
            String friend =intent.getStringExtra("user");
            String message_id = intent.getStringExtra("message_id");
            String kind = intent.getStringExtra("kind");


            boolean isVideo = kind.equals("video_call");
            if (action.equals("RECEIVE_CALL")) {



                Intent mIntent = new Intent(context, InCall.class);
                mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mIntent.putExtra("video_room_id", room_id);
                mIntent.putExtra("friendUid", friend);
                mIntent.putExtra("message_id", message_id);
                mIntent.putExtra("isIamTheCreator", false);
                mIntent.putExtra("isVideo",isVideo);
                mIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context. startActivity(mIntent);
                FirebaseDatabase.getInstance().getReference("users").child(Libs.getUserId()).child("friends").child(friend).child("chat").child(message_id).child("statue").setValue("accepted");
                FirebaseDatabase.getInstance().getReference("users").child(friend).child("friends").child(Libs.getUserId()).child("chat").child(message_id).child("statue").setValue("accepted");



            } else if (action.equals("CANCEL_CALL")) {

                FirebaseDatabase.getInstance().getReference("users").child(Libs.getUserId()).child("friends").child(friend).child("chat").child(message_id).child("statue").setValue("rejected");
                FirebaseDatabase.getInstance().getReference("users").child(friend).child("friends").child(Libs.getUserId()).child("chat").child(message_id).child("statue").setValue("rejected");


            }

            Intent it = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
            context.sendBroadcast(it);
            context.stopService(new Intent(context, HeadsUpNotificationService.class));

        }
    }

}