package com.kyle.random.history;
import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.recyclerview.widget.RecyclerView;

import com.kyle.random.R;
import com.kyle.random.friends.Friend;
import com.kyle.random.other.helpers.OnFriendChangeListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;
public class HistorySmallAdapter extends RecyclerView.Adapter<HistorySmallAdapter.MyViewHolder> {
    private ArrayList<Friend> Friends;
    public Activity context;
    OnFriendChangeListener onFriendChangeListener;
    public HistorySmallAdapter(Activity context, ArrayList<Friend> Friends, OnFriendChangeListener onFriendChangeListener) {
        this.Friends = Friends;
        this.context = context;
        this.onFriendChangeListener = onFriendChangeListener;
    }
    public static class MyViewHolder extends RecyclerView.ViewHolder {
        CircleImageView FriendImageView;
        FrameLayout item;
        CircleImageView appliedFrameLayout;
        public MyViewHolder(View itemView) {
            super(itemView);
            this.FriendImageView = itemView.findViewById(R.id.image);
            this.appliedFrameLayout = itemView.findViewById(R.id.appliedFrameLayout);
            this.item = itemView.findViewById(R.id.item);
        }
    }
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = context.getLayoutInflater().inflate(R.layout.item_history_small, parent, false);

        return  new MyViewHolder(view);
    }
    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
        final CircleImageView FriendImageView = holder.FriendImageView;
        final CircleImageView appliedFrameLayout = holder.appliedFrameLayout;
        FrameLayout item = holder.item;
        final Friend Friend = Friends.get(position);
        if (Friend.applied) {
            appliedFrameLayout.setVisibility(View.VISIBLE);
        } else {
            appliedFrameLayout.setVisibility(View.GONE);
        }
        item.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deselectAllFriends();
                Friend.applied = true;
                notifyDataSetChanged();
                onFriendChangeListener.onFriendChange(Friend.Uid,position);
            }
        });
        Picasso.get().load(Friend.profileImage).placeholder(R.drawable.loading)/*.fit()*/.into(FriendImageView);


    }
    public void deselectAllFriends() {
        for (Friend Friend : Friends) {
            Friend.applied = false;
        }
    }
    @Override
    public int getItemCount() {
        return Friends.size();
    }
}
