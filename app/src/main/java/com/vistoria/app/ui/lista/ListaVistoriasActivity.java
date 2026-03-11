package com.vistoria.app.ui.lista;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;import com.vistoria.app.data.model.Vistoria;
import com.vistoria.app.R;
import com.vistoria.app.data.repository.VistoriaRepository;
import com.vistoria.app.ui.cadastro.CadastroVistoriaActivity;
import com.vistoria.app.ui.inspecao.InspecaoActivity;
import com.vistoria.app.util.NotificacaoHelper;
import com.google.android.material.appbar.MaterialToolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.content.SharedPreferences;

import java.util.List;

/**
 * Tela 1 – Lista de Vistorias
 *
 * Activity principal do aplicativo. Exibe todas as vistorias cadastradas
 * em um RecyclerView com cards no padrão Material Design.
 *
 * Ciclo de vida relevante:
 * - onResume(): atualiza a lista ao retornar de outra Activity
 */
public class ListaVistoriasActivity extends AppCompatActivity
        implements VistoriaAdapter.OnVistoriaClickListener {

    // View Binding (gerado automaticamente pelo Gradle)
    private RecyclerView recyclerView;
    private ExtendedFloatingActionButton fabNova;
    private LinearLayout tvVazio;
    // Adapter e repositório
    private MaterialToolbar toolbar;
    private VistoriaAdapter adapter;
    private VistoriaRepository repository;

    // ─── CICLO DE VIDA ────────────────────────────────────

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_vistorias);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Inicializa canal de notificação
        NotificacaoHelper.criarCanal(this);

        // Inicializa repositório local
        repository = VistoriaRepository.getInstance(this);

        // Vincula as views
        recyclerView = findViewById(R.id.recyclerViewVistorias);
        fabNova      = findViewById(R.id.fabNovaVistoria);
        tvVazio      = findViewById(R.id.tvListaVazia);

        configurarRecyclerView();
        configurarFab();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Atualiza lista sempre que a tela volta ao foco (ex: após salvar vistoria)
        carregarVistorias();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_lista, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.menu_sair) {

            SharedPreferences prefs = getSharedPreferences("login_prefs", MODE_PRIVATE);

            prefs.edit()
                    .putBoolean("usuario_logado", false)
                    .apply();

            Intent intent = new Intent(this, com.vistoria.app.ui.login.LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // ─── CONFIGURAÇÃO ─────────────────────────────────────

    /**
     * Configura o RecyclerView com LayoutManager vertical e adapter.
     */
    private void configurarRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new VistoriaAdapter(this, this);
        recyclerView.setAdapter(adapter);
    }

    /**
     * Configura o FAB para abrir a tela de cadastro de nova vistoria.
     */
    private void configurarFab() {
        fabNova.setOnClickListener(v -> {
            Intent intent = new Intent(this, CadastroVistoriaActivity.class);
            startActivity(intent);
        });
    }

    /**
     * Carrega as vistorias do repositório e atualiza o adapter.
     * Exibe mensagem de lista vazia quando não há registros.
     */
    private void carregarVistorias() {
        List<Vistoria> vistorias = repository.getTodasVistorias();

        if (vistorias.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            tvVazio.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            tvVazio.setVisibility(View.GONE);
            adapter.setVistorias(vistorias);
        }
    }

    // ─── INTERFACE DO ADAPTER ─────────────────────────────

    /**
     * Callback chamado quando o usuário toca em um card de vistoria.
     * Abre a tela de inspeção para continuar ou visualizar a vistoria.
     */
    @Override
    public void onVistoriaClick(Vistoria vistoria) {
        Intent intent = new Intent(this, InspecaoActivity.class);
        // Passa o ID da vistoria para a próxima Activity
        intent.putExtra(InspecaoActivity.EXTRA_VISTORIA_ID, vistoria.getId());
        startActivity(intent);
    }

    /**
     * Callback para exclusão de vistoria via swipe ou menu de contexto.
     */
    @Override
    public void onVistoriaDelete(Vistoria vistoria) {
        repository.removerVistoria(vistoria.getId());
        carregarVistorias();
    }
}
