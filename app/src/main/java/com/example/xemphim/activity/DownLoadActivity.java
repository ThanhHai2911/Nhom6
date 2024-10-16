package com.example.xemphim.activity;


import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.MenuItem;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.xemphim.R;
import com.example.xemphim.adapter.DownloadedMoviesAdapter;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DownLoadActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private DownloadedMoviesAdapter adapter;
    private List<File> downloadedMovies = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_down_load);
        recyclerView = findViewById(R.id.recyclerViewDownloadedMovies);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Lấy danh sách phim đã tải
        loadDownloadedMovies();

        // Thiết lập adapter cho RecyclerView
        adapter = new DownloadedMoviesAdapter(downloadedMovies, this::playDownloadedMovie);
        recyclerView.setAdapter(adapter);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        // Đặt item mặc định được chọn là màn hình Download
        bottomNavigationView.setSelectedItemId(R.id.nav_download);

        // Xử lý sự kiện chọn item của Bottom Navigation
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Intent intent = null;

                if (item.getItemId() == R.id.nav_home) {
                    intent = new Intent(DownLoadActivity.this, MainActivity.class);
                } else if (item.getItemId() == R.id.nav_vip) {
                    intent = new Intent(DownLoadActivity.this, VipActivity.class);
                } else if (item.getItemId() == R.id.nav_profile) {
                    intent = new Intent(DownLoadActivity.this, ProfileActivity.class);
                } else if (item.getItemId() == R.id.nav_download) {
                    return true; // Trả về true để không khởi tạo lại Activity
                }

                if (intent != null) {
                    intent.putExtra("selected_item_id", item.getItemId());
                    startActivity(intent);
                    overridePendingTransition(0, 0);  // Không có hoạt ảnh cho chuyển đổi mượt mà
                }
                return true;
            }
        });
    }

    private void loadDownloadedMovies() {
        // Lấy thư mục chứa phim đã tải
        File movieDir = new File(getExternalFilesDir(Environment.DIRECTORY_MOVIES), "MyMovies");

        if (movieDir.exists() && movieDir.isDirectory()) {
            File[] movieFiles = movieDir.listFiles();
            if (movieFiles != null) {
                downloadedMovies.addAll(Arrays.asList(movieFiles));
            }
        }
    }

    // Phát phim đã tải khi nhấn vào
    private void playDownloadedMovie(File movieFile) {
        Intent intent = new Intent(this, PlayDownloadedMovieActivity.class);

        // Truyền tên phim thay vì đường dẫn
        intent.putExtra("movie_name", movieFile.getName());
        startActivity(intent);
    }
}
