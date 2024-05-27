package com.example.automaticresponse;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSIONS_REQUEST_CODE = 1;

    //Liste qui contient les contacts
    ArrayList<String> contacts = new ArrayList<>();

    //Liste qui contient les numéros des contacts
    ArrayList<String> contactNumber = new ArrayList<>();

    //Adapter pour gérer l'affichage des contacts dans le RecyclerView
    ContactRecyclerAdapter adapter = new ContactRecyclerAdapter(contacts, contactNumber);

    //Méthode pour vérifier si les permissions nécessaires sont accordées
    public boolean permissionsAccepted() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED;
    }

    //Méthode pour demander les permissions
    public void askPermissions() {
        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.READ_CONTACTS,
                Manifest.permission.READ_SMS,
                Manifest.permission.RECEIVE_SMS,
                Manifest.permission.SEND_SMS
        }, PERMISSIONS_REQUEST_CODE);
    }

    //Méthode appelée lorsque l'utilisateur répond à la demande de permissions
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //Si on a la permission des contacts, on les load
                loadContacts();
            } else {
                //Si la permission est refusée, on affiche un toast et on redemande
                Toast.makeText(this, "Merci, d'accepter les permissions pour utiliser l'application.", Toast.LENGTH_LONG).show();
                askPermissions();
            }
        }
    }

    //Méthode pour charger les contacts
    private void loadContacts() {
        //Ici on vide les listes afin d'être sur qu'aucun élément ne traine dedans
        contacts.clear();
        contactNumber.clear();

        //Ici on set la requete
        ContentResolver resolver = getContentResolver();
        Uri uri = ContactsContract.Contacts.CONTENT_URI;
        Cursor cursor = resolver.query(uri, null, null, null, null);

        //Ici on parcours la requete et on vient chercher le nom et le numéro du contact s'il y en a un
        if (cursor != null) {
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
        }

        adapter.notifyDataSetChanged();
    }

    //Méthode appelée à la création de MainActivity
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Configurer le RecyclerView
        RecyclerView rc_view = findViewById(R.id.recyclerContact);
        rc_view.setLayoutManager(new LinearLayoutManager(this));
        rc_view.setAdapter(adapter);

        //Ici on appelle les méthodes pour demander les permissions et pour charger les contacts
        if (permissionsAccepted()) {
            loadContacts();
        } else {
            askPermissions();
        }

        //Ici, on définit le titre de la vue
        TextView titleContact = findViewById(R.id.title_contact);
        titleContact.setText(R.string.contact);

        //Ici, on ajoute un event listener au bouton pour passer à la tab 2 (gestion des messages
        Button button = findViewById(R.id.message_button);
        button.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), MessageActivity.class);
            startActivity(intent);
        });
    }
}

