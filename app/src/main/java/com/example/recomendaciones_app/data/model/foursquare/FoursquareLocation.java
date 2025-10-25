package com.example.recomendaciones_app.data.model.foursquare;

import com.google.gson.annotations.SerializedName;

public class FoursquareLocation {
    private String address;
    private String locality;
    private String region;
    private String postcode;

    // Anotación: CORRECCIÓN. Añadido el campo que faltaba.
    @SerializedName("formatted_address")
    private String formattedAddress;

    // Getters y Setters
    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getLocality() {
        return locality;
    }

    public void setLocality(String locality) {
        this.locality = locality;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getPostcode() {
        return postcode;
    }

    public void setPostcode(String postcode) {
        this.postcode = postcode;
    }

    public String getFormattedAddress() {
        return formattedAddress;
    }

    public void setFormattedAddress(String formattedAddress) {
        this.formattedAddress = formattedAddress;
    }
}
