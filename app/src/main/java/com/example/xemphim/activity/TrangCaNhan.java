package com.example.xemphim.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.xemphim.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class TrangCaNhan extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_trang_ca_nhan);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        // Đặt item mặc định được chọn là màn hình Download
        bottomNavigationView.setSelectedItemId(R.id.nav_profile);

        // Xử lý sự kiện chọn item của Bottom Navigation
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Intent intent = null;

                // Sử dụng if-else thay vì switch-case
                if (item.getItemId() == R.id.nav_home) {
                    intent = new Intent(TrangCaNhan.this, MainActivity.class);
                } else if (item.getItemId() == R.id.nav_vip) {
                    intent = new Intent(TrangCaNhan.this, VipActivity.class);
                } else if (item.getItemId() == R.id.nav_profile) {
                    // Không làm gì nếu đã chọn mục Download
                    return true; // Trả về true để không khởi tạo lại Activity
                } else if (item.getItemId() == R.id.nav_download) {
                    intent = new Intent(TrangCaNhan.this, DownLoadActivity.class);
                }

                // Chuyển sang Activity mới nếu intent không null
                if (intent != null) {
                    intent.putExtra("selected_item_id", item.getItemId());
                    startActivity(intent);
                    overridePendingTransition(0, 0);  // Không có hoạt ảnh cho chuyển đổi mượt mà
                }
                return true; // Trả về true nếu đã xử lý
            }
        });

    }
}