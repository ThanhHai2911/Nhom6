package com.example.xemphim.model;

import android.content.Context;

import androidx.media3.exoplayer.ExoPlayer;

import com.example.xemphim.databinding.CustomControllerLayoutBinding;

public class PlayerControlHandler {
    private final ExoPlayer player;
    private final Context context;
    private final CustomControllerLayoutBinding binding; // Khai báo binding

    // Cập nhật constructor để nhận binding
    public PlayerControlHandler(Context context, ExoPlayer player, CustomControllerLayoutBinding binding) {
        this.context = context;
        this.player = player;
        this.binding = binding; // Gán binding
    }

    // Xử lý sự kiện Play
    public void handlePlay() {
        if (player != null) {
            player.play();
        }
    }

    // Xử lý sự kiện Pause
    public void handlePause() {
        if (player != null) {
            player.pause();
        }
    }

    // Xử lý sự kiện tua nhanh
    public void handleFastForward() {
        if (player != null) {
            player.seekForward();
        }
    }

    // Xử lý sự kiện tua lại
    public void handleRewind() {
        if (player != null) {
            player.seekBack();
        }
    }

    // Xử lý sự kiện chuyển tập trước
    public void handlePrevious() {
        if (player != null) {
            // Thực hiện logic chuyển tập trước (nếu có)
        }
    }

    // Xử lý sự kiện chuyển tập tiếp theo
    public void handleNext() {
        if (player != null) {
            // Thực hiện logic chuyển tập tiếp theo (nếu có)
        }
    }

    // Xử lý fullscreen
    public void handleFullScreen() {
        // Thực hiện logic chuyển sang chế độ fullscreen
    }
}
