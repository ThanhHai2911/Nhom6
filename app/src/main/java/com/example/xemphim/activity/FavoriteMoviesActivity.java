package com.example.xemphim.activity;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.telecom.Call;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.xemphim.API.ApiClient;
import com.example.xemphim.API.ApiService;
import com.example.xemphim.R;
import com.example.xemphim.adapter.FavoriteMoviesAdapter;
import com.example.xemphim.adapter.MovieAdapter;
import com.example.xemphim.databinding.ActivityFavoriteMoviesBinding;
import com.example.xemphim.databinding.ActivityMainBinding;
import com.example.xemphim.model.FavoriteMovie;
import com.example.xemphim.model.Movie;
import com.example.xemphim.response.MovieResponse;
import com.google.android.gms.common.api.Response;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Callback;

public class FavoriteMoviesActivity extends AppCompatActivity {
    private RecyclerView rvFavorites;
    private FavoriteMoviesAdapter favoriteMoviesAdapter;
    private List<FavoriteMovie> favoriteMovies = new ArrayList<>();
    private DatabaseReference favoritesRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite_movies);

        rvFavorites = findViewById(R.id.rvFavorites);
        rvFavorites.setLayoutManager(new LinearLayoutManager(this));

        favoritesRef = FirebaseDatabase.getInstance().getReference("favorites");
        loadFavoriteMovies();
    }

    private void loadFavoriteMovies() {
        favoritesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                favoriteMovies.clear(); // Clear current list
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    FavoriteMovie movie = snapshot.getValue(FavoriteMovie.class);
                    if (movie != null) {
                        favoriteMovies.add(movie);
                    }
                }
                favoriteMoviesAdapter = new FavoriteMoviesAdapter(FavoriteMoviesActivity.this, favoriteMovies);
                rvFavorites.setAdapter(favoriteMoviesAdapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(FavoriteMoviesActivity.this, "Lỗi khi tải danh sách yêu thích: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}



