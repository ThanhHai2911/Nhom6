package com.example.xemphim.model;
import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.xemphim.model.MovieDetail;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class LichSuPhim {
    private DatabaseReference lichSuXemRef;
    private String idUser;
    private Context context;

    public LichSuPhim(Context context) {
        this.context = context;
        lichSuXemRef = FirebaseDatabase.getInstance().getReference("LichSuXem");
        laythongtinUser();
    }

    private void laythongtinUser() {
        SharedPreferences sharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        idUser = sharedPreferences.getString("id_user", null);
    }

    public void luuLichSuXem(String movieSlug, String episodeName, List<MovieDetail.Episode.ServerData> serverDataList) {
        if (movieSlug == null) {
            Toast.makeText(context, "Không thể lưu lịch sử, thiếu thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if the user is logged in
        if (idUser == null) {
            // User is not logged in, save to LichSuXemKhongDangNhap
            saveHistoryForGuest(movieSlug, episodeName);
        } else {
            // User is logged in, proceed with existing logic
            Query query = lichSuXemRef.orderByChild("id_user").equalTo(idUser);
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    String idLichSuXem = null;

                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        String existingSlug = snapshot.child("slug").getValue(String.class);
                        if (existingSlug != null && existingSlug.equals(movieSlug)) {
                            idLichSuXem = snapshot.getKey();
                            break;
                        }
                    }

                    if (idLichSuXem != null) {
                        String movieLink = getMovieLinkForEpisode(episodeName, serverDataList);
                        updateExistingMovieHistory(idLichSuXem, episodeName, movieLink);
                    } else {
                        addNewMovieHistory(movieSlug, episodeName);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(context, "Lỗi khi kiểm tra lịch sử", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void saveHistoryForGuest(String movieSlug, String episodeName) {
        DatabaseReference lichSuKhongDangNhapRef = FirebaseDatabase.getInstance().getReference("LichSuXemKhongDangNhap");
        String idLichSuXem = lichSuKhongDangNhapRef.push().getKey();
        String watchedAt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

        Map<String, Object> lichSuXem = new HashMap<>();
        lichSuXem.put("slug", movieSlug);
        lichSuXem.put("watched_at", watchedAt);
        lichSuXem.put("tapphim", episodeName);

        lichSuKhongDangNhapRef.child(idLichSuXem).setValue(lichSuXem)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(context, "Lưu lịch sử xem phim thành công cho người dùng không đăng nhập", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(context, "Lưu lịch sử xem phim thất bại cho người dùng không đăng nhập", Toast.LENGTH_SHORT).show();
                    }
                });
    }



    private String getMovieLinkForEpisode(String episodeName, List<MovieDetail.Episode.ServerData> serverDataList) {
        for (MovieDetail.Episode.ServerData episode : serverDataList) {
            if (episode.getName().equals(episodeName)) {
                return episode.getLinkM3u8();
            }
        }
        return null;
    }

    private void addNewMovieHistory(String movieSlug, String episodeName) {
        String idLichSuXem = lichSuXemRef.push().getKey();
        String watchedAt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

        Map<String, Object> lichSuXem = new HashMap<>();
        lichSuXem.put("id_user", idUser);
        lichSuXem.put("slug", movieSlug);
        lichSuXem.put("watched_at", watchedAt);
        lichSuXem.put("tapphim", episodeName);

        lichSuXemRef.child(idLichSuXem).setValue(lichSuXem)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(context, "Lưu lịch sử xem phim thành công", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(context, "Lưu lịch sử xem phim thất bại", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateExistingMovieHistory(String idLichSuXem, String episodeName, String movieLink) {
        String watchedAt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

        Map<String, Object> updates = new HashMap<>();
        updates.put("episode", episodeName);
        updates.put("movie_link", movieLink);
        updates.put("watched_at", watchedAt);

        lichSuXemRef.child(idLichSuXem).updateChildren(updates)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(context, "Cập nhật lịch sử xem thành công", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(context, "Cập nhật lịch sử xem thất bại", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
