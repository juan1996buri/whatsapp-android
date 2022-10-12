package com.example.proyectofinalciclo.Fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.Toast;

import com.example.proyectofinalciclo.Activities.AgregarNuevoMiembroGrupoPrivadoActivity;
import com.example.proyectofinalciclo.Adaptadores.AdaptadorAgregarNuevoMiembroGrupoPrivadoActivity;
import com.example.proyectofinalciclo.Adaptadores.AdaptadorGruposMundiales;
import com.example.proyectofinalciclo.Adaptadores.AdaptadorGruposPrivados;
import com.example.proyectofinalciclo.Clases.Grupo;
import com.example.proyectofinalciclo.Clases.Usuario;
import com.example.proyectofinalciclo.Clases.ValorObjeto;
import com.example.proyectofinalciclo.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;

public class GrupoFragment extends Fragment {


    private FirebaseDatabase database;
    private FirebaseAuth auth;
    private AdaptadorGruposMundiales adaptadorGruposMundiales;
    private AdaptadorGruposPrivados adaptadorGruposPrivados;
    private ArrayList<Grupo> gruposMundiales,gruposPrivados;
    private RecyclerView recyclerviewGruposMundiales, recyclerviewGruposPrivados;
    private SearchView buscar;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view=inflater.inflate(R.layout.fragment_grupo, container, false);
        recyclerviewGruposMundiales=view.findViewById(R.id.recyclerviewGruposMundiales);
        buscar=view.findViewById(R.id.buscar);

        gruposMundiales=new ArrayList<>();
        adaptadorGruposMundiales=new AdaptadorGruposMundiales(getContext(),gruposMundiales);

        LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false);

        recyclerviewGruposMundiales.setLayoutManager(layoutManager);

        recyclerviewGruposMundiales.setAdapter(adaptadorGruposMundiales);
        database=FirebaseDatabase.getInstance();
        auth=FirebaseAuth.getInstance();


        database.getReference().child("DatosGruposMundiales").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    gruposMundiales.clear();
                    for(DataSnapshot snapshot1:snapshot.getChildren()){
                        Grupo grupo=snapshot1.getValue(Grupo.class);
                        gruposMundiales.add(grupo);
                        adaptadorGruposMundiales.notifyDataSetChanged();

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });

        recyclerviewGruposPrivados=view.findViewById(R.id.recyclerViewGruposPrivados);
        gruposPrivados=new ArrayList<>();
        ArrayList<String> idGruposExistentes= new ArrayList<>();
        adaptadorGruposPrivados=new AdaptadorGruposPrivados(getContext(),gruposPrivados);
        recyclerviewGruposPrivados.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerviewGruposPrivados.setAdapter(adaptadorGruposPrivados);


        ArrayList<String> idGruposFecha=new ArrayList<>();
        ArrayList<String> idGruposAmigos=new ArrayList<>();

        database.getReference().child("GruposPrivados").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if(snapshot.exists()) {
                    idGruposExistentes.clear();

                    for(DataSnapshot snapshot1:snapshot.getChildren()){
                        String idGrupo= snapshot1.getKey();
                        idGruposExistentes.add(idGrupo);
                    }
                }

                try {
                    idGruposFecha.clear();
                    for(String id:idGruposExistentes){
                        database.getReference().child("MiembrosGruposPrivados").child(id).child(auth.getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                                if(snapshot.exists()){
                                    database.getReference().child("GruposPrivados").child(id).child("ultimoMensaje").addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                                            if(snapshot.exists()){
                                                String grupo=snapshot.child("fecha_tiempo").getValue(String.class)+" "+id;
                                                idGruposFecha.add(grupo);
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


                    }
                }catch (Exception e){

                }

                database.getReference().child("MiembrosGruposPrivados").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                        if(snapshot.exists()){
                            database.getReference().child("GruposPrivados").addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                                    if(snapshot.exists()){
                                        try {
                                            idGruposAmigos.clear();

                                            final ArrayList<ValorObjeto> objList = new ArrayList<>();
                                            Collections.sort(idGruposFecha); // Con esta linea ordenamos la lista

                                            for (int i = 0; i < idGruposFecha.size(); i++) {
                                                final ValorObjeto aux = new ValorObjeto();
                                                aux.setTime(idGruposFecha.get(i)); // Aqui imagino que quisiste poner setTime y no setMsg
                                                aux.setMsg(String.valueOf(i));
                                                objList.add(aux);
                                            }
                                            for(int i=idGruposFecha.size()-1;i>=0;i--){

                                                String usuario = idGruposFecha.get(i);
                                                String[] parts = usuario.split(" ");
                                                String id = parts[1];
                                                idGruposAmigos.add(id);
                                            }

                                        }catch (Exception e){

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

                database.getReference().child("MiembrosGruposPrivados").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                        if(snapshot.exists()){
                            database.getReference().child("GruposPrivados").addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                                    if(snapshot.exists()){
                                        gruposPrivados.clear();
                                        try {
                                            for (String id:idGruposAmigos){
                                                database.getReference().child("DatosGruposPrivados").child(id).addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                                                        if(snapshot.exists()){
                                                            Grupo grupo=snapshot.getValue(Grupo.class);
                                                            gruposPrivados.add(grupo);
                                                            adaptadorGruposPrivados.notifyDataSetChanged();
                                                        }
                                                    }

                                                    @Override
                                                    public void onCancelled(@NonNull @NotNull DatabaseError error) {

                                                    }
                                                });
                                            }
                                        }catch (Exception e){

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
                ArrayList<Grupo> listGruposPrivados=new ArrayList<>();
                ArrayList<Grupo> listGruposMundiales=new ArrayList<>();

                for(Grupo grupo:gruposPrivados){
                    if(grupo.getNombre().toLowerCase().contains(s.toLowerCase())){
                        listGruposPrivados.add(grupo);
                    }
                }

                for(Grupo grupo:gruposMundiales){
                    if(grupo.getNombre().toLowerCase().contains(s.toLowerCase())){
                        listGruposMundiales.add(grupo);
                    }
                }


                adaptadorGruposPrivados=new AdaptadorGruposPrivados(getContext(), listGruposPrivados);
                recyclerviewGruposPrivados.setAdapter(adaptadorGruposPrivados);

                adaptadorGruposMundiales=new AdaptadorGruposMundiales(getContext(), listGruposMundiales);
                recyclerviewGruposMundiales.setAdapter(adaptadorGruposMundiales);

                return false;
            }
        });

        return view;
    }
}