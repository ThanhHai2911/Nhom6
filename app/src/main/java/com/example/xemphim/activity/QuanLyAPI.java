package com.example.xemphim.activity;

import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.xemphim.API.ApiClient;
import com.example.xemphim.API.ApiService;
import com.example.xemphim.R;
import com.example.xemphim.adapter.ApiAdapter;
import com.example.xemphim.adapter.SeriesAdapter;
import com.example.xemphim.model.ApiModel;
import com.example.xemphim.model.Series;
import com.example.xemphim.model.Series2;
import com.example.xemphim.response.SeriesResponse;
import com.example.xemphim.response.SeriesResponse2;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class QuanLyAPI extends AppCompatActivity {

    private RecyclerView rvApiList;
    private ApiAdapter apiAdapter;
    private List<ApiModel> apiList;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quan_ly_api);

        rvApiList = findViewById(R.id.rvApiList);
        rvApiList.setLayoutManager(new LinearLayoutManager(this));

        apiList = new ArrayList<>();
        databaseReference = FirebaseDatabase.getInstance().getReference("api_sources");

        // Lấy danh sách API từ Firebase
        fetchApiSources();

        apiAdapter = new ApiAdapter(apiList, new ApiAdapter.OnApiActionListener() {
            @Override
            public void onEditClick(ApiModel api) {
                editApi(api);
            }

            @Override
            public void onDeleteClick(ApiModel api) {
                deleteApi(api);
            }

            @Override
            public void onSelectClick(ApiModel api) {
                saveSelectedApi(api.getUrl()); // Lưu URL đã chọn
                // Chờ một chút để đảm bảo URL đã được lưu
                saveSelectedApi(api.getUrl());
                ApiClient.fetchBaseUrlFromFirebase(new ApiClient.OnBaseUrlFetchListener() {
                    @Override
                    public void onBaseUrlFetched(String url) {
                        // Đã lấy được URL mới, có thể thực hiện các yêu cầu API mới tại đây
                        Retrofit retrofit = ApiClient.getClient();
                        // Thực hiện các yêu cầu API nếu cần
                    }

                    @Override
                    public void onError(String errorMessage) {
                        Toast.makeText(QuanLyAPI.this, "Lỗi lấy URL: " + errorMessage, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        rvApiList.setAdapter(apiAdapter);
    }


    // Lấy danh sách API từ Firebase
    private void fetchApiSources() {
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                apiList.clear();
                for (DataSnapshot apiSnapshot : snapshot.getChildren()) {
                    String name = apiSnapshot.child("name").getValue(String.class);
                    String url = apiSnapshot.child("url").getValue(String.class);
                    apiList.add(new ApiModel(name, url));
                }
                apiAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(QuanLyAPI.this, "Lỗi tải API: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    // Sửa API
    private void editApi(ApiModel api) {
        // Mở dialog để người dùng chỉnh sửa tên và URL API
    }

    // Xóa API khỏi Firebase
    private void deleteApi(ApiModel api) {
        databaseReference.child(api.getName()).removeValue()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(QuanLyAPI.this, "API đã được xóa!", Toast.LENGTH_SHORT).show();
                    fetchApiSources(); // Refresh danh sách sau khi xóa
                })
                .addOnFailureListener(e -> Toast.makeText(QuanLyAPI.this, "Lỗi khi xóa API: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    // Lưu API đã chọn lên Firebase
    private void saveSelectedApi(String selectedApiUrl) {
        databaseReference.child("selected_source").child("url").setValue(selectedApiUrl)
                .addOnSuccessListener(aVoid -> Toast.makeText(QuanLyAPI.this, "Đã lưu API thành công!", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(QuanLyAPI.this, "Lỗi khi lưu API: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}

