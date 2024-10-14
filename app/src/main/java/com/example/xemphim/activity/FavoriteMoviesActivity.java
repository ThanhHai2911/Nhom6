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
import androidx.recyclerview.widget.RecyclerView;

import com.example.xemphim.API.ApiClient;
import com.example.xemphim.API.ApiService;
import com.example.xemphim.R;
import com.example.xemphim.adapter.FavoriteAdapter;
import com.example.xemphim.adapter.MovieAdapter;
import com.example.xemphim.databinding.ActivityFavoriteMoviesBinding;
import com.example.xemphim.databinding.ActivityMainBinding;
import com.example.xemphim.model.Movie;
import com.example.xemphim.response.MovieResponse;
import com.google.android.gms.common.api.Response;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Callback;

public class FavoriteMoviesActivity extends AppCompatActivity {

}



