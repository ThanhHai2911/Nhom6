package com.example.xemphim.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.xemphim.API.ApiClient;
import com.example.xemphim.API.ApiService;
import com.example.xemphim.R;
import com.example.xemphim.adapter.LichSuAdapter;
import com.example.xemphim.databinding.ActivityProfileBinding;
import com.example.xemphim.model.MovieDetail;
import com.example.xemphim.model.ThongBaoTrenManHinh;
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
    private boolean doubleBackToExitPressedOnce = false;
    private ActivityProfileBinding binding;
    private LichSuAdapter lichSuAdapter;
    private List<MovieDetail.MovieItem> watchedMoviesList;
    private DatabaseReference databaseReference;
    private String idUser;
    private  String nameUser;
    private String emailUser;
    private int idLoaiND;
    private DatabaseReference lichSuXemRef;
    private ApiService apiService;
    private DatabaseReference usersRef;
    private boolean isUserLoggedIn = false; // Biến để theo dõi trạng thái đăng nhập
    private SwipeRefreshLayout swipeRefreshLayout;



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
        watchedMoviesList = new ArrayList<>();
        lichSuAdapter = new LichSuAdapter(ProfileActivity.this, watchedMoviesList);
        binding.rcvLichSu.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        binding.rcvLichSu.setAdapter(lichSuAdapter);

    }
    public void setEven(){
        laythongtinUser();
        //kiem tra truy cap
        //MainActivity.kiemTraTruyCap(idUser);
        Toast.makeText(ProfileActivity.this, "Xin chào " + nameUser, Toast.LENGTH_SHORT).show();
        binding.tvTenNguoiDung.setText(nameUser);
        binding.tvEmail.setText(emailUser);

        // Kiểm tra trạng thái đăng nhập
        if (idUser != null) {
            if (idLoaiND == 3 || idLoaiND == 2){
                isUserLoggedIn = true;
                binding.btnDangNhap.setVisibility(View.VISIBLE);
                binding.btnDangNhap.setText("Admin");
            }else{
                isUserLoggedIn = true; // Người dùng đã đăng nhập
                binding.btnDangNhap.setVisibility(View.GONE);
            }

        } else {

            isUserLoggedIn = false; // Người dùng chưa đăng nhập
            binding.btnDangNhap.setVisibility(View.VISIBLE);
            binding.btnDangNhap.setText("Đăng Nhập"); // Đổi văn bản nút thành "Đăng Nhập"
        }

        binding.btnDangNhap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (binding.btnDangNhap.getText().equals("Admin")){
                    Intent intent = new Intent(ProfileActivity.this, AdminActivity.class );
                    startActivity(intent);
                }
                else {
                    Intent intent = new Intent(ProfileActivity.this, DangNhapActivity.class);
                    startActivity(intent);
                }

            }
        });

        binding.tvXemtatca.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ProfileActivity.this, LichSuXemActivity.class);
                startActivity(intent);
            }
        });

        binding.dsYeuThich.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Chuyển đến FavoriteMoviesActivity
                Intent intent = new Intent(ProfileActivity.this, FavoriteMoviesActivity.class);
                startActivity(intent);
                finish();
            }
        });
        binding.caiDat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(ProfileActivity.this, CaiDatActivity.class);
                startActivity(intent);
                finish();
            }
        });
        setupBottomNavigation();
        lichSuXemRef = FirebaseDatabase.getInstance().getReference("LichSuXem");
        usersRef = FirebaseDatabase.getInstance().getReference("Users");
        apiService = ApiClient.getClient().create(ApiService.class);
        loadWatchHistory();
        swipeRefreshLayout = binding.swipeRefreshLayout; // Khởi tạo SwipeRefreshLayout
        swipeRefreshLayout.setOnRefreshListener(() -> {
            loadWatchHistory();
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
        // Đặt item mặc định được chọn là màn hình Home
        binding.bottomNavigation.setSelectedItemId(R.id.nav_profile);
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

    private void loadWatchHistory() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser == null) {
            Toast.makeText(this, "Không có thông tin người dùng", Toast.LENGTH_SHORT).show();
            return;
        }

        // Lấy thông tin người dùng từ database
        usersRef.child(currentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String idUser = dataSnapshot.child("id_user").getValue(String.class); // Lấy id_user

                    if (idUser == null) {
                        Toast.makeText(ProfileActivity.this, "Lỗi: id_user không tồn tại", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Sử dụng idUser để tải lịch sử xem phim
                    lichSuXemRef.orderByChild("id_user").equalTo(idUser).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            watchedMoviesList.clear(); // Xóa danh sách cũ trước khi thêm dữ liệu mới

                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                String movieSlug = snapshot.child("slug").getValue(String.class);
                                String episodeName = snapshot.child("episode_name").getValue(String.class);

                                if (movieSlug != null) {
                                    MovieDetail.MovieItem movieItem = new MovieDetail.MovieItem();
                                    movieItem.setSlug(movieSlug);
                                    movieItem.setEpisodeCurrent(episodeName);
                                    watchedMoviesList.add(0,movieItem);
                                    lichSuAdapter.notifyItemChanged(0);
                                    chiTietPhim(movieSlug, movieItem); // Lấy chi tiết phim
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Toast.makeText(ProfileActivity.this, "Lỗi khi tải lịch sử xem", Toast.LENGTH_SHORT).show();
                            swipeRefreshLayout.setRefreshing(false); // Ngừng loading
                        }
                    });
                } else {
                    Toast.makeText(ProfileActivity.this, "Người dùng không tồn tại trong cơ sở dữ liệu", Toast.LENGTH_SHORT).show();
                    swipeRefreshLayout.setRefreshing(false); // Ngừng loading
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(ProfileActivity.this, "Lỗi khi lấy thông tin người dùng", Toast.LENGTH_SHORT).show();
                swipeRefreshLayout.setRefreshing(false); // Ngừng loading
            }
        });
    }

    private void chiTietPhim(String slug, MovieDetail.MovieItem movieItem) {
        Call<MovieDetail> call = apiService.getMovieDetail(slug);
        call.enqueue(new Callback<MovieDetail>() {
            @Override
            public void onResponse(Call<MovieDetail> call, Response<MovieDetail> response) {
                if (response.isSuccessful() && response.body() != null) {
                    MovieDetail movieDetail = response.body();
                    movieItem.setName(movieDetail.getMovie().getName());
                    movieItem.setPosterUrl(movieDetail.getMovie().getPosterUrl());
                    lichSuAdapter.notifyDataSetChanged();

                    // Đặt listener cho mỗi mục phim trong lịch sử
                    lichSuAdapter.setRecyclerViewItemClickListener(new LichSuAdapter.OnRecyclerViewItemClickListener() {
                        @Override
                        public void onItemClick(View view, int position) {
                            // Lấy mục phim đã nhấn
                            MovieDetail.MovieItem clickedMovie = watchedMoviesList.get(position);
                            String movieSlug = clickedMovie.getSlug();

                            if (movieSlug == null) {
                                Log.e("LichSuXemActivity", "Slug phim là null. Không thể lấy liên kết phim.");
                                return; // Kết thúc hàm nếu slug là null
                            }

                            // Tham chiếu đến Firebase để lấy liên kết phim
                            DatabaseReference moviesRef = FirebaseDatabase.getInstance().getReference("LichSuXem");
                            moviesRef.orderByChild("slug").equalTo(movieSlug).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.exists()) {
                                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                            // Lấy liên kết phim
                                            String movieLink = snapshot.child("movie_link").getValue(String.class);
                                            String episodeName = snapshot.child("episode").getValue(String.class);
                                            String slug = snapshot.child("slug").getValue(String.class);

                                            if (movieLink != null) {
                                                Log.d("LichSuXemActivity", "Liên kết phim: " + movieLink);

                                                // Tạo intent để mở màn hình phát phim
                                                Intent intent = new Intent(view.getContext(), XemPhimActivity.class);
                                                intent.putExtra("movie_link", movieLink);
                                                intent.putExtra("episode", episodeName);
                                                intent.putExtra("slug", slug);
                                                intent.putExtra("from_watch_history", true);

                                                // Bắt đầu màn hình phát phim
                                                view.getContext().startActivity(intent);
                                            } else {
                                                Log.e("LichSuXemActivity", "Liên kết phim là null cho slug: " + movieSlug);
                                            }
                                        }
                                    } else {
                                        Log.e("LichSuXemActivity", "Không tìm thấy phim cho slug: " + movieSlug);
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                    Log.e("LichSuXemActivity", "Lỗi khi lấy liên kết phim", databaseError.toException());
                                }
                            });
                        }
                    });

                } else {
                    Log.e("LichSuXemActivity", "Không thể lấy chi tiết phim cho slug: " + slug);
                }
            }

            @Override
            public void onFailure(Call<MovieDetail> call, Throwable t) {
                Log.e("LichSuXemActivity", "Lỗi khi lấy thông tin chi tiết phim", t);
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