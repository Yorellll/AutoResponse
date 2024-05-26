package com.example.automaticresponse;

import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.ContactsContract;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

//Ici la class SpamService qui gère l'envoi des messages (spam et envois automatique)
public class SpamService extends Service {

    private final IBinder binder = new LocalBinder();

    private boolean autoResponseEnabled = false;

    private SmsReceiver smsReceiver;
    private String selectedAutoResponseMessage = null;



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
            //Une fois le message envoyé on fait apparatre un toast afin de prévenir qu'il a été envoyé
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

    //Cette méthode est appelé quand on coche ou décoche la checkbox qui active ou désactive la réponse automatique
    public void setAutoResponseEnabled(boolean enabled) {
        autoResponseEnabled = enabled;
    }

    //Cette méthode gère ce qui permet d'enregistrer un récepteur de SMS
    //Celui sera déclencher à la reception d'un SMS
    private void registerSmsReceiver() {
        if (smsReceiver == null) {
            smsReceiver = new SmsReceiver();
            IntentFilter filter = new IntentFilter("android.provider.Telephony.SMS_RECEIVED");
            registerReceiver(smsReceiver, filter);
        }
    }


    //Permet de reset le recepteur de SMS
    private void unregisterSmsReceiver() {
        if (smsReceiver != null) {
            unregisterReceiver(smsReceiver);
            smsReceiver = null;
        }
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //On vérifie si la checkbox de réponse automatique est activé
        if (autoResponseEnabled) {
            //Avec le registerSmsReceiver, on attend l'arrivé de sms
            Log.d(TAG, "Service started, autoResponseEnabled: " + autoResponseEnabled);
            registerSmsReceiver();
        }
        //Si jamais le service est kill ou s'arrête, il est redémarré
        return START_STICKY;
    }


    //Quand le service est détruit on appelle cette fonction qui permet de reset le recepteur de SMS
    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterSmsReceiver();
    }

    //Ici on créé une class SmsReceiver, celle ci gère la réponse aux sms reçu
    public class SmsReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            //Ici on vérifie que l'intent est bien l'intent de reception de sms
            if (intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")) {
                Bundle bundle = intent.getExtras();
                if (bundle != null) {
                    //On met le sms que l'on extrait dans un objet
                    Object[] pdus = (Object[]) bundle.get("pdus");
                    if (pdus != null) {
                        for (Object pdu : pdus) {
                            //On convertit l'objet précédent en SmsMessage
                            SmsMessage message = SmsMessage.createFromPdu((byte[]) pdu, android.provider.Telephony.Sms.Intents.SMS_RECEIVED_ACTION);
                            //Ici à l'aide de getDisplayOriginatingAddress() on récupère le numéro de l'envoyeur
                            String sender = message.getDisplayOriginatingAddress();
                            //Enfin on appelle ma méthode responToMessage pour envoyé un message à l'envoyeur
                            if (autoResponseEnabled) {
                                respondToMessage(sender);
                            }
                        }
                    }
                }
            }
        }

        //Ici la méthode qui envoit le message à l'envoyeur
        private void respondToMessage(String sender) {
            //Ici on créé un intent qui vient chercher les intent du SpamActivity afin de récupérer le message automatique séléctionné avant de l'envoyer
            Intent intent = new Intent(getApplicationContext(), SpamActivity.class);
            intent.putExtra("autoResponseMessage", selectedAutoResponseMessage);
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(sender, null, selectedAutoResponseMessage, null, null);
            Log.d(TAG, "Auto-response sent to: " + sender);
        }
    }


}
