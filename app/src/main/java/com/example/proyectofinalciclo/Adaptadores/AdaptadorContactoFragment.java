package com.example.proyectofinalciclo.Adaptadores;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.proyectofinalciclo.Activities.InformacionUsuarioActivity;
import com.example.proyectofinalciclo.Clases.Usuario;
import com.example.proyectofinalciclo.R;
import com.example.proyectofinalciclo.databinding.ListadoUsuariosBinding;
import com.example.proyectofinalciclo.databinding.ListadoUsuariosContactosBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class AdaptadorContactoFragment extends RecyclerView.Adapter<AdaptadorContactoFragment.ListadoUsuariosViewHolder> {
    private ArrayList<Usuario> usuarios;
    private Context context;
    private FirebaseDatabase database;
    private FirebaseAuth auth;

    public AdaptadorContactoFragment(ArrayList<Usuario> usuarios, Context context) {
        this.usuarios = usuarios;
        this.context = context;
        database=FirebaseDatabase.getInstance();
        auth=FirebaseAuth.getInstance();
    }

    @NonNull
    @NotNull
    @Override
    public ListadoUsuariosViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
       View view= LayoutInflater.from(context).inflate(R.layout.listado_usuarios_contactos,parent,false);
        return new ListadoUsuariosViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull ListadoUsuariosViewHolder holder, int position) {
        Usuario usuario=usuarios.get(position);
        holder.binding.nombreDefecto.setText(usuario.getNombreUsuario());
        Glide
                .with(context)
                .load(usuario.getImagenUsuario())
                .centerCrop()
                .error(R.drawable.avatar)
                .into(holder.binding.imagenDefecto);
        database.getReference().child("Amistades").child(auth.getCurrentUser().getUid()).child(usuario.getIdUsuario()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    holder.binding.nombreDefecto.setVisibility(View.VISIBLE);
                    holder.binding.online.setVisibility(View.VISIBLE);
                    holder.binding.imagenDefecto.setVisibility(View.VISIBLE);


                }else{
                    holder.binding.nombreDefecto.setVisibility(View.GONE);
                    holder.binding.online.setVisibility(View.GONE);
                    holder.binding.activo.setVisibility(View.GONE);
                    holder.binding.imagenDefecto.setVisibility(View.GONE);
                    holder.binding.tiempo.setVisibility(View.GONE);
                    holder.binding.ultimaConexion.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });

        database.getReference().child("EstadoUsuario").child(usuario.getIdUsuario()).child("Estado").addValueEventListener(new ValueEventListener() {
            @SuppressLint("ResourceAsColor")
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    String estado=snapshot.child("estado").getValue(String.class);
                    String fecha=snapshot.child("fecha").getValue(String.class);
                    String hora=snapshot.child("hora").getValue(String.class);
                    if(estado.equals("Conectado") || estado.equals("escribiendo...")){
                        holder.binding.activo.setVisibility(View.VISIBLE);
                        holder.binding.online.setText("Conectado");
                        holder.binding.online.setTextColor(R.color.Green);
                        holder.binding.tiempo.setVisibility(View.GONE);
                        holder.binding.ultimaConexion.setVisibility(View.GONE);

                    }else{
                        holder.binding.online.setText("Desconectado");
                        holder.binding.tiempo.setText(fecha);
                        holder.binding.tiempo.setVisibility(View.VISIBLE);
                        holder.binding.ultimaConexion.setVisibility(View.VISIBLE);
                        holder.binding.activo.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(context, InformacionUsuarioActivity.class);
                intent.putExtra("idUsuario",usuario.getIdUsuario());
                context.startActivity(intent);
            }
        });

    }


    @Override
    public int getItemCount() {
        return usuarios.size();
    }

    public class ListadoUsuariosViewHolder extends RecyclerView.ViewHolder {
        ListadoUsuariosContactosBinding binding;
        public ListadoUsuariosViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);
            binding=ListadoUsuariosContactosBinding.bind(itemView);
        }
    }
}
