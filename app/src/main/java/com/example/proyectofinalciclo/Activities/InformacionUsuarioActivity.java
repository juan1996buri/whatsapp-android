package com.example.proyectofinalciclo.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.proyectofinalciclo.R;
import com.example.proyectofinalciclo.Clases.Usuario;
import com.example.proyectofinalciclo.databinding.ActivityInformacionUsuarioBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.HashMap;
import java.util.StringTokenizer;

public class InformacionUsuarioActivity extends AppCompatActivity {

    private ActivityInformacionUsuarioBinding binding;
    private FirebaseDatabase database;
    private FirebaseAuth auth;
    private String idReseiver,idSender;
    private String solicitud;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityInformacionUsuarioBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        database=FirebaseDatabase.getInstance();
        auth=FirebaseAuth.getInstance();

        idReseiver=getIntent().getStringExtra("idUsuario");
        idSender=auth.getCurrentUser().getUid();

        ObtenerDatosUsuario();
        AdministrarSolicitudChat();

        binding.swipe.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                AdministrarSolicitudChat();
                binding.swipe.setRefreshing(false);
            }
        });

        binding.retroceder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        solicitud="nuevo";
    }

    private void ObtenerDatosUsuario() {

        database.getReference().child("Usuarios").child(idReseiver).addValueEventListener(new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    Usuario usuario=snapshot.getValue(Usuario.class);
                    binding.descripcionDefecto.setText(usuario.getDescripcionUsuario());
                    binding.nombreDefecto.setText(usuario.getNombreUsuario());
                    binding.sexo.setText(usuario.getSexoUsuario());

                    EdadDelUsuario(usuario.getFechaNacimiento());

                    Picasso.with(InformacionUsuarioActivity.this).
                            load(usuario.getImagenUsuario()).fit().centerCrop()
                            .placeholder(R.drawable.avatar)
                            .error(R.drawable.avatar)
                            .placeholder(R.drawable.avatar)
                            .into(binding.imagenUsuario);
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });

    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    private void EdadDelUsuario(String fechaNacimiento) {

        String string = fechaNacimiento;
        String[] parts = string.split(" ");
        String mes = parts[0];
        String dia = parts[1];
        String año = parts[2];

        int mesUsuario=ComprobacionMesUsuario(mes);
        int diaUsuario=Integer.parseInt(dia);
        int añoUsuario=Integer.parseInt(año);

        String obtenerDiaActual;
        Calendar calendarDia=Calendar.getInstance();
        SimpleDateFormat diaActual=new SimpleDateFormat("dd");
        obtenerDiaActual=diaActual.format(calendarDia.getTime());

        String obtenerMesActual;
        Calendar calendarMes=Calendar.getInstance();
        SimpleDateFormat mesActual=new SimpleDateFormat("MMM");
        obtenerMesActual=mesActual.format(calendarMes.getTime());

        String obtenerAñoActual;
        Calendar calendarAño=Calendar.getInstance();
        SimpleDateFormat añoActual=new SimpleDateFormat("yyyy");
        obtenerAñoActual=añoActual.format(calendarAño.getTime());

        int mesActual_=ComprobacionMesUsuario(obtenerMesActual.toUpperCase());
        int diaActual_=Integer.parseInt(obtenerDiaActual);
        int añoActual_=Integer.parseInt(obtenerAñoActual);

        LocalDate start = LocalDate.of(añoUsuario, mesUsuario, diaUsuario);
        LocalDate end = LocalDate.of(añoActual_, mesActual_, diaActual_);
        long years = ChronoUnit.YEARS.between(start, end);

        String edad=String.valueOf(years);
        binding.edad.setText("Tengo "+edad+" años");
    }

    private int ComprobacionMesUsuario(String mesObtenido) {
        int mes=1;
        switch (mesObtenido){
            case "ENE":
                mes=1;
                break;
            case "FEB":
                mes=2;
                break;
            case "MAR":
                mes=3;
                break;
            case "ABR":
                mes=4;
                break;
            case "MAYO":
                mes=5;
                break;
            case "JUN":
                mes=6;
                break;
            case "JUL":
                mes=7;
                break;
            case "AGO":
                mes=8;
                break;
            case "SEPT":
                mes=9;
                break;
            case "OCT":
                mes=10;
                break;
            case "NOV":
                mes=11;
                break;
            case "DIC":
                mes=12;
                break;
        }
        return mes;
    }


    private void AdministrarSolicitudChat() {
        database.getReference().child("SolicitudAmistad").child(idSender).child(idReseiver).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if(snapshot.hasChildren()){
                    String estado=snapshot.child("tipoRequerimiento").getValue(String.class);

                    if(estado.equals("recibido")){
                        binding.enviar.setEnabled(true);
                        binding.enviar.setText("Aceptar Solicitud");
                        solicitud="amistad";
                        binding.cancelar.setText("Eliminar Solicitud");
                        binding.cancelar.setEnabled(true);
                        binding.cancelar.setVisibility(View.VISIBLE);
                        binding.cancelar.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                CancelarSolicitudAmistad();
                            }
                        });
                    }
                    if(estado.equals("enviado")){
                        binding.enviar.setEnabled(true);
                        binding.enviar.setText("Cancelar Solicitud");
                        solicitud="cancelar";
                    }
                }else{
                    database.getReference().child("Amistades").child(idSender).child(idReseiver).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                            if(snapshot.exists()){
                                String estado=snapshot.child("estado").getValue(String.class);
                                if(estado.equals("amigos")){
                                    binding.enviar.setEnabled(true);
                                    binding.enviar.setText("Eliminar Amigo");
                                    solicitud="eliminarAmigo";
                                    binding.cancelar.setEnabled(false);
                                    binding.cancelar.setVisibility(View.INVISIBLE);
                                }
                            }else {
                                binding.enviar.setEnabled(true);
                                binding.enviar.setText("Enviar Solicitud");
                                solicitud="nuevo";
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
        if(!idSender.equals(idReseiver)){
            binding.enviar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    binding.enviar.setEnabled(false);
                    if(solicitud.equals("nuevo")){
                        EnviarSolicitudAmistad();
                    }
                    if(solicitud.equals("cancelar")){
                        CancelarSolicitudAmistad();
                    }
                    if(solicitud.equals("amistad")){
                        AceptarSolicitudAmistad();
                    }
                    if(solicitud.equals("eliminarAmigo")){
                        EliminarAmigo();
                    }
                }
            });
        }
    }

    private void EnviarSolicitudAmistad() {
        database.getReference().child("SolicitudAmistad").child(idSender)
                .child(idReseiver).child("tipoRequerimiento").setValue("enviado").addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                database.getReference().child("SolicitudAmistad").child(idReseiver)
                        .child(idSender).child("tipoRequerimiento").setValue("recibido").addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull @NotNull Task<Void> task) {
                        if(task.isSuccessful()){
                            HashMap<String,Object> hashMap=new HashMap<>();
                            hashMap.put("de",idSender);
                            hashMap.put("tipo","solicitud");
                            database.getReference().child("Notificacion").child(idSender).child(idReseiver)
                                    .push().setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull @NotNull Task<Void> task) {
                                    solicitud="cancelar";
                                    binding.enviar.setText("Cancelar solicitud");
                                    binding.enviar.setEnabled(true);
                                }
                            });
                        }
                    }
                });
            }
        });
    }

    private void CancelarSolicitudAmistad() {
        database.getReference().child("SolicitudAmistad").child(idSender).child(idReseiver).removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        database.getReference().child("SolicitudAmistad").child(idReseiver).child(idSender).removeValue()
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull @NotNull Task<Void> task) {
                                        if(task.isSuccessful()){
                                            binding.enviar.setEnabled(true);
                                            binding.enviar.setText("Enviar Solicitus");
                                            solicitud="nuevo";
                                            binding.cancelar.setEnabled(false);
                                            binding.cancelar.setVisibility(View.INVISIBLE);
                                        }
                                    }
                                });
                    }
                });
    }

    private void AceptarSolicitudAmistad() {

        database.getReference().child("SolicitudAmistad").child(idSender).child(idReseiver).removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        database.getReference().child("SolicitudAmistad").child(idReseiver).child(idSender).removeValue()
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull @NotNull Task<Void> task) {
                                        if(task.isSuccessful()){
                                            database.getReference().child("Amistades").child(idSender).child(idReseiver)
                                                    .child("estado").setValue("amigos")
                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void unused) {
                                                            database.getReference().child("Amistades").child(idReseiver).child(idSender)
                                                                    .child("estado").setValue("amigos")
                                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull @NotNull Task<Void> task) {
                                                                            if(task.isSuccessful()){
                                                                                binding.enviar.setEnabled(true);
                                                                                binding.enviar.setText("Eliminar Amigo");

                                                                                binding.cancelar.setEnabled(false);
                                                                                binding.cancelar.setVisibility(View.INVISIBLE);

                                                                                String FechaActual, TiempoActual,Fecha_Tiempo;
                                                                                Calendar calendar=Calendar.getInstance();

                                                                                SimpleDateFormat fechaActual=new SimpleDateFormat("dd-MMM-yyyy");
                                                                                FechaActual=fechaActual.format(calendar.getTime());

                                                                                SimpleDateFormat tiempoActual=new SimpleDateFormat("hh:mm a");
                                                                                TiempoActual=tiempoActual.format(calendar.getTime());

                                                                                SimpleDateFormat fecha_tiempo=new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
                                                                                Fecha_Tiempo=fecha_tiempo.format(calendar.getTime());

                                                                                HashMap<String,Object> hashMap=new HashMap<>();
                                                                                hashMap.put("tipoMensaje","texto");
                                                                                hashMap.put("tiempo",TiempoActual);
                                                                                hashMap.put("fecha",FechaActual);
                                                                                hashMap.put("mensaje","escribir mensaje");
                                                                                hashMap.put("fecha_tiempo",Fecha_Tiempo);
                                                                                database.getReference().child("Chats").child(idSender+idReseiver).child("ultimoMensaje")
                                                                                        .updateChildren(hashMap)
                                                                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                            @Override
                                                                                            public void onSuccess(Void unused) {
                                                                                                database.getReference().child("Chats").child(idReseiver+idSender).child("ultimoMensaje")
                                                                                                        .updateChildren(hashMap)
                                                                                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                                            @Override
                                                                                                            public void onSuccess(Void unused) {

                                                                                                            }
                                                                                                        });
                                                                                            }
                                                                                        });
                                                                            }
                                                                        }
                                                                    });
                                                        }
                                                    });
                                        }
                                    }
                                });
                    }
                });
    }

    private void EliminarAmigo() {
        database.getReference().child("Amistades").child(idSender).child(idReseiver).removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        database.getReference().child("Amistades").child(idReseiver).child(idSender).removeValue()
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull @NotNull Task<Void> task) {
                                        if(task.isSuccessful()){
                                            binding.enviar.setEnabled(true);
                                            binding.enviar.setText("Enviar Solicitud");
                                            solicitud="nuevo";

                                        }
                                    }
                                });
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