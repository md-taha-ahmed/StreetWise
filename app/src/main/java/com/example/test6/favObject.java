package com.example.test6;

import android.net.Uri;

public class favObject {
    String name,username,category;
    float rating;
    Uri img;

    public favObject(String name, String username, String category, float rating, Uri img) {
        this.name = name;
        this.username = username;
        this.category = category;
        this.rating = rating;
        this.img = img;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public Uri getImg() {
        return img;
    }

    public void setImg(Uri img) {
        this.img = img;
    }
}

