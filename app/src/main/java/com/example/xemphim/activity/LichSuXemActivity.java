package com.example.xemphim.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.xemphim.API.ApiClient;
import com.example.xemphim.API.ApiService;
import com.example.xemphim.R;
import com.example.xemphim.adapter.LichSuAdapter;
import com.example.xemphim.adapter.SeriesAdapter;
import com.example.xemphim.databinding.ActivityLichSuXemBinding;
import com.example.xemphim.model.Movie;
import com.example.xemphim.model.MovieDetail;
import com.example.xemphim.model.Series;
import com.example.xemphim.response.MovieResponse;
import com.example.xemphim.response.SeriesResponse;
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

public class LichSuXemActivity extends AppCompatActivity {
    private ActivityLichSuXemBinding binding;
    private LichSuAdapter lichSuAdapter;
    private List<MovieDetail.MovieItem> watchedMoviesList;
    private DatabaseReference lichSuXemRef;
    private String idUser;
    private ApiService apiService;
    private DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityLichSuXemBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            idUser = currentUser.getUid();
        } else {
            Toast.makeText(this, "Vui lòng đăng nhập để xem lịch sử", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        lichSuXemRef = FirebaseDatabase.getInstance().getReference("LichSuXem");
        usersRef = FirebaseDatabase.getInstance().getReference("Users");
        apiService = ApiClient.getClient().create(ApiService.class);
        setControl();
        loadWatchHistory();
        // Thiết lập ActionBar và DrawerLayout
        setSupportActionBar(binding.toolbar);
        // Kiểm tra xem ActionBar đã được khởi tạo chưa
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Lịch sử đã xem"); // Đặt tên mới cho Toolbar
            getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Hiện biểu tượng trở về
        }
    }

    public void setControl() {
        watchedMoviesList = new ArrayList<>();
        lichSuAdapter = new LichSuAdapter(LichSuXemActivity.this, watchedMoviesList);
        binding.rcvlichsuxem.setLayoutManager(new GridLayoutManager(this, 3));
        binding.rcvlichsuxem.setAdapter(lichSuAdapter); // Set the adapter
    }

    private void loadWatchHistory() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser(); // Lấy người dùng hiện tại

        if (currentUser == null) {
            Toast.makeText(this, "Lỗi: Người dùng không xác định", Toast.LENGTH_SHORT).show();
            return;
        }

        // Lấy id_user từ database
        usersRef.child(currentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String idUser = dataSnapshot.child("id_user").getValue(String.class); // Lấy id_user

                    if (idUser == null) {
                        Toast.makeText(LichSuXemActivity.this, "Lỗi: id_user không tồn tại", Toast.LENGTH_SHORT).show();
                        return;
                    }


                    // Sử dụng idUser để tải lịch sử xem phim
                    lichSuXemRef.orderByChild("id_user").equalTo(idUser).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                String movieSlug = snapshot.child("slug").getValue(String.class);
                                String episodeName = snapshot.child("episode_name").getValue(String.class);

                                if (movieSlug != null) {
                                    MovieDetail.MovieItem movieItem = new MovieDetail.MovieItem();
                                    movieItem.setSlug(movieSlug);
                                    movieItem.setEpisodeCurrent(episodeName);
                                    watchedMoviesList.add(0,movieItem);
                                    lichSuAdapter.notifyItemChanged(0);
                                    fetchMovieDetails(movieSlug, movieItem);
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Toast.makeText(LichSuXemActivity.this, "Lỗi khi tải lịch sử xem", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Toast.makeText(LichSuXemActivity.this, "Người dùng không tồn tại trong cơ sở dữ liệu", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(LichSuXemActivity.this, "Lỗi khi lấy thông tin người dùng", Toast.LENGTH_SHORT).show();
            }
        });
    }

    //Load thong tin phim
    private void fetchMovieDetails(String slug, MovieDetail.MovieItem movieItem) {
        Call<MovieDetail> call = apiService.getMovieDetail(slug);
        call.enqueue(new Callback<MovieDetail>() {
            @Override
            public void onResponse(Call<MovieDetail> call, Response<MovieDetail> response) {
                if (response.isSuccessful() && response.body() != null) {
                    MovieDetail movieDetail = response.body();
                    movieItem.setName(movieDetail.getMovie().getName());
                    movieItem.setPosterUrl(movieDetail.getMovie().getPosterUrl());
                    lichSuAdapter.notifyDataSetChanged();
                    lichSuAdapter.setRecyclerViewItemClickListener(new LichSuAdapter.OnRecyclerViewItemClickListener() {
                        @Override
                        public void onItemClick(View view, int position) {
                            // Lấy thông tin chi tiết phim từ slug truyền đến màn hình xem phim
                            Intent intent = new Intent(view.getContext(), ChiTietActivity.class);
                            MovieDetail.MovieItem movie = watchedMoviesList.get(position);
                            intent.putExtra("slug", movie.getSlug()); // Truyền slug tới WatchMovieActivity
                            view.getContext().startActivity(intent);
                        }
                    });
                    // Remove the reinitialization of lichSuAdapter
                    // Update click listener outside this function if necessary
                    binding.progressBar.setVisibility(View.GONE);
                    binding.layout.setVisibility(View.VISIBLE);
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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_timkiem, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();

        // Lấy id_user từ Firebase
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String idUser = currentUser != null ? currentUser.getUid() : null; // Lấy UID của người dùng

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (idUser != null) { // Kiểm tra id_user không null
                    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("watchedMovies");

                    // Truy vấn Firebase theo id_user
                    Query searchQuery = databaseReference.orderByChild("id_user")
                            .equalTo(idUser); // Lọc theo id_user

                    searchQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            watchedMoviesList.clear(); // Xóa danh sách cũ

                            boolean found = false; // Biến kiểm tra xem có phim nào phù hợp không

                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                MovieDetail.MovieItem movieDetail = snapshot.getValue(MovieDetail.MovieItem.class);

                                // Kiểm tra slug có chứa từ khóa tìm kiếm không
                                if (movieDetail != null && movieDetail.getSlug().toLowerCase().contains(query.toLowerCase())) {
                                    watchedMoviesList.add(movieDetail); // Thêm phim vào danh sách
                                    found = true; // Đánh dấu có phim phù hợp
                                }
                            }

                            if (found) {
                                lichSuAdapter.notifyDataSetChanged(); // Thông báo adapter về thay đổi dữ liệu
                            } else {
                                Toast.makeText(LichSuXemActivity.this, "Không tìm thấy phim", Toast.LENGTH_SHORT).show();
                            }

                            searchView.clearFocus(); // Đóng SearchView sau khi tìm kiếm
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Toast.makeText(LichSuXemActivity.this, "Lỗi khi tìm kiếm", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Toast.makeText(LichSuXemActivity.this, "Người dùng không hợp lệ", Toast.LENGTH_SHORT).show();
                }

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

