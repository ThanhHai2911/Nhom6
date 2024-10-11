package com.example.xemphim.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.xemphim.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class DangNhapActivity extends AppCompatActivity {
    private EditText edtEmail, edtMk;
    private Button btnDangNhap;
    private CheckBox rememberMeCheckBox;
    private TextView tvTaoTaiKhoan;

    // Firebase Authentication
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dang_nhap);

        // Ánh xạ các view
        setControl();

        // Khởi tạo FirebaseAuth
        mAuth = FirebaseAuth.getInstance();

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
        mAuth.signInWithEmailAndPassword(email, matKhau)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Đăng nhập thành công
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            Toast.makeText(DangNhapActivity.this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();
                            // Chuyển tới MainActivity
                            Intent intent = new Intent(DangNhapActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish(); // Đóng màn hình đăng nhập
                        }
                    } else {
                        // Đăng nhập thất bại
                        Toast.makeText(DangNhapActivity.this, "Sai email hoặc mật khẩu", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
