package com.example.xemphim.API;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
    private static Retrofit retrofit;
    private static String baseUrl = "https://phimapi.com/";

    // Phương thức lấy URL từ Firebase
    public static void fetchBaseUrlFromFirebase(final OnBaseUrlFetchListener listener) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("api_sources").child("selected_source");

        // Lấy URL từ 'selected_source'
        databaseReference.child("url").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String newUrl = snapshot.getValue(String.class);
                if (newUrl != null && !newUrl.isEmpty()) {
                    // Đảm bảo URL kết thúc bằng dấu '/'
                    if (!newUrl.endsWith("/")) {
                        newUrl += "/"; // Thêm dấu '/' nếu chưa có
                    }
                    setBaseUrl(newUrl);
                    listener.onBaseUrlFetched(newUrl); // Gọi callback
                } else {
                    listener.onError("URL không tồn tại");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                listener.onError(error.getMessage());
            }
        });
    }



    // Phương thức thay đổi baseUrl
    public static void setBaseUrl(String newBaseUrl) {
        baseUrl = newBaseUrl;
        retrofit = null; // Đặt lại retrofit để sử dụng baseUrl mới
    }

    public static Retrofit getClient() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

    // Listener cho việc lấy base URL
    public interface OnBaseUrlFetchListener {
        void onBaseUrlFetched(String url);
        void onError(String errorMessage);
    }
}



