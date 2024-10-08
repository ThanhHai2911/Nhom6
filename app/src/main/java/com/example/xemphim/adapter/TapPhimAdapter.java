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

        holder.itemView.setOnClickListener(v -> {
            if (onEpisodeClickListener != null) {
                onEpisodeClickListener.onEpisodeClick(tapphim.getLinkM3u8());
            }
        });
    }

    @Override
    public int getItemCount() {
        return listTapPhim.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ItemTapphimBinding binding;

        public ViewHolder(@NonNull ItemTapphimBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
