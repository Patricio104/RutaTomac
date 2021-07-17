package com.patricio.rutatomac.activitys.driver;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.patricio.rutatomac.R;
import com.patricio.rutatomac.includes.MyToolbar;
import com.patricio.rutatomac.model.Driver;
import com.patricio.rutatomac.providers.AuthProvider;
import com.patricio.rutatomac.providers.DriverProvider;

import androidx.appcompat.app.AppCompatActivity;
import dmax.dialog.SpotsDialog;

public class RegisterDriverActivity extends AppCompatActivity {

    SharedPreferences mPref;

    AuthProvider mAuthProvider;
    DriverProvider mDriverProvider;

    Button btnRegister;
    TextInputEditText txtUser, txtEmail, txtPassword, txtMarca, txtPlaca, txtNumero, txtRuta;
    AlertDialog mDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_driver);
        MyToolbar.show(this, "Registro de usuario", true);

        mAuthProvider = new AuthProvider();
        mDriverProvider =  new DriverProvider();

        btnRegister = findViewById(R.id.btnRegister);
        txtUser = findViewById(R.id.txtUser);
        txtEmail = findViewById(R.id.txtEmail);
        txtPassword = findViewById(R.id.txtPassword);
        txtMarca = findViewById(R.id.txtMarca);
        txtPlaca = findViewById(R.id.txtPlaca);
        txtNumero = findViewById(R.id.txtNumeroVehiculo);
        txtRuta = findViewById(R.id.txtRuta);

        mPref = getApplicationContext().getSharedPreferences("typeUser", MODE_PRIVATE);

        btnRegister.setOnClickListener(view -> {
            clickRegister();
        });

        mDialog = new SpotsDialog.Builder().setContext(RegisterDriverActivity.this).setMessage("Espere un momento").build();
    }

    private void clickRegister() {
        final String user = txtUser.getText().toString().trim();
        final String email = txtEmail.getText().toString().trim();
        final String password = txtPassword.getText().toString().trim();
        final String marca = txtMarca.getText().toString().trim();
        final String placa = txtPlaca.getText().toString().trim();
        final String numero = txtNumero.getText().toString().trim();
        final String ruta = txtRuta.getText().toString().trim();

        if(!user.isEmpty() && !email.isEmpty() && !password.isEmpty() && !marca.isEmpty() && !placa.isEmpty() && !numero.isEmpty() && !ruta.isEmpty()){
            if (password.length()>=6){
                mDialog.show();
                register(user, email, password, marca, placa, numero, ruta);
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
            if (marca.isEmpty()){
                txtMarca.setError("Campo vacío");
            }
            if (placa.isEmpty()){
                txtMarca.setError("Campo vacío");
            }
            if (numero.isEmpty()){
                txtNumero.setError("Campo vacío");
            }
            if (ruta.isEmpty()){
                txtRuta.setError("Campo vacío");
            }
        }


    }

    private void register(String name, String email, String password, String marca, String placa, String numero, String ruta) {
        mAuthProvider.register(email, password).addOnCompleteListener(task -> {
            mDialog.show();
            if (task.isSuccessful()){
                mDialog.hide();
                String id = FirebaseAuth.getInstance().getCurrentUser().getUid();
                Driver driver = new Driver(id, name, email, marca, placa, numero, ruta);
                create(driver);
            }else{
                mDialog.hide();
                Toast.makeText(this, "No se pudo registrar el usuario", Toast.LENGTH_SHORT).show();
            }
        });

    }

    public void create(Driver driver){
        mDriverProvider.create(driver).addOnCompleteListener(task -> {
            if (task.isSuccessful()){
                //Toast.makeText(this, "Registro realizado correctamente", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(this, MapDriverActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }else{
                Toast.makeText(this, "No se pudo crear el cliente", Toast.LENGTH_SHORT).show();
            }
        });
    }
}