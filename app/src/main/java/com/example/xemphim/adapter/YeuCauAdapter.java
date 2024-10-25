package com.example.xemphim.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.xemphim.R;
import com.example.xemphim.databinding.ItemYeuCauBinding;
import com.example.xemphim.model.User;
import com.example.xemphim.model.YeuCau;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class YeuCauAdapter extends RecyclerView.Adapter<YeuCauAdapter.YeuCauViewHolder> {

    private List<YeuCau> yeuCauList;
    private OnItemClickListener listener;
    private DatabaseReference userRef;

    public YeuCauAdapter(List<YeuCau> yeuCauList) {
        this.yeuCauList = yeuCauList;
        this.userRef = FirebaseDatabase.getInstance().getReference("Users");
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public YeuCauViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemYeuCauBinding binding = ItemYeuCauBinding.inflate(inflater, parent, false);
        return new YeuCauViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull YeuCauViewHolder holder, int position) {
        YeuCau yeuCau = yeuCauList.get(position);
        holder.bind(yeuCau);
// Đổi màu trạng thái: cam nếu chưa xử lý, xanh nếu đã xử lý
        if (yeuCau.getIdTrangThai() == 0) {
            holder.binding.tvTrangThai.setText("Chưa xử lý");
            holder.binding.tvTrangThai.setTextColor(holder.itemView.getResources().getColor(R.color.orange)); // Cam
        } else {
            holder.binding.tvTrangThai.setText("Đã xử lý");
            holder.binding.tvTrangThai.setTextColor(holder.itemView.getResources().getColor(R.color.green)); // Xanh
        }
        // Lưu vị trí mới cho Holder
        final int pos = position;
        holder.position = pos;
    }

    @Override
    public int getItemCount() {
        return yeuCauList != null ? yeuCauList.size() : 0;
    }

    public class YeuCauViewHolder extends RecyclerView.ViewHolder {

        private final ItemYeuCauBinding binding;
        int position;
        public YeuCauViewHolder(@NonNull ItemYeuCauBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

            // Set sự kiện nhấn vào item
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onItemClick(yeuCauList.get(position));
                    }
                }
            });
        }

        public void bind(YeuCau yeuCau) {
            // Truy vấn theo trường "id_user" trong bảng Users
            userRef.orderByChild("id_user").equalTo(yeuCau.getIdUser()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                            User user = userSnapshot.getValue(User.class);
                            if (user != null) {
                                binding.tvUserName.setText(user.getName());  // Hiển thị tên người dùng
                            } else {
                                binding.tvUserName.setText("Unknown User");
                            }
                        }
                    } else {
                        binding.tvUserName.setText("User Not Found");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    binding.tvUserName.setText("Error Loading");
                }
            });

            binding.tvMaUser.setText(yeuCau.getIdUser());
            binding.tvContent.setText(yeuCau.getContent());
            binding.tvTrangThai.setText(yeuCau.getIdTrangThai() == 0 ? "Chưa xử lý" : "Đã xử lý");
        }

    }


    // Interface để xử lý sự kiện click vào item
    public interface OnItemClickListener {
        void onItemClick(YeuCau yeuCau);
    }
}
