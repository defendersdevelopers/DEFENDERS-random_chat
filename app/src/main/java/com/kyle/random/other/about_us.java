package com.kyle.random.other;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.kyle.random.R;
import com.kyle.random.getStarted.privacy;
import com.kyle.random.getStarted.terms;
import com.kyle.random.main.fragmentViewer;

import java.util.HashMap;
import java.util.Objects;

import es.dmoral.toasty.Toasty;

import static com.kyle.random.other.Libs.getUserId;
public class about_us extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_us);
              FirebaseDatabase.getInstance().getReference("data").child("about_us").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot D_privacy) {
                String S_privacy = String.valueOf(D_privacy.getValue());
                ((TextView) findViewById(R.id.about_us)).setText(S_privacy.replace("\\n", "\n"));
                progressDialog.dismiss();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }
    public void back(View view) {
        finish();
    }
    public void terms(View view) {
        startActivity(new Intent(this, terms.class));
    }
    public void privacy(View view) {
        startActivity(new Intent(this, privacy.class));
    }
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void deleteAccount(View view) {
        LayoutInflater factory = LayoutInflater.from(about_us.this);
        final View dialogView = factory.inflate(R.layout.alert_delete_account, null);
        final AlertDialog dialog = new AlertDialog.Builder(about_us.this).create();
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawableResource(android.R.color.transparent);
        TextView ok = dialogView.findViewById(R.id.ok);
        TextView cancel = dialogView.findViewById(R.id.cancel);
        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showProgress();
                final String userId = getUserId();
                FirebaseDatabase.getInstance().getReference("users").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot users) {

                        dismissProgress();
                        for (DataSnapshot user : users.getChildren()) {
                            HashMap<String,Object> removeMe = new HashMap<>();
                            removeMe.put("friends" , null);
                            removeMe.put("likes" , null);
                            removeMe.put("love" , null);
                            removeMe.put("cardsSeenHistory" , null);
                            removeMe.put("blockedList" , null);
                            removeMe.put("rejectedList" , null);


                            user.getRef().updateChildren(removeMe);


                        }
                        FirebaseDatabase.getInstance().getReference("reports").child(userId).removeValue();


                        FirebaseDatabase.getInstance().getReference("users").child(getUserId()).removeValue();
                        Toasty.success(about_us.this, R.string.id_635, Toast.LENGTH_SHORT, true).show();
                        logout();
                        dialog.cancel();
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });


                dialog.dismiss();
            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {




                dialog.dismiss();
            }
        });
        dialog.setView(dialogView);
        dialog.show();



    }
}