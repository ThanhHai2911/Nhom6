package com.example.xemphim.activity;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.xemphim.R;
import com.example.xemphim.adapter.ThongBaoAdapter;
import com.example.xemphim.databinding.ActivityThongBaoBinding;
import com.example.xemphim.model.ThongBao;

import java.util.ArrayList;
import java.util.List;

public class ThongBaoActivity extends AppCompatActivity {
    private ThongBaoAdapter thongBaoAdapter;
    private ActivityThongBaoBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       binding = ActivityThongBaoBinding.inflate(getLayoutInflater());
       setContentView(binding.getRoot());

       xulyrecyclerView();
    }

    private void xulyrecyclerView() {
        binding.thongbaorecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Tạo danh sách thông báo
        List<ThongBao> thongBaoList = new ArrayList<>();
        thongBaoList.add(new ThongBao("FlicksNow", "13:07 18/12/2023", "Có phim hay lắm, rảnh thì tìm trên trang chủ."));
        thongBaoList.add(new ThongBao("Thông Báo 2", "10:45 17/12/2023", "Sự kiện sắp tới tại rạp chiếu phim!"));
        // Thêm nhiều thông báo hơn

        // Khởi tạo Adapter và gán cho RecyclerView
        thongBaoAdapter = new ThongBaoAdapter(thongBaoList);
        binding.thongbaorecyclerView.setAdapter(thongBaoAdapter);
    }
}