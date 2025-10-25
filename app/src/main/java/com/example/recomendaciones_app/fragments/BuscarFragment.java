package com.example.recomendaciones_app.fragments;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.recomendaciones_app.R;
import com.example.recomendaciones_app.data.database.ProductoService;
import com.example.recomendaciones_app.data.model.Producto;
import com.example.recomendaciones_app.data.model.foursquare.FoursquareCategory;
import com.example.recomendaciones_app.data.model.foursquare.FoursquarePlace;
import com.example.recomendaciones_app.data.model.foursquare.FoursquareResponse;
import com.example.recomendaciones_app.data.model.service.Service;
import com.example.recomendaciones_app.data.network.ApiClient;
import com.example.recomendaciones_app.data.network.FoursquareApiService;
import com.example.recomendaciones_app.ui.adapters.ServiceAdapter;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BuscarFragment extends Fragment implements ServiceAdapter.OnFavoriteClickListener {

    private MapView mapView;
    private FusedLocationProviderClient fusedLocationClient;
    private final ExecutorService databaseExecutor = Executors.newSingleThreadExecutor();
    private final Handler uiHandler = new Handler(Looper.getMainLooper());

    private ProductoService productoService;

    private RecyclerView recyclerView;
    private ServiceAdapter serviceAdapter;
    private ProgressBar progressBar;
    private SearchView searchView;
    private EditText locationInput;
    private ChipGroup chipGroupCategories;
    private Button useMyLocationButton;

    private List<Service> serviceList = new ArrayList<>();
    private String currentCategoryFilter = null;

    private static final Set<String> CATEGORIAS_EXCLUIR = new HashSet<>(Arrays.asList(
        "community center", "park", "playground", "plaza", "square", "garden", "zoo", "amusement park", "campground", "cemetery", "historic site and protected site", "heritage site", "historic landmark", "protected area"
    ));

    private static final Set<String> RESTAURANT_CATEGORY_IDS = new HashSet<>(Arrays.asList(
        "13065", "13276", "13285", "13297", "13303", "13314", "13377", "13383", "13388"
    ));

    private final ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
        if (isGranted) {
            getCurrentLocationAndSearch();
        } else {
            Toast.makeText(getContext(), "Permiso denegado. No se puede usar la ubicación actual.", Toast.LENGTH_LONG).show();
        }
    });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_buscar, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mapView = view.findViewById(R.id.map);
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);

        recyclerView = view.findViewById(R.id.recycler_view_services);
        progressBar = view.findViewById(R.id.progress_bar);
        searchView = view.findViewById(R.id.search_view);
        locationInput = view.findViewById(R.id.location_input);
        chipGroupCategories = view.findViewById(R.id.chip_group_categories);
        useMyLocationButton = view.findViewById(R.id.use_my_location_button);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
        productoService = new ProductoService(getContext());

        setupRecyclerView();
        setupChips();
        setupSearchView();
        setupButtons();

        mapView.getController().setZoom(12.0);
        mapView.getController().setCenter(new GeoPoint(14.2822, -89.8975));
    }

    private void setupRecyclerView() {
        serviceAdapter = new ServiceAdapter(getContext(), serviceList, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(serviceAdapter);
    }

    private void setupChips() {
        addCategoryChip("Spa", "10032");
        addCategoryChip("Gym", "10040");
        addCategoryChip("Restaurante", "13065");
        addCategoryChip("Belleza", "11119");
        addCategoryChip("Auto", "18021");
        addCategoryChip("Servicios", "12000");
        addCategoryChip("Compras", "16000");

        chipGroupCategories.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (!checkedIds.isEmpty()) {
                int checkedId = checkedIds.get(0);
                Chip chip = group.findViewById(checkedId);
                if (chip != null) {
                    currentCategoryFilter = (String) chip.getTag();
                }
            } else {
                currentCategoryFilter = null;
            }
            
            if (locationInput.getText().toString().equals(getString(R.string.current_location_text))) {
                getCurrentLocationAndSearch();
            } else {
                searchView.post(() -> searchView.setQuery(searchView.getQuery(), true));
            }
        });
    }

    private void addCategoryChip(String name, String categoryId) {
        Chip chip = new Chip(getContext());
        chip.setText(name);
        chip.setTag(categoryId);
        chip.setCheckable(true);
        chipGroupCategories.addView(chip);
    }

    private void setupSearchView() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                String location = locationInput.getText().toString();
                if (!location.isEmpty()) {
                    searchPlaces(query, location, currentCategoryFilter);
                } else {
                    Toast.makeText(getContext(), "Por favor, ingresa una ubicación", Toast.LENGTH_SHORT).show();
                }
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
    }

    private void setupButtons() {
        useMyLocationButton.setOnClickListener(v -> {
            getCurrentLocationAndSearch();
        });
    }

    private void getCurrentLocationAndSearch() {
        if (getContext() == null) return;
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(requireActivity(), location -> {
            if (location != null) {
                locationInput.setText(getString(R.string.current_location_text));
                String latLng = String.format(Locale.US, "%.6f,%.6f", location.getLatitude(), location.getLongitude());
                String query = searchView.getQuery().toString();
                searchByCoordinates(query, latLng, currentCategoryFilter);

                mapView.getController().setCenter(new GeoPoint(location.getLatitude(), location.getLongitude()));
            } else {
                Toast.makeText(getContext(), "No se pudo obtener la ubicación actual. Asegúrate de tener la ubicación activada.", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void searchPlaces(String query, String location, String categories) {
        progressBar.setVisibility(View.VISIBLE);
        FoursquareApiService apiService = ApiClient.getFoursquareClient().create(FoursquareApiService.class);

        apiService.searchPlaces(location, query, categories, 50).enqueue(new Callback<FoursquareResponse>() {
            @Override
            public void onResponse(Call<FoursquareResponse> call, Response<FoursquareResponse> response) {
                handleFoursquareResponse(response);
            }

            @Override
            public void onFailure(Call<FoursquareResponse> call, Throwable t) {
                handleApiFailure(t);
            }
        });
    }

    private void searchByCoordinates(String query, String latLng, String categories) {
        progressBar.setVisibility(View.VISIBLE);
        FoursquareApiService apiService = ApiClient.getFoursquareClient().create(FoursquareApiService.class);

        apiService.searchByCoordinates(latLng, 5000, query, categories, 50).enqueue(new Callback<FoursquareResponse>() {
            @Override
            public void onResponse(Call<FoursquareResponse> call, Response<FoursquareResponse> response) {
                handleFoursquareResponse(response);
            }

            @Override
            public void onFailure(Call<FoursquareResponse> call, Throwable t) {
                handleApiFailure(t);
            }
        });
    }

    private void handleFoursquareResponse(Response<FoursquareResponse> response) {
        progressBar.setVisibility(View.GONE);
        if (response.isSuccessful() && response.body() != null && response.body().getResults() != null) {
            databaseExecutor.execute(() -> {
                List<String> favoriteIds = productoService.obtenerTodosLosIdsFavoritos();
                List<Service> filteredList = mapFoursquareResult(response.body().getResults(), favoriteIds);
                serviceList = filteredList;
                uiHandler.post(() -> {
                    serviceAdapter.setServices(filteredList);
                    updateMapWithResults(filteredList);
                    if (filteredList.isEmpty()) {
                        Toast.makeText(getContext(), "No se encontraron servicios para esta categoría.", Toast.LENGTH_LONG).show();
                    }
                });
            });
        } else {
            Toast.makeText(getContext(), "Error en la búsqueda. Intenta de nuevo.", Toast.LENGTH_SHORT).show();
            uiHandler.post(() -> {
                serviceAdapter.setServices(new ArrayList<>());
                updateMapWithResults(new ArrayList<>());
            });
        }
    }

    private void handleApiFailure(Throwable t) {
        progressBar.setVisibility(View.GONE);
        Toast.makeText(getContext(), "Fallo en la conexión: " + t.getMessage(), Toast.LENGTH_SHORT).show();
    }

    private List<Service> mapFoursquareResult(List<FoursquarePlace> places, List<String> favoriteIds) {
        return places.stream()
            .filter(place -> {
                if (place.getCategories() != null && !place.getCategories().isEmpty()) {
                    String categoryName = place.getCategories().get(0).getName().toLowerCase(Locale.ROOT);
                    for (String excluida : CATEGORIAS_EXCLUIR) {
                        if (categoryName.contains(excluida.toLowerCase(Locale.ROOT))) {
                            return false;
                        }
                    }
                    if (currentCategoryFilter != null && !currentCategoryFilter.isEmpty()) {
                        if (currentCategoryFilter.equals("13065")) {
                            for (FoursquareCategory cat : place.getCategories()) {
                                if (RESTAURANT_CATEGORY_IDS.contains(cat.getFsqCategoryId())) {
                                    return true;
                                }
                            }
                            return false;
                        } else {
                            String categoryId = place.getCategories().get(0).getFsqCategoryId();
                            return currentCategoryFilter.equals(categoryId);
                        }
                    }
                    return true;
                }
                return false;
            })
            .map(place -> {
                String categoryName = "Categoría no disponible";
                if (place.getCategories() != null && !place.getCategories().isEmpty()) {
                    categoryName = place.getCategories().get(0).getName();
                }

                String imageUrl = null;
                if (place.getPhotos() != null && !place.getPhotos().isEmpty()) {
                    imageUrl = place.getPhotos().get(0).getPrefix() + "original" + place.getPhotos().get(0).getSuffix();
                }

                String formattedAddress = "Dirección no disponible";
                if (place.getLocation() != null && place.getLocation().getFormattedAddress() != null) {
                    formattedAddress = place.getLocation().getFormattedAddress();
                }

                Service service = new Service(
                    place.getFsqPlaceId(),
                    place.getName(),
                    categoryName,
                    0, // categoryId no se usa en la tarjeta de servicio
                    formattedAddress,
                    place.getDistance() != null ? place.getDistance() : 0,
                    place.getRating(),
                    imageUrl,
                    place.getLatitude(),
                    place.getLongitude(),
                    null // categoryIconUrl no se usa
                );

                if (favoriteIds != null && favoriteIds.contains(service.getId())) {
                    service.setFavorite(true);
                }

                return service;
            })
            .collect(Collectors.toList());
    }

    private void updateMapWithResults(List<Service> services) {
        if (mapView == null || services == null) return;
        mapView.getOverlays().clear();
        if (services.isEmpty()) {
            mapView.invalidate();
            return;
        }
        GeoPoint firstGeoPoint = null;
        for (Service service : services) {
            GeoPoint servicePoint = new GeoPoint(service.getLatitude(), service.getLongitude());
            Marker serviceMarker = new Marker(mapView);
            serviceMarker.setPosition(servicePoint);
            serviceMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            serviceMarker.setTitle(service.getName());
            mapView.getOverlays().add(serviceMarker);
            if (firstGeoPoint == null) {
                firstGeoPoint = servicePoint;
            }
        }
        if (firstGeoPoint != null) {
            mapView.getController().setCenter(firstGeoPoint);
        }
        mapView.invalidate();
    }

    @Override
    public void onFavoriteClick(Service service, int position) {
        service.setFavorite(!service.isFavorite());
        serviceAdapter.notifyItemChanged(position);

        databaseExecutor.execute(() -> {
            if (service.isFavorite()) {
                Producto producto = new Producto();
                producto.setApiId(service.getId());
                producto.setNombre(service.getName());
                producto.setCategoria(service.getCategory());
                producto.setPrice(0); // Foursquare no da precio
                producto.setRating(service.getRating() != null ? service.getRating() : 0.0);
                producto.setThumbnail(service.getImageUrl());
                producto.setMeGusta(1);
                // Anotación: CORRECCIÓN DEFINITIVA. Se marca el producto como un servicio.
                producto.setService(true);
                productoService.insertarOActualizarProducto(producto, true, false);
            } else {
                productoService.actualizarProductoEstado(service.getId(), false, false);
            }
        });

        Toast.makeText(getContext(), service.getName() + (service.isFavorite() ? " añadido a" : " quitado de") + " favoritos", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mapView != null) {
            mapView.onResume();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mapView != null) {
            mapView.onPause();
        }
    }
}
