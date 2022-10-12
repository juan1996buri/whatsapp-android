package com.example.proyectofinalciclo.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.os.Bundle;
import android.view.View;
import android.widget.SearchView;

import com.example.proyectofinalciclo.Adaptadores.AdaptadorAgregarAmigosActivity;
import com.example.proyectofinalciclo.Clases.Usuario;
import com.example.proyectofinalciclo.databinding.ActivityAgregarAmigosBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class AgregarAmigosActivity extends AppCompatActivity {

    private ActivityAgregarAmigosBinding binding;
    private FirebaseDatabase database;
    private FirebaseAuth auth;
    private AdaptadorAgregarAmigosActivity adaptadorAgregarAmigosActivity;
    private ArrayList<Usuario> usuarios;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityAgregarAmigosBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar3);

        database=FirebaseDatabase.getInstance();
        auth=FirebaseAuth.getInstance();

        binding.retroceder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        usuarios=new ArrayList<>();
        adaptadorAgregarAmigosActivity=new AdaptadorAgregarAmigosActivity(AgregarAmigosActivity.this,usuarios);
        binding.recyclerViewUsuarios.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerViewUsuarios.setAdapter(adaptadorAgregarAmigosActivity);

        database.getReference().child("Usuarios").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    usuarios.clear();
                    for(DataSnapshot snapshot1:snapshot.getChildren()){
                        Usuario usuario=snapshot1.getValue(Usuario.class);
                        if(!FirebaseAuth.getInstance().getUid().equals(usuario.getIdUsuario())){
                            usuarios.add(usuario);
                        }
                    }
                    adaptadorAgregarAmigosActivity.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });

        binding.buscar.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                ArrayList<Usuario> list=new ArrayList<>();
                for(Usuario user:usuarios){
                    if(user.getNombreUsuario().toLowerCase().contains(newText.toLowerCase()) || user.getTelefonoUsuario().contains(newText)){
                        list.add(user);
                    }
                }
                adaptadorAgregarAmigosActivity=new AdaptadorAgregarAmigosActivity(AgregarAmigosActivity.this,list);
                binding.recyclerViewUsuarios.setAdapter(adaptadorAgregarAmigosActivity);
                return false;
            }
        });

    }





    @Override
    protected void onResume() {
        super.onResume();
        InicioActivity.CerrarSesion("Conectado");

    }

    @Override
    protected void onStart() {
        super.onStart();
        InicioActivity.CerrarSesion("Conectado");
    }



    @Override
    protected void onPause() {
        super.onPause();

        InicioActivity.CerrarSesion("Desconectado");
    }
}