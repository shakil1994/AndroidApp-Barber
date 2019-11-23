package com.example.shakil.androidbarberbooking.Adapter;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.shakil.androidbarberbooking.Interface.IRecyclerItemSelectedListener;
import com.example.shakil.androidbarberbooking.Model.EventBus.EnableNextButton;
import com.example.shakil.androidbarberbooking.Model.Salon;
import com.example.shakil.androidbarberbooking.R;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

public class MySalonAdapter extends RecyclerView.Adapter<MySalonAdapter.MyViewHolder> {

    Context context;
    List<Salon> salonList;
    List<CardView> cardViewList;

    public MySalonAdapter(Context context, List<Salon> salonList) {
        this.context = context;
        this.salonList = salonList;
        cardViewList = new ArrayList<>();
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.layout_salon, viewGroup, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder myViewHolder, int i) {
        myViewHolder.txt_salon_name.setText(salonList.get(i).getName());
        myViewHolder.txt_salon_address.setText(salonList.get(i).getAddress());
        if (!cardViewList.contains(myViewHolder.card_salon)) {
            cardViewList.add(myViewHolder.card_salon);
        }

        myViewHolder.setiRecyclerItemSelectedListener((view, pos) -> {
            // Set white background for all card not be selected
            for (CardView cardView : cardViewList) {
                cardView.setCardBackgroundColor(context.getResources().getColor(android.R.color.white));
            }

            // Set selected background for only selected item
            myViewHolder.card_salon.setCardBackgroundColor(context.getResources().getColor(android.R.color.holo_orange_dark));

            //Send Broadcast to tell Booking Activity enable Button next
            //EventBus
            EventBus.getDefault().postSticky(new EnableNextButton(1, salonList.get(pos)));
        });
    }

    @Override
    public int getItemCount() {
        return salonList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView txt_salon_name, txt_salon_address;
        CardView card_salon;

        IRecyclerItemSelectedListener iRecyclerItemSelectedListener;

        public void setiRecyclerItemSelectedListener(IRecyclerItemSelectedListener iRecyclerItemSelectedListener) {
            this.iRecyclerItemSelectedListener = iRecyclerItemSelectedListener;
        }

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            txt_salon_name = itemView.findViewById(R.id.txt_salon_name);
            txt_salon_address = itemView.findViewById(R.id.txt_salon_address);
            card_salon = itemView.findViewById(R.id.card_salon);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            iRecyclerItemSelectedListener.onItemSelectedListener(v, getAdapterPosition());
        }
    }
}
