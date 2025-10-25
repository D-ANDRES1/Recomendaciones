package com.example.recomendaciones_app.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.recomendaciones_app.R;
import com.example.recomendaciones_app.data.model.Producto;

import java.util.List;

public class HomeOfertasAdapter extends RecyclerView.Adapter<HomeOfertasAdapter.ViewHolder> {

    private final List<Producto> ofertas;
    private final OfertaAdapter.OnOfertaClickListener listener;
    private final Context context;

    public HomeOfertasAdapter(Context context, List<Producto> ofertas, OfertaAdapter.OnOfertaClickListener listener) {
        this.context = context;
        this.ofertas = ofertas;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_home_ofertas, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(context, ofertas, listener);
    }

    @Override
    public int getItemCount() {
        // Anotación: CORRECCIÓN. El adaptador ahora solo se muestra si tiene ofertas.
        return ofertas.isEmpty() ? 0 : 1;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        RecyclerView recyclerViewOfertas;

        ViewHolder(View view) {
            super(view);
            recyclerViewOfertas = view.findViewById(R.id.recyclerViewOfertas);
        }

        void bind(Context context, List<Producto> ofertas, OfertaAdapter.OnOfertaClickListener listener) {
            recyclerViewOfertas.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
            OfertaAdapter ofertaAdapter = new OfertaAdapter(context, ofertas, listener);
            recyclerViewOfertas.setAdapter(ofertaAdapter);
        }
    }
}
