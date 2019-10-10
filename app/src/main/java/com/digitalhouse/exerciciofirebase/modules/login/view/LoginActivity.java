package com.digitalhouse.exerciciofirebase.modules.login.view;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProviders;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.digitalhouse.exerciciofirebase.CadastroActivity;
import com.digitalhouse.exerciciofirebase.MainActivity;
import com.digitalhouse.exerciciofirebase.R;
import com.digitalhouse.exerciciofirebase.modules.login.viewmodel.LoginViewModel;
import com.google.android.material.textfield.TextInputEditText;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText emailEditText;
    private TextInputEditText senhaEditText;
    private Button loginButton;
    private Button registroButton;
    private LoginViewModel loginViewModel;
    private ProgressBar progressBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        emailEditText = findViewById(R.id.email_edit_text_id);
        senhaEditText = findViewById(R.id.senha_edit_text_id);
        loginButton = findViewById(R.id.button_ok_id);
        registroButton = findViewById(R.id.button_registrar_id);
        progressBar = findViewById(R.id.progressBar);

        //click com  lambda, só funciona para metodos com UM parametro
        registroButton.setOnClickListener(view -> irParaCadastro());

        //click sem lambda
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                logar();
            }
        });

        loginViewModel = ViewModelProviders.of(this).get(LoginViewModel.class);
        loginViewModel.getAutenticadoLiveData()
                .observe(this, autenticado ->{
                    if(autenticado){
                        irParaMain();
                    }else{
                        Toast.makeText(this, "falha na autenticação", Toast.LENGTH_SHORT).show();
                    }
                });

        loginViewModel.getLoaderLiveData()
        .observe(this, showLoader -> {
            progressBar.setVisibility(showLoader ? View.VISIBLE : View.GONE);
        });
    }

    private void logar() {

        String email = emailEditText.getEditableText().toString();
        String senha = senhaEditText.getEditableText().toString();

        loginViewModel.autenticarUsuario(email, senha);

    }

    private void irParaCadastro() {
        Intent intent = new Intent(this, CadastroActivity.class);
        startActivity(intent);
    }

    private void irParaMain() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}
