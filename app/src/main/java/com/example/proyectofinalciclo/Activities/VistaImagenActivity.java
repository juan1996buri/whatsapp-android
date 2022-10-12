package com.example.proyectofinalciclo.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Toast;

import com.example.proyectofinalciclo.Adaptadores.AdaptadorVistaImagenActivity;
import com.example.proyectofinalciclo.Clases.Mensaje;
import com.example.proyectofinalciclo.R;
import com.example.proyectofinalciclo.databinding.ActivityVistaImagenBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.smarteist.autoimageslider.IndicatorView.animation.type.IndicatorAnimationType;
import com.smarteist.autoimageslider.SliderAnimations;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class VistaImagenActivity extends AppCompatActivity {

    private ActivityVistaImagenBinding binding;
    private FirebaseDatabase database;
    private ArrayList<Mensaje> mensajes;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityVistaImagenBinding.inflate(getLayoutInflater());

        setContentView(binding.getRoot());
        database=FirebaseDatabase.getInstance();
        mensajes=new ArrayList<>();

        String imagen=getIntent().getStringExtra("imagen");
        String idReceiver=getIntent().getStringExtra("idReceiver");


        String idSender= FirebaseAuth.getInstance().getUid();

        AdaptadorVistaImagenActivity adaptadorVistaImagenActivity= new AdaptadorVistaImagenActivity(VistaImagenActivity.this,mensajes);
        binding.sliderView.setSliderAdapter(adaptadorVistaImagenActivity);


        database.getReference().child("Chats").child(idSender+idReceiver).child("mensaje").addValueEventListener(new ValueEventListener() {
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