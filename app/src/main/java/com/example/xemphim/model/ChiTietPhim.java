package com.example.xemphim.model;

import java.util.List;

public class ChiTietPhim {
    private boolean status;
    private String msg;
    private Movie movie;
    private List<LinkPhim> episodes;

    public List<LinkPhim> getEpisodes() {
        return episodes;
    }

    public void setEpisodes(List<LinkPhim> episodes) {
        this.episodes = episodes;
    }

    public boolean isStatus() {
        return status;
    }

    public String getMsg() {
        return msg;
    }

    public Movie getMovie() {
        return movie;
    }
}




