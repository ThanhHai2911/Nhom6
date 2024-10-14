package com.example.xemphim.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.xemphim.R;
import com.example.xemphim.adapter.LichSuXemAdapter;
import com.example.xemphim.adapter.MovieAdapter;
import com.example.xemphim.adapter.TheLoaiAdapter;
import com.example.xemphim.databinding.ActivityProfileBinding;
import com.example.xemphim.model.Movie;
import com.example.xemphim.model.MovieDetail;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ProfileActivity extends AppCompatActivity {
    private ActivityProfileBinding binding;
    private LichSuXemAdapter lichSuXemAdapter;
    private List<MovieDetail.MovieItem> watchedMoviesList;
    private DatabaseReference databaseReference;
    private String idUser;
    private  String nameUser;
    private String emailUser;
    private int idLoaiND;
    private TextView tvTenNguoiDung, tvEmail;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        // Initialize Data Binding
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setControl();
        setEven();

    }
    public void setControl(){
        tvTenNguoiDung = findViewById(R.id.tvTenNguoiDung);
        tvEmail = findViewById(R.id.tvEmail);



        // Initialize Firebase Database reference
        databaseReference = FirebaseDatabase.getInstance().getReference("watched_movies"); // Ensure the path is correct

        watchedMoviesList = new ArrayList<>();
        binding.rcvLichSu.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        binding.rcvLichSu.setAdapter(lichSuXemAdapter);

    }
    public void setEven(){
        laythongtinUser();
        Toast.makeText(ProfileActivity.this, "Xin chào " + nameUser, Toast.LENGTH_SHORT).show();
        tvTenNguoiDung.setText(nameUser);
        tvEmail.setText(emailUser);
        binding.btnDangNhap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfileActivity.this, DangNhapActivity.class);
                startActivity(intent);
            }
        });
        setupBottomNavigation();
        loadWatchHistory();
    }

    private void loadWatchHistory() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Please log in to view your watch history.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Reference to the user's watch history in Firebase
        DatabaseReference userHistoryRef = FirebaseDatabase.getInstance()
                .getReference("LichSuXem")
                .child(user.getUid()); // Using user ID to get the user's history

        userHistoryRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                watchedMoviesList.clear(); // Clear the previous list
                for (DataSnapshot movieSnapshot : snapshot.getChildren()) {
                    MovieDetail.MovieItem movieItem = movieSnapshot.getValue(MovieDetail.MovieItem.class);
                    if (movieItem != null) {
                        watchedMoviesList.add(movieItem); // Add to the list
                    }
                }
                // Set the adapter with the new data
                lichSuXemAdapter = new LichSuXemAdapter(ProfileActivity.this, watchedMoviesList);
                binding.rcvLichSu.setAdapter(lichSuXemAdapter);
                lichSuXemAdapter.notifyDataSetChanged(); // Notify the adapter of data changes
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("ProfileActivity", "Failed to read watch history: " + error.getMessage());
            }
        });
    }

    private void laythongtinUser(){
        SharedPreferences sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
        idUser = sharedPreferences.getString("id_user", null);
        nameUser = sharedPreferences.getString("name", null);
        emailUser  = sharedPreferences.getString("email", null);
        idLoaiND = sharedPreferences.getInt("id_loaiND", 0);

    }


    private void setupBottomNavigation() {
        // Xử lý sự kiện chọn item của Bottom Navigation
        binding.bottomNavigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Intent intent = null;
                if (item.getItemId() == R.id.nav_home) {
                    intent = new Intent(ProfileActivity.this, MainActivity.class);
                } else if (item.getItemId() == R.id.nav_vip) {
                    intent = new Intent(ProfileActivity.this, VipActivity.class);
                } else if (item.getItemId() == R.id.nav_download) {
                    intent = new Intent(ProfileActivity.this, DownLoadActivity.class);
                } else if (item.getItemId() == R.id.nav_profile) {
                    return true;

                }
                // Pass the selected item to the new Activity
                if (intent != null) {
                    intent.putExtra("selected_item_id", item.getItemId());
                    startActivity(intent);
                    overridePendingTransition(0, 0);  // No animation for smooth transition
                    return true;
                }
                return false;

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