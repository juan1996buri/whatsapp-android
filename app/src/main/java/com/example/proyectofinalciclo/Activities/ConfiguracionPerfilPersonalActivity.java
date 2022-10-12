package com.example.proyectofinalciclo.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
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
import android.widget.DatePicker;
import android.widget.Toast;

import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.ImageUtils;
import com.blankj.utilcode.util.UriUtils;
import com.example.proyectofinalciclo.R;
import com.example.proyectofinalciclo.Clases.Usuario;
import com.example.proyectofinalciclo.databinding.ActivityConfiguracionPerfilPersonalBinding;
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
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.HashMap;

public class ConfiguracionPerfilPersonalActivity extends AppCompatActivity {

    private ActivityConfiguracionPerfilPersonalBinding binding;
    private FirebaseAuth auth;
    private FirebaseDatabase database;
    private FirebaseStorage storage;
    private ProgressDialog dialog;
    private String rutaImagen;
    private final int REQUEST_PERMISSION_CAMERA=100;
    private final int REQUEST_IMAGE_CAMERA=101;
    private final int GALERIA=1;
    private final int  PERMISSION_CODE=2;
    private Uri imagenSelecionada;
    private Usuario usuario;
    private DatePickerDialog datePickerDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityConfiguracionPerfilPersonalBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        auth=FirebaseAuth.getInstance();
        database=FirebaseDatabase.getInstance();
        storage=FirebaseStorage.getInstance();

        ObtenerDatoUsuario();

