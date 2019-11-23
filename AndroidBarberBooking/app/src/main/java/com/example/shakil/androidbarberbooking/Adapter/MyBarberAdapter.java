package com.example.shakil.androidbarberbooking.Adapter;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import com.example.shakil.androidbarberbooking.Interface.IRecyclerItemSelectedListener;
import com.example.shakil.androidbarberbooking.Model.Barber;
import com.example.shakil.androidbarberbooking.Model.EventBus.EnableNextButton;
import com.example.shakil.androidbarberbooking.R;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

public class MyBarberAdapter extends RecyclerView.Adapter<MyBarberAdapter.MyViewHolder> {

    Context context;
    List<Barber> barberList;
    List<CardView> cardViewList;

    public MyBarberAdapter(Context context, List<Barber> barberList) {
        this.context = context;
        this.barberList = barberList;
        cardViewList = new ArrayList<>();
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.layout_barber, viewGroup, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder myViewHolder, final int i) {
        myViewHolder.txt_barber_name.setText(barberList.get(i).getName());
        if (barberList.get(i).getRatingTimes() != null) {
            myViewHolder.ratingBar.setRating(barberList.get(i).getRating().floatValue() / barberList.get(i).getRatingTimes());
        }
        else {
            myViewHolder.ratingBar.setRating(0);
        }
        if (!cardViewList.contains(myViewHolder.card_barber)){
            cardViewList.add(myViewHolder.card_barber);
        }

        myViewHolder.setiRecyclerItemSelectedListener((view, pos) -> {
            //Set background for all item no choice
            for (CardView cardView : cardViewList){
                cardView.setBackgroundColor(context.getResources().getColor(android.R.color.white));
            }
            //Set background for choice
            myViewHolder.card_barber.setBackgroundColor(context.getResources().getColor(android.R.color.holo_orange_dark));

            //Send Local Broadcast to enable button next
            //EventBus
            EventBus.getDefault().postSticky(new EnableNextButton(2, barberList.get(i)));
        });
    }

    @Override
    public int getItemCount() {
        return barberList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView txt_barber_name;
        RatingBar ratingBar;
        CardView card_barber;

        IRecyclerItemSelectedListener iRecyclerItemSelectedListener;

        public void setiRecyclerItemSelectedListener(IRecyclerItemSelectedListener iRecyclerItemSelectedListener) {
            this.iRecyclerItemSelectedListener = iRecyclerItemSelectedListener;
        }

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            txt_barber_name = itemView.findViewById(R.id.txt_barber_name);
            ratingBar = itemView.findViewById(R.id.rtb_barber);
            card_barber = itemView.findViewById(R.id.card_barber);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            iRecyclerItemSelectedListener.onItemSelectedListener(v, getAdapterPosition());
        }
    }
}
