package com.example.xemphim.activity;


import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;

import com.example.xemphim.R;
import com.example.xemphim.adapter.CategoryAdapter;
import com.example.xemphim.databinding.ActivityQltheLoaiBinding;
import com.example.xemphim.databinding.DialogAddCategoryBinding;
import com.example.xemphim.model.Category;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class QLTheLoaiActivity extends AppCompatActivity {

    private CategoryAdapter categoryAdapter;
    private List<Category> categoryList = new ArrayList<>();
    private ActivityQltheLoaiBinding binding;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Ánh xạ layout của Activity với View Binding
        binding = ActivityQltheLoaiBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Khởi tạo Firebase Database reference
        databaseReference = FirebaseDatabase.getInstance().getReference("theLoai");

        // Khởi tạo danh sách và RecyclerView
        categoryList = new ArrayList<>();
        xulyRecyclerView();
        xulyThem();

        // Đọc dữ liệu từ Firebase
        docDuLieuFirebase();
    }

    private void showCategoryDialog(int position, String currentName) {
        // Tạo dialog và sử dụng View Binding cho dialog
        Dialog dialog = new Dialog(QLTheLoaiActivity.this);
        DialogAddCategoryBinding dialogBinding = DialogAddCategoryBinding.inflate(getLayoutInflater());
        dialog.setContentView(dialogBinding.getRoot());

        // Đặt tên thể loại hiện tại vào EditText nếu là sửa
        if (position != -1) { // Nếu position khác -1 thì là sửa
            dialogBinding.editTextCategoryName.setText(currentName);
            dialogBinding.buttonAddCategory.setText("Cập nhâp");
        } else {
            dialogBinding.editTextCategoryName.setText(""); // Nếu là thêm, thì để trống
        }

        // Set background bo tròn cho dialog
        dialog.getWindow().setBackgroundDrawableResource(R.drawable.dialog_rounded_background);
        dialog.getWindow().setLayout(800, 600); // Tùy chỉnh kích thước

        // Set sự kiện khi nhấn nút "Thêm/Cập nhật" trong dialog
        dialogBinding.buttonAddCategory.setOnClickListener(v -> {
            String newCategoryName = dialogBinding.editTextCategoryName.getText().toString();
            if (!newCategoryName.isEmpty()) {
                if (position != -1) {

                    // Nếu là sửa
                    new AlertDialog.Builder(QLTheLoaiActivity.this)
                            .setTitle("Xác nhận cập nhật")
                            .setMessage("Bạn có muốn cập nhật thể loại này không?")
                            .setPositiveButton("Có", (dialog1, which) -> {
                                updateCategory(position, newCategoryName);
                            })
                            .setNegativeButton("Không", null) // Không làm gì nếu nhấn "Không"
                            .show();

                } else {
                    // Nếu là thêm
                    new AlertDialog.Builder(QLTheLoaiActivity.this)
                            .setTitle("Xác nhận thêm")
                            .setMessage("Bạn có muốn thêm thể loại này không?")
                            .setPositiveButton("Có", (dialog1, which) -> {
                                themTheLoaiFirebase(newCategoryName);
                            })
                            .setNegativeButton("Không", null) // Không làm gì nếu nhấn "Không"
                            .show();
                }
                dialog.dismiss(); // Đóng dialog sau khi thêm hoặc cập nhật
            } else {
                Toast.makeText(QLTheLoaiActivity.this, "Vui lòng nhập tên thể loại", Toast.LENGTH_SHORT).show();
            }
        });

        // Hiển thị dialog
        dialog.show();
    }
