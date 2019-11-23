package com.example.shakil.androidbarberstaff;

import android.app.AlertDialog;
import android.content.Intent;

import com.google.android.material.navigation.NavigationView;

import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.shakil.androidbarberstaff.Adapter.MyTimeSlotAdapter;
import com.example.shakil.androidbarberstaff.Common.Common;
import com.example.shakil.androidbarberstaff.Common.SpacesItemDecoration;
import com.example.shakil.androidbarberstaff.Interface.INotificationCountListener;
import com.example.shakil.androidbarberstaff.Interface.ITimeSlotLoadListener;
import com.example.shakil.androidbarberstaff.Model.BookingInformation;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import devs.mulham.horizontalcalendar.HorizontalCalendar;
import devs.mulham.horizontalcalendar.HorizontalCalendarView;
import devs.mulham.horizontalcalendar.utils.HorizontalCalendarListener;
import dmax.dialog.SpotsDialog;
import io.paperdb.Paper;

public class StaffHomeActivity extends AppCompatActivity implements ITimeSlotLoadListener, INotificationCountListener {

    @BindView(R.id.activity_main)
    DrawerLayout drawerLayout;

    @BindView(R.id.navigation_view)
    NavigationView navigationView;

    ActionBarDrawerToggle actionBarDrawerToggle;

    DocumentReference barberDoc;
    ITimeSlotLoadListener iTimeSlotLoadListener;
    AlertDialog alertDialog;

    @BindView(R.id.recycler_time_slot)
    RecyclerView recycler_time_slot;

    @BindView(R.id.calendarView)
    HorizontalCalendarView calendarView;

    TextView txt_notification_badge;
    TextView txt_barber_name;

    CollectionReference notificationCollection;
    CollectionReference currentBookDateCollection;

    EventListener<QuerySnapshot> notificationEvent;
    EventListener<QuerySnapshot> bookingEvent;

    ListenerRegistration notificationListener;
    ListenerRegistration bookingRealtimeListener;

