package com.example.recomendaciones_app.data.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.List;

public class Producto implements Serializable {

    private int localId;

    // Anotación: CORRECCIÓN. apiId ahora es String para ser compatible con Foursquare y DummyJSON.
    @SerializedName("id")
    private String apiId;

    @SerializedName("title")
    private String nombre;
    
    @SerializedName("description")
    private String descripcion;

    @SerializedName("price")
    private double price;

    @SerializedName("discountPercentage")
    private double discountPercentage;

    @SerializedName("rating")
    private double rating;
    
    @SerializedName("brand")
    private String brand;

    @SerializedName("category")
    private String categoria;

    @SerializedName("thumbnail")
    private String thumbnail;

    @SerializedName("images")
    private List<String> images;

    @SerializedName("reviews")
    private List<Review> reviews;

    private int meGusta; 
    private int visto;   
    private String timestamp;

    private transient boolean fueAnadidoComoOferta = false;
    private boolean isService = false;

    // --- Getters y Setters ---

    public boolean fueAnadidoComoOferta() {
        return fueAnadidoComoOferta;
    }

    public void setFueAnadidoComoOferta(boolean fueAnadidoComoOferta) {
        this.fueAnadidoComoOferta = fueAnadidoComoOferta;
    }

    public int getLocalId() { return localId; }
    public void setLocalId(int localId) { this.localId = localId; }

    // Anotación: CORRECCIÓN. Getter y Setter actualizados a String.
    public String getApiId() { return apiId; }
    public void setApiId(String apiId) { this.apiId = apiId; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public double getDiscountPercentage() { return discountPercentage; }
    public void setDiscountPercentage(double discountPercentage) { this.discountPercentage = discountPercentage; }

    public double getRating() { return rating; }
    public void setRating(double rating) { this.rating = rating; }
    
    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public String getThumbnail() { return thumbnail; }
    public void setThumbnail(String thumbnail) { this.thumbnail = thumbnail; }

    public List<String> getImages() { return images; }
    public void setImages(List<String> images) { this.images = images; }

    public List<Review> getReviews() { return reviews; }
    public void setReviews(List<Review> reviews) { this.reviews = reviews; }

    public int getMeGusta() { return meGusta; }
    public void setMeGusta(int meGusta) { this.meGusta = meGusta; }

    public int getVisto() { return visto; }
    public void setVisto(int visto) { this.visto = visto; }

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }

    public boolean isService() {
        return isService;
    }

    public void setService(boolean service) {
        isService = service;
    }
}
