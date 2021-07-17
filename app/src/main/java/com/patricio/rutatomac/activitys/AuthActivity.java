package com.patricio.rutatomac.activitys;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.patricio.rutatomac.R;
import com.patricio.rutatomac.activitys.client.RegisterActivity;
import com.patricio.rutatomac.activitys.driver.RegisterDriverActivity;
import com.patricio.rutatomac.includes.MyToolbar;

import androidx.appcompat.app.AppCompatActivity;


public class AuthActivity extends AppCompatActivity {

    Button goLogin, goRegister;

    SharedPreferences mPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);
        MyToolbar.show(this, "Seleccione una opción", true);

        mPref = getApplicationContext().getSharedPreferences("typeUser", MODE_PRIVATE);

        goLogin = findViewById(R.id.btngoToLogin);
        goRegister= findViewById(R.id.btngoToRegister);

        goLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToLogin();
            }
        });

        goRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToRegister();
            }
        });
    }

    private void goToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }
    private void goToRegister() {
        String typeUser = mPref.getString("user", "");
        if (typeUser.equals("client")){
            Intent intent = new Intent(this, RegisterActivity.class);
            startActivity(intent);
        }else{
            Intent intent = new Intent(this, RegisterDriverActivity.class);
            startActivity(intent);
        }

    }
}