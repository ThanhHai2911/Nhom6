package com.example.xemphim.activity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.xemphim.R;
import com.example.xemphim.adapter.UserAdapter;
import com.example.xemphim.databinding.ActivityQluserBinding;
import com.example.xemphim.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class QLUserActivity extends AppCompatActivity {
    private ActivityQluserBinding binding;
    private UserAdapter userAdapter;
    private List<User> userList;
    private DatabaseReference yeuCauRef;
    private DatabaseReference usersRef;
    private DatabaseReference loaiNguoiDungRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityQluserBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Khởi tạo Firebase reference
        yeuCauRef = FirebaseDatabase.getInstance().getReference("YeuCau");
        usersRef = FirebaseDatabase.getInstance().getReference("Users");
        loaiNguoiDungRef = FirebaseDatabase.getInstance().getReference("loaiNguoiDung");

        // Cài đặt RecyclerView
        hienThiRecyclerView();
        // Ghi lại trạng thái online
        ghiLaiTrangThai();
        // Gọi các phương thức để lấy dữ liệu
        laySoLuongYeuCauHomNay();
        demSoLuongUserVip();
        loadDuLieu(); // Thêm dữ liệu người dùng vào danh sách

    }
    private void ghiLaiTrangThai() {
        // Lấy user hiện tại
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference userStatusRef = FirebaseDatabase.getInstance().getReference("Users").child(userId).child("status");

        // Theo dõi trạng thái kết nối Firebase
        DatabaseReference connectedRef = FirebaseDatabase.getInstance().getReference(".info/connected");
        connectedRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean connected = snapshot.getValue(Boolean.class);
                if (connected) {
                    // Khi người dùng kết nối với Firebase, đặt trạng thái là "online"
                    userStatusRef.setValue("online");

                    // Khi người dùng ngắt kết nối, đặt trạng thái là "offline"
                    userStatusRef.onDisconnect().setValue("offline");
                } else {
                    // Khi không kết nối, bạn cũng có thể cập nhật lại "offline" nếu cần
                    userStatusRef.setValue("offline");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.w("Firebase", "Không thể lấy trạng thái kết nối.", error.toException());
            }
        });
    }


    private void laySoLuongYeuCauHomNay() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        String ngayHienTai = sdf.format(new Date());

        Query query = yeuCauRef.orderByChild("paymentDate").startAt(ngayHienTai).endAt(ngayHienTai + "\uf8ff");
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int soLuong = (int) dataSnapshot.getChildrenCount();
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
                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    Long idLoaiND = userSnapshot.child("id_loaiND").getValue(Long.class);
                    if (idLoaiND != null && idLoaiND == 1) {
                        countVipUsers++;
                    }
                }
                binding.tvSLGoiVipDangKy.setText(String.valueOf(countVipUsers));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(QLUserActivity.this, "Lỗi khi lấy dữ liệu", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void hienThiRecyclerView() {
        // Khởi tạo RecyclerView
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Khởi tạo danh sách người dùng và adapter
        userList = new ArrayList<>();
        userAdapter = new UserAdapter(userList, this); // Thêm context
        binding.recyclerView.setAdapter(userAdapter);
    }

    private void loadDuLieu() {
        userList.clear(); // Đảm bảo xóa danh sách trước khi thêm mới

        // Lấy danh sách loại người dùng
        loaiNguoiDungRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot loaiDataSnapshot) {
                // Tạo một map để lưu trữ loại người dùng
                HashMap<Long, String> loaiMap = new HashMap<>();
                for (DataSnapshot loaiSnapshot : loaiDataSnapshot.getChildren()) {
                    Long id = loaiSnapshot.child("id").getValue(Long.class);
                    String type = loaiSnapshot.child("type").getValue(String.class);
                    loaiMap.put(id, type);
                }

                // Bây giờ lấy dữ liệu người dùng
                usersRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        userList.clear(); // Xóa danh sách trước khi thêm mới

                        for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                            String id_user = userSnapshot.child("id_user").getValue(String.class);
                            String name = userSnapshot.child("name").getValue(String.class);
                            String email = userSnapshot.child("email").getValue(String.class);
                            String status = userSnapshot.child("status").getValue(String.class);
                            Long idLoaiND = userSnapshot.child("id_loaiND").getValue(Long.class);

                            // Xác định loại người dùng dựa vào id_loaiND
                            String goi = loaiMap.get(idLoaiND);
                            if (goi == null) {
                                goi = "Thường"; // Mặc định là Thường nếu không có trong map
                            }

                            // Kiểm tra nếu trạng thái là null và gán giá trị mặc định là "offline"
                            if (status == null) {
                                status = "offline";
                            }

                            // Log để kiểm tra giá trị
                            Log.d("UserData", "ID: " + id_user + ", Name: " + name + ", Email: " + email + ", Status: " + status + ", Goi: " + goi);

                            // Tạo đối tượng User và thêm vào danh sách
                            User user = new User(id_user, name, status, goi);
                            userList.add(user);
                        }

                        // Log số lượng người dùng
                        Log.d("UserList", "Số lượng người dùng: " + userList.size());

                        // Thông báo adapter cập nhật dữ liệu
                        userAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(QLUserActivity.this, "Lỗi khi lấy dữ liệu", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(QLUserActivity.this, "Lỗi khi lấy dữ liệu loại người dùng", Toast.LENGTH_SHORT).show();
            }
        });
    }





    private void xulyTimKiemSpiner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.status_options, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerStatus.setAdapter(adapter);
    }


}
