package com.example.xemphim.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.xemphim.API.ApiClient;
import com.example.xemphim.API.ApiService;
import com.example.xemphim.R;
import com.example.xemphim.adapter.FavoriteMoviesAdapter;
import com.example.xemphim.adapter.LichSuAdapter;
import com.example.xemphim.databinding.ActivityFavoriteMoviesBinding;
import com.example.xemphim.databinding.ActivityLichSuXemBinding;
import com.example.xemphim.model.MovieDetail;
import com.example.xemphim.model.ThongBaoTrenManHinh;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class FavoriteMoviesActivity extends AppCompatActivity {
    private ActivityFavoriteMoviesBinding binding;
    private FavoriteMoviesAdapter favoriteMoviesAdapter;
    private List<MovieDetail.MovieItem> favoriteMovies = new ArrayList<>();
    private DatabaseReference favoritesRef;
    private ApiService apiService;
    private DatabaseReference usersRef;
    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Khởi tạo binding
        binding = ActivityFavoriteMoviesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot()); // Gán layout cho a



        apiService = ApiClient.getClient().create(ApiService.class);

        favoritesRef = FirebaseDatabase.getInstance().getReference("Favorite");
        usersRef = FirebaseDatabase.getInstance().getReference("Users");
        setControl();
        loadFavoriteMovies();
        // Thiết lập ActionBar và DrawerLayout
        setSupportActionBar(binding.toolbar);
        // Kiểm tra xem ActionBar đã được khởi tạo chưa
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Danh Sách yêu thích"); // Đặt tên mới cho Toolbar
            getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Hiện biểu tượng trở về
        }
        swipeRefreshLayout = binding.swipeRefreshLayout; // Khởi tạo SwipeRefreshLayout
        swipeRefreshLayout.setOnRefreshListener(() -> {
            loadFavoriteMovies();
        });
    }
    private void setControl() {
        favoriteMovies = new ArrayList<>();
        favoriteMoviesAdapter = new FavoriteMoviesAdapter(FavoriteMoviesActivity.this, favoriteMovies);
        binding.rvFavorites.setLayoutManager(new GridLayoutManager(this, 3));
        binding.rvFavorites.setAdapter(favoriteMoviesAdapter); // Set the adapter
    }

    private void loadFavoriteMovies() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser(); // Lấy người dùng hiện tại

        if (currentUser == null) {
            Toast.makeText(this, "Lỗi: Người dùng không xác định", Toast.LENGTH_SHORT).show();
            return;
        }
        // Clear the watchedMoviesList to avoid duplication
        favoriteMovies.clear();
        favoriteMoviesAdapter.notifyDataSetChanged(); // Notify adapter about the cleared list
        // Lấy id_user từ database
        usersRef.child(currentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String idUser = dataSnapshot.child("id_user").getValue(String.class); // Lấy id_user

                    if (idUser == null) {
                        Toast.makeText(FavoriteMoviesActivity.this, "Lỗi: id_user không tồn tại", Toast.LENGTH_SHORT).show();
                        return;
                    }


                    // Sử dụng idUser để tải danh sách yêu thích
                    favoritesRef.orderByChild("id_user").equalTo(idUser).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                MovieDetail.MovieItem movieItem = new MovieDetail.MovieItem();
                                String movieSlug = snapshot.child("slug").getValue(String.class);
                                movieItem.setSlug(movieSlug);
                                fetchMovieDetails(movieSlug);
                            }
                            binding.progressBar.setVisibility(View.GONE);
                            binding.layout.setVisibility(View.VISIBLE);
                            swipeRefreshLayout.setRefreshing(false); // Ngừng loading
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Toast.makeText(FavoriteMoviesActivity.this, "Lỗi khi tải danh sách yêu thích", Toast.LENGTH_SHORT).show();
                            swipeRefreshLayout.setRefreshing(false); // Ngừng loading
                        }
                    });
                } else {
                    Toast.makeText(FavoriteMoviesActivity.this, "Người dùng không tồn tại trong cơ sở dữ liệu", Toast.LENGTH_SHORT).show();
                    swipeRefreshLayout.setRefreshing(false); // Ngừng loading
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(FavoriteMoviesActivity.this, "Lỗi khi lấy thông tin người dùng", Toast.LENGTH_SHORT).show();
                swipeRefreshLayout.setRefreshing(false); // Ngừng loading
            }
        });
    }
    //Load thong tin phim
    private void fetchMovieDetails(String slug) {
        Call<MovieDetail> call = apiService.getMovieDetail(slug);
        call.enqueue(new Callback<MovieDetail>() {
            @Override
            public void onResponse(Call<MovieDetail> call, Response<MovieDetail> response) {
                if (response.isSuccessful() && response.body() != null) {
                    MovieDetail.MovieItem movieItem = response.body().getMovie();
                    favoriteMovies.add(movieItem); // Thêm phim vào danh sách yêu thích
                    // Notify the adapter that data has changed
                    favoriteMoviesAdapter.notifyDataSetChanged();  // Notify adapter for changes
                    favoriteMoviesAdapter.setRecyclerViewItemClickListener(new FavoriteMoviesAdapter.OnRecyclerViewItemClickListener() {
                        @Override
                        public void onItemClick(View view, int position) {
                            // Truyền slug sang ChiTietActivity
                            String slug = favoriteMovies.get(position).getSlug(); // Lấy slug từ phim
                            Intent intent = new Intent(FavoriteMoviesActivity.this, ChiTietActivity.class);
                            intent.putExtra("slug", slug); // Truyền slug
                            startActivity(intent);
                        }
                    });
                    // Remove the reinitialization of lichSuAdapter
                    // Update click listener outside this function if necessary
                    binding.progressBar.setVisibility(View.GONE);
                    binding.layout.setVisibility(View.VISIBLE);
                } else {
                    Log.e("FavoriteMoviesActivity", "Failed to fetch movie details for slug: " + slug);
                }
            }

            @Override
            public void onFailure(Call<MovieDetail> call, Throwable t) {
                Log.e("FavoriteMoviesActivity", "Error fetching movie details", t);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_timkiem, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

                if (currentUser == null) {
                    Toast.makeText(FavoriteMoviesActivity.this, "Lỗi: Người dùng không xác định", Toast.LENGTH_SHORT).show();
                    return true;
                }

                usersRef.child(currentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            String idUser = dataSnapshot.child("id_user").getValue(String.class);

                            if (idUser == null) {
                                Toast.makeText(FavoriteMoviesActivity.this, "Lỗi: id_user không tồn tại", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            favoritesRef.orderByChild("id_user").equalTo(idUser).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    boolean found = false;

                                    // Lưu trữ một danh sách phim tìm thấy tạm thời
                                    List<MovieDetail.MovieItem> searchResults = new ArrayList<>();

                                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                        String movieSlug = snapshot.child("slug").getValue(String.class);
                                        if (movieSlug != null) {
                                            // Gọi hàm để lấy thông tin chi tiết phim
                                            chiTietPhimTimKiem(movieSlug, query, searchResults); // Truyền danh sách tạm thời
                                            found = true;
                                        }
                                    }

                                    if (!found) {
                                        Toast.makeText(FavoriteMoviesActivity.this, "Không tìm thấy kết quả", Toast.LENGTH_SHORT).show();
                                    }
                                    searchView.clearFocus();
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                    Toast.makeText(FavoriteMoviesActivity.this, "Lỗi khi tìm kiếm", Toast.LENGTH_SHORT).show();
                                }
                            });
                        } else {
                            Toast.makeText(FavoriteMoviesActivity.this, "Người dùng không tồn tại trong cơ sở dữ liệu", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(FavoriteMoviesActivity.this, "Lỗi khi lấy thông tin người dùng", Toast.LENGTH_SHORT).show();
                    }
                });

                searchItem.collapseActionView();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        return true;
    }

    private void chiTietPhimTimKiem(String slug, String query, List<MovieDetail.MovieItem> searchResults) {
        Call<MovieDetail> call = apiService.getMovieDetail(slug);
        call.enqueue(new Callback<MovieDetail>() {
            @Override
            public void onResponse(Call<MovieDetail> call, Response<MovieDetail> response) {
                if (response.isSuccessful() && response.body() != null) {
                    MovieDetail movieDetail = response.body();
                    MovieDetail.MovieItem movieItem = new MovieDetail.MovieItem();
                    movieItem.setName(movieDetail.getMovie().getName());
                    movieItem.setPosterUrl(movieDetail.getMovie().getPosterUrl());
                    movieItem.setSlug(slug); // Set slug for navigation

                    // Kiểm tra xem tên phim có chứa truy vấn không
                    if (movieItem.getName().toLowerCase().contains(query.toLowerCase())) {
                        // Thêm phim tìm thấy vào đầu danh sách tìm kiếm
                        searchResults.add(0, movieItem);

                        // Cập nhật danh sách chính
                        favoriteMovies.add(0, movieItem); // Thêm vào đầu danh sách chính
                        favoriteMoviesAdapter.notifyDataSetChanged(); // Cập nhật RecyclerView
                    }
                } else {
                    Log.e("FavoriteMoviesActivity", "Failed to fetch movie details for slug: " + slug);
                }
            }

            @Override
            public void onFailure(Call<MovieDetail> call, Throwable t) {
                Log.e("FavoriteMoviesActivity", "Error fetching movie details", t);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_search) {
            // Xử lý sự kiện khi nhấn vào tìm kiếm
            Toast.makeText(this, "Bạn muốn tìm kiếm gì", Toast.LENGTH_SHORT).show();
            return true;
        } else if (item.getItemId() == android.R.id.home) {
            // Chuyển đến màn hình profile
            Intent intent = new Intent(this, ProfileActivity.class); // Thay ProfileActivity bằng tên Activity profile của bạn
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    protected void onPause() {
        super.onPause();
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }
}
