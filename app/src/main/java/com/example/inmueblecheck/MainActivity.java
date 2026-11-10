package com.example.inmueblecheck;

import android.os.Bundle;
import android.transition.TransitionManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {

    private NavController navController;
    private AgenteViewModel agenteViewModel;
    private BottomNavigationView bottomNavigationView;
    private MaterialCardView bottomNavContainer;

    private boolean isModoArrendadorActivo = false;
    private boolean tieneRolArrendador = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        bottomNavContainer = findViewById(R.id.bottomNavContainer);
        bottomNavigationView = findViewById(R.id.bottom_navigation);

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        navController = navHostFragment.getNavController();

        // --- VERIFICACIÓN DE SESIÓN ACTIVA (AUTO-LOGIN) ---
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            androidx.navigation.NavGraph navGraph = navController.getNavInflater().inflate(R.xml.nav_auth);
            navGraph.setStartDestination(R.id.agenteDashboardFragment);
            navController.setGraph(navGraph);
        }

        agenteViewModel = new ViewModelProvider(this).get(AgenteViewModel.class);

        // Ocultar la pastilla en login y registro
        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            int id = destination.getId();
            if (id == R.id.loginFragment || id == R.id.registerFragment) {
                bottomNavContainer.setVisibility(View.GONE);
                if (getSupportActionBar() != null) getSupportActionBar().hide();
            } else {
                bottomNavContainer.setVisibility(View.VISIBLE);
                if (getSupportActionBar() != null) getSupportActionBar().show();
                configurarMenuInferior();
            }
        });

        // Configurar los clics de la pastilla mutante
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            // --- BOTONES MODO ARRENDATARIO ---
            if (id == R.id.nav_explorar) {
                agenteViewModel.aplicarFiltro(null);
                navController.popBackStack(R.id.agenteDashboardFragment, false);
                navController.navigate(R.id.agenteDashboardFragment);
                return true;
            } else if (id == R.id.nav_mapa) {
                navController.navigate(R.id.mapaFragment);
                return true;
            } else if (id == R.id.nav_mensajes) {
                navController.navigate(R.id.listaChatsFragment);
                return true;
            }

            // --- TOGGLE DE MUTACIÓN: ACTIVAR MODO ARRENDADOR ---
            else if (id == R.id.nav_toggle_arrendador) {
                activarModoArrendador();
                return true;
            }

            // --- TOGGLE DE MUTACIÓN: VOLVER A MODO ARRENDATARIO ---
            else if (id == R.id.nav_toggle_arrendatario) {
                activarModoArrendatario();
                return true;
            }

            // --- BOTONES MODO ARRENDADOR ---
            else if (id == R.id.nav_mis_propiedades) {
                navController.popBackStack(R.id.gerenteDashboardFragment, false);
                navController.navigate(R.id.gerenteDashboardFragment);
                return true;
            } else if (id == R.id.nav_historial) {
                navController.navigate(R.id.historialFragment);
                return true;
            } else if (id == R.id.nav_crear_inmueble) {
                navController.navigate(R.id.crearInmuebleFragment);
                return true;
            }

            // --- COMÚN ---
            else if (id == R.id.nav_configuracion) {
                navController.navigate(R.id.configuracionFragment);
                return true;
            }

            return false;
        });
    }

    private void activarModoArrendador() {
        if (isModoArrendadorActivo) return;
        isModoArrendadorActivo = true;

        // Animación de transición en la pastilla
        TransitionManager.beginDelayedTransition(bottomNavContainer);

        bottomNavigationView.getMenu().clear();
        bottomNavigationView.inflateMenu(R.menu.menu_bottom_nav_arrendador);

        // Navegamos por defecto a Mis Propiedades
        navController.popBackStack(R.id.gerenteDashboardFragment, false);
        navController.navigate(R.id.gerenteDashboardFragment);
    }

    private void activarModoArrendatario() {
        if (!isModoArrendadorActivo) return;
        isModoArrendadorActivo = false;

        // Animación de transición en la pastilla
        TransitionManager.beginDelayedTransition(bottomNavContainer);

        bottomNavigationView.getMenu().clear();
        bottomNavigationView.inflateMenu(R.menu.menu_bottom_nav_arrendatario);

        Menu menu = bottomNavigationView.getMenu();
        MenuItem itemToggle = menu.findItem(R.id.nav_toggle_arrendador);
        if (itemToggle != null) {
            itemToggle.setVisible(tieneRolArrendador);
        }

        // Navegamos por defecto a Explorar
        agenteViewModel.aplicarFiltro(null);
        navController.popBackStack(R.id.agenteDashboardFragment, false);
        navController.navigate(R.id.agenteDashboardFragment);
    }

    private void configurarMenuInferior() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        FirebaseFirestore.getInstance().collection("users").document(user.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String role = documentSnapshot.getString("role");
                        tieneRolArrendador = role != null && (role.equalsIgnoreCase("arrendador") || role.equalsIgnoreCase("gerente"));

                        if (!isModoArrendadorActivo) {
                            Menu menu = bottomNavigationView.getMenu();
                            MenuItem itemToggle = menu.findItem(R.id.nav_toggle_arrendador);

                            if (itemToggle != null) {
                                itemToggle.setVisible(tieneRolArrendador);
                            } else {
                                // Si por alguna razón el menú no se había inflado con el nuevo XML, lo forzamos
                                bottomNavigationView.getMenu().clear();
                                bottomNavigationView.inflateMenu(R.menu.menu_bottom_nav_arrendatario);
                                bottomNavigationView.getMenu().findItem(R.id.nav_toggle_arrendador).setVisible(tieneRolArrendador);
                            }
                        }
                    }
                });
    }

    @Override
    public boolean onSupportNavigateUp() {
        return navController.navigateUp() || super.onSupportNavigateUp();
    }
}