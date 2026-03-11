package com.vistoria.app.ui.login;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.vistoria.app.R;
import com.vistoria.app.ui.lista.ListaVistoriasActivity;

/**
 * Tela de Login do App de Vistoria.
 *
 * Implementa autenticação local simples via SharedPreferences.
 * Em produção substituir por autenticação real (Firebase Auth, JWT, etc.)
 *
 * Fluxo:
 * - Usuário informa e-mail e senha
 * - App valida contra credenciais salvas localmente
 * - Se "Lembrar-me" marcado, salva sessão e pula login nas próximas aberturas
 */
public class LoginActivity extends AppCompatActivity {

    private static final String PREFS_LOGIN    = "login_prefs";
    private static final String KEY_LOGADO     = "usuario_logado";
    private static final String KEY_EMAIL      = "email_salvo";
    private static final String KEY_LEMBRAR    = "lembrar_login";

    // Credenciais padrão para demonstração acadêmica
    private static final String EMAIL_DEMO  = "vistoriador@app.com";
    private static final String SENHA_DEMO  = "123456";

    // Views
    private TextInputLayout    tilEmail, tilSenha;
    private TextInputEditText  etEmail, etSenha;
    private Button             btnEntrar;
    private TextView           tvEsqueciSenha, tvCriarConta;

    private SharedPreferences prefs;

    // ─── CICLO DE VIDA ────────────────────────────────────

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        prefs = getSharedPreferences(PREFS_LOGIN, MODE_PRIVATE);

        // Se já está logado com "Lembrar-me", vai direto para a lista
        if (prefs.getBoolean(KEY_LOGADO, false)) {
            irParaLista();
            return;
        }

        setContentView(R.layout.activity_login);

        vincularViews();
        preencherEmailSalvo();
        configurarBotaoEntrar();
        configurarLinks();
    }

    // ─── CONFIGURAÇÃO ─────────────────────────────────────

    private void vincularViews() {
        tilEmail       = findViewById(R.id.tilEmail);
        tilSenha       = findViewById(R.id.tilSenha);
        etEmail        = findViewById(R.id.etEmail);
        etSenha        = findViewById(R.id.etSenha);
        btnEntrar      = findViewById(R.id.btnEntrar);
        tvEsqueciSenha = findViewById(R.id.tvEsqueciSenha);
        tvCriarConta   = findViewById(R.id.tvCriarConta);
    }

    /**
     * Se o usuário salvou o e-mail anteriormente, preenche automaticamente.
     */
    private void preencherEmailSalvo() {
        String emailSalvo = prefs.getString(KEY_EMAIL, "");
        if (!emailSalvo.isEmpty()) {
            etEmail.setText(emailSalvo);
        }
    }

    /**
     * Configura o botão Entrar: valida campos e autentica o usuário.
     */
    private void configurarBotaoEntrar() {
        btnEntrar.setOnClickListener(v -> {
            if (validarCampos()) {
                autenticar();
            }
        });
    }

    private void configurarLinks() {
        tvEsqueciSenha.setOnClickListener(v ->
            Toast.makeText(this,
                "Demo: use " + EMAIL_DEMO + " / " + SENHA_DEMO,
                Toast.LENGTH_LONG).show()
        );

        tvCriarConta.setOnClickListener(v ->
            Toast.makeText(this,
                "Cadastro não disponível nesta versão demo.",
                Toast.LENGTH_SHORT).show()
        );
    }

    // ─── VALIDAÇÃO ────────────────────────────────────────

    /**
     * Valida os campos de e-mail e senha com mensagens inline.
     */
    private boolean validarCampos() {
        boolean valido = true;

        String email = getTexto(etEmail);
        String senha = getTexto(etSenha);

        if (email.isEmpty()) {
            tilEmail.setError("Informe seu e-mail");
            valido = false;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError("E-mail inválido");
            valido = false;
        } else {
            tilEmail.setError(null);
        }

        if (senha.isEmpty()) {
            tilSenha.setError("Informe sua senha");
            valido = false;
        } else if (senha.length() < 6) {
            tilSenha.setError("Senha deve ter ao menos 6 caracteres");
            valido = false;
        } else {
            tilSenha.setError(null);
        }

        return valido;
    }

    // ─── AUTENTICAÇÃO ─────────────────────────────────────

    /**
     * Verifica as credenciais contra os dados demo.
     * Em produção: substituir por chamada de API ou Firebase.
     */
    private void autenticar() {
        String email = getTexto(etEmail);
        String senha = getTexto(etSenha);

        if (email.equals(EMAIL_DEMO) && senha.equals(SENHA_DEMO)) {
            // Salva sessão
            prefs.edit()
                .putBoolean(KEY_LOGADO, true)
                .putString(KEY_EMAIL, email)
                .apply();

            Toast.makeText(this, "Bem-vindo!", Toast.LENGTH_SHORT).show();
            irParaLista();
        } else {
            tilSenha.setError("E-mail ou senha incorretos");
            Toast.makeText(this,
                "Credenciais inválidas. Use: " + EMAIL_DEMO,
                Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Navega para a tela principal e remove o Login da pilha de back.
     */
    private void irParaLista() {
        Intent intent = new Intent(this, ListaVistoriasActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private String getTexto(TextInputEditText et) {
        return et.getText() != null ? et.getText().toString().trim() : "";
    }
}
