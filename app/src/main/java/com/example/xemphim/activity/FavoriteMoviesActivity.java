package com.example.xemphim.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class FavoriteMoviesActivity extends AppCompatActivity {
    private ActivityFavoriteMoviesBinding binding;
    private FavoriteMoviesAdapter favoriteMoviesAdapter;
    private List<MovieDetail.MovieItem> favoriteMovies = new ArrayList<>();
    private DatabaseReference favoritesRef;
    private ApiService apiService;
    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Khởi tạo binding
        binding = ActivityFavoriteMoviesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot()); // Gán layout cho a

        apiService = ApiClient.getClient().create(ApiService.class);

        favoritesRef = FirebaseDatabase.getInstance().getReference("Favorite");
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
        favoritesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                favoriteMovies.clear(); // Xóa danh sách hiện tại
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String userId = snapshot.child("id_user").getValue(String.class);
                    String slug = snapshot.child("slug").getValue(String.class);
                    if (slug != null) {
                        fetchMovieDetails(slug); // Gọi API để lấy chi tiết phim
                    }
                }
                binding.progressBar.setVisibility(View.GONE);
                binding.layout.setVisibility(View.VISIBLE);
                swipeRefreshLayout.setRefreshing(false); // Ngừng loading
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(FavoriteMoviesActivity.this, "Lỗi khi tải danh sách yêu thích: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                swipeRefreshLayout.setRefreshing(false); // Ngừng loading
            }
        });
    }

    private void fetchMovieDetails(String slug) {
        Call<MovieDetail> call = apiService.getMovieDetail(slug);

        call.enqueue(new Callback<MovieDetail>() {
            @Override
            public void onResponse(Call<MovieDetail> call, retrofit2.Response<MovieDetail> response) {
                if (response.isSuccessful() && response.body() != null) {
                    MovieDetail.MovieItem movieItem = response.body().getMovie();
                    favoriteMovies.add(movieItem); // Thêm phim vào danh sách yêu thích
                    updateRecyclerView(); // Cập nhật RecyclerView
                }
            }

            @Override
            public void onFailure(Call<MovieDetail> call, Throwable t) {
                Toast.makeText(FavoriteMoviesActivity.this, "Lỗi khi gọi API: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                swipeRefreshLayout.setRefreshing(false); // Ngừng loading
            }
        });
    }

    private void updateRecyclerView() {
        if (favoriteMoviesAdapter == null) {
            favoriteMoviesAdapter = new FavoriteMoviesAdapter(FavoriteMoviesActivity.this, favoriteMovies);
            binding.rvFavorites.setAdapter(favoriteMoviesAdapter);
            setItemClickListener(); // Thiết lập listener cho item click
        } else {
            favoriteMoviesAdapter.notifyDataSetChanged(); // Cập nhật adapter
        }
    }

    private void setItemClickListener() {
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
