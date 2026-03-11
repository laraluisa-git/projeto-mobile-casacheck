package com.vistoria.app.ui.lista;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.vistoria.app.R;
import com.vistoria.app.data.model.Vistoria;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter do RecyclerView para exibir a lista de vistorias.
 *
 * Padrão ViewHolder para reaproveitamento de views e melhor desempenho.
 */
public class VistoriaAdapter extends RecyclerView.Adapter<VistoriaAdapter.VistoriaViewHolder> {

    // Interface para comunicação entre adapter e activity (padrão Observer)
    public interface OnVistoriaClickListener {
        void onVistoriaClick(Vistoria vistoria);
        void onVistoriaDelete(Vistoria vistoria);
    }

    private final Context context;
    private List<Vistoria> vistorias;
    private final OnVistoriaClickListener listener;

    // ─── CONSTRUTOR ───────────────────────────────────────
    public VistoriaAdapter(Context context, OnVistoriaClickListener listener) {
        this.context   = context;
        this.listener  = listener;
        this.vistorias = new ArrayList<>();
    }

    // ─── RECYCLERVIEW METHODS ─────────────────────────────

    @NonNull
    @Override
    public VistoriaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_vistoria, parent, false);
        return new VistoriaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VistoriaViewHolder holder, int position) {
        Vistoria vistoria = vistorias.get(position);
        holder.bind(vistoria);
    }

    @Override
    public int getItemCount() {
        return vistorias.size();
    }

    // Atualiza a lista e notifica o RecyclerView
    public void setVistorias(List<Vistoria> novaLista) {
        this.vistorias = novaLista;
        notifyDataSetChanged();
    }

    // ─── VIEW HOLDER ──────────────────────────────────────

    /**
     * Representa um item da lista (um card de vistoria).
     */
    class VistoriaViewHolder extends RecyclerView.ViewHolder {

        private final CardView cardView;
        private final TextView tvNomeImovel;
        private final TextView tvEndereco;
        private final TextView tvData;
        private final TextView tvResponsavel;
        private final Chip chipStatus;
        private final ImageButton btnExcluir;
        private final View barraStatus;

        VistoriaViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView      = itemView.findViewById(R.id.cardVistoria);
            tvNomeImovel  = itemView.findViewById(R.id.tvNomeImovel);
            tvEndereco    = itemView.findViewById(R.id.tvEndereco);
            tvData        = itemView.findViewById(R.id.tvData);
            tvResponsavel = itemView.findViewById(R.id.tvResponsavel);
            chipStatus    = itemView.findViewById(R.id.chipStatus);
            btnExcluir    = itemView.findViewById(R.id.btnExcluir);
            barraStatus   = itemView.findViewById(R.id.barraStatus);
        }

        /**
         * Liga os dados do modelo de vistoria às views do card.
         */
        void bind(Vistoria vistoria) {
            tvNomeImovel.setText(vistoria.getNomeImovel());
            tvEndereco.setText(vistoria.getEndereco());
            tvData.setText(vistoria.getData());
            tvResponsavel.setText("Resp.: " + vistoria.getResponsavel());
            chipStatus.setText(vistoria.getStatus());

            // Define cor da barra lateral e do chip conforme status
            aplicarCoresStatus(vistoria.getStatus());

            // Click no card → abre inspeção
            cardView.setOnClickListener(v -> {
                if (listener != null) listener.onVistoriaClick(vistoria);
            });

            // Click no botão excluir
            btnExcluir.setOnClickListener(v -> {
                if (listener != null) listener.onVistoriaDelete(vistoria);
            });
        }

        /**
         * Aplica cores dinâmicas de acordo com o status da vistoria.
         */
        private void aplicarCoresStatus(String status) {
            int corBarra, corChipBg, corChipTexto;

            switch (status) {
                case "Concluída":
                    corBarra     = ContextCompat.getColor(context, R.color.status_concluida);
                    corChipBg    = ContextCompat.getColor(context, R.color.status_concluida_bg);
                    corChipTexto = ContextCompat.getColor(context, R.color.status_concluida);
                    break;
                case "Pendente":
                    corBarra     = ContextCompat.getColor(context, R.color.status_pendente);
                    corChipBg    = ContextCompat.getColor(context, R.color.status_pendente_bg);
                    corChipTexto = ContextCompat.getColor(context, R.color.status_pendente);
                    break;
                default: // "Em andamento"
                    corBarra     = ContextCompat.getColor(context, R.color.md_primary);
                    corChipBg    = ContextCompat.getColor(context, R.color.md_primary_light);
                    corChipTexto = ContextCompat.getColor(context, R.color.md_primary);
                    break;
            }

            barraStatus.setBackgroundColor(corBarra);
            chipStatus.setChipBackgroundColor(
                    android.content.res.ColorStateList.valueOf(corChipBg));
            chipStatus.setTextColor(corChipTexto);
        }
    }
}
