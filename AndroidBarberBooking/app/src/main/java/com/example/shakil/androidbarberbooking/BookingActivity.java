package com.example.shakil.androidbarberbooking;

import android.app.AlertDialog;

import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;

import com.example.shakil.androidbarberbooking.Adapter.MyViewPagerAdapter;
import com.example.shakil.androidbarberbooking.Common.Common;
import com.example.shakil.androidbarberbooking.Common.NonSwipeViewPager;
import com.example.shakil.androidbarberbooking.Model.Barber;
import com.example.shakil.androidbarberbooking.Model.EventBus.BarberDoneEvent;
import com.example.shakil.androidbarberbooking.Model.EventBus.ConfirmBookingEvent;
import com.example.shakil.androidbarberbooking.Model.EventBus.DisplayTimeSlotEvent;
import com.example.shakil.androidbarberbooking.Model.EventBus.EnableNextButton;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.shuhart.stepview.StepView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import dmax.dialog.SpotsDialog;

public class BookingActivity extends AppCompatActivity {

    AlertDialog dialog;
    CollectionReference barberRef;

    @BindView(R.id.step_view)
    StepView stepView;

    @BindView(R.id.view_pager)
    NonSwipeViewPager viewPager;

    @BindView(R.id.btn_previous_step)
    Button btn_previous_step;

    @BindView(R.id.btn_next_step)
    Button btn_next_step;

    //Event
    @OnClick(R.id.btn_previous_step)
    void previousStep() {
        if (Common.step == 3 || Common.step > 0) {
            Common.step--; //Decrease
            viewPager.setCurrentItem(Common.step);

            //Always enable NEXT when step < 3
            if (Common.step < 3) {
                btn_next_step.setEnabled(true);
                setColorButton();
            }
        }
    }

    @OnClick(R.id.btn_next_step)
    void nextClick() {
        if (Common.step < 3 || Common.step == 0) {
            Common.step++; //Increase

            //After choose salon
            if (Common.step == 1) {
                if (Common.currentSalon != null) {
                    loadBarberBySalon(Common.currentSalon.getSalonId());
                }
            }
            //Pick time slot
            else if (Common.step == 2) {
                if (Common.currentBarber != null) {
                    loadTimeSlotOfBarber(Common.currentBarber.getBarberId());
                }
            }

            //Confirm
            else if (Common.step == 3) {
                if (Common.currentTimeSlot != -1) {
                    confirmBooking();
                }
            }
            viewPager.setCurrentItem(Common.step);
        }
    }

    private void confirmBooking() {
        //Send broadcast to fragment step four
        EventBus.getDefault().postSticky(new ConfirmBookingEvent(true));
    }

    private void loadTimeSlotOfBarber(String barberId) {
        //Send local Broadcast to Fragment step 3
        EventBus.getDefault().postSticky(new DisplayTimeSlotEvent(true));
    }

    private void loadBarberBySalon(String salonId) {
        dialog.show();

        //Now , select all Barber of Salon
        if (!TextUtils.isEmpty(Common.city)) {
            barberRef = FirebaseFirestore.getInstance()
                    .collection("AllSalon")
                    .document(Common.city)
                    .collection("Branch")
                    .document(salonId)
                    .collection("Barber");

            barberRef.get().addOnCompleteListener(task -> {
                ArrayList<Barber> barbers = new ArrayList<>();
                for (QueryDocumentSnapshot barberSnapShot : task.getResult()) {
                    Barber barber = barberSnapShot.toObject(Barber.class);
                    barber.setPassword(""); //Remove Password because in client app
                    barber.setBarberId(barberSnapShot.getId()); //Get id of Barber

                    barbers.add(barber);
                }

                //Send Broadcast to BookingStep2Fragment to load Recycler
                EventBus.getDefault().postSticky(new BarberDoneEvent(barbers));

                dialog.dismiss();
            }).addOnFailureListener(e -> dialog.dismiss());
        }
    }

    //Event Bus convert
    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void buttonNextReceiver(EnableNextButton event) {
        int step = event.getStep();
        if (step == 1) {
            Common.currentSalon = event.getSalon();
        } else if (step == 2) {
            Common.currentBarber = event.getBarber();
        } else if (step == 3) {
            Common.currentTimeSlot = event.getTimeSlot();
        }

        btn_next_step.setEnabled(true);
        setColorButton();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking);
        ButterKnife.bind(BookingActivity.this);

        dialog = new SpotsDialog.Builder().setContext(this).setCancelable(false).build();

        setupStepView();
        setColorButton();

        //View
        viewPager.setAdapter(new MyViewPagerAdapter(getSupportFragmentManager()));
        viewPager.setOffscreenPageLimit(4); //We have 4 fragment so we need keep state of this 4 screen page
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {

            }

            @Override
            public void onPageSelected(int i) {

                //Show Step
                stepView.go(i, true);
                if (i == 0) {
                    btn_previous_step.setEnabled(false);
                } else {
                    btn_previous_step.setEnabled(true);
                }

                //Set disable button next here
                btn_next_step.setEnabled(false);
                setColorButton();
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });
    }

    private void setColorButton() {
        if (btn_next_step.isEnabled()) {
            btn_next_step.setBackgroundResource(R.color.colorButton);
        } else {
            btn_next_step.setBackgroundResource(android.R.color.darker_gray);
        }

        if (btn_previous_step.isEnabled()) {
            btn_previous_step.setBackgroundResource(R.color.colorButton);
        } else {
            btn_previous_step.setBackgroundResource(android.R.color.darker_gray);
        }
    }

    private void setupStepView() {
        List<String> stepList = new ArrayList<>();
        stepList.add("Salon");
        stepList.add("Barber");
        stepList.add("Time");
        stepList.add("Confirm");
        stepView.setSteps(stepList);
    }

    /*=======================================================*/
    /*EVENT BUS*/

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    /*=======================================================*/
}
