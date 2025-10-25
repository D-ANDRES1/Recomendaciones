package com.example.recomendaciones_app.data.model;

import com.google.gson.annotations.SerializedName;

public class N8nResponse {

    // Anotación: Usamos @SerializedName por si el nombre del campo en el JSON
    // fuera diferente o para evitar problemas con ofuscación de código.
    @SerializedName("reply")
    private String reply;

    public String getReply() {
        return reply;
    }

    public void setReply(String reply) {
        this.reply = reply;
    }
}
