package com.example.xemphim.model;

import android.os.Parcel;
import android.os.Parcelable;
import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;
import java.util.List;

public class Series2 implements Parcelable {

    @SerializedName("_id")
    private String id;

    @SerializedName("name")
    private String name;

    @SerializedName("slug")
    private String slug;

    @SerializedName("origin_name")
    private String originName;

    @SerializedName("poster_url")
    private String posterUrl;

    @SerializedName("thumb_url")
    private String thumbUrl;

    @SerializedName("episode_current")
    private String episodeCurrent;

    @SerializedName("quality")
    private String quality;

    @SerializedName("lang")
    private String lang;

    @SerializedName("year")
    private int year;

    private static final String APP_DOMAIN_CDN_IMAGE = "https://img.ophim.live";

    @SerializedName("modified")
    private Modified modified;

    @SerializedName("category")
    private List<Category> category;

    @SerializedName("country")
    private List<Country> country;

    public Series2(String id, String name, String slug, String originName, String posterUrl, String thumbUrl, String episodeCurrent, String quality, String lang, int year, Modified modified, List<Category> category, List<Country> country) {
        this.id = id;
        this.name = name;
        this.slug = slug;
        this.originName = originName;
        this.posterUrl = posterUrl;
        this.thumbUrl = thumbUrl;
        this.episodeCurrent = episodeCurrent;
        this.quality = quality;
        this.lang = lang;
        this.year = year;
        this.modified = modified;
        this.category = category;
        this.country = country;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

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

    public String getPosterUrl() {
        return posterUrl;
    }

    public void setPosterUrl(String posterUrl) {
        this.posterUrl = posterUrl;
    }

    public String getOriginName() {
        return originName;
    }

    public void setOriginName(String originName) {
        this.originName = originName;
    }

    public String getThumbUrl() {
        return thumbUrl;
    }

    public void setThumbUrl(String thumbUrl) {
        this.thumbUrl = thumbUrl;
    }

    public String getEpisodeCurrent() {
        return episodeCurrent;
    }

    public void setEpisodeCurrent(String episodeCurrent) {
        this.episodeCurrent = episodeCurrent;
    }

    public String getQuality() {
        return quality;
    }

    public void setQuality(String quality) {
        this.quality = quality;
    }

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public Modified getModified() {
        return modified;
    }

    public void setModified(Modified modified) {
        this.modified = modified;
    }

    public List<Category> getCategory() {
        return category;
    }

    public void setCategory(List<Category> category) {
        this.category = category;
    }

    public List<Country> getCountry() {
        return country;
    }

    public void setCountry(List<Country> country) {
        this.country = country;
    }

    public static class Modified implements Parcelable {
        @SerializedName("time")
        private String time;

        public Modified() {}

        protected Modified(Parcel in) {
            time = in.readString();
        }

        public static final Creator<Modified> CREATOR = new Creator<Modified>() {
            @Override
            public Modified createFromParcel(Parcel in) {
                return new Modified(in);
            }

            @Override
            public Modified[] newArray(int size) {
                return new Modified[size];
            }
        };

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(time);
        }

        public String getTime() {
            return time;
        }

        public void setTime(String time) {
            this.time = time;
        }
    }

    public static class Category implements Parcelable {
        @SerializedName("name")
        private String name;

        public Category() {}

        protected Category(Parcel in) {
            name = in.readString();
        }

        public static final Creator<Category> CREATOR = new Creator<Category>() {
            @Override
            public Category createFromParcel(Parcel in) {
                return new Category(in);
            }

            @Override
            public Category[] newArray(int size) {
                return new Category[size];
            }
        };

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(name);
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    public static class Country implements Parcelable {
        @SerializedName("name")
        private String name;

        public Country() {}

        protected Country(Parcel in) {
            name = in.readString();
        }

        public static final Creator<Country> CREATOR = new Creator<Country>() {
            @Override
            public Country createFromParcel(Parcel in) {
                return new Country(in);
            }

            @Override
            public Country[] newArray(int size) {
                return new Country[size];
            }
        };

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(name);
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

    }

    protected Series2(Parcel in) {
        id = in.readString();
        name = in.readString();
        slug = in.readString();
        originName = in.readString();
        posterUrl = in.readString();
        thumbUrl = in.readString();
        episodeCurrent = in.readString();
        quality = in.readString();
        lang = in.readString();
        year = in.readInt();
        category = new ArrayList<>();
        in.readList(category, Series2.Category.class.getClassLoader());
        country = new ArrayList<>();
        in.readList(country, Series2.Country.class.getClassLoader());
        modified = in.readParcelable(Modified.class.getClassLoader());
    }

    public static final Creator<Series2> CREATOR = new Creator<Series2>() {
        @Override
        public Series2 createFromParcel(Parcel in) {
            return new Series2(in);
        }

        @Override
        public Series2[] newArray(int size) {
            return new Series2[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
        dest.writeString(slug);
        dest.writeString(originName);
        dest.writeString(posterUrl);
        dest.writeString(thumbUrl);
        dest.writeString(episodeCurrent);
        dest.writeString(quality);
        dest.writeString(lang);
        dest.writeInt(year);
        dest.writeList(category);
        dest.writeList(country);
        dest.writeParcelable(modified, flags);
    }
}
