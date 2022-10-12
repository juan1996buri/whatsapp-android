package com.example.proyectofinalciclo.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.Toast;

import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.ImageUtils;
import com.blankj.utilcode.util.UriUtils;
import com.example.proyectofinalciclo.R;
import com.example.proyectofinalciclo.Clases.Usuario;
import com.example.proyectofinalciclo.databinding.ActivityConfiguracionPerfilBinding;
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
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;


import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.HashMap;

public class ConfiguracionPerfilActivity extends AppCompatActivity {

    private ActivityConfiguracionPerfilBinding binding;
    private FirebaseAuth auth;
    private FirebaseDatabase database;
    private FirebaseStorage storage;
    private ProgressDialog dialog;

    private final int GALERIA=1;
    private final int  PERMISSION_CODE=2;
    private final int REQUEST_PERMISSION_CAMERA=100;
    private final int REQUEST_IMAGE_CAMERA=101;

    private Uri imagenSelecionada;
    private String rutaImagen;
    private DatePickerDialog datePickerDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityConfiguracionPerfilBinding.inflate(getLayoutInflater());
        binding=ActivityConfiguracionPerfilBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //getSupportActionBar().hide();

        Resources res=getResources();
        String[] arrayList= res.getStringArray(R.array.sexos);
        ArrayAdapter arrayAdapter=new ArrayAdapter(this,R.layout.dropdown_items,arrayList);
        binding.sexo.setAdapter(arrayAdapter);

        DatosFechaUsuario();

        binding.daterPikerButton.setText(getDatosDias());

