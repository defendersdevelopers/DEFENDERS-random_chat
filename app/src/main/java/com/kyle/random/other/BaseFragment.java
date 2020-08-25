package com.kyle.random.other;

import androidx.fragment.app.Fragment;
import android.app.ProgressDialog;
import android.os.Bundle;

import androidx.annotation.Nullable;

import com.kyle.random.R;

import java.util.regex.Pattern;

public class BaseFragment extends Fragment {
    public ProgressDialog progressDialog;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        progressDialog = new ProgressDialog(BaseFragment.this.getActivity());
        progressDialog.setCancelable(false);
        progressDialog.setMessage(getString(R.string.id_247));
    }
    public static boolean isValidEmail(String email) {
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\." +
                "[a-zA-Z0-9_+&*-]+)*@" +
                "(?:[a-zA-Z0-9-]+\\.)+[a-z" +
                "A-Z]{2,7}$";
        Pattern pat = Pattern.compile(emailRegex);
        if (email == null)
            return false;
        return pat.matcher(email).matches();
    }




}
