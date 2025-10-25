package com.example.recomendaciones_app.data.model.foursquare;

import com.google.gson.annotations.SerializedName;

public class FoursquarePhoto {

    @SerializedName("fsq_photo_id")
    private String fsqPhotoId;
    private String prefix;
    private String suffix;
    private int width;
    private int height;

    // Getters y Setters

    public String getFsqPhotoId() {
        return fsqPhotoId;
    }

    public void setFsqPhotoId(String fsqPhotoId) {
        this.fsqPhotoId = fsqPhotoId;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getSuffix() {
        return suffix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }
}
