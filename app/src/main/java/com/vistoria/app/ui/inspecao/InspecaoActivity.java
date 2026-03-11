package com.vistoria.app.ui.inspecao;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.vistoria.app.R;
import com.vistoria.app.data.model.Comodo;
import com.vistoria.app.data.model.Vistoria;
import com.vistoria.app.data.repository.VistoriaRepository;
import com.vistoria.app.util.NotificacaoHelper;
import com.vistoria.app.util.PermissaoHelper;
import androidx.core.content.FileProvider;
import com.vistoria.app.util.GeradorLaudoPdf;

import java.io.File;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;



/**
 * Tela 3 – Inspeção do Imóvel
 *
 * Activity central da vistoria. Permite:
 * - Selecionar cômodo via chips (dropdown)
 * - Registrar observações textuais por cômodo
 * - Tirar fotos com a câmera nativa (Intent ACTION_IMAGE_CAPTURE)
 * - Detectar impactos via acelerômetro (SensorEventListener)
 * - Finalizar vistoria e gerar notificação
 */
public class InspecaoActivity extends AppCompatActivity implements SensorEventListener {

    // Chave para receber o ID da vistoria via Intent
    public static final String EXTRA_VISTORIA_ID = "vistoria_id";

    // ── Views ──────────────────────────────────────────────
    private ChipGroup chipGroupComodos;
    private TextInputEditText etObservacoes;
    private Button btnFoto, btnAdicionarComodo, btnFinalizar, btnGerarLaudo;
    private RecyclerView rvFotos;
    private TextView tvTituloInspecao, tvComodoAtual, tvAcelerometro;
    private LinearLayout layoutAcelerometro;

    // ── Dados ──────────────────────────────────────────────
    private Vistoria vistoria;
    private Comodo comodoAtual;
    private VistoriaRepository repository;
    private FotoAdapter fotoAdapter;

    // ── Câmera ─────────────────────────────────────────────
    private Uri fotoUri; // URI do arquivo temporário da câmera
    private String caminhoFotoAtual;

