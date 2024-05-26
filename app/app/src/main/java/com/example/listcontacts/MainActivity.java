package com.example.listcontacts;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.ContentResolver;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    ArrayList<String> contacts = new ArrayList<String>();
    ContactRecyclerAdapter adapter = new ContactRecyclerAdapter(contacts);

    public boolean isPermissionGranted() {
        // Return true if user has given his permission to read incoming messages
        return ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED;
    }

    public void requestContactPermission() {
        // Ask user for his permission to read incoming messages
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.READ_CONTACTS)) {
            return;
        }
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS}, 0);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        RecyclerView rc_view = findViewById(R.id.recycler);
        rc_view.setLayoutManager(new LinearLayoutManager(this));
        rc_view.setAdapter(adapter);

        if (!isPermissionGranted()) {
            requestContactPermission();
        }

        // Requête
        ContentResolver resolver = getContentResolver();
        Uri uri = ContactsContract.Contacts.CONTENT_URI; // Provider natif Android pour les informations relatives aux contacts
        Cursor cursor = resolver.query(uri, null, null, null);
        // On va maintenant parcourir la base de données tout en récupérant le nom des contacts
        // et en l'ajoutant à notre RecyclerView
        while (cursor.moveToNext()) {
            Integer column_index = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);
            contacts.add(cursor.getString(column_index));
            adapter.notifyDataSetChanged();
        }


    }
}