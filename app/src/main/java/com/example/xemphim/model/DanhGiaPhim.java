package com.example.xemphim.model;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.support.annotation.NonNull;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.example.xemphim.R;
import com.example.xemphim.activity.XemPhimActivity;
import com.example.xemphim.databinding.ActivityXemphimBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class DanhGiaPhim {
    private ActivityXemphimBinding binding;
    private DatabaseReference ratingsRef;
    private Context context;
    private String idUser;
    private String movieSlug;

    public DanhGiaPhim(Context context, ActivityXemphimBinding binding,String movieSlug) {
        this.context = context;
        this.binding = binding;
        this.movieSlug = movieSlug;
        laythongtinUser();
        ratingsRef = FirebaseDatabase.getInstance().getReference("Ratings");
    }


    public void saveRating(String movieSlug, String userId, float rating) {
        Map<String, Object> ratingData = new HashMap<>();
        ratingData.put("userId", userId);
        ratingData.put("rating", rating);
        ratingData.put("ratedAt", System.currentTimeMillis());

        new AlertDialog.Builder(context)
                .setTitle("Đánh Giá")
                .setMessage("Cảm ơn bạn đã đáng giá")
                .setPositiveButton("Ok", (dialog, which) -> {

                    ratingsRef.child(movieSlug).child(userId).setValue(ratingData)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(context, "Đánh giá đã được lưu!", Toast.LENGTH_SHORT).show();
                                // Cập nhật rating cho RatingBar
                                binding.ratingBar.setRating(rating); // Cập nhật số sao đã đánh giá
                                binding.ratingBar.setProgressTintList(ColorStateList.valueOf(context.getResources().getColor(R.color.color_your_rating))); // Đặt màu sắc cho RatingBar
                                // Gọi hàm tính toán điểm trung bình
                                calculateAverageRating();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(context, "Lỗi khi lưu đánh giá: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                })
                .show(); // Hiển thị hộp thoại
    }


    public void calculateAverageRating() {
        ratingsRef.child(movieSlug).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int totalRatings = 0;
                float sumRatings = 0;

                for (DataSnapshot ratingSnapshot : snapshot.getChildren()) {
                    float rating = ratingSnapshot.child("rating").getValue(Float.class);
                    sumRatings += rating;
                    totalRatings++;
                }

                if (totalRatings > 0) {
                    float averageRating = sumRatings / totalRatings;
                    // Cập nhật giao diện với tổng số đánh giá và trung bình sao
                    binding.tvAverageRating.setText("( " + averageRating + " điểm / " + totalRatings + " lượt)");
                } else {
                    binding.tvAverageRating.setText("( 0 điểm / 0 lượt )");
                    binding.ratingBar.setRating(0); // Reset ratingBar nếu không có đánh giá
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Xử lý lỗi
                Toast.makeText(context, "Lỗi khi tính toán đánh giá: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void kiemTraDanhGia() {
        // Lấy thông tin cần thiết
        String userId = idUser; // ID của người dùng hiện tại
        String movieSlug = this.movieSlug; // Slug của phim

        // Kiểm tra nếu người dùng đã đánh giá phim hay chưa
        DatabaseReference ratingRef;

        if (userId != null) {
            // Nếu người dùng đã đăng nhập, lấy thông tin đánh giá từ Firebase
            ratingRef = FirebaseDatabase.getInstance().getReference("Ratings")
                    .child(movieSlug)  // slug của phim
                    .child(userId);  // ID người dùng

            ratingRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        // Người dùng đã đánh giá, lấy rating
                        int userRating = dataSnapshot.child("rating").getValue(Integer.class);
                        // Highlight sao đã đánh giá với màu sắc tương ứng
                        binding.ratingBar.setRating(userRating);
                        binding.ratingBar.setIsIndicator(false); // Cho phép người dùng chỉnh sửa
                        binding.ratingBar.setProgressTintList(ColorStateList.valueOf(context.getResources().getColor(R.color.color_your_rating))); // Đặt màu sao theo rating
                    } else {
                        // Người dùng chưa đánh giá
                        binding.ratingBar.setRating(0);
                        binding.ratingBar.setIsIndicator(false); // Cho phép đánh giá
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    // Xử lý lỗi
                    Toast.makeText(context, "Lỗi khi kiểm tra đánh giá: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            // Nếu người dùng chưa đăng nhập, cho phép đánh giá nhưng không lưu vào Firebase
            binding.ratingBar.setRating(0);
            binding.ratingBar.setIsIndicator(false); // Cho phép đánh giá
            binding.ratingBar.setOnRatingBarChangeListener((ratingBar, rating, fromUser) -> {
                Toast.makeText(context, "Bạn đã đánh giá " + rating + " sao", Toast.LENGTH_SHORT).show();
            });
        }
    }

    private void laythongtinUser() {
        SharedPreferences sharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        idUser = sharedPreferences.getString("id_user", null);
    }
}