    // Launcher moderno para resultado da câmera (substitui onActivityResult)
    private final ActivityResultLauncher<Intent> cameraLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == RESULT_OK) {
                            // Foto tirada com sucesso – adiciona ao cômodo atual
                            if (caminhoFotoAtual != null) {
                                comodoAtual.addFoto(caminhoFotoAtual);
                                fotoAdapter.addFoto(caminhoFotoAtual);
                                Toast.makeText(this, "Foto adicionada!", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

    // ── Acelerômetro ───────────────────────────────────────
    private SensorManager sensorManager;
    private Sensor acelerometro;
    private static final float LIMIAR_IMPACTO = 15.0f; // m/s²
    private long ultimoImpacto = 0;

    // ── Lista de cômodos disponíveis ───────────────────────
    private static final List<String> COMODOS_PADRAO = Arrays.asList(
            "Sala de Estar", "Cozinha", "Quarto Principal",
            "Quarto 2", "Banheiro Social", "Banheiro Suíte",
            "Varanda", "Garagem", "Área de Serviço", "Outro"
    );

    // ─── CICLO DE VIDA ────────────────────────────────────

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inspecao);

        repository = VistoriaRepository.getInstance(this);

        // Recupera a vistoria pelo ID passado via Intent
        String vistoriaId = getIntent().getStringExtra(EXTRA_VISTORIA_ID);
        vistoria = repository.buscarPorId(vistoriaId);

        if (vistoria == null) {
            Toast.makeText(this, "Erro: vistoria não encontrada", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        configurarToolbar();
        vincularViews();
        inicializarAcelerometro();
        configurarChipsComodos();
        configurarBotaoFoto();
        configurarBotaoAdicionarComodo();
        configurarBotaoFinalizar();
        configurarBotaoGerarLaudo();
        selecionarComodo("Sala de Estar"); // cômodo padrão ao abrir
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Registra o listener do acelerômetro ao retornar à tela
        if (acelerometro != null) {
            sensorManager.registerListener(this, acelerometro,
                    SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Para o acelerômetro para economizar bateria
        sensorManager.unregisterListener(this);
        // Salva estado atual da inspeção
        salvarComodoAtual();
    }

    // ─── CONFIGURAÇÃO ─────────────────────────────────────

    private void configurarToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Inspeção");
            getSupportActionBar().setSubtitle(vistoria.getNomeImovel());
        }
    }

    private void vincularViews() {
        chipGroupComodos    = findViewById(R.id.chipGroupComodos);
        etObservacoes       = findViewById(R.id.etObservacoes);
        btnFoto             = findViewById(R.id.btnFoto);
        btnAdicionarComodo  = findViewById(R.id.btnAdicionarComodo);
        btnFinalizar        = findViewById(R.id.btnFinalizar);
        btnGerarLaudo       = findViewById(R.id.btnGerarLaudo);
        rvFotos             = findViewById(R.id.rvFotos);
        tvTituloInspecao    = findViewById(R.id.tvTituloInspecao);
        tvComodoAtual       = findViewById(R.id.tvComodoAtual);
        tvAcelerometro      = findViewById(R.id.tvAcelerometro);
        layoutAcelerometro  = findViewById(R.id.layoutAcelerometro);

        // Configura RecyclerView horizontal para miniaturas de fotos
        rvFotos.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        fotoAdapter = new FotoAdapter(this);
        rvFotos.setAdapter(fotoAdapter);

        tvTituloInspecao.setText("Vistoria: " + vistoria.getNomeImovel());
    }

    /**
     * Inicializa o SensorManager e obtém o sensor de acelerômetro.
     * O acelerômetro detecta movimentos bruscos no dispositivo.
     */
    private void inicializarAcelerometro() {
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        acelerometro  = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        if (acelerometro == null) {
            // Dispositivo sem acelerômetro – oculta o painel
            layoutAcelerometro.setVisibility(View.GONE);
        }
    }

    /**
     * Popula os chips de seleção de cômodo dinamicamente.
     */
    private void configurarChipsComodos() {
        chipGroupComodos.setSingleSelection(true);
        chipGroupComodos.removeAllViews();

        for (String nomeComodo : COMODOS_PADRAO) {
            Chip chip = new Chip(this);
            chip.setText(nomeComodo);
            chip.setCheckable(true);
            chip.setChipBackgroundColorResource(R.color.chip_selector);
            chip.setTextColor(getColorStateList(R.color.chip_text_selector));
            chip.setOnClickListener(v -> selecionarComodo(nomeComodo));
            chipGroupComodos.addView(chip);
        }
    }

    /**
     * Seleciona um cômodo: salva observações do anterior e carrega dados do novo.
     */
    private void selecionarComodo(String nomeComodo) {
        // Salva observações do cômodo anterior antes de trocar
        if (comodoAtual != null) {
            salvarComodoAtual();
        }

        // Busca ou cria o cômodo
        comodoAtual = buscarOuCriarComodo(nomeComodo);
        tvComodoAtual.setText(nomeComodo);

        // Carrega dados do cômodo selecionado
        etObservacoes.setText(comodoAtual.getObservacoes());
        fotoAdapter.setFotos(comodoAtual.getFotos());
    }

    /**
     * Busca cômodo existente na vistoria ou cria um novo.
     */
    private Comodo buscarOuCriarComodo(String nome) {
        if (vistoria.getComodos() != null) {
            for (Comodo c : vistoria.getComodos()) {
                if (c.getNome().equals(nome)) return c;
            }
        }
        // Cria novo cômodo e adiciona à vistoria
        Comodo novo = new Comodo(nome);
        vistoria.addComodo(novo);
        return novo;
    }

    /**
     * Salva as observações do cômodo atual no objeto de dados.
     */
    private void salvarComodoAtual() {
        if (comodoAtual != null && etObservacoes.getText() != null) {
            comodoAtual.setObservacoes(etObservacoes.getText().toString());
        }
        repository.salvarVistoria(vistoria);
    }

    // ─── CÂMERA ───────────────────────────────────────────

    /**
     * Configura o botão de câmera.
     * Cria arquivo temporário, obtém URI via FileProvider e dispara Intent.
     */
    private void configurarBotaoFoto() {
        btnFoto.setOnClickListener(v -> {
            if (PermissaoHelper.temPermissaoCamera(this)) {
                abrirCamera();
            } else {
                PermissaoHelper.solicitarPermissaoCamera(this);
            }
        });
    }

    /**
     * Cria arquivo de imagem temporário e abre a câmera nativa.
     * Usa FileProvider para segurança no compartilhamento da URI.
     */
    private void abrirCamera() {
        try {
            File fotoFile = criarArquivoFoto();
            caminhoFotoAtual = fotoFile.getAbsolutePath();

            // FileProvider converte o caminho em URI segura para compartilhar com a câmera
            fotoUri = FileProvider.getUriForFile(
                    this,
                    getApplicationContext().getPackageName() + ".fileprovider",
                    fotoFile
            );

            Intent intentCamera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intentCamera.putExtra(MediaStore.EXTRA_OUTPUT, fotoUri);

            // Concede permissão de escrita temporária para o app da câmera
            intentCamera.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

            cameraLauncher.launch(intentCamera);

        } catch (IOException e) {
            Toast.makeText(this, "Erro ao criar arquivo de foto: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Cria um arquivo de imagem com nome único baseado em timestamp.
     * Salvo no diretório privado de fotos do app (não requer permissão de escrita).
     */
    private File criarArquivoFoto() throws IOException {
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                .format(new Date());
        String nomeArquivo = "VISTORIA_" + timestamp;

        // getExternalFilesDir() não requer WRITE_EXTERNAL_STORAGE no Android 10+
        File diretorioFotos = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(nomeArquivo, ".jpg", diretorioFotos);
    }

    // ─── ACELERÔMETRO ─────────────────────────────────────

    /**
     * Callback do SensorEventListener – chamado a cada leitura do acelerômetro.
     * Detecta impactos calculando a magnitude da aceleração resultante.
     *
     * Fórmula: |a| = √(ax² + ay² + az²)
     * Se |a| - g (gravidade ~9.8) > LIMIAR_IMPACTO → impacto detectado
     */
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() != Sensor.TYPE_ACCELEROMETER) return;

        float ax = event.values[0];
        float ay = event.values[1];
        float az = event.values[2];

        // Magnitude total da aceleração
        float magnitude = (float) Math.sqrt(ax * ax + ay * ay + az * az);

        // Atualiza display do acelerômetro
        tvAcelerometro.setText(String.format(Locale.getDefault(),
                "Acelerômetro: %.1f m/s²", magnitude));

        // Detecta impacto (subtrai gravidade para eliminar leitura estática)
        float aceleracaoLiquida = Math.abs(magnitude - SensorManager.GRAVITY_EARTH);
        long agora = System.currentTimeMillis();

        if (aceleracaoLiquida > LIMIAR_IMPACTO && (agora - ultimoImpacto) > 2000) {
            ultimoImpacto = agora;
            // Vibra visualmente e alerta o usuário sobre impacto detectado
            tvAcelerometro.setTextColor(getColor(R.color.status_pendente));
            tvAcelerometro.setText(String.format("⚠ Impacto detectado! (%.1f m/s²)", magnitude));

            // Sugere anotar a vibração
            if (etObservacoes.getText() != null) {
                String obs = etObservacoes.getText().toString();
                if (!obs.contains("[vibração]")) {
                    etObservacoes.append("\n[Vibração detectada pelo sensor]");
                }
            }
        } else {
            tvAcelerometro.setTextColor(getColor(R.color.md_primary));
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Não é necessário tratar mudanças de precisão para este caso de uso
    }

    // ─── BOTÕES DE AÇÃO ───────────────────────────────────

    /**
     * Abre diálogo para o usuário adicionar um cômodo personalizado.
     */
    private void configurarBotaoAdicionarComodo() {
        btnAdicionarComodo.setOnClickListener(v -> {
            // Cria diálogo com campo de texto
            View dialogView = getLayoutInflater().inflate(R.layout.dialog_novo_comodo, null);
            AutoCompleteTextView actvComodo = dialogView.findViewById(R.id.actvNovoComodo);

            // Sugestões de cômodos no autocomplete
            ArrayAdapter<String> autoAdapter = new ArrayAdapter<>(
                    this, android.R.layout.simple_dropdown_item_1line, COMODOS_PADRAO);
            actvComodo.setAdapter(autoAdapter);

            new AlertDialog.Builder(this)
                    .setTitle("Adicionar Cômodo")
                    .setView(dialogView)
                    .setPositiveButton("Adicionar", (dialog, which) -> {
                        String nome = actvComodo.getText().toString().trim();
                        if (!nome.isEmpty()) {
                            selecionarComodo(nome);
                            // Marca o chip correspondente se existir
                            marcarChip(nome);
                            Toast.makeText(this, "Cômodo \"" + nome + "\" adicionado!",
                                    Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("Cancelar", null)
                    .show();
        });
    }

    private void marcarChip(String nomeComodo) {
        for (int i = 0; i < chipGroupComodos.getChildCount(); i++) {
            Chip chip = (Chip) chipGroupComodos.getChildAt(i);
            if (chip.getText().toString().equals(nomeComodo)) {
                chip.setChecked(true);
                return;
            }
        }
    }

    /**
     * Finaliza a vistoria: muda status, salva e dispara notificação.
     */
    private void configurarBotaoFinalizar() {
        btnFinalizar.setOnClickListener(v -> {
            salvarComodoAtual();
            vistoria.setStatus("Concluída");
            repository.salvarVistoria(vistoria);

            // Dispara notificação local
            NotificacaoHelper.notificarVistoriaConcluida(
                    this,
                    vistoria.getNomeImovel(),
                    vistoria.getProtocolo()
            );

            Toast.makeText(this,
                    "Vistoria concluída! Protocolo: " + vistoria.getProtocolo(),
                    Toast.LENGTH_LONG).show();

            // Volta para a lista
            finish();
        });
    }

    /**
     * Exibe um diálogo resumo do laudo antes de "gerar".
     * (Em produção, aqui seria gerado um PDF ou compartilhado via Intent)
     */
    private void configurarBotaoGerarLaudo() {
        btnGerarLaudo.setOnClickListener(v -> {
            salvarComodoAtual();

            try {
                File arquivoPdf = GeradorLaudoPdf.gerar(this, vistoria);
                compartilharPdf(arquivoPdf);

            } catch (Exception e) {
                Toast.makeText(this,
                        "Erro ao gerar laudo: " + e.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    /**
     * Compartilha o texto do laudo via Intent de compartilhamento nativo do Android.
     */
    private void compartilharPdf(File arquivoPdf) {
        Uri uri = FileProvider.getUriForFile(
                this,
                getPackageName() + ".fileprovider",
                arquivoPdf
        );

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("application/pdf");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT,
                "Laudo de Vistoria – " + vistoria.getNomeImovel());
        shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        startActivity(Intent.createChooser(shareIntent, "Compartilhar Laudo PDF"));
    }

    // ─── PERMISSÕES ───────────────────────────────────────

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                            @NonNull String[] permissions,
                                            @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PermissaoHelper.REQUEST_CAMERA) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                abrirCamera();
            } else {
                Toast.makeText(this, "Permissão de câmera negada.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // ─── NAVEGAÇÃO ────────────────────────────────────────

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            salvarComodoAtual();
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
