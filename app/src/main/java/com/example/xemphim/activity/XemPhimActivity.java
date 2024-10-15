package com.example.xemphim.activity;

import android.content.SharedPreferences;
import android.support.annotation.NonNull;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

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
    private String idUser;
    private  String nameUser;
    private String emailUser;
    private int idLoaiND;
    private DatabaseReference lichSuXemRef;

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
        lichSuXemRef = FirebaseDatabase.getInstance().getReference("LichSuXem");
        laythongtinUser();

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
        // Lưu lại thời điểm hiện tại của video

        if (isFullScreen) {
            // Chuyển về chế độ portrait
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        } else {
            // Chuyển sang chế độ landscape
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }

        isFullScreen = !isFullScreen;// Đổi trạng thái fullscreen
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
                                luuLichSuXem(movieSlug);
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

    private void luuLichSuXem(String movieSlug) {
        if (idUser == null || movieSlug == null) {
            Toast.makeText(XemPhimActivity.this, "Không thể lưu lịch sử, thiếu thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        // Query để kiểm tra xem slug đã tồn tại cho user này chưa
        Query query = lichSuXemRef.orderByChild("id_user").equalTo(idUser);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String idLichSuXem = null;
                // Duyệt qua các bản ghi lịch sử xem phim của user
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String existingSlug = snapshot.child("slug").getValue(String.class);
                    if (existingSlug != null && existingSlug.equals(movieSlug)) {
                        idLichSuXem = snapshot.getKey(); // Lấy ID của bản ghi để cập nhật
                        break;
                    }
                }

                if (idLichSuXem != null) {
                    // Nếu slug tồn tại, cập nhật tập phim mới
                   // updateExistingMovieHistory(idLichSuXem, episodeName, movieLink);
                } else {
                    // Nếu slug chưa tồn tại, tiến hành thêm mới
                    addNewMovieHistory(movieSlug);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(XemPhimActivity.this, "Lỗi khi kiểm tra lịch sử", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Hàm để thêm mới lịch sử xem phim nếu slug chưa tồn tại
    private void addNewMovieHistory(String movieSlug) {
        // Tạo ID mới cho lịch sử xem phim
        String idLichSuXem = lichSuXemRef.push().getKey();

        // Tạo thời gian xem phim (watched_at)
        String watchedAt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

        // Tạo bản ghi lịch sử xem phim
        Map<String, Object> lichSuXem = new HashMap<>();
        lichSuXem.put("id_user", idUser);
        lichSuXem.put("slug", movieSlug); // slug của phim
        lichSuXem.put("watched_at", watchedAt);

        // Lưu vào Firebase dưới node `LichSuXem`
        lichSuXemRef.child(idLichSuXem).setValue(lichSuXem)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(XemPhimActivity.this, "Lưu lịch sử xem phim thành công", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(XemPhimActivity.this, "Lưu lịch sử xem phim thất bại", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void laythongtinUser(){
        SharedPreferences sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
        idUser = sharedPreferences.getString("id_user", null);
        nameUser = sharedPreferences.getString("name", null);
        emailUser  = sharedPreferences.getString("email", null);
        idLoaiND = sharedPreferences.getInt("id_loaiND", 0);

    }
//    // Hàm để cập nhật lịch sử xem phim nếu slug tồn tại
//    private void updateExistingMovieHistory(String idLichSuXem, String episodeName, String movieLink) {
//        String watchedAt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
//
//        Map<String, Object> updates = new HashMap<>();
//        updates.put("episode_name", episodeName);
//        updates.put("movie_link", movieLink);
//        updates.put("watched_at", watchedAt);
//
//        lichSuXemRef.child(idLichSuXem).updateChildren(updates)
//                .addOnCompleteListener(task -> {
//                    if (task.isSuccessful()) {
//                        Toast.makeText(XemPhimActivity.this, "Cập nhật lịch sử xem thành công", Toast.LENGTH_SHORT).show();
//                    } else {
//                        Toast.makeText(XemPhimActivity.this, "Cập nhật lịch sử xem thất bại", Toast.LENGTH_SHORT).show();
//                    }
//                });
//    }



    //config
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            // Ở chế độ ngang, vào fullscreen
            binding.playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FILL);
            binding.playerView.setLayoutParams(new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.MATCH_PARENT,
                    RelativeLayout.LayoutParams.MATCH_PARENT
            ));

            // Ẩn thanh trạng thái và thanh điều hướng
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_FULLSCREEN   // Ẩn thanh trạng thái (status bar)
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION   // Ẩn thanh điều hướng (navigation bar)
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY  // Ở chế độ immersive (toàn màn hình)
            );
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            // Thoát fullscreen khi về portrait
            binding.playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT);
            binding.playerView.setLayoutParams(new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.MATCH_PARENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT  // Trở về chiều cao ban đầu (ban đầu như thế nào thì set lại)
            ));

            // Hiển thị lại các thanh trạng thái và điều hướng
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);

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
