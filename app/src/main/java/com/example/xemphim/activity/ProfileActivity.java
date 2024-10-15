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

import com.example.xemphim.API.ApiClient;
import com.example.xemphim.API.ApiService;
import com.example.xemphim.R;
import com.example.xemphim.adapter.LichSuAdapter;
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

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

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
    private DatabaseReference lichSuXemRef;
    private ApiService apiService;
    private DatabaseReference usersRef;
    private boolean isUserLoggedIn = false; // Biến để theo dõi trạng thái đăng nhập



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

        watchedMoviesList = new ArrayList<>();
        lichSuXemAdapter = new LichSuXemAdapter(ProfileActivity.this, watchedMoviesList);
        binding.rcvLichSu.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        binding.rcvLichSu.setAdapter(lichSuXemAdapter);

    }
    public void setEven(){
        laythongtinUser();
        Toast.makeText(ProfileActivity.this, "Xin chào " + nameUser, Toast.LENGTH_SHORT).show();
        tvTenNguoiDung.setText(nameUser);
        tvEmail.setText(emailUser);

        // Kiểm tra trạng thái đăng nhập
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            isUserLoggedIn = true; // Người dùng đã đăng nhập
            binding.btnDangNhap.setText("Đăng Xuất"); // Đổi văn bản nút thành "Đăng Xuất"
        } else {
            isUserLoggedIn = false; // Người dùng chưa đăng nhập
            binding.btnDangNhap.setText("Đăng Nhập"); // Đổi văn bản nút thành "Đăng Nhập"
        }

        binding.btnDangNhap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isUserLoggedIn) {
                    // Nếu người dùng đã đăng nhập, tiến hành đăng xuất
                    FirebaseAuth.getInstance().signOut();
                    isUserLoggedIn = false; // Cập nhật trạng thái đăng nhập

                    // Cập nhật giao diện người dùng
                    binding.btnDangNhap.setText("Đăng Nhập");
                    tvTenNguoiDung.setText(""); // Xóa tên người dùng
                    tvEmail.setText(""); // Xóa email người dùng
                    watchedMoviesList.clear(); // Xóa danh sách phim đã xem
                    lichSuXemAdapter.notifyDataSetChanged(); // Cập nhật adapter để hiển thị danh sách rỗng

                    Toast.makeText(ProfileActivity.this, "Đã đăng xuất!", Toast.LENGTH_SHORT).show();
                } else {
                    // Nếu người dùng chưa đăng nhập, chuyển đến màn hình đăng nhập
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
            }
        });
        setupBottomNavigation();
        lichSuXemRef = FirebaseDatabase.getInstance().getReference("LichSuXem");
        usersRef = FirebaseDatabase.getInstance().getReference("Users");
        apiService = ApiClient.getClient().create(ApiService.class);
        loadWatchHistory();
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
                                    fetchMovieDetails(movieSlug, movieItem); // Lấy chi tiết phim
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Toast.makeText(ProfileActivity.this, "Lỗi khi tải lịch sử xem", Toast.LENGTH_SHORT).show();
                            Log.e("LichSuXemActivity", "loadWatchHistory:onCancelled", databaseError.toException());
                        }
                    });
                } else {
                    Toast.makeText(ProfileActivity.this, "Người dùng không tồn tại trong cơ sở dữ liệu", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(ProfileActivity.this, "Lỗi khi lấy thông tin người dùng", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchMovieDetails(String slug, MovieDetail.MovieItem movieItem) {
        Call<MovieDetail> call = apiService.getMovieDetail(slug);
        call.enqueue(new Callback<MovieDetail>() {
            @Override
            public void onResponse(Call<MovieDetail> call, Response<MovieDetail> response) {
                if (response.isSuccessful() && response.body() != null) {
                    MovieDetail movieDetail = response.body();
                    movieItem.setName(movieDetail.getMovie().getName());
                    movieItem.setPosterUrl(movieDetail.getMovie().getPosterUrl());

                    // Thêm movieItem vào danh sách đã xem
                    watchedMoviesList.add(movieItem);
                    lichSuXemAdapter.notifyDataSetChanged(); // Thông báo adapter về thay đổi

                    // Cài đặt sự kiện nhấn cho các item trong adapter
                    lichSuXemAdapter.setRecyclerViewItemClickListener(new LichSuXemAdapter.OnRecyclerViewItemClickListener() {
                        @Override
                        public void onItemClick(View view, int position) {
                            Intent intent = new Intent(view.getContext(), ChiTietActivity.class);
                            MovieDetail.MovieItem movie = watchedMoviesList.get(position);
                            intent.putExtra("slug", movie.getSlug()); // Truyền slug tới ChiTietActivity
                            view.getContext().startActivity(intent);
                        }
                    });


                } else {
                    Log.e("LichSuXemActivity", "Failed to fetch movie details for slug: " + slug);
                }
            }

            @Override
            public void onFailure(Call<MovieDetail> call, Throwable t) {
                Log.e("LichSuXemActivity", "Error fetching movie details", t);
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