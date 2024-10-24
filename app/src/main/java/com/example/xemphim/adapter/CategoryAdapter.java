package com.example.xemphim.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.example.xemphim.R;

import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {

    private List<String> categoryList;
    private Context context;
    private OnEditClickListener onEditClickListener;
    private OnDeleteClickListener onDeleteClickListener;

    public interface OnDeleteClickListener {
        void onDeleteClick(int position);
    }
    // Interface để gọi lại khi người dùng nhấn vào nút chỉnh sửa
    public interface OnEditClickListener {
        void onEditClick(int position, String currentName);
    }
    public CategoryAdapter(Context context, List<String> categoryList, OnEditClickListener onEditClickListener, OnDeleteClickListener onDeleteClickListener) {
        this.context = context;
        this.categoryList = categoryList;
        this.onEditClickListener = onEditClickListener;
        this.onDeleteClickListener = onDeleteClickListener; // Gán giá trị cho biến này
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

        holder.editIcon.setOnClickListener(v -> {
            // Kiểm tra nếu onEditClickListener không null
            if (onEditClickListener != null) {
                onEditClickListener.onEditClick(position, category);
            }
        });


        // Sự kiện khi nhấn vào nút xóa
        holder.deleteIcon.setOnClickListener(v -> {
            // Kiểm tra nếu onDeleteClickListener không null
            if (onDeleteClickListener != null) {
                new AlertDialog.Builder(context)
                        .setTitle("Xác nhận xóa")
                        .setMessage("Bạn có muốn xóa thể loại này không?")
                        .setPositiveButton("Có", (dialog, which) -> {
                            onDeleteClickListener.onDeleteClick(position);
                        })
                        .setNegativeButton("Không", null) // Không làm gì nếu nhấn "Không"
                        .show();
            }
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

