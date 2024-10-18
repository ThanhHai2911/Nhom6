package com.example.xemphim.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.MotionEvent;
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
import com.example.xemphim.model.Phim;
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
    private boolean isPasswordVisible = false;  // Trạng thái của mật khẩu

    // Firebase Authentication
    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dang_nhap);
//// Khởi tạo Firebase Database reference
//        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Movies");
//
//        // Tạo đối tượng Movie
//        Phim newMovie = new Phim("1", "Movie Title", "Movie Description",
//                "2024-01-01", "2h", 8.5f,
//                "https://phimimg.com/upload/vod/20241005-1/795e8f69a90d9b470c844a6d017e7565.jpg",
//                "https://phimimg.com/upload/vod/20241005-1/aac3986c07b75cd6a7002797cdd61fc9.jpg",
//                2024, 0, 0, 0, "https://youtu.be/0q7XhWAB88Q?si=--l0pQykPWfpRNTg");
//
//        // Thêm phim vào database với ID tự tạo
//        String movieId = databaseReference.push().getKey();
//        if (movieId != null) {
//            newMovie.setId_movie(movieId);
//            databaseReference.child(movieId).setValue(newMovie);
//        }
        // Ánh xạ các view
        setControl();
        xemMatKhau();
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

    private void xemMatKhau() {
        // Thiết lập sự kiện nhấn vào biểu tượng con mắt
        edtMk.setOnTouchListener((v, event) -> {
            final int DRAWABLE_RIGHT = 2;  // Vị trí của drawableEnd (con mắt) là vị trí thứ 2 (bên phải)
            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (event.getRawX() >= (edtMk.getRight() - edtMk.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                    // Kiểm tra trạng thái hiện tại của mật khẩu
                    if (isPasswordVisible) {
                        // Nếu mật khẩu đang hiển thị, chuyển sang ẩn
                        edtMk.setTransformationMethod(PasswordTransformationMethod.getInstance());
                        edtMk.setCompoundDrawablesWithIntrinsicBounds(R.drawable.baseline_lock_outline_24, 0, R.drawable.baseline_visibility_off_24, 0);
                    } else {
                        // Nếu mật khẩu đang ẩn, chuyển sang hiển thị
                        edtMk.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                        edtMk.setCompoundDrawablesWithIntrinsicBounds(R.drawable.baseline_lock_outline_24, 0, R.drawable.baseline_visibility_24, 0);
                    }
                    // Thay đổi trạng thái
                    isPasswordVisible = !isPasswordVisible;
                    edtMk.setSelection(edtMk.getText().length()); // Để con trỏ vẫn ở cuối EditText
                    return true;
                }
            }
            return false;
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
                Toast.makeText(DangNhapActivity.this, "Đăng nhập thất bại, mật khẩu hoặc email không đúng", Toast.LENGTH_LONG).show();
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
