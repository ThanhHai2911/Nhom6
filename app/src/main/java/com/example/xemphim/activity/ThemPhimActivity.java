package com.example.xemphim.activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.xemphim.R;
import com.example.xemphim.databinding.ActivityThemPhimBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ThemPhimActivity extends AppCompatActivity {
    private ActivityThemPhimBinding binding;
    private DatabaseReference databaseReference;
    private ArrayList<String> selectedGenresList = new ArrayList<>();
    private String[] genres; // Danh sách thể loại
    private boolean[] selectedGenres; // Trạng thái lựa chọn của các thể loại
    private HashMap<String, String> goiMap = new HashMap<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityThemPhimBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        // Khởi tạo danh sách thể loại và các biến liên quan
        genres = getResources().getStringArray(R.array.genre_array); // Lấy danh sách thể loại từ resources
        selectedGenres = new boolean[genres.length]; // Khởi tạo mảng trạng thái cho các thể loại
        // Lấy dữ liệu từ Firebase
        layDuLieuGoi();
        layDuLieuQuocGia();
        layDuLieuTheLoai();

        // Nút Thêm phim
        binding.submitButton.setOnClickListener(v -> themPhimVaoFirebase());
    }

    private void layDuLieuGoi() {
        DatabaseReference goiRef = FirebaseDatabase.getInstance().getReference("Goi");
        goiRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ArrayList<String> goiList = new ArrayList<>();
                goiList.add("Chọn Gói"); // Thêm tùy chọn mặc định đầu tiên

                goiMap.clear(); // Đảm bảo HashMap được xóa trước khi thêm mới

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    // Lấy giá trị của cột "type" và "id" trong bảng Goi
                    String goiType = snapshot.child("type").getValue(String.class);
                    Long goiIdLong = snapshot.child("id").getValue(Long.class);

                    if (goiType != null && goiIdLong != null) {
                        String goiId = String.valueOf(goiIdLong); // Chuyển Long thành String
                        goiList.add(goiType); // Thêm type vào danh sách
                        goiMap.put(goiType, goiId); // Lưu ID tương ứng vào HashMap
                    }
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<>(ThemPhimActivity.this, android.R.layout.simple_spinner_item, goiList);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                binding.goiSpinner.setAdapter(adapter);

                // Đặt giá trị mặc định
                binding.goiSpinner.setSelection(0);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(ThemPhimActivity.this, "Lỗi khi tải dữ liệu gói", Toast.LENGTH_SHORT).show();
            }
        });
    }




    private void layDuLieuQuocGia() {
        DatabaseReference quocGiaRef = FirebaseDatabase.getInstance().getReference("quocGia");
        quocGiaRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ArrayList<String> quocGiaList = new ArrayList<>();
                quocGiaList.add("Chọn quốc gia"); // Thêm tùy chọn mặc định đầu tiên

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    // Lấy giá trị của cột "name" trong bảng quocGia
                    String quocGiaName = snapshot.child("name").getValue(String.class);
                    if (quocGiaName != null) {
                        quocGiaList.add(quocGiaName); // Thêm name vào danh sách
                    }
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<>(ThemPhimActivity.this, android.R.layout.simple_spinner_item, quocGiaList);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                binding.countrySpinner.setAdapter(adapter);

                // Đặt giá trị mặc định
                binding.countrySpinner.setSelection(0);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(ThemPhimActivity.this, "Lỗi khi tải dữ liệu quốc gia", Toast.LENGTH_SHORT).show();
            }
        });
    }





    private void layDuLieuTheLoai() {
        DatabaseReference theLoaiRef = FirebaseDatabase.getInstance().getReference("theLoai");
        theLoaiRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String[] genresArray = new String[(int) dataSnapshot.getChildrenCount()];
                boolean[] selectedGenres = new boolean[genresArray.length];
                ArrayList<String> selectedGenresList = new ArrayList<>();

                int index = 0;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    // Lấy giá trị của cột "name" trong bảng theLoai
                    String theLoaiName = snapshot.child("name").getValue(String.class);
                    if (theLoaiName != null) {
                        genresArray[index] = theLoaiName;
                        index++;
                    }
                }

                Button selectGenresButton = findViewById(R.id.select_genres_button);
                TextView selectedGenresText = findViewById(R.id.selected_genres_text);

                selectGenresButton.setOnClickListener(v -> {
                    AlertDialog.Builder builder = new AlertDialog.Builder(ThemPhimActivity.this);
                    builder.setTitle("Chọn thể loại")
                            .setMultiChoiceItems(genresArray, selectedGenres, (dialog, which, isChecked) -> {
                                if (isChecked) {
                                    selectedGenresList.add(genresArray[which]);
                                } else {
                                    selectedGenresList.remove(genresArray[which]);
                                }
                            })
                            .setPositiveButton("OK", (dialog, which) -> {
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

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(ThemPhimActivity.this, "Lỗi khi tải dữ liệu thể loại", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void themPhimVaoFirebase() {
        // Lấy giá trị từ các trường nhập liệu
        String tenPhim = binding.title.getText().toString().trim();
        String moTa = binding.description.getText().toString().trim();
        String dienVien = binding.cast.getText().toString().trim();
        String tacGia = binding.actor.getText().toString().trim();
        String thoiLuong = binding.duration.getText().toString().trim();
        String namPhatHanh = binding.releaseYear.getText().toString().trim();
        String posterUrl = binding.posterUrl.getText().toString().trim();
        String thumbUrl = binding.thumbUrl.getText().toString().trim();
        String movieUrl = binding.movieUrl.getText().toString().trim();
// Lấy giá trị từ Spinner
        String goi = binding.goiSpinner.getSelectedItem().toString();
        String goiId = goiMap.get(goi); // Lấy ID từ HashMap
        // Lấy giá trị từ Spinner
        String quocGia = binding.countrySpinner.getSelectedItem().toString();

        // Lấy thể loại từ TextView (đã được chọn từ dialog)
        String theLoai = binding.selectedGenresText.getText().toString().replace("Thể loại đã chọn: ", "").trim();

        // Kiểm tra xem các trường bắt buộc có bị bỏ trống hay không
        if (TextUtils.isEmpty(tenPhim) || TextUtils.isEmpty(moTa) || TextUtils.isEmpty(dienVien) ||
                TextUtils.isEmpty(tacGia) || TextUtils.isEmpty(thoiLuong) || TextUtils.isEmpty(namPhatHanh) ||
                TextUtils.isEmpty(posterUrl) || TextUtils.isEmpty(thumbUrl) || TextUtils.isEmpty(movieUrl) ||
                goi.equals("Chọn Gói") || quocGia.equals("Chọn quốc gia") || theLoai.isEmpty()) {

            Toast.makeText(ThemPhimActivity.this, "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }
        // Kiểm tra xem ID có hợp lệ không
        if (goiId == null) {
            Toast.makeText(ThemPhimActivity.this, "Vui lòng chọn một gói hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }
        // Lấy ngày hiện tại dưới dạng dd/MM/yyyy
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        String ngayHienTai = dateFormat.format(Calendar.getInstance().getTime());

        // Tạo ID ngẫu nhiên cho phim
        String idPhim = FirebaseDatabase.getInstance().getReference().child("Movies").push().getKey();

        // Tạo đối tượng phim chứa thông tin
        Map<String, Object> movieData = new HashMap<>();
        movieData.put("id_movie", idPhim);
        movieData.put("name", tenPhim);
        movieData.put("content", moTa);
        movieData.put("poster_url", posterUrl);
        movieData.put("thumb_url", thumbUrl);
        movieData.put("goi", goiId); // Lưu ID gói vào dữ liệu phim
        movieData.put("time", thoiLuong);
        movieData.put("year", namPhatHanh);
        movieData.put("tacGia", tacGia);
        movieData.put("theLoai", theLoai);
        movieData.put("dienVien", dienVien);
        movieData.put("quocGia", quocGia);
        movieData.put("rating", 0);  // Đặt giá trị mặc định
        movieData.put("id_kieuPhim", 0);  // Đặt giá trị mặc định

        // Thêm cột ngày thêm phim và ngày cập nhật với giá trị ngày hiện tại
        movieData.put("ngayThemPhim", ngayHienTai);
        movieData.put("ngayCapNhat", ngayHienTai);

        // Thêm phim vào Firebase
        DatabaseReference moviesRef = FirebaseDatabase.getInstance().getReference().child("Movies");
        if (idPhim != null) {
            moviesRef.child(idPhim).setValue(movieData).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(ThemPhimActivity.this, "Thêm phim thành công!", Toast.LENGTH_SHORT).show();
                    resetForm();
                } else {
                    Toast.makeText(ThemPhimActivity.this, "Thêm phim thất bại!", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void resetForm() {
        // Đặt lại các trường nhập liệu
        binding.title.setText("");
        binding.description.setText("");
        binding.cast.setText("");
        binding.actor.setText("");
        binding.duration.setText("");
        binding.releaseYear.setText("");
        binding.posterUrl.setText("");
        binding.thumbUrl.setText("");
        binding.movieUrl.setText("");

        // Đặt lại spinner "Gói" về mặc định
        binding.goiSpinner.setSelection(0);  // Vị trí 0 là "Chọn Gói"

        // Đặt lại spinner "Quốc gia" về mặc định
        binding.countrySpinner.setSelection(0);  // Vị trí 0 là "Chọn quốc gia"

        // Đặt lại TextView chọn thể loại
        binding.selectedGenresText.setText("Bạn chưa chọn thể loại nào");

        // Đặt lại danh sách thể loại đã chọn
        selectedGenresList.clear();
        selectedGenres = new boolean[genres.length];  // Reset các lựa chọn thể loại
    }


}
