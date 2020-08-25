package com.kyle.random.other.helpers;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;

import com.google.android.material.snackbar.Snackbar;
import com.kyle.random.R;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.view.View;

import java.util.HashMap;


public abstract class PermissionsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Listeners = new HashMap<>();
        permissionsError = getString(R.string.id_619);
    }


    private HashMap<Integer, OnPermissionsGrantedListener> Listeners;

    public interface OnPermissionsGrantedListener {
        @SuppressLint("MissingPermission")
        public void onPermissionsGranted();
    }

    String permissionsError ;

    public void setPermissionsError(String permissionsError) {
        this.permissionsError = permissionsError;
    }

    int current_request = 0;


    public void requestAppPermissions(final String[] permissions, OnPermissionsGrantedListener permissionsGrantedListener) {

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
            current_request++;
            Listeners.put(current_request, permissionsGrantedListener);
            int permissionCheck = PackageManager.PERMISSION_GRANTED;
            boolean showRequestPermissions = false;
            for (String permission : permissions) {
                permissionCheck = permissionCheck + ContextCompat.checkSelfPermission(this, permission);
                showRequestPermissions = showRequestPermissions || ActivityCompat.shouldShowRequestPermissionRationale(this, permission);
            }
            if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                if (showRequestPermissions) {
                    Snackbar.make(findViewById(android.R.id.content), permissionsError, Snackbar.LENGTH_INDEFINITE).setAction("GRANT", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            ActivityCompat.requestPermissions(PermissionsActivity.this, permissions, current_request);
                        }
                    }).show();
                } else {
                    ActivityCompat.requestPermissions(this, permissions, current_request);
                }
            } else {
                permissionsGrantedListener.onPermissionsGranted();
            }
        } else {
            permissionsGrantedListener.onPermissionsGranted();
        }


    }

    public void requestAppPermissions(final String permission, OnPermissionsGrantedListener permissionsGrantedListener) {
        String[] permissions = new String[]{permission};
        requestAppPermissions(permissions, permissionsGrantedListener);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        int permissionCheck = PackageManager.PERMISSION_GRANTED;
        for (int permisson : grantResults) {
            permissionCheck = permissionCheck + permisson;
        }
        if ((grantResults.length > 0) && PackageManager.PERMISSION_GRANTED == permissionCheck) {
            Listeners.get(requestCode).onPermissionsGranted();
        } else {
            //Display message when contain some Dangerous permisson not accept
            Snackbar.make(findViewById(android.R.id.content), permissionsError,
                    Snackbar.LENGTH_INDEFINITE).setAction("ENABLE", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent();
                    i.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    i.setData(Uri.parse("package:" + getPackageName()));
                    i.addCategory(Intent.CATEGORY_DEFAULT);
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    i.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                    i.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                    startActivity(i);
                }
            }).show();
        }
    }

}
