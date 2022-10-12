package com.example.proyectofinalciclo.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.SearchView;
import android.widget.Toast;

import com.example.proyectofinalciclo.Adaptadores.AdaptadorAgregarNuevoMiembroGrupoPrivadoActivity;
import com.example.proyectofinalciclo.Adaptadores.AdaptadorEliminarMiembroGrupoPrivado;
import com.example.proyectofinalciclo.Adaptadores.AdaptadorMiembrosGrupoPrivadoActivity;
import com.example.proyectofinalciclo.Clases.Usuario;
import com.example.proyectofinalciclo.databinding.ActivityAgregarNuevoMiembroGrupoPrivadoBinding;
import com.example.proyectofinalciclo.databinding.ActivityEliminarMiembroGrupoPrivadoBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;

public class EliminarMiembroGrupoPrivadoActivity extends AppCompatActivity {

    private ActivityEliminarMiembroGrupoPrivadoBinding binding;

    private FirebaseDatabase database;
    private FirebaseAuth auth;
    // private SearchView buscar;
    private String idUsuario;
    private String idGrupoPrivado;
    private ProgressDialog dialog;


    private AdaptadorEliminarMiembroGrupoPrivado adaptadorEliminarMiembroGrupoPrivado;
    private ArrayList<Usuario> usuarios;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding= ActivityEliminarMiembroGrupoPrivadoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        idGrupoPrivado=getIntent().getStringExtra("idGrupoPrivado");


        database= FirebaseDatabase.getInstance();
        auth= FirebaseAuth.getInstance();
        idUsuario=auth.getCurrentUser().getUid();

        usuarios=new ArrayList<>();
        adaptadorEliminarMiembroGrupoPrivado=new AdaptadorEliminarMiembroGrupoPrivado(usuarios,this,idGrupoPrivado);
        binding.recyclerViewListadoUsuarioChat.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerViewListadoUsuarioChat.setAdapter(adaptadorEliminarMiembroGrupoPrivado);



        ArrayList<String> idAmigos=new ArrayList<>();
        database.getReference().child("MiembrosGruposPrivados").child(idGrupoPrivado).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    idAmigos.clear();
                    for(DataSnapshot snapshot1:snapshot.getChildren()){
                        idAmigos.add(snapshot1.getKey());
                    }

                    database.getReference().child("Usuarios").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                            if(snapshot.exists()){
                                usuarios.clear();
                                for(String recorrido:idAmigos){
                                    database.getReference().child("Usuarios").child(recorrido).addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                                            Usuario usuario=snapshot.getValue(Usuario.class);
                                            if(!usuario.getIdUsuario().equals(idUsuario)){
                                                usuarios.add(usuario);
                                                adaptadorEliminarMiembroGrupoPrivado.notifyDataSetChanged();

                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull @NotNull DatabaseError error) {

                                        }
                                    });
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull @NotNull DatabaseError error) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });


        binding.floatingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                for (Usuario usuario : usuarios) {
                    if (usuario.isSelected()) {

                        database.getReference().child("MiembrosGruposPrivados").child(idGrupoPrivado).child(usuario.getIdUsuario()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull @NotNull Task<Void> task) {
                                if(task.isComplete()){
                                    Toast.makeText(EliminarMiembroGrupoPrivadoActivity.this, "Usuario eliminado", Toast.LENGTH_SHORT).show();
                                    finish();
                                }
                            }
                        });
                    }
                }

            }
        });



        binding.buscar.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {

                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                ArrayList<Usuario> listUsuarios=new ArrayList<>();
                for(Usuario user:usuarios){
                    if(user.getNombreUsuario().toLowerCase().contains(s.toLowerCase()) || user.getTelefonoUsuario().contains(s)){
                        listUsuarios.add(user);
                    }
                }
                adaptadorEliminarMiembroGrupoPrivado=new AdaptadorEliminarMiembroGrupoPrivado(listUsuarios,EliminarMiembroGrupoPrivadoActivity.this,idGrupoPrivado);
                binding.recyclerViewListadoUsuarioChat.setAdapter(adaptadorEliminarMiembroGrupoPrivado);

                return false;
            }
        });
        binding.retroceder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
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