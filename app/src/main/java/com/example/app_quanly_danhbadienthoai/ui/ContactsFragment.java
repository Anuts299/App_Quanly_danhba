package com.example.app_quanly_danhbadienthoai.ui;

import static com.example.app_quanly_danhbadienthoai.MainActivity.MY_REQUEST_CODES;

import android.Manifest;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.app_quanly_danhbadienthoai.R;
import com.example.app_quanly_danhbadienthoai.contacts_manage.Contacts;
import com.example.app_quanly_danhbadienthoai.contacts_manage.ContactsAdapter;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import cn.pedant.SweetAlert.SweetAlertDialog;
import de.hdodenhof.circleimageview.CircleImageView;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicInteger;


public class ContactsFragment extends Fragment {
    private SearchView searchContacts;
    private Spinner spinner_filter_group;
    private Button btn_add_contacts;
    private ImageView img_upload_contacts;
    private String imageURL;

    private Uri uri;
    private ActivityResultLauncher<Intent> activityResultLauncher;
    private RecyclerView recyView_Contacts;
    private List<Contacts> mListContact;
    private ContactsAdapter mContactsAdapter;

    private ArrayAdapter<String> groupAdapter, contactsAdapter;
    private ArrayList<String> groupList = new ArrayList<>();
    private ArrayList<String> contactsList = new ArrayList<>();
    private Map<String, String> groupMap = new HashMap<>();  // Map nhóm và id nhóm
    private Map<String, String> contactsMap = new HashMap<>();  // Map liên hệ và id liên hệ// Danh sách liên hệ
    private List<Contacts> filteredContacts = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_contacts, container, false);
        mListContact = new ArrayList<>();
        mContactsAdapter = new ContactsAdapter(mListContact, new ContactsAdapter.IClickListener() {
            @Override
            public void onClickUpdateItem(Contacts contacts) {
                openDialogUpdateItemContacts(contacts);
            }

            @Override
            public void onClickDeleteItem(Contacts contacts) {
                onClickDeleteData(contacts);
            }
        });
        initUi(view);
        initListener();
        getListContactsFromRealtimeDatabase();
        loadGroupList();
        // Kiểm tra quyền READ_EXTERNAL_STORAGE
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            // Nếu chưa có quyền, yêu cầu quyền
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_REQUEST_CODES);
        }

        activityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            Intent data = result.getData();
                            uri = data != null ? data.getData() : null;
                            if (uri != null) {
                                img_upload_contacts.setImageURI(uri);
                                // Lưu ảnh vào bộ nhớ nội bộ
                                saveImageToInternalStorage(uri);
                            }
                        } else {
                            new SweetAlertDialog(requireActivity(), SweetAlertDialog.WARNING_TYPE)
                                    .setTitleText("Không có ảnh nào được chọn")
                                    .setContentText("Vui lòng thử lại và chọn một ảnh.")
                                    .setConfirmText("OK")
                                    .show();
                        }
                    }
                }
        );


        return view;
    }
    private void loadGroupList() {
        DatabaseReference myRef = FirebaseDatabase.getInstance().getReference("GROUP");
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                groupList.clear();
                groupList.add("Tất cả");
                for (DataSnapshot groupSnapshot : snapshot.getChildren()) {
                    String id_group = groupSnapshot.getKey();
                    String name_group = groupSnapshot.child("ten_nhom").getValue(String.class);
                    if (id_group != null && name_group != null) {
                        groupList.add(name_group);
                        groupMap.put(name_group, id_group);  // Lưu ánh xạ tên nhóm và id nhóm
                    }
                }
                groupAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, groupList);
                groupAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinner_filter_group.setAdapter(groupAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Xử lý lỗi
            }
        });
    }
    private void loadContactsByGroup(String groupId) {
        List<Contacts> filteredContacts = new ArrayList<>();
        int totalContacts = mListContact.size();
        final AtomicInteger processedContacts = new AtomicInteger(0);  // Biến đếm số lần callback được gọi

        // Duyệt qua tất cả liên hệ
        for (Contacts contact : mListContact) {
            // Kiểm tra xem liên hệ có thuộc nhóm không
            isContactInGroup(contact, groupId, new GroupCheckCallback() {
                @Override
                public void onGroupChecked(boolean isInGroup) {
                    if (isInGroup) {
                        filteredContacts.add(contact);
                    }

                    // Tăng đếm mỗi lần callback được gọi
                    int processed = processedContacts.incrementAndGet();

                    // Kiểm tra xem tất cả các liên hệ đã được kiểm tra xong chưa
                    Log.d("Contacts", "Processed contacts: " + processed);
                    Log.d("Contacts", "Total contacts: " + totalContacts);

                    if (processed == totalContacts) {
                        // Cập nhật adapter sau khi lọc
                        setContactsAdapter(filteredContacts);
                        Log.d("Contacts", "Filtered contacts size: " + filteredContacts.size());
                    }
                }
            });
        }
    }




    private void setContactsAdapter(List<Contacts> contactsList) {
        if (contactsList == null || contactsList.isEmpty()) {
            Log.d("Contacts", "Received empty or null contacts list.");
        } else {
            Log.d("Contacts", "Setting new contacts list with " + contactsList.size() + " contacts");
        }
        mContactsAdapter.setContactsList(contactsList);  // Cập nhật danh sách liên hệ cho adapter
        mContactsAdapter.notifyDataSetChanged();  // Thông báo cho RecyclerView cập nhật giao diện
    }




    private void isContactInGroup(Contacts contact, String groupId, final GroupCheckCallback callback) {
        Log.d("Contacts", "Checking if contact " + contact.getTen_lien_he() + " is in group " + groupId);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("CONTACTS_GROUP");

        // Lọc theo id_contacts và id_group
        ref.orderByChild("id_contacts").equalTo(contact.getId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean isInGroup = false;

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    String groupIdFromDb = dataSnapshot.child("id_group").getValue(String.class);

                    // Kiểm tra xem nhóm có khớp không
                    if (groupId.equals(groupIdFromDb)) {
                        isInGroup = true;
                        break;
                    }
                }

                Log.d("Contacts", "Contact " + contact.getTen_lien_he() + " is in group: " + isInGroup);
                callback.onGroupChecked(isInGroup); // Gọi callback để thông báo kết quả
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Xử lý lỗi
                Log.e("Contacts", "Error checking contact group: " + error.getMessage());
            }
        });
    }


    // Callback interface
    public interface GroupCheckCallback {
        void onGroupChecked(boolean isInGroup);
    }



    private void saveImageToInternalStorage(Uri imageUri) {
        try {
            // Mở InputStream từ URI ảnh
            InputStream inputStream = requireContext().getContentResolver().openInputStream(imageUri);

            // Tạo tên tệp duy nhất dựa trên thời gian
            String fileName = "my_image_" + System.currentTimeMillis() + ".jpg";
            FileOutputStream outputStream = requireContext().openFileOutput(fileName, Context.MODE_PRIVATE);

            // Đọc dữ liệu ảnh và ghi vào bộ nhớ nội bộ
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, length);
            }

            // Đóng các luồng
            inputStream.close();
            outputStream.close();

            // Đọc lại ảnh từ bộ nhớ nội bộ (nếu cần thiết để kiểm tra)
            File file = new File(requireContext().getFilesDir(), fileName);
            if (file.exists()) {
                uri = Uri.fromFile(file);
            } else {
            }

        } catch (IOException e) {
            e.printStackTrace();
            // Xử lý lỗi nếu có
            Log.e("Image", "Error saving image: " + e.getMessage());
        }
    }

    public void selectImageFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        activityResultLauncher.launch(intent);
    }
    // Xử lý kết quả khi yêu cầu quyền
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_REQUEST_CODES) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Quyền đã được cấp
                Log.d("ContactsFragment", "Quyền đọc bộ nhớ đã được cấp");
            } else {

            }
        }
    }
    private void openDialogUpdateItemContacts(Contacts contacts){
        final Dialog dialog = new Dialog(getActivity());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_upload_contacts);

        TextView titleTextView = dialog.findViewById(R.id.tv_level); // Adjust ID if needed
        if (titleTextView != null) {
            titleTextView.setText("Chỉnh sửa Liên hệ");
        }

        // Find the "Thêm trình độ" button and set new text
        Button updateButton = dialog.findViewById(R.id.btn_upload_contacts); // Adjust ID if needed

        Button cancelButton = dialog.findViewById(R.id.btn_cancel_upload_contacts);
        EditText edt_name_contacts = dialog.findViewById(R.id.edt_name_contacts);
        EditText edt_phone_contacts = dialog.findViewById(R.id.edt_phone_contacts);
        EditText edt_email_contacts = dialog.findViewById(R.id.edt_email_contacts);
        EditText edt_locate_contacts = dialog.findViewById(R.id.edt_locate_contacts);
        TextView tv_birthday_contacts = dialog.findViewById(R.id.tv_birthday_contacts);
        img_upload_contacts = dialog.findViewById(R.id.img_upload_contacts);
        // Thiết lập chiều rộng và chiều cao cho Dialog
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        edt_name_contacts.setText(contacts.getTen_lien_he());
        edt_phone_contacts.setText(contacts.getSo_dien_thoai());
        edt_email_contacts.setText(contacts.getEmail()!= null && !contacts.getEmail().isEmpty() ? contacts.getEmail() : "");
        edt_locate_contacts.setText(contacts.getDia_chi()!= null && !contacts.getDia_chi().isEmpty() ? contacts.getDia_chi() : "");
        // Đặt ngày sinh (sử dụng ngày hiện tại nếu không có giá trị)
        String birthday = contacts.getNgay_sinh();
        if (birthday != null && !birthday.isEmpty()) {
            tv_birthday_contacts.setText(birthday);
        } else {
            // Lấy ngày hiện tại
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            String currentDate = sdf.format(new Date());
            tv_birthday_contacts.setText(currentDate);  // Hiển thị ngày hiện tại nếu không có ngày sinh
        }
        tv_birthday_contacts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog(tv_birthday_contacts);
            }
        });
        // Lấy đường dẫn tệp hình ảnh từ đối tượng Contacts
        String imagePath = contacts.getAnh();  // Đảm bảo rằng getAnh() trả về đường dẫn tệp hình ảnh đã lưu

        if (imagePath != null && !imagePath.isEmpty()) {
            // Chuyển đổi URI 'file://' thành đường dẫn tệp thực sự
            Uri path = Uri.parse(imagePath);
            if (path != null) {
                img_upload_contacts.setImageURI(path);
            } else {
                img_upload_contacts.setImageResource(R.drawable.img);  // Hình ảnh mặc định nếu không có
            }
        } else {
            img_upload_contacts.setImageResource(R.drawable.img);  // Hình ảnh mặc định nếu không có đường dẫn
        }
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        img_upload_contacts.setOnClickListener(v -> {
            Intent photoPicker = new Intent(Intent.ACTION_PICK);
            photoPicker.setType("image/*");
            activityResultLauncher.launch(photoPicker);
        });

        if (updateButton != null) {
            updateButton.setText("CHỈNH SỬA");
            updateButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                    DatabaseReference myRef = database.getReference("CONTACTS");

                    String new_name_contacts = edt_name_contacts.getText().toString().trim();
                    String new_phone_contacts = edt_phone_contacts.getText().toString().trim();
                    String new_email_contacts = edt_email_contacts.getText().toString().trim();
                    String new_locate_contacts = edt_locate_contacts.getText().toString().trim();
                    String new_birthday_contacts = tv_birthday_contacts.getText().toString().trim();
                    String uriupdate = String.valueOf(uri);

                    if (new_name_contacts.isEmpty()) {
                        edt_name_contacts.setError("Tên liên hệ không được để trống");
                        return; // Dừng lại nếu tên không hợp lệ
                    }

                    if (new_phone_contacts.length() != 10 || !new_phone_contacts.matches("\\d{10}")) {
                        edt_phone_contacts.setError("Số điện thoại phải có 10 chữ số");
                        return; // Dừng lại nếu số điện thoại không hợp lệ
                    }


                    if (!new_email_contacts.isEmpty() && !isValidEmail(new_email_contacts)) {
                        edt_email_contacts.setError("Email không hợp lệ");
                        return; // Dừng lại nếu email không hợp lệ
                    }


                    contacts.setTen_lien_he(new_name_contacts);
                    contacts.setSo_dien_thoai(new_phone_contacts);
                    contacts.setEmail(new_email_contacts);
                    contacts.setDia_chi(new_locate_contacts);
                    contacts.setNgay_sinh(new_birthday_contacts);
                    contacts.setAnh(uriupdate);

                    myRef.child(contacts.getId()).updateChildren(contacts.toMap(), new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                            if (error == null) {
                                new SweetAlertDialog(requireActivity(), SweetAlertDialog.SUCCESS_TYPE)
                                        .setTitleText("Chỉnh sửa thành công")
                                        .setContentText("Đã chỉnh sửa liên hệ")
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

    private boolean isValidEmail(String email) {
        String emailPattern = "[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}";
        return email.matches(emailPattern);
    }
    private void onClickDeleteData(Contacts contacts){
        // Hiển thị SweetAlertDialog xác nhận xóa
        new SweetAlertDialog(getContext(), SweetAlertDialog.WARNING_TYPE)
                .setTitleText("Xác nhận xóa")
                .setContentText("Bạn có chắc chắn muốn xóa liên hệ này không?")
                .setConfirmText("Xóa")
                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sDialog) {
                        // Xác nhận xóa - thực hiện xóa trong Firebase
                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("CONTACTS");
                        reference.child(contacts.getId()) // Thay "id_classroom" bằng ID lớp học bạn muốn xóa
                                .removeValue()
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        sDialog
                                                .setTitleText("Đã xóa!")
                                                .setContentText("Liên hệ đã được xóa.")
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
                                                .setContentText("Không thể xóa liên hệ.")
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

    private void initUi(View view) {
        searchContacts = view.findViewById(R.id.searchContacts);
        spinner_filter_group = view.findViewById(R.id.spinner_filter_group);
        btn_add_contacts = view.findViewById(R.id.btn_add_contacts);
        recyView_Contacts = view.findViewById(R.id.recyView_Contacts);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(requireContext());
        recyView_Contacts.setLayoutManager(linearLayoutManager);

        recyView_Contacts.setAdapter(mContactsAdapter);
        searchItemContacts();
    }

    private void initListener() {
        btn_add_contacts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadImageAndAddContact();
            }
        });
        spinner_filter_group.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                String groupName = parentView.getItemAtPosition(position).toString();

                // Kiểm tra nếu người dùng chọn "Tất cả"
                if (groupName.equals("Tất cả")) {
                    setContactsAdapter(mListContact);
                } else {
                    String groupId = groupMap.get(groupName);  // Lấy id nhóm từ tên nhóm
                    Log.d("Spinner", "Selected group: " + groupId);  // Log khi chọn nhóm
                    loadContactsByGroup(groupId);  // Lọc liên hệ theo nhóm
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // Xử lý khi không chọn gì (tuỳ vào ứng dụng, có thể không cần thiết)
            }
        });



    }
    public void searchItemContacts(){
        searchContacts.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchContacts.clearFocus();
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
        ArrayList<Contacts> searchList = new ArrayList<>();
        for(Contacts contacts : mListContact){
            if(contacts.getTen_lien_he().toLowerCase().contains(text.toLowerCase())||
                    contacts.getSo_dien_thoai().toLowerCase().contains(text.toLowerCase())){
                searchList.add(contacts);
            }
        }
        mContactsAdapter.searchFacultyList(searchList);
    }

    private void onClickUploadContacts() {
        Dialog dialog = new Dialog(requireContext());
        dialog.setContentView(R.layout.dialog_upload_contacts);

        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        EditText edt_name_contacts = dialog.findViewById(R.id.edt_name_contacts);
        EditText edt_phone_contacts = dialog.findViewById(R.id.edt_phone_contacts);
        EditText edt_email_contacts = dialog.findViewById(R.id.edt_email_contacts);
        EditText edt_locate_contacts = dialog.findViewById(R.id.edt_locate_contacts);
        TextView tv_birthday_contacts = dialog.findViewById(R.id.tv_birthday_contacts);

        tv_birthday_contacts.setOnClickListener(v -> showDatePickerDialog(tv_birthday_contacts));
        img_upload_contacts = dialog.findViewById(R.id.img_upload_contacts);
        if (img_upload_contacts != null) {
            img_upload_contacts.setOnClickListener(v -> {
                Intent photoPicker = new Intent(Intent.ACTION_PICK);
                photoPicker.setType("image/*");
                activityResultLauncher.launch(photoPicker);
            });
        } else {
            Log.e("ContactsFragment", "ImageView img_upload_contacts is null. Check your layout file.");
        }
        Button buttonAdd = dialog.findViewById(R.id.btn_upload_contacts);
        buttonAdd.setOnClickListener(v -> {
            String name_contacts = edt_name_contacts.getText().toString().trim();
            String phone_contacts = edt_phone_contacts.getText().toString().trim();
            String email_contacts = edt_email_contacts.getText().toString().trim();
            String locate_contacts = edt_locate_contacts.getText().toString().trim();
            String birthday_contacts = tv_birthday_contacts.getText().toString().trim();

            if (!phone_contacts.matches("\\d{10}")) {
                showAlert(SweetAlertDialog.ERROR_TYPE,"Lỗi dữ liệu nhập", "Hãy nhập đúng số điện thoại (10 chữ số)");
                return;
            }

            String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
            if (!email_contacts.matches(emailPattern) && !email_contacts.isEmpty()) {
                showAlert(SweetAlertDialog.ERROR_TYPE,"Lỗi dữ liệu nhập", "Hãy nhập đúng email (abc@gmail.com)");
                return;
            }

            if (!name_contacts.isEmpty() && !phone_contacts.isEmpty()) {
                Contacts contacts = new Contacts(null, name_contacts, phone_contacts, email_contacts, locate_contacts, birthday_contacts, uri != null ? uri.toString() : "");
                UploadContacts(contacts);
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

        Button buttonCancel = dialog.findViewById(R.id.btn_cancel_upload_contacts);
        buttonCancel.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void showDatePickerDialog(final TextView textView) {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(),
                (view, year1, month1, dayOfMonth) -> {
                    String selectedDate = String.format("%02d/%02d/%04d", dayOfMonth, month1 + 1, year1);
                    textView.setText(selectedDate);
                }, year, month, day);

        datePickerDialog.show();
    }

    private void showAlert(int alertType, String title, String content) {
        new SweetAlertDialog(requireActivity(), alertType)
                .setTitleText(title)
                .setContentText(content)
                .setConfirmText("OK")
                .show();
    }


    private void UploadContacts(Contacts contacts) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("CONTACTS");

        Query checkName = myRef.orderByChild("ten_lien_he").equalTo(contacts.getTen_lien_he());
        Query checkPhone = myRef.orderByChild("so_dien_thoai").equalTo(contacts.getSo_dien_thoai());

        checkName.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot nameSnapshot) {
                if (nameSnapshot.exists()) {
                    checkPhone.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot phoneSnapshot) {
                            if (phoneSnapshot.exists()) {
                                showAlert(SweetAlertDialog.WARNING_TYPE, "Liên hệ đã tồn tại", "Tên và số điện thoại đã có trong danh sách");
                            } else {
                                addContactToDatabase(myRef, contacts);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            showAlert(SweetAlertDialog.ERROR_TYPE, "Thêm liên hệ thất bại", "Đã xảy ra lỗi. Thử lại sau.");
                        }
                    });
                } else {
                    addContactToDatabase(myRef, contacts);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                showAlert(SweetAlertDialog.ERROR_TYPE, "Thêm liên hệ thất bại", "Đã xảy ra lỗi. Thử lại sau.");
            }
        });
    }

    private void addContactToDatabase(DatabaseReference myRef, Contacts contacts) {
        String key = myRef.push().getKey();
        if (key == null) {
            showAlert(SweetAlertDialog.ERROR_TYPE, "Thêm liên hệ thất bại", "Không thể tạo ID mới. Thử lại sau.");
            return;
        }
        contacts.setId(key);
        myRef.child(key).setValue(contacts, (error, ref) -> {
            if (error != null) {
                showAlert(SweetAlertDialog.ERROR_TYPE, "Thêm liên hệ thất bại", "Đã xảy ra lỗi. Thử lại sau.");
            } else {
                showAlert(SweetAlertDialog.SUCCESS_TYPE, "Thêm liên hệ thành công", null);
            }
        });
    }

    private void uploadImageAndAddContact() {
        if (uri == null) {
            onClickUploadContacts();
        } else {
            onClickUploadContacts();
        }
    }
    private void getListContactsFromRealtimeDatabase(){
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("CONTACTS");
        mListContact.clear();
        myRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Contacts contacts = snapshot.getValue(Contacts.class);
                if(contacts != null){
                    mListContact.add(contacts);
                }
                mContactsAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Contacts contacts = snapshot.getValue(Contacts.class);
                if(contacts == null ||mListContact == null || mListContact.isEmpty()){
                    return;
                }

                for(int i = 0; i < mListContact.size(); i++){
                    if(Objects.equals(contacts.getId(), mListContact.get(i).getId())){
                        mListContact.set(i, contacts);
                        break;
                    }
                }
                mContactsAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                Contacts contacts = snapshot.getValue(Contacts.class);
                if(contacts == null ||mListContact == null || mListContact.isEmpty()){
                    return;
                }
                for(int i = 0; i < mListContact.size(); i++){
                    if(Objects.equals(contacts.getId(), mListContact.get(i).getId())){
                        mListContact.remove(mListContact.get(i));
                        break;
                    }
                }
                mContactsAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}
