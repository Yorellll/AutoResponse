package com.example.automaticresponse;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    //Ici la list qui contient les contacts
    ArrayList<String> contacts = new ArrayList<String>();

    //Ici la liste qui contient les numéros des contacts
    ArrayList<String> contactNumber = new ArrayList<String>();

    //Ici on définit l'adapter que l'on a créé pour gérer l'affichage des contacts (ContactRecyclerAdapter)
    //On lui transmet en paramètre les contacts ainsi que leurs numéros que l'on a été récupéré dans le create
    ContactRecyclerAdapter adapter = new ContactRecyclerAdapter(contacts, contactNumber);

    //Ici on définit la métode qui vient vérifié si la permission de lire les contacts du téléphone est accordé
    public boolean isPermissionGranted() {
        return ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED;
    }

    //Ici on définit la méthode qui demande la permission de lire les contacts
    public void requestContactPermission() {
        // Ask user for his permission to read incoming messages
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.READ_CONTACTS)) {
            return;
        }
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS}, 0);
    }

//Ici on définit ce qui sera fait à la création de notre activité
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //On set la vue courante
        setContentView(R.layout.activity_main);
        //On set le contenu du recycler de la vue
        RecyclerView rc_view = findViewById(R.id.recyclerContact);
        rc_view.setLayoutManager(new LinearLayoutManager(this));
        rc_view.setAdapter(adapter);

        //On demande la permission de lire les contacts avec la méthode précédente
        if (!isPermissionGranted()) {
            requestContactPermission();
        }

        //On définit un textview "title_contact " sur "Contact" (c'est le titre de la vue)
        TextView titleContact = findViewById(R.id.title_contact);
        titleContact.setText(R.string.contact);

        //Ici on fait la requete au téléphone pour aller chercher les contact et leurs numéros
        ContentResolver resolver = getContentResolver();
        Uri uri = ContactsContract.Contacts.CONTENT_URI; // Provider natif Android pour les informations relatives aux contacts
        Cursor cursor = resolver.query(uri, null, null, null);
        // On va maintenant parcourir la base de données tout en récupérant le nom des contacts
        // et en l'ajoutant à notre RecyclerView
        while (cursor.moveToNext()) {
            String displayName = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
            int hasPhone = cursor.getInt(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));
            if (hasPhone > 0) {
                // Le contact a un numéro de téléphone, récupérons-le
                Cursor phoneCursor = resolver.query(
                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        null,
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                        new String[]{String.valueOf(cursor.getLong(cursor.getColumnIndex(ContactsContract.Contacts._ID)))},
                        null
                );

                // Itérer sur les numéros de téléphone associés à ce contact
                if (phoneCursor != null) {
                    while (phoneCursor.moveToNext()) {
                        String phoneNumber = phoneCursor.getString(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                        // On ajoute le numéro du contact si existant à la liste dédié
                        contactNumber.add(phoneNumber);
                    }
                    phoneCursor.close();
                }
            }
            //On ajoute le nom du contact à la list dédié
            contacts.add(displayName);
        }
        // Ici on vient notifier si jamais les contact / numéros sont modifi, cette ligne est présente afin de toujours avoir
        //les donées les plus récente en cas de modification
        adapter.notifyDataSetChanged();
        cursor.close();

        //On ajoute l'event listener au boutton pour passer à la tab 2 (gestion des messages)
        Button button = findViewById(R.id.message_button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), MessageActivity.class);
                startActivity(intent);
            }
        });
    }
}