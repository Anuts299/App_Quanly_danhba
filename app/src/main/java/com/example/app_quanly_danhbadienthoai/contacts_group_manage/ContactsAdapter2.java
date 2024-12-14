package com.example.app_quanly_danhbadienthoai.contacts_group_manage;

import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.app_quanly_danhbadienthoai.R;
import com.example.app_quanly_danhbadienthoai.contacts_manage.Contacts;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ContactsAdapter2 extends RecyclerView.Adapter<ContactsAdapter2.ContactsViewHolder>{

    private List<Contacts> mListContacts;


    public ContactsAdapter2(List<Contacts> mListContacts) {
        this.mListContacts = mListContacts;
    }

    @NonNull
    @Override
    public ContactsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.recycler_item, viewGroup, false);
        return new ContactsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ContactsViewHolder contactsViewHolder, int i) {
        Contacts contacts = mListContacts.get(i);
        if (contacts == null) {
            return;
        }
        contactsViewHolder.tv_item1.setText(contacts.getTen_lien_he());
        contactsViewHolder.tv_item2.setText(contacts.getSo_dien_thoai());
        contactsViewHolder.tv_item3.setText(contacts.getId());
    }

    @Override
    public int getItemCount() {
        return (mListContacts != null) ? mListContacts.size() : 0;
    }


    public class ContactsViewHolder extends RecyclerView.ViewHolder{

        private TextView tv_item1, tv_item2, tv_item3;


        public ContactsViewHolder(@NonNull View itemView) {
            super(itemView);
            tv_item1 = itemView.findViewById(R.id.tv_item1);
            tv_item2 = itemView.findViewById(R.id.tv_item2);
            tv_item3 = itemView.findViewById(R.id.tv_item3);
        }
    }
}
