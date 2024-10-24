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
    private List<String> categoryList;
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
    // Xóa thể loại từ danh sách
    String categoryName = categoryList.get(position);
    categoryList.remove(position); // Xóa thể loại khỏi danh sách
    categoryAdapter.notifyItemRemoved(position); // Cập nhật RecyclerView

    // Xóa thể loại từ Firebase
    FirebaseDatabase.getInstance()
            .getReference("theLoai")
            .child(String.valueOf(position)) // Giả định rằng ID là vị trí, có thể cần thay đổi nếu ID khác
            .removeValue();

    Toast.makeText(QLTheLoaiActivity.this, "Xóa thể loại " + categoryName + " thành công", Toast.LENGTH_SHORT).show();
}

    // sửa thể loại
    private void updateCategory(int position, String newCategoryName) {
        // Cập nhật tên thể loại trong danh sách
        categoryList.set(position, newCategoryName);
        categoryAdapter.notifyItemChanged(position); // Cập nhật item trong RecyclerView

        // Cập nhật tên thể loại trong Firebase
        // Tìm id tương ứng với position và cập nhật
        FirebaseDatabase.getInstance()
                .getReference("theLoai")
                .child(String.valueOf(position)) // Dùng vị trí để lấy id
                .child("name")
                .setValue(newCategoryName);

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
            public void onEditClick(int position, String currentName) {
               showCategoryDialog(position, currentName);
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
                    // Lấy giá trị "name" từ từng mục
                    String categoryName = snapshot.child("name").getValue(String.class);
                    categoryList.add(categoryName); // Thêm tên thể loại vào danh sách
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
        // Đọc dữ liệu từ Firebase để lấy id tiếp theo
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                long maxId = -1;

                // Duyệt qua các thể loại hiện có để tìm ID lớn nhất
                for (DataSnapshot categorySnapshot : dataSnapshot.getChildren()) {
                    Object idObject = categorySnapshot.child("id").getValue();
                    if (idObject != null) {
                        try {
                            long idLong = Long.parseLong(idObject.toString());  // Chuyển đổi từ chuỗi sang số
                            if (idLong > maxId) {
                                maxId = idLong;
                            }
                        } catch (NumberFormatException e) {
                            e.printStackTrace();
                        }
                    }
                }

                // ID mới sẽ là maxId + 1
                long newId = maxId + 1;
                String idString = String.valueOf(newId);

                // Tạo một HashMap để thêm dữ liệu
                HashMap<String, Object> newCategory = new HashMap<>();
                newCategory.put("id", idString);
                newCategory.put("name", categoryName);

                // Thêm thể loại mới vào Firebase với key là số thứ tự mới
                databaseReference.child(idString).setValue(newCategory);

                Toast.makeText(QLTheLoaiActivity.this, "Thêm thể loại thành công", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Xử lý lỗi (nếu có)
                Toast.makeText(QLTheLoaiActivity.this, "Lỗi khi thêm thể loại", Toast.LENGTH_SHORT).show();
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
