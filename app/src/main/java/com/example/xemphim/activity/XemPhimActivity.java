package com.example.xemphim.activity;

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
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
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

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
    private ImageButton btnDowload;
    private DatabaseReference favoritesRef;
    private int recursiveDepth = 0; // Biến theo dõi độ sâu của đệ quy
    private final int MAX_RECURSIVE_DEPTH = 10;
    private static final int MAX_RETRY_COUNT = 3;
    private OkHttpClient okHttpClient;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityXemphimBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS) // Tăng thời gian connect timeout
                .readTimeout(60, TimeUnit.SECONDS)    // Tăng thời gian read timeout
                .writeTimeout(60, TimeUnit.SECONDS)   // Tăng thời gian write timeout
                .retryOnConnectionFailure(true)       // Tự động retry khi lỗi kết nối
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(ApiClient.getBaseUrl())
                .client(okHttpClient) // Sử dụng OkHttpClient với timeout lớn hơn
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        apiService = retrofit.create(ApiService.class);
        setControl();
        setEvent();
    }

    public void setControl() {
        btnFullScreen = binding.btnFullScreen;
        btnDowload = binding.btnDowload;
        binding.rcvTapPhim.setLayoutManager(new GridLayoutManager(this, 4));
        favoritesRef = FirebaseDatabase.getInstance().getReference("favorites");
    }

    public void setEvent() {
        initializePlayer();
        movieSlug = getIntent().getStringExtra("slug");

        btnFullScreen.setOnClickListener(v -> toggleFullScreen());

        btnDowload.setOnClickListener(v -> {
            String movieName = binding.tvMovieTitle.getText().toString();
            if (movieLink != null && !movieLink.isEmpty()) {
                // Tải poster trước
                loadPosterAndDownloadMovie(movieSlug, movieLink, movieName);
            } else {
                Toast.makeText(XemPhimActivity.this, "Liên kết phim không hợp lệ!", Toast.LENGTH_SHORT).show();
            }
        });

        loadMovieDetails();
    }
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
        if (exoPlayer != null && exoPlayer.isPlaying()) {
            exoPlayer.pause(); // Dừng video khi Activity tạm dừng
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (exoPlayer != null) {
            exoPlayer.play(); // Phát lại video khi Activity trở lại
        }
    }
}