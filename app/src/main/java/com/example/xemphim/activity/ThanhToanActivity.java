package com.example.xemphim.activity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.xemphim.R;
import com.example.xemphim.model.PaymentInfo;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

public class ThanhToanActivity extends AppCompatActivity {
    private TextView tvPaymentContent;
    private Button btnPayment;
    private ImageButton btnCopy;
    private DatabaseReference paymentRef;
    private String idUser;
    private String nameUser;
    private String emailUser;
    private int idLoaiND;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_thanh_toan);

        // Ánh xạ các view
        tvPaymentContent = findViewById(R.id.tvPaymentContent);
        btnPayment = findViewById(R.id.btnPayment);
        btnCopy = findViewById(R.id.btnCopy); // Nút sao chép nội dung thanh toán

        // Khởi tạo Firebase database reference
        paymentRef = FirebaseDatabase.getInstance().getReference("YeuCau");

        // Lấy thông tin người dùng từ SharedPreferences
        laythongtinUser();

        // Tạo nội dung thanh toán
        generatePaymentContent(idUser);

        // Xử lý sự kiện nhấn nút "Đã thanh toán"
        btnPayment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showConfirmationDialog(); // Hiển thị hộp thoại xác nhận
            }
        });

        // Xử lý sự kiện nhấn nút "Sao chép nội dung thanh toán"
        btnCopy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                copyPaymentContent(); // Thực hiện sao chép nội dung thanh toán
            }
        });
    }

    private void laythongtinUser() {
        SharedPreferences sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
        idUser = sharedPreferences.getString("id_user", null);
        nameUser = sharedPreferences.getString("name", null);
        emailUser = sharedPreferences.getString("email", null);
        idLoaiND = sharedPreferences.getInt("id_loaiND", 0);
    }

    private void generatePaymentContent(String maKH) {
        // Tạo nội dung thanh toán ban đầu
        String initialContent = maKH + "_" + generateRandomString(5);

        // Kiểm tra nếu nội dung thanh toán đã tồn tại
        isPaymentContentExists(initialContent, new OnCheckPaymentContentListener() {
            @Override
            public void onResult(boolean exists, String content) {
                if (!exists) {
                    tvPaymentContent.setText(content); // Cập nhật nội dung thanh toán nếu chưa tồn tại
                } else {
                    // Nếu đã tồn tại, thông báo cho người dùng
                    Toast.makeText(ThanhToanActivity.this, "Nội dung thanh toán đã tồn tại, thử lại!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private String generateRandomString(int length) {
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        String characters = "0123456789abcdefghijklmnopqrstuvwxyz";
        for (int i = 0; i < length; i++) {
            int index = random.nextInt(characters.length());
            sb.append(characters.charAt(index));
        }
        return sb.toString();
    }

    private void isPaymentContentExists(String content, OnCheckPaymentContentListener listener) {
        Query query = paymentRef.orderByChild("content").equalTo(content);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Nếu nội dung đã tồn tại, gọi lại hàm để tạo chuỗi ngẫu nhiên mới
                listener.onResult(dataSnapshot.exists(), content);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(ThanhToanActivity.this, "Lỗi khi kiểm tra nội dung thanh toán", Toast.LENGTH_SHORT).show();
                listener.onResult(false, content); // Gọi lại listener mặc định nếu có lỗi
            }
        });
    }

    private void showConfirmationDialog() {
        // Hiển thị hộp thoại xác nhận thanh toán
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận thanh toán")
                .setMessage("Bạn có chắc chắn đã thanh toán không?")
                .setPositiveButton("Có", (dialog, which) -> {
                    savePaymentInfo(); // Lưu thông tin thanh toán nếu người dùng xác nhận
                })
                .setNegativeButton("Không", (dialog, which) -> {
                    dialog.dismiss(); // Đóng hộp thoại nếu người dùng không đồng ý
                })
                .show();
    }

    private void copyPaymentContent() {
        // Sao chép nội dung thanh toán vào clipboard
        String content = tvPaymentContent.getText().toString();
        if (!TextUtils.isEmpty(content)) {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("PaymentContent", content);
            clipboard.setPrimaryClip(clip);

            Toast.makeText(this, "Đã sao chép nội dung thanh toán", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Nội dung thanh toán trống", Toast.LENGTH_SHORT).show();
        }
    }

    private void savePaymentInfo() {
        String content = tvPaymentContent.getText().toString();
        if (TextUtils.isEmpty(content)) {
            Toast.makeText(this, "Nội dung thanh toán không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }

        // Lấy ngày giờ hiện tại và định dạng
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
        String ngayThanhToan = sdf.format(new Date());

        String idLichSuTT = paymentRef.push().getKey(); // Tạo id lịch sử thanh toán mới
        PaymentInfo paymentInfo = new PaymentInfo(idLichSuTT, idUser, content, 99000, ngayThanhToan, 0); // Lưu ngày thanh toán

        paymentRef.child(idLichSuTT).setValue(paymentInfo)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(ThanhToanActivity.this, "Thanh toán thành công", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(ThanhToanActivity.this, "Thanh toán thất bại", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Định nghĩa lại interface cho callback
    interface OnCheckPaymentContentListener {
        void onResult(boolean exists, String content);
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
