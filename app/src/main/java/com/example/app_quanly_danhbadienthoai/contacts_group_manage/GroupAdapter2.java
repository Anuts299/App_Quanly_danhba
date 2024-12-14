package com.example.app_quanly_danhbadienthoai.contacts_group_manage;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.app_quanly_danhbadienthoai.R;
import com.example.app_quanly_danhbadienthoai.group_manage.Group;

import java.util.ArrayList;
import java.util.List;

public class GroupAdapter2 extends RecyclerView.Adapter<GroupAdapter2.GroupViewHolder>{

    private List<Group> mListGroup;


    public GroupAdapter2(List<Group> mListGroup) {
        this.mListGroup = mListGroup;

    }

    @NonNull
    @Override
    public GroupViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.recycler_item, viewGroup, false);
        return new GroupViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GroupViewHolder groupViewHolder, int i) {
        Group group = mListGroup.get(i);
        if (group == null){
            return;
        }
        groupViewHolder.linear.setPadding(0,0,0,0);


        groupViewHolder.tv_item1.setText(group.getTen_nhom());
        groupViewHolder.tv_item3.setTag(group.getId());
        groupViewHolder.tv_item1.setGravity(Gravity.CENTER);
        groupViewHolder.tv_item2.setVisibility(View.GONE);

    }

    @Override
    public int getItemCount() {
        return (mListGroup != null) ? mListGroup.size() : 0;
    }

    public class GroupViewHolder extends RecyclerView.ViewHolder{
        private TextView tv_item1, tv_item2, tv_item3;
        private LinearLayout linear;


        public GroupViewHolder(@NonNull View itemView) {
            super(itemView);
            linear = itemView.findViewById(R.id.linear);
            tv_item1 = itemView.findViewById(R.id.tv_item1);
            tv_item2 = itemView.findViewById(R.id.tv_item2);
            tv_item3 = itemView.findViewById(R.id.tv_item3);

        }
    }

}
