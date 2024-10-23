package com.example.xemphim.activity;

import static java.lang.Double.parseDouble;
import static java.lang.Float.parseFloat;
import static java.lang.Integer.parseInt;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LegendEntry;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
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
import java.util.ArrayList;
import java.util.Calendar;

public class AdminActivity extends AppCompatActivity {

    private ActivityAdminBinding binding;
    private DatabaseReference dataUser;
    private DatabaseReference dataTruyCap;
    private DatabaseReference dataThanhToan;
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
        dataThanhToan = FirebaseDatabase.getInstance().getReference("ThanhToan");
        // Đặt lại khoảng thời gian cho hôm nay
        layThongTinUser();
        updateSelectedButton(binding.btnHMNay);
        layThongTInDoanhThu();
        layThongTinTruyCapHomNay(); // Gọi hàm lấy thông tin truy cập hôm nay


        xulyXemThongTin();
        xulybuttonMenu();



    }

    private void xulyBieuDo() {
        // Lấy giá trị từ các TextView
        String doanhThuText = binding.tvDoanhThuAmount.getText().toString();
        String truyCapText = binding.tvTruyCapAmount.getText().toString();
        String luotDangKyText = binding.tvLuotDangKyAmount.getText().toString();
        String goiVIPText = binding.tvGoiVIPAmount.getText().toString();

        // Chuyển đổi giá trị từ TextView thành float
        float doanhThu = parseValue(doanhThuText);
        float truyCap = parseValue(truyCapText);
        float luotDangKy = parseValue(luotDangKyText);
        float goiVIP = parseValue(goiVIPText);

        // Tạo dữ liệu cho BarChart
        ArrayList<BarEntry> barEntries = new ArrayList<>();
        barEntries.add(new BarEntry(0, doanhThu));
        barEntries.add(new BarEntry(1, truyCap));
        barEntries.add(new BarEntry(2, luotDangKy));
        barEntries.add(new BarEntry(3, goiVIP));

        BarDataSet barDataSet = new BarDataSet(barEntries, "Thống kê");
        barDataSet.setColors(ColorTemplate.MATERIAL_COLORS);

        BarData barData = new BarData(barDataSet);
        barData.setBarWidth(0.9f); // Độ rộng của cột


        // Cập nhật BarChart
        binding.barChart.setData(barData);
        binding.barChart.setFitBars(true); // Đảm bảo các cột vừa với biểu đồ
        binding.barChart.invalidate(); // Làm mới biểu đồ

        // Tùy chỉnh Legend (chú thích) cho biểu đồ
        Legend legend = binding.barChart.getLegend();
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM); // Đặt legend ở dưới
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER); // Canh giữa
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL); // Hiển thị theo chiều ngang
        legend.setDrawInside(false); // Để bên ngoài biểu đồ
        legend.setWordWrapEnabled(true); // Tự động xuống dòng nếu cần
        legend.setYOffset(10f); // Khoảng cách giữa legend và biểu đồ
        legend.setXOffset(0f); // Khoảng cách bên trái/phải của legend
        legend.setTextSize(12f); // Kích thước chữ của legend

        // Đặt màu sắc và nhãn cho mỗi dữ liệu (thay thế cho tên chung "Thống kê")
        ArrayList<LegendEntry> legendEntries = new ArrayList<>();
        legendEntries.add(new LegendEntry("Doanh thu", Legend.LegendForm.SQUARE, 10f, 2f, null, ColorTemplate.MATERIAL_COLORS[0]));
        legendEntries.add(new LegendEntry("Truy cập", Legend.LegendForm.SQUARE, 10f, 2f, null, ColorTemplate.MATERIAL_COLORS[1]));
        legendEntries.add(new LegendEntry("Lượt đăng ký", Legend.LegendForm.SQUARE, 10f, 2f, null, ColorTemplate.MATERIAL_COLORS[2]));
        legendEntries.add(new LegendEntry("Gói VIP", Legend.LegendForm.SQUARE, 10f, 2f, null, ColorTemplate.MATERIAL_COLORS[3]));

        legend.setCustom(legendEntries); // Đặt các ghi chú tùy chỉnh vào legend

    }
    // Phương thức để chuyển đổi giá trị từ chuỗi thành float
    private float parseValue(String value) {
        value = value.replaceAll("[^\\d.]", ""); // Xóa các ký tự không phải số
        try {
            return Float.parseFloat(value);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
    //xu ly button menu
    private void xulybuttonMenu() {
        binding.ivButtonMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Inflate the menu layout
                LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
                View popupView = inflater.inflate(R.layout.menu_layout_admin, null);

                // Create the PopupWindow
                PopupWindow popupWindow = new PopupWindow(popupView,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        true);

                // Show the PopupWindow at the desired location
                popupWindow.showAsDropDown(binding.ivButtonMenu, 0, 0);

                // Handle Quản lý phim button click
                popupView.findViewById(R.id.btn_quanly_phim).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Chuyển sang trang Quản lý phim
                        Intent intent = new Intent(AdminActivity.this, QLPhimActivity.class);
                        startActivity(intent);
                        popupWindow.dismiss();  // Đóng PopupWindow sau khi nhấn
                    }
                });

                // Handle Doanh thu button click
                popupView.findViewById(R.id.btn_doanh_thu).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Chuyển sang trang Doanh thu
