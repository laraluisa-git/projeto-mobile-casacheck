package com.vistoria.app.ui.inspecao;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.vistoria.app.R;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Adapter para exibir miniaturas (thumbnails) das fotos tiradas em um cômodo.
 * Usa Glide para carregamento eficiente das imagens com cache automático.
 */
public class FotoAdapter extends RecyclerView.Adapter<FotoAdapter.FotoViewHolder> {

    private final Context context;
    private List<String> fotos;

    public FotoAdapter(Context context) {
        this.context = context;
        this.fotos   = new ArrayList<>();
    }

    @NonNull
    @Override
    public FotoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_foto, parent, false);
        return new FotoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FotoViewHolder holder, int position) {
        String caminho = fotos.get(position);

        // Glide carrega a imagem de forma assíncrona com placeholder e tratamento de erro
        Glide.with(context)
                .load(new File(caminho))
                .placeholder(R.drawable.ic_camera_placeholder)
                .error(R.drawable.ic_camera_placeholder)
                .centerCrop()
                .into(holder.ivFoto);
    }

    @Override
    public int getItemCount() {
        return fotos.size();
    }

    /** Adiciona uma foto ao final da lista. */
    public void addFoto(String caminho) {
        fotos.add(caminho);
        notifyItemInserted(fotos.size() - 1);
    }

    /** Substitui toda a lista de fotos. */
    public void setFotos(List<String> novasFotos) {
        this.fotos = novasFotos != null ? new ArrayList<>(novasFotos) : new ArrayList<>();
        notifyDataSetChanged();
    }

    // ─── VIEW HOLDER ──────────────────────────────────────

    static class FotoViewHolder extends RecyclerView.ViewHolder {
        final ImageView ivFoto;

        FotoViewHolder(@NonNull View itemView) {
            super(itemView);
            ivFoto = itemView.findViewById(R.id.ivFoto);
        }
    }
}
