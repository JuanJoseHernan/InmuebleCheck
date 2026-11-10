package com.example.inmueblecheck;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class ConfiguracionFragment extends Fragment {

    private TextView tvUserName, tvUserEmail;
    private Button btnUpgrade, btnAbout, btnLogout;

    public ConfiguracionFragment() {
        // Constructor vacío requerido
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_configuracion, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvUserName = view.findViewById(R.id.tvConfigUserName);
        tvUserEmail = view.findViewById(R.id.tvConfigUserEmail);
        btnUpgrade = view.findViewById(R.id.btnConfigUpgrade);
        btnAbout = view.findViewById(R.id.btnConfigAbout);
        btnLogout = view.findViewById(R.id.btnConfigLogout);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            tvUserEmail.setText(user.getEmail()); // El email viene directo del login rápido
            cargarDatosUsuario(user.getUid());    // El nombre viene de Firestore
        }

        // Lógica del botón de subir de nivel
        btnUpgrade.setOnClickListener(v -> actualizarRolArrendador(user.getUid()));

        // Lógica de "Acerca de"
        btnAbout.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.aboutFragment);
        });

        // Lógica de "Cerrar Sesión"
        btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Navigation.findNavController(v).navigate(R.id.loginFragment);
        });
    }

    private void cargarDatosUsuario(String uid) {
        FirebaseFirestore.getInstance().collection("users").document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String nombre = documentSnapshot.getString("nombre");
                        String role = documentSnapshot.getString("role");

                        // Mostramos el nombre, o un texto por defecto si el usuario fue creado antes
                        if (nombre != null && !nombre.isEmpty()) {
                            tvUserName.setText(nombre);
                        } else {
                            tvUserName.setText("Usuario InmuebleCheck");
                        }

                        // Evaluamos el rol internamente para ocultar/mostrar el botón
                        if ("arrendador".equals(role) || "gerente".equals(role)) {
                            btnUpgrade.setVisibility(View.GONE);
                        } else {
                            btnUpgrade.setVisibility(View.VISIBLE);
                        }
                    }
                });
    }

    private void actualizarRolArrendador(String uid) {
        btnUpgrade.setEnabled(false);
        btnUpgrade.setText("Actualizando...");

        FirebaseFirestore.getInstance().collection("users").document(uid)
                .update("role", "arrendador")
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "¡Felicidades! Ahora eres Arrendador", Toast.LENGTH_LONG).show();
                    btnUpgrade.setVisibility(View.GONE);
                })
                .addOnFailureListener(e -> {
                    btnUpgrade.setEnabled(true);
                    btnUpgrade.setText("Convertirme en Arrendador");
                    Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}