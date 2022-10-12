package com.example.proyectofinalciclo.Fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;

import com.example.proyectofinalciclo.Activities.AgregarAmigosActivity;
import com.example.proyectofinalciclo.Adaptadores.AdaptadorAgregarAmigosActivity;
import com.example.proyectofinalciclo.Adaptadores.AdaptadorContactoFragment;
import com.example.proyectofinalciclo.Clases.Usuario;
import com.example.proyectofinalciclo.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class ContactoFragment extends Fragment {

    private FirebaseDatabase database;
    private FirebaseAuth auth;
    private String idUsuario;
    private ArrayList<Usuario> usuarios;
    private AdaptadorContactoFragment adaptadorContactoFragment;
    private RecyclerView recyclerViewListadoContactos;
    private SearchView buscar;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view=inflater.inflate(R.layout.fragment_contacto, container, false);
        recyclerViewListadoContactos=view.findViewById(R.id.recyclerViewListadoContactos);
        buscar=view.findViewById(R.id.buscar);


        database=FirebaseDatabase.getInstance();
        auth=FirebaseAuth.getInstance();

        idUsuario=auth.getCurrentUser().getUid();
        usuarios=new ArrayList<>();
        adaptadorContactoFragment=new AdaptadorContactoFragment(usuarios,getContext());
        recyclerViewListadoContactos.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewListadoContactos.setAdapter(adaptadorContactoFragment);

        ArrayList<String> idAmigos=new ArrayList<>();
        database.getReference().child("Amistades").child(idUsuario).addValueEventListener(new ValueEventListener() {
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
                                            adaptadorContactoFragment.notifyDataSetChanged();
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

         buscar.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
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
                adaptadorContactoFragment=new AdaptadorContactoFragment(listUsuarios,getContext());
                recyclerViewListadoContactos.setAdapter(adaptadorContactoFragment);

                return false;
            }
        });
        return view;

    }

}