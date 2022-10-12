package com.example.proyectofinalciclo.Adaptadores;

import android.Manifest;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proyectofinalciclo.Activities.ConfiguracionPerfilPersonalActivity;
import com.example.proyectofinalciclo.Activities.MensajeUsuarioActivity;
import com.example.proyectofinalciclo.Activities.VistaImagenActivity;
import com.example.proyectofinalciclo.Clases.Mensaje;
import com.example.proyectofinalciclo.R;
import com.example.proyectofinalciclo.databinding.ItemMensajeEnviadoBinding;
import com.example.proyectofinalciclo.databinding.ItemMensajeRecibidoBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import static com.blankj.utilcode.util.ActivityUtils.startActivity;

public class AdaptadorMensajeUsuarioActivity extends RecyclerView.Adapter {

    private ArrayList<Mensaje> mensajes;
    private Context context;
    private String idReceiver;
    private FirebaseDatabase database;

    private int ITEM_ENVIANDO=1;
    private int ITEM_RECIBIENDO=2;

    public AdaptadorMensajeUsuarioActivity(ArrayList<Mensaje> mensajes, Context context, String idReceiver) {
        this.mensajes = mensajes;
        this.context = context;
        this.idReceiver=idReceiver;
        this.database=FirebaseDatabase.getInstance();
    }

