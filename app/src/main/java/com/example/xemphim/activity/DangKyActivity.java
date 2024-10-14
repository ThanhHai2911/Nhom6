package com.example.xemphim.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.xemphim.R;
import com.example.xemphim.model.LoaiNguoiDung;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.HashMap;
import java.util.Map;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import java.util.Random;

public class DangKyActivity extends AppCompatActivity {
    // Các biến cần thiết
    private EditText edtHT, edtEmail, edtMk, edtNLMK;
    private Button btnDangKy;
    private ImageView ic_back;

    // Firebase Database and Auth
    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dang_ky);

        // Ánh xạ view
        setControl();

        // Khởi tạo Firebase
        mAuth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance().getReference("Users");

        // Xử lý sự kiện đăng ký
        btnDangKy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String hoTen = edtHT.getText().toString().trim();
                String email = edtEmail.getText().toString().trim();
                String matKhau = edtMk.getText().toString().trim();
                String nhapLaiMatKhau = edtNLMK.getText().toString().trim();

                if (TextUtils.isEmpty(hoTen) || TextUtils.isEmpty(email) || TextUtils.isEmpty(matKhau) || TextUtils.isEmpty(nhapLaiMatKhau)) {
                    Toast.makeText(DangKyActivity.this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                } else if (!matKhau.equals(nhapLaiMatKhau)) {
                    Toast.makeText(DangKyActivity.this, "Mật khẩu không khớp", Toast.LENGTH_SHORT).show();
                } else {
                    checkEmailExists(email, hoTen, matKhau);
                }
            }
        });

        ic_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DangKyActivity.this, DangNhapActivity.class);
                startActivity(intent);
                finish();
            }
        });

//        // Khởi tạo Firebase Database
//        usersRef = FirebaseDatabase.getInstance().getReference("loaiNguoiDung");
//
//        // Thêm loại người dùng vào Firebase
//        addLoaiNguoiDung();
    }
    private void addLoaiNguoiDung() {
        // Tạo các loại người dùng
        LoaiNguoiDung thuong = new LoaiNguoiDung(0, "thường");
        LoaiNguoiDung vip = new LoaiNguoiDung(1, "vip");
        LoaiNguoiDung admin = new LoaiNguoiDung(2, "admin");
        LoaiNguoiDung quanLy = new LoaiNguoiDung(3, "quản lý");

        // Sử dụng List để lưu trữ các loại người dùng
        List<LoaiNguoiDung> loaiNguoiDungList = new ArrayList<>();
        loaiNguoiDungList.add(thuong);
        loaiNguoiDungList.add(vip);
        loaiNguoiDungList.add(admin);
        loaiNguoiDungList.add(quanLy);

        // Thêm danh sách loại người dùng vào Firebase
        usersRef.setValue(loaiNguoiDungList)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(DangKyActivity.this, "Thêm loại người dùng thành công", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(DangKyActivity.this, "Có lỗi xảy ra", Toast.LENGTH_SHORT).show();
                    }
                });
    }
    private void setControl() {
        edtHT = findViewById(R.id.edtHT);
        edtEmail = findViewById(R.id.edtEmail);
        edtMk = findViewById(R.id.edtMk);
        edtNLMK = findViewById(R.id.edtNLMK);
        btnDangKy = findViewById(R.id.btnDangKy);
        ic_back = findViewById(R.id.ic_back);
    }

    private void checkEmailExists(final String email, final String hoTen, final String matKhau) {
        usersRef.orderByChild("email").equalTo(email).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Toast.makeText(DangKyActivity.this, "Email đã được sử dụng", Toast.LENGTH_SHORT).show();
                } else {
                    generateMaKH(hoTen, email, matKhau);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(DangKyActivity.this, "Đã xảy ra lỗi", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Hàm tạo mã khách hàng
    private void generateMaKH(final String hoTen, final String email, final String matKhau) {
        String maKH = createMaKH(hoTen);

        usersRef.orderByChild("maKH").equalTo(maKH).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Nếu mã đã tồn tại, tạo lại mã khác
                    generateMaKH(hoTen, email, matKhau);
                } else {
                    // Mã hợp lệ, tiến hành đăng ký
                    registerUser(email, hoTen, matKhau, maKH);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(DangKyActivity.this, "Đã xảy ra lỗi khi kiểm tra mã KH", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String createMaKH(String hoTen) {
        String[] words = hoTen.split(" ");
        StringBuilder initials = new StringBuilder("FN");

        for (String word : words) {
            initials.append(word.charAt(0));  // Lấy chữ cái đầu
        }

        // Thêm 3 chữ số ngẫu nhiên
        Random random = new Random();
        String randomNumbers = String.format("%03d", random.nextInt(1000));
        initials.append(randomNumbers);

        // Xáo trộn các ký tự sau FN
        List<Character> charList = new ArrayList<>();
        for (int i = 2; i < initials.length(); i++) {
            charList.add(initials.charAt(i));
        }
        Collections.shuffle(charList);

        StringBuilder shuffledMaKH = new StringBuilder("FN");
        for (char c : charList) {
            shuffledMaKH.append(c);
        }

        return shuffledMaKH.toString();
    }

    private void registerUser(final String email, final String hoTen, String matKhau, final String maKH) {
        mAuth.createUserWithEmailAndPassword(email, matKhau).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                FirebaseUser user = mAuth.getCurrentUser();
                if (user != null) {
                    // Tạo một Map để lưu thông tin người dùng
                    Map<String, Object> userMap = new HashMap<>();
                    userMap.put("id_user", maKH); // Mã KH
                    userMap.put("name", hoTen); // Tên người dùng
                    userMap.put("email", email); // Địa chỉ email
                    userMap.put("password", matKhau); // Mật khẩu (không nên lưu mật khẩu trong cơ sở dữ liệu)
                    userMap.put("created_at", ServerValue.TIMESTAMP); // Thời gian tạo
                    userMap.put("updated_at", ServerValue.TIMESTAMP); // Thời gian cập nhật
                    userMap.put("id_loaiND", 0); // ID loại người dùng mặc định là 0

                    // Lưu thông tin người dùng vào Firebase
                    usersRef.child(user.getUid()).setValue(userMap).addOnCompleteListener(task1 -> {
                        if (task1.isSuccessful()) {
                            Toast.makeText(DangKyActivity.this, "Đăng ký thành công", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(DangKyActivity.this, DangNhapActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(DangKyActivity.this, "Đã xảy ra lỗi khi lưu thông tin", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            } else {
                String errorMessage;
                try {
                    throw task.getException();
                } catch (Exception e) {
                    errorMessage = e.getMessage();
                }
                Toast.makeText(DangKyActivity.this, "Đăng ký thất bại: " + errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }

}
