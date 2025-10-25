package com.example.recomendaciones_app.data.model.foursquare;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class FoursquarePlace {

    @SerializedName("fsq_place_id")
    private String fsqPlaceId;

    private String name;

    // Anotación: CORRECCIÓN. Añadidos los campos de geolocalización.
    private double latitude;
    private double longitude;

    private FoursquareLocation location;
    private List<FoursquareCategory> categories;
    private Integer distance;
    private Double rating;

    // Anotación: CORRECCIÓN. Añadido el campo de fotos.
    private List<FoursquarePhoto> photos;

    // Getters y Setters

    public String getFsqPlaceId() {
        return fsqPlaceId;
    }

    public void setFsqPlaceId(String fsqPlaceId) {
        this.fsqPlaceId = fsqPlaceId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public FoursquareLocation getLocation() {
        return location;
    }

    public void setLocation(FoursquareLocation location) {
        this.location = location;
    }

    public List<FoursquareCategory> getCategories() {
        return categories;
    }

    public void setCategories(List<FoursquareCategory> categories) {
        this.categories = categories;
    }

    public Integer getDistance() {
        return distance;
    }

    public void setDistance(Integer distance) {
        this.distance = distance;
    }

    public Double getRating() {
        return rating;
    }

    public void setRating(Double rating) {
        this.rating = rating;
    }

    public List<FoursquarePhoto> getPhotos() {
        return photos;
    }

    public void setPhotos(List<FoursquarePhoto> photos) {
        this.photos = photos;
    }
}
