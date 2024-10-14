package com.example.xemphim.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.xemphim.R;
import com.example.xemphim.adapter.LichSuAdapter;
import com.example.xemphim.adapter.ThongTinLichSuAdapter;
import com.example.xemphim.databinding.ActivityLichSuXemBinding;
import com.example.xemphim.databinding.ActivityProfileBinding;
import com.example.xemphim.model.MovieDetail;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class LichSuXemActivity extends AppCompatActivity {
    private ActivityLichSuXemBinding binding;
    private LichSuAdapter lichSuAdapter;
    private List<MovieDetail.MovieItem> watchedMoviesList;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityLichSuXemBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setControl();
        setEven();
    }
    public void setControl(){
        // Initialize Firebase Database reference
        databaseReference = FirebaseDatabase.getInstance().getReference("watched_movies"); // Ensure the path is correct

        watchedMoviesList = new ArrayList<>();
        binding.rcvlichsu.setLayoutManager(new GridLayoutManager(this, 3));
        binding.rcvlichsu.setAdapter(lichSuAdapter);
    }
    public void setEven(){
        loadWatchHistory();
    }
    private void loadWatchHistory() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Please log in to view your watch history.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Reference to the user's watch history in Firebase
        DatabaseReference userHistoryRef = FirebaseDatabase.getInstance()
                .getReference("LichSuXem")
                .child(user.getUid()); // Using user ID to get the user's history

        userHistoryRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                watchedMoviesList.clear(); // Clear the previous list
                for (DataSnapshot movieSnapshot : snapshot.getChildren()) {
                    MovieDetail.MovieItem movieItem = movieSnapshot.getValue(MovieDetail.MovieItem.class);
                    if (movieItem != null) {
                        watchedMoviesList.add(movieItem); // Add to the list
                    }
                }
                // Set the adapter with the new data
                lichSuAdapter = new LichSuAdapter(LichSuXemActivity.this, watchedMoviesList);
                binding.rcvlichsu.setAdapter(lichSuAdapter);
                lichSuAdapter.notifyDataSetChanged(); // Notify the adapter of data changes
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("ProfileActivity", "Failed to read watch history: " + error.getMessage());
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Giữ màn hình sáng khi ứng dụng hoạt động
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Xóa cờ giữ màn hình sáng khi ứng dụng không còn hoạt động
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }
}