//                        Intent intent = new Intent(AdminActivity.this, QLPhimActivity.class);
//                        startActivity(intent);
//                        popupWindow.dismiss();  // Đóng PopupWindow sau khi nhấn
                    }
                });

                // Handle Quản lý User button click
                popupView.findViewById(R.id.btn_ql_user).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Chuyển sang trang Quản lý User
                        Intent intent = new Intent(AdminActivity.this, QLUserActivity.class);
                        startActivity(intent);
                        popupWindow.dismiss();  // Đóng PopupWindow sau khi nhấn
                    }
                });

                // Handle Quản lý Thể loại button click
                popupView.findViewById(R.id.btn_ql_theloai).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Chuyển sang trang Quản lý Thể loại
                        Intent intent = new Intent(AdminActivity.this, QLTheLoaiActivity.class);
                        startActivity(intent);
                        popupWindow.dismiss();  // Đóng PopupWindow sau khi nhấn
                    }
                });

                // Handle Quản lý Quốc gia button click
                popupView.findViewById(R.id.btn_ql_quocgia).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Chuyển sang trang Quản lý Quốc gia
                        Intent intent = new Intent(AdminActivity.this, QLQuocGiaActivity.class);
                        startActivity(intent);
                        popupWindow.dismiss();  // Đóng PopupWindow sau khi nhấn
                    }
                });

                // Handle Hỗ trợ button click
                popupView.findViewById(R.id.btn_hotro).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Chuyển sang trang Hỗ trợ
