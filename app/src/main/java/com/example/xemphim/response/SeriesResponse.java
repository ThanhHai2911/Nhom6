package com.example.xemphim.response;

import com.example.xemphim.model.Series;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class SeriesResponse {
    private String status;
    private String message;
    private SeriesData data;
    private TheLoaiData name;


    public String getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public SeriesData getData() {
        return data;
    }
    public TheLoaiData getTheLoaiData() {
        return name;
    }

    public class SeriesData {
        @SerializedName("items")
        private List<Series> items;

        public List<Series> getItems() {
            return items;
        }
    }
    public class TheLoaiData {
        @SerializedName("name")
        private List<Series.Category> name;

        public List<Series.Category> getName() {
            return name;
        }
    }
}
