package com.example.xemphim.adapter;

import android.app.Activity;
import android.content.Intent;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.xemphim.activity.XemPhimActivity;
import com.example.xemphim.databinding.ItemMovieBinding;
import com.example.xemphim.databinding.ItemTapphimBinding;
import com.example.xemphim.model.LinkPhim;
import com.example.xemphim.model.Movie;

import java.util.List;

public class TapPhimAdapter extends RecyclerView.Adapter<TapPhimAdapter.ViewHolder> {
    private Activity context;
    private List<LinkPhim> listTapPhim;
    private OnEpisodeClickListener onEpisodeClickListener;

    public TapPhimAdapter(Activity context, List<LinkPhim> movies, OnEpisodeClickListener onEpisodeClickListener) {
        this.onEpisodeClickListener = onEpisodeClickListener;
        this.listTapPhim = movies;
        this.context = context;
    }

    public interface OnEpisodeClickListener {
        void onEpisodeClick(String linkM3u8);
    }
    public void setOnEpisodeClickListener(OnEpisodeClickListener onEpisodeClickListener) {
        this.onEpisodeClickListener = onEpisodeClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemTapphimBinding binding = ItemTapphimBinding.inflate(context.getLayoutInflater(), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        LinkPhim tapphim = listTapPhim.get(position);
        holder.binding.btnEpisodesName.setText(tapphim.getName()); // Thiết lập tên tập phim cho đúng mục

        /// Luu Position mới cho Holder
        final int pos = position;
        holder.position = pos;
    }

    @Override
    public int getItemCount() {
        return listTapPhim.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ItemTapphimBinding binding;
        int position;
        public ViewHolder(@NonNull ItemTapphimBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            // Xử lý sự kiện khi người dùng nhấn vào tập phim
            binding.btnEpisodesName.setOnClickListener(v -> {
                LinkPhim tapphim = listTapPhim.get(position);
                if (onEpisodeClickListener != null) {
                    onEpisodeClickListener.onEpisodeClick(tapphim.getLinkM3u8());
                }
                // Optionally, you could start the new activity directly here if you prefer
                Intent intent = new Intent(context, XemPhimActivity.class);
                intent.putExtra("movie_link", tapphim.getLinkM3u8());
                intent.putExtra("slug", tapphim.getSlug());
                context.startActivity(intent);
            });
        }
    }
}
