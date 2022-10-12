package com.example.proyectofinalciclo.Adaptadores;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.proyectofinalciclo.Activities.ConfiguracionPerfilPersonalActivity;
import com.example.proyectofinalciclo.Activities.InformacionUsuarioActivity;
import com.example.proyectofinalciclo.Clases.Usuario;
import com.example.proyectofinalciclo.R;
import com.example.proyectofinalciclo.databinding.ListadoUsuariosBinding;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class AdaptadorAgregarAmigosActivity extends RecyclerView.Adapter<AdaptadorAgregarAmigosActivity.ListadoUsuariosHolderView>  {

    private Context context;
    private ArrayList<Usuario> usuarios;
    public AdaptadorAgregarAmigosActivity(Context context, ArrayList<Usuario> usuarios) {
        this.context = context;
        this.usuarios=usuarios;
    }

    @NonNull
    @NotNull
    @Override
    public ListadoUsuariosHolderView onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(context).inflate(R.layout.listado_usuarios,parent,false);

        return new ListadoUsuariosHolderView(view);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull ListadoUsuariosHolderView holder, int position) {
        Usuario usuario=usuarios.get(position);
        holder.binding.nombreDefecto.setText(usuario.getNombreUsuario());
        holder.binding.sexo.setText(usuario.getSexoUsuario());

        Picasso.with(context).load(usuario.getImagenUsuario())
                .placeholder(R.drawable.avatar)
                .error(R.drawable.avatar)
                .placeholder(R.drawable.avatar)
                .into(holder.binding.imagenDefecto);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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

    public class ListadoUsuariosHolderView extends RecyclerView.ViewHolder {
        ListadoUsuariosBinding binding;
        public ListadoUsuariosHolderView(@NonNull @NotNull View itemView) {
            super(itemView);
            binding=ListadoUsuariosBinding.bind(itemView);
        }
    }
}
