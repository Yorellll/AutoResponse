package com.example.automaticresponse;

import static android.content.ContentValues.TAG;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Log;

public class SmsReceiver extends BroadcastReceiver {

    //Ici on vient chercher le message choisit dans la tab 2, dans les données enregistrer sur le téléphone
    private String getAutoResponseMessage(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getString("reponseAutoMessage", "Ce message est envoyé automatiquement, je vous réponds dés que possible");
    }

    //Ici on vient chercher l'état de la checkbox autoresponse, stocké en mémoire
    private boolean getAutoResponseCheckBoxState(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getBoolean("reponseAutoCheck", false);
    }

    //Ici, la méthode est appellé quand on reçoit un message
    @Override
    public void onReceive(Context context, Intent intent) {
        if (getAutoResponseCheckBoxState(context)) {
            //On vérifie si l'intent est celui de réception de SMS
            if (intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")) {
                Log.println(Log.ASSERT, "Test", "On vient de recevoir un sms ");
                Bundle bundle = intent.getExtras();
                if (bundle != null) {
                    //On récupère les PDUs du SMS
                    Object[] pdus = (Object[]) bundle.get("pdus");
                    if (pdus != null) {
                        for (Object pdu : pdus) {
                            //On convertit l'objet PDU en SmsMessage
                            SmsMessage message = SmsMessage.createFromPdu((byte[]) pdu);
                            if (message != null){
                                //On récupère le numéro de l'expéditeur
                                String sender = message.getDisplayOriginatingAddress();
                                //On appelle la méthode pour répondre au message
                                respondToMessage(sender, context);
                            }
                        }
                    }
                }
            }
        }
    }

    //Ici la méthode qui envoit le message à l'envoyeur
    private void respondToMessage(String sender, Context context) {
        //Ici on envoit un message, à l'envoyeur, on va chercher le message choisit précédement grace à la méthode décrite au dessus
        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage(sender, null, getAutoResponseMessage(context), null, null);
        Log.d(TAG, "Auto-response sent to: " + sender);
    }
}