    @NonNull
    @NotNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        if(viewType==ITEM_ENVIANDO){
            View view= LayoutInflater.from(context).inflate(R.layout.item_mensaje_enviado,parent,false);
            return  new ListadoMensajesEnviadosViewHolder(view);
        }else {
            View view= LayoutInflater.from(context).inflate(R.layout.item_mensaje_recibido,parent,false);
            return  new ListadoMensajesRecibidosViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull RecyclerView.ViewHolder holder, int position) {
        Mensaje mensaje=mensajes.get(position);

        String guardarFechaActual;
        Calendar calendar=Calendar.getInstance();
        SimpleDateFormat fechaActual=new SimpleDateFormat("dd-MMM-yyyy");
        guardarFechaActual=fechaActual.format(calendar.getTime());


        if(holder.getClass()==ListadoMensajesEnviadosViewHolder.class){
            ListadoMensajesEnviadosViewHolder viewHolder=(ListadoMensajesEnviadosViewHolder) holder;

            if(mensaje.getFechaMensaje().equals(guardarFechaActual)){
                viewHolder.bindingEnviado.fechaEnviado.setText("Hoy");
            }else {
                viewHolder.bindingEnviado.fechaEnviado.setText(mensaje.getFechaMensaje());
            }

            if(mensaje.getTipoMensaje().equals("texto") ){
                viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        CharSequence[] options = new CharSequence[]
                                {
                                        "Eliminar mensaje para todos",
                                        "Eliminar mensaje para mi"
                                };
                        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(context);
                        builder.setTitle("Selecione una opcion");
                        builder.setItems(options, (dialogInterface, i) -> {
                            if (i == 0){
                                EliminarMensajeTodos(mensaje.getIdMensaje());
                            }
                            if (i == 1){
                                EliminarMensajeParaMi(mensaje.getIdMensaje());
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
                                        "Eliminar mensaje para todos",
                                        "Eliminar mensaje para mi",
                                        "Descargar documento"
                                };
                        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(context);
                        builder.setTitle("Selecione una opcion");
                        builder.setItems(options, (dialogInterface, i) -> {
                            if (i == 0){
                                EliminarMensajeTodos(mensaje.getIdMensaje());
                            }
                            if (i == 1){
                                EliminarMensajeParaMi(mensaje.getIdMensaje());
                            }
                            if(i==2){
                                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(mensaje.getMensajeTxt())));
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
                                        "Eliminar mensaje para todos",
                                        "Eliminar mensaje para mi",
                                        "Ver imagen",
                                        "Descargar imagen"
                                };
                        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(context);
                        builder.setTitle("Selecione una opcion");
                        builder.setItems(options, (dialogInterface, i) -> {
                            if (i == 0){
                                EliminarMensajeTodos(mensaje.getIdMensaje());
                            }
                            if (i == 1){
                                EliminarMensajeParaMi(mensaje.getIdMensaje());

                            }
                            if(i==2){
                                Intent intent=new Intent(context, VistaImagenActivity.class);
                                intent.putExtra("imagen",mensaje.getMensajeTxt());
                                intent.putExtra("idReceiver",idReceiver);
                                context.startActivity(intent);
                            }
                            if(i==3){
                                imageDownload(viewHolder.itemView.getContext(), mensaje.getMensajeTxt());
                            }

                        });
                        builder.show();

                    }
                });
            }

            viewHolder.bindingEnviado.leidoReceiver.setVisibility(View.VISIBLE);

               for(Mensaje msg:mensajes){
                    if(msg.getMensajeLeido().equals("si")){
                        if(mensaje.getMensajeLeido().equals("si")){
                            Resources res = context.getResources();
                            final int newColor = res.getColor(R.color.visto);
                            viewHolder.bindingEnviado.leidoReceiver.setColorFilter(newColor, PorterDuff.Mode.SRC_ATOP);
                            viewHolder.bindingEnviado.leidoReceiverImag.setColorFilter(newColor, PorterDuff.Mode.SRC_ATOP);
                        }else{
                            Resources res = context.getResources();
                            final int newColor = res.getColor(R.color.white);
                            viewHolder.bindingEnviado.leidoReceiver.setColorFilter(newColor, PorterDuff.Mode.SRC_ATOP);
                            viewHolder.bindingEnviado.leidoReceiverImag.setColorFilter(newColor, PorterDuff.Mode.SRC_ATOP);
                        }
                    }
                }




            if(mensaje.getTipoMensaje().equals("texto")){
                viewHolder.bindingEnviado.tiempoEnviado.setVisibility(View.VISIBLE);
                viewHolder.bindingEnviado.tiempoEnviado.setText(mensaje.getTiempoMensaje());
                viewHolder.bindingEnviado.mensajeEnviado.setVisibility(View.VISIBLE);
                viewHolder.bindingEnviado.mensajeEnviado.setText(mensaje.getMensajeTxt());
                viewHolder.bindingEnviado.tiempEnvMsgImagen.setVisibility(View.GONE);
                viewHolder.bindingEnviado.imagenEnviado.setVisibility(View.GONE);
                viewHolder.bindingEnviado.leidoReceiverImag.setVisibility(View.GONE);




            }else if(mensaje.getTipoMensaje().equals("imagen")){
                viewHolder.bindingEnviado.leidoReceiverImag.setVisibility(View.VISIBLE);
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
                viewHolder.bindingEnviado.leidoReceiverImag.setVisibility(View.VISIBLE);
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
                viewHolder.bindingEnviado.leidoReceiverImag.setVisibility(View.VISIBLE);
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

            if(mensaje.getFechaMensaje().equals(guardarFechaActual)){
                viewHolder.bindingRecibido.fechaRecibido.setText("Hoy");
            }else {
                viewHolder.bindingRecibido.fechaRecibido.setText(mensaje.getFechaMensaje());
            }



            if(mensaje.getTipoMensaje().equals("texto")){
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
                                EliminarMensajeParaMi(mensaje.getIdMensaje());
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
                                        "Eliminar mensaje para mi",
                                        "Descargar documento"
                                };
                        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(context);
                        builder.setTitle("Selecione una opcion");
                        builder.setItems(options, (dialogInterface, i) -> {
                            if (i == 0){
                                EliminarMensajeParaMi(mensaje.getIdMensaje());
                            }
                            if (i == 1){
                                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(mensaje.getMensajeTxt())));
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
                                        "Eliminar mensaje para mi",
                                        "Ver imagen",
                                        "Descargar imagen"
                                };
                        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(context);
                        builder.setTitle("Selecione una opcion");
                        builder.setItems(options, (dialogInterface, i) -> {
                            if (i == 0){
                                EliminarMensajeParaMi(mensaje.getIdMensaje());
                            }
                            if (i == 1){

                                Intent intent=new Intent(context, VistaImagenActivity.class);
                                intent.putExtra("imagen",mensaje.getMensajeTxt());
                                intent.putExtra("idReceiver",idReceiver);
                                context.startActivity(intent);


                            }
                            if(i==2){
                               imageDownload(viewHolder.itemView.getContext(), mensaje.getMensajeTxt());
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

    private void EliminarMensajeParaMi(String idMensaje) {
        String idsender=FirebaseAuth.getInstance().getCurrentUser().getUid();
        String senderRoom=idsender+idReceiver;
        HashMap<String,Object> hashMap=new HashMap<>();
        hashMap.put("mensajeTxt","Este mensaje a sido eliminado para mi");
        hashMap.put("tipoMensaje","eliminado");
        database.getReference().child("Chats").child(senderRoom).child("mensaje").child(idMensaje).removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(context, "Mensaje eliminado", Toast.LENGTH_SHORT).show();
                    }
                });

    }

    public  void imageDownload(Context ctx, String url){
        Picasso.with(ctx)
                .load(url)
                .into(getTarget(url));


    }

    //target to save
    private  Target getTarget(final String url){
        Target target = new Target(){

            @Override
            public void onBitmapLoaded(final Bitmap bitmap, Picasso.LoadedFrom from) {
                new Thread(new Runnable() {

                    @Override
                    public void run() {

                        File file = new File(Environment.getExternalStorageDirectory().getPath() + "/" + url);
                        try {
                            file.createNewFile();
                            FileOutputStream ostream = new FileOutputStream(file);
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, ostream);
                            ostream.flush();
                            ostream.close();
                        } catch (IOException e) {
                            Log.e("IOException", e.getLocalizedMessage());
                        }
                    }
                }).start();

            }

            @Override
            public void onBitmapFailed(Drawable errorDrawable) {

            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {

            }
        };
        return target;




    }




    private void EliminarMensajeTodos(String idMensaje) {
        String idsender=FirebaseAuth.getInstance().getCurrentUser().getUid();
        String senderRoom=idsender+idReceiver;
        String receiverRoom=idReceiver+idsender;

        HashMap<String,Object> hashMap=new HashMap<>();
        hashMap.put("mensajeTxt","Este mensaje a sido eliminado para todos");
        hashMap.put("tipoMensaje","eliminado");

        database.getReference().child("Chats").child(senderRoom).child("mensaje").child(idMensaje).removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                database.getReference().child("Chats").child(receiverRoom).child("mensaje").child(idMensaje).removeValue()
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull @NotNull Task<Void> task) {
                        Toast.makeText(context, "Mensaje eliminado", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }


    @Override
    public int getItemViewType(int position) {
        Mensaje mensaje=mensajes.get(position);
        if(FirebaseAuth.getInstance().getCurrentUser().getUid().equals(mensaje.getIdMensajero())){
            return ITEM_ENVIANDO;
        }else {
            return ITEM_RECIBIENDO;
        }
    }

    @Override
    public int getItemCount() {
        return mensajes.size();
    }

    public class ListadoMensajesEnviadosViewHolder extends RecyclerView.ViewHolder{
        ItemMensajeEnviadoBinding bindingEnviado;
        public ListadoMensajesEnviadosViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);
            bindingEnviado=ItemMensajeEnviadoBinding.bind(itemView);
        }
    }
    public class ListadoMensajesRecibidosViewHolder extends RecyclerView.ViewHolder{
        ItemMensajeRecibidoBinding bindingRecibido;
        public ListadoMensajesRecibidosViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);
            bindingRecibido=ItemMensajeRecibidoBinding.bind(itemView);
        }
    }
}
