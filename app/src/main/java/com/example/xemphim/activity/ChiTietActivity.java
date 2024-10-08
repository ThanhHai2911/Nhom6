package com.example.xemphim.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.xemphim.API.ApiClient;
import com.example.xemphim.API.ApiService;
import com.example.xemphim.adapter.TapPhimAdapter;
import com.example.xemphim.databinding.ActivityChitietphimBinding;
import com.example.xemphim.model.ChiTietPhim;
import com.example.xemphim.model.LinkPhim;
import com.example.xemphim.model.Movie;


import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

public class ChiTietActivity extends AppCompatActivity {
    private String movieSlug;
    private List<LinkPhim> serverDataList = new ArrayList<>();
    private RecyclerView rcvTapPhim;
    private ActivityChitietphimBinding binding;
    private ApiService apiService;
    private TapPhimAdapter tapPhimAdapter;
    private String movieLink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChitietphimBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setEvent();
        setContol();
    }
    private void setContol() {
        rcvTapPhim = binding.rcvTapPhim;
        GridLayoutManager layoutManager = new GridLayoutManager(this, 4);
        rcvTapPhim.setLayoutManager(layoutManager);
    }

    private void setEvent() {
        // Lấy slug từ Intent
        movieSlug = getIntent().getStringExtra("slug");
        // Lấy chi tiết phim
        //fetchMovieDetail();
        apiService = ApiClient.getClient().create(ApiService.class);
        loadMovieDetails(movieSlug);
//        // Xử lý sự kiện click nút xem phim
        binding.btnXemPhim.setOnClickListener(view -> {
            if (movieLink != null && !movieLink.isEmpty()) {
                // Khởi động activity phát video
                Intent intent = new Intent(this, XemPhimActivity.class);
                intent.putExtra("movie_link", movieLink);  // Truyền link phim
                startActivity(intent);
            } else {
                Toast.makeText(ChiTietActivity.this, "Link phim không khả dụng", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void loadMovieDetails(String slug) {

        // Ensure apiService is initialized before calling this
        Call<ChiTietPhim> call = apiService.getMovieDetail(slug);
        call.enqueue(new Callback<ChiTietPhim>() {
            @Override
            public void onResponse(Call<ChiTietPhim> call, Response<ChiTietPhim> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Get movie details from the response
                    Movie movie = response.body().getMovie();  // Assuming the response returns a Movie object

                    // Hiển thị thông tin phim using ViewBinding
                    binding.textViewTitle.setText(movie.getName());
                    binding.textViewDescription.setText(movie.getContent());
                    binding.textViewYear.setText(String.valueOf(movie.getYear()));
                    binding.textViewActors.setText(TextUtils.join(", ", movie.getActor()));
                    binding.textViewDirector.setText(TextUtils.join(", ", movie.getDirector()));

                    // Tải poster bằng Glide (poster image and thumbnail)
                    Glide.with(ChiTietActivity.this)
                            .load(movie.getThumb_url())
                            .into(binding.imageViewthumburl);  // Use correct binding ID

                    Glide.with(ChiTietActivity.this)
                            .load(movie.getPoster_url())
                            .into(binding.imageViewPoster);  // Use correct binding ID

                    // Lấy danh sách các tập phim
                    List<LinkPhim> tapPhim = response.body().getEpisodes();
                    if (tapPhim != null && !tapPhim.isEmpty()) {
                        // Lưu danh sách các tập phim
                        serverDataList.clear(); // Xóa danh sách cũ
                        for (LinkPhim episode : tapPhim) {
                            List<LinkPhim> data = episode.getServerData();
                            if (data != null) {
                                serverDataList.addAll(data); // Thêm tất cả các tập phim vào danh sách
                            }
                        }

                        // Cập nhật RecyclerView với danh sách tập phim
                        // Cập nhật RecyclerView với danh sách tập phim
                        tapPhimAdapter = new TapPhimAdapter(ChiTietActivity.this, serverDataList, new TapPhimAdapter.OnEpisodeClickListener() {
                            @Override
                            public void onEpisodeClick(String linkM3u8) {
                                // Khi người dùng click vào tập phim
                                Intent intent = new Intent(ChiTietActivity.this, XemPhimActivity.class);
                                intent.putExtra("movie_link", linkM3u8);
                                startActivity(intent);
                            }
                        });

                        rcvTapPhim.setAdapter(tapPhimAdapter);

                        // Lấy link của tập đầu tiên
                        LinkPhim firstServerData = serverDataList.get(0);
                        if (firstServerData != null) {
                            movieLink = firstServerData.getLinkM3u8();
                            Log.d("MovieDetailActivity", "Link phim tập 1: " + movieLink);
                        }
                        loadTieuDe(movie);
                    } else {
                        Toast.makeText(ChiTietActivity.this, "Không có tập phim nào", Toast.LENGTH_SHORT).show();
                    }


                } else {
                    Toast.makeText(ChiTietActivity.this, "Failed to load movie details", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ChiTietPhim> call, Throwable t) {
                Toast.makeText(ChiTietActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void loadTieuDe(Movie phim) {
        binding.textViewTitle.setText(phim.getName());
        binding.textViewYear.setText("Năm sản xuất: " + phim.getYear());
        binding.textViewActors.setText("Diễn viên: " + phim.getActor());
        binding.textViewDirector.setText("Đạo diễn: " + phim.getDirector());
        binding.textViewDescription.setText("Nội dung phim: " + phim.getContent());
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
