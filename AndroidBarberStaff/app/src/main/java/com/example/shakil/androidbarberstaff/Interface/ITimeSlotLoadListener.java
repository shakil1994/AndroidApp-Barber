package com.example.shakil.androidbarberstaff.Interface;

import com.example.shakil.androidbarberstaff.Model.BookingInformation;

import java.util.List;

public interface ITimeSlotLoadListener {
    void onTimeSlotLoadSuccess(List<BookingInformation> timeSlotList);

    void onTimeSlotLoadFailed(String message);

    void onTimeSlotLoadEmpty();
}
