package com.kyle.random.friends;

import androidx.annotation.NonNull;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.GridView;

import com.kyle.random.R;
import com.kyle.random.other.BaseActivity;
import com.kyle.random.other.Libs;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import static com.kyle.random.other.Libs.getUserId;
public class friendRequests extends BaseActivity {
String myUid;
  //  LinearLayout friendsLayout,noFriendsLayout;
    ArrayList<Friend> friendsArrayList;
    ArrayList<uidAndAddWay> friendsUidArrayList;
    friendRequestsAdapter friendRequestsAdapter;
    GridView grid_view;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.friend_requests);
        friendsArrayList= new ArrayList<>();
        myUid = getUserId();
        grid_view= findViewById(R.id.grid_view);
     //   noFriendsLayout= findViewById(R.id.noFriendsLayout);
        friendsUidArrayList=new ArrayList<>();
        friendRequestsAdapter=new friendRequestsAdapter(this,friendsArrayList);
        grid_view.setAdapter(friendRequestsAdapter);
        myBlockedList=new ArrayList<>();
        load();

    }
    ArrayList<String> myBlockedList;

    public void load(){
        FirebaseDatabase.getInstance().getReference("users").addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot users) {

                DataSnapshot me =users.child(myUid);
                friendsUidArrayList.clear();
                friendsArrayList.clear();
                for (DataSnapshot friendUid : me.child("blockedList").getChildren()) {
                    myBlockedList.add(String.valueOf(friendUid.getKey()));
                }
                for (DataSnapshot friend : me.child("friendRequests").getChildren()) {
                    friendsUidArrayList.add(new uidAndAddWay(String.valueOf(friend.child("uid").getValue()),String.valueOf(friend.child("addWay").getValue())));
                }
                if (friendsUidArrayList.isEmpty()){
                    progressDialog.dismiss();
                }else {
                    friendsArrayList.clear();
                    String  age = "",brief = "",city = "",country = "",name = "",profileImage = "",addWay="userName";
                    for (DataSnapshot user:users.getChildren()){
                        for (int i = 0; i <friendsUidArrayList.size() ; i++) {
                            if (friendsUidArrayList.get(i).uid.equals(user.getKey())){
                                age = new Libs(getApplicationContext()).getAge(user);
                                brief = String.valueOf(user.child("brief").getValue());
                                city = String.valueOf(user.child("city").getValue());
                                country = String.valueOf(user.child("country").getValue());
                                name = String.valueOf(user.child("name").getValue());
                                profileImage = String.valueOf(user.child("profileImage").getValue());
                                addWay=friendsUidArrayList.get(i).addWay;
                                boolean  heIsVIP= Boolean.parseBoolean(String.valueOf(user.child("VIP").getValue()));
                                Friend friend=  new Friend(user.getKey(),name,age,brief,city,country,profileImage,myBlockedList.contains(user.getKey()),addWay,heIsVIP);
                                friendsArrayList.add(friend);
                            }
                        }
                    }
                    friendRequestsAdapter.notifyDataSetChanged();
                    progressDialog.dismiss();
                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                progressDialog.dismiss();
            }

        });

    }
    public void back(View view) {
        finish();
    }
}