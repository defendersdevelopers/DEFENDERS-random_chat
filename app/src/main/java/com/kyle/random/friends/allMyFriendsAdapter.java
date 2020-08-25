package com.kyle.random.friends;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.kyle.random.R;
import com.kyle.random.chat.ChatRoom;
import com.kyle.random.other.Libs;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.kyle.random.other.Libs.getUserId;
public class allMyFriendsAdapter
        extends RecyclerView.Adapter<allMyFriendsAdapter.myViewHolder> {
    Activity context;
    ArrayList<Friend> Friends;
    String myUid;
    String from;
    Libs libs;
    public allMyFriendsAdapter(Activity context, ArrayList<Friend> Friends, String from) {
        this.context = context;
        this.Friends = Friends;
        this.from = from;
        libs = new Libs(context);
        myUid = getUserId();
    }
    @Override
    public myViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.item_friend_new, parent, false);
        return new myViewHolder(view);
    }
    @Override
    public void onBindViewHolder(@NonNull myViewHolder holder, int position) {
        final Friend friend = Friends.get(position);
        Picasso.get().load(friend.profileImage).into(holder.profileImage);
        holder.profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                context.startActivity(new Intent(context, ChatRoom.class).putExtra("friendUid", friend.Uid));
            }
        });
        boolean vip = friend.heIsVIP;
        holder.VIPimageView.setVisibility(vip ? View.VISIBLE : View.GONE);
        holder.name.setText(friend.name);
        switch (friend.addWay) {
            case "userName":
                holder.addWay.setVisibility(View.GONE);
                break;
            case "match":
                holder.addWay.setImageResource(R.drawable.match);
                holder.addWay.setVisibility(View.VISIBLE);
                break;
            case "random":
                holder.addWay.setImageResource(R.drawable.random);
                holder.addWay.setVisibility(View.VISIBLE);
                break;
        }
        holder.chat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                context.startActivity(new Intent(context, ChatRoom.class).putExtra("friendUid", friend.Uid));
            }
        });
    }
    @Override
    public int getItemCount() {
        return Friends.size();
    }
    public class myViewHolder extends RecyclerView.ViewHolder {
        TextView name;
        CircleImageView profileImage;
        ImageView addWay, VIPimageView;
        LinearLayout chat;
        public myViewHolder(View itemView) {
            super(itemView);
            addWay = itemView.findViewById(R.id.addWay);
            VIPimageView = itemView.findViewById(R.id.VIPimageView);
            profileImage = itemView.findViewById(R.id.profileImage);
            name = itemView.findViewById(R.id.name);
            chat = itemView.findViewById(R.id.chat);
        }
    }
}