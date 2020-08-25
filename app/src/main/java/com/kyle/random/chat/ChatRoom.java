package com.kyle.random.chat;
import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewPropertyAnimator;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.androidnetworking.AndroidNetworking;
import com.dewarder.holdinglibrary.HoldingButtonLayout;
import com.dewarder.holdinglibrary.HoldingButtonLayoutListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.kyle.random.R;
import com.kyle.random.friends.Message;
import com.kyle.random.friends.blockedList;
import com.kyle.random.other.BaseActivity;
import com.kyle.random.other.Libs;
import com.kyle.random.other.helpers.AudioRecorder;
import com.kyle.random.other.helpers.FilesUploader;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Objects;

import es.dmoral.toasty.Toasty;

import static com.kyle.random.other.Libs.getTimeAgo;
import static com.kyle.random.other.Libs.getUserId;
public class ChatRoom extends BaseActivity {
    EditText editMessage;
    String message;
    public String friendUid, myUid;
    ArrayList<Message> messagesList;
    ChatRoomAdapter ChatRoomAdapter;
    RecyclerView mChatRecycler;
    String friendImage;
    TextView userNameViewer;
    String friendName;
    TextView OnlineViewer;
    ImageView green;
    String myImage;
    private static final float SLIDE_TO_CANCEL_ALPHA_MULTIPLIER = 2.5f;
    private ViewPropertyAnimator mSlideToCancelAnimator, chatTextAnimator;
    private int mAnimationDuration;
    HoldingButtonLayout voice_message;
    View mSlideToCancel, chatText;
    AudioRecorder audioRecorder;
    boolean isVoiceExpand = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room);
        voice_message = findViewById(R.id.voice_message);
        // ----- voice message ----------
        mAnimationDuration = getResources().getInteger(android.R.integer.config_shortAnimTime);
        mSlideToCancel = findViewById(R.id.slide_to_cancel);
        chatText = findViewById(R.id.chat_text);
        voice_message.addListener(new HoldingButtonLayoutListener() {
            @Override
            public void onBeforeExpand() {
                if (mSlideToCancelAnimator != null) {
                    mSlideToCancelAnimator.cancel();
                }
                if (chatTextAnimator != null) {
                    chatTextAnimator.cancel();
                }
                mSlideToCancel.setTranslationX(0f);
                mSlideToCancel.setAlpha(0f);
                mSlideToCancel.setVisibility(View.VISIBLE);
                chatText.setAlpha(1f);
                mSlideToCancelAnimator = mSlideToCancel.animate().alpha(1f).setDuration(mAnimationDuration);
                mSlideToCancelAnimator.start();
                chatTextAnimator = chatText.animate().alpha(0f).setDuration(mAnimationDuration);
                chatTextAnimator.start();
                isVoiceExpand = true;
                requestAppPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, new OnPermissionsGrantedListener() {
                    @Override
                    public void onPermissionsGranted() {
                        File tempDir = ContextCompat.getDataDir(getApplicationContext());
                        if (tempDir != null && isVoiceExpand) {
                            String temp = tempDir.getAbsolutePath() + File.separator + System.currentTimeMillis() + ".mp3";
                            audioRecorder = new AudioRecorder(temp);
                            audioRecorder.start();
                        }
                    }
                });
            }
            @Override
            public void onExpand() {
            }
            @Override
            public void onBeforeCollapse() {
                mSlideToCancelAnimator = mSlideToCancel.animate().alpha(0f).setDuration(mAnimationDuration);
                chatTextAnimator = chatText.animate().alpha(1f).setDuration(mAnimationDuration);
                mSlideToCancelAnimator.setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        mSlideToCancel.setVisibility(View.INVISIBLE);
                        mSlideToCancelAnimator.setListener(null);
                    }
                });
                mSlideToCancelAnimator.start();
                chatTextAnimator.setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        chatTextAnimator.setListener(null);
                    }
                });
                chatTextAnimator.start();
            }
            @Override
            public void onCollapse(boolean cancel) {
                isVoiceExpand = false;
                if (audioRecorder != null) {
                    audioRecorder.stop();
                    final File voice = new File(audioRecorder.path);
                    if (cancel) {
                        voice.delete();
                    } else {
                        new FilesUploader(ChatRoom.this, Uri.fromFile(voice), "voice", ".mp3", new FilesUploader.OnImageUploadListener() {
                            @Override
                            public void OnImageUploaded(String file) {
                                Message msg = new Message("", "voice", file, "me", new Date().getTime(), false);
                                sendMessage(msg);
                                voice.delete();
                            }
                            @Override
                            public void OnError() {
                                Toasty.error(ChatRoom.this, R.string.id_185, Toast.LENGTH_SHORT, true).show();
                                voice.delete();
                            }
                        });
                    }
                    audioRecorder = null;
                }
            }
            @Override
            public void onOffsetChanged(float offset, boolean isCancel) {
                mSlideToCancel.setTranslationX(-voice_message.getWidth() * offset);
                mSlideToCancel.setAlpha(1 - SLIDE_TO_CANCEL_ALPHA_MULTIPLIER * offset);
                chatText.setAlpha(SLIDE_TO_CANCEL_ALPHA_MULTIPLIER * offset);
            }
        });
        // ----------------
        AndroidNetworking.initialize(this);
        editMessage = findViewById(R.id.editMessage);
        green = findViewById(R.id.online);
        Bundle extras = getIntent().getExtras();
        myUid = getUserId();
        if (extras != null) {
            friendUid = extras.getString("friendUid");
        }
        userNameViewer = findViewById(R.id.userNameViewer);
        mChatRecycler = findViewById(R.id.list);
        mChatRecycler.setLayoutManager(new LinearLayoutManager(this));
        mChatRecycler.setHasFixedSize(true);
        OnlineViewer = findViewById(R.id.OnlineViewer);
        messagesList = new ArrayList<>();
        final Libs libs = new Libs(ChatRoom.this);
        myImage = libs.getUserData("profileImage");
        Switch translation = findViewById(R.id.translation);
        translation.setChecked(libs.getUserData("translate_enabled").equals("true"));
        translation.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                libs.saveData("translate_enabled", String.valueOf(b));
                ChatRoomAdapter.notifyDataSetChanged();
            }
        });
        initSeen();
        initBlocks();
        loadFriendData();
    }
    public void onlineListener() {
        FirebaseDatabase.getInstance().getReference("users").child(friendUid).child("last_active").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot lastActiveDateTimeDataSnapshot) {
                long lastActiveDateTime = Long.parseLong(String.valueOf(lastActiveDateTimeDataSnapshot.getValue()));
                String timeAgo = getTimeAgo(lastActiveDateTime);
                OnlineViewer.setText(timeAgo);
                green.setVisibility(timeAgo.equals("Online") ? View.VISIBLE : View.GONE);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }
    public void initSeen() {
        seenRef = FirebaseDatabase.getInstance().getReference("users").child(friendUid).child("friends").child(myUid).child("chat");
        seenListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull final DataSnapshot messages) {
                FirebaseDatabase.getInstance().getReference("users").child(myUid).child("friends").child(friendUid).child("chat").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot myChats) {
                        for (DataSnapshot messageDataSnapshot : messages.getChildren()) {
                            Message message = messageDataSnapshot.getValue(Message.class);
                            if (message != null && message.who != null) {
                                message.key = messageDataSnapshot.getKey();
                                HashMap<String, Object> hashMap = new HashMap<>();
                                hashMap.put("seen", true);
                                if (message.who.equals("me")) {
                                    messageDataSnapshot.getRef().updateChildren(hashMap);
                                    if (myChats.child(message.key).exists()) {
                                        myChats.child(message.key).getRef().updateChildren(hashMap);
                                    }
                                }
                            }
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        };
    }
    boolean blocked = true;
    public void initBlocks() {
        FirebaseDatabase.getInstance().getReference("users").child(friendUid).child("blockedList").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull final DataSnapshot hisBlocks) {
                heLoaded = true;
                FirebaseDatabase.getInstance().getReference("users").child(getUserId()).child("blockedList").child(friendUid).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot isIBlockedHim) {
                        if (hisBlocks.hasChild(myUid)) {
                            blocked = true;
                            findViewById(R.id.chat).setVisibility(View.GONE);
                            findViewById(R.id.blocked).setVisibility(View.VISIBLE);
                            findViewById(R.id.blocked).setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    new AlertDialog.Builder(ChatRoom.this)
                                            .setMessage(R.string.id_186)
                                            .setPositiveButton(R.string.id_187, null)
                                            .show();
                                }
                            });
                        } else if (isIBlockedHim.exists()) {
                            blocked = true;
                            findViewById(R.id.chat).setVisibility(View.GONE);
                            findViewById(R.id.blocked).setVisibility(View.VISIBLE);
                            findViewById(R.id.blocked).setOnClickListener(new View.OnClickListener() {
                                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                                @Override
                                public void onClick(View view) {
                                    //todo dodo
                                    LayoutInflater factory = LayoutInflater.from(ChatRoom.this);
                                    final View dialogView = factory.inflate(R.layout.alert_reported_before, null);
                                    final AlertDialog dialog = new AlertDialog.Builder(ChatRoom.this).create();
                                    Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawableResource(android.R.color.transparent);
                                    TextView ok = dialogView.findViewById(R.id.ok);
                                    TextView cancel = dialogView.findViewById(R.id.cancel);
                                    ok.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            startActivity(new Intent(ChatRoom.this, blockedList.class));
                                            dialog.dismiss();
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
                        } else {
                            findViewById(R.id.chat).setVisibility(View.VISIBLE);
                            findViewById(R.id.blocked).setVisibility(View.GONE);
                            blocked = false;
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                    }
                });
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }
    public void loadFriendData() {
        FirebaseDatabase.getInstance().getReference("users").child(friendUid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot data) {
                friendImage = String.valueOf(data.child("profileImage").getValue());
                friendName = String.valueOf(data.child("name").getValue());
                userNameViewer.setText(friendName);
                FirebaseDatabase.getInstance().getReference("users").child(myUid).child("friends").child(friendUid).child("colorHex").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String colorHex = snapshot.exists() ? String.valueOf(snapshot.getValue()) : String.format("#%06X", (0xFFFFFF & ContextCompat.getColor(ChatRoom.this, R.color.colorPrimary)));
                        int color = Color.parseColor(colorHex);
                        ChatRoomAdapter = new ChatRoomAdapter(ChatRoom.this, messagesList, friendImage, myImage, friendUid, color);
                        mChatRecycler.setAdapter(ChatRoomAdapter);
                        loadMessages();
                        onlineListener();
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }
    DatabaseReference seenRef;
    ValueEventListener seenListener;
    @Override
    protected void onStart() {
        super.onStart();
        seenRef.addValueEventListener(seenListener);
    }
    @Override
    protected void onStop() {
        super.onStop();
        seenRef.removeEventListener(seenListener);
    }
    public void loadMessages() {
        if (heLoaded && !messagesLoaded) {
            messagesLoaded = true;
            FirebaseDatabase.getInstance().getReference("users").child(myUid).child("friends").child(friendUid).child("chat").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot messages) {
                    final boolean firstTime = messagesList.isEmpty();
                    messagesList.clear();
                    for (DataSnapshot messageDataSnapshot : messages.getChildren()) {
                        Message message = messageDataSnapshot.getValue(Message.class);
                        if (message != null && message.who != null) {
                            message.key = messageDataSnapshot.getKey();
                            messagesList.add(message);
                        }
                    }
                    ChatRoomAdapter.notifyDataSetChanged();
                    mChatRecycler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                if (firstTime) {
                                    mChatRecycler.scrollToPosition(mChatRecycler.getAdapter().getItemCount() - 1);
                                } else {
                                    mChatRecycler.smoothScrollToPosition(mChatRecycler.getAdapter().getItemCount() - 1);
                                }
                            } catch (Exception e) {
                            }
                        }
                    }, 100);
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                }
            });
        } else {
            return;
        }
    }
    boolean heLoaded = false, messagesLoaded = false;
    public void send(View view) {
        message = editMessage.getText().toString();
        if (!message.isEmpty()) {
            Message msg = new Message("", "text", message, "me", new Date().getTime(), false);
            sendMessage(msg);
            editMessage.setText("");
        }
    }
    public String sendMessage(Message message) {
        String key = FirebaseDatabase.getInstance().getReference().push().getKey();

        /* image , text , video , voice , video_call , audio_call */
        DatabaseReference myMessagesDatabaseReference = FirebaseDatabase.getInstance().getReference("users").child(myUid).child("friends").child(friendUid).child("chat").child(key);
        DatabaseReference myFriendDatabaseReference = FirebaseDatabase.getInstance().getReference("users").child(friendUid).child("friends").child(myUid).child("chat").child(key);
        message.key = key;
        message.who = "me";
        myMessagesDatabaseReference.setValue(message);
        message.who = "him";
        myFriendDatabaseReference.setValue(message);
        return message.key;
    }
    public void voice_call(View view) {
        startCall(false);
        ////////////////////////////////////////////////////////////////////////////////////
    }
    public void startCall(boolean video) {
        if (blocked) {
            return;
        }
        String room_id = myUid + "~" + friendUid;
        String call_kind = video ? "video_call" : "voice_call";
        String message_id = sendMessage(new Message("", call_kind, room_id, "", new Date().getTime(), false));
        startActivity(new Intent(ChatRoom.this, InCall.class)
                .putExtra("video_room_id", room_id)
                .putExtra("call_kind", call_kind)
                .putExtra("message_id", message_id)
                .putExtra("friendUid", friendUid)
                .putExtra("message_id", message_id)
                .putExtra("isVideo", video)
                .putExtra("isIamTheCreator", true)
        );
    }
    public void back(View view) {
        finish();
    }
    public void video_call(View view) {
        startCall(true);
    }
    public void sendImage(View view) {
        Intent galleryIntent = new Intent();
        galleryIntent.setType("image/*");
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(galleryIntent, "SELECT IMAGE"), 1);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            progressDialog.show();
            new FilesUploader(this, data.getData(), "images", ".jpg", new FilesUploader.OnImageUploadListener() {
                @Override
                public void OnImageUploaded(String file) {
                    progressDialog.dismiss();
                    Message msg = new Message("", "image", file, "me", new Date().getTime(), false);
                    sendMessage(msg);
                    Toasty.success(getApplicationContext(), R.string.id_189, Toast.LENGTH_SHORT, true).show();
                }
                @Override
                public void OnError() {
                    progressDialog.dismiss();
                    Toasty.error(getApplicationContext(), R.string.id_190, Toast.LENGTH_SHORT, true).show();
                }
            });
        }
    }
    public void viewProfile(View view) {
        if (!friendUid.isEmpty()) {
            libs.dialog_view_profile(this, friendUid);
        }
    }
}
