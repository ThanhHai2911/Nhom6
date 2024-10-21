package com.example.xemphim.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.xemphim.databinding.ItemQlPhimBinding;
import com.example.xemphim.model.QLPhim;
import java.util.List;

public class QLPhimAdapter extends RecyclerView.Adapter<QLPhimAdapter.QLPhimViewHolder> {

    private Context context;
    private List<QLPhim> phimList;

    // Constructor
    public QLPhimAdapter(Context context, List<QLPhim> phimList) {
        this.context = context;
        this.phimList = phimList;
    }

    @NonNull
    @Override
    public QLPhimViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the layout using View Binding
        ItemQlPhimBinding binding = ItemQlPhimBinding.inflate(LayoutInflater.from(context), parent, false);
        return new QLPhimViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull QLPhimViewHolder holder, int position) {
        // Get the current movie
        QLPhim currentPhim = phimList.get(position);

        // Bind the data to the views
        holder.binding.tenphim.setText(currentPhim.getTenPhim());
        holder.binding.nam.setText(currentPhim.getNamPhim());
        holder.binding.loaiphim.setText(currentPhim.getLoaiPhim());
        holder.binding.theloai.setText(currentPhim.getTheLoai());
        holder.binding.trangthai.setText(currentPhim.getTrangThai());
        holder.binding.ngaytao.setText(currentPhim.getNgayTao());

        // For the image, you can load it using Glide or another image loading library if necessary
        // For now, setting a local drawable resource
        holder.binding.imgmovie.setImageResource(currentPhim.getImageResId());
    }

    @Override
    public int getItemCount() {
        return phimList.size();
    }

    // ViewHolder class
    public static class QLPhimViewHolder extends RecyclerView.ViewHolder {
        private final ItemQlPhimBinding binding;

        public QLPhimViewHolder(@NonNull ItemQlPhimBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
