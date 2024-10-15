package com.example.xemphim.activity;

import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import androidx.media3.ui.PlayerView;
import android.widget.Toast;

import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;
import androidx.media3.common.Player;
import androidx.media3.common.util.UnstableApi;
import androidx.recyclerview.widget.GridLayoutManager;

import com.example.xemphim.API.ApiClient;
import com.example.xemphim.API.ApiService;
import com.example.xemphim.adapter.TapPhimAdapter;
import com.example.xemphim.databinding.ActivityXemphimBinding;
import com.example.xemphim.model.MovieDetail;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.common.MediaItem;
import androidx.media3.common.PlaybackException;
import androidx.media3.ui.AspectRatioFrameLayout;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
@OptIn(markerClass = UnstableApi.class)
public class XemPhimActivity extends AppCompatActivity {
    private ActivityXemphimBinding binding;
    private ExoPlayer exoPlayer;
    private ImageButton btnFullScreen;
    private boolean isFullScreen = false;
    private TapPhimAdapter tapPhimAdapter;
    private List<MovieDetail.Episode.ServerData> serverDataList = new ArrayList<>();
    private String movieLink;
    private ApiService apiService;
    private String movieSlug;
    private ImageButton btnAddToFavorites;
    private DatabaseReference favoritesRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityXemphimBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setControl();
        setEvent();
    }

    public void setControl() {
        btnFullScreen = binding.btnFullScreen;
        btnAddToFavorites = binding.btnAddToFavorites;
        binding.rcvTapPhim.setLayoutManager(new GridLayoutManager(this, 4));
        favoritesRef = FirebaseDatabase.getInstance().getReference("favorites");
    }

    public void setEvent() {
        initializePlayer();
        movieSlug = getIntent().getStringExtra("slug");
        btnFullScreen.setOnClickListener(v -> toggleFullScreen());
        apiService = ApiClient.getClient().create(ApiService.class);
        loadMovieDetails();
    }
    @OptIn(markerClass = UnstableApi.class)
    private void initializePlayer() {
        String movieLink = getIntent().getStringExtra("movie_link");
        if (exoPlayer == null) {
            exoPlayer = new ExoPlayer.Builder(this).build();
            PlayerView playerView = binding.playerView; // Đảm bảo đây là PlayerView từ Media3
            playerView.setPlayer(exoPlayer); // Thiết lập player cho PlayerView
            playerView.setKeepScreenOn(true);

            exoPlayer.addListener(new Player.Listener() {
                @Override
                public void onPlayerError(PlaybackException error) {
                    Toast.makeText(XemPhimActivity.this, "Phim lỗi vui lòng báo cáo cho admin hoặc xem phim khác: " + error.getMessage(), Toast.LENGTH_LONG).show();
                }
            });

            // Tạo MediaItem từ đường dẫn video
            MediaItem mediaItem = MediaItem.fromUri(movieLink);
            exoPlayer.setMediaItem(mediaItem);
            exoPlayer.prepare();
        }
    }

    private void playEpisode(String episodeLink) {
        if (exoPlayer != null) {
            MediaItem mediaItem = MediaItem.fromUri(episodeLink);
            exoPlayer.setMediaItem(mediaItem);
            exoPlayer.prepare();
            exoPlayer.play();
        }
    }

    private void toggleFullScreen() {
        long currentPosition = exoPlayer.getCurrentPosition(); // Lưu lại thời điểm hiện tại của video

        if (isFullScreen) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            binding.playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FILL); // Chuyển về chế độ phù hợp với kích thước
            // Đặt lại LayoutParams khi thoát toàn màn hình (phụ thuộc vào layout cha)
            binding.playerView.setLayoutParams(new RelativeLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    250 // hoặc giá trị chiều cao mong muốn
            ));
        } else {
            // Chuyển sang chế độ ngang
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            binding.playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FILL); // Chế độ toàn màn hình
            binding.playerView.setLayoutParams(new RelativeLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT
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
                        tapPhimAdapter.setRecyclerViewItemClickListener(new TapPhimAdapter.OnRecyclerViewItemClickListener() {
                            @Override
                            public void onItemClick(View view, int position) {
                                MovieDetail.Episode.ServerData selectedEpisode = serverDataList.get(position);
                                String newMovieLink = selectedEpisode.getLinkM3u8();
                                String movieTitle = movieDetails.getMovie().getName();
                                String episodeName = selectedEpisode.getName();
                                binding.tvMovieTitle.setText(movieTitle + " - " + episodeName);
                                playEpisode(newMovieLink);
                            }
                        });

                        binding.rcvTapPhim.setAdapter(tapPhimAdapter);

                        movieLink = serverDataList.get(0).getLinkM3u8();
                        initializePlayer();
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
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (exoPlayer != null) {
            exoPlayer.release();
            exoPlayer = null;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (exoPlayer != null) {
            exoPlayer.pause();
        }
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    protected void onResume() {
        super.onResume();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }
}
