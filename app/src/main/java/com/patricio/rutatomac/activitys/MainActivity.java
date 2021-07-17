package com.patricio.rutatomac.activitys;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;
import com.patricio.rutatomac.R;
import com.patricio.rutatomac.activitys.client.MapClientActivity;
import com.patricio.rutatomac.activitys.driver.MapDriverActivity;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    Button btnClient, btnDriver;

    SharedPreferences mPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mPref = getApplicationContext().getSharedPreferences("typeUser", MODE_PRIVATE);
        final SharedPreferences.Editor editor = mPref.edit();

        btnClient = findViewById(R.id.btnAmClient);
        btnDriver = findViewById(R.id.btnAmDriver);

        btnClient.setOnClickListener(view -> {
            editor.putString("user", "client");
            editor.apply();
            goToSelectAuth();
        });

        btnDriver.setOnClickListener(view -> {
            editor.putString("user", "driver");
            editor.apply();
            goToSelectAuth();
        });
    }

    private void goToSelectAuth() {
        Intent intent = new Intent(this, AuthActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (FirebaseAuth.getInstance().getCurrentUser() != null){
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
        }
    }
}