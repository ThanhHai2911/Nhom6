package com.example.xemphim.activity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.xemphim.R;
import com.example.xemphim.databinding.ActivityThemPhimBinding;

import java.util.ArrayList;

public class ThemPhimActivity extends AppCompatActivity {
    private ActivityThemPhimBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityThemPhimBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        xulyChonTheLoai();
        xulyChonQuocGia();
    }

    private void xulyChonQuocGia() {
        Spinner countrySpinner = findViewById(R.id.country_spinner);
        String selectedCountry = countrySpinner.getSelectedItem().toString();

        if (selectedCountry.equals("Chọn quốc gia")) {
            // Hiển thị thông báo yêu cầu người dùng chọn quốc gia
            Toast.makeText(this, "Vui lòng chọn một quốc gia hợp lệ.", Toast.LENGTH_SHORT).show();
        } else {
            // Xử lý khi chọn quốc gia hợp lệ
        }
    }

    private void xulyChonTheLoai() {
        setContentView(R.layout.activity_them_phim);
        Button selectGenresButton = findViewById(R.id.select_genres_button);
        TextView selectedGenresText = findViewById(R.id.selected_genres_text);

// Lấy danh sách thể loại từ resources
        String[] genres = getResources().getStringArray(R.array.genre_array);
        boolean[] selectedGenres = new boolean[genres.length]; // Mảng để theo dõi các mục được chọn
        ArrayList<String> selectedGenresList = new ArrayList<>(); // Lưu danh sách thể loại được chọn

// Khi nhấn nút chọn thể loại
        selectGenresButton.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Chọn thể loại")
                    .setMultiChoiceItems(genres, selectedGenres, (dialog, which, isChecked) -> {
                        // Khi một mục được chọn hoặc bỏ chọn
                        if (isChecked) {
                            selectedGenresList.add(genres[which]); // Thêm thể loại vào danh sách
                        } else {
                            selectedGenresList.remove(genres[which]); // Bỏ thể loại khỏi danh sách
                        }
                    })
                    .setPositiveButton("OK", (dialog, which) -> {
                        // Hiển thị thể loại đã chọn trong TextView
                        if (selectedGenresList.isEmpty()) {
                            selectedGenresText.setText("Bạn chưa chọn thể loại nào");
                        } else {
                            selectedGenresText.setText("Thể loại đã chọn: " + String.join(", ", selectedGenresList));
                        }
                    })
                    .setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss())
                    .show();
        });
    }

}