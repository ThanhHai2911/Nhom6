package com.example.xemphim.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;
import androidx.media3.common.MediaItem;
import androidx.media3.ui.PlayerView;
import com.example.xemphim.R;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector;

import java.io.File;
import java.util.Arrays;

@OptIn(markerClass = androidx.media3.common.util.UnstableApi.class)
public class PlayDownloadedMovieActivity extends AppCompatActivity {
    private ExoPlayer player;
    private PlayerView playerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_downloaded_movie);

        playerView = findViewById(R.id.playerView);

        // Lấy tên phim từ Intent
        String movieName = getIntent().getStringExtra("movie_name");

        if (movieName == null) {
            Log.e("PlayDownloadedMovieActivity", "Tên phim bị null");
            Toast.makeText(this, "Không tìm thấy tên phim!", Toast.LENGTH_SHORT).show();
            finish(); // Đóng activity nếu không có tên phim
            return;
        }

        Log.d("PlayDownloadedMovieActivity", "Tên phim: " + movieName);

        // Lấy thư mục phim dựa trên tên phim
        File movieDir = getMovieFile(movieName);

        if (movieDir.exists() && movieDir.isDirectory()) {
            // Lấy danh sách các file .ts trong thư mục phim theo thứ tự
            File[] tsFiles = movieDir.listFiles((dir, name) -> name.endsWith(".ts"));

            if (tsFiles != null && tsFiles.length > 0) {
                Arrays.sort(tsFiles); // Sắp xếp theo tên file để phát theo thứ tự

                Log.d("PlayDownloadedMovieActivity", "Số lượng tệp .ts: " + tsFiles.length);

                // Cấu hình ExoPlayer để phát phim
                DefaultTrackSelector trackSelector = new DefaultTrackSelector(this);
                player = new ExoPlayer.Builder(this)
                        .setTrackSelector(trackSelector)
                        .build();

                // Liên kết player với PlayerView
                playerView.setPlayer(player);

                // Tạo MediaItems từ các tệp .ts và thêm vào player
                for (File tsFile : tsFiles) {
                    Uri fileUri = Uri.fromFile(tsFile);
                    MediaItem mediaItem = MediaItem.fromUri(fileUri);
                    player.addMediaItem(mediaItem);
                }

                // Chuẩn bị và phát
                player.prepare();
                player.play();
            } else {
                Log.e("PlayDownloadedMovieActivity", "Không tìm thấy tệp .ts nào để phát.");
            }
        } else {
            Log.e("PlayDownloadedMovieActivity", "Thư mục phim không tồn tại hoặc không hợp lệ.");
        }
    }


    @Override
    protected void onStart() {
        super.onStart();
        if (player != null) {
            player.play();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (player != null) {
            player.pause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (player != null) {
            player.release();
            player = null;
        }
    }

    // Hàm lấy thư mục chứa phim dựa trên tên phim
    public File getMovieFile(String movieName) {
        File movieDir = new File(getExternalFilesDir(Environment.DIRECTORY_MOVIES), "MyMovies/" + movieName);

        if (!movieDir.exists()) {
            boolean created = movieDir.mkdirs();
            if (!created) {
                Log.e("PlayDownloadedMovieActivity", "Không thể tạo thư mục lưu phim");
            }
        }
        return movieDir;
    }
}
