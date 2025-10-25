package com.example.recomendaciones_app.data.model.service;

public class Service {
    private String id;
    private String name;
    private String category;
    private int categoryId;
    private String address;
    private int distance; // en metros
    private Double rating;
    private String imageUrl;
    private double latitude;
    private double longitude;
    private boolean isFavorite = false;
    // Anotación: CORRECCIÓN. Añadido campo para la URL del icono de categoría.
    private String categoryIconUrl;

    public Service(String id, String name, String category, int categoryId, String address, int distance, Double rating, String imageUrl, double latitude, double longitude, String categoryIconUrl) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.categoryId = categoryId;
        this.address = address;
        this.distance = distance;
        this.rating = rating;
        this.imageUrl = imageUrl;
        this.latitude = latitude;
        this.longitude = longitude;
        this.categoryIconUrl = categoryIconUrl;
    }

    // Getters y Setters

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

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getDistance() {
        return distance;
    }

    public void setDistance(int distance) {
        this.distance = distance;
    }

    public Double getRating() {
        return rating;
    }

    public void setRating(Double rating) {
        this.rating = rating;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
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

    public boolean isFavorite() {
        return isFavorite;
    }

    public void setFavorite(boolean favorite) {
        isFavorite = favorite;
    }

    public String getCategoryIconUrl() {
        return categoryIconUrl;
    }

    public void setCategoryIconUrl(String categoryIconUrl) {
        this.categoryIconUrl = categoryIconUrl;
    }
}
