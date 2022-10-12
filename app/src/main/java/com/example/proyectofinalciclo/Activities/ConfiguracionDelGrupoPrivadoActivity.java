package com.example.proyectofinalciclo.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.ImageUtils;
import com.blankj.utilcode.util.UriUtils;
import com.example.proyectofinalciclo.Clases.Grupo;
import com.example.proyectofinalciclo.Fragments.GrupoFragment;
import com.example.proyectofinalciclo.R;
import com.example.proyectofinalciclo.databinding.ActivityConfiguracionDelGrupoPrivadoBinding;
import com.example.proyectofinalciclo.databinding.ActivityCrearGrupoBinding;
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
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

public class ConfiguracionDelGrupoPrivadoActivity extends AppCompatActivity {

    private ActivityConfiguracionDelGrupoPrivadoBinding binding;
    private ProgressDialog dialog;
    private String rutaImagen;
    private final int REQUEST_IMAGE_CAMERA=101;
    private final int REQUEST_PERMISSION_CAMERA=100;
    private final int GALERIA=1;
    private final int  PERMISSION_CODE=2;
    private Uri imagenSelecionada;
    private String idGrupoPrivado;
    private  Grupo grupo;
    private FirebaseAuth auth;
    private FirebaseDatabase database;
    private FirebaseStorage storage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityConfiguracionDelGrupoPrivadoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        idGrupoPrivado=getIntent().getStringExtra("idGrupoPrivado");

        auth=FirebaseAuth.getInstance();
        database=FirebaseDatabase.getInstance();
        storage=FirebaseStorage.getInstance();

        dialog=new ProgressDialog(ConfiguracionDelGrupoPrivadoActivity.this);

       // Resources res=getResources();
        // String[] arrayList= res.getStringArray(R.array.tipo_de_grupos);
       // ArrayAdapter arrayAdapter=new ArrayAdapter(this,R.layout.dropdown_items,arrayList);
       // binding.tipoGrupo.setAdapter(arrayAdapter);

        binding.camara.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
                    if(ActivityCompat.checkSelfPermission(ConfiguracionDelGrupoPrivadoActivity.this, Manifest.permission.CAMERA)== PackageManager.PERMISSION_GRANTED){
                        goToCamara();
                    }else{
                        ActivityCompat.requestPermissions(ConfiguracionDelGrupoPrivadoActivity.this, new String[]{Manifest.permission.CAMERA},REQUEST_PERMISSION_CAMERA);
                    }
                }
            }
        });

        binding.retroceder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        binding.imagenUsuario.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
        });

        ObtenerDatosDelGrupo();

        GuardarDatos();
    }

    private void ObtenerDatosDelGrupo() {
        database.getReference().child("DatosGruposPrivados").child(idGrupoPrivado).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    grupo=snapshot.getValue(Grupo.class);
                    binding.nombreGrupo.setText(grupo.getNombre());
                    binding.descripcionGrupo.setText(grupo.getDescripcion());
                    Picasso.with(ConfiguracionDelGrupoPrivadoActivity.this).load(grupo.getImagen()).fit().centerCrop()
                            .placeholder(R.drawable.avatar)
                            .error(R.drawable.avatar)
                            .placeholder(R.drawable.avatar)
                            .into(binding.imagenUsuario);
                    binding.tipo.setText(grupo.getTipoGrupo());
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }

    private void GuardarDatos() {

        binding.continuarBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String nombre=binding.nombreGrupo.getText().toString();

                if(nombre.isEmpty()){
                    binding.nombreGrupo.setError("ingrese un nombre porfavor");
                    return;
                }else if(imagenSelecionada!=null){
                    GuardarImagenSelecionada(imagenSelecionada);

                }else {

                    ActualizarGrupo(grupo.getImagen());
                    //Uri uriImage = Uri.parse("android.resource://" + getPackageName() +"/"+ R.drawable.avatar);
                   // GuardarImagenSelecionada(uriImage);
                }
            }
        });

    }

    private void GuardarImagenSelecionada(Uri uri) {

        dialog.setMessage("Porvafor espere...");
        dialog.setCancelable(false);
        dialog.show();
        StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("ImagenesGrupoMundiales");

        StorageReference reference = storageReference.child(idGrupoPrivado + "." + "png");

        reference.putFile(uri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull @NotNull Task<UploadTask.TaskSnapshot> task) {
                if(task.isSuccessful()){
                    reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            String imagenSelecionada=uri.toString();
                            ActualizarGrupo(imagenSelecionada);

                        }
                    });
                }
            }
        });
    }

    private void ActualizarGrupo(String uri){

        String imagenGrupo=uri;
        String idUsuario=auth.getCurrentUser().getUid();
        String descripcionGrupo=binding.descripcionGrupo.getText().toString();
        String nombre=binding.nombreGrupo.getText().toString();

        HashMap hashMap=new HashMap();
        hashMap.put("descripcion",descripcionGrupo);
        hashMap.put("idGrupo",idGrupoPrivado);
        hashMap.put("idUsuario",idUsuario);
        hashMap.put("imagen",imagenGrupo);
        hashMap.put("nombre",nombre);


        database.getReference().child("DatosGruposPrivados")
                .child(idGrupoPrivado)
                .updateChildren(hashMap)
                .addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull @NotNull Task task) {
                        if(task.isComplete()){
                            EnviarInicio();
                        }else{
                            dialog.dismiss();
                            Toast.makeText(ConfiguracionDelGrupoPrivadoActivity.this, "se ha producido un error", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void EnviarInicio() {
        dialog.dismiss();
        Toast.makeText(this, "Grupo Actualizado", Toast.LENGTH_SHORT).show();
        MensajeGrupoPrivadoActivity.EnviarMensaje("Grupo Actualizado","actualizado");
        finish();
    }

    private void SeleccinarImagenUsuario() {
        Intent intent=new Intent(Intent.ACTION_PICK,MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent,GALERIA);
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

    private File crearImagen() throws IOException {
        String nombreImagen="foto_";
        File directorio=getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File imagen=File.createTempFile(nombreImagen,".jpg",directorio);
        rutaImagen=imagen.getAbsolutePath();
        return imagen;

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

        if(requestCode==GALERIA){
            if(data!=null && data.getData()!=null){
                Uri resultUri=data.getData();
                CropImage.activity(resultUri)
                        .setAspectRatio(1,1)
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .start(this);
            }
        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                imagenSelecionada = result.getUri();
                binding.imagenUsuario.setImageURI(imagenSelecionada);



            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }

        if(requestCode==REQUEST_IMAGE_CAMERA){
            if(resultCode== Activity.RESULT_OK){
                Bitmap bitmap = BitmapFactory.decodeFile(rutaImagen);
                ImageUtils.save(bitmap, rutaImagen, Bitmap.CompressFormat.JPEG);
                Uri bitmap2Uri = UriUtils.file2Uri(FileUtils. getFileByPath(rutaImagen));


                binding.imagenUsuario.setImageURI(bitmap2Uri);
                imagenSelecionada=bitmap2Uri;
            }
        }
    }
}