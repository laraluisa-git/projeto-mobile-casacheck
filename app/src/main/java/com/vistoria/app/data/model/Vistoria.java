package com.vistoria.app.data.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Modelo de dados principal que representa uma vistoria completa.
 * Implementa Serializable para ser passado entre Activities via Intent.
 */
public class Vistoria implements Serializable {

    // Identificador único da vistoria (timestamp de criação)
    private String id;

    // Dados básicos do imóvel
    private String nomeImovel;
    private String endereco;
    private String responsavel;
    private String data;

    // Coordenadas GPS capturadas no momento do cadastro
    private double latitude;
    private double longitude;

    // Status da vistoria: "Em andamento", "Concluída"
    private String status;

    // Lista de cômodos inspecionados
    private List<Comodo> comodos;

    // Protocolo gerado ao finalizar
    private String protocolo;

    // ─── CONSTRUTOR ───────────────────────────────────────
    public Vistoria() {
        this.id = String.valueOf(System.currentTimeMillis());
        this.status = "Em andamento";
        this.comodos = new ArrayList<>();
        this.latitude = 0.0;
        this.longitude = 0.0;
        this.protocolo = "VIS-" + this.id.substring(this.id.length() - 6);
    }

    // ─── GETTERS E SETTERS ────────────────────────────────
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getNomeImovel() { return nomeImovel; }
    public void setNomeImovel(String nomeImovel) { this.nomeImovel = nomeImovel; }

    public String getEndereco() { return endereco; }
    public void setEndereco(String endereco) { this.endereco = endereco; }

    public String getResponsavel() { return responsavel; }
    public void setResponsavel(String responsavel) { this.responsavel = responsavel; }

    public String getData() { return data; }
    public void setData(String data) { this.data = data; }

    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public List<Comodo> getComodos() { return comodos; }
    public void setComodos(List<Comodo> comodos) { this.comodos = comodos; }

    public String getProtocolo() { return protocolo; }
    public void setProtocolo(String protocolo) { this.protocolo = protocolo; }

    // Adiciona um cômodo à lista
    public void addComodo(Comodo comodo) {
        if (this.comodos == null) this.comodos = new ArrayList<>();
        this.comodos.add(comodo);
    }

    // Retorna se a localização foi capturada
    public boolean temLocalizacao() {
        return latitude != 0.0 && longitude != 0.0;
    }

    // Retorna coordenadas formatadas como string
    public String getCoordenadasFormatadas() {
        if (!temLocalizacao()) return "Não capturada";
        return String.format("%.4f° S, %.4f° O", Math.abs(latitude), Math.abs(longitude));
    }
}
