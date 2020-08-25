package com.kyle.random.friends;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.kyle.random.other.BaseActivity;

import com.kyle.random.R;

public class add_friends extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_friends);

    }

    public void addByUserName(View view) {
        startActivity(new Intent(this,add_friends_with_username.class));
    }

    public void invitWithSMS(View view) {
        startActivity(new Intent(this, invit_friends_with_sms.class));
    }

    public void inviteWithApp(View view) {
        final String appLink = "https://play.google.com/store/apps/details?id=" +getPackageName();
        Intent intent = new Intent(android.content.Intent.ACTION_SEND);
        String shareBody = getString(R.string.id_198) + appLink;
        intent.setType("text/plain");
        intent.putExtra(android.content.Intent.EXTRA_SUBJECT, getString(R.string.id_199));
        intent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);

startActivity(Intent.createChooser(intent, getString(R.string.id_200)));
}

    public void close(View view) {
        finish();
    }


}
