package com.example.xemphim.adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.xemphim.R;
import com.example.xemphim.databinding.ItemThongbaoBinding;
import com.example.xemphim.databinding.ItemUserThongbaoBinding;
import com.example.xemphim.model.ThongBao;

import java.util.List;

public class ThongBaoAdapter extends RecyclerView.Adapter<ThongBaoAdapter.ThongBaoViewHolder> {
    private Activity context;
    private List<ThongBao> thongBaoList; // Danh sách thông báo

    public ThongBaoAdapter(Activity context, List<ThongBao> thongBaoList) {
        this.context = context;
        this.thongBaoList = thongBaoList;
    }

    @NonNull
    @Override
    public ThongBaoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemThongbaoBinding binding = ItemThongbaoBinding.inflate(context.getLayoutInflater(), parent, false);
        return new ThongBaoViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ThongBaoViewHolder holder, int position) {
        ThongBao thongBao = thongBaoList.get(position);
        if (thongBao == null) {
            return;
        }

        // Cài đặt dữ liệu cho từng phần tử trong danh sách
        holder.binding.tvTitle.setText(thongBao.getTitle());
        holder.binding.tvTime.setText(thongBao.getTime());
        holder.binding.tvContent.setText(thongBao.getContent());
        holder.binding.imgIcon.setImageResource(R.drawable.ic_notification); // Cài đặt icon tạm thời
    }

    @Override
    public int getItemCount() {
        if (thongBaoList != null) {
            return thongBaoList.size();
        }
        return 0;
    }

    public class ThongBaoViewHolder extends RecyclerView.ViewHolder {
        ItemThongbaoBinding binding;
        int position;
        public ThongBaoViewHolder(@NonNull ItemThongbaoBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
