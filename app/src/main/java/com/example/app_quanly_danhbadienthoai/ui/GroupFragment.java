package com.example.app_quanly_danhbadienthoai.ui;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.app_quanly_danhbadienthoai.R;
import com.example.app_quanly_danhbadienthoai.contacts_manage.Contacts;
import com.example.app_quanly_danhbadienthoai.contacts_manage.ContactsAdapter;
import com.example.app_quanly_danhbadienthoai.group_manage.Group;
import com.example.app_quanly_danhbadienthoai.group_manage.GroupAdapter;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import cn.pedant.SweetAlert.SweetAlertDialog;


public class GroupFragment extends Fragment {
    private SearchView searchGroup;
    private Button btn_add_group;

    private RecyclerView recyView_Group;
    private List<Group> mListGroup;
    private GroupAdapter mGroupAdapter;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_group, container, false);
        mListGroup = new ArrayList<>();
        mGroupAdapter = new GroupAdapter(mListGroup, new GroupAdapter.IClickListener(){
            @Override
            public void onClickUpdateItem(Group group) {
                openDialogUpdateItemGroup(group);
            }

            @Override
            public void onClickDeleteItem(Group group) {
                onClickDeleteData(group);
            }
        });
        initUi(view);
        initListener();
        getListGroupFromRealtimeDatabase();
        return view;
    }
    private void initUi(View view){
        searchGroup = view.findViewById(R.id.searchGroup);
        btn_add_group = view.findViewById(R.id.btn_add_group);
        recyView_Group = view.findViewById(R.id.recyView_Group);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(requireContext());
        recyView_Group.setLayoutManager(linearLayoutManager);

        recyView_Group.setAdapter(mGroupAdapter);
        searchItemGroup();
    }
    private void initListener(){
        btn_add_group.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickUploadGroup();
            }
        });
    }
    private void onClickUploadGroup() {
        Dialog dialog = new Dialog(requireContext());
        dialog.setContentView(R.layout.dialog_upload_group);

        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        EditText edt_name_group = dialog.findViewById(R.id.edt_name_group);
        EditText edt_dec_group = dialog.findViewById(R.id.edt_dec_group);



        Button buttonAdd = dialog.findViewById(R.id.btn_upload_group);
        buttonAdd.setOnClickListener(v -> {
            String name_group = edt_name_group.getText().toString().trim();
            String dec_group = edt_dec_group.getText().toString().trim();



            if (!name_group.isEmpty()) {
                Group group = new Group(null, name_group, dec_group);
                UploadGroup(group);
                dialog.dismiss();
            } else {
                new SweetAlertDialog(requireActivity(), SweetAlertDialog.WARNING_TYPE)
                        .setTitleText("Thiếu thông tin")
                        .setContentText("Vui lòng nhập đủ thông tin")
                        .setConfirmText("OK")
                        .setConfirmClickListener(SweetAlertDialog::dismissWithAnimation)
                        .show();
            }
        });

        Button buttonCancel = dialog.findViewById(R.id.btn_cancel_upload_group);
        buttonCancel.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }
    private void UploadGroup(Group group) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("GROUP");

        Query checkName = myRef.orderByChild("ten_nhom").equalTo(group.getTen_nhom());

        checkName.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot nameSnapshot) {
                if (!nameSnapshot.exists()) {
                    addContactToDatabase(myRef, group);
                } else {
                    showAlert(SweetAlertDialog.WARNING_TYPE, "Nhóm đã tồn tại", "Tên nhóm đã có trong danh sách");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                showAlert(SweetAlertDialog.ERROR_TYPE, "Thêm nhóm thất bại", "Đã xảy ra lỗi. Thử lại sau.");
            }
        });
    }

    private void addContactToDatabase(DatabaseReference myRef, Group group) {
        String key = myRef.push().getKey();
        if (key == null) {
            showAlert(SweetAlertDialog.ERROR_TYPE, "Thêm nhóm thất bại", "Không thể tạo ID mới. Thử lại sau.");
            return;
        }
        group.setId(key);
        myRef.child(key).setValue(group, (error, ref) -> {
            if (error != null) {
                showAlert(SweetAlertDialog.ERROR_TYPE, "Thêm liên hệ thất bại", "Đã xảy ra lỗi. Thử lại sau.");
            } else {
                showAlert(SweetAlertDialog.SUCCESS_TYPE, "Thêm liên hệ thành công", null);
            }
        });
    }
    private void showAlert(int alertType, String title, String content) {
        new SweetAlertDialog(requireActivity(), alertType)
                .setTitleText(title)
                .setContentText(content)
                .setConfirmText("OK")
                .show();
    }
    private void getListGroupFromRealtimeDatabase(){
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRey = database.getReference("GROUP");

        myRey.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Group group = dataSnapshot.getValue(Group.class);
                if(group != null){
                    mListGroup.add(group);
                }
                mGroupAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Group group = dataSnapshot.getValue(Group.class);
                if(group == null ||mListGroup == null || mListGroup.isEmpty()){
                    return;
                }

                for(int i = 0; i < mListGroup.size(); i++){
                    if(Objects.equals(group.getId(), mListGroup.get(i).getId())){
                        mListGroup.set(i, group);
                        break;
                    }
                }
                mGroupAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                Group group = dataSnapshot.getValue(Group.class);
                if(group == null ||mListGroup == null || mListGroup.isEmpty()){
                    return;
                }

                for(int i = 0; i < mListGroup.size(); i++){
                    if(Objects.equals(group.getId(), mListGroup.get(i).getId())){
                        mListGroup.remove(mListGroup.get(i));
                        break;
                    }
                }
                mGroupAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    public void searchItemGroup(){
        searchGroup.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchGroup.clearFocus();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                searchList(newText);
                return true;
            }
        });
    }
    //Tìm danh sách
    private void searchList(String text){
        ArrayList<Group> searchList = new ArrayList<>();
        for(Group group : mListGroup){
            if(group.getTen_nhom().toLowerCase().contains(text.toLowerCase())){
                searchList.add(group);
            }
        }
        mGroupAdapter.searchGroupList(searchList);
    }
    private void openDialogUpdateItemGroup(Group group){
        final Dialog dialog = new Dialog(getActivity());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_upload_group);

        TextView title = dialog.findViewById(R.id.tv_group);
        if(title != null){
            title.setText("Chỉnh sửa nhóm");
        }
        Button updateButton = dialog.findViewById(R.id.btn_upload_group);
        Button canceButton = dialog.findViewById(R.id.btn_cancel_upload_group);

        EditText edt_name_group = dialog.findViewById(R.id.edt_name_group);
        EditText edt_dec_group = dialog.findViewById(R.id.edt_dec_group);
        // Thiết lập chiều rộng và chiều cao cho Dialog
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        edt_name_group.setText(group.getTen_nhom());
        edt_dec_group.setText(group.getMo_ta()!= null && !group.getMo_ta().isEmpty() ? group.getMo_ta() : "");

        canceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        if (updateButton != null) {
            updateButton.setText("CHỈNH SỬA");
            updateButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                    DatabaseReference myRef = database.getReference("GROUP");

                    String new_name_group = edt_name_group.getText().toString().trim();
                    String new_dec_group = edt_dec_group.getText().toString().trim();


                    if (new_name_group.isEmpty()) {
                        edt_name_group.setError("Tên nhóm không được để trống");
                        return; // Dừng lại nếu tên không hợp lệ
                    }


                    group.setTen_nhom(new_name_group);
                    group.setMo_ta(new_dec_group);


                    myRef.child(group.getId()).updateChildren(group.toMap(), new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                            if (error == null) {
                                new SweetAlertDialog(requireActivity(), SweetAlertDialog.SUCCESS_TYPE)
                                        .setTitleText("Chỉnh sửa thành công")
                                        .setContentText("Đã chỉnh sửa nhóm")
                                        .setConfirmText("OK")
                                        .setConfirmClickListener(SweetAlertDialog::dismissWithAnimation)
                                        .show();
                                dialog.dismiss();
                            } else {
                                new SweetAlertDialog(requireActivity(), SweetAlertDialog.ERROR_TYPE)
                                        .setTitleText("Lỗi")
                                        .setContentText("Cập nhật thất bại, vui lòng thử lại")
                                        .setConfirmText("OK")
                                        .show();
                            }
                        }
                    });

                }
            });
        }
        dialog.show();

    }
    private void onClickDeleteData(Group group){
        // Hiển thị SweetAlertDialog xác nhận xóa
        new SweetAlertDialog(getContext(), SweetAlertDialog.WARNING_TYPE)
                .setTitleText("Xác nhận xóa")
                .setContentText("Bạn có chắc chắn muốn xóa nhóm này không?")
                .setConfirmText("Xóa")
                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sDialog) {
                        // Xác nhận xóa - thực hiện xóa trong Firebase
                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("GROUP");
                        reference.child(group.getId()) // Thay "id_classroom" bằng ID lớp học bạn muốn xóa
                                .removeValue()
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        sDialog
                                                .setTitleText("Đã xóa!")
                                                .setContentText("Nhóm đã được xóa.")
                                                .setConfirmText("OK")
                                                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                                    @Override
                                                    public void onClick(SweetAlertDialog sDialog) {
                                                        sDialog.dismiss();
                                                    }
                                                })
                                                .changeAlertType(SweetAlertDialog.SUCCESS_TYPE);


                                    } else {
                                        sDialog
                                                .setTitleText("Lỗi!")
                                                .setContentText("Không thể xóa nhóm.")
                                                .setConfirmText("OK")
                                                .setConfirmClickListener(SweetAlertDialog::dismissWithAnimation)
                                                .changeAlertType(SweetAlertDialog.ERROR_TYPE);
                                    }
                                });
                    }
                })
                .setCancelButton("Hủy", SweetAlertDialog::dismiss)
                .show();
    }
}
