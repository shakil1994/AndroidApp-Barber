package com.example.shakil.androidbarberstaff.Adapter;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.shakil.androidbarberstaff.Common.Common;
import com.example.shakil.androidbarberstaff.DoneServiceActivity;
import com.example.shakil.androidbarberstaff.Interface.IRecyclerItemSelectedListener;
import com.example.shakil.androidbarberstaff.Model.BookingInformation;
import com.example.shakil.androidbarberstaff.R;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class MyTimeSlotAdapter extends RecyclerView.Adapter<MyTimeSlotAdapter.MyViewHolder> {

    Context context;
    List<BookingInformation> timeSlotList;
    List<CardView> itemViewList;

    public MyTimeSlotAdapter(Context context) {
        this.context = context;
        this.timeSlotList = new ArrayList<>();
        itemViewList = new ArrayList<>();
    }

    public MyTimeSlotAdapter(Context context, List<BookingInformation> timeSlotList) {
        this.context = context;
        this.timeSlotList = timeSlotList;
        itemViewList = new ArrayList<>();
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.layout_time_slot, viewGroup, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder myViewHolder, final int i) {
        myViewHolder.txt_time_slot.setText(new StringBuilder(Common.convertTimeSlotToString(i)).toString());

        //If all position is available , just show list
        if (timeSlotList.size() == 0) {
            myViewHolder.card_time_slot.setCardBackgroundColor(context.getResources().getColor(android.R.color.white));
            myViewHolder.txt_time_slot_description.setText(new StringBuilder("Available"));
            myViewHolder.txt_time_slot_description.setTextColor(context.getResources().getColor(android.R.color.black));
            myViewHolder.txt_time_slot.setTextColor(context.getResources().getColor(android.R.color.black));

            //Add Event Nothing
            myViewHolder.setiRecyclerItemSelectedListener((view, position) -> {
                //Fix crash if we not add this function
            });
        }

        //If have position is full
        else {
            for (final BookingInformation slotValue : timeSlotList) {
                //Loop all time slot from server and set different color
                int slot = Integer.parseInt(slotValue.getSlot().toString());

                //If slot == Position
                if (slot == i) {

                    if (!slotValue.isDone()) {
                        // We will set tag for all time slot is full
                        //So base on tag , we can set all remain card background without change full time slot
                        myViewHolder.card_time_slot.setTag(Common.DISABLE_TAG);
                        myViewHolder.card_time_slot.setCardBackgroundColor(context.getResources().getColor(android.R.color.darker_gray));

                        myViewHolder.txt_time_slot_description.setText("Unavailable");
                        myViewHolder.txt_time_slot_description.setTextColor(context.getResources().getColor(android.R.color.white));
                        myViewHolder.txt_time_slot.setTextColor(context.getResources().getColor(android.R.color.white));
                        //myViewHolder.card_time_slot.setEnabled(false);

                        myViewHolder.setiRecyclerItemSelectedListener((view, position) -> {
                            //Only add for gray time slot
                            //Here we will get Booking Information and store in Common.currentBookingInformation
                            //After that , start DoneServiceActivity
                            FirebaseFirestore.getInstance()
                                    .collection("AllSalon")
                                    .document(Common.state_name)
                                    .collection("Branch")
                                    .document(Common.selected_salon.getSalonId())
                                    .collection("Barber")
                                    .document(Common.currentBarber.getBarberId())
                                    .collection(Common.simpleDateFormat.format(Common.bookingDate.getTime()))
                                    .document(slotValue.getSlot().toString())
                                    .get()
                                    .addOnFailureListener(e -> Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show())
                                    .addOnCompleteListener(task -> {
                                        if (task.isSuccessful()) {
                                            if (task.getResult().exists()) {
                                                Common.currentBookingInformation = task.getResult().toObject(BookingInformation.class);
                                                Common.currentBookingInformation.setBookingId(task.getResult().getId());
                                                context.startActivity(new Intent(context, DoneServiceActivity.class));
                                            }
                                        }
                                    });
                        });
                    } else {
                        //If service is done
                        myViewHolder.card_time_slot.setTag(Common.DISABLE_TAG);
                        myViewHolder.card_time_slot.setCardBackgroundColor(context.getResources().getColor(android.R.color.holo_orange_dark));

                        myViewHolder.txt_time_slot_description.setText("Done");
                        myViewHolder.txt_time_slot_description.setTextColor(context.getResources().getColor(android.R.color.white));
                        myViewHolder.txt_time_slot.setTextColor(context.getResources().getColor(android.R.color.white));

                        myViewHolder.setiRecyclerItemSelectedListener((view, position) -> {
                            //Add here to fix crash
                        });
                    }
                } else {
                    //Fix crash
                    if (myViewHolder.getiRecyclerItemSelectedListener() == null) {

                        //We only add event for view holder which is not implement click
                        //Because if we don't put this if condition
                        //All time slot with slot value higher current time slot will be override event

                        myViewHolder.setiRecyclerItemSelectedListener((view, position) -> {

                        });
                    }
                }
            }
        }

        // Add all card to list (20 card because we have 20 time slot)
        //No add card already in CardViewList
        if (!itemViewList.contains(myViewHolder.card_time_slot)) {
            itemViewList.add(myViewHolder.card_time_slot);
        }
    }

    @Override
    public int getItemCount() {
        return Common.TIME_SLOT_TOTAL;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView txt_time_slot, txt_time_slot_description;
        CardView card_time_slot;

        IRecyclerItemSelectedListener iRecyclerItemSelectedListener;

        public IRecyclerItemSelectedListener getiRecyclerItemSelectedListener() {
            return iRecyclerItemSelectedListener;
        }

        public void setiRecyclerItemSelectedListener(IRecyclerItemSelectedListener iRecyclerItemSelectedListener) {
            this.iRecyclerItemSelectedListener = iRecyclerItemSelectedListener;
        }

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            txt_time_slot = itemView.findViewById(R.id.txt_time_slot);
            txt_time_slot_description = itemView.findViewById(R.id.txt_time_slot_description);
            card_time_slot = itemView.findViewById(R.id.card_time_slot);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            iRecyclerItemSelectedListener.onItemSelectedListener(v, getAdapterPosition());
        }
    }
}
