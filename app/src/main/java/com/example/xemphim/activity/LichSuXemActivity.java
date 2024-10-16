package com.example.xemphim.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;

import com.example.xemphim.API.ApiClient;
import com.example.xemphim.API.ApiService;
import com.example.xemphim.adapter.LichSuAdapter;
import com.example.xemphim.databinding.ActivityLichSuXemBinding;
import com.example.xemphim.model.Movie;
import com.example.xemphim.model.MovieDetail;
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
                                    MovieDetail.Episode.ServerData serverData = new MovieDetail.Episode.ServerData();
                                    movieItem.setSlug(movieSlug);
                                    movieItem.setEpisodeCurrent(episodeName);
                                    fetchMovieDetails(movieSlug, movieItem);
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Toast.makeText(LichSuXemActivity.this, "Lỗi khi tải lịch sử xem", Toast.LENGTH_SHORT).show();
                            Log.e("LichSuXemActivity", "loadWatchHistory:onCancelled", databaseError.toException());
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

                    // Add the movie item to the list after fetching details
                    watchedMoviesList.add(movieItem);

                    // Notify the adapter that data has changed
                    lichSuAdapter.notifyDataSetChanged();  // Notify adapter for changes
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

