package com.kyle.random.chat;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.builder.ColorPickerClickListener;
import com.flask.colorpicker.builder.ColorPickerDialogBuilder;
import com.google.firebase.database.FirebaseDatabase;
import com.kyle.random.R;
import com.kyle.random.other.Libs;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;
import es.dmoral.toasty.Toasty;

import static com.kyle.random.other.Libs.getUserId;
class ChatFriendsAdapter extends BaseAdapter {
    Activity context;
    ChatFriends chatFriends;
    ArrayList<FriendChatItem> users;
    ArrayList<FriendChatItem> tempUsers;
    String myUid;
    Libs libs;
    public ChatFriendsAdapter(ChatFriends chatFriends) {
        this.chatFriends = chatFriends;
        this.context = chatFriends.getActivity();
        this.users = new ArrayList<>();
        this.tempUsers = new ArrayList<>();
        myUid = getUserId();
        libs = new Libs(context);
    }
    public void setTempUsers(ArrayList<FriendChatItem> users) {
        tempUsers.clear();
        tempUsers.addAll(users);
    }
    @Override
    public int getCount() {
        return users.size();
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
        View gridView = inflater.inflate(R.layout.item_chat_friend, parent, false);
        final FriendChatItem friend = users.get(position);
        CircleImageView profileImage = gridView.findViewById(R.id.profileImage);
        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                libs.dialog_view_profile(context, friend.Uid);
            }
        });
        ImageView addWay = gridView.findViewById(R.id.addWay);
        switch (friend.addWay) {
            case "userName":
            case "weAreNotFriends":
                addWay.setVisibility(View.GONE);
                break;
            case "match":
                addWay.setImageResource(R.drawable.match);
                addWay.setVisibility(View.VISIBLE);
                break;
            case "random":
                addWay.setImageResource(R.drawable.random);
                addWay.setVisibility(View.VISIBLE);
                break;
        }
        RelativeLayout item = gridView.findViewById(R.id.item);
        TextView name = gridView.findViewById(R.id.name);
        TextView last_message_and_time = gridView.findViewById(R.id.last_message_and_time);
        ImageView VIPimageView = gridView.findViewById(R.id.VIPimageView);
        TextView date = gridView.findViewById(R.id.date);
        String simpleDateFormat = new SimpleDateFormat("M/d/yy", Locale.getDefault()).format(new Date(friend.lastMessageDate));
        date.setText(simpleDateFormat);
        ImageView flag = gridView.findViewById(R.id.flag);
        boolean vip = friend.heIsVIP;
        VIPimageView.setVisibility(vip ? View.VISIBLE : View.GONE);
        Picasso.get().load(friend.flagImage).into(flag);
        Picasso.get().load(friend.profileImage).into(profileImage);
        name.setText(friend.name);
        //last_message_and_time.setText(friend.lastMessage+" "+friend.lastMessageDate);
        last_message_and_time.setText(friend.lastMessage);
        last_message_and_time.setTypeface(Typeface.defaultFromStyle(friend.lastMessageSeen ? Typeface.NORMAL : Typeface.BOLD));
        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                context.startActivity(new Intent(context, ChatRoom.class).putExtra("friendUid", friend.Uid));
            }
        };
        item.setOnClickListener(onClickListener);
        setLongClickListener(item, friend);
        return gridView;
    }
    public void setLongClickListener(View view, final FriendChatItem friend) {
        view.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                final ArrayList<String> items = new ArrayList<>();
                items.add(context.getString(R.string.id_625));
                items.add(context.getString(R.string.id_630));
                if (!friend.addWay.equals("weAreNotFriends")) {
                    items.add(context.getString(R.string.id_631));
                }
                if (!friend.blocked) {

                    if (friend.addWay.equals("weAreNotFriends")) {
                        if (friend.isHeSentFriendRequest) {
                            items.add(context.getString(R.string.id_206));
                        } else if (!friend.isISentFriendRequest) {
                            items.add(context.getString(R.string.id_29));
                        }
                    }
                    items.add(context.getString(R.string.id_632));
                } else {
                    items.add(context.getString(R.string.id_203));
                }
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                // builder.setTitle("Action:");
                builder.setItems(items.toArray(new String[0]), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int position) {
                        if (items.get(position).equals(context.getString(R.string.id_625))) {
                            ColorPickerDialogBuilder
                                    .with(context)
                                    .setTitle(context.getString(R.string.id_625))
                                    .initialColor(Color.parseColor(friend.colorHex))
                                    .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
                                    .density(12)
                                    .setPositiveButton(context.getString(R.string.ok), new ColorPickerClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int selectedColor, Integer[] allColors) {
                                            String hex1 = String.format("#%06X", (0xFFFFFF & selectedColor));
                                            FirebaseDatabase.getInstance().getReference("users").child(myUid).child("friends").child(friend.Uid).child("colorHex").setValue(hex1);
                                        }
                                    })
                                    .setNegativeButton(context.getString(R.string.id_72), new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                        }
                                    })
                                    .build()
                                    .show();
                        }
                        if (items.get(position).equals(context.getString(R.string.id_630))) {
                            FirebaseDatabase.getInstance().getReference("users").child(myUid).child("friends").child(friend.Uid).child("chat").removeValue();
                        }
                        if (items.get(position).equals(context.getString(R.string.id_206))) {
                            FirebaseDatabase.getInstance().getReference("users").child(myUid).child("friendRequests").removeValue();
                            FirebaseDatabase.getInstance().getReference("users").child(friend.Uid).child("friends").child(myUid).setValue(myUid);
                            FirebaseDatabase.getInstance().getReference("users").child(myUid).child("friends").child(friend.Uid).setValue(friend.Uid);
                            Toasty.success(context, R.string.id_358, Toast.LENGTH_SHORT, true).show();
                        }
                        if (items.get(position).equals(context.getString(R.string.id_631))) {
                            FirebaseDatabase.getInstance().getReference("users").child(myUid).child("friends").child(friend.Uid).child("addWay").removeValue();
                            FirebaseDatabase.getInstance().getReference("users").child(friend.Uid).child("friends").child(myUid).child("addWay").removeValue();
                        }
                        if (items.get(position).equals(context.getString(R.string.id_29))) {
                            HashMap<Object, Object> HashMap = new HashMap<>();
                            HashMap.put("uid", myUid);
                            HashMap.put("addWay", "userName");
                            FirebaseDatabase.getInstance().getReference("users").child(friend.Uid).child("friendRequests").child(myUid).setValue(HashMap);
                            Toasty.success(context, R.string.id_209, Toast.LENGTH_SHORT, true).show();
                        }
                        if (items.get(position).equals(context.getString(R.string.id_632))) {
                            friend.blocked = true;
                            FirebaseDatabase.getInstance().getReference("users").child(myUid).child("blockedList").child(friend.Uid).setValue(friend.Uid);
                            Toasty.success(context, R.string.id_720, Toast.LENGTH_SHORT, true).show();
                            notifyDataSetChanged();
                        }
                        if (items.get(position).equals(context.getString(R.string.id_203))) {
                            friend.blocked = false;
                            FirebaseDatabase.getInstance().getReference("users").child(myUid).child("blockedList").child(friend.Uid).removeValue();
                            Toasty.success(context, R.string.id_219, Toast.LENGTH_SHORT, true).show();
                            notifyDataSetChanged();
                        }
                    }
                });
                AlertDialog alert = builder.create();
                alert.show();
                return false;
            }
        });
    }
    public void load(boolean loadOthers) {
        users.clear();
        for (FriendChatItem user : tempUsers) {
            if (loadOthers && user.others) {
                users.add(user);
            }
            if (!loadOthers && !user.others) {
                users.add(user);
            }
        }
        if (users.isEmpty()) {
            chatFriends.noChatMessage.setText(context.getString(loadOthers ? R.string.id_636 : R.string.id_59));
            chatFriends.noChatLayout.setVisibility(View.VISIBLE);
            chatFriends.chatLayout.setVisibility(View.GONE);
        } else {
            chatFriends.noChatLayout.setVisibility(View.GONE);
            chatFriends.chatLayout.setVisibility(View.VISIBLE);
        }
        notifyDataSetChanged();
    }
}
