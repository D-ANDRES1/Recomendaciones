package com.example.recomendaciones_app.data.api;

import com.example.recomendaciones_app.data.model.N8nRequest;
import com.example.recomendaciones_app.data.model.N8nResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface N8nApiService {

    /**
     * Anotación: Define una llamada POST al endpoint específico del webhook de n8n.
     * La URL base se configurará al crear el cliente de Retrofit, y esta es la ruta final.
     * MODIFICADO: Ahora espera un único objeto N8nResponse, no una lista.
     */
    @POST("webhook/asistente-super-secreto-123")
    Call<N8nResponse> sendMessage(@Body N8nRequest request);
}
