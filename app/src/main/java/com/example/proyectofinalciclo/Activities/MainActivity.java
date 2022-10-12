package com.example.proyectofinalciclo.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.example.proyectofinalciclo.R;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //getSupportActionBar().hide();

        Thread thread=new Thread(){
            @Override
            public void run() {

                try {
                    sleep(2000);
                }catch (Exception e){

                }
                finally {
                    Intent intent=new Intent(MainActivity.this, NumeroTelefonicoActivity.class);
                    startActivity(intent);
                    finish();
                }
                super.run();
            }
        };
        thread.start();
    }
}