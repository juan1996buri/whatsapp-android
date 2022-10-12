package com.example.proyectofinalciclo.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.os.Bundle;
import android.view.View;
import android.widget.SearchView;
import android.widget.Toast;

import com.example.proyectofinalciclo.Adaptadores.AdaptadorAgregarNuevoMiembroGrupoPrivadoActivity;
import com.example.proyectofinalciclo.Adaptadores.AdaptadorSolicitudAmistadActivity;
import com.example.proyectofinalciclo.Clases.Usuario;
import com.example.proyectofinalciclo.R;
import com.example.proyectofinalciclo.databinding.ActivityInformacionUsuarioBinding;
import com.example.proyectofinalciclo.databinding.ActivitySolicitudAmistadBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class SolicitudAmistadActivity extends AppCompatActivity {

    private ActivitySolicitudAmistadBinding binding;
    private FirebaseAuth auth;
    private FirebaseDatabase database;
    private ArrayList<Usuario> usuarios;
    private AdaptadorSolicitudAmistadActivity adaptadorSolicitudAmistadActivity;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding= ActivitySolicitudAmistadBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        usuarios=new ArrayList<>();
        database=FirebaseDatabase.getInstance();
        auth=FirebaseAuth.getInstance();

        adaptadorSolicitudAmistadActivity=new AdaptadorSolicitudAmistadActivity(usuarios,this);
        binding.recyclerViewSolicitudAmistad.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerViewSolicitudAmistad.setAdapter(adaptadorSolicitudAmistadActivity);

        binding.retroceder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
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
                adaptadorSolicitudAmistadActivity=new AdaptadorSolicitudAmistadActivity(listUsuarios,SolicitudAmistadActivity.this);
                binding.recyclerViewSolicitudAmistad.setAdapter(adaptadorSolicitudAmistadActivity);

                return false;
            }
        });
    }




    @Override
    protected void onStart() {
        super.onStart();
        InicioActivity.CerrarSesion("Conectado");
        ObtenerDatosUsuarios();
    }

    private void ObtenerDatosUsuarios() {
        ArrayList<String> idPersonasRecibido=new ArrayList<>();

       database.getReference().child("SolicitudAmistad").child(auth.getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
           @Override
           public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
               if(snapshot.exists()){
                   idPersonasRecibido.clear();
                   for(DataSnapshot snapshot1:snapshot.getChildren()){
                       if(snapshot1.exists()){
                           String estado=snapshot1.child("tipoRequerimiento").getValue(String.class);
                           if(estado.equals("recibido")){
                               idPersonasRecibido.add(snapshot1.getKey());

                           }
                       }
                   }
                   usuarios.clear();
                   for(String recorrido:idPersonasRecibido){
                       database.getReference().child("Usuarios").child(recorrido).addValueEventListener(new ValueEventListener() {
                           @Override
                           public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                               if(snapshot.exists()){
                                   Usuario usuario=snapshot.getValue(Usuario.class);
                                   usuarios.add(usuario);
                                   adaptadorSolicitudAmistadActivity.notifyDataSetChanged();

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
    @Override
    protected void onResume() {
        super.onResume();
        InicioActivity.CerrarSesion("Conectado");

    }

    @Override
    protected void onPause() {
        super.onPause();

        InicioActivity.CerrarSesion("Desconectado");
    }
}