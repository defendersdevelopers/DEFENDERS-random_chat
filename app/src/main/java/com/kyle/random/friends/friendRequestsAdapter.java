package com.kyle.random.friends;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.kyle.random.R;
import com.kyle.random.chat.ChatRoom;
import com.google.firebase.database.FirebaseDatabase;
import com.kyle.random.other.Libs;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;
import es.dmoral.toasty.Toasty;

import static com.kyle.random.other.Libs.getUserId;
class friendRequestsAdapter extends BaseAdapter {
     Context  context;
    ArrayList<Friend> Friends;
String myUid;
    public friendRequestsAdapter(Context context, ArrayList<Friend> Friends) {
        this.context = context;
        this.Friends = Friends;
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
    @SuppressLint("ViewHolder")
    public View getView(final int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        assert inflater != null;
      View gridView = inflater.inflate(R.layout.item_friend_request, parent, false);
        final Friend friend=Friends.get(position);
        CircleImageView  profileImage = gridView.findViewById(R.id.profileImage);

        TextView name = gridView.findViewById(R.id.name);
        TextView country = gridView.findViewById(R.id.country);
        TextView accept = gridView.findViewById(R.id.accept);
        TextView deny = gridView.findViewById(R.id.deny);
        deny.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                FirebaseDatabase.getInstance().getReference("users").child(myUid).child("friendRequests").child(friend.Uid).removeValue();
                Toasty.success(context, R.string.id_217, Toast.LENGTH_SHORT, true).show();

                notifyDataSetChanged();
            }
        });
        accept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseDatabase.getInstance().getReference("users").child(myUid).child("friendRequests").child(friend.Uid).removeValue();


                new Libs(context).makeFriends(myUid , friend.Uid , friend.addWay , friend.Uid);

                Toasty.success(context, R.string.id_218, Toast.LENGTH_SHORT, true).show();

                notifyDataSetChanged();
            }
        });

        Picasso.get().load(friend.profileImage).into(profileImage);
     //   name.setText(getHideName(friend.name));
        name.setText(friend.name);
        country.setText(friend.country+" , "+friend.city);
        View.OnClickListener onClickListener=new View.OnClickListener() {
            @Override
            public void onClick(View v) {
context.startActivity(new Intent(context, ChatRoom.class).putExtra("friendUid",friend.Uid));
            }
        };
        return gridView;
    }
}
