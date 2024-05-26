package com.example.automaticresponse;

import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import org.w3c.dom.Text;

import java.util.ArrayList;


//Cette class étendu de recycler view permet d'afficher l'ensemble des contacts et leurs numéro sur le layout contact
public class ContactRecyclerAdapter extends RecyclerView.Adapter<ContactRecyclerAdapter.ViewHolder> {

    //Cette List de string permet de stocker les Contact
    ArrayList<String> contactDataset;

    //Cette List de string permet de stocker les numéros des contact
    ArrayList<String> phoneNumberDataSet;


    //Ce tableau permet de stocker les contacts séléctionné sur l'écran d'accueil
    private final SparseBooleanArray selectedContact = new SparseBooleanArray();

    //Ici le constructeur de la class
    public ContactRecyclerAdapter(ArrayList<String> dataset, ArrayList<String> phoneNumber) {
        contactDataset = dataset;
        phoneNumberDataSet = phoneNumber;
    }

    //Dans ce ViewHolder on définis ce que contiendra chaque élément de la vue
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView contact;
        private final TextView phoneNumber;
        private final CheckBox checkBox;

        public ViewHolder(View v) {
            super(v);
            contact = (TextView) itemView.findViewById(R.id.contact_view);
            phoneNumber = (TextView) itemView.findViewById(R.id.phoneNumber_view);
            checkBox = (CheckBox) itemView.findViewById(R.id.checkbox_contact);

        }

        public TextView getContactTextView() {
            return contact;
        }

        public TextView getPhoneNumberTextView() {
            return phoneNumber;
        }

        public CheckBox getCheckBox() {
            return checkBox;
        }
    }

    //Ici on définit ce que se passera à la création du ViewHolder
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType){
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.contact_layout, viewGroup, false);
        return new ViewHolder(view);
    }


    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int pos){
        //Cette partie gère les noms des contacts et leurs numéros
        viewHolder.getContactTextView().setText(contactDataset.get(pos));
        if (pos < phoneNumberDataSet.size()) {
            viewHolder.getPhoneNumberTextView().setText(phoneNumberDataSet.get(pos));
        } else {
            viewHolder.getPhoneNumberTextView().setText("");
        }

        //Cette partie gère les check box, on définit ce qui sera fait à leurs changement d'état
        //Au changement d'état du check, on ajoute ou retire le contact de la list des contacts séléctionés
        viewHolder.getCheckBox().setChecked(selectedContact.get(pos, false));
        viewHolder.getCheckBox().setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                selectedContact.put(pos, true);
            } else {
                selectedContact.delete(pos);
            }
        });
    }

    //Cette méthode renvoit simplement la taille de la liste contenant les contacts
    @Override
    public int getItemCount() {
        return contactDataset.size();
    }

}
