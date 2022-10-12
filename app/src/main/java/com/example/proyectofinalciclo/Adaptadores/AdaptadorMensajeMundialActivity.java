package com.example.proyectofinalciclo.Adaptadores;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proyectofinalciclo.Activities.VistaImagenActivity;
import com.example.proyectofinalciclo.Activities.VistaImagenGrupoMundialActivity;
import com.example.proyectofinalciclo.Clases.Mensaje;
import com.example.proyectofinalciclo.Clases.Usuario;
import com.example.proyectofinalciclo.R;
import com.example.proyectofinalciclo.databinding.ItemMensajeEnvidoGrupoBinding;
import com.example.proyectofinalciclo.databinding.ItemMensajeRecibidoGrupoBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import static com.blankj.utilcode.util.ActivityUtils.startActivity;

public class AdaptadorMensajeMundialActivity extends RecyclerView.Adapter {
    private ArrayList<Mensaje> mensajes;
    private Context context;
    private String idGrupoMundial;
    private FirebaseDatabase database;

    private int ITEM_ENVIANDO = 1;
    private int ITEM_RECIBIENDO = 2;

    public AdaptadorMensajeMundialActivity(ArrayList<Mensaje> mensajes, Context context, String idGrupoMundial) {
        this.mensajes = mensajes;
        this.context = context;
        this.idGrupoMundial = idGrupoMundial;
        this.database = FirebaseDatabase.getInstance();
    }

