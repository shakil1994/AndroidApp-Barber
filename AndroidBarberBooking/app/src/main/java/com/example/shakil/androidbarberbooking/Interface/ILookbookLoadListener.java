package com.example.shakil.androidbarberbooking.Interface;

import com.example.shakil.androidbarberbooking.Model.Banner;

import java.util.List;

public interface ILookbookLoadListener {
    void onLookbookLoadSuccess(List<Banner> banners);

    void onLookbookLoadFailed(String message);
}
