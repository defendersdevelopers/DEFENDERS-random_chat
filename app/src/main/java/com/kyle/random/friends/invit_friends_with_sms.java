package com.kyle.random.friends;

import com.kyle.random.other.BaseActivity;

import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.widget.GridView;

import com.kyle.random.R;

import java.util.ArrayList;

public class invit_friends_with_sms extends BaseActivity {
ArrayList<Contact> contacts;
    contactsAdapter contactsAdapter;
    GridView grid_view;
    ContentProviderClient mCProviderClient;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.invit_friends_with_sms);
        contacts=new ArrayList<>();
        contactsAdapter=new contactsAdapter(this,contacts);
        grid_view=findViewById(R.id.grid_view);
        grid_view.setAdapter(contactsAdapter);
    //    readContacts();
        ContentResolver cResolver=invit_friends_with_sms.this.getApplicationContext().getContentResolver();
         mCProviderClient = cResolver.acquireContentProviderClient(ContactsContract.Contacts.CONTENT_URI);
        getContacts();
    }
    private void  getContacts(){
  final String[] PROJECTION = new String[] {
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
                ContactsContract.Contacts.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER
        };
        ContentResolver cr = getContentResolver();
        Cursor cursor = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, PROJECTION, null, null, null);
        if (cursor != null) {
            try {
                final int nameIndex = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);
                final int numberIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);

                String name, number;
                while (cursor.moveToNext()) {
                    name = cursor.getString(nameIndex);
                    number = cursor.getString(numberIndex);
                    contacts.add(new Contact(name,number,false));
                }
                contactsAdapter.notifyDataSetChanged();
            } finally {
                cursor.close();
            }
        }
    }
    public void readContacts(){
        ContentResolver cr = getContentResolver();
        Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI,
                null, null, null, null);

        if (cur.getCount() > 0) {
            while (cur.moveToNext()) {
                String id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID));
                String name = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                String phone = "";
                if (Integer.parseInt(cur.getString(cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
                    System.out.println("name : " + name + ", ID : " + id);

                    // get the phone number
                    Cursor pCur = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = ?",
                            new String[]{id}, null);
                    while (pCur.moveToNext()) {
                         phone = pCur.getString(
                                pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                        System.out.println("phone" + phone);
                    }
                    pCur.close();
                    contacts.add(new Contact(name,phone,false));



                }
            }
            contactsAdapter.notifyDataSetChanged();
        }

    }


}