package com.example.recomendaciones_app.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.recomendaciones_app.R;
import com.example.recomendaciones_app.data.model.service.Service;
import com.google.android.material.card.MaterialCardView;

import java.util.List;
import java.util.Locale;

public class ServiceAdapter extends RecyclerView.Adapter<ServiceAdapter.ServiceViewHolder> {

    private List<Service> serviceList;
    private Context context;
    private OnFavoriteClickListener favoriteClickListener;

    public interface OnFavoriteClickListener {
        void onFavoriteClick(Service service, int position);
    }

    public ServiceAdapter(Context context, List<Service> serviceList, OnFavoriteClickListener favoriteClickListener) {
        this.context = context;
        this.serviceList = serviceList;
        this.favoriteClickListener = favoriteClickListener;
    }

    @NonNull
    @Override
    public ServiceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_service_item, parent, false);
        return new ServiceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ServiceViewHolder holder, int position) {
        Service service = serviceList.get(position);
        holder.bind(service);
    }

    @Override
    public int getItemCount() {
        return serviceList.size();
    }

    public void setServices(List<Service> services) {
        this.serviceList = services;
        notifyDataSetChanged();
    }

    public class ServiceViewHolder extends RecyclerView.ViewHolder {

        private View gradientBackground;
        private ImageView serviceImage;
        private MaterialCardView ratingBadge;
        private TextView ratingText;
        private ImageButton favoriteButton;
        private TextView serviceName;
        private TextView serviceCategory;
        private TextView serviceAddress;
        private TextView serviceDistance;

        public ServiceViewHolder(@NonNull View itemView) {
            super(itemView);
            gradientBackground = itemView.findViewById(R.id.gradientBackground);
            serviceImage = itemView.findViewById(R.id.serviceImage);
            ratingBadge = itemView.findViewById(R.id.ratingBadge);
            ratingText = itemView.findViewById(R.id.ratingText);
            favoriteButton = itemView.findViewById(R.id.favoriteButton);
            serviceName = itemView.findViewById(R.id.tvServiceName);
            serviceCategory = itemView.findViewById(R.id.tvServiceCategory);
            serviceAddress = itemView.findViewById(R.id.tvServiceAddress);
            serviceDistance = itemView.findViewById(R.id.serviceDistance);
        }

        public void bind(final Service service) {
            serviceName.setText(service.getName());
            serviceCategory.setText(service.getCategory());
            serviceAddress.setText(service.getAddress());
            serviceDistance.setText(formatDistance(service.getDistance()));

            if (service.isFavorite()) {
                favoriteButton.setImageResource(R.drawable.ic_heart_filled);
            } else {
                favoriteButton.setImageResource(R.drawable.ic_heart_outline);
            }

            favoriteButton.setOnClickListener(v -> {
                if (favoriteClickListener != null) {
                    int pos = getBindingAdapterPosition();
                    if (pos != RecyclerView.NO_POSITION) {
                        favoriteClickListener.onFavoriteClick(service, pos);
                    }
                }
            });

            if (service.getRating() != null && service.getRating() > 7.0) {
                ratingBadge.setVisibility(View.VISIBLE);
                // Usar recurso localizable para el formato del rating
                ratingText.setText(context.getString(R.string.rating_format, service.getRating()));
            } else {
                ratingBadge.setVisibility(View.GONE);
            }

            gradientBackground.setBackground(ContextCompat.getDrawable(context, getCategoryGradient(service.getCategoryId())));

            // Anotación: CORRECCIÓN. Se usa la URL de la foto si existe, si no, la del icono de categoría.
            if (service.getImageUrl() != null && !service.getImageUrl().isEmpty()) {
                serviceImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
                Glide.with(context)
                        .load(service.getImageUrl())
                        .into(serviceImage);
            } else if (service.getCategoryIconUrl() != null && !service.getCategoryIconUrl().isEmpty()) {
                serviceImage.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                Glide.with(context)
                        .load(service.getCategoryIconUrl())
                        .into(serviceImage);
            } else {
                 serviceImage.setImageResource(R.drawable.ic_location); // Fallback genérico
                 serviceImage.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            }
        }
    }

    private String formatDistance(int distanceInMeters) {
        if (distanceInMeters < 1000) {
            // Usar recurso localizable para metros
            return context.getString(R.string.distance_meters_format, distanceInMeters);
        } else {
            float distanceInKm = distanceInMeters / 1000f;
            // Usar recurso localizable para kilómetros
            return context.getString(R.string.distance_km_format, distanceInKm);
        }
    }

    private int getCategoryGradient(int categoryId) {
        switch (categoryId) {
            case 10032: return R.drawable.gradient_spa;
            case 10040: return R.drawable.gradient_gym;
            case 13065: return R.drawable.gradient_restaurant;
            case 11119: return R.drawable.gradient_beauty;
            case 18021: return R.drawable.gradient_auto;
            default: return R.drawable.gradient_default;
        }
    }
}
