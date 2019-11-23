package com.example.shakil.androidbarberbooking.Interface;

import com.example.shakil.androidbarberbooking.Model.Banner;

import java.util.List;

public interface IBannerLoadListener {
    void onBannerLoadSuccess(List<Banner> banners);

    void onBannerLoadFailed(String message);
}
