package com.example.recomendaciones_app.data.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;

import com.example.recomendaciones_app.data.model.Producto;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ProductoService {

    private DBHelper dbHelper;
    private static final String TAG = "ProductoService";

    public ProductoService(Context context) {
        this.dbHelper = new DBHelper(context);
    }

    public void insertarProductos(List<Producto> productos) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        try {
            db.beginTransaction();
            for (Producto producto : productos) {
                ContentValues values = new ContentValues();
                values.put(DBHelper.COLUMN_API_ID, producto.getApiId());
                values.put(DBHelper.COLUMN_NOMBRE, producto.getNombre());
                values.put(DBHelper.COLUMN_DESCRIPCION, producto.getDescripcion());
                values.put(DBHelper.COLUMN_PRICE, producto.getPrice());
                values.put(DBHelper.COLUMN_RATING, producto.getRating());
                values.put(DBHelper.COLUMN_BRAND, producto.getBrand());
                values.put(DBHelper.COLUMN_CATEGORIA, producto.getCategoria());
                values.put(DBHelper.COLUMN_IMAGE_URL, producto.getThumbnail());
                values.put(DBHelper.COLUMN_TIMESTAMP, getCurrentTimestamp());
                // Anotación: CORRECCIÓN. Se guarda el estado de isService.
                values.put(DBHelper.COLUMN_IS_SERVICE, producto.isService() ? 1 : 0);

                db.insertWithOnConflict(DBHelper.TABLE_PRODUCTOS, null, values, SQLiteDatabase.CONFLICT_IGNORE);
            }
            db.setTransactionSuccessful();
        } catch (SQLiteException e) {
            Log.e(TAG, "Error al insertar productos", e);
        } finally {
            db.endTransaction();
            db.close();
        }
    }

    public void actualizarProductoEstado(String apiId, boolean visto, boolean meGusta) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        try {
            ContentValues values = new ContentValues();
            values.put(DBHelper.COLUMN_VISTO, visto ? 1 : 0);
            values.put(DBHelper.COLUMN_ME_GUSTA, meGusta ? 1 : 0);
            values.put(DBHelper.COLUMN_TIMESTAMP, getCurrentTimestamp());

            db.update(DBHelper.TABLE_PRODUCTOS, values, DBHelper.COLUMN_API_ID + " = ?", new String[]{apiId});
        } finally {
            db.close();
        }
    }
    
    public void insertarOActualizarProducto(Producto producto, boolean meGusta, boolean visto) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.query(DBHelper.TABLE_PRODUCTOS,
                    new String[]{DBHelper.COLUMN_API_ID},
                    DBHelper.COLUMN_API_ID + " = ?",
                    new String[]{producto.getApiId()},
                    null, null, null);

            ContentValues values = new ContentValues();
            values.put(DBHelper.COLUMN_ME_GUSTA, meGusta ? 1 : 0);
            values.put(DBHelper.COLUMN_VISTO, visto ? 1 : 0);
            values.put(DBHelper.COLUMN_TIMESTAMP, getCurrentTimestamp());

            if (cursor != null && cursor.getCount() > 0) {
                db.update(DBHelper.TABLE_PRODUCTOS, values, DBHelper.COLUMN_API_ID + " = ?", new String[]{producto.getApiId()});
            } else {
                values.put(DBHelper.COLUMN_API_ID, producto.getApiId());
                values.put(DBHelper.COLUMN_NOMBRE, producto.getNombre());
                values.put(DBHelper.COLUMN_DESCRIPCION, producto.getDescripcion());
                values.put(DBHelper.COLUMN_PRICE, producto.getPrice());
                values.put(DBHelper.COLUMN_RATING, producto.getRating());
                values.put(DBHelper.COLUMN_BRAND, producto.getBrand());
                values.put(DBHelper.COLUMN_CATEGORIA, producto.getCategoria());
                values.put(DBHelper.COLUMN_IMAGE_URL, producto.getThumbnail());
                // Anotación: CORRECCIÓN. Se guarda el estado de isService.
                values.put(DBHelper.COLUMN_IS_SERVICE, producto.isService() ? 1 : 0);
                db.insert(DBHelper.TABLE_PRODUCTOS, null, values);
            }
        } finally {
            if (cursor != null) cursor.close();
            db.close();
        }
    }

    public List<Producto> obtenerTodos() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.query(DBHelper.TABLE_PRODUCTOS, null, null, null, null, null, DBHelper.COLUMN_LOCAL_ID + " DESC");
            return cursorToList(cursor);
        } finally {
            if (cursor != null) cursor.close();
            db.close();
        }
    }

    public List<Producto> obtenerPorFavoritos() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = null;
        try {
            String selection = DBHelper.COLUMN_ME_GUSTA + " = ?";
            String[] selectionArgs = {"1"};
            cursor = db.query(DBHelper.TABLE_PRODUCTOS, null, selection, selectionArgs, null, null, DBHelper.COLUMN_TIMESTAMP + " DESC");
            return cursorToList(cursor);
        } finally {
            if (cursor != null) cursor.close();
            db.close();
        }
    }
    
    public List<String> obtenerTodosLosIdsFavoritos() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = null;
        List<String> ids = new ArrayList<>();
        try {
            cursor = db.query(DBHelper.TABLE_PRODUCTOS,
                    new String[]{DBHelper.COLUMN_API_ID},
                    DBHelper.COLUMN_ME_GUSTA + " = ?",
                    new String[]{"1"},
                    null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                int apiIdIndex = cursor.getColumnIndex(DBHelper.COLUMN_API_ID);
                do {
                    if (apiIdIndex != -1) {
                        ids.add(cursor.getString(apiIdIndex));
                    }
                } while (cursor.moveToNext());
            }
        } finally {
            if (cursor != null) cursor.close();
            db.close();
        }
        return ids;
    }

    private List<Producto> cursorToList(Cursor cursor) {
        List<Producto> productos = new ArrayList<>();
        if (cursor != null && cursor.moveToFirst()) {
            do {
                int localIdIndex = cursor.getColumnIndex(DBHelper.COLUMN_LOCAL_ID);
                int apiIdIndex = cursor.getColumnIndex(DBHelper.COLUMN_API_ID);
                int nombreIndex = cursor.getColumnIndex(DBHelper.COLUMN_NOMBRE);
                int descIndex = cursor.getColumnIndex(DBHelper.COLUMN_DESCRIPCION);
                int priceIndex = cursor.getColumnIndex(DBHelper.COLUMN_PRICE);
                int ratingIndex = cursor.getColumnIndex(DBHelper.COLUMN_RATING);
                int brandIndex = cursor.getColumnIndex(DBHelper.COLUMN_BRAND);
                int catIndex = cursor.getColumnIndex(DBHelper.COLUMN_CATEGORIA);
                int imageUrlIndex = cursor.getColumnIndex(DBHelper.COLUMN_IMAGE_URL);
                int vistoIndex = cursor.getColumnIndex(DBHelper.COLUMN_VISTO);
                int meGustaIndex = cursor.getColumnIndex(DBHelper.COLUMN_ME_GUSTA);
                int timestampIndex = cursor.getColumnIndex(DBHelper.COLUMN_TIMESTAMP);
                // Anotación: CORRECCIÓN. Se lee el estado de isService.
                int isServiceIndex = cursor.getColumnIndex(DBHelper.COLUMN_IS_SERVICE);

                Producto p = new Producto();
                if (localIdIndex != -1) p.setLocalId(cursor.getInt(localIdIndex));
                if (apiIdIndex != -1) p.setApiId(cursor.getString(apiIdIndex));
                if (nombreIndex != -1) p.setNombre(cursor.getString(nombreIndex));
                if (descIndex != -1) p.setDescripcion(cursor.getString(descIndex));
                if (priceIndex != -1) p.setPrice(cursor.getDouble(priceIndex));
                if (ratingIndex != -1) p.setRating(cursor.getDouble(ratingIndex));
                if (brandIndex != -1) p.setBrand(cursor.getString(brandIndex));
                if (catIndex != -1) p.setCategoria(cursor.getString(catIndex));
                if (imageUrlIndex != -1) p.setThumbnail(cursor.getString(imageUrlIndex));
                if (vistoIndex != -1) p.setVisto(cursor.getInt(vistoIndex));
                if (meGustaIndex != -1) p.setMeGusta(cursor.getInt(meGustaIndex));
                if (timestampIndex != -1) p.setTimestamp(cursor.getString(timestampIndex));
                // Anotación: CORRECCIÓN. Se asigna el estado de isService.
                if (isServiceIndex != -1) p.setService(cursor.getInt(isServiceIndex) == 1);
                
                productos.add(p);
            } while (cursor.moveToNext());
        }
        return productos;
    }

    private String getCurrentTimestamp() {
        return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).format(new Date());
    }
}
