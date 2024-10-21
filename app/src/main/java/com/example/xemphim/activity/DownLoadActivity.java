package com.example.xemphim.activity;


import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;

import com.example.xemphim.R;
import com.example.xemphim.adapter.DownloadedMoviesAdapter;
import com.example.xemphim.databinding.ActivityDownLoadBinding;
import com.example.xemphim.model.MovieItem;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class DownLoadActivity extends AppCompatActivity {
    private ActivityDownLoadBinding binding; // Sử dụng Binding
    private DownloadedMoviesAdapter adapter;
    private List<MovieItem> downloadedMovies = new ArrayList<>(); // Danh sách phim đã tải

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        // Khởi tạo binding
        binding = ActivityDownLoadBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        // Thiết lập RecyclerView với GridLayoutManager (3 cột)
        binding.recyclerViewDownloadedMovies.setLayoutManager(new GridLayoutManager(this, 3));

        // Lấy danh sách phim đã tải
        loadDownloadedMovies();

        // Thiết lập adapter cho RecyclerView
        adapter = new DownloadedMoviesAdapter(downloadedMovies, this::playDownloadedMovie);
        binding.recyclerViewDownloadedMovies.setAdapter(adapter);

        BottomNavigationView bottomNavigationView = binding.bottomNavigation;

        // Đặt item mặc định là màn hình Download
        bottomNavigationView.setSelectedItemId(R.id.nav_download);

        // Xử lý sự kiện chọn item của Bottom Navigation
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            Intent intent = null;

            if (item.getItemId() == R.id.nav_home) {
                intent = new Intent(DownLoadActivity.this, MainActivity.class);
            } else if (item.getItemId() == R.id.nav_vip) {
                intent = new Intent(DownLoadActivity.this, VipActivity.class);
            } else if (item.getItemId() == R.id.nav_profile) {
                intent = new Intent(DownLoadActivity.this, ProfileActivity.class);
            } else if (item.getItemId() == R.id.nav_download) {
                return true; // Không khởi tạo lại Activity
            }

            if (intent != null) {
                intent.putExtra("selected_item_id", item.getItemId());
                startActivity(intent);
                overridePendingTransition(0, 0); // Không có hoạt ảnh cho chuyển đổi mượt mà
            }
            return true;
        });
    }

    private void loadDownloadedMovies() {
        // Lấy thư mục chứa phim đã tải
        File movieDir = new File(getExternalFilesDir(Environment.DIRECTORY_MOVIES), "MyMovies");

        if (movieDir.exists() && movieDir.isDirectory()) {
            File[] movieFiles = movieDir.listFiles();
            if (movieFiles != null) {
                for (File movieFile : movieFiles) {
                    // Kiểm tra nếu là thư mục
                    if (movieFile.isDirectory()) {
                        String movieName = movieFile.getName();
                        File posterFile = new File(movieFile, movieName + "_poster.jpg");

                        // Kiểm tra nếu tệp poster tồn tại
                        if (posterFile.exists()) {
                            downloadedMovies.add(new MovieItem(movieName, posterFile.getAbsolutePath()));
                        }
                    }
                }
            }
        }
    }

    // Phát phim đã tải khi nhấn vào
    private void playDownloadedMovie(MovieItem movieItem) {
        Intent intent = new Intent(this, PlayDownload.class);
        intent.putExtra("movie_name", movieItem.getName());
        startActivity(intent);
    }
}

