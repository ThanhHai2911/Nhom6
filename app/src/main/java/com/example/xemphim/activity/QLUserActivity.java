package com.example.xemphim.activity;

import android.os.Bundle;
import android.widget.ArrayAdapter;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.xemphim.R;
import com.example.xemphim.adapter.UserAdapter;
import com.example.xemphim.databinding.ActivityQluserBinding;
import com.example.xemphim.model.User;

import java.util.ArrayList;
import java.util.List;

public class QLUserActivity extends AppCompatActivity {
     private ActivityQluserBinding binding;
     private UserAdapter userAdapter;
    private List<User> userList; // Danh sách người dùng
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityQluserBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        xulyTimKiemSpiner();

        hienThiRecyclerView();
        loadDuLieu();
    }

    private void loadDuLieu() {
        // Thêm dữ liệu vào danh sách người dùng
        userList.add(new User("1","Nguyễn Văn A", "online", true, false));
        userList.add(new User("2","Trần Thị B", "offline", false, true));
        // Thêm nhiều người dùng hơn nếu cần

        // Thông báo adapter cập nhật dữ liệu
        userAdapter.notifyDataSetChanged();
    }

    private void hienThiRecyclerView() {
        // Khởi tạo RecyclerView
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Khởi tạo danh sách người dùng và adapter
        userList = new ArrayList<>();
        userAdapter = new UserAdapter(userList);
        binding.recyclerView.setAdapter(userAdapter);
    }

    private void xulyTimKiemSpiner() {
        // Setup Spinner using binding
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.status_options, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Use binding.spinnerStatus to access the Spinner
        binding.spinnerStatus.setAdapter(adapter);
    }
}