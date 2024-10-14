package com.example.xemphim.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.example.xemphim.API.ApiService;
import com.example.xemphim.R;
import com.example.xemphim.adapter.MovieAdapter;
import com.example.xemphim.adapter.TapPhimAdapter;
import com.example.xemphim.adapter.ThongTinLichSuAdapter;
import com.example.xemphim.databinding.ActivityProfileBinding;
import com.example.xemphim.model.Movie;
import com.example.xemphim.model.MovieDetail;
import com.example.xemphim.response.MovieResponse;
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

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileActivity extends AppCompatActivity {
    private ActivityProfileBinding binding;
    private ThongTinLichSuAdapter thongTinLichSuAdapter;
    private List<MovieDetail.MovieItem> watchedMoviesList;
    private DatabaseReference databaseReference;
    private ApiService apiService;

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
        // Initialize Firebase Database reference
        databaseReference = FirebaseDatabase.getInstance().getReference("watched_movies"); // Ensure the path is correct

        watchedMoviesList = new ArrayList<>();
        binding.rcvLichSu.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        binding.rcvLichSu.setAdapter(thongTinLichSuAdapter);
    }
    public void setEven(){
        binding.btnDangNhap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfileActivity.this, DangNhapActivity.class);
                startActivity(intent);
            }
        });
        binding.tvXemtatca.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ProfileActivity.this, LichSuXemActivity.class);
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

        DatabaseReference userHistoryRef = FirebaseDatabase.getInstance()
                .getReference("LichSuXem")
                .child(user.getUid());

        userHistoryRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                watchedMoviesList.clear(); // Clear the previous list
                for (DataSnapshot movieSnapshot : snapshot.getChildren()) {
                    MovieDetail.MovieItem movieItem = movieSnapshot.getValue(MovieDetail.MovieItem.class);
                    if (movieItem != null) {
                        watchedMoviesList.add(movieItem);
                    }
                }

                thongTinLichSuAdapter = new ThongTinLichSuAdapter(ProfileActivity.this, watchedMoviesList);
                thongTinLichSuAdapter.setRecyclerViewItemClickListener((view, position) -> {
                    MovieDetail.MovieItem movie = watchedMoviesList.get(position);
                    // Assume the movie has a method to get the current episode; adjust as needed
                    String currentEpisode = movie.getEpisodeCurrent();

                    // Update the Firebase with the new episode
                    userHistoryRef.child(movie.getSlug()).child("episode").setValue(currentEpisode)
                            .addOnSuccessListener(aVoid -> {
                                Intent intent = new Intent(view.getContext(), ChiTietActivity.class);
                                intent.putExtra("slug", movie.getSlug());
                                view.getContext().startActivity(intent);
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(ProfileActivity.this, "Failed to update episode.", Toast.LENGTH_SHORT).show();
                            });
                });

                // Set the adapter with the updated list and click listener
                binding.rcvLichSu.setAdapter(thongTinLichSuAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("ProfileActivity", "Failed to read watch history: " + error.getMessage());
            }
        });
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