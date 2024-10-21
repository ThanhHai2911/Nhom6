package com.example.xemphim.activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import com.example.xemphim.R;

public class VipActivity extends AppCompatActivity {
    Button vip_plan_button;
    private String idUser;
    private  String nameUser;
    private String emailUser;
    private int idLoaiND;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_vip);
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        vip_plan_button = findViewById(R.id.vip_plan_button);
        // Đặt item mặc định được chọn là màn hình Home
        bottomNavigationView.setSelectedItemId(R.id.nav_vip);
        laythongtinUser();
        //kiem tra truy cap
        //MainActivity.kiemTraTruyCap(idUser);
        Toast.makeText(VipActivity.this, "Xin chào " + idUser, Toast.LENGTH_SHORT).show();
        // Xử lý sự kiện chọn item của Bottom Navigation
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Intent intent = null;
                if (item.getItemId() == R.id.nav_home) {
                    intent = new Intent(VipActivity.this, MainActivity.class);
                } else if (item.getItemId() == R.id.nav_vip) {
                    return true;
                } else if (item.getItemId() == R.id.nav_download) {
                    intent = new Intent(VipActivity.this, DownLoadActivity.class);
                } else if (item.getItemId() == R.id.nav_profile) {
                    intent = new Intent(VipActivity.this, ProfileActivity.class);

                }
                // Pass the selected item to the new Activity
                if (intent != null) {
                    intent.putExtra("selected_item_id", item.getItemId());
                    startActivity(intent);
                    overridePendingTransition(0, 0);  // No animation for smooth transition
                    return true;
                }
                return false;

            }
        });

        vip_plan_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(VipActivity.this, ThanhToanActivity.class);
                startActivity(intent);
                finish();
            }
        });

    }
    private void laythongtinUser(){
        SharedPreferences sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
        idUser = sharedPreferences.getString("id_user", null);
        nameUser = sharedPreferences.getString("name", null);
        emailUser  = sharedPreferences.getString("email", null);
        idLoaiND = sharedPreferences.getInt("id_loaiND", 0);

    }
    @Override
    protected void onResume() {
        super.onResume();
        // Giữ màn hình sáng khi ứng dụng hoạt động
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Xóa cờ giữ màn hình sáng khi ứng dụng không còn hoạt động
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }
}