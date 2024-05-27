package com.example.automaticresponse;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class MessageActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private MessageRecyclerAdapter adapter;
    private List<String> messageList;

    private String selectedSpamMessage = null;
    private String selectedAutoResponseMessage = null;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        //Ici on initialise le recyclerView et on définit son layout
        recyclerView = findViewById(R.id.recycler_view_messages);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        //Ici on appelle la méthode qui vient chercher les messages enregistrés sur le téléphone
        loadMessages();

        //Ici on set des messages prédéfinis, tout en vérifiant si ils y sont déjà pour ne pas avoir de doublon
        if (!isMessageAlreadyAdded("Merci de me rappeller plus tard")) {
            messageList.add("Merci de me rappeller plus tard");
        }
        if (!isMessageAlreadyAdded("Désolé, je suis en rendez-vous")) {
            messageList.add("Désolé, je suis en rendez-vous");
        }
        if (!isMessageAlreadyAdded("Je vous rappelle vite")) {
            messageList.add("Je vous rappelle vite");
        }

        //Ici on initialise l'adapter et on lui transmet le nécéssaire en paramètre
        adapter = new MessageRecyclerAdapter(messageList, new MessageRecyclerAdapter.OnItemClickListener() {

            //Méthode qui permet de supprimer un message de la list
            @Override
            public void onItemClick(int position) {
                messageList.remove(position);
                adapter.notifyItemRemoved(position);
                saveMessages();
            }

            //Méthode qui permet de définir ce qu'il se passe quand une checkbox auto réponse est check
            @Override
            public void onAutoResponseCheck(int position, boolean isChecked) {
                if (isChecked) {
                    selectedAutoResponseMessage = messageList.get(position);
                } else if (selectedAutoResponseMessage.equals(messageList.get(position))) {
                    selectedAutoResponseMessage = null;
                }
            }

            //Pareil pour les checkbox spam
            @Override
            public void onSpamCheck(int position, boolean isChecked) {
                if (isChecked) {
                    selectedSpamMessage = messageList.get(position);
                } else if (selectedSpamMessage.equals(messageList.get(position))) {
                    selectedSpamMessage = null;
                }
            }
        });

        //Ici on assigne la recycler à l'adapter
        recyclerView.setAdapter(adapter);

        //Ici on vient chercher les éléments du layout qui servent à créer des messages
        EditText editTextCustomMessage = findViewById(R.id.edit_text_custom_message);
        Button buttonAdd = findViewById(R.id.button_add);

        //On ajoute le listener pour ajouter un message
        buttonAdd.setOnClickListener(v -> {
            String customMessage = editTextCustomMessage.getText().toString();
            if (!customMessage.isEmpty()) {
                messageList.add(customMessage);
                adapter.notifyItemInserted(messageList.size() - 1);
                editTextCustomMessage.setText("");
                saveMessages();
            }
        });

        //On définit le listener pour aller vers la tab 3 (tab d'envoi de spam)
        Button button = findViewById(R.id.goToSpam_button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Obtient une référence aux préférences partagées en mode édition
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                SharedPreferences.Editor editor = sharedPreferences.edit();

                // Enregistre le message d'auto-réponse sélectionné dans les préférences partagées avec la clé appropriée
                editor.putString("reponseAutoMessage", selectedAutoResponseMessage);

                // Applique les modifications
                editor.apply();

                // Lance l'activité SpamActivity avec les données sélectionnées
                Intent intent = new Intent(getApplicationContext(), SpamActivity.class);
                intent.putExtra("spamMessage", selectedSpamMessage);
                startActivity(intent);
            }
        });


    }



    //Méthode qui permet de sauvegarder les messages sur le téléphone
    private void saveMessages() {
        SharedPreferences sharedPreferences = getSharedPreferences("messages_prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(messageList);
        editor.putString("message_list", json);
        editor.apply();
    }

    //Méthode qui permet de charger les messages sauvegardé sur le téléphone
    private void loadMessages() {
        SharedPreferences sharedPreferences = getSharedPreferences("messages_prefs", MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPreferences.getString("message_list", null);
        Type type = new TypeToken<ArrayList<String>>() {}.getType();
        messageList = gson.fromJson(json, type);

        if (messageList == null) {
            messageList = new ArrayList<>();
        }
    }

    private boolean isMessageAlreadyAdded(String message) {
        return messageList.contains(message);
    }
}