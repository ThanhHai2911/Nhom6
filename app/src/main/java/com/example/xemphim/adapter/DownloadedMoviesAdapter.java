package com.example.xemphim.adapter;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.xemphim.R;
import com.example.xemphim.databinding.ItemDownloadedMovieBinding;
import com.example.xemphim.model.Movie;
import com.example.xemphim.model.MovieItem;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.List;

public class DownloadedMoviesAdapter extends RecyclerView.Adapter<DownloadedMoviesAdapter.MovieViewHolder> {
    private List<MovieItem> movies;
    private OnMovieClickListener listener;

    public DownloadedMoviesAdapter(List<MovieItem> movies, OnMovieClickListener listener) {
        this.movies = movies;
        this.listener = listener;
    }

    @Override
    public MovieViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_downloaded_movie, parent, false);
        return new MovieViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MovieViewHolder holder, int position) {
        MovieItem movie = movies.get(position);
        holder.bind(movie, listener);
    }

    @Override
    public int getItemCount() {
        return movies.size();
    }

    public static class MovieViewHolder extends RecyclerView.ViewHolder {
        private TextView movieNameTextView;
        private ImageView moviePosterImageView;

        public MovieViewHolder(View itemView) {
            super(itemView);
            movieNameTextView = itemView.findViewById(R.id.movieTitle);
            moviePosterImageView = itemView.findViewById(R.id.moviePoster);
        }

        public void bind(MovieItem movie, OnMovieClickListener listener) {
            movieNameTextView.setText(movie.getName());
            // Sử dụng thư viện như Glide hoặc Picasso để tải ảnh poster
            Glide.with(itemView.getContext()).load(movie.getPosterPath()).into(moviePosterImageView);
            itemView.setOnClickListener(v -> listener.onMovieClick(movie));
        }
    }

    public interface OnMovieClickListener {
        void onMovieClick(MovieItem movie);
    }
}




