package com.kyle.random.chat;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.kyle.random.R;
import com.kyle.random.friends.Friend;
import com.kyle.random.friends.add_friends;
import com.kyle.random.friends.allMyFriendsAdapter;
import com.kyle.random.friends.friendRequests;
import com.kyle.random.friends.meModel;
import com.kyle.random.main.fragmentViewer;
import com.kyle.random.other.BaseFragment;
import com.kyle.random.other.Country;
import com.kyle.random.other.Libs;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Objects;

import static com.kyle.random.other.Libs.getUserId;
public class ChatFriends extends BaseFragment {
    String myUid;
    ArrayList<FriendChatItem> messagesArrayList;
    LinearLayout friendsLayout, noFriendsLayout;
    ChatFriendsAdapter messagesAdapter;
    ImageView addFriends, more;
    Activity context;
    GridView gridView;
    String flagImage;
    ArrayList<Country> countriesList;
    /////////////////////////////////
    ArrayList<String> myInterests;
    String age, profileImage, city;
    String name, brief, country, likes, love;
    ArrayList<String> images;
    StringBuilder interests;
    ImageView profileImageViewer, countryFlagViewer;
    TextView nameViewer, countryViewer, noChatMessage, friends_request_counter;
    ArrayList<String> myBlockedList;
    //friends
    LinearLayout chatLayout, noChatLayout;
    ArrayList<Friend> allMyFriendsArrayList;
    ArrayList<item> friendsUidArrayList;
    allMyFriendsAdapter friendsAdapter;
    RecyclerView friendsRecyclerView;
    Switch others_messages;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.chat_friends, container, false);
        myUid = getUserId();
        messagesArrayList = new ArrayList<>();
        gridView = view.findViewById(R.id.grid_view);
        chatLayout = view.findViewById(R.id.chatLayout);
        noChatLayout = view.findViewById(R.id.noChatLayout);
        friendsLayout = view.findViewById(R.id.friendsLayout);
        noFriendsLayout = view.findViewById(R.id.noFriendsLayout);
        others_messages = view.findViewById(R.id.others_messages);
        noChatMessage = view.findViewById(R.id.noChatMessage);
        others_messages.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                messagesAdapter.load(b);
            }
        });
        context = ChatFriends.this.getActivity();
        messagesAdapter = new ChatFriendsAdapter(this);
        addFriends = view.findViewById(R.id.addFriends);
        addFriends.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ChatFriends.this.getActivity(), add_friends.class));
            }
        });
        more = view.findViewById(R.id.more);
        more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ChatFriends.this.getActivity(), com.kyle.random.other.more.class));
            }
        });
        gridView.setAdapter(messagesAdapter);
        libs = new Libs(ChatFriends.this.getContext());
        libs.loadCountries(new Libs.OnLoadListener() {
            @Override
            public void OnLoad(ArrayList<Country> myCountriesList) {
                countriesList = myCountriesList;
                load();
            }
        });
        myBlockedList = new ArrayList<>();
        profileImageViewer = view.findViewById(R.id.profileImage);
        countryFlagViewer = view.findViewById(R.id.countryFlag);
        nameViewer = view.findViewById(R.id.name);
        countryViewer = view.findViewById(R.id.country);
        friends_request_counter = view.findViewById(R.id.friends_request_counter);
        addFriends = view.findViewById(R.id.addFriends);
        friendsRecyclerView = view.findViewById(R.id.friendsRecyclerView);
        myInterests = new ArrayList<>();
        friendsUidArrayList = new ArrayList<>();
        allMyFriendsArrayList = new ArrayList<>();
        friendsAdapter = new allMyFriendsAdapter(context, allMyFriendsArrayList, "fromFriendsList");
        LinearLayoutManager layoutManager
                = new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false);
        friendsRecyclerView.setLayoutManager(layoutManager);
        friendsRecyclerView.setAdapter(friendsAdapter);
        view.findViewById(R.id.editMyProfile).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Libs libs = new Libs(context);
                meModel meModel = new meModel(name, age, profileImage, brief, country, interests.toString(), likes, love, images);
                if (meModel != null) {
                    libs.dialog_edit_account(getActivity(), meModel);
                }
            }
        });
        view.findViewById(R.id.friendRequests).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), friendRequests.class));
            }
        });
        return view;
    }
    Libs libs;
    public void load() {
        final Libs libs = new Libs(getContext());
        myUid = getUserId();
        age = libs.getUserData("age");
        brief = libs.getUserData("brief");
        city = libs.getUserData("city");
        country = libs.getUserData("country");
        name = libs.getUserData("name");
        profileImage = libs.getUserData("profileImage");
        String userCountryCode = libs.getUserData("userCountryCode");
        Picasso.get().load(libs.getCountryFlagWithCountryCode(countriesList, userCountryCode)).into(countryFlagViewer);
        FirebaseDatabase.getInstance().getReference("users").addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot users) {
                try {
                    messagesArrayList.clear();
                    //# load my data
                    DataSnapshot me = users.child(myUid);
                    if (!me.exists() || !new Libs(context).isLoggedDone()) {
                        return;
                    }
                    myBlockedList.clear();
                    friendsUidArrayList.clear();
                    if (me.child("likes").exists()) {
                        likes = String.valueOf(me.child("likes").getChildrenCount());
                    } else {
                        likes = "0";
                    }
                    long friendRequests = me.child("friendRequests").getChildrenCount();
                    friends_request_counter.setText("+" + friendRequests);
                    friends_request_counter.setVisibility(friendRequests != 0 ? View.VISIBLE : View.INVISIBLE);
                    love = String.valueOf(me.child("love").getChildrenCount());
                    images = new ArrayList<>();
                    for (DataSnapshot image : me.child("images").getChildren()) {
                        images.add(String.valueOf(image.getValue()));
                    }
                    interests = new StringBuilder();
                    for (DataSnapshot interest : me.child("hobbies").getChildren()) {
                        interests.append(" #");
                        interests.append(Libs.getHobby(context, String.valueOf(interest.getValue())));
                    }
                    for (DataSnapshot friendUid : me.child("blockedList").getChildren()) {
                        myBlockedList.add(String.valueOf(friendUid.getKey()));
                    }
                    nameViewer.setText(name + "/" + age);
                    Picasso.get().load(profileImage).into(profileImageViewer);
                    countryViewer.setText(country);
                    //friends
                    for (DataSnapshot friendUid : me.child("friends").getChildren()) {
                        if (friendUid.hasChild("addWay")) {
                            friendsUidArrayList.add(new item(String.valueOf(friendUid.getKey()), String.valueOf(friendUid.child("addWay").getValue())));
                        }
                    }
                    fragmentViewer fragmentViewer = ((fragmentViewer) Objects.requireNonNull(getActivity()));
                    if (fragmentViewer.oldFragment == fragmentViewer.chat) {
                        fragmentViewer.openMain.setVisibility(friendsUidArrayList.isEmpty() ? View.VISIBLE : View.GONE);
                    }
                    allMyFriendsArrayList.clear();
                    if (!friendsUidArrayList.isEmpty()) {
                        for (DataSnapshot user : users.getChildren()) {
                            ArrayList<String> hisBlockedList = new ArrayList<>();
                            for (DataSnapshot friendUid : user.child("blockedList").getChildren()) {
                                hisBlockedList.add(String.valueOf(friendUid.getKey()));
                            }
                            for (int i = 0; i < friendsUidArrayList.size(); i++) {
                                item fr = friendsUidArrayList.get(i);
                                if (fr.uid.equals(user.getKey())) {
                                    if (!myBlockedList.contains(user.getKey()) && !hisBlockedList.contains(getUserId())) {
                                        String age = new Libs(getContext()).getAge(user);
                                        String brief = String.valueOf(user.child("brief").getValue());
                                        String city = String.valueOf(user.child("city").getValue());
                                        String country = String.valueOf(user.child("country").getValue());
                                        String name = String.valueOf(user.child("name").getValue());
                                        String profileImage = String.valueOf(user.child("profileImage").getValue());
                                        boolean heIsVIP = Boolean.parseBoolean(String.valueOf(user.child("VIP").getValue()));
                                        Friend friend = new Friend(user.getKey(), name, age, brief, city, country, profileImage, false, fr.addWay, heIsVIP);
                                        allMyFriendsArrayList.add(friend);
                                    }
                                }
                            }
                        }
                    }
                    if (allMyFriendsArrayList.isEmpty()) {
                        noFriendsLayout.setVisibility(View.VISIBLE);
                        friendsLayout.setVisibility(View.GONE);
                    } else {
                        noFriendsLayout.setVisibility(View.GONE);
                        friendsLayout.setVisibility(View.VISIBLE);
                    }
                    friendsAdapter.notifyDataSetChanged();
                    progressDialog.dismiss();

                    for (DataSnapshot friendSnap : users.child(myUid).child("friends").getChildren()) {
                        boolean iTalkedToHimBefore = false;
                        boolean chatBefore = friendSnap.child("chat").exists();
                        if (chatBefore) {
                            for (DataSnapshot message : friendSnap.child("chat").getChildren()) {
                                try {
                                    if (message.child("who").getValue().equals("me")) {
                                        iTalkedToHimBefore = true;
                                    }
                                } catch (Exception e) {
                                    message.getRef().removeValue();
                                }
                            }
                            Long lastMessageDate = null;
                            Boolean seen = null;
                            String lastMessage = null;
                            for (DataSnapshot message : friendSnap.child("chat").getChildren()) {
                                if (message != null && message.exists() && message.hasChildren()) {
                                    lastMessageDate = Long.parseLong(String.valueOf(message.child("date").getValue()));
                                    boolean iamTheSender = String.valueOf(message.child("who").getValue()).equals("me");
                                    String kind = String.valueOf(message.child("kind").getValue());
                                    String content = String.valueOf(message.child("content").getValue());
                                    if (iamTheSender) {
                                        seen = true;
                                    } else {
                                        try {
                                            seen = (boolean) message.child("seen").getValue();
                                        } catch (Exception e) {
                                            seen = true;
                                        }
                                    }
                                    switch (kind) {
                                        case "text":
                                            lastMessage = String.valueOf(iamTheSender ? getString(R.string.you) + content : content);
                                            break;
                                        case "video_call":
                                            lastMessage = getString(R.string.id_179);
                                            break;
                                        case "voice_call":
                                            lastMessage = getString(R.string.id_180);
                                            break;
                                        case "image":
                                            lastMessage = (iamTheSender ? getString(R.string.id_181) : getString(R.string.id_182));
                                            break;
                                        case "voice":
                                            lastMessage = (iamTheSender ? getString(R.string.id_183) : getString(R.string.id_184));
                                            break;
                                        default:
                                            lastMessage = "";
                                    }
                                }
                            }
                            try {
                                String friendKey = String.valueOf(friendSnap.getKey());
                                boolean isBlocked = users.child(myUid).child("blockedList").hasChild(friendKey);
                                String colorHex = friendSnap.hasChild("colorHex") ? String.valueOf(friendSnap.child("colorHex").getValue()) : String.format("#%06X", (0xFFFFFF & ContextCompat.getColor(context, R.color.colorPrimary)));
                                boolean others = !iTalkedToHimBefore && !users.child(myUid).child("friends").child(friendKey).hasChild("addWay");
                                DataSnapshot user = users.child(friendKey);
                                String name = String.valueOf(user.child("name").getValue());
                                boolean heIsVIP = Boolean.parseBoolean(String.valueOf(user.child("VIP").getValue()));
                                boolean isISentFriendRequest = user.child("friendRequests").child(myUid).exists();
                                boolean isHeSentFriendRequest = me.child("friendRequests").child(user.getKey()).exists();
                                String profileImage = String.valueOf(user.child("profileImage").getValue());
                                String userCountryCode;
                                try {
                                    userCountryCode = String.valueOf(user.child("CountryCode").getValue());
                                } catch (Exception e) {
                                    userCountryCode = "";
                                }
                                String addWay;
                                try {
                                    addWay = String.valueOf(user.child("friends").child(myUid).child("addWay").getValue());
                                } catch (Exception e) {
                                    addWay = "weAreNotFriends";
                                }
                                if (addWay.equals("null")) {
                                    addWay = "weAreNotFriends";
                                }
                                flagImage = libs.getCountryFlagWithCountryCode(countriesList, userCountryCode);
                                if (lastMessageDate != null && seen != null && lastMessage != null) {
                                    messagesArrayList.add(new FriendChatItem(friendKey, name, addWay, profileImage, lastMessage, lastMessageDate, seen, heIsVIP, flagImage, isBlocked, colorHex, others, isISentFriendRequest, isHeSentFriendRequest));
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    int othersCount = 0;
                    for (FriendChatItem item : messagesArrayList) {
                        if (item.others) {
                            othersCount++;
                        }
                    }
                    others_messages.setText(getString(R.string.id_637) + " (" + othersCount + ")");


                    if (messagesArrayList.isEmpty()) {
                        noChatMessage.setText(getString(R.string.id_59));
                        noChatLayout.setVisibility(View.VISIBLE);
                        chatLayout.setVisibility(View.GONE);
                        progressDialog.dismiss();
                    } else {
                        Collections.sort(messagesArrayList, new Comparator<FriendChatItem>() {
                            @Override
                            public int compare(FriendChatItem item1, FriendChatItem item2) {
                                return item2.lastMessageDate.compareTo(item1.lastMessageDate);
                            }
                        });
                        messagesAdapter.notifyDataSetChanged();
                        messagesAdapter.setTempUsers(messagesArrayList);
                        messagesAdapter.load(others_messages.isChecked());
                        // get others count;
                    }

                    progressDialog.dismiss();
                } catch (Exception e) {
                    e.printStackTrace();
                    // user deleted account
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                progressDialog.dismiss();
            }
        });
    }
}
