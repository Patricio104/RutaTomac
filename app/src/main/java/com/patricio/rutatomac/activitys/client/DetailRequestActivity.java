package com.patricio.rutatomac.activitys.client;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.SquareCap;
import com.patricio.rutatomac.R;
import com.patricio.rutatomac.includes.MyToolbar;
import com.patricio.rutatomac.providers.GoogleApiProvider;
import com.patricio.rutatomac.utils.DecodePoints;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DetailRequestActivity extends AppCompatActivity implements OnMapReadyCallback {

    private SupportMapFragment mMapFragment;
    private GoogleMap mMap;

    private double mExtraOriginLat;
    private double mExtraOriginLon;
    private double mExtraDestinationLat;
    private double mExtraDestinationLon;
    private String mExtraOrigin;
    private String mExtraDestination;

    private LatLng mOriginLatLon;
    private LatLng mDestinationLatLon;

    private GoogleApiProvider googleApiProvider;

    private List<LatLng> mPolylineList;
    private PolylineOptions mPolylineOptions;

    private TextView txtViewOrigin;
    private TextView txtViewDestination;
    private TextView txtViewTime;
    private TextView txtViewDistance;

    private Button mButtonRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_request);

        MyToolbar.show(this, "TUS DATOS", true);

        mMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapDriver);
        mMapFragment.getMapAsync(this);

        mExtraOriginLat =getIntent().getDoubleExtra("origin_lat", 0);
        mExtraOriginLon =getIntent().getDoubleExtra("origin_lon", 0);
        mExtraDestinationLat =getIntent().getDoubleExtra("destination_lat", 0);
        mExtraDestinationLon =getIntent().getDoubleExtra("destination_lon", 0);
        mExtraOrigin =getIntent().getStringExtra("origin");
        mExtraDestination =getIntent().getStringExtra("destination");


        mOriginLatLon = new LatLng(mExtraOriginLat, mExtraOriginLon);
        mDestinationLatLon = new LatLng(mExtraDestinationLat, mExtraDestinationLon);

        googleApiProvider = new GoogleApiProvider(DetailRequestActivity.this);

        txtViewOrigin= findViewById(R.id.txtViewOrigin);
        txtViewDestination= findViewById(R.id.txtViewDestination);
        txtViewTime= findViewById(R.id.txtViewTime);
        txtViewDistance= findViewById(R.id.txtViewDistance);
        mButtonRequest = findViewById(R.id.btnRequestNow);

        txtViewOrigin.setText(mExtraOrigin);
        txtViewDestination.setText(mExtraDestination);

        mButtonRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                gotoRequestDriver();
            }
        });

    }

    private void gotoRequestDriver() {
        Intent intent = new Intent(DetailRequestActivity.this, RequestDriverActivity.class);
        intent.putExtra("origin_lat", mOriginLatLon.latitude);
        intent.putExtra("origin_lon", mOriginLatLon.longitude);
        startActivity(intent);
        finish();
    }

    private void drawRoute(){
        googleApiProvider.getDirections(mOriginLatLon, mDestinationLatLon).enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                try {
                    JSONObject jsonObject = new JSONObject(response.body());
                    JSONArray jsonArray = jsonObject.getJSONArray("routes");
                    JSONObject route = jsonArray.getJSONObject(0);
                    JSONObject polylines = route.getJSONObject("overview_polyline");
                    String points = polylines.getString("points");
                    mPolylineList = DecodePoints.decodePoly(points);
                    mPolylineOptions = new PolylineOptions();
                    mPolylineOptions.color(Color.DKGRAY);
                    mPolylineOptions.width(13f);
                    mPolylineOptions.startCap(new SquareCap());
                    mPolylineOptions.jointType(JointType.ROUND);
                    mPolylineOptions.addAll(mPolylineList);
                    mMap.addPolyline(mPolylineOptions);

                    JSONArray legs = route.getJSONArray("legs");
                    JSONObject leg = legs.getJSONObject(0);
                    JSONObject distance = leg.getJSONObject("distance");
                    JSONObject duration = leg.getJSONObject("duration");
                    String textDistance = distance.getString("text");
                    String textDuration = duration.getString("text");

                    txtViewTime.setText(textDuration);
                    txtViewDistance.setText(textDistance);


                }catch (Exception e){
                    Log.e("Error", "Error encontrado: "+e.getMessage());
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {

            }
        });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.getUiSettings().setZoomControlsEnabled(true);

        mMap.addMarker(new MarkerOptions().position(mOriginLatLon).icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_pin_red)));
        mMap.addMarker(new MarkerOptions().position(mDestinationLatLon).icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_pin_blue)));

        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(
                new CameraPosition.Builder()
                .target(mOriginLatLon)
                .zoom(14f)
                .build()
        ));
        drawRoute();
    }
}