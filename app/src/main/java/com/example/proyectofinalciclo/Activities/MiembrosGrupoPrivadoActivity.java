package com.example.proyectofinalciclo.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.os.Bundle;
import android.view.View;
import android.widget.SearchView;

import com.example.proyectofinalciclo.Adaptadores.AdaptadorMiembrosGrupoPrivadoActivity;
import com.example.proyectofinalciclo.Clases.Usuario;
import com.example.proyectofinalciclo.databinding.ActivityMiembrosGrupoPrivadoBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import static java.security.AccessController.getContext;

public class MiembrosGrupoPrivadoActivity extends AppCompatActivity {

    private ActivityMiembrosGrupoPrivadoBinding binding;
    private FirebaseDatabase database;
    private FirebaseAuth auth;
    //private SearchView buscar;
    private String idUsuario;

    //private RecyclerView recyclerViewListadoUsuarioChat;
    private AdaptadorMiembrosGrupoPrivadoActivity adaptadorMiembrosGrupoPrivadoActivity;
    private ArrayList<Usuario> usuarios;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityMiembrosGrupoPrivadoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        String idGrupoPrivado=getIntent().getStringExtra("idGrupoPrivado");

        database= FirebaseDatabase.getInstance();
        auth= FirebaseAuth.getInstance();
        idUsuario=auth.getCurrentUser().getUid();

        binding.retroceder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        usuarios=new ArrayList<>();
        adaptadorMiembrosGrupoPrivadoActivity=new AdaptadorMiembrosGrupoPrivadoActivity(usuarios,this,idGrupoPrivado);
        binding.recyclerViewListadoMiembros.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerViewListadoMiembros.setAdapter(adaptadorMiembrosGrupoPrivadoActivity);



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
                                            usuarios.add(usuario);
                                            adaptadorMiembrosGrupoPrivadoActivity.notifyDataSetChanged();
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
                adaptadorMiembrosGrupoPrivadoActivity=new AdaptadorMiembrosGrupoPrivadoActivity(listUsuarios,MiembrosGrupoPrivadoActivity.this,idGrupoPrivado);
                binding.recyclerViewListadoMiembros.setAdapter(adaptadorMiembrosGrupoPrivadoActivity);

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