    INotificationCountListener iNotificationCountListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_staff_home);
        ButterKnife.bind(this);

        init();

        initView();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (actionBarDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        if (item.getItemId() == R.id.action_new_notification) {
            startActivity(new Intent(StaffHomeActivity.this, NotificationActivity.class));
            txt_notification_badge.setText("");
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initView() {
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout,
                R.string.open, R.string.close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        navigationView.setNavigationItemSelectedListener(menuItem -> {
            if (menuItem.getItemId() == R.id.menu_exit) {
                logout();
            }
            return true;
        });

        View headerView = navigationView.getHeaderView(0);
        txt_barber_name = headerView.findViewById(R.id.txt_barber_name);
        txt_barber_name.setText(Common.currentBarber.getName());

        alertDialog = new SpotsDialog.Builder().setContext(this).setCancelable(false).build();

        //Calender
        Calendar date = Calendar.getInstance();
        date.add(Calendar.DATE, 0);

        loadAvailableTimeSlotOfBarber(Common.currentBarber.getBarberId(), Common.simpleDateFormat.format(date.getTime()));

        recycler_time_slot.setHasFixedSize(true);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 3);
        recycler_time_slot.setLayoutManager(gridLayoutManager);
        recycler_time_slot.addItemDecoration(new SpacesItemDecoration(8));

        //Calender
        Calendar startDate = Calendar.getInstance();
        startDate.add(Calendar.DATE, 0);

        Calendar endDate = Calendar.getInstance();
        endDate.add(Calendar.DATE, 2); // 2 day left

        HorizontalCalendar horizontalCalendar = new HorizontalCalendar.Builder(this, R.id.calendarView)
                .range(startDate, endDate)
                .datesNumberOnScreen(1)
                .mode(HorizontalCalendar.Mode.DAYS)
                .defaultSelectedDate(startDate)
                .configure()
                .end()
                .build();

        horizontalCalendar.setCalendarListener(new HorizontalCalendarListener() {
            @Override
            public void onDateSelected(Calendar date, int position) {
                if (Common.bookingDate.getTimeInMillis() != date.getTimeInMillis()) {
                    Common.bookingDate = date; //This code will not load again if you select new day same with day selected
                    loadAvailableTimeSlotOfBarber(Common.currentBarber.getBarberId(), Common.simpleDateFormat.format(date.getTime()));
                }
            }
        });
    }

    private void logout() {
        //Just delete all remember key and start MainActivity
        Paper.init(this);
        Paper.book().delete(Common.SALON_KEY);
        Paper.book().delete(Common.BARBER_KEY);
        Paper.book().delete(Common.STATE_KEY);
        Paper.book().delete(Common.LOGGED_KEY);

        new AlertDialog.Builder(this).setMessage("Are you sure want to logout ?")
                .setCancelable(false)
                .setPositiveButton("YES", (dialog, which) -> {
                    Intent mainActivity = new Intent(StaffHomeActivity.this, MainActivity.class);
                    mainActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    mainActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(mainActivity);
                    finish();
                }).setNegativeButton("CANCEL", (dialog, which) -> dialog.dismiss()).show();
    }

    private void loadAvailableTimeSlotOfBarber(String barberId, final String bookDate) {
        alertDialog.show();

        //Get Information of this barber
        barberDoc.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot documentSnapshot = task.getResult();

                //if barber available
                if (documentSnapshot.exists()) {
                    //Get information of booking
                    //If not created , return empty

                    CollectionReference date = FirebaseFirestore.getInstance()
                            .collection("AllSalon")
                            .document(Common.state_name)
                            .collection("Branch")
                            .document(Common.selected_salon.getSalonId())
                            .collection("Barber")
                            .document(Common.currentBarber.getBarberId())
                            .collection(bookDate);

                    date.get().addOnCompleteListener(task1 -> {
                        if (task1.isSuccessful()) {
                            QuerySnapshot querySnapshot = task1.getResult();

                            //if don't have any appointment
                            if (querySnapshot.isEmpty()) {
                                iTimeSlotLoadListener.onTimeSlotLoadEmpty();
                            } else {
                                //If have appointment
                                List<BookingInformation> timeSlots = new ArrayList<>();
                                for (QueryDocumentSnapshot document : task1.getResult()) {
                                    timeSlots.add(document.toObject(BookingInformation.class));
                                }
                                iTimeSlotLoadListener.onTimeSlotLoadSuccess(timeSlots);
                            }
                        }
                    }).addOnFailureListener(e -> iTimeSlotLoadListener.onTimeSlotLoadFailed(e.getMessage()));
                }
            }
        });
    }

    private void init() {
        iTimeSlotLoadListener = this;
        iNotificationCountListener = this;
        initNotificationRealtimeUpdate();
        initBookingRealtimeUpdate();
    }

    private void initBookingRealtimeUpdate() {
        barberDoc = FirebaseFirestore.getInstance()
                .collection("AllSalon")
                .document(Common.state_name)
                .collection("Branch")
                .document(Common.selected_salon.getSalonId())
                .collection("Barber")
                .document(Common.currentBarber.getBarberId());

        //Get current date
        final Calendar date = Calendar.getInstance();
        date.add(Calendar.DATE, 0);
        bookingEvent = (queryDocumentSnapshots, e) -> {
            //If have any new booking , update adapter
            loadAvailableTimeSlotOfBarber(Common.currentBarber.getBarberId(), Common.simpleDateFormat.format(date.getTime()));
        };

        currentBookDateCollection = barberDoc.collection(Common.simpleDateFormat.format(date.getTime()));
        bookingRealtimeListener = currentBookDateCollection.addSnapshotListener(bookingEvent);
    }

    private void initNotificationRealtimeUpdate() {
        notificationCollection = FirebaseFirestore.getInstance()
                .collection("AllSalon")
                .document(Common.state_name)
                .collection("Branch")
                .document(Common.selected_salon.getSalonId())
                .collection("Barber")
                .document(Common.currentBarber.getBarberId())
                .collection("Notifications");

        notificationEvent = (queryDocumentSnapshots, e) -> {
            if (queryDocumentSnapshots.size() > 0) {
                loadNotification();
            }
        };

        //Only listen and count all notification
        notificationListener = notificationCollection.whereEqualTo("read", false)
                .addSnapshotListener(notificationEvent);
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this).setMessage("Are you sure want to exit ?")
                .setCancelable(false)
                .setPositiveButton("YES", (dialog, which) -> Toast.makeText(StaffHomeActivity.this, "Fake function exit", Toast.LENGTH_SHORT).show())
                .setNegativeButton("CANCEL", (dialog, which) -> dialog.dismiss()).show();
    }

    @Override
    public void onTimeSlotLoadSuccess(List<BookingInformation> timeSlotList) {
        MyTimeSlotAdapter adapter = new MyTimeSlotAdapter(this, timeSlotList);
        recycler_time_slot.setAdapter(adapter);

        alertDialog.dismiss();
    }

    @Override
    public void onTimeSlotLoadFailed(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        alertDialog.dismiss();
    }

    @Override
    public void onTimeSlotLoadEmpty() {

        MyTimeSlotAdapter adapter = new MyTimeSlotAdapter(this);
        recycler_time_slot.setAdapter(adapter);
        alertDialog.dismiss();
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.staff_home_menu, menu);
        final MenuItem menuItem = menu.findItem(R.id.action_new_notification);

        txt_notification_badge = menuItem.getActionView().findViewById(R.id.notification_badge);

        loadNotification();

        menuItem.getActionView().setOnClickListener(v -> onOptionsItemSelected(menuItem));

        return super.onCreateOptionsMenu(menu);
    }

    private void loadNotification() {
        notificationCollection.whereEqualTo("read", false)
                .get().addOnFailureListener(e -> Toast.makeText(StaffHomeActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show())
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        iNotificationCountListener.onNotificationCountListener(task.getResult().size());
                    }
                });
    }

    @Override
    public void onNotificationCountListener(int count) {
        if (count == 0) {
            txt_notification_badge.setVisibility(View.INVISIBLE);
        } else {
            txt_notification_badge.setVisibility(View.VISIBLE);
            if (count <= 9) {
                txt_notification_badge.setText(String.valueOf(count));
            } else {
                txt_notification_badge.setText("9+");
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        initBookingRealtimeUpdate();
        initNotificationRealtimeUpdate();
    }

    @Override
    protected void onStop() {
        if (notificationListener != null) {
            notificationListener.remove();
        }
        if (bookingRealtimeListener != null) {
            bookingRealtimeListener.remove();
        }
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        if (notificationListener != null) {
            notificationListener.remove();
        }
        if (bookingRealtimeListener != null) {
            bookingRealtimeListener.remove();
        }
        super.onDestroy();
    }
}
