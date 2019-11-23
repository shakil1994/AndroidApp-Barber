package com.example.shakil.androidbarberstaff.Interface;

import com.example.shakil.androidbarberstaff.Model.Salon;

import java.util.List;

public interface IBranchLoadListener {
    void onBranchLoadSuccess(List<Salon> branchList);

    void onBranchLoadFailed(String message);
}
