package com.example.proyectofinalciclo.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.proyectofinalciclo.databinding.ActivityNumeroTelefonicoBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.hbb20.CountryCodePicker;

public class NumeroTelefonicoActivity extends AppCompatActivity {
    private ActivityNumeroTelefonicoBinding binding;
    private String phoneNumber;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityNumeroTelefonicoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //getSupportActionBar().hide();

        binding.numeroTelefonico.requestFocus();
        binding.continuarBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String numeroTelefonico=binding.numeroTelefonico.getText().toString();
                if(numeroTelefonico.isEmpty()){
                    binding.numeroTelefonico.setError("Ingrese un numero telefonico");
                }else{
                    binding.ccp.registerCarrierNumberEditText(binding.numeroTelefonico);
                    phoneNumber=binding.ccp.getFullNumberWithPlus();
                    Intent intent=new Intent(NumeroTelefonicoActivity.this,CodigoTelefonoActivity.class);
                    intent.putExtra("numeroTelefonico",phoneNumber);
                    binding.numeroTelefonico.setText("");
                    startActivity(intent);

                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(FirebaseAuth.getInstance().getCurrentUser()!=null){
            Intent intent=new Intent(NumeroTelefonicoActivity.this, InicioActivity.class);
            startActivity(intent);
            finish();
        }
    }
}