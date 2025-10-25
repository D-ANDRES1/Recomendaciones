package com.example.recomendaciones_app.data.manager;

import com.example.recomendaciones_app.data.model.Producto;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Clase Singleton para gestionar el estado del carrito de compras de forma centralizada.
 */
public class CarritoManager {

    private static CarritoManager instance;
    private final List<Producto> cartItems = new ArrayList<>();

    private CarritoManager() {}

    public static synchronized CarritoManager getInstance() {
        if (instance == null) {
            instance = new CarritoManager();
        }
        return instance;
    }

    public void addProduct(Producto producto) {
        cartItems.add(producto);
    }

    // Anotación: CORRECCIÓN. El método ahora acepta un apiId de tipo String para ser consistente.
    public void removeItem(String productId) {
        if (productId == null) return;
        for (Iterator<Producto> iterator = cartItems.iterator(); iterator.hasNext(); ) {
            Producto producto = iterator.next();
            // Anotación: CORRECCIÓN. Se usa .equals() para comparar Strings, no '=='.
            if (productId.equals(producto.getApiId())) {
                iterator.remove();
                break; // Suponemos que no hay IDs duplicados, así que podemos salir.
            }
        }
    }

    public List<Producto> getCartItems() {
        return cartItems;
    }

    public void clearCart() {
        cartItems.clear();
    }
}
