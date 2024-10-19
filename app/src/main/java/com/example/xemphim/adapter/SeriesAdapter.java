package com.example.xemphim.adapter;

import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.List;

import com.example.xemphim.activity.ChiTietActivity;
import com.example.xemphim.databinding.ItemSeriesBinding;
import com.example.xemphim.model.Movie;
import com.example.xemphim.model.Series;

public class SeriesAdapter extends RecyclerView.Adapter<SeriesAdapter.SeriesViewHolder> {
    private Activity context;  // Thêm biến Activity context
    private List<Series> seriesList;
    private static OnRecyclerViewItemClickListener recyclerViewItemClickListener;

    // Constructor
    public SeriesAdapter(Activity context, List<Series> seriesList) {
        this.context = context;  // Khởi tạo biến context
        this.seriesList = seriesList;
    }

    // Setter cho listener
    public void setRecyclerViewItemClickListener(OnRecyclerViewItemClickListener recyclerViewItemClickListener) {
        SeriesAdapter.recyclerViewItemClickListener = recyclerViewItemClickListener;
    }

    @NonNull
    @Override
    public SeriesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemSeriesBinding binding = ItemSeriesBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new SeriesViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull SeriesViewHolder holder, int position) {
        Series series = seriesList.get(position);
        holder.binding.movieTitle.setText(series.getName());
        holder.binding.movieYear.setText(String.valueOf(series.getYear()));

        // Sử dụng Glide để load hình ảnh
        Glide.with(context)  // Sử dụng context đã được cung cấp
                .load(series.getPosterUrl())
                .into(holder.binding.moviePoster);

        /// Luu Position mới cho Holder
        final int pos = position;
        holder.position = pos;
    }

    @Override
    public int getItemCount() {
        return seriesList.size();
    }

    // ViewHolder class
    public class SeriesViewHolder extends RecyclerView.ViewHolder {
        ItemSeriesBinding binding;
        int position;

        public SeriesViewHolder(@NonNull ItemSeriesBinding binding) {
            super(binding.getRoot());
            this.binding = binding;


//            itemView.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    recyclerViewItemClickListener.onItemClick(view, position);
//                }
//            });

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //Lay thong tin chi tiet phim tu slug truyen den man hinh chi tiet phim
                    Intent intent = new Intent(view.getContext(), ChiTietActivity.class);
                    Series phimbo = seriesList.get(position);
                    intent.putExtra("slug", phimbo.getSlug());
                    view.getContext().startActivity(intent);
                }
            });
        }

    }
    public void updateData(List<Series> newData) {
        seriesList.clear(); // Xóa dữ liệu cũ
        seriesList.addAll(newData); // Thêm dữ liệu mới
        notifyDataSetChanged(); // Thông báo adapter đã có dữ liệu mới
    }

    // Interface để xử lý sự kiện click
    public interface OnRecyclerViewItemClickListener {
        void onItemClick(View view, int position);
    }
}
