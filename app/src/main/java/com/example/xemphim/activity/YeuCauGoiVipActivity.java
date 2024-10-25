package com.example.xemphim.activity;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.xemphim.R;
import com.example.xemphim.adapter.YeuCauAdapter;
import com.example.xemphim.databinding.ActivityYeuCauGoiVipBinding;
import com.example.xemphim.databinding.DialogYeuCauDetailBinding;
import com.example.xemphim.model.YeuCau;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

public class YeuCauGoiVipActivity extends AppCompatActivity {

    private ActivityYeuCauGoiVipBinding binding;
    private YeuCauAdapter adapter;
    private List<YeuCau> yeuCauList;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityYeuCauGoiVipBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Đặt padding cho view để tương thích với system bars
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Khởi tạo danh sách yêu cầu và adapter
        yeuCauList = new ArrayList<>();
        adapter = new YeuCauAdapter(yeuCauList);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerView.setAdapter(adapter);

        // Tham chiếu tới bảng YeuCau trên Firebase
        databaseReference = FirebaseDatabase.getInstance().getReference("YeuCau");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                yeuCauList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    YeuCau yeuCau = dataSnapshot.getValue(YeuCau.class);
                    yeuCauList.add(yeuCau);
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Xử lý lỗi
            }
        });
        // Set sự kiện click cho từng yêu cầu
        adapter.setOnItemClickListener(yeuCau -> showDetailDialog(yeuCau));
    }

    // Hàm hiển thị chi tiết yêu cầu khi nhấn vào
    private void showDetailDialog(YeuCau yeuCau) {
        // Sử dụng binding cho dialog
        DialogYeuCauDetailBinding dialogBinding = DialogYeuCauDetailBinding.inflate(LayoutInflater.from(this));

        // Lấy tham chiếu tới bảng Users
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users");

        // Truy vấn thông tin người dùng từ bảng Users theo id_user trong yêu cầu
        userRef.orderByChild("id_user").equalTo(yeuCau.getIdUser()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                        // Lấy thông tin người dùng
                        String userName = userSnapshot.child("name").getValue(String.class);
                        dialogBinding.tvUserNameDialog.setText(userName);  // Hiển thị tên người dùng
                    }
                } else {
                    dialogBinding.tvUserNameDialog.setText("Unknown User");  // Không tìm thấy người dùng
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                dialogBinding.tvUserNameDialog.setText("Error Loading User");
            }
        });

        // Set dữ liệu cho dialog từ yêu cầu
        dialogBinding.tvIdUserDialog.setText(yeuCau.getIdUser());
        dialogBinding.tvContentDialog.setText(yeuCau.getContent());
        dialogBinding.tvAmountDialog.setText(String.valueOf(yeuCau.getAmount()));
        dialogBinding.tvPaymentDateDialog.setText(yeuCau.getPaymentDate());

        // Tạo AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogBinding.getRoot());
        AlertDialog dialog = builder.create();
        dialog.show();

        // Xử lý sự kiện khi nhấn nút "Xác nhận lên VIP"
        dialogBinding.btnXacNhanVip.setOnClickListener(v -> {
            // Hiển thị hộp thoại xác nhận
            new AlertDialog.Builder(this)
                    .setTitle("Xác nhận")
                    .setMessage("Bạn có chắc chắn muốn xác nhận yêu cầu này lên VIP?")
                    .setPositiveButton("Đồng ý", (dialogInterface, which) -> {
                        // Nếu người dùng đồng ý, cập nhật trạng thái lên VIP
                        databaseReference.child(yeuCau.getIdLichSuTT()).child("idTrangThai").setValue(1); // Cập nhật trạng thái lên VIP
                        dialog.dismiss();
                    })
                    .setNegativeButton("Hủy", null)  // Hủy không làm gì
                    .show();
        });
    }


}
