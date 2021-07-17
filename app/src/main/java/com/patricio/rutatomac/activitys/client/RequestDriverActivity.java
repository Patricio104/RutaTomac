package com.patricio.rutatomac.activitys.client;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DatabaseError;
import com.patricio.rutatomac.R;
import com.patricio.rutatomac.providers.GeofireProvider;

import androidx.appcompat.app.AppCompatActivity;

public class RequestDriverActivity extends AppCompatActivity {

    private LottieAnimationView mAnimation;
    private TextView txtViewLookingFor;
    private Button mButtonCancelRequest;

    private GeofireProvider mGeofireProvider;

    private double mExtraLatitud;
    private double mExtraLongitud;
    private LatLng mOriginLatLon;

    private double mRadius = 0.1;

    private boolean mDriverFound= false;
    private String mIdDriverFound = "";
    private LatLng mDriverLatLon;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_driver);

        mAnimation = findViewById(R.id.animation);
        txtViewLookingFor = findViewById(R.id.txtViewLookingFor);
        mButtonCancelRequest = findViewById(R.id.btnCancelRequest);

        mAnimation.playAnimation();

        mExtraLatitud = getIntent().getDoubleExtra("origin_lat",0);
        mExtraLatitud = getIntent().getDoubleExtra("origin_lon",0);
        mOriginLatLon = new LatLng(mExtraLatitud, mExtraLongitud);
        mGeofireProvider = new GeofireProvider();

        getClosesDriver();

    }

    private void getClosesDriver(){
        mGeofireProvider.getActiveDrivers(mOriginLatLon, mRadius).addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                if (!mDriverFound){
                    mDriverFound= true;
                    mIdDriverFound = key;
                    mDriverLatLon = new LatLng(location.latitude, location.longitude);
                    txtViewLookingFor.setText("CONDUCTOR ENCONTRADO\nESPERANDO RESPUESTA");

                    Log.e("TAG", "ID_Driver encontrado: "+ mIdDriverFound);
                }
            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {
                //INGRESA CUANDO TERMINA LA BUSQUEDA DE UN CONDCUCTOR EN UN RADIO DE 0.1 KM
                if (!mDriverFound){
                    mRadius = mRadius + 0.4f;

                    //no encontró ningún conductor
                    if (mRadius > 10){
                        Toast.makeText(getApplicationContext(),"No se encontró ningún conductor",Toast.LENGTH_SHORT).show();
                        return;
                    }else{
                        getClosesDriver();
                    }
                }

            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }
}