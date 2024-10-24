package com.example.xemphim.activity;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.xemphim.R;
import com.example.xemphim.adapter.UserAdapter;
import com.example.xemphim.databinding.ActivityQluserBinding;
import com.example.xemphim.model.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class QLUserActivity extends AppCompatActivity {
    private ActivityQluserBinding binding;
    private UserAdapter userAdapter;
    private List<User> userList;
    private DatabaseReference yeuCauRef;
    private DatabaseReference usersRef;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityQluserBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Khởi tạo Firebase reference
        yeuCauRef = FirebaseDatabase.getInstance().getReference("YeuCau");
        usersRef = FirebaseDatabase.getInstance().getReference("Users");
        // Lấy số lượng yêu cầu hôm nay
        laySoLuongYeuCauHomNay();
        demSoLuongUserVip();
        // Cài đặt Spinner và RecyclerView
//        xulyTimKiemSpiner();
//        hienThiRecyclerView();
//        loadDuLieu();
    }

    private void laySoLuongYeuCauHomNay() {
        // Lấy ngày hôm nay dưới dạng chuỗi "dd/MM/yyyy"
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        String ngayHienTai = sdf.format(new Date());

        // Truy vấn Firebase để lấy các yêu cầu trong ngày hôm nay dựa trên paymentDate
        Query query = yeuCauRef.orderByChild("paymentDate").startAt(ngayHienTai).endAt(ngayHienTai + "\uf8ff");
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int soLuong = (int) dataSnapshot.getChildrenCount();

                // Hiển thị số lượng yêu cầu hôm nay lên TextView
                binding.tvSLUserDangKy.setText(String.valueOf(soLuong));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(QLUserActivity.this, "Lỗi khi lấy dữ liệu", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void demSoLuongUserVip() {
        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int countVipUsers = 0;
                // Duyệt qua tất cả user trong bảng Users
                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    // Lấy id_loaiND của mỗi user
                    Long idLoaiND = userSnapshot.child("id_loaiND").getValue(Long.class);
                    // Nếu id_loaiND == 1 (gói VIP) thì tăng biến đếm
                    if (idLoaiND != null && idLoaiND == 1) {
                        countVipUsers++;
                    }
                }
                // Hiển thị số lượng user sử dụng gói VIP lên TextView
                binding.tvSLGoiVipDangKy.setText(String.valueOf(countVipUsers));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Xử lý lỗi nếu có
            }
        });
    }

    private void loadDuLieu() {
        // Thêm dữ liệu vào danh sách người dùng
        userList.add(new User("Nguyễn Văn A", "online", true, false));
        userList.add(new User("Trần Thị B", "offline", false, true));

        // Thông báo adapter cập nhật dữ liệu
        userAdapter.notifyDataSetChanged();
    }

    private void hienThiRecyclerView() {
        // Khởi tạo RecyclerView
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Khởi tạo danh sách người dùng và adapter
        userList = new ArrayList<>();
        userAdapter = new UserAdapter(userList);
        binding.recyclerView.setAdapter(userAdapter);
    }

    private void xulyTimKiemSpiner() {
        // Setup Spinner using binding
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.status_options, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Use binding.spinnerStatus to access the Spinner
        binding.spinnerStatus.setAdapter(adapter);
    }
}
