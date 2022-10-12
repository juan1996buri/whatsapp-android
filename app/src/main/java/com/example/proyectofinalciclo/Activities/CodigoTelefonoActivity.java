package com.example.proyectofinalciclo.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import com.example.proyectofinalciclo.R;
import com.example.proyectofinalciclo.databinding.ActivityCodigoTelefonoBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

import static androidx.constraintlayout.motion.utils.Oscillator.TAG;

public class CodigoTelefonoActivity extends AppCompatActivity {

    private ActivityCodigoTelefonoBinding binding;

    private FirebaseAuth auth;
    private String VerificacionId;
    private ProgressDialog dialog;

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendingToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityCodigoTelefonoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //getSupportActionBar().hide();


        String numeroTelefonico=getIntent().getStringExtra("numeroTelefonico");
        binding.textNumeroTelefono.setText(numeroTelefonico);
        auth=FirebaseAuth.getInstance();

     /*   binding.continuarBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PhoneAuthCredential credential=PhoneA
            }
        });



        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                numeroTelefonico,
                60,
                TimeUnit.SECONDS,
                this,
                mCallbacks);

        mCallbacks=new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull @NotNull PhoneAuthCredential phoneAuthCredential) {
                signInWithPhoneAuthCredential(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(@NonNull @NotNull FirebaseException e) {
                Toast.makeText(CodigoTelefonoActivity.this, "numero invalido", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCodeSent(@NonNull @NotNull String s, @NonNull @NotNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                super.onCodeSent(s, forceResendingToken);
                mResendingToken=forceResendingToken;
                mVerificationId=s;
            }
        };*/

        dialog=new ProgressDialog(this);
        dialog.setMessage("Porfavor espere...");
        dialog.setCancelable(false);
        dialog.show();

        PhoneAuthOptions options=PhoneAuthOptions.newBuilder(auth)
                .setPhoneNumber(numeroTelefonico)
                .setActivity(CodigoTelefonoActivity.this)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    @Override
                    public void onVerificationCompleted(@NonNull @NotNull PhoneAuthCredential phoneAuthCredential) {
                        dialog.dismiss();
                    }

                    @Override
                    public void onVerificationFailed(@NonNull @NotNull FirebaseException e) {
                        Toast.makeText(CodigoTelefonoActivity.this, "se ha producido un error", Toast.LENGTH_SHORT).show();

                        dialog.dismiss();
                        finish();
                    }

                    @Override
                    public void onCodeSent(@NonNull @NotNull String verificarId, @NonNull @NotNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                        super.onCodeSent(verificarId, forceResendingToken);

                        VerificacionId=verificarId;
                        //InputMethodManager imm=(InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        //imm.toggleSoftInput(InputMethodManager.SHOW_FORCED,0);
                        dialog.dismiss();

                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);

                        imm.showSoftInput(binding.codigo, InputMethodManager.SHOW_FORCED);
                        binding.codigo.requestFocus();

                    }
                }).build();
        PhoneAuthProvider.verifyPhoneNumber(options);



        binding.continuarBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String codigo=binding.codigo.getOTP();
                if(codigo.isEmpty()){
                    Toast.makeText(CodigoTelefonoActivity.this, "ingrese un codigo porfavor", Toast.LENGTH_SHORT).show();
                }else{
                    PhoneAuthCredential credential=PhoneAuthProvider.getCredential(VerificacionId,codigo);
                    auth.signInWithCredential(credential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull @NotNull Task<AuthResult> task) {
                          if(task.isSuccessful()){
                              Toast.makeText(CodigoTelefonoActivity.this, "Acceso concedido", Toast.LENGTH_SHORT).show();
                              Intent intent=new Intent(CodigoTelefonoActivity.this,ConfiguracionPerfilActivity.class);
                              startActivity(intent);
                              finishAffinity();
                          }else{
                              Toast.makeText(CodigoTelefonoActivity.this, "Codigo incorrecto", Toast.LENGTH_SHORT).show();
                              Intent intent=new Intent(CodigoTelefonoActivity.this,NumeroTelefonicoActivity.class);
                              startActivity(intent);
                              finish();
                          }
                        }
                    });
                }
            }
        });


    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential){
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull @NotNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(CodigoTelefonoActivity.this, "acceso concedido", Toast.LENGTH_SHORT).show();
                            sendUserToMainActivity();
                        }else{
                            Toast.makeText(CodigoTelefonoActivity.this, ""+task.getException().toString(), Toast.LENGTH_SHORT).show();

                        }
                    }
                });
    }

    private void sendUserToMainActivity(){
        Intent intent=new Intent(CodigoTelefonoActivity.this,ConfiguracionPerfilActivity.class);
        startActivity(intent);
        finishAffinity();
    }


}