package com.example.app_quanly_danhbadienthoai.group_manage;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.app_quanly_danhbadienthoai.R;
import com.example.app_quanly_danhbadienthoai.contacts_manage.Contacts;
import com.example.app_quanly_danhbadienthoai.contacts_manage.ContactsAdapter;

import java.util.ArrayList;
import java.util.List;

public class GroupAdapter extends RecyclerView.Adapter<GroupAdapter.GroupViewHolder>{

    private List<Group> mListGroup;
    private IClickListener mIClickListener;

    public interface IClickListener{
        void onClickUpdateItem(Group group);
        void onClickDeleteItem(Group group);
    }

    public GroupAdapter(List<Group> mListGroup, IClickListener mIClickListener) {
        this.mListGroup = mListGroup;
        this.mIClickListener = mIClickListener;
    }

    @NonNull
    @Override
    public GroupViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.recycler_item_group, viewGroup, false);
        return new GroupViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GroupViewHolder groupViewHolder, int i) {
        Group group = mListGroup.get(i);
        if (group == null){
            return;
        }
        groupViewHolder.tv_name_group.setText(group.getTen_nhom());
        groupViewHolder.tv_dec_group.setText(group.getMo_ta());

        groupViewHolder.btn_update_group.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mIClickListener.onClickUpdateItem(group);
            }
        });
        groupViewHolder.btn_update_delete_group.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mIClickListener.onClickDeleteItem(group);
            }
        });

    }

    @Override
    public int getItemCount() {
        return (mListGroup != null) ? mListGroup.size() : 0;
    }

    public class GroupViewHolder extends RecyclerView.ViewHolder{
        private TextView tv_name_group, tv_dec_group;
        private Button btn_update_delete_group, btn_update_group;

        public GroupViewHolder(@NonNull View itemView) {
            super(itemView);
            tv_name_group = itemView.findViewById(R.id.tv_name_group);
            tv_dec_group = itemView.findViewById(R.id.tv_dec_group);
            btn_update_delete_group = itemView.findViewById(R.id.btn_update_delete_group);
            btn_update_group = itemView.findViewById(R.id.btn_update_group);
        }
    }
    public void searchGroupList(ArrayList<Group> searchList){
        mListGroup = searchList;
        notifyDataSetChanged();
    }
}
