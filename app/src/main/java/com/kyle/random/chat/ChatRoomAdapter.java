package com.kyle.random.chat;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Environment;
import android.os.StrictMode;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.util.Linkify;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.text.TextUtilsCompat;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.DownloadListener;
import com.github.captain_miao.optroundcardview.OptRoundCardView;
import com.google.cloud.translate.Translate;
import com.google.cloud.translate.TranslateOptions;
import com.google.cloud.translate.Translation;
import com.google.firebase.database.FirebaseDatabase;
import com.kyle.random.R;
import com.kyle.random.friends.Message;
import com.kyle.random.other.Libs;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;
import es.dmoral.toasty.Toasty;
import me.jagar.chatvoiceplayerlibrary.VoicePlayerView;

import static com.kyle.random.KEYS.TRANSLATE_KEY;
import static com.kyle.random.other.Libs.dpToPx;
import static com.kyle.random.other.Libs.getUserId;
public class ChatRoomAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    ChatRoom context;
    ArrayList<Message> Messages;
    String myUid, myImage;
    String friendImage, friendUid;
    Libs libs;
    boolean isLeftToRight;
    int color;
    public ChatRoomAdapter(ChatRoom context, ArrayList<Message> Messages, String friendImage, String myImage, String friendUid , int color) {
        isLeftToRight = TextUtilsCompat.getLayoutDirectionFromLocale(Locale.getDefault()) == ViewCompat.LAYOUT_DIRECTION_LTR;
        this.context = context;
        this.Messages = Messages;
        this.friendImage = friendImage;
        this.myImage = myImage;
        this.color=color ;
        myUid = getUserId();
        libs = new Libs(context);
        this.friendUid = friendUid;
        initTranslation();
    }
    Translate translate;
    public void initTranslation() {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        TranslateOptions translateOptions = TranslateOptions.newBuilder().setApiKey(TRANSLATE_KEY).build();
        translate = translateOptions.getService();
    }
    public String translate(String originalText, String to) {
        Translation translation = translate.translate(originalText, Translate.TranslateOption.targetLanguage(to.toLowerCase()), Translate.TranslateOption.model("base"));
        return translation.getTranslatedText();
    }
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new RecyclerView.ViewHolder(context.getLayoutInflater().inflate(R.layout.item_message, parent, false)) {
            @Override
            public String toString() {
                return super.toString();
            }
        };
    }
    public int getLastSentIndex() {
        int index = -1;
        for (int i = 0; i < Messages.size(); i++) {
            if (Messages.get(i).who.equals("me")) {
                index = i;
            }
        }
        return index;
    }
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        View view = holder.itemView;
        final Message message = Messages.get(position);





        if (position == 0) {
            LinearLayout container = view.findViewById(R.id.container);
            container.setPadding(container.getPaddingLeft(), dpToPx(10), container.getPaddingRight(), container.getPaddingBottom());
        }

        OptRoundCardView top_card_view = view.findViewById(R.id.top_card_view);
        top_card_view.setCardBackgroundColor(color);







        final TextView textAfterTrans = view.findViewById(R.id.textAfterTrans);
        ImageView statue = view.findViewById(R.id.statue);
        CircleImageView me_profile_image = view.findViewById(R.id.me_profile_image);
        CircleImageView friend_profile_image = view.findViewById(R.id.friend_profile_image);
        friend_profile_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                libs.dialog_view_profile(context, friendUid);
            }
        });
        TextView friend_message = view.findViewById(R.id.friend_message);
        TextView me_message = view.findViewById(R.id.me_message);
        RelativeLayout textVoiceCall = view.findViewById(R.id.textVoiceCall);
        RelativeLayout textVideoCall = view.findViewById(R.id.textVideoCall);
        ImageView image_message_me = view.findViewById(R.id.image_message_me);
        ImageView image_message_friend = view.findViewById(R.id.image_message_friend);
        VoicePlayerView voice_message_me = view.findViewById(R.id.voice_message_me);
        VoicePlayerView voice_message_friend = view.findViewById(R.id.voice_message_friend);
        LinearLayout myItem = view.findViewById(R.id.myItem);
        LinearLayout myFriendItem = view.findViewById(R.id.myFriendItem);
        myItem.setLayoutDirection(!isLeftToRight ? View.LAYOUT_DIRECTION_LTR : View.LAYOUT_DIRECTION_RTL);
        myFriendItem.setLayoutDirection(isLeftToRight ? View.LAYOUT_DIRECTION_LTR : View.LAYOUT_DIRECTION_RTL);
        image_message_friend.setVisibility(View.VISIBLE);
        voice_message_friend.setVisibility(View.VISIBLE);
        myItem.setVisibility(View.VISIBLE);
        image_message_me.setVisibility(View.VISIBLE);
        voice_message_me.setVisibility(View.VISIBLE);
        myFriendItem.setVisibility(View.VISIBLE);
        me_message.setVisibility(View.VISIBLE);
        textVideoCall.setVisibility(View.VISIBLE);
        textVoiceCall.setVisibility(View.VISIBLE);
        friend_message.setVisibility(View.VISIBLE);
        me_profile_image.setVisibility(View.VISIBLE);
        friend_profile_image.setVisibility(View.VISIBLE);
        if (position == getLastSentIndex()) {
            statue.setVisibility(View.VISIBLE);
            statue.setImageResource(message.seen ? R.drawable.seen : R.drawable.delivered);
        } else {
            statue.setVisibility(View.GONE);
        }
        statue.setImageResource(message.seen ? R.drawable.seen : R.drawable.delivered);
        switch (message.kind) {
            case "text":
                image_message_me.setVisibility(View.GONE);
                voice_message_me.setVisibility(View.GONE);
                image_message_friend.setVisibility(View.GONE);
                voice_message_friend.setVisibility(View.GONE);
                textVideoCall.setVisibility(View.GONE);
                textVoiceCall.setVisibility(View.GONE);
                if (message.who.equals("me")) {
                    myFriendItem.setVisibility(View.GONE);
                    Picasso.get().load(myImage).into(me_profile_image);
                    me_message.setText(message.content);
                    Linkify.addLinks(me_message, Linkify.ALL);
                    setLongClickListener(me_message, message);
                } else {
                    myItem.setVisibility(View.GONE);
                    Picasso.get().load(friendImage).placeholder(R.drawable.loading).into(friend_profile_image);
                    friend_message.setText(message.content);
                    Linkify.addLinks(friend_message, Linkify.ALL);
                    setLongClickListener(friend_message, message);
                }
                if (libs.getUserData("translate_enabled").equals("true")) {
                    textAfterTrans.setVisibility(View.VISIBLE);
                    //todo translate words with google
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            final String result = translate(message.content, libs.getLanguage());
                            context.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    textAfterTrans.setVisibility(message.content.equals(result) ? View.GONE : View.VISIBLE);
                                    if (!result.isEmpty()) {
                                        textAfterTrans.setText(result);
                                    }
                                }
                            });
                        }
                    }).start();
                } else {
                    textAfterTrans.setVisibility(View.GONE);
                }
                break;
            case "video_call":
                textVideoCall.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        context.startCall(true);
                    }
                });
                textVoiceCall.setVisibility(View.GONE);
                myItem.setVisibility(View.GONE);
                myFriendItem.setVisibility(View.GONE);
                break;
            case "voice_call":
                textVoiceCall.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        context.startCall(false);
                    }
                });
                textVideoCall.setVisibility(View.GONE);
                myItem.setVisibility(View.GONE);
                myFriendItem.setVisibility(View.GONE);
                break;
            case "image":
                textVideoCall.setVisibility(View.GONE);
                textVoiceCall.setVisibility(View.GONE);
                if (message.who.equals("me")) {
                    voice_message_me.setVisibility(View.GONE);
                    myFriendItem.setVisibility(View.GONE);
                    me_message.setVisibility(View.GONE);
                    Picasso.get().load(message.content).placeholder(R.drawable.loading).into(image_message_me);
                    Picasso.get().load(myImage).placeholder(R.drawable.loading).into(me_profile_image);
                    setLongClickListener(myItem, message);
                } else {
                    myItem.setVisibility(View.GONE);
                    voice_message_friend.setVisibility(View.GONE);
                    friend_message.setVisibility(View.GONE);
                    Picasso.get().load(message.content).placeholder(R.drawable.loading).into(image_message_friend);
                    Picasso.get().load(friendImage).placeholder(R.drawable.loading).into(friend_profile_image);
                    setLongClickListener(myFriendItem, message);
                }
                break;
            case "voice":
                textVideoCall.setVisibility(View.GONE);
                textVoiceCall.setVisibility(View.GONE);
                final VoicePlayerView playerView;
                if (message.who.equals("me")) {
                    playerView = voice_message_me;
                    image_message_me.setVisibility(View.GONE);
                    myFriendItem.setVisibility(View.GONE);
                    me_message.setVisibility(View.GONE);
                    Picasso.get().load(myImage).into(me_profile_image);
                    setLongClickListener(voice_message_me, message);
                } else {
                    playerView = voice_message_friend;
                    myItem.setVisibility(View.GONE);
                    image_message_friend.setVisibility(View.GONE);
                    friend_message.setVisibility(View.GONE);
                    Picasso.get().load(friendImage).into(friend_profile_image);
                    setLongClickListener(voice_message_friend, message);
                }
                ((TextView)playerView.findViewById(R.id.txtTime)).setFilters(new InputFilter[] {
                        new InputFilter.AllCaps() {
                            @Override
                            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                                return Libs.arabicToDecimal(source.toString());
                            }
                        }
                });
                final String dir = Environment.getExternalStorageDirectory() + File.separator + context.getString(R.string.app_name);
                final String filename = message.key + ".mp3";
                File target = new File(dir, filename);
                if (target.exists()) {
                    playerView.setAudio(target.getAbsolutePath());
                } else {
                    playerView.setAudio("none");
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            downloadVoice(message.content, dir, filename, playerView);
                        }
                    }).start();
                }
                break;
            default:
                myItem.setVisibility(View.GONE);
                myFriendItem.setVisibility(View.GONE);
                break;
        }
    }
    public void setLongClickListener(View view, final Message message) {
        view.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                final ArrayList<String> items = new ArrayList<>();
                if (message.kind.equals("text")) {
                    items.add(context.getString(R.string.id_626));
                }
                if (message.kind.equals("image")) {
                    items.add(context.getString(R.string.id_627));
                }
                items.add(context.getString(R.string.id_628));
                if (message.who.equals("me")) {
                    items.add(context.getString(R.string.id_629));
                }
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                // builder.setTitle("Action:");
                builder.setItems(items.toArray(new String[0]), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int position) {
                        if (items.get(position).equals(context.getString(R.string.id_626))) {
                            Libs.setClipboard(context, message.content);
                            Toasty.success(context, R.string.id_620, Toast.LENGTH_SHORT, true).show();
                        }
                        if (items.get(position).equals(context.getString(R.string.id_627))) {
                            downloadImage(message.content);
                        }
                        if (items.get(position).equals(context.getString(R.string.id_628))) {
                            deleteForMe(message.key);
                        }
                        if (items.get(position).equals(context.getString(R.string.id_629))) {
                            deleteForMe(message.key);
                            deleteForHim(message.key);
                        }
                    }
                });
                AlertDialog alert = builder.create();
                alert.show();
                return false;
            }
        });
    }
    public void downloadVoice(final String urlString, String dirPath, String fileName, final VoicePlayerView playerView) {
        final File target = new File(dirPath, fileName);
        AndroidNetworking.download(urlString, dirPath, fileName)
                .build()
                .startDownload(new DownloadListener() {
                    @Override
                    public void onDownloadComplete() {
                        playerView.setAudio(target.getAbsolutePath());
                    }
                    @Override
                    public void onError(ANError anError) {
                        target.delete();
                        Toasty.error(context, R.string.id_191, Toast.LENGTH_SHORT).show();
                    }
                });
    }
    public void downloadImage(String url) {
        Toasty.info(context, R.string.id_357, Toast.LENGTH_SHORT).show();
        String DIRECTORY_PICTURES = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getAbsolutePath();
        String dirPath = DIRECTORY_PICTURES + File.separator + context.getString(R.string.app_name);
        new File(dirPath).mkdirs();
        String fileName = new Date().getTime() + ".png";
        AndroidNetworking.download(url, dirPath, fileName)
                .build()
                .startDownload(new DownloadListener() {
                    @Override
                    public void onDownloadComplete() {
                        Toasty.success(context, R.string.id_192, Toast.LENGTH_SHORT).show();
                    }
                    @Override
                    public void onError(ANError anError) {
                        Toasty.error(context, R.string.id_193, Toast.LENGTH_SHORT).show();
                    }
                });
    }
    public void deleteForMe(String key){
        FirebaseDatabase.getInstance().getReference("users").child(myUid ).child("friends").child(friendUid).child("chat").child(key).removeValue();
    }public void deleteForHim(String key){
        FirebaseDatabase.getInstance().getReference("users").child(friendUid).child("friends").child(myUid).child("chat").child(key).removeValue();

    }
    @Override
    public int getItemCount() {
        return Messages.size();
    }
}
