package com.kyle.random.chat;
import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.kyle.random.R;
import com.kyle.random.friends.Message;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.kyle.random.other.Libs.getUserId;
public class inCallChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    Activity context;
    ArrayList<Message> Messages;
    String myUid, myImage;
    String friendImage;
    public inCallChatAdapter(Activity context, ArrayList<Message> Messages, String friendImage, String myImage) {
        this.context = context;
        this.Messages = Messages;
        this.friendImage = friendImage;
        this.myImage = myImage;
        myUid = getUserId();
    }
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new RecyclerView.ViewHolder(context.getLayoutInflater().inflate(R.layout.item_incall_message, parent, false)) {
            @Override
            public String toString() {
                return super.toString();
            }
        };
    }
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        View view = holder.itemView;
        final Message message = Messages.get(position);
        LinearLayout item = view.findViewById(R.id.item);
        if (position == 0) {
            item.setAlpha((float) 1.0f);
        }
        else if (position ==1) {

            item.setAlpha((float) 1.0f);
        }
        else  if (position == 2) {
            item.setAlpha((float) 0.7f);
        }

        else  {
            item.setAlpha((float) 0.4f);
        }


        CircleImageView profile_imageCircleImageView = view.findViewById(R.id.profile_image);
        TextView messageTextView = view.findViewById(R.id.message);
        if (message.kind.equals("text")) {
            if (message.who.equals("me")) {
                Picasso.get().load(myImage).into(profile_imageCircleImageView);
            } else {
                Picasso.get().load(friendImage).into(profile_imageCircleImageView);
            }
            messageTextView.setText(message.content);
        }
    }
    @Override
    public int getItemCount() {
        return Messages.size();
    }
}
