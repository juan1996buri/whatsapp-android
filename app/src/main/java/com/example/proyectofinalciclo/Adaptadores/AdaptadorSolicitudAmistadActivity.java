package com.example.proyectofinalciclo.Adaptadores;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.proyectofinalciclo.Clases.Usuario;
import com.example.proyectofinalciclo.R;
import com.example.proyectofinalciclo.databinding.ListadoUsuariosSolicitudesBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

public class AdaptadorSolicitudAmistadActivity extends RecyclerView.Adapter<AdaptadorSolicitudAmistadActivity.ListadoUsuarioSolicitudHolderView> {
    private ArrayList<Usuario> usuarios;
    private Context context;
    private FirebaseDatabase database;

    public AdaptadorSolicitudAmistadActivity(ArrayList<Usuario> usuarios, Context context) {
        this.usuarios = usuarios;
        this.context = context;
        database=FirebaseDatabase.getInstance();
    }

    @NonNull
    @NotNull
    @Override
    public ListadoUsuarioSolicitudHolderView onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(context).inflate(R.layout.listado_usuarios_solicitudes,parent,false);
        return new ListadoUsuarioSolicitudHolderView(view);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull ListadoUsuarioSolicitudHolderView holder, int position) {
        Usuario usuario=usuarios.get(position);
        holder.binding.nombre.setVisibility(View.VISIBLE);
        holder.binding.descripcion.setVisibility(View.VISIBLE);
        holder.binding.imagen.setVisibility(View.VISIBLE);
        holder.binding.cancelar.setVisibility(View.VISIBLE);
        holder.binding.aceptar.setVisibility(View.VISIBLE);
        holder.binding.nombre.setText(usuario.getNombreUsuario());
        holder.binding.descripcion.setText(usuario.getSexoUsuario());

        Glide
                .with(context)
                .load(usuario.getImagenUsuario())
                .centerCrop()
                .error(R.drawable.avatar)
                .into(holder.binding.imagen);


        database.getReference().child("SolicitudAmistad").child(FirebaseAuth.getInstance().getUid()).child(usuario.getIdUsuario()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    holder.binding.aceptar.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            database.getReference().child("SolicitudAmistad").child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                    .child(usuario.getIdUsuario()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    database.getReference().child("SolicitudAmistad").child(usuario.getIdUsuario())
                                            .child(FirebaseAuth.getInstance().getCurrentUser().getUid()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull @NotNull Task<Void> task) {
                                            if(task.isSuccessful()){
                                                database.getReference().child("Amistades").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child(usuario.getIdUsuario())
                                                        .child("estado").setValue("amigos")
                                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void unused) {
                                                                database.getReference().child("Amistades").child(usuario.getIdUsuario()).child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                                                        .child("estado").setValue("amigos")
                                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                            @Override
                                                                            public void onComplete(@NonNull @NotNull Task<Void> task) {
                                                                                if(task.isSuccessful()){

                                                                                    Toast.makeText(context, "Solicitud aceptada", Toast.LENGTH_SHORT).show();
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
                                                                                    database.getReference().child("Chats").child(FirebaseAuth.getInstance().getCurrentUser().getUid()+usuario.getIdUsuario()).child("ultimoMensaje")
                                                                                            .updateChildren(hashMap)
                                                                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                                @Override
                                                                                                public void onSuccess(Void unused) {
                                                                                                    database.getReference().child("Chats").child(usuario.getIdUsuario()+FirebaseAuth.getInstance().getCurrentUser().getUid()).child("ultimoMensaje")
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
                    });
                    holder.binding.cancelar.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            database.getReference().child("SolicitudAmistad").child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                    .child(usuario.getIdUsuario()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {

                                    database.getReference().child("SolicitudAmistad").child(usuario.getIdUsuario())
                                            .child(FirebaseAuth.getInstance().getCurrentUser().getUid()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull @NotNull Task<Void> task) {
                                            if(task.isSuccessful()){

                                                Toast.makeText(context, "solicitud cancelada", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                                }
                            });
                        }
                    });
                }else{
                    holder.binding.nombre.setVisibility(View.GONE);
                    holder.binding.descripcion.setVisibility(View.GONE);
                    holder.binding.imagen.setVisibility(View.GONE);
                    holder.binding.cancelar.setVisibility(View.GONE);
                    holder.binding.aceptar.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return usuarios.size();
    }

    public class ListadoUsuarioSolicitudHolderView extends RecyclerView.ViewHolder {
        private ListadoUsuariosSolicitudesBinding binding;
        public ListadoUsuarioSolicitudHolderView(@NonNull @NotNull View itemView) {
            super(itemView);
            binding=ListadoUsuariosSolicitudesBinding.bind(itemView);
        }
    }
}
