package com.example.xemphim.model;

import java.util.List;

public class LinkPhim {
    private String name; // Tên tập phim (Full)
    private String slug;
    private String filename;
    private String link_embed; // Trùng với JSON là "link_embed"
    private String link_m3u8;
    private List<LinkPhim> server_data;// Trùng với JSON là "link_m3u8"

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getLinkEmbed() {
        return link_embed;
    }

    public void setLinkEmbed(String link_embed) {
        this.link_embed = link_embed;
    }

    public String getLinkM3u8() {
        return link_m3u8;
    }

    public void setLinkM3u8(String link_m3u8) {
        this.link_m3u8 = link_m3u8;
    }
    public List<LinkPhim> getServerData() {
        return server_data;
    }
}
