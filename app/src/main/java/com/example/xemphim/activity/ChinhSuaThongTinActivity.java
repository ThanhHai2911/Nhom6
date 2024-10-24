package com.example.xemphim.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.xemphim.R;
import com.example.xemphim.databinding.ActivityChinhSuaThongTinBinding;
import com.example.xemphim.model.ThongBaoTrenManHinh;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ChinhSuaThongTinActivity extends AppCompatActivity {
    private ActivityChinhSuaThongTinBinding binding;
    private DatabaseReference databaseReference; // Tham chiếu đến Realtime Database
    private String userId;
    private boolean isPasswordVisible = false;  // Trạng thái của mật khẩu

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityChinhSuaThongTinBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Intent serviceIntent = new Intent(this, ThongBaoTrenManHinh.class);
        startService(serviceIntent);

        // Khởi tạo Firebase Realtime Database
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");

        // Lấy người dùng hiện tại
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            userId = user.getUid(); // Lấy ID người dùng từ Firebase Authentication
        } else {
            // Xử lý trường hợp người dùng chưa đăng nhập
            Log.w("UpdateUserActivity", "Người dùng chưa đăng nhập.");
            finish(); // Đóng Activity nếu người dùng chưa đăng nhập
            return;
        }

        // Thiết lập sự kiện cho nút Lưu
        binding.btnLuu.setOnClickListener(view -> updateUserInfo());
        binding.btnThoat.setOnClickListener(view -> finish()); // Đóng activity nếu nhấn Huỷ
        xemMatKhauCu();
        xemMatKhauMoi();
    }

    private void updateUserInfo() {
        String currentPassword = binding.edtMkCu.getText().toString().trim(); // Mật khẩu hiện tại
        String newPassword = binding.edtMkMoi.getText().toString().trim(); // Mật khẩu mới

        // Kiểm tra nếu các trường rỗng
        if (currentPassword.isEmpty() || newPassword.isEmpty()) {
            Log.w("UpdateUserActivity", "Vui lòng điền đầy đủ thông tin.");
            return; // Dừng nếu có trường rỗng
        }

        // Kiểm tra nếu userId không null
        if (userId == null) {
            Log.w("UpdateUserActivity", "Không tìm thấy ID người dùng, không thể cập nhật thông tin.");
            return;
        }

        reauthenticateAndChangePassword(currentPassword, newPassword);
    }

    private void reauthenticateAndChangePassword(String currentPassword, String newPassword) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            // Lấy email của người dùng (giả định người dùng đăng nhập bằng email và mật khẩu)
            String email = user.getEmail();

            // Xác thực người dùng bằng mật khẩu hiện tại
            AuthCredential credential = EmailAuthProvider.getCredential(email, currentPassword);
            user.reauthenticate(credential).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Log.d("UpdateUserActivity", "Xác thực thành công.");

                    // Cập nhật mật khẩu sau khi xác thực thành công
                    user.updatePassword(newPassword).addOnCompleteListener(updateTask -> {
                        if (updateTask.isSuccessful()) {
                            Log.d("UpdateUserActivity", "Đổi mật khẩu thành công.");

                            // Lưu mật khẩu mới vào cơ sở dữ liệu (tùy chọn, không khuyến nghị lưu mật khẩu)
                            databaseReference.child(userId).child("password").setValue(newPassword)
                                    .addOnCompleteListener(passwordUpdateTask -> {
                                        if (passwordUpdateTask.isSuccessful()) {
                                            Log.d("UpdateUserActivity", "Lưu mật khẩu mới vào cơ sở dữ liệu thành công.");
                                        } else {
                                            Log.w("UpdateUserActivity", "Lỗi khi lưu mật khẩu mới vào cơ sở dữ liệu.", passwordUpdateTask.getException());
                                        }
                                    });
                        } else {
                            Log.w("UpdateUserActivity", "Lỗi khi cập nhật mật khẩu.", updateTask.getException());
                        }
                    });
                } else {
                    Log.w("UpdateUserActivity", "Xác thực thất bại. Kiểm tra lại mật khẩu hiện tại.", task.getException());
                    Toast.makeText(ChinhSuaThongTinActivity.this, "Mật khẩu cũ không trùng khớp.", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Log.w("UpdateUserActivity", "Người dùng chưa đăng nhập, không thể xác thực.");
        }
    }
    private void xemMatKhauCu() {
        // Thiết lập sự kiện nhấn vào biểu tượng con mắt
        binding.edtMkCu.setOnTouchListener((v, event) -> {
            final int DRAWABLE_RIGHT = 2;  // Vị trí của drawableEnd (con mắt) là vị trí thứ 2 (bên phải)
            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (event.getRawX() >= (binding.edtMkCu.getRight() - binding.edtMkCu.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                    // Kiểm tra trạng thái hiện tại của mật khẩu
                    if (isPasswordVisible) {
                        // Nếu mật khẩu đang hiển thị, chuyển sang ẩn
                        binding.edtMkCu.setTransformationMethod(PasswordTransformationMethod.getInstance());
                        binding.edtMkCu.setCompoundDrawablesWithIntrinsicBounds(R.drawable.baseline_lock_outline_24, 0, R.drawable.baseline_visibility_off_24, 0);
                    } else {
                        // Nếu mật khẩu đang ẩn, chuyển sang hiển thị
                        binding.edtMkCu.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                        binding.edtMkCu.setCompoundDrawablesWithIntrinsicBounds(R.drawable.baseline_lock_outline_24, 0, R.drawable.baseline_visibility_24, 0);
                    }
                    // Thay đổi trạng thái
                    isPasswordVisible = !isPasswordVisible;
                    binding.edtMkCu.setSelection(binding.edtMkCu.getText().length()); // Để con trỏ vẫn ở cuối EditText
                    return true;
                }
            }
            return false;
        });
    }
    private void xemMatKhauMoi() {
        // Thiết lập sự kiện nhấn vào biểu tượng con mắt
        binding.edtMkMoi.setOnTouchListener((v, event) -> {
            final int DRAWABLE_RIGHT = 2;  // Vị trí của drawableEnd (con mắt) là vị trí thứ 2 (bên phải)
            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (event.getRawX() >= (binding.edtMkMoi.getRight() - binding.edtMkMoi.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                    // Kiểm tra trạng thái hiện tại của mật khẩu
                    if (isPasswordVisible) {
                        // Nếu mật khẩu đang hiển thị, chuyển sang ẩn
                        binding.edtMkMoi.setTransformationMethod(PasswordTransformationMethod.getInstance());
                        binding.edtMkMoi.setCompoundDrawablesWithIntrinsicBounds(R.drawable.baseline_lock_outline_24, 0, R.drawable.baseline_visibility_off_24, 0);
                    } else {
                        // Nếu mật khẩu đang ẩn, chuyển sang hiển thị
                        binding.edtMkMoi.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                        binding.edtMkMoi.setCompoundDrawablesWithIntrinsicBounds(R.drawable.baseline_lock_outline_24, 0, R.drawable.baseline_visibility_24, 0);
                    }
                    // Thay đổi trạng thái
                    isPasswordVisible = !isPasswordVisible;
                    binding.edtMkMoi.setSelection(binding.edtMkMoi.getText().length()); // Để con trỏ vẫn ở cuối EditText
                    return true;
                }
            }
            return false;
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

