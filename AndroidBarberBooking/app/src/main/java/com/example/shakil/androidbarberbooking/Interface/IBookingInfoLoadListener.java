package com.example.shakil.androidbarberbooking.Interface;

import com.example.shakil.androidbarberbooking.Model.BookingInformation;

public interface IBookingInfoLoadListener {
    void onBookingInfoLoadEmpty();

    void onBookingInfoLoadSuccess(BookingInformation bookingInformation, String documentId);

    void onBookingInfoLoadFailed(String message);
}
