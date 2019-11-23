package com.example.shakil.androidbarberbooking.Interface;

import com.example.shakil.androidbarberbooking.Model.Salon;

import java.util.List;

public interface IBranchLoadListener {
    void onBranchLoadSuccess(List<Salon> salonList);

    void onBranchLoadFailed(String message);
}
