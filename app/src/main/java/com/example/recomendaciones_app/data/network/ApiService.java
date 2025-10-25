package com.example.recomendaciones_app.data.network;

import com.example.recomendaciones_app.data.model.ProductoResponse;
import com.example.recomendaciones_app.data.model.Category;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {

    @GET("products")
    Call<ProductoResponse> getProducts(@Query("limit") int limit, @Query("skip") int skip);

    @GET("products/search")
    Call<ProductoResponse> searchProducts(@Query("q") String query, @Query("limit") int limit, @Query("skip") int skip);

    // Anotación: CORRECCIÓN DEFINITIVA. La API devuelve una lista de strings.
    // Las actividades se encargarán de transformar esta lista si necesitan objetos Category.
    @GET("products/categories")
    Call<List<Category>> getAllCategories();

    @GET("products/category/{categoryName}")
    Call<ProductoResponse> getProductsByCategory(@Path("categoryName") String categoryName);

}
