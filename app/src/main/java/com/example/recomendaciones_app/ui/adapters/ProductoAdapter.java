package com.example.recomendaciones_app.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.recomendaciones_app.R;
import com.example.recomendaciones_app.data.model.Producto;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;

import java.util.List;

public class ProductoAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public interface OnProductoActionListener {
        void onMeGustaClick(Producto producto, int position);
        void onVerMasClick(Producto producto);
    }

    private List<Producto> productos;
    private Context context;
    private OnProductoActionListener listener;

    private static final int TYPE_PRODUCTO = 0;
    private static final int TYPE_SERVICIO = 1;

    public ProductoAdapter(Context context, List<Producto> productos, OnProductoActionListener listener) {
        this.context = context;
        this.productos = productos;
        this.listener = listener;
    }

    @Override
    public int getItemViewType(int position) {
        Producto producto = productos.get(position);
        return producto.isService() ? TYPE_SERVICIO : TYPE_PRODUCTO;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_SERVICIO) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_service_item, parent, false);
            return new ServiceViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_producto, parent, false);
            return new ProductoViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Producto producto = productos.get(position);
        if (holder instanceof ProductoViewHolder) {
            ProductoViewHolder productoHolder = (ProductoViewHolder) holder;
            productoHolder.tvNombre.setText(producto.getNombre());

            // Anotación: CORRECCIÓN DIRECTA. Si el precio es 0 (como en los servicios), se oculta el campo de precio.
            if (producto.getPrice() == 0) {
                productoHolder.tvPrecio.setVisibility(View.GONE);
            } else {
                productoHolder.tvPrecio.setVisibility(View.VISIBLE);
                productoHolder.tvPrecio.setText(String.format(holder.itemView.getContext().getString(R.string.precio_format), producto.getPrice()));
            }

            productoHolder.chipCategoria.setText(producto.getCategoria());
            productoHolder.rbProductoRating.setRating((float) producto.getRating());
            Glide.with(context)
                    .load(producto.getThumbnail())
                    .placeholder(R.drawable.placeholder_image)
                    .error(R.drawable.error_image)
                    .into(productoHolder.ivProductoImagen);
            updateMeGustaButton(productoHolder.btnMeGusta, producto.getMeGusta() == 1);
            productoHolder.btnMeGusta.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onMeGustaClick(producto, position);
                }
            });
            productoHolder.btnVerMas.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onVerMasClick(producto);
                }
            });
        } else if (holder instanceof ServiceViewHolder) {
            ServiceViewHolder serviceHolder = (ServiceViewHolder) holder;
            serviceHolder.tvServiceName.setText(producto.getNombre());
            serviceHolder.tvServiceCategory.setText(producto.getCategoria());
            serviceHolder.tvServiceAddress.setText(producto.getDescripcion() != null ? producto.getDescripcion() : "");
            serviceHolder.rbServiceRating.setRating((float) producto.getRating());
            Glide.with(context)
                    .load(producto.getThumbnail())
                    .placeholder(R.drawable.placeholder_image)
                    .error(R.drawable.error_image)
                    .into(serviceHolder.ivServiceImage);
            updateMeGustaButton(serviceHolder.btnMeGusta, producto.getMeGusta() == 1);
            serviceHolder.btnMeGusta.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onMeGustaClick(producto, position);
                }
            });
            serviceHolder.btnVerMas.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onVerMasClick(producto);
                }
            });
        }
    }

    private void updateMeGustaButton(MaterialButton button, boolean isLiked) {
        if (isLiked) {
            button.setIconResource(R.drawable.ic_favorite_filled);
        } else {
            button.setIconResource(R.drawable.ic_favorite_border);
        }
    }

    @Override
    public int getItemCount() {
        return productos != null ? productos.size() : 0;
    }

    public void setProductos(List<Producto> nuevosProductos) {
        this.productos.clear();
        if (nuevosProductos != null) {
            this.productos.addAll(nuevosProductos);
        }
        notifyDataSetChanged();
    }

    public void removeItem(int position) {
        if (this.productos != null && position >= 0 && position < this.productos.size()) {
            this.productos.remove(position);
            notifyItemRemoved(position);
        }
    }

    public void addProductos(List<Producto> productosAdicionales) {
        int startPosition = this.productos.size();
        this.productos.addAll(productosAdicionales);
        notifyItemRangeInserted(startPosition, productosAdicionales.size());
    }

    public static class ProductoViewHolder extends RecyclerView.ViewHolder {
        ImageView ivProductoImagen;
        TextView tvNombre, tvPrecio;
        Chip chipCategoria;
        RatingBar rbProductoRating;
        MaterialButton btnVerMas, btnMeGusta;

        public ProductoViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProductoImagen = itemView.findViewById(R.id.ivProductoImagen);
            tvNombre = itemView.findViewById(R.id.tvNombre);
            tvPrecio = itemView.findViewById(R.id.tvPrecio);
            chipCategoria = itemView.findViewById(R.id.chipCategoria);
            rbProductoRating = itemView.findViewById(R.id.rbProductoRating);
            btnVerMas = itemView.findViewById(R.id.btnVerMas);
            btnMeGusta = itemView.findViewById(R.id.btnMeGusta);
        }
    }

    public static class ServiceViewHolder extends RecyclerView.ViewHolder {
        ImageView ivServiceImage;
        TextView tvServiceName, tvServiceCategory, tvServiceAddress;
        RatingBar rbServiceRating;
        MaterialButton btnVerMas, btnMeGusta;

        public ServiceViewHolder(@NonNull View itemView) {
            super(itemView);
            ivServiceImage = itemView.findViewById(R.id.serviceImage);
            tvServiceName = itemView.findViewById(R.id.tvServiceName);
            tvServiceCategory = itemView.findViewById(R.id.tvServiceCategory);
            tvServiceAddress = itemView.findViewById(R.id.tvServiceAddress);
            rbServiceRating = itemView.findViewById(R.id.rbServiceRating);
            btnVerMas = itemView.findViewById(R.id.btnVerMas);
            btnMeGusta = itemView.findViewById(R.id.btnMeGusta);
        }
    }
}
