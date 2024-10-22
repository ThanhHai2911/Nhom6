package com.example.xemphim.activity;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.xemphim.R;
import com.example.xemphim.adapter.QLPhimAdapter;
import com.example.xemphim.model.Goi;
import com.example.xemphim.model.KieuPhim;
import com.example.xemphim.model.Phim;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class QLPhimActivity extends AppCompatActivity {
    private Button btnNgayTao, btnNamPhatHanh, btnTrangThai;
    private RecyclerView recyclerView;
    private QLPhimAdapter adapter;
    private List<Phim> phimList; // Danh sách phim
    private List<KieuPhim> kieuPhimList;
    private List<Goi> goiList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qlphim);

        // Khởi tạo RecyclerView và danh sách phim
        phimList = new ArrayList<>();
        recyclerView = findViewById(R.id.recyclerView);


        // Khởi tạo Button
        btnNgayTao = findViewById(R.id.btnngaytao);
        btnNamPhatHanh = findViewById(R.id.btnnamphathanh);
        btnTrangThai = findViewById(R.id.btntrangthai);

        // Xử lý sự kiện cho các nút
        btnNgayTao.setOnClickListener(v -> showDatePickerDialog());
        btnNamPhatHanh.setOnClickListener(v -> showYearPickerDialog());
        btnTrangThai.setOnClickListener(v -> chonTrangThai());

        // Tải phim từ Firebase
        fetchMoviesFromFirebase();
        kieuPhimList = new ArrayList<>();
        goiList = new ArrayList<>();
        fetchKieuPhimFromFirebase();
        fetchGoiFromFirebase();
        fetchMoviesFromFirebase();

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new QLPhimAdapter(this, phimList,kieuPhimList, goiList);
        recyclerView.setAdapter(adapter);
    }

    private void fetchMoviesFromFirebase() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Movies");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                phimList.clear(); // Xóa dữ liệu cũ (nếu có)
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    // Lấy dữ liệu từ snapshot
                    Phim movie = snapshot.getValue(Phim.class);
                    if (movie != null) {
                        // Kiểm tra và thêm vào danh sách
                        phimList.add(movie);
                    }
                }
                // Cập nhật RecyclerView sau khi có dữ liệu
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("QLPhimActivity", "Failed to fetch data", databaseError.toException());
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
        builder.setItems(statusOptions, (dialog, which) -> {
            // Xử lý khi người dùng chọn một trạng thái
            String selectedStatus = statusOptions[which];
            // Cập nhật văn bản của nút "Trạng thái" thành giá trị được chọn
            btnTrangThai.setText(selectedStatus);
            Toast.makeText(QLPhimActivity.this, "Trạng thái đã chọn: " + selectedStatus, Toast.LENGTH_SHORT).show();
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

    private void fetchKieuPhimFromFirebase() {
        DatabaseReference kieuPhimRef = FirebaseDatabase.getInstance().getReference("kieuPhim");
        kieuPhimRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                kieuPhimList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    KieuPhim kieuPhim = snapshot.getValue(KieuPhim.class);
                    if (kieuPhim != null) {
                        kieuPhimList.add(kieuPhim);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("QLPhimActivity", "Failed to fetch kieuPhim", databaseError.toException());
            }
        });
    }

    private void fetchGoiFromFirebase() {
        DatabaseReference goiRef = FirebaseDatabase.getInstance().getReference("goi");
        goiRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                goiList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Goi goi = snapshot.getValue(Goi.class);
                    if (goi != null) {
                        goiList.add(goi);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("QLPhimActivity", "Failed to fetch goi", databaseError.toException());
            }
        });
    }
}
