package com.example.xemphim.activity;

import android.widget.RelativeLayout;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;

import com.example.xemphim.API.ApiClient;
import com.example.xemphim.API.ApiService;
import com.example.xemphim.adapter.TapPhimAdapter;
import com.example.xemphim.databinding.ActivityXemphimBinding; // Import View Binding
import com.example.xemphim.model.FavoriteMovie;
import com.example.xemphim.model.MovieDetail;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.PlaybackException;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class XemPhimActivity extends AppCompatActivity {
    private ActivityXemphimBinding binding; // Khai báo View Binding
    private ExoPlayer exoPlayer; // ExoPlayer để phát video
    private ImageButton btnFullScreen;
    private boolean isFullScreen = false;
    private TapPhimAdapter tapPhimAdapter;
    private List<MovieDetail.Episode.ServerData> serverDataList = new ArrayList<>();
    private String movieLink;
    private ApiService apiService;
    private String movieSlug;
    private ImageButton btnAddToFavorites; // Thêm biến cho nút yêu thích
    private DatabaseReference favoritesRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityXemphimBinding.inflate(getLayoutInflater()); // Khởi tạo View Binding
        setContentView(binding.getRoot()); // Đặt layout cho Activity

        setControl();
        setEvent();

    }

    public void setControl() {
        // Gán View cho các biến
        btnFullScreen = binding.btnFullScreen; // Gán nút toàn màn hình
        btnAddToFavorites = binding.btnAddToFavorites; // Gán nút yêu thích
        binding.rcvTapPhim.setLayoutManager(new GridLayoutManager(this, 4)); // Thiết lập RecyclerView
        // Khởi tạo Firebase Database
        favoritesRef = FirebaseDatabase.getInstance().getReference("favorites"); // Thay "favorites" bằng tên bảng của bạn
    }

    public void setEvent() {
        initializePlayer();
        // Thiết lập sự kiện cho nút toàn màn hình
        movieSlug = getIntent().getStringExtra("slug");
        btnAddToFavorites.setOnClickListener(v -> addToFavorites());
        btnFullScreen.setOnClickListener(v -> toggleFullScreen());
        apiService = ApiClient.getClient().create(ApiService.class);
        loadMovieDetails();

    }
    private void addToFavorites() {
        // Lấy thông tin phim hiện tại
        String movieId = movieSlug; // Hoặc bất kỳ ID nào bạn có cho phim
        String movieTitle = binding.tvMovieTitle.getText().toString();
        String movieLink = serverDataList.get(0).getLinkM3u8(); // Link tập phim đầu tiên
        FavoriteMovie favoriteMovie = new FavoriteMovie(movieId, movieTitle, movieLink, movieSlug);

        // Lưu vào Firebase
        favoritesRef.child(movieId).setValue(favoriteMovie)
                .addOnSuccessListener(aVoid -> Toast.makeText(XemPhimActivity.this, "Đã thêm vào yêu thích!", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(XemPhimActivity.this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void initializePlayer() {
        String movieLink = getIntent().getStringExtra("movie_link");
        if (exoPlayer == null) {
            exoPlayer = new ExoPlayer.Builder(this).build();
            binding.playerView.setPlayer(exoPlayer); // Sử dụng View Binding để gán player
            binding.playerView.setKeepScreenOn(true);

            // Thêm listener để lắng nghe lỗi
            exoPlayer.addListener(new Player.Listener() {
                @Override
                public void onPlayerError(PlaybackException error) {
                    Toast.makeText(XemPhimActivity.this, "Phim lỗi vui lòng báo cáo cho admin hoặc xem phim khác: " + error.getMessage(), Toast.LENGTH_LONG).show();
                }
            });

            // Tạo HlsMediaSource
            DefaultHttpDataSource.Factory dataSourceFactory = new DefaultHttpDataSource.Factory();
            HlsMediaSource hlsMediaSource = new HlsMediaSource.Factory(dataSourceFactory)
                    .createMediaSource(MediaItem.fromUri(movieLink));

            // Thêm video vào player
            exoPlayer.setMediaSource(hlsMediaSource);
            exoPlayer.prepare();
        }
    }

    private void playEpisode(String episodeLink) {
        if (exoPlayer != null) {
            // Prepare a new media source for the new episode
            DefaultHttpDataSource.Factory dataSourceFactory = new DefaultHttpDataSource.Factory();
            HlsMediaSource hlsMediaSource = new HlsMediaSource.Factory(dataSourceFactory)
                    .createMediaSource(MediaItem.fromUri(episodeLink));

            exoPlayer.setMediaSource(hlsMediaSource);
            exoPlayer.prepare();
            exoPlayer.play(); // Start playback immediately
        }
    }

    private void toggleFullScreen() {
        long currentPosition = exoPlayer.getCurrentPosition(); // Lưu lại thời điểm hiện tại của video

        if (isFullScreen) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            binding.playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT); // Chuyển về chế độ phù hợp với kích thước
            // Đặt lại LayoutParams khi thoát toàn màn hình (phụ thuộc vào layout cha)
            binding.playerView.setLayoutParams(new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.MATCH_PARENT,
                    200 // hoặc giá trị chiều cao mong muốn
            ));
        } else {
            // Chuyển sang chế độ ngang
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            binding.playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FILL); // Chế độ toàn màn hình
            binding.playerView.setLayoutParams(new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.MATCH_PARENT,
                    RelativeLayout.LayoutParams.MATCH_PARENT
            ));
        }

        isFullScreen = !isFullScreen; // Đổi trạng thái fullscreen
    }

    private void loadMovieDetails() {
        Call<MovieDetail> call = apiService.getMovieDetail(movieSlug);
        call.enqueue(new Callback<MovieDetail>() {
            @Override
            public void onResponse(Call<MovieDetail> call, Response<MovieDetail> response) {
                if (response.isSuccessful() && response.body() != null) {
                    MovieDetail movieDetails = response.body();
                    List<MovieDetail.Episode> tapPhim = movieDetails.getEpisodes();
                    String movieTitle = movieDetails.getMovie().getName();
                    String episodeName = getIntent().getStringExtra("episodeCurrent");
                    binding.tvMovieTitle.setText(movieDetails.getMovie().getName());
                    binding.tvMovieTitle.setText(movieTitle + " - " + episodeName);
                    if (tapPhim != null && !tapPhim.isEmpty()) {
                        serverDataList.clear();
                        for (MovieDetail.Episode episode : tapPhim) {
                            List<MovieDetail.Episode.ServerData> data = episode.getServerData();
                            if (data != null) {
                                serverDataList.addAll(data);
                            }
                        }

                        tapPhimAdapter = new TapPhimAdapter(XemPhimActivity.this, serverDataList);
                        // Cập nhật RecyclerView với danh sách tập phim
                        tapPhimAdapter.setRecyclerViewItemClickListener(new TapPhimAdapter.OnRecyclerViewItemClickListener() {
                            @Override
                            public void onItemClick(View view, int position) {
                                MovieDetail.Episode.ServerData selectedEpisode = serverDataList.get(position);
                                String newMovieLink = selectedEpisode.getLinkM3u8();
                                //Hien thi ten tap phim  dang xem
                                String movieTitle = movieDetails.getMovie().getName();
                                String episodeName = selectedEpisode.getName();
                                binding.tvMovieTitle.setText(movieTitle + " - " + episodeName);

                                playEpisode(newMovieLink);
                            }
                        });

                        binding.rcvTapPhim.setAdapter(tapPhimAdapter);

                        // Play the first episode automatically
                        movieLink = serverDataList.get(0).getLinkM3u8();
                        initializePlayer(); // Initialize player with the first episode
                    } else {
                        Toast.makeText(XemPhimActivity.this, "Không có tập phim nào", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<MovieDetail> call, Throwable t) {
                Toast.makeText(XemPhimActivity.this, "Failed to load movie details", Toast.LENGTH_SHORT).show();
            }
        });
    }



    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            binding.playerView.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT
            ));
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (exoPlayer != null) {
            exoPlayer.release();  // Giải phóng ExoPlayer khi Activity bị hủy
            exoPlayer = null;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (exoPlayer != null) {
            exoPlayer.pause(); // Tạm dừng video khi Activity không còn hiển thị
        }
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Giữ màn hình sáng khi ứng dụng hoạt động
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

}
