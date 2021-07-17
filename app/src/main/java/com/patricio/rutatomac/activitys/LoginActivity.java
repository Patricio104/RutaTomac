package com.patricio.rutatomac.activitys;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.patricio.rutatomac.R;
import com.patricio.rutatomac.activitys.client.MapClientActivity;
import com.patricio.rutatomac.activitys.driver.MapDriverActivity;
import com.patricio.rutatomac.includes.MyToolbar;

import androidx.appcompat.app.AppCompatActivity;
import dmax.dialog.SpotsDialog;

public class
LoginActivity extends AppCompatActivity {

    TextInputEditText txtUser, txtPassword;
    Button btnLogin;
    FirebaseAuth mAuth;
    DatabaseReference mDatabse;
    AlertDialog mDialog;
    SharedPreferences mPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        MyToolbar.show(this, "Login de usuario", true);

        txtUser = findViewById(R.id.txtInputEmail);
        txtPassword = findViewById(R.id.txtInputPassword);
        btnLogin =findViewById(R.id.btnLogin);

        mPref = getApplicationContext().getSharedPreferences("typeUser", MODE_PRIVATE);

        mAuth = FirebaseAuth.getInstance();

        mDatabse = FirebaseDatabase.getInstance().getReference();

        mDialog = new SpotsDialog.Builder().setContext(LoginActivity.this).setMessage("Espere un momento").build();

        btnLogin.setOnClickListener(view -> login());
    }

    private void login() {
        String email = txtUser.getText().toString().trim();
        String password = txtPassword.getText().toString().trim();
        Log.e("IO", "email: "+email+ " Pass: "+password );

        if (!email.isEmpty() && !password.isEmpty()){
            if (password.length()>=6){
                mDialog.show();
                mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
                    if (task.isSuccessful()){
                        String user= mPref.getString("user", "");
                        if (user.equals("client")){
                            Intent intent = new Intent(this, MapClientActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                        }else{
                            Intent intent = new Intent(this, MapDriverActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                        }
                    }else{
                        Toast.makeText(LoginActivity.this, "La contraseña o el password son incorrectos", Toast.LENGTH_SHORT).show();
                    }
                    mDialog.dismiss();
                });
            }else{
                txtPassword.setError("La contraseña es menor de 6 cracteres");
            }
        }else{
            if(email.isEmpty()){
                txtUser.setError("Email vacío");
            }
            if (password.isEmpty()){
                txtPassword.setError("Contraseña vació");
            }
        }
    }
}