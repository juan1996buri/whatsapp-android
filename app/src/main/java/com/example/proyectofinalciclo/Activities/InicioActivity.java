package com.example.proyectofinalciclo.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.proyectofinalciclo.Fragments.ChatFragment;
import com.example.proyectofinalciclo.Fragments.ContactoFragment;
import com.example.proyectofinalciclo.Fragments.GrupoFragment;
import com.example.proyectofinalciclo.R;
import com.example.proyectofinalciclo.databinding.ActivityInicioBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class InicioActivity extends AppCompatActivity {

    private ActivityInicioBinding binding;
    private FragmentManager fragmentManager;
    private FirebaseAuth auth;
    private FirebaseDatabase database;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityInicioBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth=FirebaseAuth.getInstance();
        database=FirebaseDatabase.getInstance();

        //getSupportActionBar().setTitle("Write Me");


        setSupportActionBar(binding.toolbar);

        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new ChatFragment()).commit();


        binding.bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment fragment=null;
                switch (item.getItemId()){
                    case R.id.menu_chats:
                        fragment=new ChatFragment();
                        break;
                    case R.id.menu_contactos:
                        fragment=new ContactoFragment();
                        break;
                    case R.id.menu_grupos:
                        fragment=new GrupoFragment();
                        break;
                }
                if (fragment!=null){
                    fragmentManager=getSupportFragmentManager();
                    fragmentManager.beginTransaction()
                            .replace(R.id.fragment_container,fragment)
                            .commit();
                }
                return true;
            }
        });
    }



    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){

            case R.id.item_añadir_amigo:
                AgregarAmigos();
                break;
            case R.id.item_solicitud_amistad:
                SolicitudAmistad();

                break;
            case R.id.item_grupo:
                Intent intentGrupo=new Intent(InicioActivity.this, CrearGrupoActivity.class);
                startActivity(intentGrupo);
                break;
            case R.id.item_configuracion_perfil:
                ConfigurarPerfil();
                break;
            case R.id.item_cerrar_secion:
                CerrarSesion("Desconectado");
                auth.signOut();
                Intent intent=new Intent(InicioActivity.this,NumeroTelefonicoActivity.class);
                startActivity(intent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_superior_derecho,menu);
        MenuItem menuItem = menu.findItem(R.id.item_solicitud_amistad);
        return super.onCreateOptionsMenu(menu);
    }



    private void VerificacionUsuario() {
        database.getReference().child("Usuarios").child(auth.getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if(!snapshot.exists()){
                    EnviarConfiguracionPerfil();
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }

    private void EnviarConfiguracionPerfil() {
        Intent intent=new Intent(InicioActivity.this, ConfiguracionPerfilActivity.class);
        startActivity(intent);
        finish();
    }


    private void EnviarLoginNumeroTelefonico() {
        Intent intent=new Intent(InicioActivity.this, NumeroTelefonicoActivity.class);
        startActivity(intent);
        finish();
    }
    private void AgregarAmigos() {
        Intent intent=new Intent(InicioActivity.this, AgregarAmigosActivity.class);
        startActivity(intent);
    }

    private void SolicitudAmistad() {
        Intent intent=new Intent(InicioActivity.this, SolicitudAmistadActivity.class);
        startActivity(intent);
    }



    private void ConfigurarPerfil() {
        Intent intent=new Intent(InicioActivity.this, ConfiguracionPerfilPersonalActivity.class);
        startActivity(intent);
    }

    static void CerrarSesion(String estado) {
        FirebaseAuth auth=FirebaseAuth.getInstance();
        FirebaseDatabase database=FirebaseDatabase.getInstance();

        String guardarFechaActual,guardarTiempoActual;
        Calendar calendar=Calendar.getInstance();

        SimpleDateFormat fechaActual=new SimpleDateFormat("dd-MMM-yyyy");
        guardarFechaActual=fechaActual.format(calendar.getTime());

        SimpleDateFormat horaActual=new SimpleDateFormat("hh:mm a");
        guardarTiempoActual=horaActual.format(calendar.getTime());

        HashMap<String,Object> hashMap=new HashMap<>();
        hashMap.put("hora",guardarTiempoActual);
        hashMap.put("fecha",guardarFechaActual);
        hashMap.put("estado",estado);

        if(auth.getCurrentUser()!=null){
            database.getReference().child("EstadoUsuario").child(auth.getUid())
                    .child("Estado")
                    .updateChildren(hashMap);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        CerrarSesion("Conectado");

    }


    @Override
    protected void onPause() {
        super.onPause();
        CerrarSesion("Desconectado");
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(FirebaseAuth.getInstance()==null){
            EnviarLoginNumeroTelefonico();
        }else{
            CerrarSesion("Conectado");
            VerificacionUsuario();
        }
    }

}

