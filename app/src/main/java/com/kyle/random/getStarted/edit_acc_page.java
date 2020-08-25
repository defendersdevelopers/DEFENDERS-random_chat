package com.kyle.random.getStarted;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.abdeveloper.library.MultiSelectDialog;
import com.abdeveloper.library.MultiSelectModel;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.kyle.random.R;
import com.kyle.random.other.BaseActivity;
import com.kyle.random.other.Country;
import com.kyle.random.other.Libs;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import es.dmoral.toasty.Toasty;

import static com.kyle.random.other.Libs.getUserId;
public class edit_acc_page extends BaseActivity {
    ImageView profilePicViewer;
    EditText edit_brief;
    RecyclerView imagesViewer;
    MyRecyclerViewAdapter myRecyclerViewAdapter;
    ArrayList<ImageItem> images;
    String Uid;
    StorageReference storageReference;
    String image, brief;
    Uri filePath;
    ArrayList<String> old_countries;
    EditText edit_name;
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_acc_page);
        profilePicViewer = findViewById(R.id.profilePicViewer);
        Uid = getUserId();
        imagesViewer = findViewById(R.id.imagesViewer);
        images = new ArrayList<>();
        myRecyclerViewAdapter = new MyRecyclerViewAdapter(this, images);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        imagesViewer.setLayoutManager(layoutManager);
        old_countries = new ArrayList<>();
        imagesViewer.setAdapter(myRecyclerViewAdapter);
        edit_brief = findViewById(R.id.brief);
        edit_name = findViewById(R.id.name);
        loadUserData();
    }
    public void addImage(View view) {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, getString(R.string.id_232)), 100);
        storageReference = FirebaseStorage.getInstance().getReference();
    }
    public void changeProfilePic(View view) {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, getString(R.string.id_232)), 200);
        storageReference = FirebaseStorage.getInstance().getReference();
    }
    public void chooseInterests(View view) {
        startActivity(new Intent(this, select_hobbies.class).putExtra("isEdit", true));
    }
    public void chooseLanguage(View view) {
        if (countriesList == null || countriesList.isEmpty()) {
            Toasty.info(this, "برجاء انتظار التحميل اولا", Toast.LENGTH_SHORT).show();
            return;
        }
        ArrayList<Integer> preSelectedCountries = new ArrayList<>();
        ArrayList<MultiSelectModel> formattedCountries = new ArrayList<>();
        for (int i = 0; i < countriesList.size(); i++) {
            Country country = countriesList.get(i);
            formattedCountries.add(new MultiSelectModel(i, country.CountryName));
            if (old_countries.contains(country.CountryCode)) {
                preSelectedCountries.add(i);
            }
        }
        MultiSelectDialog multiSelectDialog = new MultiSelectDialog()
                .title(getString(R.string.id_125)) //setting title for dialog
                .titleSize(25)
                .positiveText(getString(R.string.id_234))
                .negativeText(getString(R.string.id_235))
                //.setMinSelectionLimit(1) //you can set minimum checkbox selection limit (Optional)
                //.setMaxSelectionLimit(20) //you can set maximum checkbox selection limit (Optional)
                .preSelectIDsList(preSelectedCountries) //List of ids that you need to be selected
                .multiSelectList(formattedCountries) // the multi select model list with ids and name
                .onSubmit(new MultiSelectDialog.SubmitCallbackListener() {
                    @Override
                    public void onDismiss(ArrayList<Integer> ids) {

                    }
                    public void onSelected(ArrayList<Integer> selectedIds, ArrayList<String> selectedNames, String dataString) {
                        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference("users").child(Uid).child("liked_countries");
                        rootRef.removeValue();
                        if (!selectedIds.isEmpty()) {
                            HashMap<Object, Object> HashMap = new HashMap<>();
                            for (int i = 0; i < selectedIds.size(); i++) {
                                Country selected = countriesList.get(selectedIds.get(i));
                                HashMap.put(selected.CountryCode, selected.CountryCode);
                            }
                            rootRef.setValue(HashMap);
                        }
                        Toasty.success(edit_acc_page.this, R.string.id_237, Toast.LENGTH_SHORT, true).show();
                    }

                });
        multiSelectDialog.show(getSupportFragmentManager(), "multiSelectDialog");
    }
    String name;
    public void save(View view) {
        brief = edit_brief.getText().toString();
        name = edit_name.getText().toString();
        if (!brief.isEmpty()) {
            FirebaseDatabase.getInstance().getReference("users").child(Uid).child("brief").setValue(brief);
        }
        if (!name.isEmpty()) {
            FirebaseDatabase.getInstance().getReference("users").child(Uid).child("name").setValue(name);
        }
        finish();
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            filePath = data.getData();
            if (filePath == null) {
                Toasty.info(this, R.string.id_238, Toast.LENGTH_SHORT, true).show();
            } else {
                progressDialog.show();
                if (requestCode == 100) {
                    uploadImage(false);
                } else {
                    uploadImage(true);
                }
            }
        }
    }
    public void uploadImage(final Boolean profilePhoto) {

        String random = FirebaseDatabase.getInstance().getReference().push().getKey();
        final StorageReference ref = storageReference.child("images").child(random + ".png");
        filePath=filePath;
        ref.putFile(filePath).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri downUri) {
                        String image = downUri.toString();
                        if (profilePhoto) {
                            FirebaseDatabase.getInstance().getReference("users").child(Uid).child("profileImage").setValue(image);
                        } else {
                            FirebaseDatabase.getInstance().getReference("users").child(Uid).child("images").push().setValue(image);
                        }
                        progressDialog.dismiss();
                    }
                });
            }
        });
    }
    ArrayList<Country> countriesList;
    public void loadUserData() {
        new Libs(edit_acc_page.this).loadCountries(new Libs.OnLoadListener() {
            @Override
            public void OnLoad(final ArrayList<Country> countriesList) {
                edit_acc_page.this.countriesList = countriesList;
                FirebaseDatabase.getInstance().getReference("users").child(Uid).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot user) {
                        old_countries.clear();
                        images.clear();
                        image = String.valueOf(user.child("profileImage").getValue());
                        brief = String.valueOf(user.child("brief").getValue());
                        name=String.valueOf(user.child("name").getValue());
                        if (!name.isEmpty()){
                            edit_name.setText(name);
                        }
                        for (DataSnapshot image : user.child("images").getChildren()) {
                            images.add(new ImageItem(image.getKey(), String.valueOf(image.getValue())));
                        }
                        StringBuilder countriesStr = new StringBuilder();
                        try {
                            for (DataSnapshot language : user.child("liked_countries").getChildren()) {
                                String countryCode = String.valueOf(language.getValue());
                                old_countries.add(countryCode);
                                String countryName = new Libs(edit_acc_page.this).getCountryNameWithCountryCode(countriesList, countryCode);
                                countriesStr.append(countryName).append(" , ");
                            }
                            countriesStr = new StringBuilder(countriesStr.substring(0, countriesStr.length() - 2));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        TextView countries = findViewById(R.id.countries);
                        countries.setText((countriesStr.length() == 0) ? getString(R.string.id_127) : countriesStr.toString());
                        countries.setSelected(true);
                        if (!brief.equals("i'm a new user !")) {
                            edit_brief.setText(brief);
                        }

                        TextView hobbies = findViewById(R.id.hobbies);
                        StringBuilder hobbiesStr = new StringBuilder();
                        try {
                            for (DataSnapshot interest : user.child("hobbies").getChildren()) {
                                String hobby = Libs.getHobby(getApplicationContext(), String.valueOf(interest.getValue()));
                                hobbiesStr.append(hobby).append(" , ");
                            }
                            hobbiesStr = new StringBuilder(hobbiesStr.substring(0, hobbiesStr.length() - 2));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        hobbies.setText((hobbiesStr.length() == 0) ? getString(R.string.ch_interests) : hobbiesStr.toString());
                        hobbies.setSelected(true);
                        Picasso.get().load(image).into(profilePicViewer);
                        myRecyclerViewAdapter.notifyDataSetChanged();
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                    }
                });
            }
        });
    }
    public class MyRecyclerViewAdapter extends RecyclerView.Adapter<MyRecyclerViewAdapter.ViewHolder> {
        private List<ImageItem> images;
        private LayoutInflater mInflater;
        MyRecyclerViewAdapter(Context context, ArrayList<ImageItem> images) {
            this.mInflater = LayoutInflater.from(context);
            this.images = images;
        }
        @Override
        @NonNull
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = mInflater.inflate(R.layout.item_image, parent, false);
            return new ViewHolder(view);
        }
        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            final ImageItem image = images.get(position);
            Picasso.get().load(image.value).into(holder.image);
            holder.remove.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FirebaseDatabase.getInstance().getReference("users").child(Uid).child("images").child(image.key).removeValue();
                }
            });
        }
        @Override
        public int getItemCount() {
            return images.size();
        }
        public class ViewHolder extends RecyclerView.ViewHolder {
            ImageView image, remove;
            ViewHolder(View itemView) {
                super(itemView);
                image = itemView.findViewById(R.id.image);
                remove = itemView.findViewById(R.id.remove);
            }
        }
    }
    public class ImageItem {
        String key, value;
        public ImageItem(String key, String value) {
            this.key = key;
            this.value = value;
        }
    }
}
