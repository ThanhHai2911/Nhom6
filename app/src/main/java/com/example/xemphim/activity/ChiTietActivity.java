package com.example.xemphim.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.example.xemphim.API.ApiClient;
import com.example.xemphim.API.ApiService;
import com.example.xemphim.adapter.SeriesAdapter;
import com.example.xemphim.adapter.TapPhimAdapter;
import com.example.xemphim.databinding.ActivityChitietphimBinding;
import com.example.xemphim.model.Movie;
import com.example.xemphim.model.MovieDetail;
import com.example.xemphim.model.Series;
import com.example.xemphim.model.ThongBaoTrenManHinh;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;


import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import android.text.TextUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ChiTietActivity extends AppCompatActivity {
    private String movieSlug;
    private List<MovieDetail.Episode.ServerData> serverDataList = new ArrayList<>();
    private ActivityChitietphimBinding binding;
    private ApiService apiService;
    private TapPhimAdapter tapPhimAdapter;
    private String movieLink;
    private MovieDetail.MovieItem movieDetails;
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private FirebaseAuth mAuth;
    private String idUser;
    private  String nameUser;
    private String emailUser;
    private int idLoaiND;
    private DatabaseReference lichSuXemRef;
    private DatabaseReference ratingsRef;
    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChitietphimBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Intent serviceIntent = new Intent(this, ThongBaoTrenManHinh.class);
        startService(serviceIntent);
        setEvent();
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

                String episodeCurrent = serverDataList.get(0).getName();
                // Lưu lịch sử xem phim
                luuLichSuXem(movieSlug,episodeCurrent,serverDataList);
                // Khởi động activity phát video
                Intent intent = new Intent(this, XemPhimActivity.class);
                intent.putExtra("movie_link", movieLink);  // Truyền link phim
                intent.putExtra("slug", movieSlug);
                intent.putExtra("episodeCurrent", episodeCurrent);
                startActivity(intent);
            } else {
                Toast.makeText(ChiTietActivity.this, "Link phim không khả dụng", Toast.LENGTH_SHORT).show();
            }
        });
        swipeRefreshLayout = binding.swipeRefreshLayout; // Khởi tạo SwipeRefreshLayout
        swipeRefreshLayout.setOnRefreshListener(() -> {
            loadMovieDetails(movieSlug);
            calculateAverageRating(movieSlug);
        });
        lichSuXemRef = FirebaseDatabase.getInstance().getReference("LichSuXem");
        laythongtinUser();
        ratingsRef = FirebaseDatabase.getInstance().getReference("Ratings");
        // Tính và hiển thị trung bình sao và số lượt đánh giá
        calculateAverageRating(movieSlug);
    }

    public void luuLichSuXem(String movieSlug, String episodeName, List<MovieDetail.Episode.ServerData> serverDataList) {
        if (movieSlug == null) {
            Toast.makeText(this, "Không thể lưu lịch sử, thiếu thông tin", Toast.LENGTH_SHORT).show();
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

                    } else {
                        addNewMovieHistory(movieSlug, episodeName);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(ChiTietActivity.this, "Lỗi khi kiểm tra lịch sử", Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(this, "Lưu lịch sử xem phim thành công cho người dùng không đăng nhập", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Lưu lịch sử xem phim thất bại cho người dùng không đăng nhập", Toast.LENGTH_SHORT).show();
                    }
                });
    }


    // Hàm để thêm mới lịch sử xem phim nếu slug chưa tồn tại
    private void addNewMovieHistory(String movieSlug,String episode) {
        // Tạo ID mới cho lịch sử xem phim
        String idLichSuXem = lichSuXemRef.push().getKey();

        // Tạo thời gian xem phim (watched_at)
        String watchedAt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

        // Tạo bản ghi lịch sử xem phim
        Map<String, Object> lichSuXem = new HashMap<>();
        lichSuXem.put("id_user", idUser);
        lichSuXem.put("slug", movieSlug); // slug của phim
        lichSuXem.put("watched_at", watchedAt);
        lichSuXem.put("movie_link", movieLink);
        lichSuXem.put("episode", episode);

        // Lưu vào Firebase dưới node `LichSuXem`
        lichSuXemRef.child(idLichSuXem).setValue(lichSuXem)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(ChiTietActivity.this, "Lưu lịch sử xem phim thành công", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(ChiTietActivity.this, "Lưu lịch sử xem phim thất bại", Toast.LENGTH_SHORT).show();
                    }
                });
    }


    // Helper method to create the movie data map
    private Map<String, Object> createMovieDataMap(String movieSlug, String movieLink, String episodeCurrent, String userId) {
        Map<String, Object> movieData = new HashMap<>();
        movieData.put("slug", movieSlug);
        movieData.put("link", movieLink);
        movieData.put("episode", episodeCurrent);
        movieData.put("userId", userId); // Save the user ID
        return movieData;
    }

    private void laythongtinUser(){
        SharedPreferences sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
        idUser = sharedPreferences.getString("id_user", null);
        nameUser = sharedPreferences.getString("name", null);
        emailUser  = sharedPreferences.getString("email", null);
        idLoaiND = sharedPreferences.getInt("id_loaiND", 0);

    }

    private void loadMovieDetails(String slug) {
        binding.progressBar.setVisibility(View.VISIBLE);
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
                    // Giới hạn số ký tự cho description
                    final int MAX_CHAR_COUNT = 200;
                    String fullDescription = movie.getContent();
                    if (fullDescription.length() > MAX_CHAR_COUNT) {
                        String shortDescription = fullDescription.substring(0, MAX_CHAR_COUNT) + "...";
                        binding.textViewDescription.setText(shortDescription);
                        binding.buttonReadMore.setVisibility(View.VISIBLE);
                    } else {
                        binding.textViewDescription.setText(fullDescription);
                        binding.buttonReadMore.setVisibility(View.GONE);
                    }

                    // Sự kiện nhấn nút "Xem thêm" để hiển thị toàn bộ phần mô tả
                    binding.buttonReadMore.setOnClickListener(new View.OnClickListener() {
                        boolean isExpanded = false; // Biến trạng thái để kiểm tra xem có đang mở rộng hay không

                        @Override
                        public void onClick(View v) {
                            if (!isExpanded) {
                                binding.textViewDescription.setText(fullDescription);
                                binding.buttonReadMore.setText("Thu gọn");
                                isExpanded = true;
                            } else {
                                String shortDescription = fullDescription.substring(0, MAX_CHAR_COUNT) + "...";
                                binding.textViewDescription.setText(shortDescription);
                                binding.buttonReadMore.setText("Xem thêm");
                                isExpanded = false;
                            }
                        }
                    });
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

                        // Lấy link của tập đầu tiên
                        MovieDetail.Episode.ServerData firstServerData = serverDataList.get(0);
                        if (firstServerData != null) {
                            movieLink = firstServerData.getLinkM3u8();
                            Log.d("MovieDetailActivity", "Link phim tập 1: " + movieLink);
                        }

                        tapPhimAdapter.notifyDataSetChanged();
                        swipeRefreshLayout.setRefreshing(false); // Ngừng loading
                        binding.progressBar.setVisibility(View.GONE);
                        binding.scvChitiet.setVisibility(View.VISIBLE);
                        binding.imageViewthumburl.requestLayout();
                    } else {
                        Toast.makeText(ChiTietActivity.this, "Không có tập phim nào", Toast.LENGTH_SHORT).show();
                        swipeRefreshLayout.setRefreshing(false); // Ngừng loading
                    }


                } else {
                    Toast.makeText(ChiTietActivity.this, "Failed to load movie details", Toast.LENGTH_SHORT).show();
                    swipeRefreshLayout.setRefreshing(false); // Ngừng loading
                }
            }

            @Override
            public void onFailure(Call<MovieDetail> call, Throwable t) {
                Toast.makeText(ChiTietActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                binding.progressBar.setVisibility(View.GONE);
                swipeRefreshLayout.setRefreshing(false); // Ngừng loading
            }
        });
    }
    public void calculateAverageRating(String movieSlug) {
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
                    binding.ratingBar.setRating(averageRating);
                    binding.ratingBar.setIsIndicator(true);
                } else {
                    binding.tvAverageRating.setText("( 0 điểm / 0 lượt )");
                    binding.ratingBar.setRating(0); // Reset ratingBar nếu không có đánh giá
                    binding.ratingBar.setIsIndicator(true);
                }
                swipeRefreshLayout.setRefreshing(false); // Ngừng loading
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Xử lý lỗi
                swipeRefreshLayout.setRefreshing(false); // Ngừng loading
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
