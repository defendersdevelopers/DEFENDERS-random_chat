package com.kyle.random;
import android.content.Context;

import com.kyle.random.other.Hobby;

import java.util.ArrayList;

import io.agora.rtc.video.VideoEncoderConfiguration;
public class KEYS {
    public static String DEEP_AR = "97681d9a07f53c1bbe62748d2c007c5cc4e5ce5c496c646cc553c67156fb6c1a9d0d2b8bec43c48d";
    public static String AGORA_KEY = "a78b2c843afd4097bc0a97da5bfccc94";
    public static String TRANSLATE_KEY = "AIzaSyBbdT8XDrm0EuZs_96ylmti8yUYugV62G8";
    public static VideoEncoderConfiguration.VideoDimensions VIDEO_QUALITY = VideoEncoderConfiguration.VD_1280x720; //  VD_640x360 is cheaper
    // todo if you wanna change hobbies
    public static ArrayList<Hobby> getAllHobbies(Context context) {
        ArrayList<Hobby> items = new ArrayList<>();
        items.add(new Hobby("hobby_1", context.getString(R.string.id_600), R.drawable.h_19));
        items.add(new Hobby("hobby_2", context.getString(R.string.id_601), R.drawable.h_17));
        items.add(new Hobby("hobby_3", context.getString(R.string.id_602), R.drawable.h_18));
        items.add(new Hobby("hobby_4", context.getString(R.string.id_603), R.drawable.h_10));
        items.add(new Hobby("hobby_5", context.getString(R.string.id_604), R.drawable.h_29));
        items.add(new Hobby("hobby_6", context.getString(R.string.id_605), R.drawable.h_37));
        items.add(new Hobby("hobby_7", context.getString(R.string.id_606), R.drawable.h_13));
        items.add(new Hobby("hobby_8", context.getString(R.string.id_607), R.drawable.h_34));
        items.add(new Hobby("hobby_9", context.getString(R.string.id_608), R.drawable.h_33));
        return items;
    }
    // todo don't forget to change xml strings too

    public static String APPLICATION_ID = "com.kyle.random";
}
