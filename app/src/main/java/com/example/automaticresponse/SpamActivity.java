package com.example.automaticresponse;

import static android.content.ContentValues.TAG;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.ContactsContract;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Spinner;


import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

public class SpamActivity extends AppCompatActivity {

    private Spinner contactSpinner;
    private CheckBox autoResponseCheckBox;
    private SpamService spamService;
    private boolean isBound = false;

    private String spamMessage;
    private String autoResponseMessage;

    //Méthode pour vérifier sur la permission d'envoyer des sms est accordé
    public boolean isSendPermissionGranted() {
        // Return true if user has given his permission to send SMS
        return ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED;
    }

    //Méthode pour demander la permission d'envoyer des sms
    public void requestSendSMS() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.SEND_SMS)) {
            return;
        }
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, 0);

    }

    //Méthode pour vérifier sur la permission de lire les sms est accordé
    public boolean isReadPermissionGranted() {
        // Return true if user has given his permission to read SMS
        return ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED;
    }

    //Méthode pour demander la permission de lire les sms
    public void requestReadSMS() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.READ_SMS)) {
            return;
        }
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_SMS}, 0);

    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spam);

        //Ici on commence par vérifier si les permissions sont accordés
        if (!isSendPermissionGranted()) {
            requestSendSMS();
        }

        if (!isReadPermissionGranted()) {
            requestReadSMS();
        }
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

        //Ici on set le contactSpinner avec les contact du téléphone
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
            if (isBound) {
                spamService.setAutoResponseEnabled(isChecked);
            }
        });
    }

    //Ici comme la tab 1 on va chercher les contacts du téléphone
    private List<String> getContacts() {
        // Replace this with actual logic to get contacts from the phone
        ArrayList<String> contacts = new ArrayList<String>();
        ArrayList<String> contactNumber = new ArrayList<String>();

        ContentResolver resolver = getContentResolver();
        Uri uri = ContactsContract.Contacts.CONTENT_URI; // Provider natif Android pour les informations relatives aux contacts
        Cursor cursor = resolver.query(uri, null, null, null);
        // On va maintenant parcourir la base de données tout en récupérant le nom des contacts
        // et en l'ajoutant à notre RecyclerView
        while (cursor.moveToNext()) {
            @SuppressLint("Range") String displayName = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
            @SuppressLint("Range") int hasPhone = cursor.getInt(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));
            if (hasPhone > 0) {
                // Le contact a un numéro de téléphone, récupérons-le
                @SuppressLint("Range") Cursor phoneCursor = resolver.query(
                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        null,
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                        new String[]{String.valueOf(cursor.getLong(cursor.getColumnIndex(ContactsContract.Contacts._ID)))},
                        null
                );

                // Itérer sur les numéros de téléphone associés à ce contact
                if (phoneCursor != null) {
                    while (phoneCursor.moveToNext()) {
                        @SuppressLint("Range") String phoneNumber = phoneCursor.getString(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                        // Ajoutez le numéro de téléphone à votre liste ou faites autre chose avec
                        contactNumber.add(phoneNumber);
                    }
                    phoneCursor.close();
                }
            }
            // Ajoutez le nom du contact à votre liste
            contacts.add(displayName);
        }
        cursor.close();

        return contacts;
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(this, SpamService.class);
        bindService(intent, connection, BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (isBound) {
            unbindService(connection);
            isBound = false;
        }
    }

    //Ici on définit le service qui sert à répondre automatiquement aux messages reçu même quand l'app est éteinte
    private final ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            SpamService.LocalBinder binder = (SpamService.LocalBinder) service;
            spamService = binder.getService();
            isBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isBound = false;
        }
    };
}
