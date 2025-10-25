package com.example.recomendaciones_app.ui.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.recomendaciones_app.R;
import com.example.recomendaciones_app.data.api.N8nApiService;
import com.example.recomendaciones_app.data.model.ChatMessage;
import com.example.recomendaciones_app.data.model.N8nRequest;
import com.example.recomendaciones_app.data.model.N8nResponse;
import com.example.recomendaciones_app.data.network.ApiClient;
import com.example.recomendaciones_app.ui.adapters.ChatAdapter;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AsistenteActivity extends AppCompatActivity {

    private static final String TAG = "AsistenteActivity";

    private RecyclerView chatRecyclerView;
    private EditText messageInput;
    private MaterialButton sendButton;
    private ChatAdapter chatAdapter;
    private List<ChatMessage> chatMessages;
    // Anotación: FUNCIONALIDAD AÑADIDA. Servicio de API para conectar con n8n.
    private N8nApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_asistente);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        chatRecyclerView = findViewById(R.id.chatRecyclerView);
        messageInput = findViewById(R.id.messageInput);
        sendButton = findViewById(R.id.sendButton);

        chatMessages = new ArrayList<>();
        chatAdapter = new ChatAdapter(chatMessages);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        chatRecyclerView.setLayoutManager(layoutManager);
        chatRecyclerView.setAdapter(chatAdapter);

        // Anotación: FUNCIONALIDAD AÑADIDA. Inicializamos el servicio de n8n.
        apiService = ApiClient.getN8nClient().create(N8nApiService.class);

        sendButton.setOnClickListener(v -> sendMessage());
    }

    private void sendMessage() {
        String messageText = messageInput.getText().toString().trim();
        if (messageText.isEmpty()) {
            return;
        }

        // 1. Añadir el mensaje del usuario a la UI
        addMessageToChat(messageText, true);
        messageInput.setText("");

        // 2. Crear la petición para n8n
        N8nRequest request = new N8nRequest(messageText);

        // 3. Realizar la llamada a la API de n8n
        apiService.sendMessage(request).enqueue(new Callback<N8nResponse>() {
            @Override
            public void onResponse(Call<N8nResponse> call, Response<N8nResponse> response) {
                // 4. Procesar la respuesta si es exitosa
                if (response.isSuccessful() && response.body() != null) {
                    String reply = response.body().getReply();
                    addMessageToChat(reply, false);
                } else {
                    // Manejar respuesta no exitosa o cuerpo vacío
                    Log.e(TAG, "Respuesta no exitosa o cuerpo vacío. Código: " + response.code());
                    addMessageToChat("Error: No he podido obtener una respuesta.", false);
                }
            }

            @Override
            public void onFailure(Call<N8nResponse> call, Throwable t) {
                // 5. Manejar el fallo en la llamada
                Log.e(TAG, "Fallo en la llamada a la API", t);
                addMessageToChat("Error de conexión. Revisa tu red o el servidor.", false);
            }
        });
    }

    // Anotación: FUNCIONALIDAD AÑADIDA. Método de ayuda para añadir mensajes a la lista.
    private void addMessageToChat(String text, boolean isUserMessage) {
        if (text == null || text.trim().isEmpty()) {
            return; // No añadir mensajes vacíos
        }
        runOnUiThread(() -> {
            chatMessages.add(new ChatMessage(text, isUserMessage));
            chatAdapter.notifyItemInserted(chatMessages.size() - 1);
            chatRecyclerView.scrollToPosition(chatMessages.size() - 1);
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
