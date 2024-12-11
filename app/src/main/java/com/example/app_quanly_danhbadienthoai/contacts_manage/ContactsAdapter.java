package com.example.app_quanly_danhbadienthoai.contacts_manage;

import android.annotation.SuppressLint;
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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ContactsAdapter extends RecyclerView.Adapter<ContactsAdapter.ContactsViewHolder>{

    private List<Contacts> mListContacts;
    private IClickListener mIClickListener;

    public interface IClickListener{
        void onClickUpdateItem(Contacts contacts);
        void onClickDeleteItem(Contacts contacts);
    }

    public ContactsAdapter(List<Contacts> mListContacts, IClickListener mIClickListener) {
        this.mListContacts = mListContacts;
        this.mIClickListener = mIClickListener;
    }

    @NonNull
    @Override
    public ContactsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.recycler_item_contacts, viewGroup, false);
        return new ContactsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ContactsViewHolder contactsViewHolder, int i) {
        Contacts contacts = mListContacts.get(i);
        if (contacts == null) {
            return;
        }
        contactsViewHolder.tv_name_contacts.setText(contacts.getTen_lien_he());
        contactsViewHolder.tv_phone_contacts.setText(contacts.getSo_dien_thoai());
        contactsViewHolder.tv_email_contacts.setText(contacts.getEmail());
        contactsViewHolder.tv_locate_contacts.setText(contacts.getDia_chi());
        contactsViewHolder.tv_birthday_contacts.setText(contacts.getNgay_sinh());

        // Lấy đường dẫn tệp hình ảnh từ đối tượng Contacts
        String imagePath = contacts.getAnh();  // Đảm bảo rằng getAnh() trả về đường dẫn tệp hình ảnh đã lưu

        // Thêm log để kiểm tra đường dẫn ảnh
        Log.d("ContactsAdapter", "Image path: " + imagePath);

        if (imagePath != null && !imagePath.isEmpty()) {
            // Chuyển đổi URI 'file://' thành đường dẫn tệp thực sự
            String path = Uri.parse(imagePath).getPath();
            File imgFile = new File(path);
            if (imgFile.exists()) {
                Glide.with(contactsViewHolder.itemView.getContext())
                        .load(imgFile)  // Sử dụng File object để tải hình ảnh
                        .into(contactsViewHolder.recImageContacts);
            } else {
                contactsViewHolder.recImageContacts.setImageResource(R.drawable.img);  // Hình ảnh mặc định nếu không có
            }
        } else {
            contactsViewHolder.recImageContacts.setImageResource(R.drawable.img);  // Hình ảnh mặc định nếu không có đường dẫn
        }
        contactsViewHolder.btn_update_contacts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mIClickListener.onClickUpdateItem(contacts);
            }
        });

        contactsViewHolder.btn_update_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mIClickListener.onClickDeleteItem(contacts);
            }
        });
    }

    @Override
    public int getItemCount() {
        return (mListContacts != null) ? mListContacts.size() : 0;
    }
    public void searchFacultyList(ArrayList<Contacts> searchList){
        mListContacts = searchList;
        notifyDataSetChanged();
    }

    public class ContactsViewHolder extends RecyclerView.ViewHolder{

        private ImageView recImageContacts;
        private TextView tv_name_contacts, tv_phone_contacts, tv_email_contacts, tv_locate_contacts, tv_birthday_contacts;
        private Button btn_update_contacts, btn_update_delete;

        public ContactsViewHolder(@NonNull View itemView) {
            super(itemView);
            recImageContacts = itemView.findViewById(R.id.recImageContacts);
            tv_name_contacts = itemView.findViewById(R.id.tv_name_contacts);
            tv_phone_contacts = itemView.findViewById(R.id.tv_phone_contacts);
            tv_email_contacts = itemView.findViewById(R.id.tv_email_contacts);
            tv_locate_contacts = itemView.findViewById(R.id.tv_locate_contacts);
            tv_birthday_contacts = itemView.findViewById(R.id.tv_birthday_contacts);
            btn_update_contacts = itemView.findViewById(R.id.btn_update_contacts);
            btn_update_delete = itemView.findViewById(R.id.btn_update_delete);
        }
    }
}
