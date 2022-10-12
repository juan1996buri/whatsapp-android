package com.example.proyectofinalciclo.Adaptadores;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proyectofinalciclo.Clases.Usuario;
import com.example.proyectofinalciclo.R;
import com.example.proyectofinalciclo.databinding.ListadoNuevoMiembroGrupoPrivadoBinding;
import com.example.proyectofinalciclo.databinding.ListadoUsuariosChatBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class AdaptadorEliminarMiembroGrupoPrivado extends RecyclerView.Adapter<AdaptadorEliminarMiembroGrupoPrivado.ListadoUsuariosChatHolderView> {
    private ArrayList<Usuario> usuarios;
    private Context context;
    private FirebaseDatabase database;
    private FirebaseAuth auth;
    private String idGrupoPrivado;

    public AdaptadorEliminarMiembroGrupoPrivado(ArrayList<Usuario> usuarios, Context context,String idGrupoPrivado) {
        this.usuarios = usuarios;
        this.context = context;
        database= FirebaseDatabase.getInstance();
        auth= FirebaseAuth.getInstance();
        this.idGrupoPrivado=idGrupoPrivado;
    }

    @NonNull
    @NotNull
    @Override
    public ListadoUsuariosChatHolderView onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(context).inflate(R.layout.listado_usuarios_chat,parent,false);

        return new ListadoUsuariosChatHolderView(view);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull ListadoUsuariosChatHolderView holder, int position) {
        Usuario usuario=usuarios.get(position);


        holder.binding.nombreDefecto.setText(usuario.getNombreUsuario());
        holder.binding.tiempoUltMensaje.setVisibility(View.GONE);


        database.getReference().child("Amistades").child(auth.getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    for(DataSnapshot snapshot1:snapshot.getChildren()){
                        String id= snapshot1.getKey();

                        if(id.equals(usuario.getIdUsuario())){
                            Log.i("respuesta",id);
                            Log.i("respuesta",usuario.getNombreUsuario());
                            holder.binding.fechaUltimoMensaje.setText("Amigo");
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });

        if(auth.getCurrentUser().getUid().equals(usuario.getIdUsuario())){
            holder.binding.fechaUltimoMensaje.setText("Yo");
        }else{
            holder.binding.fechaUltimoMensaje.setText("Desconocido");
        }




        Picasso.with(context).load(usuario.getImagenUsuario()).fit().centerCrop()
                .placeholder(R.drawable.avatar)
                .error(R.drawable.avatar)
                .placeholder(R.drawable.avatar)
                .into(holder.binding.imagenDefecto);

        database.getReference().child("DatosGruposPrivados").child(idGrupoPrivado).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    String id=snapshot.child("idUsuario").getValue(String.class);
                    Resources res = context.getApplicationContext().getResources();
                    final int newColor = res.getColor(R.color.Green);
                    holder.binding.ultimoMensaje.setTextColor(newColor);
                    holder.binding.ultimoMensaje.setTypeface(null, Typeface.BOLD);

                    if(id.equals(usuario.getIdUsuario())){
                        holder.binding.ultimoMensaje.setText("Admistrador");


                    }else{
                        holder.binding.ultimoMensaje.setText("Miembro");
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

                Resources res = context.getApplicationContext().getResources();
                final int desmarcar = res.getColor(R.color.item);

                Resources resp = context.getApplicationContext().getResources();
                final int marcar = resp.getColor(R.color.marcarItem);

                usuario.setSelected(!usuario.isSelected());
                holder.binding.cardViewEliminarUsuario.setBackgroundColor(usuario.isSelected() ? marcar : desmarcar);
            }
        });

        ArrayList<String> amigos=new ArrayList<>();

    }

    @Override
    public int getItemCount() {
        return usuarios.size();
    }


    public class ListadoUsuariosChatHolderView extends RecyclerView.ViewHolder {
        private ListadoUsuariosChatBinding binding;
        public ListadoUsuariosChatHolderView(@NonNull @NotNull View itemView) {
            super(itemView);
            binding=ListadoUsuariosChatBinding.bind(itemView);
        }
    }
}
