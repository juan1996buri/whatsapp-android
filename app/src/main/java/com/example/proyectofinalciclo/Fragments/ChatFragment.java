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

import com.example.proyectofinalciclo.Activities.MensajeUsuarioActivity;
import com.example.proyectofinalciclo.Adaptadores.AdaptadorChatFragment;
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

public class ChatFragment extends Fragment {

    private FirebaseDatabase database;
    private FirebaseAuth auth;
    private SearchView buscar;
    private String idUsuario;

    private RecyclerView recyclerViewListadoUsuarioChat;
    private AdaptadorChatFragment adaptadorChatFragment;
    private ArrayList<Usuario> usuarios;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view= inflater.inflate(R.layout.fragment_chat, container, false);
        recyclerViewListadoUsuarioChat=view.findViewById(R.id.recyclerViewListadoUsuarioChat);
        buscar=view.findViewById(R.id.busquedaChat);

        database=FirebaseDatabase.getInstance();
        auth=FirebaseAuth.getInstance();

        idUsuario=auth.getCurrentUser().getUid();

        usuarios=new ArrayList<>();
        adaptadorChatFragment=new AdaptadorChatFragment(usuarios,getContext());
        recyclerViewListadoUsuarioChat.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewListadoUsuarioChat.setAdapter(adaptadorChatFragment);



        ArrayList<String> idAmigos=new ArrayList<>();

        ArrayList<String> idAmigosFecha=new ArrayList<>();

        database.getReference().child("Amistades").child(idUsuario).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    idAmigos.clear();
                    for(DataSnapshot snapshot1:snapshot.getChildren()){
                        idAmigos.add(snapshot1.getKey());
                    }

                    try {
                        idAmigosFecha.clear();
                        for(String id: idAmigos){
                            database.getReference().child("Chats").child(auth.getCurrentUser().getUid()+id).child("ultimoMensaje").addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                                    if(snapshot.exists()){
                                        String fecha_idUsuario=snapshot.child("fecha_tiempo").getValue(String.class)+" "+id;
                                        idAmigosFecha.add(fecha_idUsuario);

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

                database.getReference().child("Amistades").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                        if(snapshot.exists()){
                            try {
                                idAmigos.clear();
                                final ArrayList<ValorObjeto> objList = new ArrayList<>();
                                Collections.sort(idAmigosFecha); // Con esta linea ordenamos la lista

                                for (int i = 0; i < idAmigosFecha.size(); i++) {
                                    final ValorObjeto aux = new ValorObjeto();
                                    aux.setTime(idAmigosFecha.get(i)); // Aqui imagino que quisiste poner setTime y no setMsg
                                    aux.setMsg(String.valueOf(i));
                                    objList.add(aux);
                                }
                                for(int i=idAmigosFecha.size()-1;i>=0;i--){

                                    String usuario = idAmigosFecha.get(i);
                                    String[] parts = usuario.split(" ");
                                    String id = parts[1];
                                    idAmigos.add(id);
                                }
                            }catch (Exception e){

                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {

                    }
                });


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
                                        adaptadorChatFragment.notifyDataSetChanged();
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
                adaptadorChatFragment=new AdaptadorChatFragment(listUsuarios,getContext());
                recyclerViewListadoUsuarioChat.setAdapter(adaptadorChatFragment);

                return false;
            }
        });


        return view;
    }
}