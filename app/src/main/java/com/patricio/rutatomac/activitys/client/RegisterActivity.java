package com.patricio.rutatomac.activitys.client;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.patricio.rutatomac.R;
import com.patricio.rutatomac.includes.MyToolbar;
import com.patricio.rutatomac.model.Client;
import com.patricio.rutatomac.providers.AuthProvider;
import com.patricio.rutatomac.providers.ClientProvider;

import androidx.appcompat.app.AppCompatActivity;
import dmax.dialog.SpotsDialog;

public class RegisterActivity extends AppCompatActivity {


    AuthProvider mAuthProvider;
    ClientProvider mClientProvider;

    Button btnRegister;
    TextInputEditText txtUser, txtEmail, txtPassword;
    AlertDialog mDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        MyToolbar.show(this, "Registro de usuario", true);


        mAuthProvider = new AuthProvider();
        mClientProvider =  new ClientProvider();

        btnRegister = findViewById(R.id.btnRegister);
        txtUser = findViewById(R.id.txtUser);
        txtEmail = findViewById(R.id.txtEmail);
        txtPassword = findViewById(R.id.txtPassword);

        btnRegister.setOnClickListener(view -> {
            clickRegister();
        });

        mDialog = new SpotsDialog.Builder().setContext(RegisterActivity.this).setMessage("Espere un momento").build();
    }

    private void clickRegister() {
        final String user = txtUser.getText().toString().trim();
        final String email = txtEmail.getText().toString().trim();
        final String password = txtPassword.getText().toString().trim();

        if(!user.isEmpty() && !email.isEmpty() && !password.isEmpty()){
            if (password.length()>=6){
                mDialog.show();
                register(user, email, password);
            }else{
                txtPassword.setError("Ingrese al menos 6 caracteres");
            }
        }else{
            if (user.isEmpty()){
                txtUser.setError("Campo vacío");
            }
            if (email.isEmpty()){
                txtEmail.setError("Campo vacío");
            }
            if (password.isEmpty()){
                txtPassword.setError("Campo vacío");
            }
        }


    }

    private void register(String name, String email, String password) {
        mAuthProvider.register(email, password).addOnCompleteListener(task -> {
            mDialog.show();
            if (task.isSuccessful()){
                mDialog.hide();
                String id = FirebaseAuth.getInstance().getCurrentUser().getUid();
                Client client = new Client(id, name, email);
                create(client);
            }else{
                Toast.makeText(this, "No se pudo registrar el usuario", Toast.LENGTH_SHORT).show();
            }
        });

    }

    public void create(Client client){
        mClientProvider.create(client).addOnCompleteListener(task -> {
            if (task.isSuccessful()){
                //Toast.makeText(this, "Registro realizado correctamente", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(this, MapClientActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }else{
                Toast.makeText(this, "No se pudo crear el cliente", Toast.LENGTH_SHORT).show();
            }
        });
    }
/*
    private void saveUser(String id, String user, String email) {
        String selectedUser = mPref.getString("user", "");
        User objectUser = new User();
        objectUser.setEmail(email);
        objectUser.setName(user);
        Log.e("TAG", "saveUser: "+selectedUser);

        if (selectedUser.equals("driver")){
            mDatabase.child("Users").child("Drivers").child(id).setValue(objectUser).addOnCompleteListener(task -> {
                if (task.isSuccessful()){
                    messageSuccess();
                }else{
                    messageFail();
                }
            });
        }else if(selectedUser.equals("client")){
            mDatabase.child("Users").child("Clients").child(id).setValue(objectUser).addOnCompleteListener(task -> {
                if (task.isSuccessful()){
                    messageSuccess();
                }else{
                    messageFail();
                }
            });
        }
    }
    public void messageFail(){
        Toast.makeText(RegisterActivity.this, "Falló el registro", Toast.LENGTH_SHORT).show();
    }
    public void messageSuccess(){
        Toast.makeText(RegisterActivity.this, "Registro exitoso", Toast.LENGTH_SHORT).show();
    }*/
}