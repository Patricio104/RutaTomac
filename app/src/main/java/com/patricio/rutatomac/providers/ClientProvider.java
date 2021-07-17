package com.patricio.rutatomac.providers;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.patricio.rutatomac.model.Client;

import java.util.HashMap;
import java.util.Map;


public class ClientProvider {
    DatabaseReference mDatabasse;

    public ClientProvider(){
        mDatabasse = FirebaseDatabase.getInstance().getReference().child("Users").child("Clients");
    }

    public Task<Void> create(Client client){
        Map<String, Object> map = new HashMap<>();
        map.put("name", client.getName());
        map.put("email", client.getEmail());
        return mDatabasse.child(client.getId()).setValue(map);
    }
}