//                        Intent intent = new Intent(AdminActivity.this, QLPhimActivity.class);
//                        startActivity(intent);
//                        popupWindow.dismiss();  // Đóng PopupWindow sau khi nhấn
                    }
                });

                // Handle Quản lý API button click
                popupView.findViewById(R.id.btn_ql_api).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Chuyển sang trang Quản lý API
                        Intent intent = new Intent(AdminActivity.this, QuanLyAPI.class);
                        startActivity(intent);
                        popupWindow.dismiss();  // Đóng PopupWindow sau khi nhấn
                    }
                });
            }
        });
    }

    private void xulyXemThongTin() {
        // Lắng nghe sự kiện khi nhấn nút "Hôm nay"
        binding.btnHMNay.setOnClickListener(view -> {
            layThongTinUser();
            updateSelectedButton(binding.btnHMNay);
            layThongTInDoanhThu();
            layThongTinTruyCapHomNay();
            // Gọi hàm lấy thông tin truy cập hôm nay
        });

        // Lắng nghe sự kiện khi nhấn nút "7 ngày qua"
        binding.btn7NgY.setOnClickListener(view -> {
            layThongTinTrongKhoangThoiGian(7);
            updateSelectedButton(binding.btn7NgY);
            laythongtinDoanhThuTrongKhoang(7);
            layThongTinTruyCapTrongKhoangThoiGian(7);
            // Gọi hàm lấy thông tin truy cập 7 ngày qua
        });

        // Lắng nghe sự kiện khi nhấn nút "1 tháng qua"
        binding.btnThang.setOnClickListener(view -> {
            layThongTinTrongKhoangThoiGian(30);
            updateSelectedButton(binding.btnThang);
            laythongtinDoanhThuTrongKhoang(30);
            layThongTinTruyCapTrongKhoangThoiGian(30);
           // Cập nhật số lượng truy cập 1 tháng qua
        });
    }

    // doanh thu
    private void laythongtinDoanhThuTrongKhoang(int soNgay) {

        long startTime = LayThoigianCachDay(soNgay);
        long endTime = System.currentTimeMillis();

        dataThanhToan.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                double doanhthu = 0;
                for (DataSnapshot data : snapshot.getChildren()){
                    Long amount = data.child("amount").getValue(Long.class);
                    Long ngaymua = data.child("paymentDate").getValue(Long.class);
                    if (ngaymua != null && ngaymua >= startTime && ngaymua <= endTime){
                        doanhthu += amount;
                    }

                }
                // Định dạng và hiển thị số lượng gói VIP
                DecimalFormat decimalFormat = new DecimalFormat("#,###");
                String formattedDoanhThu = decimalFormat.format(doanhthu);
                binding.tvDoanhThuAmount.setText(formattedDoanhThu + " đ");
                // Gọi hàm xulyBieuDo() sau khi đã cập nhật dữ liệu
                xulyBieuDo();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }
    private void layThongTInDoanhThu() {
        LayThoigianNgayHomNay(); // 23:59:59 hôm nay

        dataThanhToan.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                double doanhthu = 0;
                for (DataSnapshot data : snapshot.getChildren()){
                    Long amount = data.child("amount").getValue(Long.class);
                    Long ngaymua = data.child("paymentDate").getValue(Long.class);
                    if (ngaymua != null && ngaymua >= startOfDay && ngaymua <= endOfDay){
                        doanhthu += amount;
                    }

                }
                // Định dạng và hiển thị số lượng gói VIP
                DecimalFormat decimalFormat = new DecimalFormat("#,###");
                String formattedDoanhThu = decimalFormat.format(doanhthu);
                binding.tvDoanhThuAmount.setText(formattedDoanhThu + " đ");
                // Gọi hàm xulyBieuDo() sau khi đã cập nhật dữ liệu
                xulyBieuDo();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", "Lỗi khi đọc dữ liệu: " + error.getMessage());
            }
        });
    }

    // Truy Caap
    private void layThongTinTruyCapHomNay() {
        LayThoigianNgayHomNay(); // 23:59:59 hôm nay

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
                // Gọi hàm xulyBieuDo() sau khi đã cập nhật dữ liệu
                xulyBieuDo();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", "Lỗi khi đọc dữ liệu: " + error.getMessage());
            }
        });
    }
    private void layThongTinTruyCapTrongKhoangThoiGian(int soNgay) {

        long startTime = LayThoigianCachDay(soNgay);
        long endTime = System.currentTimeMillis();


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
                // Gọi hàm xulyBieuDo() sau khi đã cập nhật dữ liệu
                xulyBieuDo();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", "Lỗi khi đọc dữ liệu: " + error.getMessage());
            }
        });
    }

    // lay thong tin user va goi vip
    private void layThongTinUser() {
        // Reset calendar về ngày hiện tại
        calendar.setTimeInMillis(System.currentTimeMillis());
        LayThoigianNgayHomNay();

        dataUser.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

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

                        }
                    }
                }



                binding.tvGoiVIPAmount.setText("" + soluongVip);

                binding.tvLuotDangKyAmount.setText("" + userTodayCount);
                // Gọi hàm xulyBieuDo() sau khi đã cập nhật dữ liệu
                xulyBieuDo();
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

    //Lay thoi gian ngay hom nay
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

                int soluongVip = 0;
                int userCount = 0;

                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    Long createdAt = userSnapshot.child("created_at").getValue(Long.class);
                    Long idLoaiND = userSnapshot.child("id_loaiND").getValue(Long.class);

                    if (createdAt != null && createdAt >= startTime && createdAt <= endTime) {
                        userCount++;
                        if (idLoaiND == 1) {
                            soluongVip++;
                        }
                    }
                }


                binding.tvGoiVIPAmount.setText("" + soluongVip);
                binding.tvLuotDangKyAmount.setText("" + userCount);
                // Gọi hàm xulyBieuDo() sau khi đã cập nhật dữ liệu
                xulyBieuDo();
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