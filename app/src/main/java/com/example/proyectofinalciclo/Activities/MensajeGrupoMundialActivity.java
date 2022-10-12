package com.example.proyectofinalciclo.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.ImageUtils;
import com.blankj.utilcode.util.UriUtils;
import com.example.proyectofinalciclo.Adaptadores.AdaptadorMensajeMundialActivity;
import com.example.proyectofinalciclo.Adaptadores.AdaptadorMensajePrivadoActivity;
import com.example.proyectofinalciclo.Clases.Grupo;
import com.example.proyectofinalciclo.Clases.Mensaje;
import com.example.proyectofinalciclo.Fragments.BottomSheetGrupoMundialFragment;
import com.example.proyectofinalciclo.Fragments.BottomSheetGrupoPrivadoFragment;
import com.example.proyectofinalciclo.R;
import com.example.proyectofinalciclo.databinding.ActivityInformacionGrupoMundialBinding;
import com.example.proyectofinalciclo.databinding.ActivityMensajeGrupoMundialBinding;
import com.example.proyectofinalciclo.databinding.ActivityMensajeUsuarioBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
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

public class MensajeGrupoMundialActivity extends AppCompatActivity {
    private ActivityMensajeGrupoMundialBinding binding;
    private FirebaseAuth auth;
    private FirebaseDatabase database;
    private FirebaseStorage storage;
    private String idGrupoMundial;
    private String idSender;
    private String senderRoom;
    private String receiverRoom;
    private ArrayList<Mensaje> mensajes;
    private AdaptadorMensajeMundialActivity adaptadorMensajeMundialActivity;
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
        binding= ActivityMensajeGrupoMundialBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbarUsuarioMensaje);

        mensajes=new ArrayList<>();

        idGrupoMundial=getIntent().getStringExtra("idGrupo");

        adaptadorMensajeMundialActivity=new AdaptadorMensajeMundialActivity(mensajes,this,idGrupoMundial);

        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));

        binding.recyclerView.setAdapter(adaptadorMensajeMundialActivity);


        database=FirebaseDatabase.getInstance();
        auth=FirebaseAuth.getInstance();
        storage= FirebaseStorage.getInstance();

        idSender=auth.getCurrentUser().getUid();
        // senderRoom=idSender+idReceiver;
        //receiverRoom=idReceiver+idSender;

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



        ObtenerDatosDeGrupoSelecionado();
        ObtenerDatosMensaje();
        GuardarDatosMensaje();


        binding.archivo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BottomSheetGrupoMundialFragment bottomSheetFragment = new BottomSheetGrupoMundialFragment();

                bottomSheetFragment.show(getSupportFragmentManager(),bottomSheetFragment.getTag());

            }
        });


    }
    public  void Imagen(){
        tipoMensaje= "image";

        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
            if(checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_DENIED){
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
            if(ActivityCompat.checkSelfPermission(MensajeGrupoMundialActivity.this, Manifest.permission.CAMERA)== PackageManager.PERMISSION_GRANTED){
                goToCamara();
            }else{
                ActivityCompat.requestPermissions(MensajeGrupoMundialActivity.this, new String[]{Manifest.permission.CAMERA},REQUEST_PERMISSION_CAMERA);
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

        StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("ImagenesGrupoPrivado");

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

        StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("DocumentosGrupoPrivado");

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
        database.getReference().child("GruposMundiales").child(idGrupoMundial).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    mensajes.clear();
                    for(DataSnapshot snapshot1:snapshot.getChildren()){
                        Mensaje mensaje= snapshot1.getValue(Mensaje.class);


                        mensajes.add(mensaje);
                        adaptadorMensajeMundialActivity.notifyDataSetChanged();


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
                    Toast.makeText(MensajeGrupoMundialActivity.this, "ingrese un mensaje porfavor", Toast.LENGTH_SHORT).show();

                }else{

                    binding.mensaje.setText("");
                    EnviarMensaje(mensajeTxt,"texto");
                    //GenerarCodigoMensajeAleatorio();
                }
            }
        });
    }

    private void EnviarMensaje(String mensajeTxt, String tipoMensaje) {
        String FechaActual, TiempoActual;
        Calendar calendar=Calendar.getInstance();

        SimpleDateFormat fechaActual=new SimpleDateFormat("dd-MMM-yyyy");
        FechaActual=fechaActual.format(calendar.getTime());

        SimpleDateFormat tiempoActual=new SimpleDateFormat("hh:mm a");
        TiempoActual=tiempoActual.format(calendar.getTime());

        String  keyRandom=database.getReference().push().getKey();
        Mensaje mensaje=new Mensaje(mensajeTxt,idSender,FechaActual,TiempoActual,tipoMensaje,keyRandom,"no");



        database.getReference().child("GruposMundiales").child(idGrupoMundial)
                .child(keyRandom)
                .setValue(mensaje)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {

                    }
                });


        ObtenerContadorMensajesSender();
        ActivarContadorMensaje();

    }


    private void EstablecerContadorSender(){


    }


    private void ObtenerContadorMensajesSender(){


    }

    private void ActivarContadorMensaje() {


    }

    private void ObtenerDatosDeGrupoSelecionado() {
        database.getReference().child("DatosGruposMundiales").child(idGrupoMundial).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if(snapshot.exists()){

                    Grupo usuario=snapshot.getValue(Grupo.class);
                    binding.nombreUsuario.setText(usuario.getNombre());
                    Picasso.with(MensajeGrupoMundialActivity.this).
                            load(usuario.getImagen()).fit().centerCrop()
                            .placeholder(R.drawable.avatar)
                            .error(R.drawable.avatar)
                            .placeholder(R.drawable.avatar)
                            .into(binding.imagenUsuario);


                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_grupo_mundial,menu);
        database.getReference().child("CreadorGrupoMundial").child(idGrupoMundial).child(idSender).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    String estado=snapshot.child("Grupo").getValue(String.class);
                    if(estado.equals("Admin")){
                        MenuItem item = menu.findItem(R.id.informacion_grupo_mundial);
                        item.setVisible(false);
                    }
                }else{
                    MenuItem item = menu.findItem(R.id.eliminar_grupo_mundial);
                    item.setVisible(false);
                    MenuItem item2 = menu.findItem(R.id.configuracion_grupo_mundial);
                    item2.setVisible(false);
                }

            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.informacion_grupo_mundial:
                InformacionGrupo();
                break;
            case R.id.configuracion_grupo_mundial:
                ConfigurarGrupoMundial();
                break;
            case R.id.eliminar_grupo_mundial:
                EliminarGrupo();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void EliminarGrupo() {
        database.getReference().child("CreadorGrupoMundial").child(idGrupoMundial).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                database.getReference().child("DatosGruposMundiales").child(idGrupoMundial).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        database.getReference().child("GruposMundiales").child(idGrupoMundial).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull @NotNull Task<Void> task) {
                                if(task.isComplete()){
                                    Intent intent=new Intent(MensajeGrupoMundialActivity.this,InicioActivity.class);
                                    Toast.makeText(MensajeGrupoMundialActivity.this, "Grupo Eliminado", Toast.LENGTH_SHORT).show();
                                    startActivity(intent);
                                    finish();
                                }
                            }
                        });
                    }
                });
            }
        });
    }

    private void ConfigurarGrupoMundial() {
        Intent intent=new Intent(MensajeGrupoMundialActivity.this, ConfiguracionDelGrupoMundialActivity.class);
        intent.putExtra("idGrupoMundial",idGrupoMundial);
        startActivity(intent);
    }

    private void InformacionGrupo() {
        Intent intent=new Intent(MensajeGrupoMundialActivity.this, InformacionGrupoMundialActivity.class);
        intent.putExtra("idGrupoMundial",idGrupoMundial);
        startActivity(intent);
    }

    private void EstablecerVistoMensajeRecibido(){

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

        InicioActivity.CerrarSesion("Desconectado");
    }
}