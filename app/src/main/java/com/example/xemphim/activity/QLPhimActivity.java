package com.example.xemphim.activity;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
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
    private ImageView deleteIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qlphim);

        // Khởi tạo RecyclerView và danh sách phim
        phimList = new ArrayList<>();
        kieuPhimList = new ArrayList<>();
        goiList = new ArrayList<>();
        recyclerView = findViewById(R.id.recyclerView);

        // Khởi tạo Button
        btnNgayTao = findViewById(R.id.btnngaytao);
        btnNamPhatHanh = findViewById(R.id.btnnamphathanh);
        btnTrangThai = findViewById(R.id.btntrangthai);
        deleteIcon = findViewById(R.id.delete_icon); // Icon xóa

        // Cài đặt RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new QLPhimAdapter(this, phimList, kieuPhimList, goiList, selectedCount -> {
            if (selectedCount > 0) {
                deleteIcon.setVisibility(View.VISIBLE); // Hiển thị icon xóa nếu có phim được chọn
            } else {
                deleteIcon.setVisibility(View.GONE); // Ẩn icon xóa nếu không có phim nào được chọn
            }
        });
        recyclerView.setAdapter(adapter);

        // Xử lý sự kiện cho các nút
        btnNgayTao.setOnClickListener(v -> showDatePickerDialog());
        btnNamPhatHanh.setOnClickListener(v -> showYearPickerDialog());
        btnTrangThai.setOnClickListener(v -> chonTrangThai());

        // Tải dữ liệu từ Firebase
        fetchKieuPhimFromFirebase();
        fetchGoiFromFirebase();
        fetchMoviesFromFirebase();

        // Xử lý sự kiện xóa phim khi nhấn vào icon xóa
        deleteIcon.setOnClickListener(v -> {
            List<Phim> selectedMovies = adapter.getSelectedMovies();
            if (!selectedMovies.isEmpty()) {
                deleteSelectedMovies(selectedMovies);
            } else {
                Toast.makeText(this, "Không có phim nào được chọn", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteSelectedMovies(List<Phim> selectedMovies) {
        for (Phim phim : selectedMovies) {
            // Xóa phim từ Firebase
            FirebaseDatabase.getInstance().getReference("Movies")
                    .child(phim.getId_movie())
                    .removeValue();
        }
        // Cập nhật lại giao diện
        adapter.getSelectedMovies().clear(); // Xóa danh sách phim đã chọn
        adapter.notifyDataSetChanged(); // Cập nhật RecyclerView
        deleteIcon.setVisibility(View.GONE); // Ẩn icon xóa sau khi xóa
        Toast.makeText(this, "Đã xóa " + selectedMovies.size() + " phim", Toast.LENGTH_SHORT).show();
    }

    private void fetchMoviesFromFirebase() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Movies");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                phimList.clear(); // Xóa dữ liệu cũ (nếu có)
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Phim movie = snapshot.getValue(Phim.class);
                    if (movie != null) {
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
        final String[] statusOptions = {"Hoạt động", "Đóng"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Chọn trạng thái");
        builder.setItems(statusOptions, (dialog, which) -> {
            String selectedStatus = statusOptions[which];
            btnTrangThai.setText(selectedStatus);
            Toast.makeText(QLPhimActivity.this, "Trạng thái đã chọn: " + selectedStatus, Toast.LENGTH_SHORT).show();
        });
        builder.show();
    }

    private void showYearPickerDialog() {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                QLPhimActivity.this,
                (view, selectedYear, selectedMonth, selectedDay) -> btnNamPhatHanh.setText(String.valueOf(selectedYear)),
                year, 0, 1);

        DatePicker datePicker = datePickerDialog.getDatePicker();
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

        datePickerDialog.show();
    }

    private void showDatePickerDialog() {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                QLPhimActivity.this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    String selectedDate = selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear;
                    btnNgayTao.setText(selectedDate);
                },
                year, month, day);

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
        DatabaseReference goiRef = FirebaseDatabase.getInstance().getReference("Goi");
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
