package com.example.shakil.androidbarberstaff.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import com.example.shakil.androidbarberstaff.Common.CustomLoginDialog;
import com.example.shakil.androidbarberstaff.Interface.IDialogClickListener;
import com.example.shakil.androidbarberstaff.Interface.IGetBarberListener;
import com.example.shakil.androidbarberstaff.Interface.IRecyclerItemSelectedListener;
import com.example.shakil.androidbarberstaff.Interface.IUserLoginRememberListener;
import com.example.shakil.androidbarberstaff.Model.Barber;
import com.example.shakil.androidbarberstaff.Model.Salon;
import com.example.shakil.androidbarberstaff.R;
import com.example.shakil.androidbarberstaff.StaffHomeActivity;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

import dmax.dialog.SpotsDialog;

public class MySalonAdapter extends RecyclerView.Adapter<MySalonAdapter.MyViewHolder> implements IDialogClickListener {

    Context context;
    List<Salon> salonList;
    List<CardView> itemViewList;
    IUserLoginRememberListener iUserLoginRememberListener;
    IGetBarberListener iGetBarberListener;

    public MySalonAdapter(Context context, List<Salon> salonList, IUserLoginRememberListener iUserLoginRememberListener, IGetBarberListener iGetBarberListener) {
        this.context = context;
        this.salonList = salonList;
        itemViewList = new ArrayList<>();
        this.iUserLoginRememberListener = iUserLoginRememberListener;
        this.iGetBarberListener = iGetBarberListener;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.layout_salon, viewGroup, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder myViewHolder, int i) {
        myViewHolder.txt_salon_name.setText(salonList.get(i).getName());
        myViewHolder.txt_salon_address.setText(salonList.get(i).getAddress());
        if (!itemViewList.contains(myViewHolder.card_salon)) {
            itemViewList.add(myViewHolder.card_salon);
        }

        myViewHolder.setiRecyclerItemSelectedListener((view, positive) -> {
            Common.selected_salon = salonList.get(positive);

            showLoginDialog();
        });
    }

    private void showLoginDialog() {
        CustomLoginDialog.getInstance().showLoginDialog("STAFF LOGIN",
                "LOGIN",
                "CANCEL",
                context,
                this);
    }

    @Override
    public int getItemCount() {
        return salonList.size();
    }

    @Override
    public void onClickPositiveButton(final DialogInterface dialogInterface, final String userName, String password) {
        //Show loading Dialog
        final AlertDialog loading = new SpotsDialog.Builder().setCancelable(false).setContext(context).build();

        loading.show();

        FirebaseFirestore.getInstance()
                .collection("AllSalon")
                .document(Common.state_name)
                .collection("Branch")
                .document(Common.selected_salon.getSalonId())
                .collection("Barber")
                .whereEqualTo("username", userName)
                .whereEqualTo("password", password)
                .limit(1)
                .get()
                .addOnFailureListener(e -> {
                    Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                    loading.dismiss();
                }).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (task.getResult().size() > 0) {
                            dialogInterface.dismiss();
                            loading.dismiss();

                            iUserLoginRememberListener.onUserLoginSuccess(userName);

                            //Create Barber
                            Barber barber = new Barber();
                            for (DocumentSnapshot barberSnapShot : task.getResult()) {
                                barber = barberSnapShot.toObject(Barber.class);
                                barber.setBarberId(barberSnapShot.getId());
                            }
                            iGetBarberListener.onGetBarberSuccess(barber);

                            //We will navigate Staff Home and clear all previous activity
                            Intent staffHome = new Intent(context, StaffHomeActivity.class);
                            staffHome.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            staffHome.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            context.startActivity(staffHome);
                        } else {
                            loading.dismiss();
                            Toast.makeText(context, "Wrong username / password or wrong salon", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    @Override
    public void onClickNegativeButton(DialogInterface dialogInterface) {
        dialogInterface.dismiss();
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
