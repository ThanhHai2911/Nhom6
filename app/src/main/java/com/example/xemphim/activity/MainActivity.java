    package com.example.xemphim.activity;
    import android.content.Intent;
    import android.content.SharedPreferences;
    import android.os.Bundle;
    import java.util.Date;
    import androidx.annotation.NonNull;
    import androidx.appcompat.app.ActionBarDrawerToggle;
    import androidx.appcompat.app.AppCompatActivity;
    import androidx.recyclerview.widget.LinearLayoutManager;

    import com.example.xemphim.API.ApiClient;
    import com.example.xemphim.API.ApiService;
    import com.example.xemphim.R;
    import com.example.xemphim.adapter.BannerAdapter;
    import com.example.xemphim.adapter.MovieAdapter;
    import com.example.xemphim.adapter.PhimAdapter;
    import com.example.xemphim.adapter.SeriesAdapter;
    import com.example.xemphim.databinding.ActivityMainBinding;
    import com.example.xemphim.model.Movie;
    import com.example.xemphim.model.Phim;
    import com.example.xemphim.model.Series;
    import com.example.xemphim.model.TruyCap;
    import com.example.xemphim.response.MovieResponse;
    import com.example.xemphim.response.SeriesResponse;
    import com.google.android.material.navigation.NavigationView;

    import java.text.SimpleDateFormat;
    import java.util.ArrayList;
    import java.util.List;
    import java.util.Locale;

    import retrofit2.Call;
    import retrofit2.Callback;
    import retrofit2.Response;

    import androidx.appcompat.widget.SearchView;

    import android.os.Handler;
    import android.util.Log;
    import android.view.Menu;
    import android.view.MenuItem;
    import android.view.View;
    import android.view.WindowManager;
    import android.widget.PopupWindow;
    import android.widget.TextView;
    import android.widget.Toast;
    import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
    import com.google.android.material.bottomnavigation.BottomNavigationView;
    import com.google.firebase.database.DataSnapshot;
    import com.google.firebase.database.DatabaseError;
    import com.google.firebase.database.DatabaseReference;
    import com.google.firebase.database.FirebaseDatabase;
    import com.google.firebase.database.ValueEventListener;

    public class MainActivity extends AppCompatActivity {
        private boolean doubleBackToExitPressedOnce = false;
        private ActivityMainBinding binding;
        private ApiService apiService;
        private SwipeRefreshLayout swipeRefreshLayout;
        private String movieSlug;
        private PopupWindow popupWindow;
        private MovieAdapter movieAdapter;
        private SeriesAdapter seriesAdapter;
        private String idUser;
        private  String nameUser;
        private String emailUser;
        private int idLoaiND;
        private BannerAdapter bannerAdapter;
        private PhimAdapter phimAdapter;
        private DatabaseReference movieRef;
        private List<Phim> movieList;
        private List<Series> seriesPhimLe;
        private List<Series> seriesPhimBo;
        private List<Series> seriesPhimHoatHinh;
        private List<Series> seriesTvShow;
        // bien de kiểm tra người dùng có đang ỏ trong ứng dụng hay không
        public static Boolean truycap = false;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            binding = ActivityMainBinding.inflate(getLayoutInflater());
            setContentView(binding.getRoot());
            seriesPhimLe = new ArrayList<>();
            seriesPhimBo = new ArrayList<>();
            seriesPhimHoatHinh = new ArrayList<>();
            seriesTvShow = new ArrayList<>();
            binding.xemThemPhimBo.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, AllMovie.class);
                intent.putParcelableArrayListExtra("seriesList", new ArrayList<>(seriesPhimBo)); // Chuyển danh sách phim bộ
                intent.putExtra("type", "series"); // Thêm loại phim bộ
                startActivity(intent);
            });

            binding.xemThemPhimLe.setOnClickListener(v -> {
                Log.d("MainActivity", "xemThemPhimLe clicked");
                Intent intent = new Intent(MainActivity.this, AllMovie.class);
                intent.putParcelableArrayListExtra("phimLe", new ArrayList<>(seriesPhimLe)); // Danh sách phim lẻ
                intent.putExtra("type", "movie"); // Thêm loại phim lẻ
                startActivity(intent);
            });
            binding.xemThemTVshow.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, AllMovie.class);
                intent.putParcelableArrayListExtra("tVshow", new ArrayList<>(seriesTvShow)); // Chuyển danh sách phim bộ
                intent.putExtra("type", "tvShow"); // Thêm loại phim bộ
                startActivity(intent);
            });
            binding.xemThemHoatHinh.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, AllMovie.class);
                intent.putParcelableArrayListExtra("hoatHinh", new ArrayList<>(seriesPhimHoatHinh)); // Chuyển danh sách phim bộ
                intent.putExtra("type", "hoathinh"); // Thêm loại phim bộ
                startActivity(intent);
            });

            // truy cập thông tin người dùng.
            laythongtinUser();
            Toast.makeText(MainActivity.this, "Xin chào " + nameUser, Toast.LENGTH_SHORT).show();
            updateUser();

            // Kiểm tra và thêm thông tin truy cập
            kiemTraTruyCap(idUser);

            apiService = ApiClient.getClient().create(ApiService.class);
            // Thiết lập ActionBar và DrawerLayout
            setSupportActionBar(binding.toolbar);

            ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                    this, binding.drawerlayout, binding.toolbar,
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
            swipeRefreshLayout.setOnRefreshListener(() -> {
              //  loadMovies(); // Tải lại danh sách phim
                loadSeries(); // Tải lại danh sach phim bo
                loadTVShow();// Tải lại danh sách tvshow
                loadPhimLe();
                loadPhimHoatHinh();
                fetchMoviesFromFirebase();
                binding.tvDanhSachTimKiem.setVisibility(View.GONE);
                binding.recyclerViewMovies.setVisibility(View.GONE);
            });
            Banner();
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            movieRef = database.getReference("movies"); // Đây là nơi lưu trữ thông tin phim trên Firebase

            // Khởi tạo movieList và RecyclerView
            movieList = new ArrayList<>();
            phimAdapter = new PhimAdapter(this, movieList);
            binding.recyclerViewphim.setLayoutManager(new LinearLayoutManager(this));
            binding.recyclerViewphim.setAdapter(phimAdapter);

            setupRecyclerViews();
            // Khởi tạo Firebase Realtime Database reference

            // Load phim từ Firebase
            fetchMoviesFromFirebase();
           // loadMovies();
            loadSeries();
            loadTVShow();
            loadPhimLe();
            loadPhimHoatHinh();

            navigationBottom();

        }

         public static void kiemTraTruyCap(String idUser) {
            // Kiểm tra xem id_user có null hay không và xem ngày truy cập đã tồn tại hay chưa
                DatabaseReference truyCapRef = FirebaseDatabase.getInstance().getReference("TruyCap");
                long currentTime = System.currentTimeMillis();

                // Lấy ngày hiện tại (không bao gồm giờ, phút, giây)
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                String currentDate = sdf.format(new Date(currentTime));

                // Tìm kiếm bản ghi theo id_user và ngày truy cập
                truyCapRef.orderByChild("id_user").equalTo(idUser).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        // neu truycap == false thì sẽ thêm vào TruyCap trên firebase
                        if (truycap == false){
                            themTruyCap(idUser);
                            truycap = true;
                        }

//                        boolean exists = false;
//
//                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
//                            TruyCap truyCap = snapshot.getValue(TruyCap.class);
//                            if (truyCap != null) {
//                                // So sánh ngày truy cập
//                                long timeStamp = truyCap.getThoigiantruycap();
//                                String truyCapDate = sdf.format(new Date(timeStamp));
//
//                                if (truyCapDate.equals(currentDate)) {
//                                    exists = true; // Nếu đã có bản ghi cho ngày hôm nay
//                                    break;
//                                }
//                            }
//                        }
//
//                        if (!exists) {
//                            // Nếu không có bản ghi nào, gọi phương thức thêm truy cập
//
//                        } else {
//                            Log.d("TruyCap", "Người dùng đã truy cập hôm nay.");
//                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.e("TruyCap", "Lỗi khi kiểm tra truy cập: " + databaseError.getMessage());
                    }
                });
            }


        public static void themTruyCap(String idUser) {
            DatabaseReference truyCapRef = FirebaseDatabase.getInstance().getReference("TruyCap");
            long currentTime = System.currentTimeMillis();

            // Tạo một ID mới cho bản ghi truy cập
            String truyCapId = truyCapRef.push().getKey();
            TruyCap truyCap = new TruyCap(idUser, currentTime);

            // Thêm thông tin truy cập vào Firebase
            truyCapRef.child(truyCapId).setValue(truyCap)
                    .addOnSuccessListener(aVoid -> {
                        // Xử lý thành công
                        Log.d("TruyCap", "Thêm truy cập thành công cho người dùng: " + idUser);
                    })
                    .addOnFailureListener(e -> {
                        // Xử lý lỗi
                        Log.e("TruyCap", "Lỗi khi thêm truy cập: " + e.getMessage());
                    });
        }





        private void laythongtinUser(){
            SharedPreferences sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
            idUser = sharedPreferences.getString("id_user", null);
            nameUser = sharedPreferences.getString("name", null);
            emailUser  = sharedPreferences.getString("email", null);
            idLoaiND = sharedPreferences.getInt("id_loaiND", 0);

        }
        private void updateUser(){
            // Tham chiếu đến NavigationView
            NavigationView navigationView = findViewById(R.id.navigationView);  // Giả sử NavigationView có id là nav_view

            // Lấy header view từ NavigationView
            View headerView = navigationView.getHeaderView(0);

            // Tham chiếu đến TextView trong header view
            TextView textView = headerView.findViewById(R.id.tvTenNguoiDung); // Thay bằng id của TextView trong layout_header

            if(nameUser != null){
                // Thay đổi nội dung TextView
                textView.setText(nameUser);
            }else{
                textView.setText("Khách");
            }
        }
        private void navigationBottom() {
            // Đặt item mặc định được chọn là màn hình Home
            binding.bottomNavigation.setSelectedItemId(R.id.nav_home);

            // Xử lý sự kiện chọn item của Bottom Navigation
            binding.bottomNavigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    Intent intent = null;
                    if (item.getItemId() == R.id.nav_home) {
                        intent = new Intent(MainActivity.this, MainActivity.class);
                    } else if (item.getItemId() == R.id.nav_vip) {
                        intent = new Intent(MainActivity.this, VipActivity.class);
                    } else if (item.getItemId() == R.id.nav_download) {
                        intent = new Intent(MainActivity.this, DownLoadActivity.class);
                    } else if (item.getItemId() == R.id.nav_profile) {
                        intent = new Intent(MainActivity.this, ProfileActivity.class);

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
                                // Nếu có kết quả, hiển thị kết quả tìm kiếm
                                if (!series.isEmpty()) {
                                    // Show the search results section
                                    binding.tvDanhSachTimKiem.setVisibility(View.VISIBLE);
                                    binding.recyclerViewMovies.setVisibility(View.VISIBLE);

                                    // Set up the adapter with search results
                                    binding.recyclerViewMovies.setAdapter(new SeriesAdapter(MainActivity.this, series));
                                } else {
                                    // If no results, hide the search results section
                                    binding.tvDanhSachTimKiem.setVisibility(View.GONE);
                                    binding.recyclerViewMovies.setVisibility(View.GONE);
                                    Toast.makeText(MainActivity.this, "Không tìm thấy phim", Toast.LENGTH_SHORT).show();
                                }

                                // Đóng SearchView sau khi tìm kiếm
                                searchView.clearFocus();
                            } else {
                                binding.tvDanhSachTimKiem.setVisibility(View.GONE);
                                binding.recyclerViewMovies.setVisibility(View.GONE);
                                Toast.makeText(MainActivity.this, "Không tìm thấy phim", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<SeriesResponse> call, Throwable t) {
                            binding.tvDanhSachTimKiem.setVisibility(View.GONE);
                            binding.recyclerViewMovies.setVisibility(View.GONE);
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
            binding.recyclerViewphim.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        }
        private void fetchMoviesFromFirebase() {
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Movies");
            databaseReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    movieList.clear(); // Xóa dữ liệu cũ (nếu có)
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Phim movie = snapshot.getValue(Phim.class);
                        if (movie != null) {
                            movieList.add(movie);
                        }
                    }
                    // Cập nhật RecyclerView sau khi có dữ liệu
                    phimAdapter.notifyDataSetChanged();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.e("MainActivity", "Failed to fetch data", databaseError.toException());
                }
            });
        }
