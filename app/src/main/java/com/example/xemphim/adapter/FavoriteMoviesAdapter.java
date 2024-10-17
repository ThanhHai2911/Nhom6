package com.example.xemphim.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.xemphim.R;
import com.example.xemphim.activity.XemPhimActivity;
import com.example.xemphim.databinding.ItemMovieBinding;
import com.example.xemphim.model.FavoriteMovie;
import com.example.xemphim.model.Movie;

import java.util.List;

public class FavoriteMoviesAdapter extends RecyclerView.Adapter<FavoriteMoviesAdapter.ViewHolder> {
    private List<FavoriteMovie> favoriteMovies;
    private Context context;

    public FavoriteMoviesAdapter(Context context, List<FavoriteMovie> favoriteMovies) {
        this.context = context;
        this.favoriteMovies = favoriteMovies;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_movie, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        FavoriteMovie favoriteMovie = favoriteMovies.get(position);
        holder.tvTitle.setText(favoriteMovie.getTitle());
        // Thêm các hành động khác như click để xem phim
        holder.itemView.setOnClickListener(v -> {
            // Mở phim khi người dùng nhấn vào
            Intent intent = new Intent(context, XemPhimActivity.class);
            intent.putExtra("slug", favoriteMovie.getSlug());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return favoriteMovies.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle;

        public ViewHolder(View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.movieTitle); // Đảm bảo bạn có TextView trong layout item
        }
    }
}

