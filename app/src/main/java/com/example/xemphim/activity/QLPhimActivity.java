package com.example.xemphim.activity;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.xemphim.R;
import com.example.xemphim.adapter.QLPhimAdapter;
import com.example.xemphim.model.QLPhim;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class QLPhimActivity extends AppCompatActivity {
    private Button btnNgayTao ,btnNamPhatHanh, btnTrangThai;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qlphim);

        xulyRecyclerView();
        // Khởi tạo Button
         btnNgayTao = findViewById(R.id.btnngaytao);
        btnNamPhatHanh = findViewById(R.id.btnnamphathanh);
        btnTrangThai = findViewById(R.id.btntrangthai);
        // Xử lý sự kiện khi nhấn vào Button Ngày tạo
        btnNgayTao.setOnClickListener(v -> showDatePickerDialog());
        // Xử lý sự kiện khi nhấn vào Button Năm phát hành
        btnNamPhatHanh.setOnClickListener(v -> showYearPickerDialog());

        btnTrangThai.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chonTrangThai();
            }
        });


    }

    private void chonTrangThai() {
        // Tạo một mảng các lựa chọn trạng thái
        final String[] statusOptions = {"Hoạt động", "Đóng"};

        // Tạo AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Chọn trạng thái");

        // Gán các tùy chọn vào dialog
        builder.setItems(statusOptions, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Xử lý khi người dùng chọn một trạng thái
                String selectedStatus = statusOptions[which];
                // Cập nhật văn bản của nút "Trạng thái" thành giá trị được chọn
                btnTrangThai.setText(selectedStatus);
                Toast.makeText(QLPhimActivity.this, "Trạng thái đã chọn: " + selectedStatus, Toast.LENGTH_SHORT).show();
            }
        });

        // Hiển thị AlertDialog
        builder.show();
    }

    // Hiển thị DatePickerDialog để chọn Năm
    private void showYearPickerDialog() {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);

        // Tạo DatePickerDialog nhưng chỉ cho phép chọn năm
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                QLPhimActivity.this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    // Cập nhật text cho Button Năm phát hành
                    btnNamPhatHanh.setText(String.valueOf(selectedYear));
                },
                year, 0, 1);

        // Lấy DatePicker từ dialog
        DatePicker datePicker = datePickerDialog.getDatePicker();

        // Kiểm tra và ẩn các phần tử ngày và tháng nếu tồn tại
        int dayId = getResources().getIdentifier("day", "id", "android");
        int monthId = getResources().getIdentifier("month", "id", "android");

        if (dayId != 0) {
            View dayView = datePicker.findViewById(dayId);
            if (dayView != null) {
                dayView.setVisibility(View.GONE);
            }
        }

        if (monthId != 0) {
            View monthView = datePicker.findViewById(monthId);
            if (monthView != null) {
                monthView.setVisibility(View.GONE);
            }
        }

        // Hiển thị dialog
        datePickerDialog.show();
    }

    // Hiển thị DatePickerDialog
    private void showDatePickerDialog() {
        // Lấy ngày hiện tại
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        // Tạo DatePickerDialog
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                QLPhimActivity.this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    // Xử lý khi chọn ngày
                    String selectedDate = selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear;
                    // Cập nhật text cho Button hoặc thực hiện hành động khác
                    btnNgayTao.setText(selectedDate);
                },
                year, month, day);

        // Hiển thị DatePickerDialog
        datePickerDialog.show();
    }

    private void xulyRecyclerView() {
        // Inside your activity or fragment

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

// Prepare the movie list
        List<QLPhim> phimList = new ArrayList<>();
        phimList.add(new QLPhim("Chú chuột meo", "2024", "(phim bộ) Tập: 20??", "[Hài] [Gia đình] [Tình cảm]", "Hoạt động", "20/02/2024", R.drawable.poster_url));
        phimList.add(new QLPhim("Chú chuột meo", "2024", "(phim bộ) Tập: 20??", "[Hài] [Gia đình] [Tình cảm]", "Hoạt động", "20/02/2024", R.drawable.poster_url));
        phimList.add(new QLPhim("Chú chuột meo", "2024", "(phim bộ) Tập: 20??", "[Hài] [Gia đình] [Tình cảm]", "Hoạt động", "20/02/2024", R.drawable.poster_url));

        // Add more QLPhim objects as needed

// Set the adapter
        QLPhimAdapter adapter = new QLPhimAdapter(this, phimList);
        recyclerView.setAdapter(adapter);
    }
}