package com.example.shakil.androidbarberstaff.Interface;

import com.example.shakil.androidbarberstaff.Model.MyNotification;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.List;

public interface INotificationLoadListener {
    void onNotificationLoadSuccess(List<MyNotification> myNotificationList, DocumentSnapshot lastDocument);

    void onNotificationFailed(String message);
}
