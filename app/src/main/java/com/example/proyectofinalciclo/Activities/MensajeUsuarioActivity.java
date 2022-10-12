package com.example.proyectofinalciclo.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.ImageUtils;
import com.blankj.utilcode.util.UriUtils;
import com.bumptech.glide.Glide;
import com.example.proyectofinalciclo.Adaptadores.AdaptadorMensajeUsuarioActivity;
import com.example.proyectofinalciclo.Clases.Mensaje;
import com.example.proyectofinalciclo.Clases.Usuario;
import com.example.proyectofinalciclo.Fragments.BottomSheetFragment;
import com.example.proyectofinalciclo.R;
import com.example.proyectofinalciclo.databinding.ActivityMensajeUsuarioBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.core.Constants;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import static androidx.constraintlayout.motion.utils.Oscillator.TAG;

public class MensajeUsuarioActivity extends AppCompatActivity {

    private ActivityMensajeUsuarioBinding binding;
    private FirebaseAuth auth;
    private FirebaseDatabase database;
    private FirebaseStorage storage;
    private String idReceiver;
    private String idSender;
    private String senderRoom;
    private String receiverRoom;
    private ArrayList<Mensaje> mensajes;
    private AdaptadorMensajeUsuarioActivity adaptadorMensajeUsuarioActivity;
    private int IMAGE_CALERIA=1;
    private String tipoMensaje=" ";
    private int numMensajes=0;

