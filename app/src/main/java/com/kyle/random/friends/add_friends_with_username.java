package com.kyle.random.friends;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;

import com.kyle.random.R;
import com.kyle.random.chat.ChatRoom;
import com.kyle.random.other.BaseActivity;
import com.kyle.random.other.Libs;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;
import es.dmoral.toasty.Toasty;

import static com.kyle.random.other.Libs.getUserId;
public class add_friends_with_username extends BaseActivity {
    EditText editUserName;
    CircleImageView userImage;
    String FriendUid;
    TextView nameTextView, briefTextView, addFriend;
    CardView friendViewer;
    String myUid;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_freinds_with_username);
        myUid = getUserId();
        addFriend = findViewById(R.id.addFriend);
        editUserName = findViewById(R.id.editUserName);
        userImage = findViewById(R.id.userImage);
        nameTextView = findViewById(R.id.userName);
        briefTextView = findViewById(R.id.brief);
        friendViewer = findViewById(R.id.friendViewer);
        TextView username = findViewById(R.id.username);
        String text = getString(R.string.id_201)+" <font color=#29b6f6>" + new Libs(this).getUserData("username") + "</font>";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            username.setText(Html.fromHtml(text, Html.FROM_HTML_MODE_LEGACY), TextView.BufferType.SPANNABLE);
        } else {
            username.setText(Html.fromHtml(text), TextView.BufferType.SPANNABLE);
        }
    }
    public void search(View view) {
        String userName;
        userName = editUserName.getText().toString();
        if (userName.isEmpty()) {
            Toasty.info(this, R.string.id_202, Toast.LENGTH_SHORT, true).show();

            friendViewer.setVisibility(View.GONE);
        } else {
            progressDialog.show();
            FirebaseDatabase.getInstance().getReference("users").orderByChild("userName").equalTo(userName).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot users) {
                    boolean found = false;
                    for (DataSnapshot user : users.getChildren()) {
                        FriendUid = user.getKey();
                        if (FriendUid != null && !FriendUid.equals(myUid)) {
                            found= true;
                            path = FirebaseDatabase.getInstance().getReference("users").child(FriendUid).child("friendRequests").child(getUserId());
                            String profileImage = String.valueOf(user.child("profileImage").getValue());
                            String name = String.valueOf(user.child("name").getValue());
                            String brief = String.valueOf(user.child("brief").getValue());
                            nameTextView.setText(name);
                            briefTextView.setText(brief);
                            Picasso.get().load(profileImage).into(userImage);
                            getAction();
                        }
                    }
                    if (!found){
                        friendViewer.setVisibility(View.GONE);
                    }
                    progressDialog.dismiss();
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    progressDialog.dismiss();
                }
            });
        }
    }
    boolean he_blocked_me = false;
    boolean im_blocked_him = false;
    public void getAction() {
        progressDialog.show();
        path.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull final DataSnapshot isIaddedHim) {
                FirebaseDatabase.getInstance().getReference("users").child(getUserId()).child("friendRequests").child(FriendUid).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull final DataSnapshot isHeAddedMe) {
                        FirebaseDatabase.getInstance().getReference("users").child(getUserId()).child("friends").child(FriendUid).child("addWay").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull final DataSnapshot isWeFriends) {
                                FirebaseDatabase.getInstance().getReference("users").child(getUserId()).child("blockedList").addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot myBlockedList) {
                                        progressDialog.dismiss();
                                        im_blocked_him = false;
                                        for (DataSnapshot friendUid : myBlockedList.getChildren()) {
                                            if (FriendUid.equals(friendUid.getKey())) {
                                                im_blocked_him = true;
                                            }
                                        }
                                        FirebaseDatabase.getInstance().getReference("users").child(FriendUid).child("blockedList").addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot hisBlockedList) {
                                                friendViewer.setVisibility(View.VISIBLE);
                                                he_blocked_me = false;
                                                for (DataSnapshot friendUid : hisBlockedList.getChildren()) {
                                                    if (myUid.equals(friendUid.getKey())) {
                                                        he_blocked_me = true;
                                                    }
                                                }
                                                if (im_blocked_him) {
                                                    addFriend.setText(R.string.id_203);
                                                    addFriend.setOnClickListener(new View.OnClickListener() {
                                                        @Override
                                                        public void onClick(View v) {
                                                            FirebaseDatabase.getInstance().getReference("users").child(getUserId()).child("blockedList").child(FriendUid).removeValue();

                                                            Toasty.success(add_friends_with_username.this, R.string.bsr, Toast.LENGTH_SHORT, true).show();


                                                            getAction();
                                                        }
                                                    });
                                                    return;
                                                }
                                                if (he_blocked_me) {
                                                    addFriend.setText(R.string.id_204);
                                                    addFriend.setEnabled(false);
                                                    return;
                                                }
                                                if (isWeFriends.exists()) {
                                                    addFriend.setText(R.string.id_205);
                                                    addFriend.setOnClickListener(new View.OnClickListener() {
                                                        @Override
                                                        public void onClick(View v) {
                                                            startActivity(new Intent(getApplicationContext(), ChatRoom.class).putExtra("friendUid", FriendUid));
                                                        }
                                                    });
                                                    return;
                                                }
                                                if (isHeAddedMe.exists()) {
                                                    addFriend.setText(R.string.id_206);
                                                    addFriend.setOnClickListener(new View.OnClickListener() {
                                                        @Override
                                                        public void onClick(View v) {
                                                            FirebaseDatabase.getInstance().getReference("users").child(myUid).child("friendRequests").removeValue();
                                                            FirebaseDatabase.getInstance().getReference("users").child(FriendUid).child("friends").child(myUid).setValue(myUid);
                                                            FirebaseDatabase.getInstance().getReference("users").child(myUid).child("friends").child(FriendUid).setValue(FriendUid);
                                                             Toasty.success(add_friends_with_username.this, R.string.id_358, Toast.LENGTH_SHORT, true).show();

                                                            getAction();
                                                        }
                                                    });
                                                    return;
                                                }
                                                if (isIaddedHim.exists()) {
                                                    addFriend.setText(R.string.id_207);
                                                    addFriend.setOnClickListener(new View.OnClickListener() {
                                                        @Override
                                                        public void onClick(View v) {
                                                            path.removeValue();
                                                            Toasty.success(add_friends_with_username.this, R.string.id_359, Toast.LENGTH_SHORT, true).show();
                                                            getAction();
                                                        }
                                                    });
                                                    return;
                                                }
                                                addFriend.setText(R.string.id_208);
                                                addFriend.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {
                                                     //   path.setValue(true);
                                                        HashMap<Object, Object> HashMap = new HashMap<>();
                                                        HashMap.put("uid", myUid);
                                                        HashMap.put("addWay", "userName");
                                                        FirebaseDatabase.getInstance().getReference("users").child(FriendUid).child("friendRequests").child(myUid).setValue(HashMap);
                                                        Toasty.success(add_friends_with_username.this, R.string.id_209, Toast.LENGTH_SHORT, true).show();

                                                        getAction();
                                                    }
                                                });
                                            }
                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                            }
                                        });
                                    }
                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {
                                    }
                                });
                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                            }
                        });
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                    }
                });
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }
    DatabaseReference path;
}
