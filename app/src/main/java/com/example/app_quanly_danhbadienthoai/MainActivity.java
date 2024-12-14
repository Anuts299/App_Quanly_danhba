    package com.example.app_quanly_danhbadienthoai;

    import android.os.Bundle;
    import android.os.Handler;
    import android.util.Log;
    import android.view.MotionEvent;
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
    import com.example.app_quanly_danhbadienthoai.ui.LinkFragment;

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
            findViewById(R.id.btn_contacts_group).setOnTouchListener(new View.OnTouchListener() {
                private Handler handler = new Handler();
                private boolean isLongPress = false;

                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            // Khi bắt đầu nhấn, khởi động Handler để đợi 3 giây
                            handler.postDelayed(() -> {
                                isLongPress = true;
                                Log.d("MainActivity", "Long press triggered - Switching to Contacts_groupFragment");
                                replaceFragment(new Contacts_groupFragment());
                            }, 3000);
                            return true;

                        case MotionEvent.ACTION_UP:
                            // Nếu không phải nhấn giữ, để sự kiện được xử lý bởi OnClickListener
                            if (!isLongPress) {
                                Log.d("MainActivity", "Short press detected - Let OnClickListener handle this");
                                replaceFragment(new LinkFragment());
                            }
                            // Reset trạng thái và dừng Handler
                            handler.removeCallbacksAndMessages(null);
                            isLongPress = false;
                            return false; // Trả về false để sự kiện tiếp tục truyền tới OnClickListener

                        case MotionEvent.ACTION_CANCEL:
                            Log.d("MainActivity", "Touch event canceled");
                            handler.removeCallbacksAndMessages(null);
                            isLongPress = false;
                            return true;

                        default:
                            return false;
                    }
                }
            });





        }

        private void replaceFragment(Fragment fragment) {
            Log.d("MainActivity", "replaceFragment triggered - Switching to " + fragment.getClass().getSimpleName());
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.replace(R.id.frame_layout, fragment);
            transaction.addToBackStack(null); // Để quay lại fragment trước đó
            transaction.commit();
        }

    }