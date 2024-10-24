package com.example.xemphim.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.xemphim.R;
import com.example.xemphim.adapter.ThongBaoAdapter;
import com.example.xemphim.databinding.ActivityDsthongBaoBinding;
import com.example.xemphim.databinding.ActivityQlThongBaoBinding;
import com.example.xemphim.model.ThongBao;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class DSThongBaoActivity extends AppCompatActivity {
    private ActivityDsthongBaoBinding binding;
    private ThongBaoAdapter thongBaoAdapter;
    private List<ThongBao> thongBaoList = new ArrayList<>();
    private DatabaseReference mDatabase;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityDsthongBaoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Khởi tạo Firebase Realtime Database
        mDatabase = FirebaseDatabase.getInstance().getReference();
        binding.btnBack.setOnClickListener(v -> onBackPressed());
        binding.recyclerViewApis.setLayoutManager(new LinearLayoutManager(this));
        thongBaoAdapter = new ThongBaoAdapter(DSThongBaoActivity.this,thongBaoList);
        binding.recyclerViewApis.setAdapter(thongBaoAdapter);
        binding.ivThem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DSThongBaoActivity.this, QLThongBaoActivity.class);
                startActivity(intent);
            }
        });
        getThongBaoFromDatabase();
    }
    private void getThongBaoFromDatabase() {
        DatabaseReference thongBaoRef = FirebaseDatabase.getInstance().getReference().child("ThongBao");

        thongBaoRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                thongBaoList.clear(); // Xóa danh sách trước khi thêm mới
                List<String> seenIds = new ArrayList<>(); // Danh sách để lưu ID đã thấy

                for (DataSnapshot thongBaoSnapshot : snapshot.getChildren()) {
                    ThongBao thongBao = thongBaoSnapshot.getValue(ThongBao.class);
                    String idThongBao = thongBaoSnapshot.child("idThongBao").getValue(String.class);
                    if (thongBao != null) {
                        // Kiểm tra xem ID đã thấy hay chưa
                        if (!seenIds.contains(idThongBao)) {
                            thongBaoList.add(thongBao); // Thêm vào danh sách nếu ID chưa thấy
                            seenIds.add(idThongBao); // Đánh dấu ID là đã thấy
                        }
                    }
                }
                // Cập nhật RecyclerView với danh sách thông báo mới
                thongBaoAdapter.notifyDataSetChanged(); // Cập nhật adapter
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(DSThongBaoActivity.this, "Lỗi khi lấy thông báo: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }



    @Override
    protected void onResume() {
        super.onResume();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);  // Giữ màn hình sáng khi hoạt động
    }

    @Override
    protected void onPause() {
        super.onPause();
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);  // Tắt giữ màn hình sáng khi dừng hoạt động
    }
}