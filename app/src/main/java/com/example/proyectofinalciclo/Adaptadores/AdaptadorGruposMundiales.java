package com.example.proyectofinalciclo.Adaptadores;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proyectofinalciclo.Activities.MensajeGrupoMundialActivity;
import com.example.proyectofinalciclo.Clases.Grupo;
import com.example.proyectofinalciclo.R;
import com.example.proyectofinalciclo.databinding.ListadoGruposMundialesBinding;
import com.example.proyectofinalciclo.databinding.ListadoGruposPrivadosBinding;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class AdaptadorGruposMundiales extends RecyclerView.Adapter<AdaptadorGruposMundiales.ListadoGruposMundiales> {
    private Context context;
    private ArrayList<Grupo> grupos;
    public AdaptadorGruposMundiales(Context context, ArrayList<Grupo> grupos){
        this.context=context;
        this.grupos=grupos;
    }

    @NonNull
    @NotNull
    @Override
    public ListadoGruposMundiales onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(context).inflate(R.layout.listado_grupos_mundiales,parent,false);
        return new ListadoGruposMundiales(view);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull ListadoGruposMundiales holder, int position) {
        Grupo grupo=grupos.get(position);
        holder.binding.nombreGrupo.setText(grupo.getNombre());

        holder.binding.nombreGrupo.setSelected(true);

        holder.binding.descripcion.setText(grupo.getDescripcion());
        Picasso.with(context).load(grupo.getImagen()).fit().centerCrop()
                .placeholder(R.drawable.avatar)
                .error(R.drawable.avatar)
                .placeholder(R.drawable.avatar)
                .into(holder.binding.imagenDefecto);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(context, MensajeGrupoMundialActivity.class);
                intent.putExtra("idGrupo",grupo.getIdGrupo());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return grupos.size();
    }

    public class ListadoGruposMundiales extends RecyclerView.ViewHolder {
        ListadoGruposMundialesBinding binding;
        public ListadoGruposMundiales(@NonNull @NotNull View itemView) {
            super(itemView);
            binding=ListadoGruposMundialesBinding.bind(itemView);
        }
    }
}
