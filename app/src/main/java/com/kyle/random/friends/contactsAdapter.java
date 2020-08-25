package com.kyle.random.friends;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.telephony.SmsManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.kyle.random.R;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;
import es.dmoral.toasty.Toasty;
import mehdi.sakout.fancybuttons.FancyButton;

import static com.kyle.random.other.Libs.getUserId;
class contactsAdapter extends BaseAdapter {
    Context context;
    ArrayList<Contact> contacts;
    String myName="",myUid;

    public contactsAdapter(Context context, ArrayList<Contact> contacts) {
        this.context = context;
        this.contacts = contacts;
        myUid = getUserId();
        myName = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
    }

    @Override
    public int getCount() {
        return contacts.size();
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
    public View getView(final int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        assert inflater != null;
        @SuppressLint("ViewHolder") View gridView = inflater.inflate(R.layout.item_contact, parent, false);
        final Contact contact = contacts.get(position);
        CircleImageView profileImage = gridView.findViewById(R.id.profileImage);
        TextView name = gridView.findViewById(R.id.name);
        TextView phone = gridView.findViewById(R.id.phone);
        name.setText(contact.name);
        phone.setText(contact.phone);
        FancyButton fancy = gridView.findViewById(R.id.fancy);
        fancy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!contact.invitedBefoer) {
                    sendSMS(contact.phone,context.getString(R.string.id_212)+myName+context.getString(R.string.id_211));
                    contact.invitedBefoer=true;
                }
            }
        });
        TextView inviteButtonText = gridView.findViewById(R.id.inviteButtonText);
        inviteButtonText.setText(contact.invitedBefoer ? context.getString(R.string.id_213) : context.getString(R.string.id_214));
        fancy.setBackgroundColor(contact.invitedBefoer ? Color.parseColor("#A1A1A1") : Color.parseColor("#29b6f6"));
        fancy.setTextColor(contact.invitedBefoer ? Color.parseColor("#A1A1A1") : Color.parseColor("#29b6f6"));
        return gridView;
    }

    public void sendSMS(String phoneNo, String msg) {
        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNo, null, msg, null, null);
            Toasty.success(context , R.string.id_215, Toast.LENGTH_SHORT, true).show();
        } catch (Exception ex) {
            Toasty.info(context, ex.getMessage() == null ? context.getString(R.string.id_216) : ex.getMessage(), Toast.LENGTH_SHORT, true).show();
            ex.printStackTrace();
        }
    }
}
