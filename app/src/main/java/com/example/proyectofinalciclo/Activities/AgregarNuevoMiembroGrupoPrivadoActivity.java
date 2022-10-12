package com.example.proyectofinalciclo.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.SearchView;
import android.widget.Toast;

import com.example.proyectofinalciclo.Adaptadores.AdaptadorAgregarNuevoMiembroGrupoPrivadoActivity;
import com.example.proyectofinalciclo.Adaptadores.AdaptadorChatFragment;
import com.example.proyectofinalciclo.Clases.Grupo;
import com.example.proyectofinalciclo.Clases.Usuario;
import com.example.proyectofinalciclo.Fragments.GrupoFragment;
import com.example.proyectofinalciclo.R;
import com.example.proyectofinalciclo.databinding.ActivityAgregarNuevoMiembroGrupoPrivadoBinding;
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

public class AgregarNuevoMiembroGrupoPrivadoActivity extends AppCompatActivity {
   private ActivityAgregarNuevoMiembroGrupoPrivadoBinding binding;

    private FirebaseDatabase database;
    private FirebaseAuth auth;
   // private SearchView buscar;
    private String idUsuario;
    private String idGrupoPrivado;
    private ProgressDialog dialog;


    private AdaptadorAgregarNuevoMiembroGrupoPrivadoActivity adaptadorAgregarNuevoMiembroGrupoPrivadoActivity;
    private ArrayList<Usuario> usuarios;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityAgregarNuevoMiembroGrupoPrivadoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        idGrupoPrivado=getIntent().getStringExtra("idGrupoPrivado");


        database= FirebaseDatabase.getInstance();
        auth= FirebaseAuth.getInstance();

        idUsuario=auth.getCurrentUser().getUid();

        usuarios=new ArrayList<>();
        ArrayList<String> idAmigos=new ArrayList<>();
        ArrayList<String> idIntegrantesGrupo=new ArrayList<>();
        ArrayList<String> usuariosIguales=new ArrayList<>();
        adaptadorAgregarNuevoMiembroGrupoPrivadoActivity=new AdaptadorAgregarNuevoMiembroGrupoPrivadoActivity(usuarios,this);
        binding.recyclerViewListadoUsuarioChat.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerViewListadoUsuarioChat.setAdapter(adaptadorAgregarNuevoMiembroGrupoPrivadoActivity);

        binding.retroceder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        database.getReference().child("Amistades").child(idUsuario).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if(snapshot.exists()){

                    idAmigos.clear();
                    for(DataSnapshot snapshot1:snapshot.getChildren()){
                        idAmigos.add(snapshot1.getKey());
                    }

                    database.getReference().child("MiembrosGruposPrivados").child(idGrupoPrivado).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                            if(snapshot.exists()){
                                idIntegrantesGrupo.clear();
                                for(DataSnapshot snapshot1:snapshot.getChildren()){
                                    String idIntegrante=snapshot1.getKey();
                                    idIntegrantesGrupo.add(idIntegrante);

                                }
                                usuariosIguales.clear();

                                for(String idAm:idAmigos){
                                    for(String idInteg:idIntegrantesGrupo){
                                        if(idAm.equals(idInteg)){
                                            usuariosIguales.add(idInteg);

                                        }
                                    }
                                }
                                if(!usuariosIguales.isEmpty()){
                                    for (String idUs:usuariosIguales) {
                                        idAmigos.remove(idUs);
                                    }
                                }

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
                                                    usuarios.add(usuario);
                                                    adaptadorAgregarNuevoMiembroGrupoPrivadoActivity.notifyDataSetChanged();
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
                        HashMap<String,Object> hashMap=new HashMap<>();
                        hashMap.put("Grupo","Miembro");
                        database.getReference().child("MiembrosGruposPrivados").child(idGrupoPrivado).child(usuario.getIdUsuario()).setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull @NotNull Task<Void> task) {
                                if(task.isSuccessful()){
                                    Toast.makeText(AgregarNuevoMiembroGrupoPrivadoActivity.this, "usuario agregado", Toast.LENGTH_SHORT).show();

                                    finish();
                                }else{
                                    Toast.makeText(AgregarNuevoMiembroGrupoPrivadoActivity.this, "Se a producido un error", Toast.LENGTH_SHORT).show();
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
                adaptadorAgregarNuevoMiembroGrupoPrivadoActivity=new AdaptadorAgregarNuevoMiembroGrupoPrivadoActivity(listUsuarios,AgregarNuevoMiembroGrupoPrivadoActivity.this);
                binding.recyclerViewListadoUsuarioChat.setAdapter(adaptadorAgregarNuevoMiembroGrupoPrivadoActivity);

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