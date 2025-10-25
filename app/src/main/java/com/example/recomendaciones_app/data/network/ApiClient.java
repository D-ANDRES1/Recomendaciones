package com.example.recomendaciones_app.data.network;

import android.util.Log;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {

    private static final String TAG = "ApiClient";
    private static final String FOURSQUARE_API_KEY = "GM4WMNYSOHESWGCL2VRQ4DLFXUSIAXMSVE3BY2XBGL0K2X1N";

    // TODO: ¡IMPORTANTE! Reemplaza esta clave por una cadena de texto larga y aleatoria.
    // Puedes usar un generador de contraseñas online.
    private static final String N8N_SECRET_KEY = "brT$ppLJGKUVF8n66trw";

    private static final String DUMMYJSON_BASE_URL = "https://dummyjson.com/";
    private static final String FOURSQUARE_BASE_URL = "https://places-api.foursquare.com/";
    private static final String N8N_BASE_URL = "https://primary-production-7802.up.railway.app/";

    private static Retrofit dummyJsonRetrofit = null;
    private static Retrofit foursquareRetrofit = null;
    private static Retrofit n8nRetrofit = null;

    public static Retrofit getClient() {
        if (dummyJsonRetrofit == null) {
            dummyJsonRetrofit = new Retrofit.Builder()
                    .baseUrl(DUMMYJSON_BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return dummyJsonRetrofit;
    }

    public static Retrofit getFoursquareClient() {
        if (foursquareRetrofit == null) {
            Interceptor headerInterceptor = chain -> {
                Request originalRequest = chain.request();
                Request.Builder builder = originalRequest.newBuilder()
                        .header("Authorization", "Bearer " + FOURSQUARE_API_KEY)
                        .header("Accept", "application/json")
                        .header("X-Places-Api-Version", "2025-06-17");

                Request newRequest = builder.build();
                return chain.proceed(newRequest);
            };

            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .addInterceptor(headerInterceptor)
                    .build();

            foursquareRetrofit = new Retrofit.Builder()
                    .baseUrl(FOURSQUARE_BASE_URL)
                    .client(okHttpClient)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return foursquareRetrofit;
    }

    // Anotación: LÓGICA ALTERNATIVA. Ahora se usa una clave secreta estática.
    public static Retrofit getN8nClient() {
        if (n8nRetrofit == null) {
            // 1. Creamos un interceptor simple.
            Interceptor secretKeyInterceptor = chain -> {
                Request.Builder requestBuilder = chain.request().newBuilder();

                // 2. Añadimos la clave secreta a una cabecera. Usaremos 'X-Api-Key'.
                requestBuilder.addHeader("X-Api-Key", N8N_SECRET_KEY);
                Log.d(TAG, "Secret key added to header.");

                return chain.proceed(requestBuilder.build());
            };

            // 3. Creamos un cliente OkHttp con nuestro interceptor.
            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .addInterceptor(secretKeyInterceptor)
                    .build();

            // 4. Creamos el cliente de Retrofit con el cliente OkHttp seguro.
            n8nRetrofit = new Retrofit.Builder()
                    .baseUrl(N8N_BASE_URL)
                    .client(okHttpClient)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return n8nRetrofit;
    }
}
