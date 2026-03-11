package com.vistoria.app.data.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Modelo que representa um cômodo inspecionado dentro de uma vistoria.
 */
public class Comodo implements Serializable {

    // Nome do cômodo (ex: "Sala", "Cozinha", "Quarto", "Banheiro")
    private String nome;

    // Observações textuais sobre o cômodo
    private String observacoes;

    // Lista de caminhos (URI) das fotos tiradas neste cômodo
    private List<String> fotos;

    // Status do cômodo: "OK", "Atenção", "Reprovado"
    private String status;

    // ─── CONSTRUTOR ───────────────────────────────────────
    public Comodo() {
        this.fotos = new ArrayList<>();
        this.status = "OK";
    }

    public Comodo(String nome) {
        this();
        this.nome = nome;
    }

    // ─── GETTERS E SETTERS ────────────────────────────────
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getObservacoes() { return observacoes; }
    public void setObservacoes(String observacoes) { this.observacoes = observacoes; }

    public List<String> getFotos() { return fotos; }
    public void setFotos(List<String> fotos) { this.fotos = fotos; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    // Adiciona o caminho de uma foto
    public void addFoto(String fotoPath) {
        if (this.fotos == null) this.fotos = new ArrayList<>();
        this.fotos.add(fotoPath);
    }

    // Retorna número de fotos
    public int getNumeroFotos() {
        return fotos == null ? 0 : fotos.size();
    }
}
