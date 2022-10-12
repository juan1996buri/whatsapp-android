package com.example.proyectofinalciclo.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;

import com.example.proyectofinalciclo.Clases.Grupo;
import com.example.proyectofinalciclo.R;
import com.example.proyectofinalciclo.databinding.ActivityInformacionGrupoMundialBinding;
import com.example.proyectofinalciclo.databinding.ActivityInformacionGrupoPrivadoBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class InformacionGrupoMundialActivity extends AppCompatActivity {
    private ActivityInformacionGrupoMundialBinding binding;
    private FirebaseAuth auth;
    private FirebaseDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityInformacionGrupoMundialBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        database=FirebaseDatabase.getInstance();
        auth=FirebaseAuth.getInstance();
        String idGrupoMundial=getIntent().getStringExtra("idGrupoMundial");

        setSupportActionBar(binding.toolbar);

        binding.retroceder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        database.getReference().child("DatosGruposMundiales").child(idGrupoMundial).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    Grupo grupo=snapshot.getValue(Grupo.class);
                    binding.nombreGrupo.setText(grupo.getNombre());
                    binding.tipoGrupo.setText("Mundial");

                    if(grupo.getDescripcion().isEmpty()){
                        binding.descripcionGrupo.setText("Sin descripcion");
                    }else{
                        binding.descripcionGrupo.setText(grupo.getDescripcion());
                    }

                    Picasso.with(InformacionGrupoMundialActivity.this).
                            load(grupo.getImagen()).fit().centerCrop()
                            .placeholder(R.drawable.avatar)
                            .error(R.drawable.avatar)
                            .placeholder(R.drawable.avatar)
                            .into(binding.imagenUsuario);
                }

            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });

    }
}