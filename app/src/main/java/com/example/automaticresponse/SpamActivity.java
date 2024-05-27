package com.example.automaticresponse;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Spinner;


import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;

import java.util.ArrayList;
import java.util.List;

public class SpamActivity extends AppCompatActivity {

    private Spinner contactSpinner;
    private CheckBox autoResponseCheckBox;
    private SpamService spamService;
    private boolean isBound = false;

    private String spamMessage;
    private String autoResponseMessage;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spam);

        //Ici on vient chercher le spamMessage séléctionné à la tab précédente
        Intent intent = getIntent();
        spamMessage = intent.getStringExtra("spamMessage");

        //Ici on paramètre la toolbar pour revenir sur l'écran précédent
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Gestion spam");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        contactSpinner = findViewById(R.id.contact_spinner);
        Button spamContactButton = findViewById(R.id.spam_contact_button);
        autoResponseCheckBox = findViewById(R.id.toggle_auto_response);

        //Ici on set le contactSpinner avec les contacts du téléphone
        List<String> contacts = getContacts();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, contacts);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        contactSpinner.setAdapter(adapter);

        //Ici on set l'action au click du bouton de spam, c'est a dire qu'on envoit le message spam au contact séléctionné
        spamContactButton.setOnClickListener(v -> {
            String selectedContact = (String) contactSpinner.getSelectedItem();
            if (isBound) {
                spamService.sendSpam(selectedContact, spamMessage);
            }else {
                Log.println(Log.ASSERT, "Message", "service non run");

            }
        });

        //Ici on set l'action à l'activation / désactivation de la checkbox
        autoResponseCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            //Ici on gérer l'activation et désactivation de la réponse automatique, en stoquant son état dans le téléphone
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("reponseAutoCheck", isChecked);
            editor.apply();
        });
    }

    //Ici comme la tab 1 on va chercher les contacts du téléphone
    private List<String> getContacts() {

        ArrayList<String> contacts = new ArrayList<String>();
        ArrayList<String> contactNumber = new ArrayList<String>();

        ContentResolver resolver = getContentResolver();
        Uri uri = ContactsContract.Contacts.CONTENT_URI; // Provider natif Android pour les informations relatives aux contacts
        Cursor cursor = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            cursor = resolver.query(uri, null, null, null);
        }
        while (cursor.moveToNext()) {
            @SuppressLint("Range") String displayName = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
            @SuppressLint("Range") int hasPhone = cursor.getInt(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));
            if (hasPhone > 0) {
                @SuppressLint("Range") Cursor phoneCursor = resolver.query(
                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        null,
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                        new String[]{String.valueOf(cursor.getLong(cursor.getColumnIndex(ContactsContract.Contacts._ID)))},
                        null
                );

                if (phoneCursor != null) {
                    while (phoneCursor.moveToNext()) {
                        @SuppressLint("Range") String phoneNumber = phoneCursor.getString(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                        contactNumber.add(phoneNumber);
                    }
                    phoneCursor.close();
                }
            }
            contacts.add(displayName);
        }
        cursor.close();

        return contacts;
    }

    //Ici on connecte le spam service au démarrage de SpamActivity
    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(this, SpamService.class);
        bindService(intent, connection, BIND_AUTO_CREATE);
    }

    //Ici on déconnecte le service à l'arrêt de SpamActivity
    @Override
    protected void onStop() {
        super.onStop();
        if (isBound) {
            unbindService(connection);
            isBound = false;
        }
    }

    //Ici on définit un nouveau serviceConnection, ce code permet de se connecter à un service, ici SpamService créer en amont
    private final ServiceConnection connection = new ServiceConnection() {

        //On appelle onServiceConnected quand le service est connecté
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            SpamService.LocalBinder binder = (SpamService.LocalBinder) service;
            spamService = binder.getService();
            isBound = true;
        }

        //Celle ci quand le service est deconnecté
        @Override
        public void onServiceDisconnected(ComponentName name) {
            isBound = false;
        }
    };
}
