package com.patricio.rutatomac.providers;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.patricio.rutatomac.model.Driver;

public class DriverProvider {
    DatabaseReference mDatabasse;

    public DriverProvider(){
        mDatabasse = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers");
    }

    public Task<Void> create(Driver driver){
        return mDatabasse.child(driver.getId()).setValue(driver);
    }
}
