package com.example.xemphim.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.xemphim.API.ApiClient;
import com.example.xemphim.API.ApiService;
import com.example.xemphim.adapter.SeriesAdapter;
import com.example.xemphim.adapter.TapPhimAdapter;
import com.example.xemphim.databinding.ActivityChitietphimBinding;
import com.example.xemphim.model.Movie;
import com.example.xemphim.model.MovieDetail;
import com.example.xemphim.model.Series;


import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

public class ChiTietActivity extends AppCompatActivity {
    private String movieSlug;
    private List<MovieDetail.Episode.ServerData> serverDataList = new ArrayList<>();
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
                intent.putExtra("slug", movieSlug);
                startActivity(intent);
            } else {
                Toast.makeText(ChiTietActivity.this, "Link phim không khả dụng", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void loadMovieDetails(String slug) {
        // Ensure apiService is initialized before calling this
        Call<MovieDetail> call = apiService.getMovieDetail(slug);
        call.enqueue(new Callback<MovieDetail>() {
            @Override
            public void onResponse(Call<MovieDetail> call, Response<MovieDetail> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Get movie details from the response
                    MovieDetail.MovieItem movie = response.body().getMovie();  // Assuming the response returns a Movie object

                    // Hiển thị thông tin phim using ViewBinding
                    binding.textViewTitle.setText(movie.getName());
                    binding.textViewDescription.setText(movie.getContent());
                    binding.textViewYear.setText(String.valueOf(movie.getYear()));
                    binding.textViewActors.setText(TextUtils.join(", ", movie.getActor()));
                    binding.textViewDirector.setText(TextUtils.join(", ", movie.getDirector()));
                    List<String> directores = movie.getDirector();
                    String directory = "";

                    // Kiểm tra nếu danh sách không null và không rỗng
                    if (directores != null && !directores.isEmpty()) {
                        // Sử dụng TextUtils để nối các chuỗi
                        directory = TextUtils.join(", ", directores);
                    }

                    // Gán giá trị vào TextView
                    binding.tvCountry.setText(directory);

                    List<MovieDetail.MovieItem.Country> countries = movie.getCountry();
                    if (countries != null && !countries.isEmpty()) {
                        // Duyệt qua danh sách thể loại và ghép tên của chúng thành chuỗi
                        List<String> countryNames = new ArrayList<>();
                        for (MovieDetail.MovieItem.Country country : countries) {
                            countryNames.add(country.getName());
                        }
                        // Chuyển danh sách tên thể loại thành chuỗi, ngăn cách bởi dấu phẩy
                        String countryText = TextUtils.join(", ", countryNames);
                        // Hiển thị chuỗi thể loại lên TextView
                        binding.tvCountry.setText(countryText);
                    }

                    List<MovieDetail.MovieItem.Category> categories = movie.getCategory();
                    if (categories != null && !categories.isEmpty()) {
                        // Duyệt qua danh sách thể loại và ghép tên của chúng thành chuỗi
                        List<String> categoryNames = new ArrayList<>();
                        for (MovieDetail.MovieItem.Category category : categories) {
                            categoryNames.add(category.getName());
                        }
                        // Chuyển danh sách tên thể loại thành chuỗi, ngăn cách bởi dấu phẩy
                        String categoryText = TextUtils.join(", ", categoryNames);
                        // Hiển thị chuỗi thể loại lên TextView
                        binding.categoryName.setText(categoryText);
                    }


                    // Tải poster bằng Glide (poster image and thumbnail)
                    Glide.with(ChiTietActivity.this)
                            .load(movie.getThumbUrl())
                            .into(binding.imageViewthumburl);  // Use correct binding ID

                    Glide.with(ChiTietActivity.this)
                            .load(movie.getPosterUrl())
                            .into(binding.imageViewPoster);  // Use correct binding ID

                    // Lấy danh sách các tập phim
                    List<MovieDetail.Episode> tapPhim = response.body().getEpisodes();
                    if (tapPhim != null && !tapPhim.isEmpty()) {
                        // Lưu danh sách các tập phim
                        serverDataList.clear(); // Xóa danh sách cũ
                        for (MovieDetail.Episode episode : tapPhim) {
                            List<MovieDetail.Episode.ServerData> data = episode.getServerData();
                            if (data != null) {
                                serverDataList.addAll(data); // Thêm tất cả các tập phim vào danh sách
                            }
                        }
                        tapPhimAdapter = new TapPhimAdapter(ChiTietActivity.this, serverDataList);
                        // Cập nhật RecyclerView với danh sách tập phim
                        tapPhimAdapter.setRecyclerViewItemClickListener(new TapPhimAdapter.OnRecyclerViewItemClickListener() {
                            @Override
                            public void onItemClick(View view, int position) {
                                //Lay thong tin chi tiet phim tu slug truyen den man hinh chi tiet phim
                                Intent intent = new Intent(view.getContext(), XemPhimActivity.class);
                                MovieDetail.Episode.ServerData tapphim = serverDataList.get(position);
                                intent.putExtra("movie_link", tapphim.getLinkM3u8());
                                intent.putExtra("slug", tapphim.getSlug());
                                view.getContext().startActivity(intent);
                            }
                        });

                        rcvTapPhim.setAdapter(tapPhimAdapter);

                        // Lấy link của tập đầu tiên
                        MovieDetail.Episode.ServerData firstServerData = serverDataList.get(0);
                        if (firstServerData != null) {
                            movieLink = firstServerData.getLinkM3u8();
                            Log.d("MovieDetailActivity", "Link phim tập 1: " + movieLink);
                        }

                        tapPhimAdapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(ChiTietActivity.this, "Không có tập phim nào", Toast.LENGTH_SHORT).show();
                    }


                } else {
                    Toast.makeText(ChiTietActivity.this, "Failed to load movie details", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<MovieDetail> call, Throwable t) {
                Toast.makeText(ChiTietActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
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
