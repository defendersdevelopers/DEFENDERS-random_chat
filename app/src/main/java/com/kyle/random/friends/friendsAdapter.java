package com.kyle.random.friends;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.cardview.widget.CardView;

import com.kyle.random.R;
import com.kyle.random.chat.ChatRoom;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;
import es.dmoral.toasty.Toasty;

import static com.kyle.random.other.Libs.getUserId;
public class friendsAdapter extends BaseAdapter {
     Context  context;
    ArrayList<Friend> Friends;
String myUid;
String from;
    public friendsAdapter(Context context, ArrayList<Friend> Friends,String from) {
        this.context = context;
        this.Friends = Friends;
        this.from=from;
       myUid = getUserId();
    }

    @Override
    public int getCount() {
        return Friends.size();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        assert inflater != null;
        @SuppressLint("ViewHolder") View gridView = inflater.inflate(R.layout.item_friend, parent, false);
        final Friend friend=Friends.get(position);
        CircleImageView  profileImage = gridView.findViewById(R.id.profileImage);
        CardView item = gridView.findViewById(R.id.item);
        TextView name = gridView.findViewById(R.id.name);
        TextView country = gridView.findViewById(R.id.country);
        ImageView chat = gridView.findViewById(R.id.chat);
        ImageView block = gridView.findViewById(R.id.block);
        Picasso.get().load(friend.profileImage).into(profileImage);
    //    name.setText(getHideName(friend.name));
        name.setText(friend.name);
        country.setText(friend.country+" , "+friend.city);
        View.OnClickListener onClickListener=new View.OnClickListener() {
            @Override
            public void onClick(View v) {
context.startActivity(new Intent(context, ChatRoom.class).putExtra("friendUid",friend.Uid));
            }
        };
        block.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (friend.blocked){
                    friend.blocked=false;
                    FirebaseDatabase.getInstance().getReference("users").child(myUid).child("blockedList").child(friend.Uid).removeValue();
                   Toasty.success(context, R.string.id_219, Toast.LENGTH_SHORT, true).show();

                    if (from.equals("fromBlockedList")){
                        Friends.remove(position);
                        notifyDataSetChanged();
                    }

                }else {
                    friend.blocked=true;
                    FirebaseDatabase.getInstance().getReference("users").child(myUid).child("blockedList").child(friend.Uid).setValue(friend.Uid);
                    Toasty.success(context, R.string.id_720, Toast.LENGTH_SHORT, true).show();
                    if (from.equals("fromFriendsList")){
                        Friends.remove(position);
                        notifyDataSetChanged();
                    }
                }



            }
        });
        chat.setOnClickListener(onClickListener);
        item.setOnClickListener(onClickListener);
        return gridView;
    }
}
