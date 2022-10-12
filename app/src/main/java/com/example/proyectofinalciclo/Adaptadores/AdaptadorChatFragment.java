package com.example.proyectofinalciclo.Adaptadores;


import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.example.proyectofinalciclo.Activities.MensajeUsuarioActivity;
import com.example.proyectofinalciclo.Clases.Mensaje;
import com.example.proyectofinalciclo.Clases.Usuario;
import com.example.proyectofinalciclo.R;
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

public class AdaptadorChatFragment extends RecyclerView.Adapter<AdaptadorChatFragment.ListadoUsuariosChatHolderView> {
    private ArrayList<Usuario> usuarios;
    private Context context;
    private FirebaseDatabase database;
    private FirebaseAuth auth;

    public AdaptadorChatFragment(ArrayList<Usuario> usuarios, Context context) {
        this.usuarios = usuarios;
        this.context = context;
        database= FirebaseDatabase.getInstance();
        auth= FirebaseAuth.getInstance();
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


        Picasso.with(context).load(usuario.getImagenUsuario()).fit().centerCrop()
                .placeholder(R.drawable.avatar)
                .error(R.drawable.avatar)
                .placeholder(R.drawable.avatar)
                .into(holder.binding.imagenDefecto);



        database.getReference().child("Amistades").child(auth.getCurrentUser().getUid()).child(usuario.getIdUsuario()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    holder.binding.nombreDefecto.setVisibility(View.VISIBLE);
                    holder.binding.imagenDefecto.setVisibility(View.VISIBLE);
                    holder.binding.tiempoUltMensaje.setVisibility(View.VISIBLE);
                    holder.binding.ultimoMensaje.setVisibility(View.VISIBLE);

                    String idsender=FirebaseAuth.getInstance().getCurrentUser().getUid();


                    ArrayList<String> fechaUltMensaje=new ArrayList<>();
                    ArrayList<String> txtUltMensaje=new ArrayList<>();
                    ArrayList<String> tiempoUltMensaje=new ArrayList<>();
                    ArrayList<String> tipoMensajeUlt=new ArrayList<>();


                    database.getReference().child("Chats").child(idsender+usuario.getIdUsuario()).child("ultimoMensaje").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                            if(snapshot.exists()){


                                fechaUltMensaje.clear();
                                txtUltMensaje.clear();
                                tiempoUltMensaje.clear();
                                tiempoUltMensaje.clear();

                                String fechaUltMensaje_=snapshot.child("fecha").getValue(String.class);
                                fechaUltMensaje.add(fechaUltMensaje_);
                                String txtUltMensaje_=snapshot.child("mensaje").getValue(String.class);
                                txtUltMensaje.add(txtUltMensaje_);
                                String tiempoUltMensaje_=snapshot.child("tiempo").getValue(String.class);
                                tiempoUltMensaje.add(tiempoUltMensaje_);
                                String tipoMensajeUlt_=snapshot.child("tipoMensaje").getValue(String.class);
                                tipoMensajeUlt.add(tipoMensajeUlt_);
                            }

                            database.getReference().child("Chats").child(FirebaseAuth.getInstance().getUid()+usuario.getIdUsuario()).child("mensajesPendientes").addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                                    if(snapshot.exists()){
                                        int cant=snapshot.child("cantidad").getValue(int.class);
                                        String catMsg=String.valueOf(cant);
                                        holder.binding.contMensajes.setVisibility(View.VISIBLE);
                                        holder.binding.contMensajes.setText(catMsg);
                                        holder.binding.tiempoUltMensaje.setVisibility(View.INVISIBLE);


                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull @NotNull DatabaseError error) {

                                }
                            });


                        }

                        @Override
                        public void onCancelled(@NonNull @NotNull DatabaseError error) {

                        }
                    });

                    database.getReference().child("Chats").child(usuario.getIdUsuario()+idsender).child("Estado").child(usuario.getIdUsuario()).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                            if(snapshot.exists()){



                                String estado=snapshot.child("estado").getValue(String.class);
                                if(estado.equals("escribiendo...")){
                                    Resources res = context.getApplicationContext().getResources();
                                    final int newColor = res.getColor(R.color.Green);
                                    holder.binding.ultimoMensaje.setTextColor(newColor);
                                    holder.binding.ultimoMensaje.setTypeface(null, Typeface.BOLD);
                                    holder.binding.ultimoMensaje.setText(estado);
                                }else if(estado.equals("Desconectado") || estado.equals("Conectado")){

                                    holder.binding.ultimoMensaje.setText(estado);

                                    if(!tiempoUltMensaje.isEmpty() && !fechaUltMensaje.isEmpty()){

                                        holder.binding.tiempoUltMensaje.setText(tiempoUltMensaje.get(0));
                                        holder.binding.fechaUltimoMensaje.setText(fechaUltMensaje.get(0));


                                        Resources res = context.getApplicationContext().getResources();
                                        final int newColor = res.getColor(R.color.black);
                                        holder.binding.ultimoMensaje.setTextColor(newColor);
                                         holder.binding.ultimoMensaje.setTypeface(null, Typeface.NORMAL);

                                        if(tipoMensajeUlt.get(0).equals("texto")){
                                            holder.binding.ultimoMensaje.setText(txtUltMensaje.get(0));
                                        }
                                        if(tipoMensajeUlt.get(0).equals("imagen")){
                                            holder.binding.ultimoMensaje.setText("imagen");
                                        }
                                        if(tipoMensajeUlt.get(0).equals("pdf")){
                                            holder.binding.ultimoMensaje.setText("pdf");
                                        }
                                        if(tipoMensajeUlt.get(0).equals("word")){
                                            holder.binding.ultimoMensaje.setText("word");
                                        }
                                    }


                                }


                            }
                        }

                        @Override
                        public void onCancelled(@NonNull @NotNull DatabaseError error) {

                        }
                    });


                    database.getReference().child("Chats").child(FirebaseAuth.getInstance().getCurrentUser().getUid()+usuario.getIdUsuario()).child("mensajesPendientes").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                            if(!snapshot.exists()){
                                holder.binding.contMensajes.setVisibility(View.GONE);
                                holder.binding.tiempoUltMensaje.setVisibility(View.VISIBLE);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull @NotNull DatabaseError error) {

                        }
                    });
                    holder.itemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            database.getReference().child("Chats").child(FirebaseAuth.getInstance().getCurrentUser().getUid()+usuario.getIdUsuario()).child("mensajesPendientes").removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {


                                }
                            });

                            ArrayList<Mensaje> mensajes=new ArrayList<>();

                            database.getReference().child("Chats").child(FirebaseAuth.getInstance().getCurrentUser().getUid()+usuario.getIdUsuario()).child("mensajesPendientes").addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                                    if(!snapshot.exists()){

                                        database.getReference().child("Chats").child(usuario.getIdUsuario()+FirebaseAuth.getInstance().getCurrentUser().getUid()).child("mensaje").addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                                                if(snapshot.exists()){
                                                    mensajes.clear();

                                                    for(DataSnapshot snapshot1:snapshot.getChildren()){

                                                        Mensaje mensaje= snapshot1.getValue(Mensaje.class);
                                                        String idMensaje=snapshot1.getKey();
                                                        mensaje.setIdMensaje(idMensaje);

                                                        mensajes.add(mensaje);

                                                        HashMap<String,Object> hashMap=new HashMap<>();
                                                        hashMap.put("mensajeLeido","si");
                                                        database.getReference().child("Chats").child(usuario.getIdUsuario()+FirebaseAuth.getInstance().getUid()).child("mensaje").child(idMensaje).updateChildren(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void unused) {

                                                            }
                                                        });
                                                    }
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
                            holder.binding.contMensajes.setVisibility(View.GONE);
                            Intent intent=new Intent(context, MensajeUsuarioActivity.class);
                            intent.putExtra("idReceiver",usuario.getIdUsuario());
                            context.startActivity(intent);
                        }
                    });

                }else{
                    holder.binding.nombreDefecto.setVisibility(View.GONE);
                    holder.binding.imagenDefecto.setVisibility(View.GONE);
                    holder.binding.tiempoUltMensaje.setVisibility(View.GONE);
                    holder.binding.ultimoMensaje.setVisibility(View.GONE);
                    holder.binding.contMensajes.setVisibility(View.GONE);
                    holder.binding.fechaUltimoMensaje.setVisibility(View.GONE);
                    //holder.binding.view.setVisibility(View.GONE);
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


    public class ListadoUsuariosChatHolderView extends RecyclerView.ViewHolder {
        private ListadoUsuariosChatBinding binding;
        public ListadoUsuariosChatHolderView(@NonNull @NotNull View itemView) {
            super(itemView);
            binding=ListadoUsuariosChatBinding.bind(itemView);
        }
    }
}