        binding.camara.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
                    if(ActivityCompat.checkSelfPermission(ConfiguracionPerfilActivity.this, Manifest.permission.CAMERA)== PackageManager.PERMISSION_GRANTED){
                        goToCamara();
                    }else{
                        ActivityCompat.requestPermissions(ConfiguracionPerfilActivity.this, new String[]{Manifest.permission.CAMERA},REQUEST_PERMISSION_CAMERA);
                    }
                }
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
        GuardarDatos();
    }

    private void SeleccinarImagenUsuario() {
        Intent intent=new Intent(Intent.ACTION_PICK,MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent,GALERIA);
    }

    private void DatosFechaUsuario() {
        DatePickerDialog.OnDateSetListener dateSetListener=new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int day) {
                month=month+1;
                String date=HacerFecha(day,month,year);
                binding.daterPikerButton.setText(date);
            }

        };
        Calendar calendar=Calendar.getInstance();
        int year=calendar.get(Calendar.YEAR);
        int month=calendar.get(Calendar.MONTH);
        int day=calendar.get(Calendar.DAY_OF_MONTH);
        int style= AlertDialog.THEME_HOLO_LIGHT;
        datePickerDialog=new DatePickerDialog(this, style,dateSetListener,year,month,day );
        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
    }


    private String HacerFecha(int day, int month, int year) {
        return getFormatoFecha(month)+" "+day+" "+year;
    }

    private String getFormatoFecha(int month) {
        if(month==1)
            return "ENE";
        if(month==2)
            return "FEB";
        if(month==3)
            return "MAR";
        if(month==4)
            return "ABR";
        if(month==5)
            return "MAYO";
        if(month==6)
            return "JUN";
        if(month==7)
            return "JUL";
        if(month==8)
            return "AGO";
        if(month==9)
            return "SEPT";
        if(month==10)
            return "OCT";
        if(month==11)
            return "NOV";
        if(month==12)
            return "DIC";
        return "ENE";
    }
    public  void  openDatePicker(View view){
        datePickerDialog.show();
    }

    private String getDatosDias() {
        Calendar calendar=Calendar.getInstance();
        int year=calendar.get(Calendar.YEAR);
        int month=calendar.get(Calendar.MONTH);
        month=month+1;
        int day=calendar.get(Calendar.DAY_OF_MONTH);
        return HacerFecha(day,month,year);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private   int  ComprobacionEdadUsuario(){
        String fechaNacimiento=binding.daterPikerButton.getText().toString();

        String string = fechaNacimiento;
        String[] parts = string.split(" ");
        String mes = parts[0];
        String dia = parts[1];
        String año = parts[2];

        int mesUsuario=ComprobacionMesUsuario(mes);
        int diaUsuario=Integer.parseInt(dia);
        int añoUsuario=Integer.parseInt(año);


        String obtenerDiaActual;
        Calendar calendarDia=Calendar.getInstance();
        SimpleDateFormat diaActual=new SimpleDateFormat("dd");
        obtenerDiaActual=diaActual.format(calendarDia.getTime());

        String obtenerMesActual;
        Calendar calendarMes=Calendar.getInstance();
        SimpleDateFormat mesActual=new SimpleDateFormat("MMM");
        obtenerMesActual=mesActual.format(calendarMes.getTime());

        String obtenerAñoActual;
        Calendar calendarAño=Calendar.getInstance();
        SimpleDateFormat añoActual=new SimpleDateFormat("yyyy");
        obtenerAñoActual=añoActual.format(calendarAño.getTime());

        int mesActual_=ComprobacionMesUsuario(obtenerMesActual.toUpperCase());
        int diaActual_=Integer.parseInt(obtenerDiaActual);
        int añoActual_=Integer.parseInt(obtenerAñoActual);



        LocalDate start = LocalDate.of(añoUsuario, mesUsuario, diaUsuario);
        LocalDate end = LocalDate.of(añoActual_, mesActual_, diaActual_);
        long years = ChronoUnit.YEARS.between(start, end);
        int edad=(int) years;

        return  edad;
    }
    private int ComprobacionMesUsuario(String mesObtenido) {
        int mes=1;
        switch (mesObtenido){
            case "ENE":
                mes=1;
                break;
            case "FEB":
                mes=2;
                break;
            case "MAR":
                mes=3;
                break;
            case "ABR":
                mes=4;
                break;
            case "MAYO":
                mes=5;
                break;
            case "JUN":
                mes=6;
                break;
            case "JUL":
                mes=7;
                break;
            case "AGO":
                mes=8;
                break;
            case "SEPT":
                mes=9;
                break;
            case "OCT":
                mes=10;
                break;
            case "NOV":
                mes=11;
                break;
            case "DIC":
                mes=12;
                break;
        }
        return mes;
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
                Bitmap bitmap =BitmapFactory.decodeFile(rutaImagen);
                ImageUtils.save(bitmap, rutaImagen, Bitmap.CompressFormat.JPEG);
               Uri bitmap2Uri = UriUtils.file2Uri(FileUtils. getFileByPath(rutaImagen));
               binding.imagenUsuario.setImageURI(bitmap2Uri);
               imagenSelecionada=bitmap2Uri;
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

    private void GuardarDatos() {
        auth=FirebaseAuth.getInstance();
        database=FirebaseDatabase.getInstance();
        storage=FirebaseStorage.getInstance();

        dialog=new ProgressDialog(ConfiguracionPerfilActivity.this);
        dialog.setMessage("Porvafor espere...");
        dialog.setCancelable(false);

        binding.continuarBtn.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View v) {
                String nombreUsuario=binding.nombreUsuario.getText().toString();
                String descripcionUsuario=binding.descripcionUsuario.getText().toString();
                String sexo=binding.sexo.getText().toString();
                int edadUsuario=ComprobacionEdadUsuario();
                if(edadUsuario<18){
                    Toast.makeText(ConfiguracionPerfilActivity.this, "ingrese una fecha mayor", Toast.LENGTH_SHORT).show();
                    return;
                }else if(nombreUsuario.isEmpty()){
                    binding.nombreUsuario.setError("Ingrese un nombre porfavor");
                    return;
                }else if(descripcionUsuario.isEmpty()){
                    binding.descripcionUsuario.setError("Ingrese una descripcion porfavor");
                    return;
                }else if(sexo.isEmpty()){
                    Toast.makeText(ConfiguracionPerfilActivity.this, "Selecione un sexo", Toast.LENGTH_SHORT).show();
                    return;
                }else if(imagenSelecionada!=null){
                    UsuarioAgregado(imagenSelecionada);
                }else{
                    Uri uriImage = Uri.parse("android.resource://" + getPackageName() +"/"+ R.drawable.avatar);
                    UsuarioAgregado(uriImage);
                }
            }
        });

    }

    private void UsuarioAgregado(Uri imagenSelecionada) {
        dialog.show();
        StorageReference reference=storage.getReference().child("Perfiles").child(auth.getCurrentUser().getUid());
        reference.putFile(imagenSelecionada).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull @NotNull Task<UploadTask.TaskSnapshot> task) {
                if(task.isSuccessful()){
                    reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            String imagenUsuario=uri.toString();
                            String idUsuario=auth.getCurrentUser().getUid();
                            String telefonoUsuario=auth.getCurrentUser().getPhoneNumber();
                            Usuario usuario=new Usuario(binding.nombreUsuario.getText().toString(),binding.descripcionUsuario.getText().toString(),telefonoUsuario,imagenUsuario,idUsuario,binding.sexo.getText().toString(),binding.daterPikerButton.getText().toString());

                            database.getReference().child("Usuarios")
                                    .child(idUsuario)
                                    .setValue(usuario)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull @NotNull Task<Void> task) {
                                            if(task.isSuccessful()){
                                                EnviarInicio();
                                            }else{
                                                dialog.dismiss();
                                                Toast.makeText(ConfiguracionPerfilActivity.this, "se ha producido un error", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                        }
                    });
                }
            }
        });
    }


    private void EnviarInicio() {
        dialog.dismiss();
       Toast.makeText(this, "Acceso concedido", Toast.LENGTH_SHORT).show();
        Intent intent=new Intent(ConfiguracionPerfilActivity.this, InicioActivity.class);
        startActivity(intent);
       finish();
    }

    @Override
    protected void onStart() {
        super.onStart();

        if(FirebaseAuth.getInstance().getCurrentUser()!=null){
            database.getReference().child("Usuarios").child(auth.getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                    if(snapshot.exists()){
                        FechaTiempoDeIngresoSesion();
                        Intent intent=new Intent(ConfiguracionPerfilActivity.this,InicioActivity.class);
                        startActivity(intent);
                        finish();
                    }
                }

                @Override
                public void onCancelled(@NonNull @NotNull DatabaseError error) {

                }
            });
        }
    }

    private void FechaTiempoDeIngresoSesion() {

        String guardarFechaActual, guardarTiempoActual;
        Calendar calendar=Calendar.getInstance();

        SimpleDateFormat fechaActual=new SimpleDateFormat("dd-MMM-yyyy");
        guardarFechaActual=fechaActual.format(calendar.getTime());

        SimpleDateFormat tiempoActual=new SimpleDateFormat("hh:mm a");
        guardarTiempoActual=tiempoActual.format(calendar.getTime());

        HashMap<String,Object> hashMap=new HashMap<>();
        hashMap.put("estado","Conectado");
        hashMap.put("fecha",guardarFechaActual);
        hashMap.put("hora",guardarTiempoActual);

        database.getReference().child("EstadoUsuario").child(auth.getCurrentUser().getUid())
                .child("Estado")
                .setValue(hashMap);
    }
}