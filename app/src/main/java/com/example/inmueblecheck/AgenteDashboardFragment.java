package com.example.inmueblecheck;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class AgenteDashboardFragment extends Fragment {

    private AgenteViewModel viewModel;
    private InmuebleAdapter adapter;
    private EditText etBuscar;
    private ImageButton btnFiltros;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_agente_dashboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        etBuscar = view.findViewById(R.id.etBuscarInmueble);
        btnFiltros = view.findViewById(R.id.btnFiltros);
        RecyclerView recyclerView = view.findViewById(R.id.recyclerViewInmuebles);

        viewModel = new ViewModelProvider(requireActivity()).get(AgenteViewModel.class);

        adapter = new InmuebleAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        // CLIC EN LA TARJETA -> NAVEGAR AL DETALLE
        adapter.setOnInmuebleClickListener(inmueble -> {
            Bundle bundle = new Bundle();
            bundle.putString("inmuebleId", inmueble.getUid());
            bundle.putString("direccion", inmueble.getDireccion());
            bundle.putDouble("precio", inmueble.getPrecio());
            bundle.putString("descripcion", inmueble.getDescripcion());
            bundle.putString("contacto", inmueble.getDatosContacto());
            bundle.putString("foto", inmueble.getFotoPortada());

            Navigation.findNavController(view).navigate(R.id.detalleInmuebleFragment, bundle);
        });

        // Obtenemos los inmuebles según el filtro aplicado en ViewModel
        viewModel.getInmuebles().observe(getViewLifecycleOwner(), inmuebles -> {
            if (inmuebles != null) {
                adapter.setInmuebles(inmuebles);
            }
        });

        // LÓGICA DE BÚSQUEDA POR TEXTO (Filtra mientras escribes)
        etBuscar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (adapter != null) {
                    // Si implementas filtro por texto en el adapter, se invoca aquí
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // LÓGICA DEL BOTÓN DE FILTROS (Popup)
        btnFiltros.setOnClickListener(v -> mostrarMenuFiltros(v));
    }

    private void mostrarMenuFiltros(View anchorView) {
        PopupMenu popup = new PopupMenu(getContext(), anchorView);
        popup.getMenuInflater().inflate(R.menu.menu_filtros, popup.getMenu());

        popup.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();

            if (id == R.id.filtro_venta) {
                viewModel.aplicarFiltro("Venta");
                Toast.makeText(getContext(), "Mostrando: Solo Venta", Toast.LENGTH_SHORT).show();
                return true;
            } else if (id == R.id.filtro_renta) {
                viewModel.aplicarFiltro("Renta");
                Toast.makeText(getContext(), "Mostrando: Solo Renta", Toast.LENGTH_SHORT).show();
                return true;
            } else if (id == R.id.filtro_todos) {
                viewModel.aplicarFiltro(null);
                Toast.makeText(getContext(), "Mostrando: Todos los inmuebles", Toast.LENGTH_SHORT).show();
                return true;
            }
            return false;
        });

        popup.show();
    }
}