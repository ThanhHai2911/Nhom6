package com.example.xemphim.adapter;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.xemphim.databinding.ItemLichsuxemBinding;
import com.example.xemphim.model.Movie;
import com.example.xemphim.model.MovieDetail;

import java.util.List;

public class ThongTinLichSuAdapter extends RecyclerView.Adapter<ThongTinLichSuAdapter.MovieViewHolder> {
    private Activity context;
    private List<MovieDetail.MovieItem> movies;
    private static OnRecyclerViewItemClickListener recyclerViewItemClickListener;

    public ThongTinLichSuAdapter(Activity context, List<MovieDetail.MovieItem> movies) {
        this.context = context;
        this.movies = movies;
    }

    public void setRecyclerViewItemClickListener(OnRecyclerViewItemClickListener listener) {
        recyclerViewItemClickListener = listener;
    }

    @NonNull
    @Override
    public MovieViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemLichsuxemBinding binding = ItemLichsuxemBinding.inflate(context.getLayoutInflater(), parent, false);
        return new MovieViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull MovieViewHolder holder, int position) {
        MovieDetail.MovieItem movie = movies.get(position);
        holder.binding.movieTitle.setText(movie.getName());

        // Sử dụng Glide để load hình ảnh
        Glide.with(context)  // Sử dụng context đã được cung cấp
                .load(movie.getPosterUrl())
                .into(holder.binding.moviePoster);



        /// Luu Position mới cho Holder
        final int pos = position;
        holder.position = pos;
    }

    @Override
    public int getItemCount() {
        return movies.size();
    }

    public class MovieViewHolder extends RecyclerView.ViewHolder {
        ItemLichsuxemBinding binding;
        int position;
        public MovieViewHolder(@NonNull ItemLichsuxemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    recyclerViewItemClickListener.onItemClick(view, position);
                }
            });
        }
    }

    // Interface để xử lý sự kiện click
    public interface OnRecyclerViewItemClickListener {
        void onItemClick(View view, int position);
    }
}
