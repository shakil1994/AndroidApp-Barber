package com.example.shakil.androidbarberbooking.Retrofit;

import com.example.shakil.androidbarberbooking.Model.FCMResponse;
import com.example.shakil.androidbarberbooking.Model.FCMSendData;

import io.reactivex.Observable;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface IFCMApi {
    @Headers({
            "Content-Type:application/json",
            "Authorization:key=AAAASEpqGMQ:APA91bHYjzr3cgHw4w5vfjgOlwiIC7fKk0W_mTyAXUXEL8yEAnsPLpN35gF_YDwSh7DRfTGunoc5VVZmH-_U4bhkm-ymAOaeWNsKkjrNHpCLF9Zfw68iV92IyIdkgHN79cRxPt-nGocR"
    })

    @POST("fcm/send")
    Observable<FCMResponse> sendNotification(@Body FCMSendData body);
}