    @NonNull
    @NotNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        if(viewType==ITEM_ENVIANDO){
            View view= LayoutInflater.from(context).inflate(R.layout.item_mensaje_envido_grupo,parent,false);

            return new ListadoMensajesEnviadosViewHolder(view);
        }else {
            View view=LayoutInflater.from(context).inflate(R.layout.item_mensaje_recibido_grupo,parent,false);

            return new ListadoMensajesRecibidosViewHolder(view);
        }

    }

    @Override
    public int getItemViewType(int position) {
        if(FirebaseAuth.getInstance().getCurrentUser().getUid().equals(mensajes.get(position).getIdMensajero())){
            return  ITEM_ENVIANDO;
        }else{
            return ITEM_RECIBIENDO;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull RecyclerView.ViewHolder holder, int position) {
        Mensaje mensaje=mensajes.get(position);

        String guardarFechaActual;
        Calendar calendar=Calendar.getInstance();
        SimpleDateFormat fechaActual=new SimpleDateFormat("dd-MMM-yyyy");
        guardarFechaActual=fechaActual.format(calendar.getTime());


        if(holder.getClass() == ListadoMensajesEnviadosViewHolder.class){
            ListadoMensajesEnviadosViewHolder viewHolder=(ListadoMensajesEnviadosViewHolder) holder;

            if(mensaje.getFechaMensaje().equals(guardarFechaActual)){
                viewHolder.bindingEnviado.fechaEnviado.setText("Hoy");
            }else {
                viewHolder.bindingEnviado.fechaEnviado.setText(mensaje.getFechaMensaje());
            }

            database.getReference().child("Usuarios").child(mensaje.getIdMensajero()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                    if(snapshot.exists()){
                        Usuario usuario =snapshot.getValue(Usuario.class);
                        Picasso.with(context).load(usuario.getImagenUsuario()).fit().centerCrop()
                                .placeholder(R.drawable.avatar)
                                .error(R.drawable.avatar)
                                .into(viewHolder.bindingEnviado.usuarioEnviado);
                    }
                }

                @Override
                public void onCancelled(@NonNull @NotNull DatabaseError error) {

                }
            });

            if(mensaje.getTipoMensaje().equals("texto") ){
                viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        CharSequence[] options = new CharSequence[]
                                {
                                        "Eliminar mensaje ",
                                };
                        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(context);
                        builder.setTitle("Selecione una opcion");
                        builder.setItems(options, (dialogInterface, i) -> {
                            if (i == 0){
                                 EliminarMensaje(mensaje.getIdMensaje());
                            }
                            if (i == 1){
                                // EliminarMensajeParaMi(mensaje.getIdMensaje());
                            }
                        });
                        builder.show();

                    }
                });
            }
            if(mensaje.getTipoMensaje().equals("pdf") || mensaje.getTipoMensaje().equals("word")){
                viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        CharSequence[] options = new CharSequence[]
                                {
                                        "Eliminar mensaje ",
                                        "Descargar documento"
                                };
                        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(context);
                        builder.setTitle("Selecione una opcion");
                        builder.setItems(options, (dialogInterface, i) -> {
                            if (i == 0){
                                EliminarMensaje(mensaje.getIdMensaje());
                            }
                            if (i == 1){
                                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(mensaje.getMensajeTxt())));

                                //EliminarMensajeParaMi(mensaje.getIdMensaje());
                            }
                            if(i==2){
                            }

                        });
                        builder.show();

                    }
                });
            }

            if(mensaje.getTipoMensaje().equals("imagen")){
                viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        CharSequence[] options = new CharSequence[]
                                {
                                        "Eliminar mensaje",
                                        "Ver imagen",
                                        "Descargar imagen"
                                };
                        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(context);
                        builder.setTitle("Selecione una opcion");
                        builder.setItems(options, (dialogInterface, i) -> {
                            if (i == 0){
                                 EliminarMensaje(mensaje.getIdMensaje());
                            }
                            if (i == 1){
                                Intent intent=new Intent(context, VistaImagenGrupoMundialActivity.class);
                                intent.putExtra("imagen",mensaje.getMensajeTxt());
                                intent.putExtra("idGrupoMundial",idGrupoMundial);
                                context.startActivity(intent);
                                //EliminarMensajeParaMi(mensaje.getIdMensaje());

                            }
                            if(i==2){

                            }
                            if(i==3){
                                //imageDownload(viewHolder.itemView.getContext(), mensaje.getMensajeTxt());
                            }

                        });
                        builder.show();

                    }
                });
            }



            if(mensaje.getTipoMensaje().equals("texto")){
                viewHolder.bindingEnviado.tiempoEnviado.setVisibility(View.VISIBLE);
                viewHolder.bindingEnviado.tiempoEnviado.setText(mensaje.getTiempoMensaje());
                viewHolder.bindingEnviado.mensajeEnviado.setVisibility(View.VISIBLE);
                viewHolder.bindingEnviado.mensajeEnviado.setText(mensaje.getMensajeTxt());
                viewHolder.bindingEnviado.tiempEnvMsgImagen.setVisibility(View.GONE);
                viewHolder.bindingEnviado.imagenEnviado.setVisibility(View.GONE);




            }else if(mensaje.getTipoMensaje().equals("imagen")){
                viewHolder.bindingEnviado.mensajeEnviado.setVisibility(View.GONE);
                viewHolder.bindingEnviado.tiempoEnviado.setVisibility(View.GONE);
                viewHolder.bindingEnviado.fechaEnviado.setVisibility(View.GONE);
                viewHolder.bindingEnviado.tiempEnvMsgImagen.setVisibility(View.VISIBLE);
                viewHolder.bindingEnviado.tiempEnvMsgImagen.setText(mensaje.getTiempoMensaje());
                viewHolder.bindingEnviado.imagenEnviado.setVisibility(View.VISIBLE);
                viewHolder.bindingEnviado.leidoReceiver.setVisibility(View.GONE);
                Picasso.with(context).load(mensaje.getMensajeTxt()).fit().centerCrop()
                        .placeholder(R.drawable.avatar)
                        .error(R.drawable.avatar)
                        .into(viewHolder.bindingEnviado.imagenEnviado);
            }else if(mensaje.getTipoMensaje().equals("pdf")){
                viewHolder.bindingEnviado.mensajeEnviado.setVisibility(View.GONE);
                viewHolder.bindingEnviado.tiempoEnviado.setVisibility(View.GONE);
                viewHolder.bindingEnviado.fechaEnviado.setVisibility(View.GONE);
                viewHolder.bindingEnviado.imagenEnviado.setVisibility(View.VISIBLE);
                viewHolder.bindingEnviado.tiempEnvMsgImagen.setVisibility(View.VISIBLE);
                viewHolder.bindingEnviado.tiempEnvMsgImagen.setText(mensaje.getTiempoMensaje());
                viewHolder.bindingEnviado.leidoReceiver.setVisibility(View.GONE);
                Picasso.with(context).load("https://firebasestorage.googleapis.com/v0/b/proyectofinal-129ab.appspot.com/o/ImagenesDocumentos%2Ficons8-pdf-96.png?alt=media&token=ad95b73f-61ec-4a48-b1a4-a082fa97a221").fit().centerCrop()
                        .placeholder(R.drawable.plceholder)
                        .error(R.drawable.plceholder)
                        .into(viewHolder.bindingEnviado.imagenEnviado);
            }else if(mensaje.getTipoMensaje().equals("word")){
                viewHolder.bindingEnviado.mensajeEnviado.setVisibility(View.GONE);
                viewHolder.bindingEnviado.tiempoEnviado.setVisibility(View.GONE);
                viewHolder.bindingEnviado.fechaEnviado.setVisibility(View.GONE);
                viewHolder.bindingEnviado.imagenEnviado.setVisibility(View.VISIBLE);
                viewHolder.bindingEnviado.tiempEnvMsgImagen.setVisibility(View.VISIBLE);
                viewHolder.bindingEnviado.tiempEnvMsgImagen.setText(mensaje.getTiempoMensaje());
                viewHolder.bindingEnviado.leidoReceiver.setVisibility(View.GONE);
                Picasso.with(context).load("https://firebasestorage.googleapis.com/v0/b/proyectofinal-129ab.appspot.com/o/ImagenesDocumentos%2Ficons8-word-96.png?alt=media&token=df19d628-905c-4314-b684-a8937e9de878").fit().centerCrop()
                        .placeholder(R.drawable.plceholder)
                        .error(R.drawable.plceholder)
                        .into(viewHolder.bindingEnviado.imagenEnviado);
            }else if(mensaje.getTipoMensaje().equals("eliminado")){
                viewHolder.bindingEnviado.tiempoEnviado.setVisibility(View.VISIBLE);
                viewHolder.bindingEnviado.tiempoEnviado.setText(mensaje.getTiempoMensaje());
                viewHolder.bindingEnviado.mensajeEnviado.setVisibility(View.VISIBLE);
                viewHolder.bindingEnviado.mensajeEnviado.setText(mensaje.getMensajeTxt());
                viewHolder.bindingEnviado.tiempEnvMsgImagen.setVisibility(View.GONE);
                viewHolder.bindingEnviado.imagenEnviado.setVisibility(View.GONE);
                viewHolder.bindingEnviado.leidoReceiver.setVisibility(View.GONE);
            }



        }else{
            ListadoMensajesRecibidosViewHolder viewHolder=(ListadoMensajesRecibidosViewHolder) holder;

            viewHolder.bindingRecibido.activo.setVisibility(View.GONE);


            if(mensaje.getFechaMensaje().equals(guardarFechaActual)){
                viewHolder.bindingRecibido.fechaRecibido.setText("Hoy");
            }else {
                viewHolder.bindingRecibido.fechaRecibido.setText(mensaje.getFechaMensaje());
            }

            database.getReference().child("Usuarios").child(mensaje.getIdMensajero()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                    if(snapshot.exists()){
                        Usuario usuario =snapshot.getValue(Usuario.class);
                        viewHolder.bindingRecibido.nombreRecib.setText(usuario.getNombreUsuario());
                        Picasso.with(context).load(usuario.getImagenUsuario()).fit().centerCrop()
                                .placeholder(R.drawable.avatar)
                                .error(R.drawable.avatar)
                                .into(viewHolder.bindingRecibido.usuarioRecibido);
                    }
                }

                @Override
                public void onCancelled(@NonNull @NotNull DatabaseError error) {

                }
            });

           /* if(mensaje.getTipoMensaje().equals("texto")){
                viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        CharSequence[] options = new CharSequence[]
                                {
                                        "Eliminar mensaje para mi"
                                };
                        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(context);
                        builder.setTitle("Selecione una opcion");
                        builder.setItems(options, (dialogInterface, i) -> {
                            if (i == 0){
                                // EliminarMensajeParaMi(mensaje.getIdMensaje());
                            }
                        });
                        builder.show();

                    }
                });

            }*/
            if(mensaje.getTipoMensaje().equals("pdf") || mensaje.getTipoMensaje().equals("word")){
                viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        CharSequence[] options = new CharSequence[]
                                {

                                        "Descargar documento"
                                };
                        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(context);
                        builder.setTitle("Selecione una opcion");
                        builder.setItems(options, (dialogInterface, i) -> {
                            if (i == 0){
                                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(mensaje.getMensajeTxt())));

                                // EliminarMensajeParaMi(mensaje.getIdMensaje());
                            }
                            if (i == 1){
                            }

                        });
                        builder.show();

                    }
                });

            }

            if(mensaje.getTipoMensaje().equals("imagen") || mensaje.getTipoMensaje().equals("foto")){
                viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        CharSequence[] options = new CharSequence[]
                                {

                                        "Ver imagen",
                                        "Descargar imagen"
                                };
                        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(context);
                        builder.setTitle("Selecione una opcion");
                        builder.setItems(options, (dialogInterface, i) -> {
                            if (i == 0){
                                Intent intent=new Intent(context, VistaImagenGrupoMundialActivity.class);
                                intent.putExtra("imagen",mensaje.getMensajeTxt());
                                intent.putExtra("idGrupoMundial",idGrupoMundial);
                                context.startActivity(intent);
                                // EliminarMensajeParaMi(mensaje.getIdMensaje());
                            }
                            if (i == 1){




                            }
                            if(i==2){
                                // imageDownload(viewHolder.itemView.getContext(), mensaje.getMensajeTxt());
                            }

                        });
                        builder.show();

                    }
                });

            }




            if(mensaje.getTipoMensaje().equals("texto")){
                viewHolder.bindingRecibido.tiempoRecibido.setVisibility(View.VISIBLE);
                viewHolder.bindingRecibido.tiempoRecibido.setText(mensaje.getTiempoMensaje());
                viewHolder.bindingRecibido.mensajeRecibido.setVisibility(View.VISIBLE);
                viewHolder.bindingRecibido.mensajeRecibido.setText(mensaje.getMensajeTxt());
                viewHolder.bindingRecibido.tiempRecibMsgImagen.setVisibility(View.GONE);
                viewHolder.bindingRecibido.imagenRecibido.setVisibility(View.GONE);

            }else if(mensaje.getTipoMensaje().equals("imagen")){
                viewHolder.bindingRecibido.mensajeRecibido.setVisibility(View.GONE);
                viewHolder.bindingRecibido.tiempoRecibido.setVisibility(View.GONE);
                viewHolder.bindingRecibido.fechaRecibido.setVisibility(View.GONE);
                viewHolder.bindingRecibido.imagenRecibido.setVisibility(View.VISIBLE);
                viewHolder.bindingRecibido.tiempRecibMsgImagen.setVisibility(View.VISIBLE);
                viewHolder.bindingRecibido.tiempRecibMsgImagen.setText(mensaje.getTiempoMensaje());
                Picasso.with(context).load(mensaje.getMensajeTxt()).fit().centerCrop()
                        .placeholder(R.drawable.avatar)
                        .error(R.drawable.avatar)
                        .into(viewHolder.bindingRecibido.imagenRecibido);
            }else if(mensaje.getTipoMensaje().equals("pdf")){
                viewHolder.bindingRecibido.mensajeRecibido.setVisibility(View.GONE);
                viewHolder.bindingRecibido.tiempoRecibido.setVisibility(View.GONE);
                viewHolder.bindingRecibido.fechaRecibido.setVisibility(View.GONE);
                viewHolder.bindingRecibido.imagenRecibido.setVisibility(View.VISIBLE);
                viewHolder.bindingRecibido.tiempRecibMsgImagen.setVisibility(View.VISIBLE);
                viewHolder.bindingRecibido.tiempRecibMsgImagen.setText(mensaje.getTiempoMensaje());
                Picasso.with(context).load("https://firebasestorage.googleapis.com/v0/b/proyectofinal-129ab.appspot.com/o/ImagenesDocumentos%2Ficons8-pdf-96.png?alt=media&token=ad95b73f-61ec-4a48-b1a4-a082fa97a221").fit().centerCrop()
                        .placeholder(R.drawable.plceholder)
                        .error(R.drawable.plceholder)
                        .into(viewHolder.bindingRecibido.imagenRecibido);
            }else if(mensaje.getTipoMensaje().equals("word")){
                viewHolder.bindingRecibido.mensajeRecibido.setVisibility(View.GONE);
                viewHolder.bindingRecibido.tiempoRecibido.setVisibility(View.GONE);
                viewHolder.bindingRecibido.fechaRecibido.setVisibility(View.GONE);
                viewHolder.bindingRecibido.imagenRecibido.setVisibility(View.VISIBLE);
                viewHolder.bindingRecibido.tiempRecibMsgImagen.setVisibility(View.VISIBLE);
                viewHolder.bindingRecibido.tiempRecibMsgImagen.setText(mensaje.getTiempoMensaje());
                Picasso.with(context).load("https://firebasestorage.googleapis.com/v0/b/proyectofinal-129ab.appspot.com/o/ImagenesDocumentos%2Ficons8-word-96.png?alt=media&token=df19d628-905c-4314-b684-a8937e9de878").fit().centerCrop()
                        .placeholder(R.drawable.plceholder)
                        .error(R.drawable.plceholder)
                        .into(viewHolder.bindingRecibido.imagenRecibido);
            }else if(mensaje.getTipoMensaje().equals("eliminado")){
                viewHolder.bindingRecibido.tiempoRecibido.setVisibility(View.VISIBLE);
                viewHolder.bindingRecibido.tiempoRecibido.setText(mensaje.getTiempoMensaje());
                viewHolder.bindingRecibido.mensajeRecibido.setVisibility(View.VISIBLE);
                viewHolder.bindingRecibido.mensajeRecibido.setText(mensaje.getMensajeTxt());
                viewHolder.bindingRecibido.tiempRecibMsgImagen.setVisibility(View.GONE);
                viewHolder.bindingRecibido.imagenRecibido.setVisibility(View.GONE);
            }


        }
    }

    private void EliminarMensaje(String idMensaje) {
        database.getReference().child("GruposMundiales").child(idGrupoMundial).child(idMensaje).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull @NotNull Task<Void> task) {
                if(task.isComplete()){
                    Toast.makeText(context, "mensaje eliminado", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mensajes.size();
    }


    public class ListadoMensajesEnviadosViewHolder extends RecyclerView.ViewHolder{
        ItemMensajeEnvidoGrupoBinding bindingEnviado;
        public ListadoMensajesEnviadosViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);
            bindingEnviado=ItemMensajeEnvidoGrupoBinding.bind(itemView);
        }
    }
    public class ListadoMensajesRecibidosViewHolder extends RecyclerView.ViewHolder{
        ItemMensajeRecibidoGrupoBinding bindingRecibido;
        public ListadoMensajesRecibidosViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);
            bindingRecibido=ItemMensajeRecibidoGrupoBinding.bind(itemView);
        }
    }
}