package com.example.xemphim.activity;

import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;
import com.google.firebase.database.Transaction;
import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.xemphim.R;
import com.example.xemphim.databinding.ActivityAdminBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DecimalFormat;
import java.util.Calendar;

public class AdminActivity extends AppCompatActivity {

    private ActivityAdminBinding binding;
    private DatabaseReference dataUser;
    private DatabaseReference dataTruyCap;
    private long startOfDay;
    private long endOfDay;
    private Calendar calendar = Calendar.getInstance();

    private Button selectedButton = null; // Biến để lưu nút hiện tại

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
       binding = ActivityAdminBinding.inflate(getLayoutInflater());
       setContentView(binding.getRoot());

        dataUser = FirebaseDatabase.getInstance().getReference("Users");
        dataTruyCap = FirebaseDatabase.getInstance().getReference("TruyCap");
        // Đặt lại khoảng thời gian cho hôm nay
        layThongTinUserVaDoanhThu();
        layThongTinTruyCapHomNay(); // Gọi hàm lấy thông tin truy cập hôm nay

        // Lắng nghe sự kiện khi nhấn nút "Hôm nay"
        binding.btnHMNay.setOnClickListener(view -> {
            layThongTinUserVaDoanhThu();
            updateSelectedButton(binding.btnHMNay);
            layThongTinTruyCapHomNay(); // Gọi hàm lấy thông tin truy cập hôm nay
        });

        // Lắng nghe sự kiện khi nhấn nút "7 ngày qua"
        binding.btn7NgY.setOnClickListener(view -> {
            layThongTinTrongKhoangThoiGian(7);
            updateSelectedButton(binding.btn7NgY);
            layThongTinTruyCapTrongKhoangThoiGian(7); // Gọi hàm lấy thông tin truy cập 7 ngày qua
        });

