package com.example.inmueblecheck;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class InmuebleAdapter extends RecyclerView.Adapter<InmuebleAdapter.InmuebleViewHolder> {

    private List<Inmueble> listaInmuebles = new ArrayList<>();
    private OnInmuebleClickListener clickListener;
    private OnEditarClickListener editarListener;
    private OnEstadoClickListener estadoListener;

    public interface OnInmuebleClickListener { void onInmuebleClick(Inmueble inmueble); }
    public interface OnEditarClickListener { void onEditarClick(Inmueble inmueble); }
    public interface OnEstadoClickListener { void onEstadoClick(Inmueble inmueble); }

    public void setOnInmuebleClickListener(OnInmuebleClickListener listener) { this.clickListener = listener; }
    public void setOnEditarClickListener(OnEditarClickListener listener) { this.editarListener = listener; }
    public void setOnEstadoClickListener(OnEstadoClickListener listener) { this.estadoListener = listener; }

    public void setInmuebles(List<Inmueble> inmuebles) {
        this.listaInmuebles = inmuebles;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public InmuebleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_inmueble, parent, false);
        return new InmuebleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull InmuebleViewHolder holder, int position) {
        holder.bind(listaInmuebles.get(position), clickListener, editarListener, estadoListener);
    }

    @Override
    public int getItemCount() {
        return listaInmuebles != null ? listaInmuebles.size() : 0;
    }

    static class InmuebleViewHolder extends RecyclerView.ViewHolder {
        private TextView tvDireccion, tvPrecio, tvTipo;
        private ImageView ivFoto;
        private ImageButton btnEditar, btnEstado;

        public InmuebleViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDireccion = itemView.findViewById(R.id.tvDireccion);
            tvPrecio = itemView.findViewById(R.id.tvPrecio);
            tvTipo = itemView.findViewById(R.id.tvStatus);
            ivFoto = itemView.findViewById(R.id.ivInmuebleFoto);
            btnEditar = itemView.findViewById(R.id.btnEditar);
            btnEstado = itemView.findViewById(R.id.btnEstado);
        }

        public void bind(Inmueble inmueble, OnInmuebleClickListener clickListener,
                         OnEditarClickListener editListener, OnEstadoClickListener stateListener) {
            Context context = itemView.getContext();

            tvDireccion.setText(inmueble.getDireccion());
            NumberFormat format = NumberFormat.getCurrencyInstance(Locale.getDefault());
            tvPrecio.setText(format.format(inmueble.getPrecio()));

            String tipo = inmueble.getTipoTransaccion() != null ? inmueble.getTipoTransaccion() : "N/A";
            tvTipo.setText(tipo.toUpperCase());

            if ("Venta".equalsIgnoreCase(tipo)) {
                tvTipo.setTextColor(Color.parseColor("#D32F2F"));
                tvTipo.setBackgroundColor(Color.parseColor("#FFEBEE"));
            } else {
                tvTipo.setTextColor(Color.parseColor("#1976D2"));
                tvTipo.setBackgroundColor(Color.parseColor("#E3F2FD"));
            }

            if (inmueble.getFotoPortada() != null && !inmueble.getFotoPortada().isEmpty()) {
                Glide.with(context).load(inmueble.getFotoPortada()).centerCrop().into(ivFoto);
            } else {
                ivFoto.setImageResource(R.drawable.ic_home_black);
            }

            // Lógica Botón Editar
            if (editListener != null) {
                btnEditar.setVisibility(View.VISIBLE);
                btnEditar.setOnClickListener(v -> editListener.onEditarClick(inmueble));
            } else {
                btnEditar.setVisibility(View.GONE);
            }

            // Lógica Botón Estado (Historial)
            if (stateListener != null) {
                btnEstado.setVisibility(View.VISIBLE);
                btnEstado.setOnClickListener(v -> stateListener.onEstadoClick(inmueble));

                if ("disponible".equals(inmueble.getEstado())) {
                    btnEstado.setImageResource(android.R.drawable.checkbox_on_background);
                } else {
                    btnEstado.setImageResource(android.R.drawable.ic_menu_revert);
                }
            } else {
                btnEstado.setVisibility(View.GONE);
            }

            itemView.setOnClickListener(v -> {
                if (clickListener != null) clickListener.onInmuebleClick(inmueble);
            });
        }
    }
}