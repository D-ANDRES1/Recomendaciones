package com.example.recomendaciones_app.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.recomendaciones_app.R;
import com.example.recomendaciones_app.data.manager.CarritoManager;
import com.example.recomendaciones_app.data.model.Producto;
import com.example.recomendaciones_app.ui.adapters.CarritoAdapter;

import java.util.List;

public class CarritoActivity extends AppCompatActivity implements CarritoAdapter.OnCarritoActionListener {

    private RecyclerView recyclerViewCarrito;
    private TextView tvCarritoVacio, tvTotalAmount, tvSubtotalAmount, tvAhorroAmount, tvAhorroLabel;
    private Button btnComprarAhora;
    private CarritoAdapter carritoAdapter;
    private List<Producto> listaDeCarrito;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_carrito);

        Toolbar toolbar = findViewById(R.id.toolbarCarrito);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        recyclerViewCarrito = findViewById(R.id.recyclerViewCarrito);
        tvCarritoVacio = findViewById(R.id.tvCarritoVacio);
        tvTotalAmount = findViewById(R.id.tvTotalAmount);
        tvSubtotalAmount = findViewById(R.id.tvSubtotalAmount);
        tvAhorroAmount = findViewById(R.id.tvAhorroAmount);
        tvAhorroLabel = findViewById(R.id.tvAhorroLabel);
        btnComprarAhora = findViewById(R.id.btnComprarAhora);

        listaDeCarrito = CarritoManager.getInstance().getCartItems();

        setupRecyclerView();
        updateVisualStateAndTotals(); // Se llama para configurar el estado inicial

        btnComprarAhora.setOnClickListener(v -> {
            Toast.makeText(this, "Compra finalizada. ¡Gracias!", Toast.LENGTH_SHORT).show();
            int previousSize = listaDeCarrito.size();
            CarritoManager.getInstance().clearCart();
            carritoAdapter.notifyItemRangeRemoved(0, previousSize);
            updateVisualStateAndTotals();
        });
    }

    private void setupRecyclerView() {
        recyclerViewCarrito.setLayoutManager(new LinearLayoutManager(this));
        carritoAdapter = new CarritoAdapter(this, listaDeCarrito, this);
        recyclerViewCarrito.setAdapter(carritoAdapter);
    }

    // Anotación: CORRECCIÓN. Renombrado y ya no llama a notifyDataSetChanged.
    private void updateVisualStateAndTotals() {
        if (listaDeCarrito.isEmpty()) {
            tvCarritoVacio.setVisibility(View.VISIBLE);
            findViewById(R.id.summaryLayout).setVisibility(View.GONE);
            recyclerViewCarrito.setVisibility(View.GONE);
            btnComprarAhora.setEnabled(false);
        } else {
            tvCarritoVacio.setVisibility(View.GONE);
            findViewById(R.id.summaryLayout).setVisibility(View.VISIBLE);
            recyclerViewCarrito.setVisibility(View.VISIBLE);
            btnComprarAhora.setEnabled(true);
            calcularTotal();
        }
    }

    private void calcularTotal() {
        double subtotal = 0;
        double ahorro = 0;

        for (Producto item : listaDeCarrito) {
            subtotal += item.getPrice();
            if (item.fueAnadidoComoOferta() && item.getDiscountPercentage() > 0) {
                ahorro += item.getPrice() * (item.getDiscountPercentage() / 100.0);
            }
        }

        double totalFinal = subtotal - ahorro;

        tvSubtotalAmount.setText(String.format(getString(R.string.precio_format), subtotal));
        tvTotalAmount.setText(String.format(getString(R.string.precio_format), totalFinal));

        if (ahorro > 0) {
            String ahorroFormateado = String.format(getString(R.string.precio_format), ahorro);
            tvAhorroAmount.setText(String.format(getString(R.string.ahorro_format), ahorroFormateado));
            tvAhorroAmount.setVisibility(View.VISIBLE);
            tvAhorroLabel.setVisibility(View.VISIBLE);
        } else {
            tvAhorroAmount.setVisibility(View.GONE);
            tvAhorroLabel.setVisibility(View.GONE);
        }
    }

    @Override
    public void onRemoveItem(Producto item, int position) {
        CarritoManager.getInstance().removeItem(item.getApiId());
        // Anotación: CORRECCIÓN. No es necesario eliminar de listaDeCarrito porque es una referencia a la del manager.
        
        // Anotación: CORRECIÓN. Se notifica al adaptador de forma específica.
        carritoAdapter.notifyItemRemoved(position);

        // Se actualiza la UI (totales y estado de carrito vacío)
        updateVisualStateAndTotals();
        
        Toast.makeText(this, "Producto eliminado del carrito", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onItemClick(Producto producto) {
        Intent intent = new Intent(this, ProductoDetalleActivity.class);
        intent.putExtra(ProductoDetalleActivity.EXTRA_PRODUCTO, producto);
        intent.putExtra(ProductoDetalleActivity.EXTRA_IS_OFERTA, producto.fueAnadidoComoOferta());
        startActivity(intent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
