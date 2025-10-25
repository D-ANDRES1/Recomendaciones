package com.example.recomendaciones_app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.recomendaciones_app.data.model.Category;
import com.example.recomendaciones_app.data.network.ApiClient;
import com.example.recomendaciones_app.data.network.ApiService;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OnboardingActivity extends AppCompatActivity {

    public static final String PREFS_NAME = "RecomendacionesPrefs";
    public static final String KEY_USER_CATEGORIES = "user_categories";
    public static final String KEY_ONBOARDING_COMPLETED = "onboarding_completed";

    private ChipGroup chipGroupCategories;
    private ProgressBar progressBar;
    private ScrollView categoriesScrollView;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        chipGroupCategories = findViewById(R.id.chipGroupCategories);
        progressBar = findViewById(R.id.progressBarOnboarding);
        categoriesScrollView = findViewById(R.id.categoriesScrollView);
        Button btnSave = findViewById(R.id.btnSaveOnboarding);

        apiService = ApiClient.getClient().create(ApiService.class);

        loadCategories();

        btnSave.setOnClickListener(v -> savePreferences());
    }

    private void loadCategories() {
        progressBar.setVisibility(View.VISIBLE);
        categoriesScrollView.setVisibility(View.GONE);

        apiService.getAllCategories().enqueue(new Callback<List<Category>>() {
            @Override
            public void onResponse(Call<List<Category>> call, Response<List<Category>> response) {
                progressBar.setVisibility(View.GONE);
                categoriesScrollView.setVisibility(View.VISIBLE);

                if (response.isSuccessful() && response.body() != null) {
                    List<Category> categoryList = response.body();

                    chipGroupCategories.removeAllViews();
                    for (Category category : categoryList) {
                        Chip chip = new Chip(OnboardingActivity.this);
                        chip.setText(category.getName());
                        chip.setTag(category.getSlug());
                        chip.setCheckable(true);
                        chipGroupCategories.addView(chip);
                    }
                } else {
                    String errorMsg = "Error al cargar las categorías";
                    try {
                        if (response.errorBody() != null) {
                            errorMsg += ": " + response.errorBody().string();
                        }
                    } catch (Exception e) {
                        errorMsg += ". Error parsing errorBody: " + e.getMessage();
                    }
                    Toast.makeText(OnboardingActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<List<Category>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(OnboardingActivity.this, "Fallo de red: " + t.getMessage(), Toast.LENGTH_LONG).show();
                t.printStackTrace();
            }
        });
    }

    private void savePreferences() {
        Set<String> selectedCategories = new HashSet<>();
        for (int id : chipGroupCategories.getCheckedChipIds()) {
            Chip chip = chipGroupCategories.findViewById(id);
            if (chip != null && chip.getTag() != null) {
                selectedCategories.add(chip.getTag().toString());
            }
        }

        if (selectedCategories.isEmpty()) {
            Toast.makeText(OnboardingActivity.this, "Por favor, selecciona al menos una categoría", Toast.LENGTH_SHORT).show();
            return;
        }

        SharedPreferences settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putStringSet(KEY_USER_CATEGORIES, selectedCategories);
        editor.putBoolean(KEY_ONBOARDING_COMPLETED, true);
        editor.apply();

        Toast.makeText(this, "Preferencias guardadas", Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(OnboardingActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
