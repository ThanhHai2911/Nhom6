package com.example.xemphim.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.xemphim.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class DangNhapActivity extends AppCompatActivity {
    private EditText edtEmail, edtMk;
    private Button btnDangNhap;
    private CheckBox rememberMeCheckBox;
    private TextView tvTaoTaiKhoan;

    // Firebase Authentication
    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dang_nhap);

        // Ánh xạ các view
        setControl();

        // Khởi tạo FirebaseAuth và DatabaseReference
        mAuth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance().getReference("Users"); // Đảm bảo rằng bạn đã khởi tạo đúng đường dẫn

        // Xử lý sự kiện khi bấm đăng nhập
        btnDangNhap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = edtEmail.getText().toString().trim();
                String matKhau = edtMk.getText().toString().trim();

                // Kiểm tra rỗng
                if (TextUtils.isEmpty(email) || TextUtils.isEmpty(matKhau)) {
                    Toast.makeText(DangNhapActivity.this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                } else {
                    // Thực hiện đăng nhập
                    loginUser(email, matKhau);
                }
            }
        });

        tvTaoTaiKhoan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DangNhapActivity.this, DangKyActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void setControl() {
        edtEmail = findViewById(R.id.edtEmail);
        edtMk = findViewById(R.id.edtMk);
        btnDangNhap = findViewById(R.id.loginButton);
        rememberMeCheckBox = findViewById(R.id.rememberMeCheckBox);
        tvTaoTaiKhoan = findViewById(R.id.tvTaoTaiKhoan);
    }

    private void loginUser(String email, String matKhau) {
        mAuth.signInWithEmailAndPassword(email, matKhau).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                FirebaseUser user = mAuth.getCurrentUser();
                if (user != null) {
                    // Lấy thông tin người dùng từ Firebase
                    usersRef.child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                // Lấy thông tin người dùng từ snapshot
                                String hoTen = dataSnapshot.child("name").getValue(String.class);
                                String idUser = dataSnapshot.child("id_user").getValue(String.class);
                                String email = dataSnapshot.child("email").getValue(String.class);
                                Integer idLoaiND = dataSnapshot.child("id_loaiND").getValue(Integer.class);

                                // Lưu thông tin vào SharedPreferences
                                SharedPreferences sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putString("id_user", idUser);
                                editor.putString("name", hoTen);
                                editor.putString("email", email);
                                editor.putInt("id_loaiND", idLoaiND);
                                editor.apply();

                                // Chuyển đến màn hình chính
                                Intent intent = new Intent(DangNhapActivity.this, MainActivity.class);
                                startActivity(intent);
                                finish();
                            } else {
                                Toast.makeText(DangNhapActivity.this, "Thông tin người dùng không tồn tại", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Toast.makeText(DangNhapActivity.this, "Lỗi khi lấy thông tin người dùng", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            } else {
                // Xử lý lỗi đăng nhập
                String errorMessage = "Đăng nhập thất bại"; // Thông báo lỗi mặc định
                if (task.getException() != null) {
                    errorMessage = task.getException().getMessage(); // Lấy thông điệp lỗi từ exception
                    Log.e("DangNhapActivity", "Đăng nhập thất bại: " + errorMessage); // Ghi log lỗi
                }
                Toast.makeText(DangNhapActivity.this, "Đăng nhập thất bại: " + errorMessage, Toast.LENGTH_LONG).show();
            }
        });
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
