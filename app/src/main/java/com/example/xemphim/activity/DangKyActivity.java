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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.HashMap;
import java.util.Map;

public class DangKyActivity extends AppCompatActivity {
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
                    // Email đã tồn tại
                    Toast.makeText(DangKyActivity.this, "Email đã được sử dụng", Toast.LENGTH_SHORT).show();
                } else {
                    // Đăng ký tài khoản mới
                    registerUser(email, hoTen, matKhau);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(DangKyActivity.this, "Đã xảy ra lỗi", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void registerUser(final String email, final String hoTen, String matKhau) {
        mAuth.createUserWithEmailAndPassword(email, matKhau).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                FirebaseUser user = mAuth.getCurrentUser();
                if (user != null) {
                    Map<String, Object> userMap = new HashMap<>();
                    userMap.put("email", email);
                    userMap.put("hoTen", hoTen);

                    usersRef.child(user.getUid()).setValue(userMap).addOnCompleteListener(task1 -> {
                        if (task1.isSuccessful()) {
                            Toast.makeText(DangKyActivity.this, "Đăng ký thành công", Toast.LENGTH_SHORT).show();
                            // Chuyển sang MainActivity sau khi đăng ký thành công
                            Intent intent = new Intent(DangKyActivity.this, DangNhapActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Clear all previous activities
                            startActivity(intent);
                            finish(); // Đóng Activity đăng ký sau khi chuyển sang MainActivity
                        } else {
                            Toast.makeText(DangKyActivity.this, "Đã xảy ra lỗi khi lưu thông tin", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            } else {
                // Chi tiết lỗi đăng ký Firebase
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
