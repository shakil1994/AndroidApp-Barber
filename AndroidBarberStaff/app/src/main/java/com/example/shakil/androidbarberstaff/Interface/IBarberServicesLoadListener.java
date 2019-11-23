package com.example.shakil.androidbarberstaff.Interface;

import com.example.shakil.androidbarberstaff.Model.BarberServices;

import java.util.List;

public interface IBarberServicesLoadListener {
    void onBarberServicesLoadSuccess(List<BarberServices> barberServicesList);

    void onBarberServicesLoadFailed(String message);
}
