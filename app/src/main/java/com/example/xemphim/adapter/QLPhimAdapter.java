package com.example.xemphim.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide; // Import Glide
import com.example.xemphim.databinding.ItemQlPhimBinding;
import com.example.xemphim.model.Goi;
import com.example.xemphim.model.KieuPhim;
import com.example.xemphim.model.Phim; // Đảm bảo rằng bạn đã import lớp Phim
import java.util.List;

public class QLPhimAdapter extends RecyclerView.Adapter<QLPhimAdapter.QLPhimViewHolder> {

    private Context context;
    private List<Phim> phimList; // Danh sách phim
    private List<KieuPhim> kieuPhimList; // Danh sách kieuPhim
    private List<Goi> goiList; // Danh sách goi

    // Constructor
    public QLPhimAdapter(Context context, List<Phim> phimList, List<KieuPhim> kieuPhimList, List<Goi> goiList) {
        this.context = context;
        this.phimList = phimList;
        this.kieuPhimList = kieuPhimList;
        this.goiList = goiList;
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
        Phim currentPhim = phimList.get(position);

        // Bind dữ liệu
        holder.binding.tenphim.setText(currentPhim.getName());
        holder.binding.nam.setText(currentPhim.getYear());

        // Lấy tên kieuPhim từ danh sách kieuPhim
        String kieuPhimName = getKieuPhimNameById(String.valueOf(currentPhim.getId_kieuPhim()));
        holder.binding.loaiphim.setText(kieuPhimName);

        // Lấy type của gói từ danh sách goi
        String goiType = getGoiTypeById(Integer.valueOf(currentPhim.getGoi()));
        holder.binding.goiPhim.setText(goiType);

        holder.binding.theloai.setText(currentPhim.getTheLoai());
        holder.binding.ngaytao.setText(currentPhim.getNgayThemPhim());

        // Load hình ảnh
        Glide.with(context)
                .load(currentPhim.getPoster_url())
                .into(holder.binding.imgmovie);
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

    // Hàm để lấy tên kieuPhim
    private String getKieuPhimNameById(String id) {
        for (KieuPhim kieuPhim : kieuPhimList) {
            if (kieuPhim.getId_kieuPhim().equals(id)) {
                return kieuPhim.getTenKieuPhim();
            }
        }
        return "Không xác định"; // Nếu không tìm thấy
    }

    // Hàm để lấy loại gói
    private String getGoiTypeById(int id) {
        for (Goi goi : goiList) {
            if (goi.getId() == id) {
                return goi.getType();
            }
        }
        return "Không xác định"; // Nếu không tìm thấy
    }
}
