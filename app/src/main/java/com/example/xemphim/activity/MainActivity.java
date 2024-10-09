package com.example.xemphim.activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.xemphim.API.ApiClient;
import com.example.xemphim.API.ApiService;
import com.example.xemphim.R;
import com.example.xemphim.adapter.MovieAdapter;
import com.example.xemphim.adapter.SeriesAdapter;
import com.example.xemphim.adapter.TheLoaiAdapter;
import com.example.xemphim.databinding.ActivityMainBinding;
import com.example.xemphim.model.Movie;
import com.example.xemphim.model.Series;
import com.example.xemphim.response.MovieResponse;
import com.example.xemphim.response.SeriesResponse;

import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import androidx.appcompat.widget.Toolbar;
import androidx.appcompat.widget.SearchView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.PopupMenu;
import android.widget.PopupWindow;
import android.widget.Toast;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;


public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private ApiService apiService;
    private SwipeRefreshLayout swipeRefreshLayout;
    private String movieSlug;
    private PopupWindow popupWindow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        apiService = ApiClient.getClient().create(ApiService.class);
        // Thiết lập ActionBar và DrawerLayout
        setSupportActionBar(binding.toobar1);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, binding.drawerlayout, binding.toobar1,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close
        );

        binding.drawerlayout.addDrawerListener(toggle);
        toggle.syncState();

        // Đặt biểu tượng trở về
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Hiện biểu tượng trở về
        }
        // Ẩn tiêu đề
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        swipeRefreshLayout = binding.swipeRefreshLayout; // Khởi tạo SwipeRefreshLayout
        movieSlug = getIntent().getStringExtra("slug");
        swipeRefreshLayout.setOnRefreshListener(() -> {
            loadMovies(); // Tải lại danh sách phim
            loadSeries(); // Tải lại danh sach phim bo
            loadTVShow();// Tải lại danh sách tvshow
            loadPhimLe();
            loadPhimHoatHinh();
        });

        setupRecyclerViews();
        loadMovies();
        loadSeries();
        loadTVShow();
        loadPhimLe();
        loadPhimHoatHinh();


    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
         // Nạp menu
        getMenuInflater().inflate(R.menu.menu_timkiem, menu);
        // Tìm kiếm item trong menu
        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();

        // Thiết lập listener cho sự kiện tìm kiếm
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // Khi người dùng nhấn vào nút tìm kiếm trên bàn phím
                // Hiển thị nội dung tìm kiếm qua Toast
                Toast.makeText(MainActivity.this, "Tìm kiếm: " + query, Toast.LENGTH_SHORT).show();

                // Gọi API tìm kiếm với từ khóa và giới hạn 10 kết quả
                apiService.searchMovies(query, 10).enqueue(new Callback<SeriesResponse>() {
                    @Override
                    public void onResponse(Call<SeriesResponse> call, Response<SeriesResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            List<Series> series = response.body().getData().getItems();
                            binding.recyclerViewMovies.setAdapter(new SeriesAdapter(MainActivity.this, series));

                            // Đóng SearchView sau khi tìm kiếm
                            searchView.clearFocus();
                        } else {
                            Toast.makeText(MainActivity.this, "Không tìm thấy phim", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<SeriesResponse> call, Throwable t) {
                        Toast.makeText(MainActivity.this, "Lỗi khi tìm kiếm", Toast.LENGTH_SHORT).show();
                    }
                });

                //searchView.clearFocus();
                searchItem.collapseActionView();

                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // Xử lý khi nội dung tìm kiếm thay đổi (nếu cần)
                return false;
            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_search) {
            // Xử lý sự kiện khi nhấn vào tìm kiếm
            Toast.makeText(this, "Bạn muốn tìm kiếm gì", Toast.LENGTH_SHORT).show();
            return true;
        } else if (id == R.id.nav_phimbo) {
            // Xử lý sự kiện khi nhấn vào thông báo
            Toast.makeText(this, "Thông báo được nhấn", Toast.LENGTH_SHORT).show();
            return true;
        }else if (id == R.id.nav_theloai) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void setupRecyclerViews() {
        binding.recyclerViewMovies.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        binding.recyclerViewSeries.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        binding.recyclerViewtvShow.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        binding.recyclerViewphimle.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        binding.recyclerViewphimhoathinh.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
    }

    private void loadMovies() {
        // Hiển thị ProgressBar và ẩn nội dung chính
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.mainContent.setVisibility(View.GONE);

        apiService.getMovies(1).enqueue(new Callback<MovieResponse>() {
            @Override
            public void onResponse(Call<MovieResponse> call, Response<MovieResponse> response) {
                // Ẩn ProgressBar và hiển thị nội dung chính
                binding.progressBar.setVisibility(View.GONE);
                binding.mainContent.setVisibility(View.VISIBLE);
                swipeRefreshLayout.setRefreshing(false); // Ngừng loading

                if (response.isSuccessful() && response.body() != null) {
                    List<Movie> movies = response.body().getItems();
                    binding.recyclerViewMovies.setAdapter(new MovieAdapter(MainActivity.this, movies));
                }
            }

            @Override
            public void onFailure(Call<MovieResponse> call, Throwable t) {
                // Ẩn ProgressBar và thông báo lỗi
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(MainActivity.this, "Lỗi khi tải dữ liệu", Toast.LENGTH_SHORT).show();
                binding.mainContent.setVisibility(View.VISIBLE); // Hiển thị lại nội dung chính
                swipeRefreshLayout.setRefreshing(false); // Ngừng loading
            }
        });
    }


    private void loadSeries() {
        apiService.getSeries().enqueue(new Callback<SeriesResponse>() {
            @Override
            public void onResponse(Call<SeriesResponse> call, Response<SeriesResponse> response) {
                // Ẩn ProgressBar và hiển thị nội dung chính
                binding.progressBar.setVisibility(View.GONE);
                binding.mainContent.setVisibility(View.VISIBLE);
                swipeRefreshLayout.setRefreshing(false); // Ngừng loading

                if (response.isSuccessful() && response.body() != null) {
                    List<Series> series = response.body().getData().getItems();
                    binding.recyclerViewSeries.setAdapter(new SeriesAdapter(MainActivity.this, series));
                }
            }

            @Override
            public void onFailure(Call<SeriesResponse> call, Throwable t) {
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(MainActivity.this, "Lỗi khi tải dữ liệu", Toast.LENGTH_SHORT).show();
                binding.mainContent.setVisibility(View.VISIBLE); // Hiển thị lại nội dung chính
                swipeRefreshLayout.setRefreshing(false); // Ngừng loading
            }
        });
    }

    private void loadPhimLe() {
        apiService.getPhimLe().enqueue(new Callback<SeriesResponse>() {
            @Override
            public void onResponse(Call<SeriesResponse> call, Response<SeriesResponse> response) {
                // Ẩn ProgressBar và hiển thị nội dung chính
                binding.progressBar.setVisibility(View.GONE);
                binding.mainContent.setVisibility(View.VISIBLE);
                swipeRefreshLayout.setRefreshing(false); // Ngừng loading

                if (response.isSuccessful() && response.body() != null) {
                    List<Series> series = response.body().getData().getItems();
                    binding.recyclerViewphimle.setAdapter(new SeriesAdapter(MainActivity.this, series));
                }
            }

            @Override
            public void onFailure(Call<SeriesResponse> call, Throwable t) {
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(MainActivity.this, "Lỗi khi tải dữ liệu", Toast.LENGTH_SHORT).show();
                binding.mainContent.setVisibility(View.VISIBLE); // Hiển thị lại nội dung chính
                swipeRefreshLayout.setRefreshing(false); // Ngừng loading
            }
        });
    }

    private void loadTVShow() {
        apiService.getTVShow().enqueue(new Callback<SeriesResponse>() {
            @Override
            public void onResponse(Call<SeriesResponse> call, Response<SeriesResponse> response) {
                // Ẩn ProgressBar và hiển thị nội dung chính
                binding.progressBar.setVisibility(View.GONE);
                binding.mainContent.setVisibility(View.VISIBLE);
                swipeRefreshLayout.setRefreshing(false); // Ngừng loading

                if (response.isSuccessful() && response.body() != null) {
                    List<Series> series = response.body().getData().getItems();
                    binding.recyclerViewtvShow.setAdapter(new SeriesAdapter(MainActivity.this, series));
                }
            }

            @Override
            public void onFailure(Call<SeriesResponse> call, Throwable t) {
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(MainActivity.this, "Lỗi khi tải dữ liệu", Toast.LENGTH_SHORT).show();
                binding.mainContent.setVisibility(View.VISIBLE); // Hiển thị lại nội dung chính
                swipeRefreshLayout.setRefreshing(false); // Ngừng loading
            }
        });
    }

    private void loadPhimHoatHinh() {
        apiService.getHoatHinh().enqueue(new Callback<SeriesResponse>() {
            @Override
            public void onResponse(Call<SeriesResponse> call, Response<SeriesResponse> response) {
                // Ẩn ProgressBar và hiển thị nội dung chính
                binding.progressBar.setVisibility(View.GONE);
                binding.mainContent.setVisibility(View.VISIBLE);
                swipeRefreshLayout.setRefreshing(false); // Ngừng loading

                if (response.isSuccessful() && response.body() != null) {
                    List<Series> series = response.body().getData().getItems();
                    binding.recyclerViewphimhoathinh.setAdapter(new SeriesAdapter(MainActivity.this, series));
                }
            }

            @Override
            public void onFailure(Call<SeriesResponse> call, Throwable t) {
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(MainActivity.this, "Lỗi khi tải dữ liệu", Toast.LENGTH_SHORT).show();
                binding.mainContent.setVisibility(View.VISIBLE); // Hiển thị lại nội dung chính
                swipeRefreshLayout.setRefreshing(false); // Ngừng loading
            }
        });
    }

    public void laydulieu() {

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
