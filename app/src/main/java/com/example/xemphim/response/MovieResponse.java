package com.example.xemphim.response;

import com.example.xemphim.model.Movie;

import java.util.List;

public class MovieResponse {
    private boolean status;
    private List<Movie> items;
    private Pagination pagination;

    public static class Pagination {
        private int totalItems;
        private int totalItemsPerPage;
        private int currentPage;
        private int totalPages;

        // Getters and Setters
    }

    // Getters and Setters for MovieResponse
    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public List<Movie> getItems() {
        return items;
    }

    public void setItems(List<Movie> items) {
        this.items = items;
    }

    public Pagination getPagination() {
        return pagination;
    }

    public void setPagination(Pagination pagination) {
        this.pagination = pagination;
    }
}
