package com.example.xemphim.activity;

import android.content.SharedPreferences;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.RelativeLayout;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;
import androidx.media3.common.MediaItem;
import androidx.media3.common.PlaybackException;
import androidx.media3.common.Player;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.datasource.DefaultHttpDataSource;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.exoplayer.hls.HlsMediaSource;
import androidx.media3.ui.AspectRatioFrameLayout;
import androidx.media3.ui.PlayerView;
import androidx.recyclerview.widget.GridLayoutManager;

import com.example.xemphim.API.ApiClient;
import com.example.xemphim.API.ApiService;
import com.example.xemphim.R;
import com.example.xemphim.adapter.TapPhimAdapter;
import com.example.xemphim.databinding.ActivityXemphimBinding; // Import View Binding
import com.example.xemphim.model.FavoriteMovie;
import com.example.xemphim.model.MovieDetail;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
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
    private int recursiveDepth = 0; // Biến theo dõi độ sâu của đệ quy
    private final int MAX_RECURSIVE_DEPTH = 10;
    private static final int MAX_RETRY_COUNT = 3;
    private OkHttpClient okHttpClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityXemphimBinding.inflate(getLayoutInflater()); // Khởi tạo View Binding
        setContentView(binding.getRoot()); // Đặt layout cho Activity

        okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS) // Tăng thời gian connect timeout
                .readTimeout(60, TimeUnit.SECONDS)    // Tăng thời gian read timeout
                .writeTimeout(60, TimeUnit.SECONDS)   // Tăng thời gian write timeout
                .retryOnConnectionFailure(true)       // Tự động retry khi lỗi kết nối
                .build();
        setControl();
        setEvent();
        // Kiểm tra và cập nhật màu nút trái tim
        checkAndToggleFavorite();

        // Thêm sự kiện nhấn cho nút thêm vào danh sách yêu thích
        btnAddToFavorites.setOnClickListener(v -> addToFavorites());

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

        btnFullScreen.setOnClickListener(v -> toggleFullScreen());
        apiService = ApiClient.getClient().create(ApiService.class);
        loadMovieDetails();
        lichSuXemRef = FirebaseDatabase.getInstance().getReference("LichSuXem");
        laythongtinUser();
        binding.btnDowload.setOnClickListener(v -> {
            String movieName = binding.tvMovieTitle.getText().toString();
            if (movieLink != null && !movieLink.isEmpty()) {
                // Hiện thông báo "Đang tải..."
                Toast.makeText(XemPhimActivity.this, "Đang tải...", Toast.LENGTH_SHORT).show();

                // Tải poster trước
                loadPosterAndDownloadMovie(movieSlug, movieLink, movieName);
            } else {
                Toast.makeText(XemPhimActivity.this, "Liên kết phim không hợp lệ!", Toast.LENGTH_SHORT).show();
            }
        });

    }
    private void addToFavorites() {
        // Lấy thông tin cần thiết
        String userId = idUser; // ID của người dùng hiện tại
        String movieSlug = this.movieSlug; // Slug của phim

        // Tham chiếu đến bảng Favorite
        DatabaseReference favoritesRef = FirebaseDatabase.getInstance().getReference("Favorite");

        // Tham chiếu đến vị trí phim yêu thích
        favoritesRef.orderByChild("slug").equalTo(movieSlug).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                boolean movieExists = false;
                String movieIdToDelete = ""; // Để lưu id_phim khi cần xóa

                // Kiểm tra từng entry trong danh sách yêu thích
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    FavoriteMovie favoriteMovie = snapshot.getValue(FavoriteMovie.class);
                    if (favoriteMovie != null && favoriteMovie.getId_user().equals(userId)) {
                        movieExists = true;
                        movieIdToDelete = snapshot.getKey(); // Lưu id_phim để xóa
                        break;
                    }
                }

                if (movieExists) {
                    // Nếu phim đã tồn tại, xóa nó
                    favoritesRef.child(movieIdToDelete).removeValue()
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(XemPhimActivity.this, "Đã xóa khỏi danh sách yêu thích!", Toast.LENGTH_SHORT).show();
                                // Đổi màu trái tim về trắng
                                btnAddToFavorites.setImageResource(R.drawable.baseline_favorite_24);
                            })
                            .addOnFailureListener(e -> Toast.makeText(XemPhimActivity.this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                } else {
                    // Nếu phim chưa tồn tại, thêm nó
                    FavoriteMovie favoriteMovie = new FavoriteMovie(userId, movieSlug);
                    favoritesRef.push().setValue(favoriteMovie)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(XemPhimActivity.this, "Đã thêm vào yêu thích!", Toast.LENGTH_SHORT).show();
                                // Đổi màu trái tim thành đỏ
                                btnAddToFavorites.setImageResource(R.drawable.baseline_favorite_24_red);
                            })
                            .addOnFailureListener(e -> Toast.makeText(XemPhimActivity.this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(XemPhimActivity.this, "Lỗi: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkAndToggleFavorite() {
        // Lấy thông tin cần thiết
        String userId = idUser; // ID của người dùng hiện tại
        String movieSlug = this.movieSlug; // Slug của phim
        DatabaseReference favoritesRef = FirebaseDatabase.getInstance().getReference("Favorite");

        // Kiểm tra nếu phim đã có trong danh sách yêu thích
        favoritesRef.orderByChild("slug").equalTo(movieSlug).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                boolean movieExists = false;

                // Kiểm tra từng entry trong danh sách yêu thích
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    FavoriteMovie favoriteMovie = snapshot.getValue(FavoriteMovie.class);
                    if (favoriteMovie != null && favoriteMovie.getId_user().equals(userId)) {
                        movieExists = true;
                        break;
                    }
                }

                // Nếu phim đã tồn tại, đổi màu nút thành đỏ
                if (movieExists) {
                    btnAddToFavorites.setImageResource(R.drawable.baseline_favorite_24_red);
                } else {
                    // Nếu phim chưa tồn tại, để màu trắng
                    btnAddToFavorites.setImageResource(R.drawable.baseline_favorite_24);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(XemPhimActivity.this, "Lỗi: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void initializePlayer() {
        movieLink = getIntent().getStringExtra("movie_link"); // Đã sửa để lấy lại movieLink
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


    //Tai phim xem offline
    private void loadPosterAndDownloadMovie(String movieSlug, String movieLink, String movieName) {
        Call<MovieDetail> call = apiService.getMovieDetail(movieSlug);
        call.enqueue(new Callback<MovieDetail>() {
            @Override
            public void onResponse(Call<MovieDetail> call, Response<MovieDetail> response) {
                if (response.isSuccessful() && response.body() != null) {
                    MovieDetail movieDetails = response.body();
                    String posterUrl = movieDetails.getMovie().getPosterUrl();
                    Log.d("Poster URL", "Poster link: " + posterUrl);
                    // Tải poster
                    downloadPoster(posterUrl, movieName, () -> {
                        // Sau khi tải poster xong, tiến hành tải phim
                        downloadMovie(movieLink, movieName);
                    });
                }
            }

            @Override
            public void onFailure(Call<MovieDetail> call, Throwable t) {
                Toast.makeText(XemPhimActivity.this, "Failed to load movie details", Toast.LENGTH_SHORT).show();
            }
        });
    }

    //Tai xuong poster
    private void downloadPoster(String posterUrl, String movieName, Runnable onSuccess) {
        Call<ResponseBody> call = apiService.downloadMovie(posterUrl); // Sử dụng API để tải poster
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        File movieDir = getMovieFile(movieName); // Lấy thư mục phim
                        File posterFile = new File(movieDir, movieName + "_poster.jpg"); // Đặt tên file poster
                        try (InputStream inputStream = response.body().byteStream();
                             FileOutputStream outputStream = new FileOutputStream(posterFile)) {

                            byte[] buffer = new byte[4096];
                            int bytesRead;
                            while ((bytesRead = inputStream.read(buffer)) != -1) {
                                outputStream.write(buffer, 0, bytesRead);
                            }

                            Log.d("DownloadPoster", "Poster đã được lưu tại: " + posterFile.getAbsolutePath());
                            onSuccess.run(); // Gọi hàm để tiếp tục tải phim sau khi poster tải xong
                        }
                    } catch (IOException e) {
                        Log.e("DownloadPoster", "Lỗi khi lưu poster: " + e.getMessage());
                    }
                } else {
                    Log.e("DownloadPoster", "Tải poster thất bại: " + posterUrl);
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e("DownloadPoster", "Lỗi khi tải poster: " + t.getMessage());
            }
        });
    }

    // Hàm tải về từng đoạn video
    private void downloadMovie(String m3u8Link, String movieName) {
        if (recursiveDepth > MAX_RECURSIVE_DEPTH) { // Giới hạn đệ quy
            runOnUiThread(() -> Toast.makeText(XemPhimActivity.this, "Quá nhiều tệp m3u8 con, tải không thành công!", Toast.LENGTH_LONG).show());
            return;
        }

        recursiveDepth++; // Tăng độ sâu mỗi khi đệ quy
        Call<ResponseBody> call = apiService.downloadMovie(m3u8Link);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    runOnUiThread(() -> Toast.makeText(XemPhimActivity.this, "Tải file m3u8 không thành công!", Toast.LENGTH_LONG).show());
                    return;
                }

                try (BufferedReader reader = new BufferedReader(new InputStreamReader(response.body().byteStream()))) {
                    String line;
                    List<String> tsLinks = new ArrayList<>();
                    while ((line = reader.readLine()) != null) {
                        if (line.endsWith(".m3u8")) {
                            // Nếu là m3u8 con, tải đệ quy
                            String subM3U8Link = line.startsWith("http") ? line : m3u8Link.substring(0, m3u8Link.lastIndexOf("/") + 1) + line;
                            downloadMovie(subM3U8Link, movieName); // Gọi đệ quy để tải tệp con
                            return;
                        }
                        if (line.endsWith(".ts")) {
                            tsLinks.add(line); // Thêm link .ts vào danh sách
                        }
                    }

                    if (tsLinks.isEmpty()) {
                        runOnUiThread(() -> Toast.makeText(XemPhimActivity.this, "Không tìm thấy link .ts trong file m3u8!", Toast.LENGTH_LONG).show());
                        return;
                    }

                    // Tải các file .ts tuần tự
                    downloadAllTsFilesSequentially(tsLinks, m3u8Link, movieName);

                } catch (IOException e) {
                    runOnUiThread(() -> Toast.makeText(XemPhimActivity.this, "Lỗi khi phân tích file m3u8: " + e.getMessage(), Toast.LENGTH_LONG).show());
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                runOnUiThread(() -> Toast.makeText(XemPhimActivity.this, "Lỗi khi tải file m3u8: " + t.getMessage(), Toast.LENGTH_LONG).show());
            }
        });
    }
    // Lấy thư mục lưu phim
    public File getMovieFile(String movieName) {
        // Lấy thư mục Movies riêng của ứng dụng
        File movieDir = new File(getExternalFilesDir(Environment.DIRECTORY_MOVIES), "MyMovies/" + movieName);

        if (!movieDir.exists()) {
            boolean created = movieDir.mkdirs();
            if (!created) {
                Log.e("XemPhimActivity", "Không thể tạo thư mục lưu phim");
            }
        }
        return movieDir;
    }

    // Hàm để tải tuần tự từng file .ts
    private void downloadAllTsFilesSequentially(List<String> tsLinks, String m3u8Link, String movieName) {
        List<File> tsFiles = new ArrayList<>();
        // Bắt đầu quá trình tải các file .ts với lần thử đầu tiên
        downloadTsFile(tsLinks, m3u8Link, movieName, 0, tsFiles, 0);
    }
    private void downloadTsFile(List<String> tsLinks, String m3u8Link, String movieName, int index, List<File> tsFiles, int retryCount) {
        if (index >= tsLinks.size()) {
            createM3U8Playlist(getMovieFile(movieName), tsFiles); // Tạo tệp .m3u8 sau khi tải xong tất cả file .ts
            mergeTsFiles(tsFiles, movieName); // Ghép file khi tải xong tất cả file .ts
            return;
        }

        String tsLink = tsLinks.get(index);
        // Kiểm tra và sửa URL nếu cần
        String tsFullLink = tsLink.startsWith("http") ? tsLink : m3u8Link.substring(0, m3u8Link.lastIndexOf("/") + 1) + tsLink;
        tsFullLink = tsFullLink.replace("hls//", "hls/");

        // Tạo biến final để sử dụng trong inner class
        final String finalTsFullLink = tsFullLink;

        Call<ResponseBody> call = apiService.downloadMovie(finalTsFullLink);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    Log.e("downloadTsFile", "Lỗi khi tải file .ts: " + finalTsFullLink);

                    if (retryCount < MAX_RETRY_COUNT) {
                        // Thử tải lại file nếu chưa quá số lần retry
                        Log.d("downloadTsFile", "Đang thử lại lần thứ " + (retryCount + 1) + " cho file: " + finalTsFullLink);
                        new Handler(Looper.getMainLooper()).postDelayed(() -> {
                            downloadTsFile(tsLinks, m3u8Link, movieName, index, tsFiles, retryCount + 1);
                        }, 2000); // Đợi 2 giây trước khi thử lại
                    } else {
                        Log.e("downloadTsFile", "Bỏ qua file sau " + MAX_RETRY_COUNT + " lần thử lại: " + finalTsFullLink);
                        // Bỏ qua file và tiếp tục tải file tiếp theo
                        downloadTsFile(tsLinks, m3u8Link, movieName, index + 1, tsFiles, 0); // Tiếp tục file tiếp theo
                    }
                    return;
                }

                // Lưu file .ts vào bộ nhớ
                File movieDir = getMovieFile(movieName);
                File tsFile = new File(movieDir, movieName + "_" + tsLink.substring(tsLink.lastIndexOf("/") + 1));

                // Kiểm tra xem file đã tồn tại trong danh sách chưa, nếu chưa thì mới thêm
                if (!tsFiles.contains(tsFile)) {
                    try (InputStream inputStream = response.body().byteStream();
                         FileOutputStream outputStream = new FileOutputStream(tsFile)) {

                        byte[] buffer = new byte[4096];
                        int bytesRead;
                        while ((bytesRead = inputStream.read(buffer)) != -1) {
                            outputStream.write(buffer, 0, bytesRead);
                        }

                        tsFiles.add(tsFile); // Thêm file đã tải vào danh sách

                    } catch (IOException e) {
                        Log.e("downloadTsFile", "Lỗi ghi file .ts: " + e.getMessage());
                    }
                } else {
                    Log.d("downloadTsFile", "File đã tồn tại, bỏ qua: " + tsFile.getName());
                }

                // Tải tiếp file tiếp theo
                downloadTsFile(tsLinks, m3u8Link, movieName, index + 1, tsFiles, 0); // Reset retryCount khi tải file mới
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e("downloadTsFile", "Lỗi khi tải file .ts: " + t.getMessage());

                if (retryCount < MAX_RETRY_COUNT) {
                    // Thử tải lại file nếu gặp lỗi mạng hoặc lỗi khác
                    Log.d("downloadTsFile", "Đang thử lại lần thứ " + (retryCount + 1) + " cho file: " + finalTsFullLink);
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        downloadTsFile(tsLinks, m3u8Link, movieName, index, tsFiles, retryCount + 1);
                    }, 2000); // Thêm delay khi retry
                } else {
                    Log.e("downloadTsFile", "Bỏ qua file sau " + MAX_RETRY_COUNT + " lần thử lại: " + finalTsFullLink);
                    // Bỏ qua file và tiếp tục tải file tiếp theo
                    downloadTsFile(tsLinks, m3u8Link, movieName, index + 1, tsFiles, 0); // Tiếp tục file tiếp theo
                }
            }
        });
    }
    // Ghép tất cả file .ts thành 1 file
    private void mergeTsFiles(List<File> tsFiles, String movieName) {
        File mergedFile = getMovieFile(movieName); // Tạo file đích cho phim đã ghép

        try (FileOutputStream fos = new FileOutputStream(mergedFile)) {
            for (File tsFile : tsFiles) {
                try (FileInputStream fis = new FileInputStream(tsFile)) {
                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = fis.read(buffer)) != -1) {
                        fos.write(buffer, 0, bytesRead); // Ghi dữ liệu của file .ts vào file hợp nhất
                    }
                }
            }
            runOnUiThread(() -> Toast.makeText(XemPhimActivity.this, "Đã ghép file thành công: " + mergedFile.getAbsolutePath(), Toast.LENGTH_LONG).show());
        } catch (IOException e) {
            runOnUiThread(() -> Toast.makeText(XemPhimActivity.this, "Lỗi khi ghép file .ts: " + e.getMessage(), Toast.LENGTH_LONG).show());
        }
    }
    // Tạo file .m3u8
    private void createM3U8Playlist(File movieDir, List<File> tsFiles) {
        File m3u8File = new File(movieDir, "playlist.m3u8");

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(m3u8File))) {
            writer.write("#EXTM3U\n");
            writer.write("#EXT-X-VERSION:3\n");
            writer.write("#EXT-X-TARGETDURATION:10\n");
            writer.write("#EXT-X-MEDIA-SEQUENCE:0\n");

            for (File tsFile : tsFiles) {
                // Gọi hàm để lấy thời gian thực tế cho từng file .ts
                double duration = getTsFileDuration(tsFile); // Thay thế bằng cách lấy thời gian thực tế

                writer.write("#EXTINF:" + duration + ",\n");
                writer.write(tsFile.getName() + "\n");
            }

            writer.write("#EXT-X-ENDLIST\n");
            writer.flush();
            Log.d("createM3U8Playlist", "Đã tạo file playlist.m3u8 tại: " + m3u8File.getAbsolutePath());
        } catch (IOException e) {
            Log.e("PlayDownloadedMovieActivity", "Lỗi khi tạo file playlist.m3u8", e);
        }
    }
    // Giả lập hàm để lấy độ dài file .ts (cần được điều chỉnh theo thực tế)
    private double getTsFileDuration(File tsFile) {
        // Giả sử mỗi file .ts có độ dài 2 giây, thay thế bằng cách thực tế để lấy thời gian
        return 3.0; // Thay thế bằng logic thực tế để xác định độ dài
    }



    //config
    @OptIn(markerClass = UnstableApi.class)
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
