package com.kyle.random.getStarted;

import com.kyle.random.other.BaseActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.kyle.random.R;
import com.kyle.random.main.fragmentViewer;

public class hobbies extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.hobbies);
    }

    public void ok(View view) {
        startActivity(new Intent(this,select_hobbies.class).putExtra("isEdit",false));
    }

    public void skip(View view) {
        startActivity(new Intent(this, fragmentViewer.class));
    }
}