//        private void loadMovies() {
//            // Hiển thị ProgressBar và ẩn nội dung chính
//            binding.progressBar.setVisibility(View.VISIBLE);
//            binding.mainContent.setVisibility(View.GONE);
//
//            apiService.getMovies(1).enqueue(new Callback<MovieResponse>() {
//                @Override
//                public void onResponse(Call<MovieResponse> call, Response<MovieResponse> response) {
//                    // Ẩn ProgressBar và hiển thị nội dung chính
//                    binding.progressBar.setVisibility(View.GONE);
//                    binding.mainContent.setVisibility(View.VISIBLE);
//                    swipeRefreshLayout.setRefreshing(false); // Ngừng loading
//
//                    if (response.isSuccessful() && response.body() != null) {
//                        List<Movie> movies = response.body().getItems();
//                        // Khởi tạo MovieAdapter
//                        movieAdapter = new MovieAdapter(MainActivity.this, movies);
//                        // Thiết lập sự kiện click cho từng item
//                        movieAdapter.setRecyclerViewItemClickListener(new MovieAdapter.OnRecyclerViewItemClickListener() {
//                            @Override
//                            public void onItemClick(View view, int position) {
//                                //Lay thong tin chi tiet phim tu slug truyen den man hinh chi tiet phim
//                                Intent intent = new Intent(view.getContext(), ChiTietActivity.class);
//                                Movie movie = movies.get(position);
//                                intent.putExtra("slug", movie.getSlug());
//                                view.getContext().startActivity(intent);
//                            }
//                        });
//
//                    }
//                }
//
//                @Override
//                public void onFailure(Call<MovieResponse> call, Throwable t) {
//                    // Ẩn ProgressBar và thông báo lỗi
//                    binding.progressBar.setVisibility(View.GONE);
//                    Toast.makeText(MainActivity.this, "Lỗi khi tải dữ liệu", Toast.LENGTH_SHORT).show();
//                    binding.mainContent.setVisibility(View.VISIBLE); // Hiển thị lại nội dung chính
//                    swipeRefreshLayout.setRefreshing(false); // Ngừng loading
//                }
//            });
//        }


        private void loadSeries() {
            // Giả sử bạn muốn bắt đầu từ trang 1
            int page = 1;
            apiService.getSeries(page).enqueue(new Callback<SeriesResponse>() {
                @Override
                public void onResponse(Call<SeriesResponse> call, Response<SeriesResponse> response) {
                    // Ẩn ProgressBar và hiển thị nội dung chính
                    binding.progressBar.setVisibility(View.GONE);
                    binding.mainContent.setVisibility(View.VISIBLE);
                    binding.bottomNavigation.setVisibility(View.VISIBLE);
                    // Ngừng loading
                    swipeRefreshLayout.setRefreshing(false); // Ngừng loading

                    if (response.isSuccessful() && response.body() != null) {
                        seriesPhimBo = response.body().getData().getItems();
                        binding.recyclerViewSeries.setAdapter(new SeriesAdapter(MainActivity.this, seriesPhimBo));
                    }
                }

                @Override
                public void onFailure(Call<SeriesResponse> call, Throwable t) {
                    binding.progressBar.setVisibility(View.GONE);
                    binding.bottomNavigation.setVisibility(View.GONE);
                    Toast.makeText(MainActivity.this, "Lỗi khi tải dữ liệu", Toast.LENGTH_SHORT).show();
                    binding.mainContent.setVisibility(View.VISIBLE); // Hiển thị lại nội dung chính
                    swipeRefreshLayout.setRefreshing(false); // Ngừng loading
                }
            });
        }
        private void loadPhimLe() {
            int page = 1;
            apiService.getPhimLe(page).enqueue(new Callback<SeriesResponse>() {
                @Override
                public void onResponse(Call<SeriesResponse> call, Response<SeriesResponse> response) {
                    // Ẩn ProgressBar và hiển thị nội dung chính
                    binding.progressBar.setVisibility(View.GONE);
                    binding.mainContent.setVisibility(View.VISIBLE);
                    binding.bottomNavigation.setVisibility(View.VISIBLE);
                    swipeRefreshLayout.setRefreshing(false); // Ngừng loading

                    if (response.isSuccessful() && response.body() != null) {
                        seriesPhimLe = response.body().getData().getItems();
                        binding.recyclerViewphimle.setAdapter(new SeriesAdapter(MainActivity.this, seriesPhimLe));
                    }
                }

                @Override
                public void onFailure(Call<SeriesResponse> call, Throwable t) {
                    binding.progressBar.setVisibility(View.GONE);
                    binding.bottomNavigation.setVisibility(View.GONE);
                    Toast.makeText(MainActivity.this, "Lỗi khi tải dữ liệu", Toast.LENGTH_SHORT).show();
                    binding.mainContent.setVisibility(View.VISIBLE); // Hiển thị lại nội dung chính
                    swipeRefreshLayout.setRefreshing(false); // Ngừng loading
                }
            });
        }

        private void loadTVShow() {
            int page = 1;
            apiService.getTVShow(page).enqueue(new Callback<SeriesResponse>() {
                @Override
                public void onResponse(Call<SeriesResponse> call, Response<SeriesResponse> response) {
                    // Ẩn ProgressBar và hiển thị nội dung chính
                    binding.progressBar.setVisibility(View.GONE);
                    binding.mainContent.setVisibility(View.VISIBLE);
                    binding.bottomNavigation.setVisibility(View.VISIBLE);
                    swipeRefreshLayout.setRefreshing(false); // Ngừng loading

                    if (response.isSuccessful() && response.body() != null) {
                        seriesTvShow = response.body().getData().getItems();
                        binding.recyclerViewtvShow.setAdapter(new SeriesAdapter(MainActivity.this, seriesTvShow));
                    }
                }

                @Override
                public void onFailure(Call<SeriesResponse> call, Throwable t) {
                    binding.progressBar.setVisibility(View.GONE);
                    binding.bottomNavigation.setVisibility(View.GONE);
                    Toast.makeText(MainActivity.this, "Lỗi khi tải dữ liệu", Toast.LENGTH_SHORT).show();
                    binding.mainContent.setVisibility(View.VISIBLE); // Hiển thị lại nội dung chính
                    swipeRefreshLayout.setRefreshing(false); // Ngừng loading
                }
            });
        }

        private void loadPhimHoatHinh() {
            int page = 1;
            apiService.getHoatHinh(page).enqueue(new Callback<SeriesResponse>() {
                @Override
                public void onResponse(Call<SeriesResponse> call, Response<SeriesResponse> response) {
                    // Ẩn ProgressBar và hiển thị nội dung chính
                    binding.progressBar.setVisibility(View.GONE);
                    binding.mainContent.setVisibility(View.VISIBLE);
                    binding.bottomNavigation.setVisibility(View.VISIBLE);
                    swipeRefreshLayout.setRefreshing(false); // Ngừng loading

                    if (response.isSuccessful() && response.body() != null) {
                        seriesPhimHoatHinh = response.body().getData().getItems();
                        binding.recyclerViewphimhoathinh.setAdapter(new SeriesAdapter(MainActivity.this, seriesPhimHoatHinh));
                    }
                }

                @Override
                public void onFailure(Call<SeriesResponse> call, Throwable t) {
                    binding.progressBar.setVisibility(View.GONE);
                    binding.bottomNavigation.setVisibility(View.GONE);
                    Toast.makeText(MainActivity.this, "Lỗi khi tải dữ liệu", Toast.LENGTH_SHORT).show();
                    binding.mainContent.setVisibility(View.VISIBLE); // Hiển thị lại nội dung chính
                    swipeRefreshLayout.setRefreshing(false); // Ngừng loading
                }
            });
        }

        private void Banner() {
            apiService.getMovies(1).enqueue(new Callback<MovieResponse>() {
                @Override
                public void onResponse(Call<MovieResponse> call, Response<MovieResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        List<Movie> movies = response.body().getItems();
                        Log.d("API Response", "Movies fetched: " + movies.size());

                        // Setup the banner with the movies list
                        setupBannerViewPager(movies);
                    } else {
                        Log.e("API Error", "Response was not successful or body is null");
                    }
                }

                @Override
                public void onFailure(Call<MovieResponse> call, Throwable t) {
                    Toast.makeText(MainActivity.this, "Lỗi khi tải dữ liệu banner", Toast.LENGTH_SHORT).show();
                }
            });
        }

        private void setupBannerViewPager(List<Movie> movies) {
            BannerAdapter bannerAdapter = new BannerAdapter(this, movies);
            binding.viewPagerBanner.setAdapter(bannerAdapter);

            Handler bannerHandler = new Handler();
            Runnable bannerRunnable = new Runnable() {
                private int currentPage = 0;

                @Override
                public void run() {
                    if (currentPage == movies.size()) {
                        currentPage = 0;
                    }
                    binding.viewPagerBanner.setCurrentItem(currentPage++, true);
                    bannerHandler.postDelayed(this, 4000);
                }
            };
            bannerHandler.postDelayed(bannerRunnable, 4000);
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
        @Override
        public void onBackPressed() {
            if (doubleBackToExitPressedOnce) {
                super.finishAffinity();  // Exit the app
                return;
            }

            this.doubleBackToExitPressedOnce = true;
            Toast.makeText(this, "Nhấn thoát thêm một lần nữa", Toast.LENGTH_SHORT).show();

            // Reset the flag after 2 seconds
            new Handler().postDelayed(() -> doubleBackToExitPressedOnce = false, 2000);
        }

    }