        // Lắng nghe sự kiện khi nhấn nút "1 tháng qua"
        binding.btnThang.setOnClickListener(view -> {
            layThongTinTrongKhoangThoiGian(30);
            updateSelectedButton(binding.btnThang);
            layThongTinTruyCapTrongKhoangThoiGian(30); // Cập nhật số lượng truy cập 1 tháng qua
        });
    }
    private void layThongTinTruyCapHomNay() {
        long currentTime = System.currentTimeMillis();
        long startOfDay = currentTime - (currentTime % (24 * 60 * 60 * 1000)); // 00:00:00 hôm nay
        long endOfDay = startOfDay + (24 * 60 * 60 * 1000) - 1; // 23:59:59 hôm nay

        dataTruyCap.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int count = 0;
                for (DataSnapshot accessSnapshot : snapshot.getChildren()) {
                    Long timestamp = accessSnapshot.child("thoigiantruycap").getValue(Long.class);
                    if (timestamp != null && timestamp >= startOfDay && timestamp <= endOfDay) {
                        count++;
                    }
                }

                binding.tvTruyCapAmount.setText("" + count);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", "Lỗi khi đọc dữ liệu: " + error.getMessage());
            }
        });
    }



    private void layThongTinTruyCapTrongKhoangThoiGian(int soNgay) {

        long currentTime = System.currentTimeMillis();
        long startTime = LayThoigianCachDay(soNgay);
        long endTime = currentTime;

        dataTruyCap.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int count = 0;
                for (DataSnapshot accessSnapshot : snapshot.getChildren()) {
                    Long timestamp = accessSnapshot.child("thoigiantruycap").getValue(Long.class);
                    if (timestamp != null && timestamp >= startTime && timestamp <= endTime) {
                        count++;
                    }
                }

                binding.tvTruyCapAmount.setText("" + count);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", "Lỗi khi đọc dữ liệu: " + error.getMessage());
            }
        });
    }





    private void layThongTinUserVaDoanhThu() {
        // Reset calendar về ngày hiện tại
        calendar.setTimeInMillis(System.currentTimeMillis());
        LayThoigianNgayHomNay();

        dataUser.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                double DoanhThu = 0;
                int soluongVip = 0;
                int userTodayCount = 0;

                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    Long createdAt = userSnapshot.child("created_at").getValue(Long.class);
                    Long idLoaiND = userSnapshot.child("id_loaiND").getValue(Long.class);

                    // Kiểm tra thời gian và tính toán
                    if (createdAt != null && createdAt >= startOfDay && createdAt <= endOfDay) {
                        userTodayCount++; // Tăng số lượng người dùng đăng ký hôm nay

                        if (idLoaiND != null && idLoaiND == 1) {
                            soluongVip++; // Tăng số lượng gói VIP
                            DoanhThu += 99000; // Cộng doanh thu
                        }
                    }
                }

                // Định dạng và hiển thị số lượng gói VIP
                DecimalFormat decimalFormat = new DecimalFormat("#,###");
                String formattedDoanhThu = decimalFormat.format(DoanhThu);

                binding.tvGoiVIPAmount.setText("" + soluongVip);
                binding.tvDoanhThuAmount.setText(formattedDoanhThu + " đ");
                binding.tvLuotDangKyAmount.setText("" + userTodayCount);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", "Lỗi khi đọc dữ liệu: " + error.getMessage());
            }
        });
    }

    // Phương thức để cập nhật màu của các nút
    private void updateSelectedButton(Button newButton) {
        // Nếu nút đã được chọn khác nút hiện tại, đổi màu
        if (selectedButton != null) {
            selectedButton.setBackgroundTintList(getResources().getColorStateList(R.color.colorDefault)); // Màu mặc định
            selectedButton.setTextColor(getResources().getColor(R.color.defaultTextColor)); // Màu chữ mặc định
        }

        // Cập nhật nút hiện tại và đổi màu
        selectedButton = newButton;
        selectedButton.setBackgroundTintList(getResources().getColorStateList(R.color.colorSelected)); // Màu đã chọn
        selectedButton.setTextColor(getResources().getColor(R.color.selectedTextColor)); // Màu chữ đã chọn
    }


    private void LayThoigianNgayHomNay() {
        // Đặt thời gian về 00:00:00 hôm nay
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        startOfDay = calendar.getTimeInMillis();

        // Đặt thời gian về 23:59:59 hôm nay
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        endOfDay = calendar.getTimeInMillis();
    }
    // Phương thức để hiển thị thông tin cho 7 ngày qua và 1 tháng qua
    private void layThongTinTrongKhoangThoiGian(int soNgay) {
        calendar.setTimeInMillis(System.currentTimeMillis()); // Đặt lại về hiện tại
        long startTime = LayThoigianCachDay(soNgay);
        long endTime = System.currentTimeMillis();

        dataUser.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                double DoanhThu = 0;
                int soluongVip = 0;
                int userCount = 0;

                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    Long createdAt = userSnapshot.child("created_at").getValue(Long.class);
                    Long idLoaiND = userSnapshot.child("id_loaiND").getValue(Long.class);

                    if (createdAt != null && createdAt >= startTime && createdAt <= endTime) {
                        userCount++;
                        if (idLoaiND == 1) {
                            soluongVip++;
                            DoanhThu += 99000;
                        }
                    }
                }

                DecimalFormat decimalFormat = new DecimalFormat("#,###");
                String formattedDoanhThu = decimalFormat.format(DoanhThu);

                binding.tvGoiVIPAmount.setText("" + soluongVip);
                binding.tvDoanhThuAmount.setText(formattedDoanhThu + " đ");
                binding.tvLuotDangKyAmount.setText("" + userCount);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", "Lỗi khi đọc dữ liệu: " + error.getMessage());
            }
        });
    }

    // Phương thức tính toán thời gian từ số ngày trước (7 ngày, 30 ngày)
    private long LayThoigianCachDay(int soNgay) {
        calendar.add(Calendar.DAY_OF_YEAR, -soNgay);
        return calendar.getTimeInMillis();
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