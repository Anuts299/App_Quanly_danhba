package com.example.app_quanly_danhbadienthoai.ui;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import com.example.app_quanly_danhbadienthoai.R;
import com.example.app_quanly_danhbadienthoai.contacts_group_manage.Contacts_group;
import com.example.app_quanly_danhbadienthoai.group_manage.Group;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import cn.pedant.SweetAlert.SweetAlertDialog;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class LinkFragment extends Fragment {
    private Spinner spinner_group, spinner_contacts;
    private Button btn_add_link;

    // Adapter và danh sách cho Spinner
    private ArrayAdapter<String> groupAdapter;
    private ArrayList<String> groupList;
    private Map<String, String> groupMap = new HashMap<>();

    private ArrayAdapter<String> contactsAdapter;
    private ArrayList<String> contactsList;
    private Map<String, String> contactsMap = new HashMap<>();

    private String selectedContactId, selectedGroupId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_link, container, false);

        groupList = new ArrayList<>();
        contactsList = new ArrayList<>();
        loadGroupList();
        loadContactsList();

        iniUi(view);
        initListener();
        return view;
    }

    private void iniUi(View view){
        spinner_group = view.findViewById(R.id.spinner_group);
        spinner_contacts = view.findViewById(R.id.spinner_contacts);
        btn_add_link = view.findViewById(R.id.btn_add_link);

        // Set listener cho các Spinner để lấy ID của nhóm và liên hệ đã chọn
        spinner_group.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                selectedGroupId = groupMap.get(groupList.get(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {}
        });

        spinner_contacts.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                selectedContactId = contactsMap.get(contactsList.get(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {}
        });
    }

    private void initListener(){
        btn_add_link.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Kiểm tra nếu đã chọn liên hệ và nhóm
                if (selectedContactId == null || selectedGroupId == null) {
                    showAlert(SweetAlertDialog.WARNING_TYPE,"Chưa đủ thông tin yêu cầu","Hãy chọn cả nhóm và liên hệ");
                    return;
                }

                // Kết nối tới bảng CONTACTS_GROUP trên Firebase
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference ref = database.getReference("CONTACTS_GROUP");

                // Kiểm tra sự tồn tại của id_contact và id_group
                ref.orderByChild("id_contacts").equalTo(selectedContactId).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        boolean exists = false;

                        // Duyệt qua tất cả các bản ghi có idContact phù hợp
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            Contacts_group contactsGroup = snapshot.getValue(Contacts_group.class);

                            if (contactsGroup != null && contactsGroup.getId_group().equals(selectedGroupId)) {
                                // Đã tồn tại liên hệ trong nhóm
                                exists = true;
                                break;
                            }
                        }

                        if (exists) {
                            // Hiển thị thông báo: liên hệ đã tồn tại trong nhóm
                            showAlert(SweetAlertDialog.WARNING_TYPE,"Lliên hệ này đã có trong nhóm","Hãy thêm vào nhóm khác");
                            Log.d("DragDebug", "Contact already exists in group.");
                        } else {
                            // Thêm mới liên hệ vào nhóm
                            String id = ref.push().getKey(); // Tạo ID tự động
                            Contacts_group newContactsGroup = new Contacts_group(id, selectedContactId, selectedGroupId);
                            ref.child(id).setValue(newContactsGroup);

                            // Hiển thị thông báo thêm mới thành công
                            showAlert(SweetAlertDialog.SUCCESS_TYPE,"Thêm liên hệ vào nhóm thành công","Hoan hô");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        showAlert(SweetAlertDialog.ERROR_TYPE,"Thêm liên hệ vào nhóm thất bại","Có lỗi xảy ra, vui lòng thử lại!");
                        Log.d("DragDebug", "Firebase query cancelled: " + databaseError.getMessage());
                    }
                });
            }
        });
    }

    private void loadGroupList(){
        DatabaseReference myRef = FirebaseDatabase.getInstance().getReference("GROUP");
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                groupList.clear();
                for(DataSnapshot groupSnapshot : snapshot.getChildren()){
                    String id_group = groupSnapshot.getKey();
                    String name_group = groupSnapshot.child("ten_nhom").getValue(String.class);
                    if(id_group != null && name_group != null){
                        groupList.add(name_group);
                        groupMap.put(name_group, id_group);
                    }
                }
                // Khởi tạo ArrayAdapter cho Spinner và tải dữ liệu nhóm từ Firebase
                groupAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, groupList);
                groupAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinner_group.setAdapter(groupAdapter);
                groupAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                new SweetAlertDialog(requireActivity(), SweetAlertDialog.WARNING_TYPE)
                        .setTitleText("Tải danh sách thất bại")
                        .setConfirmText("OK")
                        .show();
            }
        });
    }

    private void loadContactsList(){
        DatabaseReference myRef = FirebaseDatabase.getInstance().getReference("CONTACTS");
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                contactsList.clear();
                for(DataSnapshot contactsSnapshot : snapshot.getChildren()){
                    String id_contacts = contactsSnapshot.getKey();
                    String name_contacts = contactsSnapshot.child("ten_lien_he").getValue(String.class);
                    if(id_contacts != null && name_contacts != null){
                        contactsList.add(name_contacts);
                        contactsMap.put(name_contacts, id_contacts);
                    }
                }
                // Khởi tạo ArrayAdapter cho Spinner và tải dữ liệu liên hệ từ Firebase
                contactsAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, contactsList);
                contactsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinner_contacts.setAdapter(contactsAdapter);
                contactsAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                new SweetAlertDialog(requireActivity(), SweetAlertDialog.WARNING_TYPE)
                        .setTitleText("Tải danh sách thất bại")
                        .setConfirmText("OK")
                        .show();
            }
        });
    }

    // Hàm giúp hiển thị Toast
    private void showAlert(int alertType, String title, String content) {
        new SweetAlertDialog(requireActivity(), alertType)
                .setTitleText(title)
                .setContentText(content)
                .setConfirmText("OK")
                .show();
    }
}
