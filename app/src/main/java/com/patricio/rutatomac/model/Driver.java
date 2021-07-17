package com.patricio.rutatomac.model;

public class Driver {
    String id;
    String name;
    String email;
    String vehicleBrand;
    String vehiclePlate;
    String vehicleNumber;
    String vehicleRuta;

    public Driver(String id, String name, String email, String vehicleBrand, String vehiclePlate, String vehicleNumber, String vehicleRuta) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.vehicleBrand = vehicleBrand;
        this.vehiclePlate = vehiclePlate;
        this.vehicleNumber = vehicleNumber;
        this.vehicleRuta = vehicleRuta;
    }

    public Driver() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getVehicleBrand() {
        return vehicleBrand;
    }

    public void setVehicleBrand(String vehicleBrand) {
        this.vehicleBrand = vehicleBrand;
    }

    public String getVehiclePlate() {
        return vehiclePlate;
    }

    public void setVehiclePlate(String vehiclePlate) {
        this.vehiclePlate = vehiclePlate;
    }

    public String getVehicleNumber() {
        return vehicleNumber;
    }

    public void setVehicleNumber(String vehicleNumber) {
        this.vehicleNumber = vehicleNumber;
    }

    public String getVehicleRuta() {
        return vehicleRuta;
    }

    public void setVehicleRuta(String vehicleRuta) {
        this.vehicleRuta = vehicleRuta;
    }
}
