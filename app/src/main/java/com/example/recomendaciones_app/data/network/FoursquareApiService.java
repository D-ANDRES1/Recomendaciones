package com.example.recomendaciones_app.data.network;

import com.example.recomendaciones_app.data.model.foursquare.FoursquareResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface FoursquareApiService {

    // Anotación: CORRECCIÓN. Las cabeceras ya no son necesarias aquí, se gestionan en ApiClient.
    @GET("places/search")
    Call<FoursquareResponse> searchPlaces(
            @Query("near") String location,
            @Query("query") String query,
            @Query("categories") String categories,
            @Query("limit") int limit
    );

    @GET("places/search")
    Call<FoursquareResponse> searchByCoordinates(
            @Query("ll") String latLng,
            @Query("radius") int radius,
            @Query("query") String query,
            @Query("categories") String categories,
            @Query("limit") int limit
    );
}
