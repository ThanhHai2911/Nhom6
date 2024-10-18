package com.example.xemphim.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.example.xemphim.R;

import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {

    private List<String> categoryList;
    private Context context;

    public CategoryAdapter(Context context, List<String> categoryList) {
        this.context = context;
        this.categoryList = categoryList;
    }

    @Override
    public CategoryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_category, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(CategoryViewHolder holder, int position) {
        String category = categoryList.get(position);
        holder.categoryName.setText(category);

        // Sự kiện khi nhấn vào nút sửa
        holder.editIcon.setOnClickListener(v -> {
            // Code chỉnh sửa thể loại
            Toast.makeText(context, "Sửa " + category, Toast.LENGTH_SHORT).show();
        });

        // Sự kiện khi nhấn vào nút xóa
        holder.deleteIcon.setOnClickListener(v -> {
            // Code xóa thể loại
            Toast.makeText(context, "Xóa " + category, Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public int getItemCount() {
        return categoryList.size();
    }

    public static class CategoryViewHolder extends RecyclerView.ViewHolder {

        TextView categoryName;
        ImageView editIcon, deleteIcon;

        public CategoryViewHolder(View itemView) {
            super(itemView);
            categoryName = itemView.findViewById(R.id.category_name);
            editIcon = itemView.findViewById(R.id.edit_icon);
            deleteIcon = itemView.findViewById(R.id.delete_icon);
        }
    }
}

