package com.example.xemphim.activity;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.xemphim.R;
import com.example.xemphim.adapter.CategoryAdapter;

import java.util.ArrayList;
import java.util.List;

public class QLTheLoaiActivity extends AppCompatActivity {
     private RecyclerView recyclerView;
     private CategoryAdapter categoryAdapter;
     private List<String> categoryList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qlthe_loai);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3)); // 3 cột giống như trong hình

        // Dữ liệu mẫu
        categoryList = new ArrayList<>();
        categoryList.add("Kinh dị");
        categoryList.add("Hành động");
        categoryList.add("Tình cảm");
        categoryList.add("Phiêu lưu");
        categoryList.add("Hài");
        categoryList.add("Gia đình");
        categoryList.add("Tài liệu");
        categoryList.add("Thiếu nhi");

        categoryAdapter = new CategoryAdapter(this, categoryList);
        recyclerView.setAdapter(categoryAdapter);
    }
}