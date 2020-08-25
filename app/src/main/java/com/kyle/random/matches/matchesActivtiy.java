package com.kyle.random.matches;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.kyle.random.R;
import com.kyle.random.friends.meModel;
import com.kyle.random.gems.gems_store;
import com.kyle.random.main.fragmentViewer;
import com.kyle.random.other.BaseFragment;
import com.kyle.random.other.Country;
import com.kyle.random.other.Libs;
import com.yuyakaido.android.cardstackview.CardStackLayoutManager;
import com.yuyakaido.android.cardstackview.CardStackListener;
import com.yuyakaido.android.cardstackview.CardStackView;
import com.yuyakaido.android.cardstackview.Direction;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.kyle.random.matches.matchesAdapter.Swipe;
import static com.kyle.random.other.Libs.getUserId;
public class matchesActivtiy extends BaseFragment {
    ArrayList<Match> matchesList;
    ArrayList<String> myInterests;
    String myUid;
    // String age, profileImage, city;
    //   String name, brief, country, likes, love;
    ArrayList<String> images;
    StringBuilder interests;
    //    ImageView profileImageViewer, countryFlagViewer, countryFlag2Viewer;
    //   TextView nameViewer, briefViewer, countryViewer, country2Viewer, interestsViewer, likesViewer, loveViewer;
    //  RecyclerView imagesViewer;
    boolean match_plus;
    ArrayList<String> myRejectedList, myFriendsList;
    fragmentViewer context;
    //  ImageView showEditAccountDialog;
    com.kyle.random.friends.meModel meModel;
    //  TextView inter_layout;
    Libs libs;
    RelativeLayout youHaveReachedThaMaxNumberOfCardsLayout;
    String currentDate;
    String my_age, my_brief, my_country, my_name, my_profileImage, my_likes, my_love;
    ArrayList<cardsSeenHistoryItem> cardsSeenHistory;
    // ImageView vipView;
    // ImageView matchPlusGoldenBorder;
//    TextView placeView;
    ArrayList<Matcher> MatchesWithData;
    matchesAdapter matchesAdapter;
    CardStackView cardStackView;
    CardStackLayoutManager cardStackLayoutManager;
    String currentMatcherId = "";
    View view;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.activity_matches, container, false);
        context = (fragmentViewer) matchesActivtiy.this.getActivity();
        //  showEditAccountDialog = view.findViewById(R.id.showEditAccountDialog);
        images = new ArrayList<>();
        MatchesWithData = new ArrayList<>();
        myUid = getUserId();
        matchesList = new ArrayList<>();
        myInterests = new ArrayList<>();
        cardsSeenHistory = new ArrayList();
        myRejectedList = new ArrayList<>();
        myFriendsList = new ArrayList<>();
        libs = new Libs(context);
        my_age = libs.getUserData("age");
        my_brief = libs.getUserData("brief");
        my_country = libs.getUserData("country");
        my_name = libs.getUserData("name");
        my_profileImage = libs.getUserData("profileImage");
        my_likes = libs.getUserData("likes");
        my_love = libs.getUserData("love");
        people_who_like_me_list = new HashMap<>();
        currentDate = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());
        //inter_layout = view.findViewById(R.id.inter_layout);
        // placeView = view.findViewById(R.id.placeView);
        youHaveReachedThaMaxNumberOfCardsLayout = view.findViewById(R.id.youHaveReachedThaMaxNumberOfCardsLayout);
        cardStackView = view.findViewById(R.id.card_stack_view);
        /*
        showEditAccountDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                libs libs = new libs(context);
                if (meModel != null) {
                    libs.dialog_edit_account(meModel);
                }
            }
        });

         */
        view.findViewById(R.id.pay).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getContext(), gems_store.class));
            }
        });
        progressDialog.show();
        libs.loadCountries(new Libs.OnLoadListener() {
            @Override
            public void OnLoad(ArrayList<Country> countriesList) {
                matchesActivtiy.this.countriesList = countriesList;
                loadMyData();
            }
        });
        return view;
    }
    @Override
    public void onStart() {
        super.onStart();
        ((fragmentViewer) matchesActivtiy.this.getActivity()).openMain.setVisibility(View.GONE);
        if (matchesActivtiy.this.countriesList != null) {
            loadMyData();
        }
    }
    int maxNumberOfCardsICanSee;
    ArrayList<Country> countriesList;
    HashMap<String, Boolean> people_who_like_me_list;
    String myGender;
    int TheNumberOfCardsThisUserHaveSeenToday;
    public void loadMyData() {
        match_plus = Boolean.parseBoolean(libs.getUserData("match_plus"));
        FirebaseDatabase.getInstance().getReference("users").child(myUid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot myData) {
                people_who_like_me_list.clear();
                cardsSeenHistory.clear();
                myRejectedList.clear();
                myFriendsList.clear();
                match_plus = (boolean) myData.child("match_plus").getValue();
                for (DataSnapshot userId : myData.child("rejectedList").getChildren()) {
                    String userUID = userId.getValue().toString();
                    myRejectedList.add(userUID);
                }
                for (DataSnapshot userId : myData.child("friends").getChildren()) {
                    String userUID = userId.getKey();
                    myFriendsList.add(userUID);
                }
                for (DataSnapshot userId : myData.child("likes").getChildren()) {
                    String id = userId.getValue().toString();
                    people_who_like_me_list.put(id, false);
                }
                for (DataSnapshot userId : myData.child("love").getChildren()) {
                    String id = userId.getValue().toString();
                    people_who_like_me_list.put(id, true);
                }
                for (DataSnapshot interest : myData.child("hobbies").getChildren()) {
                    String interestKey = Libs.getHobby(context, String.valueOf(interest.getValue()));
                    myInterests.add(interestKey);
                }
                for (DataSnapshot user : myData.child("cardsSeenHistory").getChildren()) {
                    cardsSeenHistory.add(new cardsSeenHistoryItem(user.getKey(), user.getValue().toString()));
                }
                myGender = myData.child("gender").getValue().toString();
                youHaveReachedThaMaxNumberOfCardsLayout.setVisibility(View.GONE);
                meModel = new meModel(my_name, my_age, my_profileImage, my_brief, my_country, myInterests.toString(), my_likes, my_love, images);
                TheNumberOfCardsThisUserHaveSeenToday = 0;
                for (cardsSeenHistoryItem cardsSeenHistoryItem : cardsSeenHistory) {
                    if (currentDate.equals(cardsSeenHistoryItem.day)) {
                        TheNumberOfCardsThisUserHaveSeenToday++;
                    }
                }
                if (!match_plus && TheNumberOfCardsThisUserHaveSeenToday > 4) {
                    youHaveReachedThaMaxNumberOfCardsLayout.setVisibility(View.VISIBLE);
                    cardStackView.setVisibility(View.GONE);
                    dismissDialog();
                } else {
                    maxNumberOfCardsICanSee = 5 - TheNumberOfCardsThisUserHaveSeenToday;
                    findMatches();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                dismissDialog();
            }
        });
    }
    public void dismissDialog() {
        context.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                view.findViewById(R.id.noInterestsYetLayout).setVisibility(View.VISIBLE);
            }
        });
        progressDialog.dismiss();
    }
    public void findMatches() {
        FirebaseDatabase.getInstance().getReference("users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot users) {
                matchesList.clear();
                for (DataSnapshot user : users.getChildren()) {
                    try {
                        String currentUserId = user.getKey();
                        if (currentUserId == null) {
                            continue;
                        }
                        int matchRatio = 0;
                        for (DataSnapshot interest : user.child("hobbies").getChildren()) {
                            String interestValue = Libs.getHobby(context, String.valueOf(interest.getValue()));
                            if (!myInterests.isEmpty()) {
                                if (myInterests.contains(interestValue)) {
                                    matchRatio++;
                                }
                            }
                        }
                        if (people_who_like_me_list.containsKey(currentUserId)) {
                            matchRatio += people_who_like_me_list.get(currentUserId) ? 200 : 100;
                        }
                        boolean rejected = false;
                        boolean heIsMyFriend = false;
                        boolean iLoveHimAlready = false;
                        for (DataSnapshot uid : user.child("likes").getChildren()) {
                            if (getUserId().equals(uid.getValue())) {
                                iLoveHimAlready = true;
                            }
                        }
                        for (DataSnapshot uid : user.child("love").getChildren()) {
                            if (getUserId().equals(uid.getValue())) {
                                iLoveHimAlready = true;
                            }
                        }
                        for (DataSnapshot userId : user.child("rejectedList").getChildren()) {
                            String userUID = userId.getValue().toString();
                            if (myUid.equals(userUID)) {
                                rejected = true;
                            }
                        }
                        for (String rej : myRejectedList) {
                            if (rej.equals(user.getKey())) {
                                rejected = true;
                            }
                        }
                        for (String id : myFriendsList) {
                            if (id.equals(user.getKey())) {
                                heIsMyFriend = true;
                            }
                        }
                        double userLat, userLng;
                        try {
                            userLat = (double) user.child("lat").getValue();
                            userLng = (double) user.child("lng").getValue();
                        } catch (Exception e) {
                            userLat = 0;
                            userLng = 0;
                        }
                        if (currentUserId.equals("lUkKrgP3jmRglQCwMpx2mz6vcLt1")){
                            double myLat = Double.parseDouble(libs.getUserData("lat"));
                            double myLng = Double.parseDouble(libs.getUserData("lng"));
                            double thDistance = distance(userLat, userLng, myLat, myLng);
                            thDistance=thDistance;
                        }
                        double myLat = Double.parseDouble(libs.getUserData("lat"));
                        double myLng = Double.parseDouble(libs.getUserData("lng"));
                        double thDistance = distance(userLat, userLng, myLat, myLng);
                        if (thDistance <= 100) {
                            matchRatio =+1000;
                        }
                        if (thDistance > 100&&thDistance <= 400) {
                            matchRatio =+700;
                        }
                        if (thDistance > 96&&thDistance <= 750) {
                            matchRatio =+400;
                        }
                        if (thDistance > 750&&thDistance <= 1050) {
                            matchRatio =+100;
                        }
                        Match match = new Match(currentUserId, matchRatio, rejected);
                        boolean notMe = !currentUserId.equals(myUid);
                        boolean notMyGender = !String.valueOf(user.child("gender").getValue()).equals(myGender);
                        // boolean Matched = matchRatio != 0; if you want only same hobbies
                        if (notMe && notMyGender && !match.blocked && !heIsMyFriend && !iLoveHimAlready && !rejected) {
                            matchesList.add(match);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    Collections.sort(matchesList, new Comparator<Match>() {
                        @Override
                        public int compare(Match match, Match t1) {
                            return match.matchRatio.compareTo(t1.matchRatio);
                        }
                    });
                }
                if (matchesList.isEmpty()) {
                    cardStackView.setVisibility(View.GONE);
                    youHaveReachedThaMaxNumberOfCardsLayout.setVisibility(View.GONE);
                    dismissDialog();
                } else {
                    matchesList=matchesList;
                    cardStackView.setVisibility(View.VISIBLE);
                    fill_list(users, matchesList);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                dismissDialog();
            }
        });
    }
    private double distance(double lat1, double lon1, double lat2, double lon2) {
        //getting distance in kilometers (km)
        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1))
                * Math.sin(deg2rad(lat2))
                + Math.cos(deg2rad(lat1))
                * Math.cos(deg2rad(lat2))
                * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;
        return (dist);
    }
    private double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }
    private double rad2deg(double rad) {
        return (rad * 180.0 / Math.PI);
    }
    public void fill_list(DataSnapshot users, ArrayList<Match> Matches) {
        MatchesWithData.clear();
        //   String firstMatchUid = Matches.get(0).userId;
        if (match_plus) {
            maxNumberOfCardsICanSee = Matches.size();
        }
        if (maxNumberOfCardsICanSee > Matches.size()) {
            maxNumberOfCardsICanSee = Matches.size();
        }
        for (int i = 0; i < maxNumberOfCardsICanSee; i++) {
            Match match = Matches.get(i);
            for (DataSnapshot user : users.getChildren()) {
                if (match.userId.equals(user.getKey())) {
                    try {
                        String age = new Libs(getContext()).getAge(user);
                        boolean hideAge;
                        try {
                            hideAge = (boolean) user.child("hideAge").getValue();
                        } catch (Exception e) {
                            hideAge = false;
                        }
                        boolean hideLocation = false;
                        try {
                            hideLocation = (boolean) user.child("hideLocation").getValue();
                        } catch (Exception e) {
                            hideLocation = false;
                        }
                        String brief = String.valueOf(user.child("brief").getValue());
                        String city = String.valueOf(user.child("city").getValue());
                        String country = String.valueOf(user.child("country").getValue());
                        String name = String.valueOf(user.child("name").getValue());
                        String profileImage = String.valueOf(user.child("profileImage").getValue());
                        String likes = String.valueOf(user.child("likes").getChildrenCount());
                        String love = String.valueOf(user.child("love").getChildrenCount());
                        boolean heIsUsingMatchPlus = (boolean) user.child("match_plus").getValue();
                        String fakeCountry = String.valueOf(user.child("fakeCountry").getValue());
                        String fakeCity = String.valueOf(user.child("fakeCity").getValue());
                        String fakeCountryCode = String.valueOf(user.child("fakeCountryCode").getValue());
                        String userCountryCode = "";
                        try {
                            userCountryCode = String.valueOf(user.child("CountryCode").getValue());
                        } catch (Exception e) {
                            userCountryCode = "";
                        }
                        boolean heIsVIP = false;
                        try {
                            heIsVIP = (boolean) user.child("VIP").getValue();
                        } catch (Exception e) {
                            heIsVIP = false;
                        }
                        boolean VIPFrame = false;
                        try {
                            VIPFrame = (boolean) user.child("VIPFrame").getValue();
                        } catch (Exception e) {
                            VIPFrame = false;
                        }
                        images.clear();
                        for (DataSnapshot image : user.child("images").getChildren()) {
                            images.add(String.valueOf(image.getValue()));
                        }
                        interests = new StringBuilder();
                        if (user.child("hobbies").getChildrenCount() == 0) {
                            interests.append("");
                        } else {
                            for (DataSnapshot interest : user.child("hobbies").getChildren()) {
                                interests.append(" #");
                                interests.append(Libs.getHobby(context, String.valueOf(interest.getValue())));
                            }
                        }
                        String fakeName = String.valueOf(user.child("fakeName").getValue());
                        MatchesWithData.add(new Matcher(match.userId, countriesList, fakeCountry, fakeCity, brief, country, city, name, profileImage, likes, love
                                , userCountryCode, match.matchRatio, age, match.blocked, hideAge, hideLocation, heIsUsingMatchPlus,
                                heIsVIP, images, String.valueOf(interests), fakeName, VIPFrame, fakeCountryCode));
                    } catch (Exception e) {
                        // user not full registered
                    }
                }
            }
        }
        //set up swipe
        cardStackLayoutManager = new CardStackLayoutManager(context, new CardStackListener() {
            @Override
            public void onCardDragging(Direction direction, float ratio) {
            }
            @Override
            public void onCardSwiped(Direction direction) {
                if (direction.name().toLowerCase().equals("right")) {
                    if (matchesAdapter.superLike) {
                        matchesAdapter.superLike = false;
                        libs.dialog_so_like(currentMatcherId);
                    } else {
                        libs.dialog_like(currentMatcherId);
                    }
                    Swipe(matchesActivtiy.this, currentMatcherId, "", false);
                }
                if (direction.name().toLowerCase().equals("left")) {
                    FirebaseDatabase.getInstance().getReference("users").child(myUid).child("rejectedList").child(currentMatcherId).setValue(currentMatcherId);
                    Swipe(matchesActivtiy.this, currentMatcherId, "", false);
                }
            }
            @Override
            public void onCardRewound() {
            }
            @Override
            public void onCardCanceled() {
            }
            @Override
            public void onCardAppeared(View view, int position) {
                try {
                    if (!matchesList.isEmpty()) {
                        currentMatcherId = matchesList.get(position).userId;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            @Override
            public void onCardDisappeared(View view, int position) {
                if (position + 1 == maxNumberOfCardsICanSee && !match_plus) {
                    youHaveReachedThaMaxNumberOfCardsLayout.setVisibility(View.VISIBLE);
                    cardStackView.setVisibility(View.GONE);
                    dismissDialog();
                }
            }
        });
        cardStackLayoutManager.setCanScrollVertical(false);
        final LinearInterpolator linearInterpolator = new LinearInterpolator();
        Interpolator interpolator = new Interpolator() {
            @Override
            public float getInterpolation(float t) {
                return linearInterpolator.getInterpolation(t) * 4;//can be 3 or 5
            }
        };
        cardStackLayoutManager.setOverlayInterpolator(interpolator);
        cardStackView.setLayoutManager(cardStackLayoutManager);
        MatchesWithData=MatchesWithData;
        Collections.reverse(MatchesWithData);
        matchesAdapter = new matchesAdapter(matchesActivtiy.this.getActivity(), MatchesWithData, matchesActivtiy.this);
        cardStackView.setAdapter(matchesAdapter);
        matchesAdapter.notifyDataSetChanged();
        dismissDialog();
    }
    public static class myCuteAdapter extends RecyclerView.Adapter<myCuteAdapter.ViewHolder> {
        private List<item> images;
        private LayoutInflater mInflater;
        Context context;
        public myCuteAdapter(Context context, ArrayList<item> images) {
            this.mInflater = LayoutInflater.from(context);
            this.images = images;
            this.context = context;
        }
        @Override
        @NonNull
        public myCuteAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = mInflater.inflate(R.layout.item_wrap_circle_image_view, parent, false);
            return new ViewHolder(view);
        }
        @Override
        public void onBindViewHolder(@NonNull myCuteAdapter.ViewHolder holder, int position) {
            final item item = images.get(position);
            holder.image.setImageResource(item.image);
            holder.advantageName.setText(item.advantageName);
            holder.advantageDis.setText(item.advantageDis);
        }
        @Override
        public int getItemCount() {
            return images.size();
        }
        public static class ViewHolder extends RecyclerView.ViewHolder {
            CircleImageView image;
            TextView advantageName, advantageDis;
            ViewHolder(View itemView) {
                super(itemView);
                image = itemView.findViewById(R.id.image);
                advantageName = itemView.findViewById(R.id.advantageName);
                advantageDis = itemView.findViewById(R.id.advantageDis);
            }
        }
    }
    public static class item {
        String advantageName, advantageDis;
        int image;
        public item(String advantageDis, String advantageName, int image) {
            this.advantageName = advantageName;
            this.advantageDis = advantageDis;
            this.image = image;
        }
    }
    boolean iLikeHim = false, iLoveHim = false;
}