    private final int REQUEST_IMAGE_CAMERA=101;
    private String rutaImagen;
    private final int REQUEST_PERMISSION_CAMERA=100;
    private final int  PERMISSION_CODE=2;
    private final int GALERIA=1;
    private Uri imagenSelecionada;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityMensajeUsuarioBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbarUsuarioMensaje);

        mensajes=new ArrayList<>();
        
        binding.conexion.setSelected(true);

        idReceiver=getIntent().getStringExtra("idReceiver");
        adaptadorMensajeUsuarioActivity=new AdaptadorMensajeUsuarioActivity(mensajes,this,idReceiver);

        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));

        binding.recyclerView.setAdapter(adaptadorMensajeUsuarioActivity);

        database=FirebaseDatabase.getInstance();
        auth=FirebaseAuth.getInstance();
        storage=FirebaseStorage.getInstance();

        idSender=auth.getCurrentUser().getUid();
        senderRoom=idSender+idReceiver;
        receiverRoom=idReceiver+idSender;

        if (Build.VERSION.SDK_INT >= 11) {
            binding.recyclerView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                @Override
                public void onLayoutChange(View v,
                                           int left, int top, int right, int bottom,
                                           int oldLeft, int oldTop, int oldRight, int oldBottom) {

                    if(binding.recyclerView.getAdapter().getItemCount()>1){
                        if (bottom < oldBottom) {

                            binding.recyclerView.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    binding.recyclerView.smoothScrollToPosition(
                                            binding.recyclerView.getAdapter().getItemCount() - 1);
                                }
                            }, 100);
                        }
                    }

                }
            });
        }

        binding.retroceder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        EstablecerContadorSender();

        ObtenerDatosDelUsuarioSelecionado();
        ObtenerDatosMensaje();
        GuardarDatosMensaje();


        binding.archivo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BottomSheetFragment bottomSheetFragment = new BottomSheetFragment();

                bottomSheetFragment.show(getSupportFragmentManager(),bottomSheetFragment.getTag());

            }
        });

        final Handler handler = new Handler();
        binding.mensaje.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                database.getReference().child("Chats").child(senderRoom).child("Estado").child(idSender).child("estado").setValue("escribiendo...");
                handler.removeCallbacksAndMessages(null);
                handler.postDelayed(userStoppedTyping,1000);
            }

            Runnable userStoppedTyping = new Runnable() {
                @Override
                public void run() {
                    database.getReference().child("Chats").child(senderRoom).child("Estado").child(idSender).child("estado").setValue("Conectado");
                }
            };
        });
    }
    public  void Imagen(){
        tipoMensaje= "image";

        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
            if(checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                    ==PackageManager.PERMISSION_DENIED){
                String[] permission={Manifest.permission.READ_EXTERNAL_STORAGE};
                requestPermissions(permission,PERMISSION_CODE);
            }else{
                SeleccinarImagenUsuario();
            }
        }else {
            SeleccinarImagenUsuario();
        }
    }
    public void Pdf(){
        tipoMensaje = "pdf";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.parse("package:" + getPackageName()));
            startActivity(intent);
            return;
        }
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.setType("application/pdf");
        startActivityForResult(intent.createChooser(intent, "Select PDF File"), 5);
    }

    public void Word(){
        tipoMensaje = "word";

        //Intent intent = new Intent();
        //intent.setAction(Intent.ACTION_GET_CONTENT);
        //intent.setType("application/msword");
        //startActivityForResult(intent.createChooser(intent, "Select Ms Word File"), 5);

        String[] mimeTypes =
                {"application/msword",
                        "application/vnd.openxmlformats-officedocument.wordprocessingml.document", // .doc & .docx
                        //"application/vnd.ms-powerpoint",
                        //"application/vnd.openxmlformats-officedocument.presentationml.presentation", // .ppt & .pptx
                        //"application/vnd.ms-excel",
                        //"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", // .xls & .xlsx
                        //"text/plain",
                        //"application/pdf",
                        //"application/zip"
                };

        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            intent.setType(mimeTypes.length == 1 ? mimeTypes[0] : "*/*");
            if (mimeTypes.length > 0) {
                intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
            }
        } else {
            String mimeTypesStr = "";
            for (String mimeType : mimeTypes) {
                mimeTypesStr += mimeType + "|";
            }
            intent.setType(mimeTypesStr.substring(0,mimeTypesStr.length() - 1));
        }
        startActivityForResult(Intent.createChooser(intent,"ChooseFile"), 5);
    }

    public void Camara(){
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
            if(ActivityCompat.checkSelfPermission(MensajeUsuarioActivity.this, Manifest.permission.CAMERA)== PackageManager.PERMISSION_GRANTED){
                goToCamara();
            }else{
                ActivityCompat.requestPermissions(MensajeUsuarioActivity.this, new String[]{Manifest.permission.CAMERA},REQUEST_PERMISSION_CAMERA);
            }
        }
    }

    private File crearImagen() throws IOException {
        String nombreImagen="foto_";
        File directorio=getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File imagen=File.createTempFile(nombreImagen,".jpg",directorio);
        rutaImagen=imagen.getAbsolutePath();
        return imagen;

    }

    private void goToCamara(){
        Intent cameraIntent=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if(cameraIntent.resolveActivity(getPackageManager())!=null){
            File imagenArchivo=null;
            try {
                imagenArchivo=crearImagen();
            }catch (IOException ex){
                Toast.makeText(this, "se ha producido un error", Toast.LENGTH_SHORT).show();
            }

            if(imagenArchivo!=null){
                Uri fotoUri= FileProvider.getUriForFile(this,"com.example.proyectofinalciclo.fileprovider",imagenArchivo);
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT,fotoUri);
            }
            startActivityForResult(cameraIntent,REQUEST_IMAGE_CAMERA);
        }
    }

    private void SeleccinarImagenUsuario() {
        Intent intent=new Intent();
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent,GALERIA);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull @NotNull String[] permissions, @NonNull @NotNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode==REQUEST_PERMISSION_CAMERA){
            if(permissions.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                goToCamara();
            }else{
                Toast.makeText(this, "necesita habilitar permisos", Toast.LENGTH_SHORT).show();
            }
        }
        if(requestCode==PERMISSION_CODE){
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                SeleccinarImagenUsuario();
            } else {
                Toast.makeText(this, "", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==IMAGE_CALERIA){
            if(data!=null && data.getData()!=null){
                Uri imagenSelecionadaGaleria=data.getData();

                GuardarImagenSelecionada(imagenSelecionadaGaleria);

            }
        }

        if (requestCode == 5 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            //if a file is selected
            if (data.getData() != null) {
                //uploading the file
                uploadFile(data.getData());
            }else{
                Toast.makeText(this, "No file chosen", Toast.LENGTH_SHORT).show();
            }
        }

        if(requestCode==REQUEST_IMAGE_CAMERA){
            if(resultCode== Activity.RESULT_OK){
                Bitmap bitmap = BitmapFactory.decodeFile(rutaImagen);
                ImageUtils.save(bitmap, rutaImagen, Bitmap.CompressFormat.JPEG);
                Uri bitmap2Uri = UriUtils.file2Uri(FileUtils. getFileByPath(rutaImagen));
                GuardarImagenSelecionada(bitmap2Uri);
            }
        }
        Thread thread=new Thread(){
            @Override
            public void run() {

                try {
                    database.getReference().child("EstadoUsuario").child(idSender).child("Estado").child("estado").setValue("escribiendo...");

                    sleep(2000);

                }catch (Exception e){

                }
                finally {
                    database.getReference().child("EstadoUsuario").child(idSender).child("Estado").child("estado").setValue("Conectado");

                }
                super.run();
            }
        };
        thread.start();

    }

    private void GuardarImagenSelecionada(Uri imagenSelecionadaGaleria){
        String keyRandom=GenerarCodigoMensajeAleatorio();

        StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("imagenesUsuarios");

        StorageReference reference = storageReference.child(keyRandom + "." + "png");

        reference.putFile(imagenSelecionadaGaleria).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull @NotNull Task<UploadTask.TaskSnapshot> task) {
                if(task.isSuccessful()){
                    reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            String imagenSelecionada=uri.toString();
                            EnviarMensaje(imagenSelecionada, "imagen");


                        }
                    });
                }
            }
        });
    }

    // ----------------------
    private void uploadFile(Uri data) {

        String keyRandom=GenerarCodigoMensajeAleatorio();

        StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Documentos");

        StorageReference sRef =storageReference.child(keyRandom + "." + tipoMensaje);


        sRef.putFile(data).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull @NotNull Task<UploadTask.TaskSnapshot> task) {
                if(task.isSuccessful()){

                    sRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            String direccionPDF=uri.toString();
                            EnviarMensaje(direccionPDF,tipoMensaje);

                        }
                    });
                }
            }
        });

    }

    private String  GenerarCodigoMensajeAleatorio(){
        String keyRandom=database.getReference().push().getKey();
        return keyRandom;
    }
    private void ObtenerDatosMensaje() {
        database.getReference().child("Chats").child(senderRoom).child("mensaje").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    mensajes.clear();
                    for(DataSnapshot snapshot1:snapshot.getChildren()){
                        Mensaje mensaje= snapshot1.getValue(Mensaje.class);
                        String idMensaje=snapshot1.getKey();
                        mensaje.setIdMensaje(idMensaje);
                        mensajes.add(mensaje);
                        adaptadorMensajeUsuarioActivity.notifyDataSetChanged();
                    }
                    binding.recyclerView.getLayoutManager().scrollToPosition(mensajes.size()-1);
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }

    private void GuardarDatosMensaje() {
        binding.enviarBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String mensajeTxt=binding.mensaje.getText().toString();
                if(mensajeTxt.isEmpty()){
                    Toast.makeText(MensajeUsuarioActivity.this, "ingrese un mensaje porfavor", Toast.LENGTH_SHORT).show();

                }else{

                    binding.mensaje.setText("");
                    EnviarMensaje(mensajeTxt,"texto");
                    //GenerarCodigoMensajeAleatorio();
                }
            }
        });
    }

    private void EnviarMensaje(String mensajeTxt, String tipoMensaje) {
        String FechaActual, TiempoActual,Fecha_Tiempo;
        Calendar calendar=Calendar.getInstance();

        SimpleDateFormat fechaActual=new SimpleDateFormat("dd-MMM-yyyy");
        FechaActual=fechaActual.format(calendar.getTime());

        SimpleDateFormat tiempoActual=new SimpleDateFormat("hh:mm a");
        TiempoActual=tiempoActual.format(calendar.getTime());

        SimpleDateFormat fecha_tiempo=new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        Fecha_Tiempo=fecha_tiempo.format(calendar.getTime());

        String  keyRandom=database.getReference().push().getKey();
        Mensaje mensaje=new Mensaje(mensajeTxt,idSender,FechaActual,TiempoActual,tipoMensaje,keyRandom,"no");



        database.getReference().child("Chats").child(senderRoom).child("mensaje")
                .child(keyRandom)
                .setValue(mensaje)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        database.getReference().child("Chats").child(receiverRoom).child("mensaje")
                                .child(keyRandom)
                                .setValue(mensaje)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull @NotNull Task<Void> task) {
                                        if(task.isSuccessful()){
                                            HashMap<String,Object> hashMap=new HashMap<>();
                                            hashMap.put("tipoMensaje",tipoMensaje);
                                            hashMap.put("tiempo",TiempoActual);
                                            hashMap.put("fecha",FechaActual);
                                            hashMap.put("mensaje",mensaje.getMensajeTxt());
                                            hashMap.put("fecha_tiempo",Fecha_Tiempo);
                                            database.getReference().child("Chats").child(senderRoom).child("ultimoMensaje")
                                                    .updateChildren(hashMap)
                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void unused) {
                                                            database.getReference().child("Chats").child(receiverRoom).child("ultimoMensaje")
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


        ObtenerContadorMensajesSender();
        ActivarContadorMensaje();

    }


    private void EstablecerContadorSender(){
        database.getReference().child("Chats").child(idReceiver+idSender).child("mensajesPendientes").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    int cant=snapshot.child("cantidad").getValue(int.class);
                    numMensajes=cant;

                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });

    }


    private void ObtenerContadorMensajesSender(){

        database.getReference().child("Chats").child(idReceiver+idSender).child("mensajesPendientes").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    int cant=snapshot.child("cantidad").getValue(int.class);
                    cant++;

                    numMensajes=cant;
                    String mostrar=String.valueOf(numMensajes);
                }

                database.getReference().child("Chats").child(idReceiver+idSender).child("mensajesPendientes").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                        if(!snapshot.exists()){
                            numMensajes=0;
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

    }

    private void ActivarContadorMensaje() {

        database.getReference().child("Chats").child(idReceiver+idSender).child("mensajesPendientes").child("cantidad").setValue(numMensajes)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull @NotNull Task<Void> task) {
                        if(task.isSuccessful()){
                            database.getReference().child("Chats").child(idReceiver+idSender).child("mensajesPendientes").addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                                    if(snapshot.exists()){
                                        int cant=snapshot.child("cantidad").getValue(int.class);
                                        database.getReference().child("Chats").child(idReceiver+idSender).child("mensajesPendientes").child("cantidad").setValue(cant+1)
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void unused) {

                                                    }
                                                });
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull @NotNull DatabaseError error) {

                                }
                            });
                        }
                    }
                });
        numMensajes++;
    }

    private void ObtenerDatosDelUsuarioSelecionado() {
        database.getReference().child("Usuarios").child(idReceiver).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if(snapshot.exists()){

                    Usuario usuario=snapshot.getValue(Usuario.class);
                    binding.nombreUsuario.setText(usuario.getNombreUsuario());
                    Picasso.with(MensajeUsuarioActivity.this).
                            load(usuario.getImagenUsuario()).fit().centerCrop()
                            .placeholder(R.drawable.avatar)
                            .error(R.drawable.avatar)
                            .placeholder(R.drawable.avatar)
                            .into(binding.imagenUsuario);

                   database.getReference().child("EstadoUsuario").child(usuario.getIdUsuario()).child("Estado").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                            if(snapshot.exists()){
                                String conexionEstadoUsuario=snapshot.child("estado").getValue(String.class);
                                String fechaEstadoUsuario=snapshot.child("fecha").getValue(String.class);
                                String horaEstadoUsuario=snapshot.child("hora").getValue(String.class);
                                HashMap<String,Object> hashMap=new HashMap<>();
                                hashMap.put("estado",conexionEstadoUsuario);

                                database.getReference().child("Chats").child(receiverRoom).child("Estado").child(usuario.getIdUsuario()).updateChildren(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull @NotNull Task<Void> task) {
                                        if(task.isComplete()){
                                            database.getReference().child("Chats").child(receiverRoom).child("Estado").child(usuario.getIdUsuario()).addValueEventListener(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                                                    if(snapshot.exists()){
                                                        String estado=snapshot.child("estado").getValue(String.class);
                                                        if(estado.equals("Conectado")){
                                                            binding.conexion.setText("Conectado(a)");
                                                            Resources res = getApplicationContext().getResources();
                                                            final int newColor = res.getColor(R.color.white);
                                                            binding.conexion.setTextColor(newColor);

                                                        }else  if(estado.equals("Desconectado")){
                                                            binding.conexion.setText("Ultm conex "+fechaEstadoUsuario+" a las "+horaEstadoUsuario);
                                                            Resources res = getApplicationContext().getResources();
                                                            final int newColor = res.getColor(R.color.white);
                                                            binding.conexion.setTextColor(newColor);
                                                        }else{
                                                            binding.conexion.setText(estado);

                                                            Resources res = getApplicationContext().getResources();
                                                            final int newColor = res.getColor(R.color.Green);
                                                            binding.conexion.setTextColor(newColor);
                                                            EstablecerVistoMensajeRecibido();
                                                        }
                                                    }
                                                }

                                                @Override
                                                public void onCancelled(@NonNull @NotNull DatabaseError error) {

                                                }
                                            });
                                        }
                                    }
                                });

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
    }

    private void EstablecerVistoMensajeRecibido(){
        ArrayList<Mensaje> mensajes=new ArrayList<>();

        database.getReference().child("Chats").child(FirebaseAuth.getInstance().getUid()+idReceiver).child("mensaje").addListenerForSingleValueEvent(new ValueEventListener() {
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
                        database.getReference().child("Chats").child(FirebaseAuth.getInstance().getUid()+idReceiver).child("mensaje").child(idMensaje).updateChildren(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.munu_mensaje_usuario,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.informacion_usuario:
                InformacionUsuario();
                break;
            case R.id.vaciar_mensajes:
                VaciarMensajes();
                break;

        }
        return super.onOptionsItemSelected(item);
    }

    private void VaciarMensajes() {
        database.getReference().child("Chats").child(senderRoom).child("mensaje").removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull @NotNull Task<Void> task) {
                if(task.isComplete()){

                    database.getReference().child("Chats").child(senderRoom).child("mensaje").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                            if(!snapshot.exists()){
                                mensajes.clear();
                                Toast.makeText(MensajeUsuarioActivity.this, "Mensajes Eliminados", Toast.LENGTH_SHORT).show();
                                adaptadorMensajeUsuarioActivity.notifyDataSetChanged();
                                binding.recyclerView.getLayoutManager().scrollToPosition(mensajes.size()-1);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull @NotNull DatabaseError error) {

                        }
                    });
                }
            }
        });
    }

    private void InformacionUsuario() {
        Intent intent=new Intent(MensajeUsuarioActivity.this,InformacionUsuarioActivity.class);
        intent.putExtra("idUsuario",idReceiver);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        InicioActivity.CerrarSesion("Conectado");

    }

    @Override
    protected void onStart() {
        super.onStart();
        InicioActivity.CerrarSesion("Conectado");
    }



    @Override
    protected void onPause() {
        super.onPause();
        database.getReference().child("Chats").child(idSender+idReceiver).child("mensajesPendientes").removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull @NotNull Task<Void> task) {
                if(task.isSuccessful()){

                }

            }
        });

        database.getReference().child("Chats").child(idReceiver+FirebaseAuth.getInstance().getUid()).child("mensaje").addListenerForSingleValueEvent(new ValueEventListener() {
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
                        database.getReference().child("Chats").child(idReceiver+FirebaseAuth.getInstance().getUid()).child("mensaje").child(idMensaje).updateChildren(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
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

        InicioActivity.CerrarSesion("Desconectado");
    }
}