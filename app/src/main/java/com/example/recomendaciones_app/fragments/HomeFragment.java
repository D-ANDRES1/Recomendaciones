package com.example.recomendaciones_app.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ConcatAdapter;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.recomendaciones_app.R;
import com.example.recomendaciones_app.OnboardingActivity;
import com.example.recomendaciones_app.data.database.ProductoService;
import com.example.recomendaciones_app.data.model.Producto;
import com.example.recomendaciones_app.data.model.ProductoResponse;
import com.example.recomendaciones_app.data.model.Category;
import com.example.recomendaciones_app.data.network.ApiClient;
import com.example.recomendaciones_app.data.network.ApiService;
import com.example.recomendaciones_app.ui.activities.AsistenteActivity;
import com.example.recomendaciones_app.ui.activities.ProductoDetalleActivity;
import com.example.recomendaciones_app.ui.adapters.HomeOfertasAdapter;
import com.example.recomendaciones_app.ui.adapters.OfertaAdapter;
import com.example.recomendaciones_app.ui.adapters.ProductoAdapter;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment implements ProductoAdapter.OnProductoActionListener, OfertaAdapter.OnOfertaClickListener {

    private static final int PAGE_SIZE = 30;
    private static final long DEBOUNCE_DELAY = 300;
    private static final double MIN_DISCOUNT_FOR_DEAL = 15.0;
    private static final int MAX_OFERTAS_A_MOSTRAR = 15;
    private static final String FOR_YOU_SLUG = "for-you-slug";

    private RecyclerView recyclerViewPrincipal;
    private ProductoAdapter productoAdapter;
    private HomeOfertasAdapter homeOfertasAdapter;
    private List<Producto> listaProductos = new ArrayList<>();
    private List<Producto> listaOfertas = new ArrayList<>();
    private List<Producto> listaOfertasBackup = new ArrayList<>();
    private EditText searchInput;
    private ChipGroup chipGroupCategories;
    private ProgressBar progressBar, progressBarBottom;
    private View categoriesScrollView;

    private ApiService apiService;
    private ProductoService productoService;
    private final ExecutorService databaseExecutor = Executors.newSingleThreadExecutor();

    private boolean isLoading = false;
    private int skip = 0;
    private int totalProducts = 0;
    private String currentSearchQuery = null;
    private String currentCategory = null;

    private Handler searchHandler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;
    private TextWatcher searchWatcher;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        apiService = ApiClient.getClient().create(ApiService.class);
        productoService = new ProductoService(getContext());

        recyclerViewPrincipal = view.findViewById(R.id.recyclerViewProductos);
        searchInput = view.findViewById(R.id.searchInput);
        chipGroupCategories = view.findViewById(R.id.chipGroupHomeCategories);
        categoriesScrollView = view.findViewById(R.id.categoriesScrollView);
        progressBar = view.findViewById(R.id.progressBar);
        progressBarBottom = view.findViewById(R.id.progressBarBottom);

        // Anotación: FUNCIONALIDAD AÑADIDA. Se añade el listener para la tarjeta del Asistente IA.
        View assistantCard = view.findViewById(R.id.assistantCard);
        assistantCard.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), AsistenteActivity.class);
            startActivity(intent);
        });

        setupRecyclerView();
        setupSearch();
        setupCategoryChips();
        loadInitialData();
    }

    private void setupRecyclerView() {
        productoAdapter = new ProductoAdapter(getContext(), listaProductos, this);
        homeOfertasAdapter = new HomeOfertasAdapter(getContext(), listaOfertas, this);

        ConcatAdapter concatAdapter = new ConcatAdapter(homeOfertasAdapter, productoAdapter);
        recyclerViewPrincipal.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewPrincipal.setAdapter(concatAdapter);

        recyclerViewPrincipal.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (layoutManager == null) return;

                boolean isSpecificCategory = currentCategory != null && !currentCategory.equals("all-slug");

                if (FOR_YOU_SLUG.equals(currentCategory) || isSpecificCategory) {
                    return;
                }

                int visibleItemCount = layoutManager.getChildCount();
                int totalItemCountInView = layoutManager.getItemCount();
                int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();

                if (!isLoading && (visibleItemCount + firstVisibleItemPosition) >= totalItemCountInView
                        && firstVisibleItemPosition >= 0
                        && productoAdapter.getItemCount() < totalProducts) {
                    loadMoreProducts();
                }
            }
        });
    }

    private void setupSearch() {
        searchWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchHandler.removeCallbacks(searchRunnable);
            }

            @Override
            public void afterTextChanged(Editable s) {
                currentSearchQuery = s.toString();
                searchRunnable = () -> {
                    if (!currentSearchQuery.isEmpty()) {
                        currentCategory = null;
                        chipGroupCategories.clearCheck();
                    }
                    setSearchUiMode(!currentSearchQuery.isEmpty());
                    resetAndFetch();
                };
                searchHandler.postDelayed(searchRunnable, DEBOUNCE_DELAY);
            }
        };
        searchInput.addTextChangedListener(searchWatcher);
    }

    private void setSearchUiMode(boolean isSearching) {
        if (isSearching) {
            if (!listaOfertas.isEmpty()) {
                listaOfertasBackup.clear();
                listaOfertasBackup.addAll(listaOfertas);
                listaOfertas.clear();
            }
        } else {
            if (listaOfertas.isEmpty() && !listaOfertasBackup.isEmpty()) {
                listaOfertas.addAll(listaOfertasBackup);
                listaOfertasBackup.clear();
            }
        }
        homeOfertasAdapter.notifyDataSetChanged();
        categoriesScrollView.setVisibility(isSearching ? View.GONE : View.VISIBLE);
    }

    private void setupCategoryChips() {
        if (!isNetworkAvailable()) return;
        apiService.getAllCategories().enqueue(new Callback<List<Category>>() {
            @Override
            public void onResponse(Call<List<Category>> call, Response<List<Category>> response) {
                if (getContext() == null || !isAdded()) return;
                if (response.isSuccessful() && response.body() != null) {
                    chipGroupCategories.removeAllViews();
                    addChip(getString(R.string.category_todos), "all-slug", true);
                    addChip(getString(R.string.category_para_ti), FOR_YOU_SLUG, false);
                    for (Category category : response.body()) {
                        addChip(category.getName(), category.getSlug(), false);
                    }
                }
            }

            @Override
            public void onFailure(Call<List<Category>> call, Throwable t) {
                if (getContext() == null || !isAdded()) return;
            }
        });

        chipGroupCategories.setOnCheckedChangeListener((group, checkedId) -> {
            Chip chip = group.findViewById(checkedId);
            if (chip == null || !chip.isChecked()) {
                return;
            }
            
            currentCategory = chip.getTag().toString();
            currentSearchQuery = null;

            searchInput.removeTextChangedListener(searchWatcher);
            searchInput.setText("");
            searchInput.addTextChangedListener(searchWatcher);

            resetAndFetch();
        });
    }

    private void addChip(String name, String slug, boolean isChecked) {
        Chip chip = new Chip(getContext());
        chip.setText(name);
        chip.setTag(slug);
        chip.setCheckable(true);
        chip.setChecked(isChecked);
        chipGroupCategories.addView(chip);
    }

    private void loadInitialData() {
        if (isNetworkAvailable()) {
            fetchOffers();
            resetAndFetch();
        } else {
            Toast.makeText(getContext(), R.string.no_hay_conexion, Toast.LENGTH_LONG).show();
            loadFromDatabase();
        }
    }

    private void fetchOffers() {
        apiService.getProducts(150, 0).enqueue(new Callback<ProductoResponse>() {
            @Override
            public void onResponse(Call<ProductoResponse> call, Response<ProductoResponse> response) {
                if (getContext() == null || !isAdded()) return;
                if (response.isSuccessful() && response.body() != null) {
                    extractAndSetOfertas(response.body().getProducts());
                }
            }

            @Override
            public void onFailure(Call<ProductoResponse> call, Throwable t) {
            }
        });
    }

    private void resetAndFetch() {
        skip = 0;
        listaProductos.clear();
        if (productoAdapter != null) productoAdapter.notifyDataSetChanged();
        fetchProducts();
    }

    private void loadMoreProducts() {
        skip += PAGE_SIZE;
        fetchProducts();
    }

    private void fetchProducts() {
        if (isLoading) return;
        isLoading = true;
        showProgress(true, skip > 0);

        if (FOR_YOU_SLUG.equals(currentCategory)) {
            fetchForYouProducts();
        } else {
            fetchNormalProducts();
        }
    }

    private void fetchNormalProducts() {
        Call<ProductoResponse> call;
        boolean isSpecificCategory = currentCategory != null && !currentCategory.equals("all-slug");

        if (currentSearchQuery != null && !currentSearchQuery.isEmpty()) {
            call = apiService.searchProducts(currentSearchQuery, PAGE_SIZE, skip);
        } else if (isSpecificCategory) {
            call = apiService.getProductsByCategory(currentCategory);
        } else {
            call = apiService.getProducts(PAGE_SIZE, skip);
        }

        call.enqueue(new Callback<ProductoResponse>() {
            @Override
            public void onResponse(Call<ProductoResponse> call, Response<ProductoResponse> response) {
                if (getContext() == null || !isAdded()) return;
                if (response.isSuccessful() && response.body() != null) {
                    ProductoResponse pr = response.body();
                    totalProducts = pr.getTotal();

                    if (skip == 0) {
                        productoAdapter.setProductos(pr.getProducts());
                    } else {
                        productoAdapter.addProductos(pr.getProducts());
                    }

                    databaseExecutor.execute(() -> productoService.insertarProductos(pr.getProducts()));
                }
                showProgress(false, skip > 0);
                isLoading = false;
            }

            @Override
            public void onFailure(Call<ProductoResponse> call, Throwable t) {
                if (getContext() == null || !isAdded()) return;
                showProgress(false, skip > 0);
                isLoading = false;
            }
        });
    }

    private void extractAndSetOfertas(List<Producto> productos) {
        List<Producto> poolDeOfertas = new ArrayList<>();
        for (Producto p : productos) {
            if (p.getDiscountPercentage() > MIN_DISCOUNT_FOR_DEAL) {
                poolDeOfertas.add(p);
            }
        }
        Collections.shuffle(poolDeOfertas);
        listaOfertas.clear();
        int count = 0;
        for (Producto p : poolDeOfertas) {
            if (count < MAX_OFERTAS_A_MOSTRAR) {
                listaOfertas.add(p);
                count++;
            } else {
                break;
            }
        }
        homeOfertasAdapter.notifyDataSetChanged(); 
    }

    private void fetchForYouProducts() {
        SharedPreferences prefs = getContext().getSharedPreferences(OnboardingActivity.PREFS_NAME, Context.MODE_PRIVATE);
        Set<String> userCategories = prefs.getStringSet(OnboardingActivity.KEY_USER_CATEGORIES, null);

        if (userCategories == null || userCategories.isEmpty()) {
            Toast.makeText(getContext(), "Aún no has seleccionado tus intereses.", Toast.LENGTH_LONG).show();
            showProgress(false, false);
            isLoading = false;
            return;
        }

        List<Producto> forYouProducts = new ArrayList<>();
        AtomicInteger callsToMake = new AtomicInteger(userCategories.size());

        for (String categorySlug : userCategories) {
            apiService.getProductsByCategory(categorySlug).enqueue(new Callback<ProductoResponse>() {
                @Override
                public void onResponse(Call<ProductoResponse> call, Response<ProductoResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        forYouProducts.addAll(response.body().getProducts());
                    }
                    if (callsToMake.decrementAndGet() == 0) {
                        if (getContext() == null || !isAdded()) return;
                        finalizeForYouFetch(forYouProducts);
                    }
                }

                @Override
                public void onFailure(Call<ProductoResponse> call, Throwable t) {
                    if (callsToMake.decrementAndGet() == 0) {
                        if (getContext() == null || !isAdded()) return;
                        finalizeForYouFetch(forYouProducts);
                    }
                }
            });
        }
    }

    private void finalizeForYouFetch(List<Producto> products) {
        Collections.shuffle(products);
        productoAdapter.setProductos(products);
        totalProducts = products.size();
        showProgress(false, false);
        isLoading = false;
    }

    private void loadFromDatabase() {
        databaseExecutor.execute(() -> {
            List<Producto> productosDB = productoService.obtenerTodos();
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> productoAdapter.setProductos(productosDB));
            }
        });
    }

    private void showProgress(boolean show, boolean isBottom) {
        if (isBottom) {
            progressBarBottom.setVisibility(show ? View.VISIBLE : View.GONE);
        } else {
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) return false;
        NetworkCapabilities capabilities = cm.getNetworkCapabilities(cm.getActiveNetwork());
        return capabilities != null && (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) || capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR));
    }

    private void startProductoDetalleActivity(Producto producto, boolean isOferta) {
        Intent intent = new Intent(getActivity(), ProductoDetalleActivity.class);
        intent.putExtra(ProductoDetalleActivity.EXTRA_PRODUCTO, producto);
        intent.putExtra(ProductoDetalleActivity.EXTRA_IS_OFERTA, isOferta);
        startActivity(intent);
    }

    @Override
    public void onMeGustaClick(Producto producto, int position) {
        boolean nuevoEstado = producto.getMeGusta() == 0;
        producto.setMeGusta(nuevoEstado ? 1 : 0);
        productoAdapter.notifyItemChanged(position);
        databaseExecutor.execute(() -> productoService.actualizarProductoEstado(producto.getApiId(), producto.getVisto() == 1, nuevoEstado));
    }

    @Override
    public void onVerMasClick(Producto producto) {
        startProductoDetalleActivity(producto, false);
    }

    @Override
    public void onOfertaClick(Producto producto) {
        startProductoDetalleActivity(producto, true);
    }
}
