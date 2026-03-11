package com.vistoria.app.ui.cadastro;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.vistoria.app.R;
import com.vistoria.app.data.model.Vistoria;
import com.vistoria.app.data.repository.VistoriaRepository;
import com.vistoria.app.ui.inspecao.InspecaoActivity;
import com.vistoria.app.util.PermissaoHelper;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Tela 2 – Cadastro da Vistoria
 *
 * Formulário para registro dos dados do imóvel.
 * Integra o sensor de GPS via FusedLocationProviderClient
 * para captura automática de coordenadas.
 */
public class CadastroVistoriaActivity extends AppCompatActivity {

    // Referências às views do formulário
    private TextInputEditText etImovel, etEndereco, etResponsavel, etData;
    private TextInputLayout tilImovel, tilEndereco, tilResponsavel, tilData;
    private Button btnCapturarLocalizacao, btnAvancar;

    // GPS – FusedLocationProviderClient (recomendado pelo Google)
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;

    // Armazena as coordenadas capturadas
    private double latitudeCapturada = 0.0;
    private double longitudeCapturada = 0.0;

    // Repositório para persistência
    private VistoriaRepository repository;

    // ─── CICLO DE VIDA ────────────────────────────────────

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro_vistoria);

        repository = VistoriaRepository.getInstance(this);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        configurarToolbar();
        vincularViews();
        preencherDataAtual();
        configurarBotaoLocalizacao();
        configurarBotaoAvancar();
        configurarLocationCallback();
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Para as atualizações de localização quando a activity sai do foco
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }

    // ─── CONFIGURAÇÃO ─────────────────────────────────────

    private void configurarToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Nova Vistoria");
        }
    }

    private void vincularViews() {
        etImovel      = findViewById(R.id.etImovel);
        etEndereco    = findViewById(R.id.etEndereco);
        etResponsavel = findViewById(R.id.etResponsavel);
        etData        = findViewById(R.id.etData);

        tilImovel      = findViewById(R.id.tilImovel);
        tilEndereco    = findViewById(R.id.tilEndereco);
        tilResponsavel = findViewById(R.id.tilResponsavel);
        tilData        = findViewById(R.id.tilData);

        btnCapturarLocalizacao = findViewById(R.id.btnCapturarLocalizacao);
        btnAvancar             = findViewById(R.id.btnAvancar);
    }

    /**
     * Preenche o campo de data com a data atual no formato dd/MM/yyyy.
     */
    private void preencherDataAtual() {
        String dataHoje = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                .format(new Date());
        etData.setText(dataHoje);
    }

    /**
     * Configura o LocationCallback que recebe atualizações do GPS.
     */
    private void configurarLocationCallback() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                Location location = locationResult.getLastLocation();
                if (location != null) {
                    latitudeCapturada  = location.getLatitude();
                    longitudeCapturada = location.getLongitude();

                    // Atualiza o botão para indicar sucesso
                    btnCapturarLocalizacao.setText(
                            String.format(Locale.getDefault(),
                                    "✓ GPS: %.4f°, %.4f°",
                                    latitudeCapturada, longitudeCapturada));
                    btnCapturarLocalizacao.setBackgroundTintList(
                            getColorStateList(R.color.status_concluida));

                    Toast.makeText(CadastroVistoriaActivity.this,
                            "Localização capturada com sucesso!", Toast.LENGTH_SHORT).show();

                    // Para de pedir atualizações após obter a localização
                    fusedLocationClient.removeLocationUpdates(locationCallback);
                }
            }
        };
    }

    /**
     * Configura o botão de captura de localização GPS.
     * Solicita permissão se necessário, depois inicia o FusedLocationProvider.
     */
    private void configurarBotaoLocalizacao() {
        btnCapturarLocalizacao.setOnClickListener(v -> {
            if (PermissaoHelper.temPermissaoLocalizacao(this)) {
                iniciarCapturarLocalizacao();
            } else {
                PermissaoHelper.solicitarPermissaoLocalizacao(this);
            }
        });
    }

    /**
     * Inicia a captura de localização com alta precisão via GPS.
     */
    @SuppressLint("MissingPermission")
    private void iniciarCapturarLocalizacao() {
        btnCapturarLocalizacao.setText("Buscando GPS...");
        btnCapturarLocalizacao.setEnabled(false);

        // Primeiro tenta a última localização conhecida (rápido)
        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                latitudeCapturada  = location.getLatitude();
                longitudeCapturada = location.getLongitude();
                atualizarBotaoLocalizacaoSucesso();
            } else {
                // Se não houver, solicita atualização ativa
                LocationRequest request = new LocationRequest.Builder(
                        Priority.PRIORITY_HIGH_ACCURACY, 5000)
                        .setMinUpdateIntervalMillis(2000)
                        .setMaxUpdates(1)
                        .build();
                fusedLocationClient.requestLocationUpdates(
                        request, locationCallback, Looper.getMainLooper());
            }
        });

        btnCapturarLocalizacao.setEnabled(true);
    }

    private void atualizarBotaoLocalizacaoSucesso() {
        btnCapturarLocalizacao.setText(
                String.format(Locale.getDefault(),
                        "✓ GPS: %.4f°, %.4f°",
                        latitudeCapturada, longitudeCapturada));
        btnCapturarLocalizacao.setBackgroundTintList(
                getColorStateList(R.color.status_concluida));
        Toast.makeText(this, "Localização capturada!", Toast.LENGTH_SHORT).show();
    }

    /**
     * Configura o botão "Avançar": valida o formulário e
     * cria a vistoria no repositório antes de navegar para inspeção.
     */
    private void configurarBotaoAvancar() {
        btnAvancar.setOnClickListener(v -> {
            if (validarFormulario()) {
                Vistoria novaVistoria = criarVistoria();
                repository.salvarVistoria(novaVistoria);

                // Navega para a tela de inspeção passando o ID
                Intent intent = new Intent(this, InspecaoActivity.class);
                intent.putExtra(InspecaoActivity.EXTRA_VISTORIA_ID, novaVistoria.getId());
                startActivity(intent);
                finish(); // Remove esta activity da pilha de volta
            }
        });
    }

    /**
     * Valida todos os campos obrigatórios do formulário.
     * Exibe erros inline nos TextInputLayouts.
     *
     * @return true se todos os campos estão válidos
     */
    private boolean validarFormulario() {
        boolean valido = true;

        String imovel      = getText(etImovel);
        String endereco    = getText(etEndereco);
        String responsavel = getText(etResponsavel);
        String data        = getText(etData);

        if (imovel.isEmpty()) {
            tilImovel.setError("Campo obrigatório");
            valido = false;
        } else {
            tilImovel.setError(null);
        }

        if (endereco.isEmpty()) {
            tilEndereco.setError("Campo obrigatório");
            valido = false;
        } else {
            tilEndereco.setError(null);
        }

        if (responsavel.isEmpty()) {
            tilResponsavel.setError("Campo obrigatório");
            valido = false;
        } else {
            tilResponsavel.setError(null);
        }

        if (data.isEmpty()) {
            tilData.setError("Campo obrigatório");
            valido = false;
        } else {
            tilData.setError(null);
        }

        return valido;
    }

    /**
     * Cria objeto Vistoria com os dados do formulário.
     */
    private Vistoria criarVistoria() {
        Vistoria vistoria = new Vistoria();
        vistoria.setNomeImovel(getText(etImovel));
        vistoria.setEndereco(getText(etEndereco));
        vistoria.setResponsavel(getText(etResponsavel));
        vistoria.setData(getText(etData));
        vistoria.setLatitude(latitudeCapturada);
        vistoria.setLongitude(longitudeCapturada);
        return vistoria;
    }

    private String getText(TextInputEditText et) {
        return et.getText() != null ? et.getText().toString().trim() : "";
    }

    // ─── PERMISSÕES ───────────────────────────────────────

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                            @NonNull String[] permissions,
                                            @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PermissaoHelper.REQUEST_LOCALIZACAO) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                iniciarCapturarLocalizacao();
            } else {
                Toast.makeText(this,
                        "Permissão de localização negada. " +
                                "Ative nas configurações do dispositivo.",
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    // ─── NAVEGAÇÃO ────────────────────────────────────────

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
