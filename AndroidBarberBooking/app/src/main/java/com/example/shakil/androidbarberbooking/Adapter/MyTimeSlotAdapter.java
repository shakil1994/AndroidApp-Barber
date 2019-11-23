package com.example.shakil.androidbarberbooking.Adapter;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.shakil.androidbarberbooking.Common.Common;
import com.example.shakil.androidbarberbooking.Interface.IRecyclerItemSelectedListener;
import com.example.shakil.androidbarberbooking.Model.EventBus.EnableNextButton;
import com.example.shakil.androidbarberbooking.Model.TimeSlot;
import com.example.shakil.androidbarberbooking.R;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

public class MyTimeSlotAdapter extends RecyclerView.Adapter<MyTimeSlotAdapter.MyViewHolder> {

    Context context;
    List<TimeSlot> timeSlotList;
    List<CardView> cardViewList;

    public MyTimeSlotAdapter(Context context) {
        this.context = context;
        this.timeSlotList = new ArrayList<>();
        cardViewList = new ArrayList<>();
    }

    public MyTimeSlotAdapter(Context context, List<TimeSlot> timeSlotList) {
        this.context = context;
        this.timeSlotList = timeSlotList;
        cardViewList = new ArrayList<>();
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

            //If all time slot empty , all card is enable
            myViewHolder.card_time_slot.setEnabled(true);

            myViewHolder.card_time_slot.setCardBackgroundColor(context.getResources().getColor(android.R.color.white));
            myViewHolder.txt_time_slot_description.setText("Available");
            myViewHolder.txt_time_slot_description.setTextColor(context.getResources().getColor(android.R.color.black));
            myViewHolder.txt_time_slot.setTextColor(context.getResources().getColor(android.R.color.black));
        }

        //If have position is full
        else {
            for (TimeSlot slotValue : timeSlotList) {
                //Loop all time slot from server and set different color
                int slot = Integer.parseInt(slotValue.getSlot().toString());

                //If slot == Position
                if (slot == i) {

                    // We will set tag for all time slot is full
                    //So base on tag , we can set all remain card background without change full time slot
                    myViewHolder.card_time_slot.setEnabled(false);

                    myViewHolder.card_time_slot.setTag(Common.DISABLE_TAG);

                    myViewHolder.card_time_slot.setCardBackgroundColor(context.getResources().getColor(android.R.color.darker_gray));

                    myViewHolder.txt_time_slot_description.setText("Unavailable");
                    myViewHolder.txt_time_slot_description.setTextColor(context.getResources().getColor(android.R.color.white));
                    myViewHolder.txt_time_slot.setTextColor(context.getResources().getColor(android.R.color.white));
                }
            }
        }

        // Add all card to list (20 card because we have 20 time slot)
        //No add card already in CardViewList
        if (!cardViewList.contains(myViewHolder.card_time_slot)) {
            cardViewList.add(myViewHolder.card_time_slot);
        }

        //Check if card time slot is available
        myViewHolder.setiRecyclerItemSelectedListener((view, pos) -> {
            //Loop all card in card List
            for (CardView cardView : cardViewList) {
                //Only available card time slot be change
                if (cardView.getTag() == null) {
                    cardView.setCardBackgroundColor(context.getResources().getColor(android.R.color.white));
                }
            }

            //Our selected card will be change color
            myViewHolder.card_time_slot.setCardBackgroundColor(context.getResources().getColor(android.R.color.holo_orange_dark));

            //After that , send broadcast to enable button NEXT
            //put index of time slot we have selected
            //Go to step 3
            EventBus.getDefault().postSticky(new EnableNextButton(3, i));
        });
    }

    @Override
    public int getItemCount() {
        return Common.TIME_SLOT_TOTAL;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView txt_time_slot, txt_time_slot_description;
        CardView card_time_slot;

        IRecyclerItemSelectedListener iRecyclerItemSelectedListener;

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
