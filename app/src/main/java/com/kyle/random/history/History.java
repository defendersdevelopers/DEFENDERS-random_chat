package com.kyle.random.history;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.kyle.random.R;
import com.kyle.random.friends.Friend;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.kyle.random.friends.meModel;
import com.kyle.random.main.fragmentViewer;
import com.kyle.random.other.BaseFragment;
import com.kyle.random.other.Country;
import com.kyle.random.other.helpers.OnFriendChangeListener;
import com.kyle.random.other.Libs;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.kyle.random.other.Libs.getUserId;
public class History extends BaseFragment {
    ImageView more;
    String myUid;
    LinearLayout noFriendsLayout;
    Libs libs;
    CircleImageView profileImageViewer;
    RecyclerView history_small, history_big;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_history, container, false);
        myUid = getUserId();
        libs = new Libs(getActivity());
        noFriendsLayout = view.findViewById(R.id.noFriendsLayout);
        profileImageViewer = view.findViewById(R.id.profileImage);
        profileImageViewer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseDatabase.getInstance().getReference("users").child(myUid).addListenerForSingleValueEvent(new ValueEventListener() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onDataChange(@NonNull DataSnapshot user) {
                        String love = String.valueOf(user.child("love").getChildrenCount());
                        String likes = String.valueOf(user.child("likes").getChildrenCount());
                        StringBuilder interests = new StringBuilder();
                        for (DataSnapshot interest : user.child("hobbies").getChildren()) {
                            interests.append(" #");
                            interests.append(Libs.getHobby(getActivity() ,String.valueOf(interest.getValue())));
                        }
                      ArrayList<String>   images=new ArrayList<>();
                        for (DataSnapshot image : user.child("images").getChildren()) {
                            images.add(String.valueOf(image.getValue()));
                        }
                        meModel meModel = new meModel(libs.getUserData("name"), libs.getUserData("age"), libs.getUserData("profileImage"), libs.getUserData("brief"), libs.getUserData("country"), interests.toString(), likes, love,images);
                        libs.dialog_edit_account(getActivity(),meModel);
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                    }
                });
            }
        });
        more = view.findViewById(R.id.more);
        more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), com.kyle.random.other.more.class));
            }
        });
        //more.setOnClickListener(v -> startActivity(new Intent(getActivity(), com.kyle.random.other.more.class)));
        history_small = view.findViewById(R.id.history_small);
        history_big = view.findViewById(R.id.history_big);
        history_big.setHasFixedSize(true);
        history_small.setHasFixedSize(true);
        history_big.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
        history_small.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
        String profileImage = libs.getUserData("profileImage");
        Picasso.get().load(profileImage)/*.fit()*/.into(profileImageViewer);
        fragmentViewer = (fragmentViewer) getActivity();



        return view;
    }
    @Override
    public void onStart() {
        super.onStart();
        libs.loadCountries(new Libs.OnLoadListener() {
            @Override
            public void OnLoad(ArrayList<Country> countriesList) {
                load(countriesList);
            }
        });

    }
    fragmentViewer fragmentViewer;
    OnFriendChangeListener onFriendChangeListener = new OnFriendChangeListener() {
        @Override
        public void onFriendChange(String uid, final int Position) {
            history_big.post(new Runnable() {
                @Override
                public void run() {
                    history_big.smoothScrollToPosition(Position);
                }
            });
        }
    };
    public void load(final ArrayList<Country> countriesList) {
        FirebaseDatabase.getInstance().getReference("users").addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot users) {
                if (fragmentViewer.oldFragment != fragmentViewer.history) {
                    return;
                }
                DataSnapshot me = users.child(myUid);
                ArrayList<String> myBlockedList = new ArrayList<>();
                for (DataSnapshot friendUid : me.child("blockedList").getChildren()) {
                    myBlockedList.add(String.valueOf(friendUid.getKey()));
                }
                ArrayList<DataAndUid> history_list = new ArrayList<>();
                for (DataSnapshot friend : me.child("history").getChildren()) {
                    history_list.add(new DataAndUid(friend.getKey(), String.valueOf(friend.getValue())));
                }
                Collections.sort(history_list, new Comparator<DataAndUid>() {
                    @Override
                    public int compare(DataAndUid t2, DataAndUid t1) {
                        return t1 .date.compareTo(t2.date);
                    }
                });


                ArrayList<Friend> historyArrayList = new ArrayList<>();
                for (int i = 0; i < history_list.size(); i++) {
                    DataAndUid dataAndUid = history_list.get(i);
                    DataSnapshot user = users.child(dataAndUid.uid);
                    String age = new Libs(fragmentViewer).getAge(user);
                    String brief = String.valueOf(user.child("brief").getValue());
                    String city = String.valueOf(user.child("city").getValue());
                    String country = String.valueOf(user.child("country").getValue());
                    String name = String.valueOf(user.child("name").getValue());
                    String profileImage = String.valueOf(user.child("profileImage").getValue());
                    boolean heIsVIP = Boolean.parseBoolean(String.valueOf(user.child("VIP").getValue()));
                    String userCountryCode;
                    try {
                        userCountryCode = String.valueOf(user.child("CountryCode").getValue());
                    } catch (Exception e) {
                        userCountryCode = "";
                    }
                    Friend friend = new Friend(user.getKey(), name, age, brief, city, country, profileImage, myBlockedList.contains(user.getKey()), "userName", heIsVIP);
                    friend.randomChatDate = dataAndUid.date;
                    friend.userCountryCode = userCountryCode;
                    if (historyArrayList.isEmpty()) {
                        friend.applied = true;
                    }
                    if (!friend.blocked) {
                        historyArrayList.add(friend);
                    }
                }
                if (historyArrayList.isEmpty()) {
                    noFriendsLayout.setVisibility(View.VISIBLE);
                } else {
                    noFriendsLayout.setVisibility(View.GONE);
                }
                HistorySmallAdapter historySmallAdapter = new HistorySmallAdapter(getActivity(), historyArrayList, onFriendChangeListener);
                HistoryBigAdapter historyBigAdapter = new HistoryBigAdapter(getActivity(), historyArrayList, countriesList);
                history_big.setAdapter(historyBigAdapter);
                history_small.setAdapter(historySmallAdapter);
                fragmentViewer.openMain.setVisibility(historyArrayList.isEmpty() ? View.VISIBLE : View.GONE);
                progressDialog.dismiss();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                progressDialog.dismiss();
            }
        });
    }
}
