package com.example.xemphim.activity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.util.Log;
import android.util.TypedValue;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.OptIn;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.media3.common.MediaItem;
import androidx.media3.common.PlaybackException;
import androidx.media3.common.Player;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.datasource.DefaultHttpDataSource;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.exoplayer.hls.HlsMediaSource;
import androidx.media3.ui.AspectRatioFrameLayout;
import androidx.media3.ui.PlayerView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.xemphim.API.ApiClient;
import com.example.xemphim.API.ApiService;
import com.example.xemphim.R;
import com.example.xemphim.adapter.BinhLuanPhimAdapter;
import com.example.xemphim.adapter.TapPhimAdapter;
import com.example.xemphim.databinding.ActivityXemphimBinding; // Import View Binding
import com.example.xemphim.model.BinhLuanPhim;
import com.example.xemphim.model.FavoriteMovie;
import com.example.xemphim.model.MovieDetail;

import com.example.xemphim.model.MovieDownloader;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class XemPhimActivity extends AppCompatActivity implements BinhLuanPhimAdapter.OnCommentDeleteListener{
    private ActivityXemphimBinding binding; // Khai báo View Binding
    private ExoPlayer exoPlayer; // ExoPlayer để phát video
    private boolean isFullScreen = false;
    private TapPhimAdapter tapPhimAdapter;
    private List<MovieDetail.Episode.ServerData> serverDataList = new ArrayList<>();
    private String movieLink;
    private ApiService apiService;
    private String movieSlug;
    private DatabaseReference favoritesRef;
    private String idUser;
    private String nameUser;
    private String emailUser;
    private int idLoaiND;
    private MovieDownloader movieDownloader;
    private DatabaseReference lichSuXemRef;
    private DatabaseReference usersRef;
    private BinhLuanPhimAdapter binhLuanPhimAdapter;
    private List<BinhLuanPhim> binhLuanPhimList = new ArrayList<>();
    private DatabaseReference ratingsRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityXemphimBinding.inflate(getLayoutInflater()); // Khởi tạo View Binding
        setContentView(binding.getRoot()); // Đặt layout cho Activity
        apiService = ApiClient.getClient().create(ApiService.class);
        movieDownloader = new MovieDownloader(apiService, this);
        setControl();
        setEvent();
    }

    public void setControl() {
        binding.rcvTapPhim.setLayoutManager(new GridLayoutManager(this, 2, RecyclerView.HORIZONTAL, false)); // Thiết lập RecyclerView
        // Khởi tạo Firebase Database
        favoritesRef = FirebaseDatabase.getInstance().getReference("favorites"); // Thay "favorites" bằng tên bảng của bạn
        binhLuanPhimAdapter = new BinhLuanPhimAdapter(this, binhLuanPhimList, this);
        binding.rvComments.setLayoutManager(new GridLayoutManager(this, 1));
        binding.rvComments.setAdapter(binhLuanPhimAdapter);
    }

    public void setEvent() {
        initializePlayer();
        // Thiết lập sự kiện cho nút toàn màn hình
        movieSlug = getIntent().getStringExtra("slug");

        binding.btnFullScreen.setOnClickListener(v -> toggleFullScreen());
        apiService = ApiClient.getClient().create(ApiService.class);
        loadMovieDetails();
        lichSuXemRef = FirebaseDatabase.getInstance().getReference("LichSuXem");
        usersRef = FirebaseDatabase.getInstance().getReference("Users");
        laythongtinUser();
        binding.btnDowload.setOnClickListener(v -> {
            String movieName = binding.tvMovieTitle.getText().toString();
            if (movieLink != null && !movieLink.isEmpty()) {
                // Hiện thông báo "Đang tải..."
                Toast.makeText(XemPhimActivity.this, "Đang tải...", Toast.LENGTH_SHORT).show();
                // Gọi phương thức download từ movieDownloader
                movieDownloader.loadPosterAndDownloadMovie(movieSlug, movieLink, movieName);
            } else {
                Toast.makeText(XemPhimActivity.this, "Liên kết phim không hợp lệ!", Toast.LENGTH_SHORT).show();
            }
        });
        // Kiểm tra và cập nhật màu nút trái tim
        checkAndToggleFavorite();
        // Thêm sự kiện nhấn cho nút thêm vào danh sách yêu thích
        binding.btnAddToFavorites.setOnClickListener(v -> addToFavorites());
        // Thêm sự kiện nhấn cho nút bình luận
        binding.btnSubmitComment.setOnClickListener(v -> {
            String comment = binding.commentInput.getText().toString();
            addCommentToMovie(comment);
        });
        // Gọi hàm này sau khi người dùng nhấn vào phim hoặc sau khi thêm bình luận
        loadCommentsForMovie(this.movieSlug);

        ratingsRef = FirebaseDatabase.getInstance().getReference("Ratings");
        // Xử lý khi người dùng đánh giá phim
        binding.ratingBar.setOnRatingBarChangeListener((ratingBar, rating, fromUser) -> {
            if (fromUser) {
                saveRating(movieSlug, idUser, rating);  // Lưu đánh giá
            }
        });

        // Tính và hiển thị trung bình sao và số lượt đánh giá
        calculateAverageRating(movieSlug);
        // Gọi hàm này sau khi người dùng đánh giá phim
        kiemTraDanhGia();
    }
    public void saveRating(String movieSlug, String userId, float rating) {
        // Tạo một bản ghi cho đánh giá của người dùng
        Map<String, Object> ratingData = new HashMap<>();
        ratingData.put("userId", userId);
        ratingData.put("rating", rating);
        ratingData.put("ratedAt", System.currentTimeMillis());

        // Lưu dữ liệu vào Firebase
        ratingsRef.child(movieSlug).child(userId).setValue(ratingData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(XemPhimActivity.this, "Đánh giá đã được lưu!", Toast.LENGTH_SHORT).show();
                    // Gọi hàm tính toán điểm trung bình sau khi lưu thành công
                    calculateAverageRating(movieSlug);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(XemPhimActivity.this, "Lỗi khi lưu đánh giá: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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
                    binding.ratingBar.setRating(averageRating); // Cập nhật ratingBar
                } else {
                    binding.tvAverageRating.setText("Không có đánh giá");
                    binding.ratingBar.setRating(0); // Reset ratingBar nếu không có đánh giá
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Xử lý lỗi
            }
        });
    }


    private void kiemTraDanhGia(){
        // Lấy thông tin cần thiết
        String userId = idUser; // ID của người dùng hiện tại
        String movieSlug = this.movieSlug; // Slug của phim
        // Kiểm tra nếu người dùng đã đánh giá phim hay chưa
        DatabaseReference ratingRef = FirebaseDatabase.getInstance().getReference("Ratings")
                .child(movieSlug)  // slug của phim
                .child(userId);  // ID người dùng

        ratingRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Người dùng đã đánh giá, lấy rating
                    int userRating = dataSnapshot.child("rating").getValue(Integer.class);
                    // Highlight sao đã đánh giá
                    binding.ratingBar.setRating(userRating);
                } else {
                    // Người dùng chưa đánh giá
                    binding.ratingBar.setRating(0);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Xử lý lỗi
            }
        });
    }




    private void addCommentToMovie(String comment) {
        // Lấy thông tin cần thiết
        String userId = idUser; // ID của người dùng hiện tại
        String movieSlug = this.movieSlug; // Slug của phim

        // Lấy tên người dùng
        usersRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot userSnapshot) {
                String name = nameUser; // Tên người dùng
                if (name == null) {
                    name = "Người dùng ẩn danh"; // Hoặc một tên mặc định
                }

                // Định dạng ngày giờ
                String formattedDate = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(new Date());

                // Tạo một đối tượng Comment
                BinhLuanPhim newComment = new BinhLuanPhim(userId, movieSlug, comment, System.currentTimeMillis(), name, formattedDate);

                // Tham chiếu đến bảng Comments
                DatabaseReference commentsRef = FirebaseDatabase.getInstance().getReference("Comments");

                // Lưu bình luận vào Firebase
                commentsRef.push().setValue(newComment)
                        .addOnSuccessListener(aVoid -> {
                            // Thêm bình luận vào adapter và cập nhật UI ngay lập tức
                            binhLuanPhimAdapter.addComment(newComment); // Giả sử bạn có phương thức này trong adapter
                            binhLuanPhimAdapter.notifyDataSetChanged(); // Cập nhật RecyclerView

                            Toast.makeText(XemPhimActivity.this, "Bình luận đã được lưu!", Toast.LENGTH_SHORT).show();
                            // Xóa nội dung bình luận trong EditText nếu cần
                            binding.commentInput.setText("");
                        })
                        .addOnFailureListener(e -> Toast.makeText(XemPhimActivity.this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(XemPhimActivity.this, "Lỗi khi lấy thông tin người dùng", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadCommentsForMovie(String movieSlug) {
        DatabaseReference commentsRef = FirebaseDatabase.getInstance().getReference("Comments");

        commentsRef.orderByChild("slug").equalTo(movieSlug).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d("LoadComments", "Number of comments: " + dataSnapshot.getChildrenCount());
                if (dataSnapshot.exists()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        String userId = snapshot.child("userId").getValue(String.class);
                        String commentText = snapshot.child("commentText").getValue(String.class);
                        String userName = snapshot.child("userName").getValue(String.class); // Lấy tên người dùng từ bình luận
                        long timestamp = snapshot.child("timestamp").getValue(Long.class);


                        Log.d("LoadComments", "Current comment UserId: " + userId); // Log userId
                        Log.d("LoadComments", "Current comment Text: " + commentText); // Log commentText

                        if (userId != null && commentText != null) {
                            // Lấy tên người dùng từ bảng users
                            usersRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot userSnapshot) {

                                    // Định dạng ngày giờ
                                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
                                    String formattedDate = sdf.format(new Date(timestamp));

                                    // Tạo bình luận và thêm vào adapter
                                    BinhLuanPhim comment = new BinhLuanPhim(userId, movieSlug, commentText, timestamp, userName, formattedDate);
                                    binhLuanPhimAdapter.addComment(comment);
                                    binhLuanPhimAdapter.notifyDataSetChanged(); // Cập nhật RecyclerView
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                    Toast.makeText(XemPhimActivity.this, "Lỗi khi lấy thông tin người dùng", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                } else {
                    Log.d("LoadComments", "No comments found for this movie.");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(XemPhimActivity.this, "Lỗi khi tải bình luận", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onCommentDelete(int position) {
        // Lấy thông tin người dùng và phim
        String userId = binhLuanPhimAdapter.getCommentUserId(position);
        String movieSlug = this.movieSlug; // Slug của phim

        // Kiểm tra người dùng có quyền xóa bình luận
        if (userId.equals(idUser)) {
            // Tạo hộp thoại xác nhận
            new AlertDialog.Builder(XemPhimActivity.this)
                    .setTitle("Xóa bình luận")
                    .setMessage("Bạn có chắc chắn muốn xóa bình luận này không?")
                    .setPositiveButton("Có", (dialog, which) -> {
                        DatabaseReference commentsRef = FirebaseDatabase.getInstance().getReference("Comments");

                        // Xác định bình luận cần xóa
                        commentsRef.orderByChild("slug").equalTo(movieSlug).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                    String commentUserId = snapshot.child("userId").getValue(String.class);
                                    if (commentUserId != null && commentUserId.equals(userId)) {
                                        // Xóa bình luận khỏi Firebase
                                        snapshot.getRef().removeValue()
                                                .addOnSuccessListener(aVoid -> {
                                                    // Xóa bình luận khỏi adapter
                                                    binhLuanPhimAdapter.removeComment(position);
                                                    Toast.makeText(XemPhimActivity.this, "Bình luận đã được xóa!", Toast.LENGTH_SHORT).show();
                                                })
                                                .addOnFailureListener(e -> {
                                                    Toast.makeText(XemPhimActivity.this, "Lỗi khi xóa bình luận: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                });
                                        break; // Đã xóa bình luận, không cần tiếp tục tìm kiếm
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                Toast.makeText(XemPhimActivity.this, "Lỗi khi kiểm tra bình luận", Toast.LENGTH_SHORT).show();
                            }
                        });
                    })
                    .setNegativeButton("Không", (dialog, which) -> {
                        dialog.dismiss(); // Đóng hộp thoại nếu người dùng không muốn xóa
                    })
                    .show(); // Hiển thị hộp thoại
        } else {
            Toast.makeText(XemPhimActivity.this, "Bạn không có quyền xóa bình luận này!", Toast.LENGTH_SHORT).show();
        }
    }


    private void addToFavorites() {
        // Lấy thông tin cần thiết
        String userId = idUser; // ID của người dùng hiện tại
        String movieSlug = this.movieSlug; // Slug của phim

        // Tham chiếu đến bảng Favorite
        DatabaseReference favoritesRef = FirebaseDatabase.getInstance().getReference("Favorite");

        // Tham chiếu đến vị trí phim yêu thích
        favoritesRef.orderByChild("slug").equalTo(movieSlug).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                boolean movieExists = false;
                String movieIdToDelete = ""; // Để lưu id_phim khi cần xóa

                // Kiểm tra từng entry trong danh sách yêu thích
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    FavoriteMovie favoriteMovie = snapshot.getValue(FavoriteMovie.class);
                    if (favoriteMovie != null && favoriteMovie.getId_user().equals(userId)) {
                        movieExists = true;
                        movieIdToDelete = snapshot.getKey(); // Lưu id_phim để xóa
                        break;
                    }
                }
                if (movieExists) {
                    // Nếu phim đã tồn tại, xóa nó
                    favoritesRef.child(movieIdToDelete).removeValue()
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(XemPhimActivity.this, "Đã xóa khỏi danh sách yêu thích!", Toast.LENGTH_SHORT).show();
                                // Đổi màu trái tim về trắng
                                binding.btnAddToFavorites.setImageResource(R.drawable.baseline_favorite_24);
                            })
                            .addOnFailureListener(e -> Toast.makeText(XemPhimActivity.this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                } else {
                    // Nếu phim chưa tồn tại, thêm nó
                    FavoriteMovie favoriteMovie = new FavoriteMovie(userId, movieSlug);
                    favoritesRef.push().setValue(favoriteMovie)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(XemPhimActivity.this, "Đã thêm vào yêu thích!", Toast.LENGTH_SHORT).show();
                                // Đổi màu trái tim thành đỏ
                                binding.btnAddToFavorites.setImageResource(R.drawable.baseline_favorite_24_red);
                            })
                            .addOnFailureListener(e -> Toast.makeText(XemPhimActivity.this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(XemPhimActivity.this, "Lỗi: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkAndToggleFavorite() {
        // Lấy thông tin cần thiết
        String userId = idUser; // ID của người dùng hiện tại
        String movieSlug = this.movieSlug; // Slug của phim
        DatabaseReference favoritesRef = FirebaseDatabase.getInstance().getReference("Favorite");
        // Kiểm tra nếu phim đã có trong danh sách yêu thích
        favoritesRef.orderByChild("slug").equalTo(movieSlug).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                boolean movieExists = false;

                // Kiểm tra từng entry trong danh sách yêu thích
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    FavoriteMovie favoriteMovie = snapshot.getValue(FavoriteMovie.class);
                    if (favoriteMovie != null && favoriteMovie.getId_user() != null && userId != null && favoriteMovie.getId_user().equals(userId)) {
                        movieExists = true;
                        break;
                    }
                }
                // Nếu phim đã tồn tại, đổi màu nút thành đỏ
                if (movieExists) {
                    binding.btnAddToFavorites.setImageResource(R.drawable.baseline_favorite_24_red);
                } else {
                    // Nếu phim chưa tồn tại, để màu trắng
                    binding.btnAddToFavorites.setImageResource(R.drawable.baseline_favorite_24);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(XemPhimActivity.this, "Lỗi: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initializePlayer() {
        movieLink = getIntent().getStringExtra("movie_link"); // Đã sửa để lấy lại movieLink
        if (exoPlayer == null) {
            exoPlayer = new ExoPlayer.Builder(this).build();
            PlayerView playerView = binding.playerView; // Đảm bảo đây là PlayerView từ Media3
            playerView.setPlayer(exoPlayer); // Thiết lập player cho PlayerView
            playerView.setKeepScreenOn(true);

            exoPlayer.addListener(new Player.Listener() {
                @Override
                public void onPlayerError(PlaybackException error) {
                    Toast.makeText(XemPhimActivity.this, "Phim lỗi vui lòng báo cáo cho admin hoặc xem phim khác: " + error.getMessage(), Toast.LENGTH_LONG).show();
                }
            });

            // Tạo MediaItem từ đường dẫn video
            MediaItem mediaItem = MediaItem.fromUri(movieLink);
            exoPlayer.setMediaItem(mediaItem);
            exoPlayer.prepare();
        }
    }

    private void playEpisode(String episodeLink) {
        if (exoPlayer != null) {
            MediaItem mediaItem = MediaItem.fromUri(episodeLink);
            exoPlayer.setMediaItem(mediaItem);
            exoPlayer.prepare();
            exoPlayer.play();
        }
    }

    private void toggleFullScreen() {
        if (isFullScreen) {
            // Quay về chế độ portrait
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

            // Hiện thanh trạng thái và thanh điều hướng
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);

            // Thiết lập chiều cao của PlayerView về 250dp
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) binding.playerView.getLayoutParams();
            params.height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 250, getResources().getDisplayMetrics());
            binding.playerView.setLayoutParams(params);
        } else {
            // Chuyển sang chế độ landscape
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

            // Ẩn thanh trạng thái và thanh điều hướng
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

            // Thiết lập chiều cao của PlayerView để chiếm toàn bộ chiều cao màn hình
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) binding.playerView.getLayoutParams();
            params.height = ViewGroup.LayoutParams.MATCH_PARENT;
            params.width = ViewGroup.LayoutParams.MATCH_PARENT;
            binding.playerView.setLayoutParams(params);
        }

        isFullScreen = !isFullScreen; // Đổi trạng thái fullscreen
    }



    private void loadMovieDetails() {
        Call<MovieDetail> call = apiService.getMovieDetail(movieSlug);
        call.enqueue(new Callback<MovieDetail>() {
            @Override
            public void onResponse(Call<MovieDetail> call, Response<MovieDetail> response) {
                if (response.isSuccessful() && response.body() != null) {
                    MovieDetail movieDetails = response.body();
                    List<MovieDetail.Episode> tapPhim = movieDetails.getEpisodes();
                    String movieTitle = movieDetails.getMovie().getName();
                    String episodeName = getIntent().getStringExtra("episodeCurrent");
                    binding.tvMovieTitle.setText(movieDetails.getMovie().getName());
                    binding.tvMovieTitle.setText(movieTitle + " - " + episodeName);
                    if (tapPhim != null && !tapPhim.isEmpty()) {
                        serverDataList.clear();
                        for (MovieDetail.Episode episode : tapPhim) {
                            List<MovieDetail.Episode.ServerData> data = episode.getServerData();
                            if (data != null) {
                                serverDataList.addAll(data);
                            }
                        }
                        tapPhimAdapter = new TapPhimAdapter(XemPhimActivity.this, serverDataList);
                        // Cập nhật RecyclerView với danh sách tập phim
                        tapPhimAdapter.setRecyclerViewItemClickListener(new TapPhimAdapter.OnRecyclerViewItemClickListener() {
                            @Override
                            public void onItemClick(View view, int position) {
                                MovieDetail.MovieItem movieItem = new MovieDetail.MovieItem();
                                MovieDetail.Episode.ServerData selectedEpisode = serverDataList.get(position);
                                String newMovieLink = selectedEpisode.getLinkM3u8();
                                //Hien thi ten tap phim  dang xem
                                String movieTitle = movieDetails.getMovie().getName();
                                String episodeName = selectedEpisode.getName();
                                binding.tvMovieTitle.setText(movieTitle + " - " + episodeName);
                                luuLichSuXem(movieSlug);
                                playEpisode(newMovieLink);
                            }
                        });

                        binding.rcvTapPhim.setAdapter(tapPhimAdapter);

                        // Play the first episode automatically
                        movieLink = serverDataList.get(0).getLinkM3u8();
                        initializePlayer(); // Initialize player with the first episode
                    } else {
                        Toast.makeText(XemPhimActivity.this, "Không có tập phim nào", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<MovieDetail> call, Throwable t) {
                Toast.makeText(XemPhimActivity.this, "Failed to load movie details", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void luuLichSuXem(String movieSlug) {
        if (idUser == null || movieSlug == null) {
            Toast.makeText(XemPhimActivity.this, "Không thể lưu lịch sử, thiếu thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        // Query để kiểm tra xem slug đã tồn tại cho user này chưa
        Query query = lichSuXemRef.orderByChild("id_user").equalTo(idUser);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String idLichSuXem = null;
                // Duyệt qua các bản ghi lịch sử xem phim của user
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String existingSlug = snapshot.child("slug").getValue(String.class);
                    if (existingSlug != null && existingSlug.equals(movieSlug)) {
                        idLichSuXem = snapshot.getKey(); // Lấy ID của bản ghi để cập nhật
                        break;
                    }
                }
                if (idLichSuXem != null) {
                    // Nếu slug tồn tại, cập nhật tập phim mới
                    // updateExistingMovieHistory(idLichSuXem, episodeName, movieLink);
                } else {
                    // Nếu slug chưa tồn tại, tiến hành thêm mới
                    addNewMovieHistory(movieSlug);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(XemPhimActivity.this, "Lỗi khi kiểm tra lịch sử", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Hàm để thêm mới lịch sử xem phim nếu slug chưa tồn tại
    private void addNewMovieHistory(String movieSlug) {
        // Tạo ID mới cho lịch sử xem phim
        String idLichSuXem = lichSuXemRef.push().getKey();

        // Tạo thời gian xem phim (watched_at)
        String watchedAt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

        // Tạo bản ghi lịch sử xem phim
        Map<String, Object> lichSuXem = new HashMap<>();
        lichSuXem.put("id_user", idUser);
        lichSuXem.put("slug", movieSlug); // slug của phim
        lichSuXem.put("watched_at", watchedAt);

        // Lưu vào Firebase dưới node `LichSuXem`
        lichSuXemRef.child(idLichSuXem).setValue(lichSuXem)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(XemPhimActivity.this, "Lưu lịch sử xem phim thành công", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(XemPhimActivity.this, "Lưu lịch sử xem phim thất bại", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void laythongtinUser() {
        SharedPreferences sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
        idUser = sharedPreferences.getString("id_user", null);
        nameUser = sharedPreferences.getString("name", null);
        emailUser = sharedPreferences.getString("email", null);
        idLoaiND = sharedPreferences.getInt("id_loaiND", 0);

    }
//    // Hàm để cập nhật lịch sử xem phim nếu slug tồn tại
//    private void updateExistingMovieHistory(String idLichSuXem, String episodeName, String movieLink) {
//        String watchedAt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
//
//        Map<String, Object> updates = new HashMap<>();
//        updates.put("episode_name", episodeName);
//        updates.put("movie_link", movieLink);
//        updates.put("watched_at", watchedAt);
//
//        lichSuXemRef.child(idLichSuXem).updateChildren(updates)
//                .addOnCompleteListener(task -> {
//                    if (task.isSuccessful()) {
//                        Toast.makeText(XemPhimActivity.this, "Cập nhật lịch sử xem thành công", Toast.LENGTH_SHORT).show();
//                    } else {
//                        Toast.makeText(XemPhimActivity.this, "Cập nhật lịch sử xem thất bại", Toast.LENGTH_SHORT).show();
//                    }
//                });
//    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (exoPlayer != null) {
            exoPlayer.release();  // Giải phóng ExoPlayer khi Activity bị hủy
            exoPlayer = null;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (exoPlayer != null) {
            exoPlayer.pause(); // Tạm dừng video khi Activity không còn hiển thị
        }
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Giữ màn hình sáng khi ứng dụng hoạt động
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

}
