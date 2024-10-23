package com.example.xemphim.activity;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.xemphim.R;
import com.example.xemphim.databinding.ActivityHoTroBinding;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

public class HoTroActivity extends AppCompatActivity {

    private static final int PICK_IMAGE = 1; // Mã yêu cầu chọn ảnh
    private ActivityHoTroBinding binding;  // View Binding
    private Uri imageUri;  // Để lưu URI của ảnh đã chọn

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHoTroBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Lắng nghe sự kiện khi người dùng nhấn vào imgUpload để chọn ảnh
        binding.imgUpload.setOnClickListener(view -> openGallery());

        // Lắng nghe sự kiện khi người dùng nhấn vào nút gửi yêu cầu
        binding.btnSubmit.setOnClickListener(view -> submitForm());
    }

    // Mở thư viện ảnh để chọn
    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*"); // Bộ lọc tất cả các định dạng ảnh
        startActivityForResult(Intent.createChooser(intent, "Chọn một ảnh"), PICK_IMAGE);
    }

    // Xử lý kết quả sau khi chọn ảnh
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            imageUri = data.getData();
            binding.imgUpload.setImageURI(imageUri);  // Hiển thị ảnh đã chọn
        }
    }

    // Gửi form hỗ trợ
    private void submitForm() {
        String name = binding.inputTicketCode.getText().toString().trim();
        String description = binding.inputDescription.getText().toString().trim();

        // Kiểm tra nếu thông tin không đầy đủ
        if (name.isEmpty() || description.isEmpty()) {
            Toast.makeText(this, "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        // Tải ảnh lên Firebase Storage trước, rồi sau đó lưu thông tin vào Firebase Realtime Database
        uploadImageAndSubmitRequest(name, description);
    }

    // Tải ảnh lên Firebase Storage và lưu thông tin vào Realtime Database
    private void uploadImageAndSubmitRequest(String name, String description) {
        if (imageUri == null) {
            Toast.makeText(this, "Không tìm thấy ảnh. Vui lòng chọn lại ảnh.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Tham chiếu tới Firebase Storage
        StorageReference storageRef = FirebaseStorage.getInstance().getReference("support_images")
                .child(System.currentTimeMillis() + "." + getFileExtension(imageUri));


        // Thực hiện tải lên tệp
        storageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    storageRef.getDownloadUrl().addOnSuccessListener(downloadUri -> {
                        String imageUrl = downloadUri.toString();
                        saveToFirebaseRealtimeDatabase(name, description, imageUrl);
                    });
                })
                .addOnFailureListener(e -> {
                    Log.e("Upload Error", "Không thể tải lên hình ảnh: " + e.getMessage());
                    Toast.makeText(this, "Lỗi tải lên ảnh: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    // Phương thức để lấy phần mở rộng của tệp ảnh
    private String getFileExtension(Uri uri) {
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    // Lưu thông tin vào Firebase Realtime Database
    private void saveToFirebaseRealtimeDatabase(String name, String description, String imageUrl) {
        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference("HoTro");

        String requestId = databaseRef.push().getKey(); // Tạo ID duy nhất cho yêu cầu
        Map<String, Object> supportRequest = new HashMap<>();
        supportRequest.put("name", name);
        supportRequest.put("description", description);
        supportRequest.put("imageUrl", imageUrl);

        if (requestId != null) {
            databaseRef.child(requestId)
                    .setValue(supportRequest)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Gửi yêu cầu thành công", Toast.LENGTH_SHORT).show();
                        resetForm();  // Xóa form sau khi gửi thành công
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Không thể gửi yêu cầu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }

    // Xóa form sau khi gửi thành công
    private void resetForm() {
        binding.inputTicketCode.setText("");
        binding.inputDescription.setText("");
        binding.imgUpload.setImageResource(R.drawable.baseline_upload_file_24);  // Đặt lại ảnh mặc định
        imageUri = null;
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