        binding.retroceder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        binding.actualizarBtn.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View v) {
                GuardarDatosConfigurados();
            }
        });
        binding.camara.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
                    if(ActivityCompat.checkSelfPermission(ConfiguracionPerfilPersonalActivity.this, Manifest.permission.CAMERA)== PackageManager.PERMISSION_GRANTED){
                        goToCamara();
                    }else{
                        ActivityCompat.requestPermissions(ConfiguracionPerfilPersonalActivity.this, new String[]{Manifest.permission.CAMERA},REQUEST_PERMISSION_CAMERA);
                    }
                }
            }
        });

        binding.imagenUsuario.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
        });
    }
    private void DatosFechaUsuario(int month, int day,int year) {
        DatePickerDialog.OnDateSetListener dateSetListener=new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int day) {
                month=month+1;

                String date=HacerFecha(day,month,year);
                binding.daterPikerButton.setText(date);
            }
        };

        int style= AlertDialog.THEME_HOLO_LIGHT;

        datePickerDialog=new DatePickerDialog(this, style,dateSetListener,year,month,day );
        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
    }


    private String HacerFecha(int day, int month, int year) {
        return getFormatoFecha(month)+" "+day+" "+year;
    }

    public  void  openDatePicker(View view){
        datePickerDialog.show();
    }

    private void SeleccinarImagenUsuario() {
        Intent intent=new Intent();
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent,GALERIA);
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

    private void ObtenerDatoUsuario() {
        dialog=new ProgressDialog(ConfiguracionPerfilPersonalActivity.this);
        dialog.setMessage("Cargando porvafor espere...");
        dialog.setCancelable(false);
        dialog.show();
        database.getReference().child("Usuarios").child(auth.getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {

               if ((snapshot.exists()) )
                {
                    Resources res=getResources();
                    String[] arrayList= res.getStringArray(R.array.sexos);
                    ArrayAdapter arrayAdapter=new ArrayAdapter(ConfiguracionPerfilPersonalActivity.this,R.layout.dropdown_items,arrayList);

                    usuario=snapshot.getValue(Usuario.class);
                    binding.nombreUsuario.setText(usuario.getNombreUsuario());
                    binding.descripcionUsuario.setText(usuario.getDescripcionUsuario());
                    binding.daterPikerButton.setText(usuario.getFechaNacimiento());

                    Picasso.with(ConfiguracionPerfilPersonalActivity.this).load(usuario.getImagenUsuario()).fit().centerCrop()
                            .placeholder(R.drawable.avatar)
                            .error(R.drawable.avatar)
                            .placeholder(R.drawable.avatar)
                            .into(binding.imagenUsuario);

                    String string = usuario.getFechaNacimiento();
                    String[] parts = string.split(" ");
                    String mes = parts[0];
                    String dia = parts[1];
                    String año = parts[2];

                    int mesUsuario=ComprobacionMesUsuario(mes.toUpperCase(),"usuario");
                    int diaUsuario=Integer.parseInt(dia);
                    int añoUsuario=Integer.parseInt(año);
                    DatosFechaUsuario(mesUsuario,diaUsuario,añoUsuario);

                    binding.telefono.setText(usuario.getTelefonoUsuario());

                    binding.sexo.setText(usuario.getSexoUsuario());
                    binding.sexo.setAdapter(arrayAdapter);
                    dialog.dismiss();
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
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
    private int ComprobacionMesUsuario(String mesObtenido, String accion) {
        int mes=1;
        switch (mesObtenido){
            case "ENE":
                mes=0;
                break;
            case "FEB":
                mes=1;
                break;
            case "MAR":
                mes=2;
                break;
            case "ABR":
                mes=3;
                break;
            case "MAYO":
                mes=4;
                break;
            case "JUN":
                mes=5;
                break;
            case "JUL":
                mes=6;
                break;
            case "AGO":
                mes=7;
                break;
            case "SEPT":
                mes=8;
                break;
            case "OCT":
                mes=9;
                break;
            case "NOV":
                mes=10;
                break;
            case "DIC":
                mes=11;
                break;
        }
        if(accion.equals("usuario")){
            return mes;
        }else{
            return mes+1;
        }

    }




    @RequiresApi(api = Build.VERSION_CODES.O)
    private   int  ComprobacionEdadUsuario(){
        String fechaNacimiento=binding.daterPikerButton.getText().toString();



        String string = fechaNacimiento;
        String[] parts = string.split(" ");
        String mes = parts[0];
        String dia = parts[1];
        String año = parts[2];



        int mesUsuario=ComprobacionMesUsuario(mes,"comparacion");
        int diaUsuario=Integer.parseInt(dia);
        int añoUsuario=Integer.parseInt(año);




        LocalDate fHoy= LocalDate.now();
        LocalDate cumple= LocalDate.of(añoUsuario, mesUsuario, diaUsuario);
        long edad= ChronoUnit.YEARS.between(cumple, fHoy);
        int años=(int) edad;


        return años;


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

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void GuardarDatosConfigurados() {
        dialog=new ProgressDialog(ConfiguracionPerfilPersonalActivity.this);
        dialog.setMessage("Actualizando porfavor espere...");
        dialog.setCancelable(false);
        String nombreUsuario=binding.nombreUsuario.getText().toString();
        String descripcionUsuario=binding.descripcionUsuario.getText().toString();
        if(nombreUsuario.isEmpty()){
            binding.nombreUsuario.setError("ingrese un nombre");
            return;
        }else if(descripcionUsuario.isEmpty()){
            binding.descripcionUsuario.setText("ingrese una descripcion");
            return;
        }else{
            if(imagenSelecionada!=null){
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
                                    GuardarDatosUsuario(imagenUsuario);
                                }
                            });
                        }
                    }
                });
            }else{
               dialog.show();
                GuardarDatosUsuario(usuario.getImagenUsuario());
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void GuardarDatosUsuario(String imagenUsuario) {
        HashMap<String,Object> hashMap=new HashMap<>();
        hashMap.put("idUsuario",usuario.getIdUsuario());
        hashMap.put("descripcionUsuario",binding.descripcionUsuario.getText().toString());
       hashMap.put("fechaNacimiento",binding.daterPikerButton.getText().toString());

        hashMap.put("imagenUsuario",imagenUsuario);
        hashMap.put("nombreUsuario",binding.nombreUsuario.getText().toString());
        hashMap.put("sexoUsuario",binding.sexo.getText().toString());
       hashMap.put("telefonoUsuario",auth.getCurrentUser().getPhoneNumber());

        int edad= ComprobacionEdadUsuario();

        if(edad<18){
            Toast.makeText(this, "ingrese una fecha valida", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
            return;
        }else{
            database.getReference().child("Usuarios").child(auth.getCurrentUser().getUid()).updateChildren(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull @NotNull Task<Void> task) {
                    if(task.isSuccessful()){
                        dialog.dismiss();
                       // Toast.makeText(ConfiguracionPerfilPersonalActivity.this, "datos guardato con exito", Toast.LENGTH_SHORT).show();
                        // Intent intent=new Intent(ConfiguracionPerfilPersonalActivity.this,InicioActivity.class);
                        //startActivity(intent);
                        finish();
                    }else{
                        Toast.makeText(ConfiguracionPerfilPersonalActivity.this, "Intente mas tarde", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
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