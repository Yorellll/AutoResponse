package com.example.automaticresponse;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.os.Binder;
import android.os.IBinder;
import android.provider.ContactsContract;
import android.telephony.SmsManager;
import android.widget.Toast;

import androidx.annotation.Nullable;

//Ici la class SpamService qui gère l'envoi des messages spam
public class SpamService extends Service {

    private final IBinder binder = new LocalBinder();

//    private boolean autoResponseEnabled = false;
//
//    private SmsReceiver smsReceiver;
//    private final String selectedAutoResponseMessage = null;



    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public class LocalBinder extends Binder {
        SpamService getService() {
            return SpamService.this;
        }
    }

    //Ici la méthode qui gère l'envoi du message spam, on lui donne en paramètre le message séléctionner à la tab précédente et
    //Le contact séléctionner dans le spinner du dessus
    public void sendSpam(String contact, String spamMessage) {
        //Ici on vient chercher le numéro de téléphone du contact donné en paramètre
        String phoneNumber = getPhoneNumberFromContact(contact);
        //On vérifie s'il est null ou non
        if (phoneNumber != null) {
            //Ici si il y a bien un numéro, on envoit le message
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNumber, null, spamMessage, null, null);
            //Une fois le message envoyé on fait apparaitre un toast afin de prévenir qu'il a été envoyé
            Toast.makeText(this, "Spam envoyé à " + contact, Toast.LENGTH_SHORT).show();
        } else {
            //On fait apparaitre un toast prevenant que le message n'a pa été envoyé car il n'y a pas de numéro
            Toast.makeText(this, "Aucun numéro pour " + contact, Toast.LENGTH_SHORT).show();
        }
    }

    //Avec cette méthode on vient chercher le numéro du contact séléctionné
    @SuppressLint("Range")
    private String getPhoneNumberFromContact(String contactName) {
        //On set la variable du numéro et on parcours les contacts pour trouver le numéro
        //Pour finir on le renvoit
        String phoneNumber = null;
        ContentResolver resolver = getContentResolver();
        Cursor cursor = resolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                null,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " = ?",
                new String[]{contactName},
                null);

        if (cursor != null && cursor.moveToFirst()) {
            phoneNumber = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
            cursor.close();
        }
        return phoneNumber;
    }

}
