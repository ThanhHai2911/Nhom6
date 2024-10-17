package com.example.xemphim.adapter;

import static androidx.core.content.ContextCompat.startActivity;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.xemphim.R;
import com.example.xemphim.activity.PlayDownloadedMovieActivity;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.List;

public class DownloadedMoviesAdapter extends RecyclerView.Adapter<DownloadedMoviesAdapter.MovieViewHolder> {

    private List<File> movieFiles;
    private OnMovieClickListener listener;

    public DownloadedMoviesAdapter(List<File> movieFiles, OnMovieClickListener listener) {
        this.movieFiles = movieFiles;
        this.listener = listener;
    }

    @NonNull
    @Override
    public MovieViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_downloaded_movie, parent, false);
        return new MovieViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MovieViewHolder holder, int position) {
        File movieFile = movieFiles.get(position);
        holder.bind(movieFile);
    }

    @Override
    public int getItemCount() {
        return movieFiles.size();
    }

    class MovieViewHolder extends RecyclerView.ViewHolder {

        TextView tvMovieName;
        ImageView ivMoviePoster; // Thêm ImageView cho poster phim

        public MovieViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMovieName = itemView.findViewById(R.id.movieTitle);
            ivMoviePoster = itemView.findViewById(R.id.moviePoster); // Khởi tạo ImageView
            itemView.setOnClickListener(v -> listener.onMovieClick(movieFiles.get(getAdapterPosition())));
        }

        public void bind(File movieFile) {
            tvMovieName.setText(movieFile.getName());

            // Giả sử bạn có một phương thức để lấy URL poster từ tên phim
            String posterUrl = getPosterUrl(movieFile.getName());
            // Tải poster vào ImageView
            Picasso.get()
                    .load(posterUrl)
                    .into(ivMoviePoster);
        }

        // Phương thức để lấy URL poster dựa trên tên phim
        private String getPosterUrl(String movieName) {
            // Thay đổi logic để lấy URL poster phù hợp với dự án của bạn
            return "https://example.com/posters/" + movieName.replace(" ", "%20") + ".jpg"; // Ví dụ
        }
    }

    public interface OnMovieClickListener {
        void onMovieClick(File movieFile);
    }
}