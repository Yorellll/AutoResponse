package com.example.automaticresponse;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

//Cette class étendu de recycler view permet d'afficher l'ensemble des messages
public class MessageRecyclerAdapter extends RecyclerView.Adapter<MessageRecyclerAdapter.MessageViewHolder> {

    //Ici on set une List qui contiendra l'ensemble des messages
    private final List<String> messageList;

    //Ici on set un click listener pour les messages
    private final OnItemClickListener listener;


    private int selectedAutoResponsePosition = -1;
    private int selectedSpamPosition = -1;

    //Ici on définit le click listener, celui ci permet définir la manière l'utilisateur pourra intéragir avec les messages
    //On appelle dedans les méthodes nécéssaire au bon déroulement des intéractions
    public interface OnItemClickListener {
        void onItemClick(int position);
        void onAutoResponseCheck(int position, boolean isChecked);
        void onSpamCheck(int position, boolean isChecked);
    }

    //Ici le constructeur de la class
    public MessageRecyclerAdapter(List<String> messageList, OnItemClickListener listener) {
        this.messageList = messageList;
        this.listener = listener;
    }


    //Ici on définit ce que se passera à la création du ViewHolder
    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.content_message, parent, false);
        return new MessageViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        //On vient chercher le message dans list selon la position donnée
        String message = messageList.get(position);
        //On vient set le text du message
        holder.textViewMessage.setText(message);

        //On set les listener à null afin de ne pas avoir de problème avec les anciens listener
        holder.checkBoxAutoResponse.setOnCheckedChangeListener(null);
        holder.checkBoxSpam.setOnCheckedChangeListener(null);

        //On set l'état des checkbox en fonction de leurs position
        holder.checkBoxAutoResponse.setChecked(position == selectedAutoResponsePosition);
        holder.checkBoxSpam.setChecked(position == selectedSpamPosition);

        //Ici pour les checkbox réponse automatique, on ajoute un nouveau listener
        //L'ensemble de ce listener permet de gérer le fait qu'une seule checkbox parmis toutes
        //les checkbox réponse auto, puisse être check
        holder.checkBoxAutoResponse.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                //Si c'est check, on update la position de l'item séléctioné
                int previousPosition = selectedAutoResponsePosition;
                selectedAutoResponsePosition = position;
                //Permet de notifier que la nouvelle position à changé
                notifyItemChanged(previousPosition);
                notifyItemChanged(selectedAutoResponsePosition);

                //Définit le fait que ce soit la nouvelle position pour le listener
                listener.onAutoResponseCheck(position, true);
            } else if (selectedAutoResponsePosition == position) {
                //Réinitialise la position de la checkbox si elle est désélectionner
                selectedAutoResponsePosition = -1;

                //Permet de notifier que la nouvelle position à changé
                notifyItemChanged(position);

                //Définit le fait que ce soit la nouvelle position pour le listener
                listener.onAutoResponseCheck(position, false);
            }
        });

        //Pareil pour les checkbox lié au spam
        holder.checkBoxSpam.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                int previousPosition = selectedSpamPosition;
                selectedSpamPosition = position;
                notifyItemChanged(previousPosition);
                notifyItemChanged(selectedSpamPosition);
                listener.onSpamCheck(position, true);
            } else if (selectedSpamPosition == position) {
                selectedSpamPosition = -1;
                notifyItemChanged(position);
                listener.onSpamCheck(position, false);
            }
        });

    }

    //Cette méthode renvoit simplement le taille de la list de message
    @Override
    public int getItemCount() {
        return messageList.size();
    }


    //Ici le ViewHolder pour les messages
    class MessageViewHolder extends RecyclerView.ViewHolder {

        //On définit les éléments
        TextView textViewMessage;
        CheckBox checkBoxAutoResponse;
        CheckBox checkBoxSpam;
        ImageView imageViewDelete;

        //Ici le constructeur du ViewHolder
        MessageViewHolder(View itemView) {
            super(itemView);
            textViewMessage = itemView.findViewById(R.id.textView_message);
            checkBoxAutoResponse = itemView.findViewById(R.id.checkBox_auto_response);
            checkBoxSpam = itemView.findViewById(R.id.checkBox_spam);
            imageViewDelete = itemView.findViewById(R.id.imageView_delete);

            //Ici le listener pour l'icone qui permet de supprimer le message
            imageViewDelete.setOnClickListener(v -> listener.onItemClick(getAdapterPosition()));
        }
    }
}
