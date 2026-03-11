package com.vistoria.app.data.repository;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.vistoria.app.data.model.Vistoria;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Repositório responsável por toda a persistência local de vistorias.
 * Usa SharedPreferences + Gson para armazenar/recuperar objetos JSON.
 *
 * Padrão Singleton para garantir instância única durante o ciclo de vida do app.
 */
public class VistoriaRepository {

    private static final String PREFS_NAME = "vistoria_prefs";
    private static final String KEY_VISTORIAS = "vistorias_lista";

    private static VistoriaRepository instance;

    private final SharedPreferences prefs;
    private final Gson gson;

    // ─── SINGLETON ────────────────────────────────────────
    private VistoriaRepository(Context context) {
        // Usa applicationContext para evitar memory leak
        this.prefs = context.getApplicationContext()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.gson = new Gson();
    }

    public static synchronized VistoriaRepository getInstance(Context context) {
        if (instance == null) {
            instance = new VistoriaRepository(context);
        }
        return instance;
    }

    // ─── CRUD ─────────────────────────────────────────────

    /**
     * Retorna todas as vistorias salvas, ordenadas por data de criação (mais recente primeiro).
     */
    public List<Vistoria> getTodasVistorias() {
        String json = prefs.getString(KEY_VISTORIAS, null);
        if (json == null) return new ArrayList<>();

        Type tipoLista = new TypeToken<List<Vistoria>>() {}.getType();
        List<Vistoria> lista = gson.fromJson(json, tipoLista);
        return lista != null ? lista : new ArrayList<>();
    }

    /**
     * Salva ou atualiza uma vistoria (baseado no ID).
     */
    public void salvarVistoria(Vistoria vistoria) {
        List<Vistoria> lista = getTodasVistorias();

        // Verifica se já existe (atualização)
        boolean encontrada = false;
        for (int i = 0; i < lista.size(); i++) {
            if (lista.get(i).getId().equals(vistoria.getId())) {
                lista.set(i, vistoria);
                encontrada = true;
                break;
            }
        }

        // Se não existe, adiciona no topo da lista
        if (!encontrada) {
            lista.add(0, vistoria);
        }

        persistirLista(lista);
    }

    /**
     * Remove uma vistoria pelo ID.
     */
    public void removerVistoria(String id) {
        List<Vistoria> lista = getTodasVistorias();
        lista.removeIf(v -> v.getId().equals(id));
        persistirLista(lista);
    }

    /**
     * Busca uma vistoria específica pelo ID.
     */
    public Vistoria buscarPorId(String id) {
        for (Vistoria v : getTodasVistorias()) {
            if (v.getId().equals(id)) return v;
        }
        return null;
    }

    /**
     * Serializa e persiste a lista no SharedPreferences.
     */
    private void persistirLista(List<Vistoria> lista) {
        String json = gson.toJson(lista);
        prefs.edit().putString(KEY_VISTORIAS, json).apply();
    }
}
