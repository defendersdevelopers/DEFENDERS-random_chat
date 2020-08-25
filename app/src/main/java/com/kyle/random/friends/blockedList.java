package com.kyle.random.friends;

import androidx.annotation.NonNull;
import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.GridView;
import android.widget.LinearLayout;

import com.kyle.random.other.BaseActivity;
import com.kyle.random.R;
import com.kyle.random.other.Libs;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import static com.kyle.random.other.Libs.getUserId;


public class blockedList extends BaseActivity {
    String myUid;
    ArrayList<String> images;
    StringBuilder interests;

    ArrayList<String> myBlockedList;
    LinearLayout friendsLayout,noFriendsLayout;
    ArrayList<Friend> friendsArrayList;
    ArrayList<String> blockedFriendsUidArrayList;
    friendsAdapter friendsAdapter;
    GridView gridView;
    Libs libs;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.blocked_list);
        myBlockedList=new ArrayList<>();
        friendsArrayList= new ArrayList<>();
        blockedFriendsUidArrayList=new ArrayList<>();
        gridView=findViewById(R.id.grid_view);
        friendsAdapter=new friendsAdapter(this,friendsArrayList,"fromBlockedList");
        noFriendsLayout=findViewById(R.id.noFriendsLayout);
        friendsLayout=findViewById(R.id.friendsLayout);
        gridView.setAdapter(friendsAdapter);

        libs=new Libs(this);
        myUid =getUserId();
        loadMyData();

    }
    public void loadMyData() {
        FirebaseDatabase.getInstance().getReference("users").child(myUid).addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot user) {
                blockedFriendsUidArrayList.clear();
                myBlockedList.clear();
                images=new ArrayList<>();
                for (DataSnapshot image : user.child("images").getChildren()) {
                    images.add(String.valueOf(image.getValue()));
                }
                interests = new StringBuilder();
                for (DataSnapshot interest : user.child("hobbies").getChildren()) {
                    interests.append(" #");
                    interests.append(Libs.getHobby(blockedList.this ,String.valueOf(interest.getValue())));
                }
                for (DataSnapshot friendUid : user.child("blockedList").getChildren()) {
                    blockedFriendsUidArrayList.add(String.valueOf(friendUid.getValue()));
                }
                if (blockedFriendsUidArrayList.isEmpty()){
                    noFriendsLayout.setVisibility(View.VISIBLE);
                    friendsLayout.setVisibility(View.GONE);
                    progressDialog.dismiss();
                }else {
                    noFriendsLayout.setVisibility(View.GONE);
                    friendsLayout.setVisibility(View.VISIBLE);
                    loadFriendsData();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                progressDialog.dismiss();
            }

        });

    }
    public void loadFriendsData(){
        FirebaseDatabase.getInstance().getReference("users").addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot users) {
                friendsArrayList.clear();
                for (DataSnapshot user:users.getChildren()){
                    if (blockedFriendsUidArrayList.contains(user.getKey())){
                        String  age = new Libs(getApplicationContext()).getAge(user);
                        String  brief = String.valueOf(user.child("brief").getValue());
                        String city = String.valueOf(user.child("city").getValue());
                        String  country = String.valueOf(user.child("country").getValue());
                        String  name = String.valueOf(user.child("name").getValue());
                        String profileImage = String.valueOf(user.child("profileImage").getValue());
                        boolean                 heIsVIP= Boolean.parseBoolean(String.valueOf(user.child("VIP").getValue()));
                        friendsArrayList.add(new Friend(user.getKey(),name,age,brief,city,country,profileImage,true,"userName",heIsVIP));
                    }
                }

                friendsAdapter.notifyDataSetChanged();
                progressDialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                progressDialog.dismiss();
            }

        });

    }

}