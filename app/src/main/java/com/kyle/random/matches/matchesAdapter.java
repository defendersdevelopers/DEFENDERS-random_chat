package com.kyle.random.matches;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.kyle.random.R;
import com.kyle.random.chat.ChatRoom;
import com.kyle.random.other.Libs;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;
import com.yuyakaido.android.cardstackview.Direction;
import com.yuyakaido.android.cardstackview.Duration;
import com.yuyakaido.android.cardstackview.SwipeAnimationSetting;

import android.view.animation.AccelerateInterpolator;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

import es.dmoral.toasty.Toasty;

import static com.kyle.random.other.Libs.getUserId;
public class matchesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    Activity context;
    ArrayList<Matcher> Matchers;
    Libs libs;

    String myUid;
    matchesActivtiy matchesActivtiy;
    public matchesAdapter(Activity context, ArrayList<Matcher> Matchers, matchesActivtiy matchesActivtiy) {
        this.context = context;
        this.Matchers = Matchers;
        libs = new Libs(context);
        myUid = getUserId();
        this.matchesActivtiy = matchesActivtiy;
    }
    private ViewGroup mParent;
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        mParent = parent;
        return new RecyclerView.ViewHolder(context.getLayoutInflater().inflate(R.layout.item_matcher, parent, false)) {
            @Override
            public String toString() {
                return super.toString();
            }
        };
    }
   public boolean superLike;
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        final View view = holder.itemView;
        final Matcher matcher = Matchers.get(position);
        final View card = view.findViewById(R.id.card);
        final View container = view.findViewById(R.id.container);
        container.post(new Runnable() {

            @Override
            public void run() {

                int height = container.getHeight();


                card.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,height));
            }

        });





        ImageView profileImageViewer, countryFlagViewer, countryFlag2Viewer;
        TextView nameViewer, briefViewer, countryViewer, country2Viewer, interestsViewer, likesViewer, loveViewer;
        RecyclerView imagesViewer;
        ImageView vipView;
        ImageView matchPlusGoldenBorder;
        View like, so_like;
        View reject = view.findViewById(R.id.reject);
        View chat = view.findViewById(R.id.chat);
        // Bounce RubberBand
        YoYo.with(Techniques.Bounce)
                .duration(1600)
                .repeat(3000)
                .playOn(view.findViewById(R.id.star));
        ///Chat Animation
        new CountDownTimer(700000, 4700) {
            @Override
            public void onTick(long millisUntilFinished) {
                //this will be done every 1000 milliseconds ( 1 seconds )
             //   int progress = (int) (7000 - millisUntilFinished) / 1000);
                YoYo.with(Techniques.RotateInUpLeft)
                        .duration(1200)
                        .playOn(view.findViewById(R.id.message_azar));
                new Handler(context.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        YoYo.with(Techniques.Shake)
                                .duration(2000)
                                .playOn(view.findViewById(R.id.message_azar));
                        new Handler(context.getMainLooper()).postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                YoYo.with(Techniques.SlideOutUp)
                                        .duration(1000)
                                        .playOn(view.findViewById(R.id.message_azar));
                            }
                        }, 2100);
                    }
                }, 1200);
            }
            @Override
            public void onFinish() {
            }
        }.start();



        /////////////////////////////////////////////
        matchPlusGoldenBorder = view.findViewById(R.id.matchPlusGoldenBorder);
        vipView = view.findViewById(R.id.vipView);
        TextView inter_layout = view.findViewById(R.id.inter_layout);
        profileImageViewer = view.findViewById(R.id.profileImage);
        countryFlagViewer = view.findViewById(R.id.countryFlag);
        countryFlag2Viewer = view.findViewById(R.id.countryFlag2);
        nameViewer = view.findViewById(R.id.name);
        briefViewer = view.findViewById(R.id.bio);
        countryViewer = view.findViewById(R.id.country);
        country2Viewer = view.findViewById(R.id.country2);
        interestsViewer = view.findViewById(R.id.interests);
        likesViewer = view.findViewById(R.id.likes);
        loveViewer = view.findViewById(R.id.love);
        imagesViewer = view.findViewById(R.id.imagesViewer);
        View placeView = view.findViewById(R.id.placeView);
        imagesViewer.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
        imagesViewer.setHasFixedSize(true);
        ////////////////////////////////////////////
        ImageView chat2 = view.findViewById(R.id.chat2);
        View.OnClickListener chatOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean match_plus = Boolean.parseBoolean(libs.getUserData("match_plus"));
                if (match_plus) {

                    context.startActivity(new Intent(context, ChatRoom.class).putExtra("friendUid", matcher.userId));
                } else {
                    libs.dialog_match_plus();
                }
            }
        };
        chat2.setOnClickListener(chatOnClickListener);
        chat.setOnClickListener(chatOnClickListener);
        so_like = view.findViewById(R.id.so_like);
        so_like.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                superLike = true;
                boolean match_plus = Boolean.parseBoolean(libs.getUserData("match_plus"));
                if (match_plus) {
                    Swipe(matchesActivtiy, matcher.userId, "right", true);
                }else {
                  //  Toasty.info(context, R.string.match_plus, Toast.LENGTH_SHORT, true).show();
                    libs.dialog_match_plus();
                }
            }
        });
        like = view.findViewById(R.id.like);
        like.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                superLike = false;
                Swipe(matchesActivtiy, matcher.userId, "right", true);
            }
        });
        reject.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick(View v) {

                //todo dodo
                LayoutInflater factory = LayoutInflater.from(context);
                final View dialogView = factory.inflate(R.layout.alert_skip, null);
                final AlertDialog dialog = new AlertDialog.Builder(context).create();
                Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawableResource(android.R.color.transparent);
                TextView ok = dialogView.findViewById(R.id.ok);
                TextView cancel = dialogView.findViewById(R.id.cancel);
                ok.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();

                        Swipe(matchesActivtiy, matcher.userId, "left", true);
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
        });
        matchPlusGoldenBorder.setVisibility(matcher.heIsUsingMatchPlus ? View.VISIBLE : View.GONE);
        vipView.setVisibility(matcher.isVIPFrame() ? View.VISIBLE : View.GONE);
        if (matcher.interests == null || String.valueOf(matcher.interests).isEmpty()) {
            interestsViewer.setVisibility(View.GONE);
            inter_layout.setVisibility(View.GONE);
        }
        interestsViewer.setText(String.valueOf(matcher.interests));
        if (matcher.images.isEmpty()) {
            imagesViewer.setVisibility(View.GONE);
        }
        imagesViewer.setAdapter(new RecyclerView.Adapter() {
            @NonNull
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                return new RecyclerView.ViewHolder(context.getLayoutInflater().inflate(R.layout.item_image_no_rmove, parent, false)) {
                    @Override
                    public String toString() {
                        return super.toString();
                    }
                };
            }
            @Override
            public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
                ImageView imageView = holder.itemView.findViewById(R.id.image);
                Picasso.get().load(matcher.images.get(position)).into(imageView);
            }
            @Override
            public int getItemCount() {
                return matcher.images.size();
            }
        });
        String finalName  = matcher.getName();

        if (matcher.isHideAge()) {
            nameViewer.setText(finalName);
        } else {
            nameViewer.setText(finalName + "/" + matcher.age);
        }
        Picasso.get().load(matcher.profileImage).into(profileImageViewer);
        briefViewer.setText(matcher.brief);
        if (matcher.isHideLocation()) {
            countryViewer.setVisibility(View.GONE);
            countryFlagViewer.setVisibility(View.GONE);
            country2Viewer.setVisibility(View.GONE);
            countryFlag2Viewer.setVisibility(View.GONE);
            placeView.setVisibility(View.GONE);
        } else {
            placeView.setVisibility(View.VISIBLE);
            String path = libs.getCountryFlagWithCountryCode(matcher.countriesList, matcher.getCountryCode());
            countryViewer.setText(matcher.getCountry());
            country2Viewer.setText(matcher.getCountry());
            Picasso.get().load(path).into(countryFlagViewer);
            Picasso.get().load(path).into(countryFlag2Viewer);
            likesViewer.setText(matcher.likes);
            loveViewer.setText(matcher.love);
        }
    }

    public static void Swipe(matchesActivtiy matchesActivtiy, String currentMatchUid, String direction, boolean ato_swipe) {
        String currentDate = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());
        FirebaseDatabase.getInstance().getReference("users").child(getUserId()).child("cardsSeenHistory").child(currentMatchUid).setValue(currentDate);
        matchesActivtiy.cardsSeenHistory.add(new cardsSeenHistoryItem(currentMatchUid, matchesActivtiy.currentDate));
        if (ato_swipe) {
            matchesActivtiy.cardStackLayoutManager.setSwipeAnimationSetting(new SwipeAnimationSetting.Builder()
                    .setDirection(direction.equals("right") ? Direction.Right : Direction.Left)
                    .setDuration(Duration.Normal.duration)
                    .setInterpolator(new AccelerateInterpolator())
                    .build());
            matchesActivtiy.cardStackView.setLayoutManager(matchesActivtiy.cardStackLayoutManager);
            matchesActivtiy.cardStackView.swipe();
        }
    }
    @Override
    public int getItemCount() {
        return Matchers.size();
    }
}
