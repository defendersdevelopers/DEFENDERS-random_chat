package com.kyle.random.getStarted;

import androidx.annotation.NonNull;
import com.kyle.random.other.BaseActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.kyle.random.R;
import com.kyle.random.other.Libs;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import es.dmoral.toasty.Toasty;
public class enter_invite_code extends BaseActivity {
EditText codeEditText;
    Libs libs;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enter_invite_code);
        codeEditText=findViewById(R.id.codeEditText);
        libs=new Libs(enter_invite_code.this);
    }
    boolean found=false;
    String InvitorId;
    public void checkCode(View view) {

        String code=codeEditText.getText().toString();
        if (code.isEmpty()){
            Toasty.info(this, R.string.id_241, Toast.LENGTH_SHORT, true).show();

        }else {
            FirebaseDatabase.getInstance().getReference("users").orderByChild("myInviteCode").equalTo(code).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot users) {
                    if (users.hasChildren()){
                        for (DataSnapshot user:users.getChildren()){

                            found=true;
                            InvitorId=user.getKey();

                        }
                    }

                    if (!found){
                        Toasty.info(enter_invite_code.this, R.string.id_242, Toast.LENGTH_SHORT, true).show();
       }else {
                        libs.addGemsToUser(15,InvitorId);
                        startActivity(new Intent(enter_invite_code.this,hobbies.class));
                    }


                }


                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }

            });

        }


    }

    public void skip(View view) {
        startActivity(new Intent(enter_invite_code.this,hobbies.class));
    }
}