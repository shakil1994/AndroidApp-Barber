package com.example.shakil.androidbarberbooking.Service;

import android.widget.Toast;

import com.example.shakil.androidbarberbooking.Common.Common;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import io.paperdb.Paper;

public class MyFCMService extends FirebaseMessagingService {
    @Override
    public void onNewToken(String s) {
        super.onNewToken(s);

        Common.updateToken(this, s);
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        //dataSend.put("update_done", "true");
        if (remoteMessage.getData() != null) {
            if (remoteMessage.getData().get("update_done") != null) {
                updateLastBooking();

                Map<String, String> dataReceived = remoteMessage.getData();
                Paper.init(this);
                Paper.book().write(Common.RATING_INFORMATION_KEY, new Gson().toJson(dataReceived));
            }

            if (remoteMessage.getData().get(Common.TITLE_KEY) != null && remoteMessage.getData().get(Common.CONTENT_KEY) != null) {
                Common.showNotification(this,
                        new Random().nextInt(),
                        remoteMessage.getData().get(Common.TITLE_KEY),
                        remoteMessage.getData().get(Common.CONTENT_KEY),
                        null);
            }
        }
    }

    private void updateLastBooking() {
        //Here we need get current user login
        //Because are maybe run on background so we need get from paper

        //If app running
        final CollectionReference userBooking;
        if (Common.currentUser != null) {
            userBooking = FirebaseFirestore.getInstance()
                    .collection("User")
                    .document(Common.currentUser.getPhoneNumber())
                    .collection("Booking");
        } else {
            //If app not running
            Paper.init(this);
            String user = Paper.book().read(Common.LOGGED_KEY);

            userBooking = FirebaseFirestore.getInstance()
                    .collection("User")
                    .document(user)
                    .collection("Booking");
        }

        //Check if exists by get current date
        //Why we only work for current date ? Because in my scenario , we only load
        //appointment for current date and next 3 day
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, 0);
        calendar.add(Calendar.HOUR_OF_DAY, 0);
        calendar.add(Calendar.MINUTE, 0);

        Timestamp timestamp = new Timestamp(calendar.getTimeInMillis());

        userBooking
                .whereGreaterThanOrEqualTo("timestamp", timestamp) //Get only booking info with time is today or next
                .whereEqualTo("done", false) // And done field is false (not done service)
                .limit(1)
                .get()
                .addOnFailureListener(e -> Toast.makeText(MyFCMService.this, e.getMessage(), Toast.LENGTH_SHORT).show()).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (task.getResult().size() > 0) {
                            //Update
                            DocumentReference userBookingCurrentDocument = null;
                            for (DocumentSnapshot documentSnapshot : task.getResult()) {
                                userBookingCurrentDocument = userBooking.document(documentSnapshot.getId());
                            }
                            if (userBookingCurrentDocument != null) {
                                Map<String, Object> dataUpdate = new HashMap<>();
                                dataUpdate.put("done", true);
                                userBookingCurrentDocument.update(dataUpdate)
                                        .addOnFailureListener(e -> Toast.makeText(MyFCMService.this, e.getMessage(), Toast.LENGTH_SHORT).show());
                            }
                        }
                    }
                });
    }
}
