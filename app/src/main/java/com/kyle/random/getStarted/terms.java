package com.kyle.random.getStarted;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.kyle.random.R;
import com.kyle.random.other.BaseActivity;
import com.kyle.random.other.about_us;
public class terms extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.terms);
        loadPrivacy();
    }

    public void Ok(View view) {
        finish();
    }
    public void loadPrivacy() {
        progressDialog.show();


        FirebaseDatabase.getInstance().getReference("data").child("terms").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot D_privacy) {
              String  S_privacy = String.valueOf(D_privacy.getValue());
                ((TextView)findViewById(R.id.terms)).setText(S_privacy.replace("\\n" , "\n"));
                progressDialog.dismiss();
            }


            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }

        });
    }
}
