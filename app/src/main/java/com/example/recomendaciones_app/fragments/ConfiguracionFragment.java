package com.example.recomendaciones_app.fragments;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;

import com.example.recomendaciones_app.LoginActivity;
import com.example.recomendaciones_app.MainActivity;
import com.example.recomendaciones_app.OnboardingActivity;
import com.example.recomendaciones_app.R;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ConfiguracionFragment extends Fragment {

    private static final String CHANNEL_ID = "ofertas_channel";
    private static final int NOTIFICATION_ID = 1;
    public static final String KEY_DARK_MODE = "dark_mode_enabled";

    private MaterialSwitch switchDarkMode;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_configuracion, container, false);

        Button btnEditProfile = view.findViewById(R.id.btnEditProfile);
        Button btnChangePreferences = view.findViewById(R.id.btnChangePreferences);
        Button btnTestNotification = view.findViewById(R.id.btnTestNotification);
        Button btnLogout = view.findViewById(R.id.btnLogout);

        switchDarkMode = view.findViewById(R.id.switchDarkMode);
        setupDarkModeSwitch();

        btnEditProfile.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Función de editar perfil no implementada.", Toast.LENGTH_SHORT).show();
        });

        btnChangePreferences.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), OnboardingActivity.class);
            startActivity(intent);
        });

        btnTestNotification.setOnClickListener(v -> {
            sendTestNotification();
        });

        // Anotación: LÓGICA MEJORADA. Se verifica que el cierre de sesión sea efectivo.
        btnLogout.setOnClickListener(v -> {
            if (getContext() == null) return; // Evita crashes si el fragmento no está adjunto

            // 1. Cierra la sesión del usuario actual en Firebase.
            FirebaseAuth.getInstance().signOut();

            // 2. Verifica que el usuario actual sea nulo después del signOut.
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser == null) {
                // 3. Si la sesión se cerró correctamente, muestra un mensaje y redirige.
                Toast.makeText(getContext(), "Sesión cerrada correctamente", Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(getActivity(), LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);

                if (getActivity() != null) {
                    getActivity().finish();
                }
            } else {
                // 4. Si algo falló, informa al usuario.
                Toast.makeText(getContext(), "Error: No se pudo cerrar la sesión.", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }

    private void setupDarkModeSwitch() {
        if (getContext() == null) return;
        SharedPreferences prefs = getContext().getSharedPreferences(OnboardingActivity.PREFS_NAME, Context.MODE_PRIVATE);
        boolean isDarkMode = prefs.getBoolean(KEY_DARK_MODE, isSystemInDarkMode());
        switchDarkMode.setChecked(isDarkMode);

        switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (getContext() == null) return;
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean(KEY_DARK_MODE, isChecked);
            editor.apply();

            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
        });
    }

    private boolean isSystemInDarkMode() {
        if (getContext() == null) return false;
        int nightModeFlags = getContext().getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        return nightModeFlags == Configuration.UI_MODE_NIGHT_YES;
    }

    private void sendTestNotification() {
        if (getContext() == null) return;
        Intent intent = new Intent(getContext(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(getContext(), 0, intent, PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getContext(), CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(getString(R.string.notification_title))
                .setContentText(getString(R.string.notification_content))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getContext());

        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(getContext(), "Permiso de notificaciones no concedido.", Toast.LENGTH_SHORT).show();
            return;
        }

        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }
}
