package com.patricio.rutatomac.providers;

import android.content.Context;

import com.google.android.gms.maps.model.LatLng;
import com.patricio.rutatomac.R;
import com.patricio.rutatomac.retrofit.IGoogleApi;
import com.patricio.rutatomac.retrofit.RetrofitClient;

import retrofit2.Call;

public class GoogleApiProvider {
    Context context;

    public GoogleApiProvider(Context context){
        this.context=context;
    }

    public Call<String> getDirections(LatLng originLaton, LatLng destinationLatLon){
        String baseUrl = "https://maps.googleapis.com";
        String query = "/maps/api/directions/json?mode=driving&transit_routing_preferences=less_driving&"
                + "origin=" + originLaton.latitude + "," +originLaton.longitude + "&"
                + "destination=" + destinationLatLon.latitude + "," + destinationLatLon.longitude + "&"
                + "key=" + context.getResources().getString(R.string.google_maps_key);

        return RetrofitClient.getClient(baseUrl).create(IGoogleApi.class).getDirections(baseUrl + query);
    }
}
