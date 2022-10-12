package com.example.proyectofinalciclo.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Toast;

import com.example.proyectofinalciclo.Adaptadores.AdaptadorVistaImagenActivity;
import com.example.proyectofinalciclo.Clases.Mensaje;
import com.example.proyectofinalciclo.R;
import com.example.proyectofinalciclo.databinding.ActivityVistaImagenGrupoMundialBinding;
import com.example.proyectofinalciclo.databinding.ActivityVistaImagenGrupoPrivadoBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class VistaImagenGrupoMundialActivity extends AppCompatActivity {
    private ActivityVistaImagenGrupoMundialBinding binding;
    private FirebaseDatabase database;
    private ArrayList<Mensaje> mensajes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding= ActivityVistaImagenGrupoMundialBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        database= FirebaseDatabase.getInstance();
        mensajes=new ArrayList<>();

        String imagen=getIntent().getStringExtra("imagen");
        String idGrupoMundial=getIntent().getStringExtra("idGrupoMundial");

        AdaptadorVistaImagenActivity adaptadorVistaImagenActivity= new AdaptadorVistaImagenActivity(VistaImagenGrupoMundialActivity.this,mensajes);
        binding.sliderView.setSliderAdapter(adaptadorVistaImagenActivity);

       // Toast.makeText(this, idGrupoMundial, Toast.LENGTH_SHORT).show();

        database.getReference().child("GruposMundiales").child(idGrupoMundial).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    mensajes.clear();
                    Mensaje ms=new Mensaje();
                    ms.setMensajeTxt(imagen);
                    mensajes.add(ms);
                    for(DataSnapshot snapshot1:snapshot.getChildren()){
                        String tipo=snapshot1.child("tipoMensaje").getValue(String.class);
                        String mensajeTxt=snapshot1.child("mensajeTxt").getValue(String.class);
                        if(tipo.equals("imagen") || tipo.equals("foto") ){
                            Mensaje mensaje=snapshot1.getValue(Mensaje.class);
                            if(!mensaje.getMensajeTxt().equals(ms.getMensajeTxt())){
                                mensajes.add(mensaje);
                            }


                            adaptadorVistaImagenActivity.notifyDataSetChanged();
                        }
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