//xoa the loai
private void deleteCategory(int position) {
    // Lấy ID từ categoryList
    String categoryId = categoryList.get(position).getId();

    // Xóa thể loại khỏi Firebase dựa trên ID thực
    FirebaseDatabase.getInstance()
            .getReference("theLoai")
            .child(categoryId) // Xóa bằng ID từ Firebase
            .removeValue();

    // Xóa thể loại khỏi danh sách và cập nhật RecyclerView
    categoryList.remove(position);
    categoryAdapter.notifyItemRemoved(position);

    Toast.makeText(QLTheLoaiActivity.this, "Xóa thể loại thành công", Toast.LENGTH_SHORT).show();
}


    // sửa thể loại
    private void updateCategory(int position, String newCategoryName) {
        // Lấy ID từ categoryList
        String categoryId = categoryList.get(position).getId();

        // Cập nhật tên thể loại trong Firebase dựa trên ID
        FirebaseDatabase.getInstance()
                .getReference("theLoai")
                .child(categoryId) // Cập nhật theo ID từ Firebase
                .child("name")
                .setValue(newCategoryName);

        // Cập nhật tên trong danh sách và RecyclerView
        categoryList.get(position).name = newCategoryName;
        categoryAdapter.notifyItemChanged(position);

        Toast.makeText(QLTheLoaiActivity.this, "Cập nhật thể loại thành công", Toast.LENGTH_SHORT).show();
    }


    private void xulyThem() {
        binding.btnAddTheLoai.setOnClickListener(v -> {
            // Gọi dialog để thêm thể loại
            showCategoryDialog(-1, null); // -1 cho biết là thêm
        });
    }

    private void xulyRecyclerView() {
        binding.recyclerView.setLayoutManager(new GridLayoutManager(this, 3)); // 3 cột

        // Khởi tạo adapter và set cho RecyclerView
        categoryAdapter = new CategoryAdapter(this, categoryList, new CategoryAdapter.OnEditClickListener() {
            @Override
            public void onEditClick(int position, Category currentCategory) {
                // Truyền cả đối tượng Category (bao gồm id và name)
                showCategoryDialog(position, currentCategory.getName());
            }
        }, new CategoryAdapter.OnDeleteClickListener() {
            @Override
            public void onDeleteClick(int position) {
                deleteCategory(position); // Gọi phương thức xóa
            }
        });

        binding.recyclerView.setAdapter(categoryAdapter);
    }


    private void docDuLieuFirebase() {
        // Lắng nghe dữ liệu từ Firebase Realtime Database
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                categoryList.clear(); // Xóa dữ liệu cũ

                // Duyệt qua tất cả các thể loại trong "theLoai"
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    // Lấy giá trị "id" và "name" từ từng mục
                    String id = snapshot.getKey(); // Sử dụng key từ Firebase
                    String categoryName = snapshot.child("name").getValue(String.class);

                    // Thêm đối tượng Category vào danh sách
                    categoryList.add(new Category(id, categoryName));
                }

                // Cập nhật adapter
                categoryAdapter.notifyDataSetChanged();
            }


            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Xử lý lỗi (nếu có)
                Toast.makeText(QLTheLoaiActivity.this, "Lỗi khi lấy dữ liệu từ Firebase", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void themTheLoaiFirebase(String categoryName) {
        // Lấy số lượng thể loại hiện tại để tạo id mới
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                long count = dataSnapshot.getChildrenCount(); // Đếm số lượng hiện có để tạo id mới

                // Tạo id mới
                String newId = String.valueOf(count + 1); // Nếu count bắt đầu từ 0, id đầu tiên sẽ là 1

                // Tạo một HashMap để thêm dữ liệu
                HashMap<String, Object> newCategory = new HashMap<>();
                newCategory.put("id", newId);  // Sử dụng id vừa tạo
                newCategory.put("name", categoryName);

                // Thêm thể loại mới vào Firebase
                databaseReference.child(newId).setValue(newCategory)
                        .addOnSuccessListener(aVoid ->
                                Toast.makeText(QLTheLoaiActivity.this, "Thêm thể loại thành công", Toast.LENGTH_SHORT).show()
                        )
                        .addOnFailureListener(e ->
                                Toast.makeText(QLTheLoaiActivity.this, "Lỗi khi thêm thể loại", Toast.LENGTH_SHORT).show()
                        );
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(QLTheLoaiActivity.this, "Lỗi khi lấy dữ liệu", Toast.LENGTH_SHORT).show();
            }
        });
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Giải phóng tài nguyên của binding khi Activity bị hủy
        binding = null;
    }
}
