package com.example.recomendaciones_app.data.model.foursquare;

import com.google.gson.annotations.SerializedName;

public class FoursquareCategory {
    @SerializedName("fsq_category_id")
    private String fsqCategoryId;
    private String name;
    // Anotación: CORRECCIÓN. Añadido el objeto Icono.
    private FoursquareIcon icon;

    // Getters y Setters
    public String getFsqCategoryId() {
        return fsqCategoryId;
    }

    public void setFsqCategoryId(String fsqCategoryId) {
        this.fsqCategoryId = fsqCategoryId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public FoursquareIcon getIcon() {
        return icon;
    }

    public void setIcon(FoursquareIcon icon) {
        this.icon = icon;
    }
}
