package com.example.xemphim.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.xemphim.databinding.ItemMovieBinding;
import com.example.xemphim.model.Phim;

import java.util.List;

public class PhimAdapter extends RecyclerView.Adapter<PhimAdapter.PhimViewHolder> {

    private Context context;
    private List<Phim> movieList;

    public PhimAdapter(Context context, List<Phim> movieList) {
        this.context = context;
        this.movieList = movieList;
    }

    @NonNull
    @Override
    public PhimViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Sử dụng ViewBinding cho item_movie
        ItemMovieBinding binding = ItemMovieBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new PhimViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull PhimViewHolder holder, int position) {
        Phim movie = movieList.get(position);

        // Đặt dữ liệu vào các thành phần view
        holder.binding.movieTitle.setText(movie.getName());
        holder.binding.movieYear.setText(String.valueOf(movie.getYear()));

        // Sử dụng Glide để load ảnh từ URL
        Glide.with(context)
                .load(movie.getPoster_url())
                .into(holder.binding.moviePoster);
    }

    @Override
    public int getItemCount() {
        return movieList.size();
    }

    public static class PhimViewHolder extends RecyclerView.ViewHolder {
        private final ItemMovieBinding binding;

        public PhimViewHolder(ItemMovieBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
