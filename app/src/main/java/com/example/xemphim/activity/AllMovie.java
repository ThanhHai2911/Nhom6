package com.example.xemphim.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.xemphim.API.ApiClient;
import com.example.xemphim.API.ApiService;
import com.example.xemphim.R;
import com.example.xemphim.adapter.MovieAdapter;
import com.example.xemphim.adapter.SeriesAdapter;
import com.example.xemphim.databinding.ActivityAllMovieBinding;
import com.example.xemphim.databinding.ActivityLichSuXemBinding;
import com.example.xemphim.model.Movie;
import com.example.xemphim.model.Series;
import com.example.xemphim.response.MovieResponse;
import com.example.xemphim.response.SeriesResponse;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AllMovie extends AppCompatActivity {

    private ActivityAllMovieBinding binding;
    private SeriesAdapter seriesAdapter;
    private List<Series> seriesList;
    private boolean isLoading = false; // Trạng thái đang tải phim
    private int currentPage = 1; // Trang hiện tại
    private String type;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAllMovieBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        type = getIntent().getStringExtra("type");
        seriesList = new ArrayList<>(); // Khởi tạo danh sách series
        seriesAdapter = new SeriesAdapter(this, seriesList); // Khởi tạo adapter với danh sách rỗng
        binding.recyclerViewMovies.setLayoutManager(new GridLayoutManager(this, 3));
        binding.recyclerViewMovies.setAdapter(seriesAdapter); // Thiết lập adapter cho RecyclerView

        // Nhận danh sách và loại phim từ Intent
        List<Series> receivedSeriesList = getIntent().getParcelableArrayListExtra("seriesList");
        List<Series> phimLeList = getIntent().getParcelableArrayListExtra("phimLe");
        List<Series> tvShowList = getIntent().getParcelableArrayListExtra("tVshow");
        List<Series> phimHoatHinhList = getIntent().getParcelableArrayListExtra("hoatHinh");

        // Cập nhật dữ liệu dựa trên loại phim
        if ("series".equals(type) && receivedSeriesList != null) {
            seriesList.addAll(receivedSeriesList);
            seriesAdapter.notifyDataSetChanged(); // Cập nhật cho phim bộ
            setTitle("Danh Sách Phim Bộ");
        } else if ("movie".equals(type) && phimLeList != null) {
            seriesList.addAll(phimLeList);
            seriesAdapter.notifyDataSetChanged(); // Cập nhật cho phim lẻ
            setTitle("Danh Sách Phim Lẻ");
        } else if ("hoathinh".equals(type) && phimHoatHinhList != null) {
            seriesList.addAll(phimHoatHinhList);
            seriesAdapter.notifyDataSetChanged(); // Cập nhật cho phim hoạt hình
            setTitle("Danh Sách Phim Hoạt Hình");
        } else if (tvShowList != null) { // Chỉ cần kiểm tra tvShowList
            seriesList.addAll(tvShowList);
            seriesAdapter.notifyDataSetChanged(); // Cập nhật cho chương trình truyền hình
            setTitle("Danh Sách TV Show");
        } else {
            Log.e("AllMovieActivity", "All lists are null!");
        }
        // Thêm listener cuộn
        binding.recyclerViewMovies.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                GridLayoutManager layoutManager = (GridLayoutManager) recyclerView.getLayoutManager();

                if (layoutManager != null && !isLoading) {
                    int visibleItemCount = layoutManager.getChildCount();
                    int totalItemCount = layoutManager.getItemCount();
                    int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();

                    if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount
                            && firstVisibleItemPosition >= 0) {
                        // Tải trang tiếp theo
                        currentPage++;
                        loadMoreMovies(currentPage, type); // Truyền cả currentPage và type
                    }
                }
            }
        });
    }

    private void loadMoreMovies(int page, String type) {
        isLoading = true; // Đặt trạng thái tải
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        Call<SeriesResponse> call;

        switch (type) {
            case "movie":
                call = apiService.getPhimLe(page); // Gọi API cho phim lẻ
                break;
            case "series":
                call = apiService.getSeries(page); // Gọi API cho phim bộ
                break;
            case "hoathinh":
                call = apiService.getHoatHinh(page); // Gọi API cho phim hoạt hình
                break;
            case "tvShow":
                call = apiService.getTVShow(page); // Gọi API cho chương trình truyền hình
                break;
            default:
                isLoading = false; // Đặt trạng thái tải về false nếu loại không hợp lệ
                return;
        }

        call.enqueue(new Callback<SeriesResponse>() {
            @Override
            public void onResponse(Call<SeriesResponse> call, Response<SeriesResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Series> newMovies = response.body().getData().getItems(); // Điều chỉnh theo cấu trúc API của bạn
                    if (newMovies != null) {
                        seriesList.addAll(newMovies); // Thêm phim mới vào danh sách
                        seriesAdapter.notifyDataSetChanged(); // Thông báo adapter đã có dữ liệu mới
                    }
                }
                isLoading = false; // Đặt trạng thái tải về false
            }

            @Override
            public void onFailure(Call<SeriesResponse> call, Throwable t) {
                // Xử lý lỗi
                Log.e("AllMovie", "Error loading movies: " + t.getMessage());
                isLoading = false; // Đặt trạng thái tải về false
            }
        });
    }
}






