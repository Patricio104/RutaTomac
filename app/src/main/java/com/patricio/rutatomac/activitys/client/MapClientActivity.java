package com.patricio.rutatomac.activitys.client;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;

import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.firebase.database.DatabaseError;
import com.google.maps.android.SphericalUtil;
import com.patricio.rutatomac.R;
import com.patricio.rutatomac.activitys.MainActivity;
import com.patricio.rutatomac.includes.MyToolbar;
import com.patricio.rutatomac.providers.AuthProvider;
import com.patricio.rutatomac.providers.GeofireProvider;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MapClientActivity extends AppCompatActivity implements OnMapReadyCallback {

    AuthProvider mAuthProvider;
    private GoogleMap mMap;

    private SupportMapFragment mMapFragment;

    private LocationRequest mLocationRequest;
    private FusedLocationProviderClient mFusedLocation;

    private final static int LOCATION_REQUEST_CODE = 1;
    private final static int SETTINGS_REQUEST_CODE = 2;

    private GeofireProvider mGeofireProvider;

    private Marker mMarker;
    private LatLng mCurrentLatLng;

    private List<Marker> mDriversMarkers = new ArrayList<>();

    private boolean mFirstTime = true;

    private PlacesClient mPlaces;
    private AutocompleteSupportFragment mAutocomplete;
    private AutocompleteSupportFragment mAutocompleteDestination;

    private String mOrigin;
    private LatLng mOriginLatLon;
    private String mDestination;
    private LatLng mDestinationLatLon;

    private GoogleMap.OnCameraIdleListener mCameraListener;

    private Button btnRequestDriver;


    LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(@NonNull LocationResult locationResult) {
            for (Location location : locationResult.getLocations()) {
                if (getApplicationContext() != null) {

                    mCurrentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                    /*
                    if (mMarker != null){
                        mMarker.remove();
                    }

                    mMarker = mMap.addMarker(new MarkerOptions().position(
                            new LatLng(location.getLatitude(), location.getLongitude())
                    )
                            .title("Tu posición")
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.my_location)));

                    */

                    //Localización en tiempo real del usuario
                    mMap.moveCamera(CameraUpdateFactory.newCameraPosition(
                            new CameraPosition.Builder()
                                    .target(new LatLng(location.getLatitude(), location.getLongitude()))
                                    .zoom(12f)
                                    .build()
                    ));

                    if (mFirstTime) {
                        mFirstTime = false;
                        getActiveDrivers();
                        limitSearch();
                    }

                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_client);
        MyToolbar.show(this, "Cliente", false);
        mAuthProvider = new AuthProvider();
        mGeofireProvider = new GeofireProvider();
        btnRequestDriver = findViewById(R.id.btnRequestDriver);

        mFusedLocation = LocationServices.getFusedLocationProviderClient(this);

        mMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapDriver);
        mMapFragment.getMapAsync(this);

        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), getResources().getString(R.string.google_maps_key));
        }

        mPlaces = Places.createClient(this);
        instanceAutocompleteOrigin();
        instanceAutocompleteDestination();
        onCameraMove();

        btnRequestDriver.setOnClickListener(view -> {
            requestDriver();
        });

    }

    private void requestDriver() {
        if (mOriginLatLon != null && mDestinationLatLon != null) {
            Intent intent = new Intent(MapClientActivity.this, DetailRequestActivity.class);
            intent.putExtra("origin_lat", mOriginLatLon.latitude);
            intent.putExtra("origin_lon", mOriginLatLon.longitude);
            intent.putExtra("destination_lat", mDestinationLatLon.latitude);
            intent.putExtra("destination_lon", mDestinationLatLon.longitude);
            intent.putExtra("origin", mOrigin);
            intent.putExtra("destination", mDestination);
            startActivity(intent);
        } else {
            Toast.makeText(this, "debe seleccionar el lugar de recogiday de destino", Toast.LENGTH_SHORT).show();
        }
    }

    private void limitSearch() {
        LatLng northSide = SphericalUtil.computeOffset(mCurrentLatLng, 5000, 0);
        LatLng southSide = SphericalUtil.computeOffset(mCurrentLatLng, 5000, 180);
        mAutocomplete.setCountry("MX");
        mAutocomplete.setLocationBias(RectangularBounds.newInstance(southSide, northSide));
        mAutocompleteDestination.setCountry("MX");
        mAutocompleteDestination.setLocationBias(RectangularBounds.newInstance(southSide, northSide));
    }

    private void onCameraMove() {
        mCameraListener = new GoogleMap.OnCameraIdleListener() {
            @Override
            public void onCameraIdle() {
                try {
                    Geocoder geocoder = new Geocoder(MapClientActivity.this);
                    mOriginLatLon = mMap.getCameraPosition().target;
                    List<Address> addressList = geocoder.getFromLocation(mOriginLatLon.latitude, mOriginLatLon.longitude, 1);
                    String city = addressList.get(0).getLocality();
                    String country = addressList.get(0).getCountryName();
                    String address = addressList.get(0).getAddressLine(0);
                    mOrigin = address + " " + city;
                    mAutocomplete.setText(address + " " + city);
                } catch (Exception e) {
                    Log.e("Error", "mensaje: " + e.getMessage());
                }
            }
        };
    }

    private void instanceAutocompleteOrigin() {
        mAutocomplete = (AutocompleteSupportFragment) getSupportFragmentManager().findFragmentById(R.id.placeAutocompleteOrigin);
        mAutocomplete.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.LAT_LNG, Place.Field.NAME));
        mAutocomplete.setHint("Lugar de recogida");
        mAutocomplete.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                mOrigin = place.getName();
                mOriginLatLon = place.getLatLng();
                Log.e("PLACE", "Name: " + mOrigin);
                Log.e("PLACE", "Lat: " + mOriginLatLon.latitude);
                Log.e("PLACE", "Lon: " + mOriginLatLon.longitude);
            }

            @Override
            public void onError(@NonNull Status status) {
                Log.e("PLACE ERROR", "IS: " + status);
            }
        });
    }

    private void instanceAutocompleteDestination() {
        mAutocompleteDestination = (AutocompleteSupportFragment) getSupportFragmentManager().findFragmentById(R.id.placeAutocompleteDestination);
        mAutocompleteDestination.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.LAT_LNG, Place.Field.NAME));
        mAutocompleteDestination.setHint("Lugar de destino");
        mAutocompleteDestination.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                mDestination = place.getName();
                mDestinationLatLon = place.getLatLng();
                Log.e("PLACE", "Name: " + mDestination);
                Log.e("PLACE", "Lat: " + mDestinationLatLon.latitude);
                Log.e("PLACE", "Lon: " + mDestinationLatLon.longitude);
            }

            @Override
            public void onError(@NonNull Status status) {
                Log.e("PLACE ERROR", "IS: " + status);
            }
        });
    }

    public void getActiveDrivers() {
        mGeofireProvider.getActiveDrivers(mCurrentLatLng, 10).addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                //Añadir los marcadores de los conductores que se conecten a la aplicación
                for (Marker marker : mDriversMarkers) {
                    if (marker.getTag() != null) {
                        if (marker.getTag().equals(key)) {
                            return;
                        }
                    }
                }

                LatLng driverLocation = new LatLng(location.latitude, location.longitude);
                Marker marker = mMap.addMarker(new MarkerOptions().position(driverLocation).title("Conductor disponible").icon(BitmapDescriptorFactory.fromResource(R.drawable.coche)));
                marker.setTag(key);
                mDriversMarkers.add(marker);
            }

            @Override
            public void onKeyExited(String key) {
                for (Marker marker : mDriversMarkers) {
                    if (marker.getTag() != null) {
                        if (marker.getTag().equals(key)) {
                            marker.remove();
                            mDriversMarkers.remove(marker);
                            return;
                        }
                    }
                }
            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {
                //Actualizar la posición de cada conductor
                for (Marker marker : mDriversMarkers) {
                    if (marker.getTag() != null) {
                        if (marker.getTag().equals(key)) {
                            marker.setPosition(new LatLng(location.latitude, location.longitude));
                        }
                    }
                }
            }

            @Override
            public void onGeoQueryReady() {

            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mMap.setMyLocationEnabled(true);

        mMap.setOnCameraIdleListener(mCameraListener);

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(3000);
        mLocationRequest.setFastestInterval(3000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(5);
        startLocation();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.driver_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            logout();
        }
        return super.onOptionsItemSelected(item);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    if (gpsActived()){
                        mFusedLocation.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                        mMap.setMyLocationEnabled(true);
                    }else {
                        showAlertDialogNOGPS();
                    }

                }
                else{
                    checkLocationPermission();
                }
            }
            else{
                checkLocationPermission();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SETTINGS_REQUEST_CODE && gpsActived()) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                return;
            }
            mFusedLocation.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
            mMap.setMyLocationEnabled(true);
        }else if(requestCode ==  SETTINGS_REQUEST_CODE && !gpsActived()){
            showAlertDialogNOGPS();
        }
    }

    private void showAlertDialogNOGPS() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Por favor activa tu ubicacion para continuar")
                .setPositiveButton("Configuraciones", (dialogInterface, i) -> startActivityForResult(
                        new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS), SETTINGS_REQUEST_CODE))
                .create()
                .show();
    }

    private boolean gpsActived(){
        boolean isActive = false;
        Log.e("TAG", "gpsActived: No ");
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            Log.e("TAG", "gpsActived: Si ");
            isActive = true;
        }
        return isActive;
    }

    //Metodo escuchador de la ubicación
    private void startLocation() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Log.e("TAG", "startLocation: M");
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                Log.e("StartLocation", "gpsActived: Si ");
                if (gpsActived()){
                    mFusedLocation.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                    mMap.setMyLocationEnabled(true);
                }else{
                    showAlertDialogNOGPS();
                }

            }else{
                Log.e("TAG", "gpsActived: No ");
                checkLocationPermission();
            }
        } else {
            if (gpsActived()){
                mFusedLocation.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                mMap.setMyLocationEnabled(true);
            }else{
                showAlertDialogNOGPS();
            }

        }
    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                Log.e("TAG", "startLocation: pide permisos 1 location");
                new AlertDialog.Builder(this)
                        .setTitle("Proporciona los permisos para continuar")
                        .setMessage("Esta aplicación requiere de los permisos de ubicación para poder utilizarse")
                        .setPositiveButton("OK", (dialogInterface, i) -> {
                            ActivityCompat.requestPermissions(MapClientActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
                        })
                        .create()
                        .show();
            } else {
                Log.e("TAG", "startLocation: Pide permisos 2");
                ActivityCompat.requestPermissions(MapClientActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);

            }
        }
    }

    public void logout() {
        mAuthProvider.logout();
        Intent intent = new Intent(MapClientActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}