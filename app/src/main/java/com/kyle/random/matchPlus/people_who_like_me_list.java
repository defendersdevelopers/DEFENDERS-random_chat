package com.kyle.random.matchPlus;

import androidx.annotation.NonNull;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.GridView;
import android.widget.LinearLayout;

import com.kyle.random.R;
import com.kyle.random.friends.Friend;
import com.kyle.random.friends.friendsAdapter;
import com.kyle.random.other.BaseActivity;
import com.kyle.random.other.Libs;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import static com.kyle.random.other.Libs.getUserId;
public class people_who_like_me_list extends BaseActivity {
    String myUid;
    ArrayList<String> people_who_like_me_list;
    LinearLayout friendsLayout,noFriendsLayout;
    ArrayList<Friend> friendsArrayList;
    ArrayList<String> blockedFriendsUidArrayList;
    friendsAdapter friendsAdapter;
    GridView gridView;
    Libs libs;
    ArrayList<String> myBlockedList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.people_who_like_me_list);
        people_who_like_me_list=new ArrayList<>();
        friendsArrayList= new ArrayList<>();
        blockedFriendsUidArrayList=new ArrayList<>();
        gridView=findViewById(R.id.grid_view);
        friendsAdapter=new friendsAdapter(this,friendsArrayList,"fromLikeMeList");
        noFriendsLayout=findViewById(R.id.noFriendsLayout);
        friendsLayout=findViewById(R.id.friendsLayout);
        gridView.setAdapter(friendsAdapter);
        myBlockedList=new ArrayList<>();
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
                people_who_like_me_list.clear();


                for (DataSnapshot friendUid : user.child("blockedList").getChildren()) {
                    myBlockedList.add(String.valueOf(friendUid.getKey()));
                }
                for (DataSnapshot friendUid : user.child("love").getChildren()) {
                    people_who_like_me_list.add(String.valueOf(friendUid.getValue()));
                }
                for (DataSnapshot friendUid : user.child("likes").getChildren()) {
                    people_who_like_me_list.add(String.valueOf(friendUid.getValue()));
                }
                if (people_who_like_me_list.isEmpty()){
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
                    if (people_who_like_me_list.contains(user.getKey())){
                        String  age = new Libs(getApplicationContext()).getAge(user);
                        String  brief = String.valueOf(user.child("brief").getValue());
                        String city = String.valueOf(user.child("city").getValue());
                        String  country = String.valueOf(user.child("country").getValue());
                        String  name = String.valueOf(user.child("name").getValue());
                        String profileImage = String.valueOf(user.child("profileImage").getValue());
                       boolean heIsVIP= Boolean.parseBoolean(String.valueOf(user.child("VIP").getValue()));
                        friendsArrayList.add(new Friend(user.getKey(),name,age,brief,city,country,profileImage,myBlockedList.contains(user.getKey()),"userName",heIsVIP));
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