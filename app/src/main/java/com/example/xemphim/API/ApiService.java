package com.example.xemphim.API;

import com.example.xemphim.model.MovieDetail;
import com.example.xemphim.response.MovieResponse;
import com.example.xemphim.response.SeriesResponse;
import com.example.xemphim.response.SeriesResponse2;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.Url;

public interface ApiService {
    @GET("danh-sach/phim-moi-cap-nhat")
    Call<MovieResponse> getMovies(@Query("page") int page);

    @GET("v1/api/tim-kiem")
    Call<SeriesResponse> searchMovies(@Query("keyword") String keyword, @Query("limit") int limit);


    @GET("v1/api/danh-sach/phim-bo")
    Call<SeriesResponse> getSeries(@Query("page") int page);

    @GET("phim/{slug}")
    Call<MovieDetail> getMovieDetail(@Path("slug") String slug);

    @GET("v1/api/danh-sach/tv-shows")
    Call<SeriesResponse> getTVShow(@Query("page") int page);

    @GET("v1/api/danh-sach/phim-le")
    Call<SeriesResponse> getPhimLe(@Query("page") int page);

    @GET("v1/api/danh-sach/hoat-hinh")
    Call<SeriesResponse> getHoatHinh(@Query("page") int page);

    @GET("v1/api/the-loai/tinh-cam")
    Call<SeriesResponse> getTheLoai();

    @GET("v1/api/quoc-gia/{slug}")
    Call<SeriesResponse> getQuocGia(@Path("slug") String slug);
    @GET
    Call<ResponseBody> downloadMovie(@Url String movieLink);
    @GET("v1/api/danh-sach/phim-bo")
    Call<SeriesResponse2> getSeries2(@Query("page") int page);
}

