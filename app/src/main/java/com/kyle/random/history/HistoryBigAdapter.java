package com.kyle.random.history;
import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.FirebaseDatabase;
import com.kyle.random.R;
import com.kyle.random.chat.ChatRoom;
import com.kyle.random.friends.Friend;
import com.kyle.random.other.Country;
import com.kyle.random.other.Libs;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import es.dmoral.toasty.Toasty;

import static com.kyle.random.other.Libs.getUserId;
public class HistoryBigAdapter extends RecyclerView.Adapter<HistoryBigAdapter.MyViewHolder> {
    Activity context;
    ArrayList<Friend> Friends;
    String myUid;
    Libs libs;
    ArrayList<Country> countriesList;
    public HistoryBigAdapter(Activity context, ArrayList<Friend> Friends, ArrayList<Country> countriesList) {
        this.context = context;
        this.Friends = Friends;
        myUid = getUserId();
        libs = new Libs(context);
        this.countriesList = countriesList;
    }
    public static class MyViewHolder extends RecyclerView.ViewHolder {
        ImageView profileImage, report, remove, like, countryFlag;
        TextView date, name, country;
        View chat;
        CardView item;
        public MyViewHolder(View itemView) {
            super(itemView);
            this.profileImage = itemView.findViewById(R.id.profileImage);
            this.report = itemView.findViewById(R.id.report);
            this.remove = itemView.findViewById(R.id.remove);
            this.like = itemView.findViewById(R.id.like);
            this.countryFlag = itemView.findViewById(R.id.countryFlag);
            this.date = itemView.findViewById(R.id.date);
            this.name = itemView.findViewById(R.id.name);
            this.country = itemView.findViewById(R.id.country);
            this.chat = itemView.findViewById(R.id.chat);
            this.item = itemView.findViewById(R.id.item);

        }
    }
    @Override
    public HistoryBigAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_history_big, parent, false);
        return new HistoryBigAdapter.MyViewHolder(view);
    }
    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
        final Friend friend = Friends.get(position);
        Picasso.get().load(friend.profileImage).placeholder(R.drawable.loading)/*.fit()*/.into(holder.profileImage);
        String countryFlag = libs.getCountryFlagWithCountryCode(countriesList, friend.userCountryCode);
        Picasso.get().load(countryFlag).placeholder(R.drawable.loading)/*.fit()*/.into(holder.countryFlag);
        holder.item.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                libs.dialog_view_profile(context,friend.Uid);
            }
        });
        try {
            String simpleDateFormat = new SimpleDateFormat("M/d/yy", Locale.getDefault()).format(new Date(Long.parseLong(friend.randomChatDate)));
            holder.date.setText(simpleDateFormat);
        } catch (Exception e) {
            holder.date.setText("01/01/2020");
        }
        holder.name.setText(friend.name);
        holder.country.setText(friend.country + " , " + friend.city);
        holder.remove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseDatabase.getInstance().getReference("users").child(myUid).child("history").child(friend.Uid).removeValue();
                Toasty.success(context, R.string.rem, Toast.LENGTH_SHORT, true).show();
            }
        });
        holder.report.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                libs.report(friend.Uid,context);
            }
        });
        holder.like.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                libs.dialog_like(friend.Uid);
            }
        });
        holder.chat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                context.startActivity(new Intent(context, ChatRoom.class).putExtra("friendUid", friend.Uid));
            }
        });
    }
    @Override
    public int getItemCount() {
        return Friends.size();
    }
}
