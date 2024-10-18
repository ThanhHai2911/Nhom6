package com.example.xemphim.activity;

import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.xemphim.databinding.ActivityChinhSuaThongTinBinding;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ChinhSuaThongTinActivity extends AppCompatActivity {
    private ActivityChinhSuaThongTinBinding binding;
    private DatabaseReference databaseReference; // Tham chiếu đến Realtime Database
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityChinhSuaThongTinBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        // Khởi tạo Firebase Realtime Database
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");

        // Lấy người dùng hiện tại
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            userId = user.getUid(); // Lấy ID người dùng từ Firebase Authentication
        } else {
            // Xử lý trường hợp người dùng chưa đăng nhập
            Log.w("UpdateUserActivity", "User is not logged in.");
            finish(); // Đóng Activity nếu người dùng chưa đăng nhập
            return;
        }
        // Thiết lập sự kiện cho nút Lưu
        binding.btnLuu.setOnClickListener(view -> updateUserInfo());
        binding.btnHuy.setOnClickListener(view -> finish()); // Đóng activity nếu nhấn Huỷ
    }

    private void updateUserInfo() {
        String name = binding.edtTenNguoiDung.getText().toString().trim();
        String newPassword = binding.edtMkMoi.getText().toString().trim();

        // Kiểm tra nếu các trường không rỗng
        if (name.isEmpty() || newPassword.isEmpty()) {
            Log.w("UpdateUserActivity", "Please fill all fields.");
            return; // Dừng nếu có trường rỗng
        }

        // Kiểm tra nếu userId không null
        if (userId == null) {
            Log.w("UpdateUserActivity", "User ID is null, cannot update user information.");
            return;
        }

        // Cập nhật thông tin người dùng trong Realtime Database
        databaseReference.child(userId).child("name").setValue(name)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d("UpdateUserActivity", "User name updated successfully.");
                        // Cập nhật mật khẩu mới
                        updatePassword(newPassword);
                    } else {
                        Log.w("UpdateUserActivity", "Error updating user name in database", task.getException());
                    }
                });
    }

    private void updatePassword(String newPassword) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            // Update password directly
            user.updatePassword(newPassword).addOnCompleteListener(updateTask -> {
                if (updateTask.isSuccessful()) {
                    Log.d("UpdateUserActivity", "Password updated successfully.");
                    // Save the new password to the database, if necessary
                    databaseReference.child(userId).child("password").setValue(newPassword)
                            .addOnCompleteListener(passwordUpdateTask -> {
                                if (passwordUpdateTask.isSuccessful()) {
                                    Log.d("UpdateUserActivity", "Password saved in database successfully.");
                                } else {
                                    Log.w("UpdateUserActivity", "Error saving password in database", passwordUpdateTask.getException());
                                }
                            });
                } else {
                    Log.w("UpdateUserActivity", "Error updating password", updateTask.getException());
                }
            });
        } else {
            Log.w("UpdateUserActivity", "No user is logged in, cannot update password.");
        }
    }


}
