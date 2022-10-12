package com.example.proyectofinalciclo.Adaptadores;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proyectofinalciclo.Activities.MensajeGrupoMundialActivity;
import com.example.proyectofinalciclo.Activities.MensajeGrupoPrivadoActivity;
import com.example.proyectofinalciclo.Clases.Grupo;
import com.example.proyectofinalciclo.R;
import com.example.proyectofinalciclo.databinding.ListadoGruposMundialesBinding;
import com.example.proyectofinalciclo.databinding.ListadoGruposPrivadosBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class AdaptadorGruposPrivados  extends RecyclerView.Adapter<AdaptadorGruposPrivados.ListadoGruposPrivados> {
    private Context context;
    private ArrayList<Grupo> grupos;
    private FirebaseDatabase database;
    public AdaptadorGruposPrivados(Context context, ArrayList<Grupo> grupos){
        this.context=context;
        this.grupos=grupos;
        database=FirebaseDatabase.getInstance();
    }

    @NonNull
    @NotNull
    @Override
    public ListadoGruposPrivados onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(context).inflate(R.layout.listado_grupos_privados,parent,false);
        return new ListadoGruposPrivados(view);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull ListadoGruposPrivados holder, int position) {
        Grupo grupo=grupos.get(position);
        holder.binding.nombreGrupo.setText(grupo.getNombre());
        holder.binding.nombreGrupo.setSelected(true);

        Picasso.with(context).load(grupo.getImagen()).fit().centerCrop()
                .placeholder(R.drawable.avatar)
                .error(R.drawable.avatar)
                .placeholder(R.drawable.avatar)
                .into(holder.binding.imagenDefecto);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(context, MensajeGrupoPrivadoActivity.class);
                intent.putExtra("idGrupo",grupo.getIdGrupo());
                context.startActivity(intent);
            }
        });
        database.getReference().child("GruposPrivados").child(grupo.getIdGrupo()).child("ultimoMensaje").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    String fecha=snapshot.child("fecha").getValue(String.class);
                    String fecha_tiempo=snapshot.child("fecha_tiempo").getValue(String.class);
                    String mensaje=snapshot.child("mensaje").getValue(String.class);
                    String tiempo=snapshot.child("tiempo").getValue(String.class);
                    String tipoMensaje=snapshot.child("tipoMensaje").getValue(String.class);
                    holder.binding.fecha.setText(fecha);
                    holder.binding.tiempo.setText(tiempo);
                    if(tipoMensaje.equals("texto")){
                        holder.binding.descripcion.setText(mensaje);
                    }
                    if(tipoMensaje.equals("pdf") || tipoMensaje.equals("word")){
                        holder.binding.descripcion.setText("Archivo");
                    }
                    if(tipoMensaje.equals("foto") || tipoMensaje.equals("imagen")){
                        holder.binding.descripcion.setText("imagen");
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return grupos.size();
    }

    public class ListadoGruposPrivados extends RecyclerView.ViewHolder {
        ListadoGruposPrivadosBinding binding;
        public ListadoGruposPrivados(@NonNull @NotNull View itemView) {
            super(itemView);
            binding=ListadoGruposPrivadosBinding.bind(itemView);
        }
    }
}
