package com.example.app_quanly_danhbadienthoai.ui;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.hardware.lights.LightState;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.app_quanly_danhbadienthoai.R;
import com.example.app_quanly_danhbadienthoai.contacts_group_manage.ContactsAdapter2;
import com.example.app_quanly_danhbadienthoai.contacts_group_manage.Contacts_group;
import com.example.app_quanly_danhbadienthoai.contacts_group_manage.GroupAdapter2;
import com.example.app_quanly_danhbadienthoai.contacts_manage.Contacts;
import com.example.app_quanly_danhbadienthoai.group_manage.Group;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;


public class Contacts_groupFragment extends Fragment {

    private RecyclerView recyView_Contacts2, recyView_Group2;
    private List<Contacts> mListContacts;
    private ContactsAdapter2 mContacts2Adapter;
    private List<Group> mListGroup;
    private GroupAdapter2 mGroup2Adapter;
    private Contacts selectedContact;

    private int currentDraggedX;
    private int currentDraggedY;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_contacts_group, container, false);
        mListContacts = new ArrayList<>();
        mListGroup = new ArrayList<>();
        mContacts2Adapter = new ContactsAdapter2(mListContacts);
        mGroup2Adapter = new GroupAdapter2(mListGroup);
        initUi(view);
        getListContactsFromRealtimeDatabase();
        getListGroupFromRealtimeDatabase();
        setupDragAndDrop();
        return view;
    }
    private void initUi(View view){
        recyView_Contacts2 = view.findViewById(R.id.recyView_Contacts2);
        recyView_Group2 = view.findViewById(R.id.recyView_Group2);
        recyView_Contacts2.setClipChildren(false);
        recyView_Contacts2.setClipToPadding(false);
        recyView_Group2.setClipChildren(false);
        recyView_Group2.setClipToPadding(false);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(requireContext());
        recyView_Contacts2.setLayoutManager(linearLayoutManager);

        recyView_Contacts2.setAdapter(mContacts2Adapter);

        LinearLayoutManager linearLayoutManager2 = new LinearLayoutManager(requireContext());
        recyView_Group2.setLayoutManager(linearLayoutManager2);

        recyView_Group2.setAdapter(mGroup2Adapter);
    }
    private void getListContactsFromRealtimeDatabase(){
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRey = database.getReference("CONTACTS");

        myRey.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Contacts contacts = dataSnapshot.getValue(Contacts.class);
                if(contacts != null){
                    mListContacts.add(contacts);
                }
                mContacts2Adapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Contacts contacts = dataSnapshot.getValue(Contacts.class);
                if(contacts == null ||mListContacts == null || mListContacts.isEmpty()){
                    return;
                }

                for(int i = 0; i < mListContacts.size(); i++){
                    if(Objects.equals(contacts.getId(), mListContacts.get(i).getId())){
                        mListContacts.set(i, contacts);
                        break;
                    }
                }
                mContacts2Adapter.notifyDataSetChanged();
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                Contacts contacts = dataSnapshot.getValue(Contacts.class);
                if(contacts == null ||mListContacts == null || mListContacts.isEmpty()){
                    return;
                }

                for(int i = 0; i < mListContacts.size(); i++){
                    if(Objects.equals(contacts.getId(), mListContacts.get(i).getId())){
                        mListContacts.remove(mListContacts.get(i));
                        break;
                    }
                }
                mContacts2Adapter.notifyDataSetChanged();
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
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
                mGroup2Adapter.notifyDataSetChanged();
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
                mGroup2Adapter.notifyDataSetChanged();
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
                mGroup2Adapter.notifyDataSetChanged();
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    private void setupDragAndDrop(){
        ItemTouchHelper.Callback callback = new ItemTouchHelper.Callback() {
            @Override
            public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                if(recyclerView.getId() == R.id.recyView_Contacts2){
                    return makeMovementFlags(ItemTouchHelper.UP | ItemTouchHelper.DOWN, ItemTouchHelper.ACTION_STATE_DRAG);
                }
                return 0;
            }

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder viewHolder1) {
                int fromPosition = viewHolder.getAdapterPosition();
                int toPosition = viewHolder1.getAdapterPosition();

                if (fromPosition < 0 || toPosition < 0 || fromPosition >= mListContacts.size() || toPosition >= mListContacts.size()) {
                    return false;
                }

                // Hoán đổi vị trí trong danh sách và cập nhật adapter
                Collections.swap(mListContacts, fromPosition, toPosition);
                mContacts2Adapter.notifyItemMoved(fromPosition, toPosition);
                return true;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int i) {

            }

            @Override
            public boolean isLongPressDragEnabled() {
                return true;
            }

            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView,
                                    @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY,
                                    int actionState, boolean isCurrentlyActive) {
                if (isCurrentlyActive) {
                    // Lấy tọa độ trong hệ tọa độ cửa sổ (Window)
                    int[] itemCoords = new int[2];
                    viewHolder.itemView.getLocationInWindow(itemCoords); // Sử dụng getLocationInWindow

                    currentDraggedX = itemCoords[0] + (int) dX;

                    // Cộng thêm độ lệch 561 vào currentDraggedY
                    currentDraggedY = itemCoords[1] + (int) dY; // Thêm 561 đơn vị vào vị trí Y

                    // Thay đổi vị trí hiển thị
                    viewHolder.itemView.setTranslationX(dX);
                    viewHolder.itemView.setTranslationY(dY);

                    // Log tọa độ của mục kéo
                    Log.d("DragDebug", "Dragged item original coords: X=" + itemCoords[0] + ", Y=" + itemCoords[1]);
                    Log.d("DragDebug", "Dragged item adjusted coords (currentDraggedX, currentDraggedY): X="
                            + currentDraggedX + ", Y=" + currentDraggedY);

                    // Tự động cuộn RecyclerView nếu cần
                    if (viewHolder.itemView.getBottom() + (int) dY > recyclerView.getHeight()) {
                        recyclerView.scrollBy(0, 20); // Cuộn xuống
                    } else if (viewHolder.itemView.getTop() + (int) dY < 0) {
                        recyclerView.scrollBy(0, -20); // Cuộn lên
                    }
                } else {
                    viewHolder.itemView.setElevation(11.0f);
                    // Reset trạng thái sau khi thả
                    viewHolder.itemView.setTranslationX(0);
                    viewHolder.itemView.setTranslationY(0);
                }
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }



            @Override
            public void onSelectedChanged(@Nullable RecyclerView.ViewHolder viewHolder, int actionState) {
                super.onSelectedChanged(viewHolder, actionState);
                if (actionState == ItemTouchHelper.ACTION_STATE_DRAG && viewHolder != null) {
                    int position = viewHolder.getAdapterPosition();
                    if (position >= 0 && position < mListContacts.size()) {
                        selectedContact = mListContacts.get(position);
                    }
                    viewHolder.itemView.bringToFront();
                }
            }

            @Override
            public void clearView(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                super.clearView(recyclerView, viewHolder);
                if (recyclerView.getId() == R.id.recyView_Contacts2 && selectedContact != null) {
                    // Kiểm tra vị trí thả item trên recyView_Group2
                    View targetView = findTargetGroupView();
                    if (targetView != null) {
                        int targetPosition = recyView_Group2.getChildAdapterPosition(targetView);
                        Log.d("DragDebug", "Target position: " + targetPosition);
                        if (targetPosition >= 0 && targetPosition < mListGroup.size()) {
                            Group selectedGroup = mListGroup.get(targetPosition);
                            handleDrop(selectedGroup);
                        }
                    }else {
                        Log.d("DragDebug", "No target view detected during drop!");
                    }
                }else {
                    Log.d("DragDebug", "No selected contact or wrong RecyclerView ID.");
                }
                selectedContact = null;
            }
        };
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(recyView_Contacts2);
    }

    private View findTargetGroupView() {
        // Lấy tọa độ của RecyclerView trên màn hình
        int[] recyclerViewCoords = new int[2];
        recyView_Group2.getLocationOnScreen(recyclerViewCoords);

        // Duyệt qua từng view con trong RecyclerView đích
        for (int i = 0; i < recyView_Group2.getChildCount(); i++) {
            View child = recyView_Group2.getChildAt(i);

            // Lấy tọa độ của view con trên màn hình
            int[] childCoords = new int[2];
            child.getLocationOnScreen(childCoords);

            // Tạo một Rect đại diện cho view con
            Rect childRect = new Rect(
                    childCoords[0],                       // X-left
                    childCoords[1],                       // Y-top
                    childCoords[0] + child.getWidth(),    // X-right
                    childCoords[1] + child.getHeight()    // Y-bottom
            );

            // Log tọa độ của Rect
            Log.d("DragDebug", "Child " + i + " Rect: " + childRect.toString());

            // Kiểm tra nếu tọa độ mục kéo nằm trong Rect này
            if (isItemDraggedInside(childRect)) {
                Log.d("DragDebug", "Target found at index: " + i);
                return child;
            }
        }

        return null; // Không tìm thấy view phù hợp
    }



    private boolean isItemDraggedInside(Rect targetRect) {
        // Log tọa độ item kéo và targetRect để kiểm tra
        Log.d("DragDebug", "Dragged X=" + currentDraggedX + ", Y=" + currentDraggedY + ", Target Rect=" + targetRect);

        // Kiểm tra xem tọa độ kéo có nằm trong targetRect không
        return targetRect.contains(currentDraggedX, currentDraggedY);
    }

    private void handleDrop(Group group) {
        // Lấy thông tin Contact đang kéo
        Contacts selectedContact = getSelectedContact();

        if (selectedContact == null || group == null) {
            Log.d("DragDebug", "Selected contact or group is null.");
            return;
        };

        // Kết nối tới bảng CONTACTS_GROUP trên Firebase
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference("CONTACTS_GROUP");

        // Kiểm tra sự tồn tại của id_contact và id_group
        ref.orderByChild("id_contacts").equalTo(selectedContact.getId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                boolean exists = false;

                // Duyệt qua tất cả các bản ghi có idContact phù hợp
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Contacts_group contactsGroup = snapshot.getValue(Contacts_group.class);

                    if (contactsGroup != null && contactsGroup.getId_group().equals(group.getId())) {
                        // Đã tồn tại liên hệ trong nhóm
                        exists = true;
                        break;
                    }
                }

                if (exists) {
                    // Hiển thị thông báo: liên hệ đã tồn tại trong nhóm
                    showToast("Liên hệ đã tồn tại trong nhóm này!");
                    Log.d("DragDebug", "Contact already exists in group.");
                } else {
                    // Thêm mới liên hệ vào nhóm
                    String id = ref.push().getKey(); // Tạo ID tự động
                    Contacts_group newContactsGroup = new Contacts_group(id, selectedContact.getId(), group.getId());
                    ref.child(id).setValue(newContactsGroup);

                    // Hiển thị thông báo thêm mới thành công
                    showToast("Đã thêm liên hệ vào nhóm!");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                showToast("Có lỗi xảy ra, vui lòng thử lại!");
                Log.d("DragDebug", "Firebase query cancelled: " + databaseError.getMessage());
            }
        });
    }

    // Hàm tiện ích để hiển thị thông báo
    private void showToast(String message) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }

    private Contacts getSelectedContact() {
        return selectedContact;
    }

}