package com.example.xemphim.response;

import com.example.xemphim.model.Series;
import com.example.xemphim.model.Series2;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class SeriesResponse2 {
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
        private List<Series2> items;

        public List<Series2> getItems() {
            return items;
        }
    }
    public class TheLoaiData {
        @SerializedName("name")
        private List<Series2.Category> name;
        public List<Series2.Category> getName() {
            return name;
        }
    }
}