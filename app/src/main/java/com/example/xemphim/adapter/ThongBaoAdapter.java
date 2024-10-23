package com.example.xemphim.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.xemphim.R;
import com.example.xemphim.model.ThongBao;

import java.util.List;

public class ThongBaoAdapter extends RecyclerView.Adapter<ThongBaoAdapter.ThongBaoViewHolder> {

    private List<ThongBao> thongBaoList; // Danh sách thông báo

    // Constructor của adapter
    public ThongBaoAdapter(List<ThongBao> thongBaoList) {
        this.thongBaoList = thongBaoList;
    }

    @NonNull
    @Override
    public ThongBaoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_thongbao, parent, false);
        return new ThongBaoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ThongBaoViewHolder holder, int position) {
        ThongBao thongBao = thongBaoList.get(position);
        if (thongBao == null) {
            return;
        }

        // Cài đặt dữ liệu cho từng phần tử trong danh sách
        holder.tvTitle.setText(thongBao.getTitle());
        holder.tvTime.setText(thongBao.getTime());
        holder.tvContent.setText(thongBao.getContent());
        holder.imgIcon.setImageResource(R.drawable.ic_notification); // Cài đặt icon tạm thời
    }

    @Override
    public int getItemCount() {
        if (thongBaoList != null) {
            return thongBaoList.size();
        }
        return 0;
    }

    public class ThongBaoViewHolder extends RecyclerView.ViewHolder {

        private TextView tvTitle, tvTime, tvContent;
        private ImageView imgIcon;

        public ThongBaoViewHolder(@NonNull View itemView) {
            super(itemView);

            // Liên kết với view trong item_thongbao.xml
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvContent = itemView.findViewById(R.id.tvContent);
            imgIcon = itemView.findViewById(R.id.imgIcon);
        }
    }
}
