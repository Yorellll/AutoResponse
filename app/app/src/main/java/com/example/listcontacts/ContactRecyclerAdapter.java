package com.example.listcontacts;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class ContactRecyclerAdapter extends RecyclerView.Adapter<ContactRecyclerAdapter.ViewHolder> {
    ArrayList<String> contactDataset;
    public ContactRecyclerAdapter(ArrayList<String> dataset) {
        contactDataset = dataset;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView contact;

        public ViewHolder(View v) {
            super(v);
            contact = (TextView) itemView.findViewById(R.id.contact_view);

        }

        public TextView getTextView() {
            return contact;
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType){
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.contact_layout, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int pos){
        viewHolder.getTextView().setText(contactDataset.get(pos));
    }

    @Override
    public int getItemCount() {
        return contactDataset.size();
    }

}
