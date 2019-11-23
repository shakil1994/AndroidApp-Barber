package com.example.shakil.androidbarberstaff.Interface;

import com.example.shakil.androidbarberstaff.Model.City;

import java.util.List;

public interface IOnAllStateLoadListener {
    void onAllStateLoadSuccess(List<City> cityList);

    void onAllStateLoadFailed(String message);
}
