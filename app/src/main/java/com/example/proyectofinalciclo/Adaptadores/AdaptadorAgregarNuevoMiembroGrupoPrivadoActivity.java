package com.example.proyectofinalciclo.Adaptadores;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proyectofinalciclo.Activities.MensajeUsuarioActivity;
import com.example.proyectofinalciclo.Clases.Mensaje;
import com.example.proyectofinalciclo.Clases.Usuario;
import com.example.proyectofinalciclo.R;
import com.example.proyectofinalciclo.databinding.ListadoNuevoMiembroGrupoPrivadoBinding;
import com.example.proyectofinalciclo.databinding.ListadoUsuariosChatBinding;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;

public class AdaptadorAgregarNuevoMiembroGrupoPrivadoActivity extends RecyclerView.Adapter<AdaptadorAgregarNuevoMiembroGrupoPrivadoActivity.ListadoUsuariosChatHolderView> {
    private ArrayList<Usuario> usuarios;
    private Context context;
    private FirebaseDatabase database;
    private FirebaseAuth auth;

    public AdaptadorAgregarNuevoMiembroGrupoPrivadoActivity(ArrayList<Usuario> usuarios, Context context) {
        this.usuarios = usuarios;
        this.context = context;
        database= FirebaseDatabase.getInstance();
        auth= FirebaseAuth.getInstance();
    }

    @NonNull
    @NotNull
    @Override
    public ListadoUsuariosChatHolderView onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(context).inflate(R.layout.listado_nuevo_miembro_grupo_privado,parent,false);

        return new ListadoUsuariosChatHolderView(view);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull ListadoUsuariosChatHolderView holder, int position) {
        Usuario usuario=usuarios.get(position);

        holder.binding.nombreDefecto.setText(usuario.getNombreUsuario());

        holder.binding.ultimoMensaje.setText("Amigo");
        database.getReference().child("EstadoUsuario").child(usuario.getIdUsuario()).child("Estado").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    String estado=snapshot.child("estado").getValue(String.class);
                    if(estado.equals("Conectado")){
                        Resources res = context.getResources();
                        final int newColor = res.getColor(R.color.Green);
                        holder.binding.tipo.setTextColor(newColor);
                        holder.binding.tipo.setText("Conectado");
                    }else{
                        Resources res = context.getResources();
                        final int newColor = res.getColor(R.color.black);
                        holder.binding.tipo.setTextColor(newColor);
                        holder.binding.tipo.setText("Desconectado");
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });


        Picasso.with(context).load(usuario.getImagenUsuario()).fit().centerCrop()
                .placeholder(R.drawable.avatar)
                .error(R.drawable.avatar)
                .placeholder(R.drawable.avatar)
                .into(holder.binding.imagenDefecto);

                    holder.itemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            Resources res = context.getApplicationContext().getResources();
                            final int desmarcar = res.getColor(R.color.item);

                            Resources resp = context.getApplicationContext().getResources();
                            final int marcar = resp.getColor(R.color.marcarItem);

                            usuario.setSelected(!usuario.isSelected());
                            holder.binding.cardViewAgregarMiembro.setBackgroundColor(usuario.isSelected() ? marcar : desmarcar);
                        }
                    });
    }

    @Override
    public int getItemCount() {
        return  usuarios == null ? 0 :usuarios.size();
    }

    public class ListadoUsuariosChatHolderView extends RecyclerView.ViewHolder {
        private ListadoNuevoMiembroGrupoPrivadoBinding binding;
        public ListadoUsuariosChatHolderView(@NonNull @NotNull View itemView) {
            super(itemView);
            binding=ListadoNuevoMiembroGrupoPrivadoBinding.bind(itemView);
        }
    }
}
