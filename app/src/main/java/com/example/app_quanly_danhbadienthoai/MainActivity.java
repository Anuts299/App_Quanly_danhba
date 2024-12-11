package com.example.app_quanly_danhbadienthoai;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.app_quanly_danhbadienthoai.ui.ContactsFragment;
import com.example.app_quanly_danhbadienthoai.ui.Contacts_groupFragment;
import com.example.app_quanly_danhbadienthoai.ui.GroupFragment;

public class MainActivity extends AppCompatActivity {
    public static final int MY_REQUEST_CODES =10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        // Hiển thị câu chào mừng
        TextView welcomeText = findViewById(R.id.tv_welcome);
        welcomeText.setVisibility(View.VISIBLE);

        // Sau 0.8 giây, chuyển sang ContactsFragment
        new Handler().postDelayed(() -> {
            // Xóa câu chào mừng
            welcomeText.setVisibility(View.GONE);

            // Thay thế FrameLayout bằng ContactsFragment
            replaceFragment(new ContactsFragment());
        }, 2500);
        findViewById(R.id.btn_group).setOnClickListener(view -> replaceFragment(new GroupFragment()));
        findViewById(R.id.btn_contacts).setOnClickListener(view -> replaceFragment(new ContactsFragment()));
        findViewById(R.id.btn_contacts_group).setOnClickListener(view -> replaceFragment(new Contacts_groupFragment()));
    }

    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.frame_layout, fragment);
        transaction.addToBackStack(null); // Để quay lại fragment trước đó
        transaction.commit();
    }
}