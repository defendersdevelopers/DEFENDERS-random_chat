package com.kyle.random.getStarted;

import com.kyle.random.KEYS;
import com.kyle.random.other.BaseActivity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.kyle.random.R;
import com.kyle.random.main.fragmentViewer;
import com.google.firebase.database.FirebaseDatabase;
import com.kyle.random.other.Hobby;

import java.util.ArrayList;
import java.util.HashMap;

import es.dmoral.toasty.Toasty;

import static com.kyle.random.other.Libs.getUserId;
public class select_hobbies extends BaseActivity {
    ArrayList<Hobby> items;
TextView text;
boolean isEdit;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_hobbies);
        items =  KEYS.getAllHobbies(this);
        ImageAdapter adapter = new ImageAdapter(select_hobbies.this, items);
        GridView grid = findViewById(R.id.grid_view);
        text=findViewById(R.id.text);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            isEdit = extras.getBoolean("isEdit");
        }
        text.setText(isEdit?getString(R.string.id_243):getString(R.string.id_244));
        text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isEdit){
                    finish();
                }else {
                    startActivity(new Intent(select_hobbies.this, fragmentViewer.class));
                }
            }
        });
        grid.setAdapter(adapter);
        grid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            }
        });


        adapter.notifyDataSetChanged();

    }

    public void next(View view) {
        ArrayList<Hobby> hobbies=getCheckedHobbies();
        HashMap<String,String> hashMap=new HashMap<>();
        if (hobbies.isEmpty()){
            Toasty.info(select_hobbies.this, R.string.id_245, Toast.LENGTH_SHORT, true).show();

        }else {


            for (Hobby hobby:hobbies){
                hashMap.put(String.valueOf(hobby.key),hobby.key);
            }

            FirebaseDatabase.getInstance().getReference("users").child(getUserId()).child("hobbies").setValue(hashMap);
            Skip(null);
        }
    }

    public void Skip(View view) {
        if (isEdit){
            finish();
        }else{
            startActivity(new Intent(this,fragmentViewer.class));
        }
    }

    public class ImageAdapter extends BaseAdapter {
        private Context mContext;
        ArrayList<Hobby> items;

        public ImageAdapter(select_hobbies c, ArrayList<Hobby> items) {
            mContext = c;
            this.items = items;
        }

        @Override
        public int getCount() {
            return items.size();
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
        public View getView(final int position, View convertView, ViewGroup
                parent) {
            LayoutInflater inflater = (LayoutInflater)
                    mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View gridView;
            if (convertView == null) {

                gridView = inflater.inflate(R.layout.item_hobby, null);
                TextView textView = gridView.findViewById(R.id.name);
                final Hobby currentItem = items.get(position);
                textView.setText(currentItem.name);
                final CheckBox checkBox = gridView.findViewById(R.id.checkBox);
                ImageView imageView = gridView.findViewById(R.id.image);
                final LinearLayout item = gridView.findViewById(R.id.item);
                item.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        items.get(position).check = !items.get(position).check;
                        item.setBackgroundColor(items.get(position).check ? Color.parseColor("#0086c3") : Color.parseColor("#000086C3"));
                       checkBox.setChecked(items.get(position).check);
                        notifyDataSetChanged();
                    }
                });
                imageView.setImageResource(currentItem.imageId);

                checkBox.setChecked(currentItem.check);
                checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        items.get(position).check = isChecked;
                        item.setBackgroundColor(isChecked ? Color.parseColor("#0086c3") : Color.parseColor("#000086C3"));
                       notifyDataSetChanged();
                    }
                });


            } else {
                gridView = convertView;
            }
            return gridView;
        }
    }

    public ArrayList<Hobby> getCheckedHobbies() {
        ArrayList<Hobby> CheckedHobbies = new ArrayList<>();
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).check) {
                CheckedHobbies.add(items.get(i));
            }
        }

        return CheckedHobbies;
    }



}
