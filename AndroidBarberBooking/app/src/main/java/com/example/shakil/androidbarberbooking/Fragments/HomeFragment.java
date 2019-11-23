package com.example.shakil.androidbarberbooking.Fragments;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.shakil.androidbarberbooking.Adapter.HomeSliderAdapter;
import com.example.shakil.androidbarberbooking.Adapter.LookbookAdapter;
import com.example.shakil.androidbarberbooking.BookingActivity;
import com.example.shakil.androidbarberbooking.CartActivity;
import com.example.shakil.androidbarberbooking.Common.Common;
import com.example.shakil.androidbarberbooking.Database.CartDataSource;
import com.example.shakil.androidbarberbooking.Database.CartDatabase;
import com.example.shakil.androidbarberbooking.Database.LocalCartDataSource;
import com.example.shakil.androidbarberbooking.HistoryActivity;
import com.example.shakil.androidbarberbooking.Interface.IBannerLoadListener;
import com.example.shakil.androidbarberbooking.Interface.IBookingInfoLoadListener;
import com.example.shakil.androidbarberbooking.Interface.IBookingInformationChangeListener;
import com.example.shakil.androidbarberbooking.Interface.ILookbookLoadListener;
import com.example.shakil.androidbarberbooking.MainActivity;
import com.example.shakil.androidbarberbooking.Model.Banner;
import com.example.shakil.androidbarberbooking.Model.BookingInformation;
import com.example.shakil.androidbarberbooking.R;
import com.example.shakil.androidbarberbooking.Service.PicassoImageLoadingService;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.nex3z.notificationbadge.NotificationBadge;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import dmax.dialog.SpotsDialog;
import io.paperdb.Paper;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import ss.com.bannerslider.Slider;

public class HomeFragment extends Fragment implements ILookbookLoadListener, IBannerLoadListener, IBookingInfoLoadListener, IBookingInformationChangeListener {

    private Unbinder unbinder;
    AlertDialog dialog;
    CartDataSource cartDataSource;

    @BindView(R.id.notification_badge)
    NotificationBadge notificationBadge;

    @BindView(R.id.layout_user_information)
    LinearLayout layout_user_information;

    @BindView(R.id.txt_user_name)
    TextView txt_user_name;

    @BindView(R.id.banner_slider)
    Slider banner_slider;

    @BindView(R.id.recycler_look_book)
    RecyclerView recycler_look_book;

    @BindView(R.id.card_booking_info)
    CardView card_booking_info;

    @BindView(R.id.txt_salon_address)
    TextView txt_salon_address;

    @BindView(R.id.txt_salon_barber)
    TextView txt_salon_barber;

    @BindView(R.id.txt_time)
    TextView txt_time;

    @BindView(R.id.txt_time_remain)
    TextView txt_time_remain;

    @OnClick(R.id.layout_user_information)
    void logoutDislog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Sign out?")
                .setMessage("Please confirm you really want to sign out?")
                .setNegativeButton("CANCEL", (dialog1, which) -> {
                    dialog1.dismiss();
                }).setPositiveButton("OK", (dialog1, which) -> {
                    Common.currentBarber = null;
                    Common.currentBooking = null;
                    Common.currentSalon = null;
                    Common.currentTimeSlot = -1;
                    Common.currentBookingId = "";
                    Common.currentUser = null;

                    FirebaseAuth.getInstance().signOut();

                    Intent intent = new Intent(getContext(), MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    getActivity().finish();
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @OnClick(R.id.card_view_cart)
    void openCartActivity() {
        startActivity(new Intent(getActivity(), CartActivity.class));
    }

    @OnClick(R.id.card_view_history)
    void openHistoryActivity() {
        startActivity(new Intent(getActivity(), HistoryActivity.class));
    }

    @OnClick(R.id.btn_change_booking)
    void changeBooking() {
        changeBookingFromUser();
    }

    private void changeBookingFromUser() {
        //Show dialog Confirm
        androidx.appcompat.app.AlertDialog.Builder confirmDialog = new androidx.appcompat.app.AlertDialog.Builder(getActivity())
                .setCancelable(false)
                .setTitle("Hey!")
                .setMessage("Do you really want to change booking information?\nBecause we will delete your old booking information\nJust confirm")
                .setNegativeButton("CANCEL", (dialog, which) -> dialog.dismiss()).setPositiveButton("OK", (dialog, which) -> {
                    deleteBookingFromBarber(true); //True because we call from button change

                });

        confirmDialog.show();
    }

    @OnClick(R.id.btn_delete_booking)
    void deleteBooking() {
        deleteBookingFromBarber(false);
    }

    private void deleteBookingFromBarber(final boolean isChange) {
        /*To delete booking , first we need delete from Barber collections
         * After that , we will delete from user booking collections
         * And final , delete event
         */

        // We need Load Common.currentBooking because we need same data from BookingInformation
        if (Common.currentBooking != null) {

            dialog.show();

            //Get booking information in barber object
            DocumentReference barberBookingInfo = FirebaseFirestore.getInstance()
                    .collection("AllSalon")
                    .document(Common.currentBooking.getCityBook())
                    .collection("Branch")
                    .document(Common.currentBooking.getSalonId())
                    .collection("Barber")
                    .document(Common.currentBooking.getBarberId())
                    .collection(Common.convertTimeStampToStringKey(Common.currentBooking.getTimestamp()))
                    .document(Common.currentBooking.getSlot().toString());

            //When we have document , just delete it
            barberBookingInfo.delete().addOnFailureListener(e -> Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show()).addOnSuccessListener(aVoid -> {
                //After delete on Barber done
                //We will start delete from User
                deleteBookingFromUser(isChange);
            });
        } else {
            Toast.makeText(getContext(), "Current Booking must not be null!", Toast.LENGTH_SHORT).show();
        }
    }

    private void deleteBookingFromUser(final boolean isChange) {
        //First , we need get information from user object
        if (!TextUtils.isEmpty(Common.currentBookingId)) {
            DocumentReference userBookingInfo = FirebaseFirestore.getInstance()
                    .collection("User")
                    .document(Common.currentUser.getPhoneNumber())
                    .collection("Booking")
                    .document(Common.currentBookingId);

            //Delete
            userBookingInfo.delete().addOnFailureListener(e -> Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show()).addOnSuccessListener(aVoid -> {
                //After delete from "User" , just delete from Calendar
                //First , we need get save Uri of event we just add
                Paper.init(getActivity());
                if (Paper.book().read(Common.EVENT_URI_CACHE) != null) {
                    String eventString = Paper.book().read(Common.EVENT_URI_CACHE).toString();
                    Uri eventUri = null;
                    if (eventString != null && !TextUtils.isEmpty(eventString)) {
                        eventUri = Uri.parse(eventString);
                    }
                    if (eventUri != null) {
                        getActivity().getContentResolver().delete(eventUri, null, null);
                    }
                }

                Toast.makeText(getActivity(), "Success delete booking!", Toast.LENGTH_SHORT).show();

                //Refresh
                loadUserBooking();

                //Check it isChange -> Call change button , we will fired interface
                if (isChange) {
                    iBookingInformationChangeListener.onBookingInformationChange();
                }

                dialog.dismiss();
            });
        } else {
            dialog.dismiss();
            Toast.makeText(getContext(), "Booking information ID must not be empty", Toast.LENGTH_SHORT).show();
        }
    }

    @OnClick(R.id.card_view_booking)
    void booking() {
        startActivity(new Intent(getActivity(), BookingActivity.class));
    }

    //FireStore
    CollectionReference bannerRef, lookbookRef;

    //Interface
    IBannerLoadListener iBannerLoadListener;
    ILookbookLoadListener iLookbookLoadListener;
    IBookingInfoLoadListener iBookingInfoLoadListener;
    IBookingInformationChangeListener iBookingInformationChangeListener;

    ListenerRegistration userBookingListener = null;
    EventListener<QuerySnapshot> userBookingEvent = null;

    public HomeFragment() {
        bannerRef = FirebaseFirestore.getInstance().collection("Banner");
        lookbookRef = FirebaseFirestore.getInstance().collection("Lookbook");
    }

    @Override
    public void onResume() {
        super.onResume();
        loadUserBooking();
        countCartItem();
    }

    private void loadUserBooking() {
        CollectionReference userBooking = FirebaseFirestore.getInstance()
                .collection("User")
                .document(Common.currentUser.getPhoneNumber())
                .collection("Booking");

        //Get Current Date
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, 0);
        calendar.add(Calendar.HOUR_OF_DAY, 0);
        calendar.add(Calendar.MINUTE, 0);

        Timestamp toDayTimeStamp = new Timestamp(calendar.getTime());

        //Select booking information from Firebase with done = false and timestamp greater today
        userBooking.whereGreaterThanOrEqualTo("timestamp", toDayTimeStamp)
                .whereEqualTo("done", false)
                .limit(1)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (!task.getResult().isEmpty()) {
                            for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                                BookingInformation bookingInformation = queryDocumentSnapshot.toObject(BookingInformation.class);
                                iBookingInfoLoadListener.onBookingInfoLoadSuccess(bookingInformation, queryDocumentSnapshot.getId());
                                break; //Exit loop as soon as
                            }
                        } else {
                            iBookingInfoLoadListener.onBookingInfoLoadEmpty();
                        }
                    }
                }).addOnFailureListener(e -> iBookingInfoLoadListener.onBookingInfoLoadFailed(e.getMessage()));

        //Here , after userBooking has been assign data (collections)
        //We will make realtime listen here

        //If userBookingEvent already init
        if (userBookingEvent != null) {

            // Only add if userBookingListener == null
            if (userBookingListener == null) {

                //That mean we just add 1 time
                userBookingListener = userBooking.addSnapshotListener(userBookingEvent);
            }
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        dialog = new SpotsDialog.Builder().setContext(getContext()).setCancelable(false).build();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        unbinder = ButterKnife.bind(this, view);

        cartDataSource = new LocalCartDataSource(CartDatabase.getInstance(getContext()).cartDAO());

        //Init
        Slider.init(new PicassoImageLoadingService());
        iBannerLoadListener = this;
        iLookbookLoadListener = this;
        iBookingInfoLoadListener = this;
        iBookingInformationChangeListener = this;

        //Check is logged ?
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null){
            setUserInformation();
            loadBanner();
            loadLookBook();
            initRealtimeUserBooking(); // Need declare above loadUserBooking();
            loadUserBooking();
            countCartItem();
        }
        return view;
    }

    private void initRealtimeUserBooking() {
        //Warning : Please follow this step is carefully
        //Because it this step you do wrong , this will make
        //infinity loop is your app , and you will retrieve QUOTA LIMIT from firestore
        //If you do wrong , FireStore will get reach limit and you need wait for next 24 hours for reset
        //I've this bug for two days :( small bug but take me a lot of time to wait :(

        // We only init event if event is null
        if (userBookingEvent == null) {
            userBookingEvent = (queryDocumentSnapshots, e) -> {
                //In this event , when it fired , we will call loadUserBooking again
                //To reload all booking information
                loadUserBooking();
            };
        }
    }

    private void countCartItem() {
        cartDataSource.countItemInCart(Common.currentUser.getPhoneNumber())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<Integer>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onSuccess(Integer integer) {
                        notificationBadge.setText(String.valueOf(integer));
                    }

                    @Override
                    public void onError(Throwable e) {
                        Toast.makeText(getContext(), "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadLookBook() {
        lookbookRef.get().addOnCompleteListener(task -> {
            List<Banner> lookbooks = new ArrayList<>();
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot bannerSnapShot : task.getResult()) {
                    Banner banner = bannerSnapShot.toObject(Banner.class);
                    lookbooks.add(banner);
                }
                iLookbookLoadListener.onLookbookLoadSuccess(lookbooks);
            }
        }).addOnFailureListener(e -> iLookbookLoadListener.onLookbookLoadFailed(e.getMessage()));
    }

    private void loadBanner() {
        bannerRef.get().addOnCompleteListener(task -> {
            List<Banner> banners = new ArrayList<>();
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot bannerSnapShot : task.getResult()) {
                    Banner banner = bannerSnapShot.toObject(Banner.class);
                    banners.add(banner);
                }
                iBannerLoadListener.onBannerLoadSuccess(banners);
            }
        }).addOnFailureListener(e -> iBannerLoadListener.onBannerLoadFailed(e.getMessage()));
    }

    private void setUserInformation() {
        layout_user_information.setVisibility(View.VISIBLE);
        txt_user_name.setText(Common.currentUser.getName());
    }

    @Override
    public void onLookbookLoadSuccess(List<Banner> banners) {
        recycler_look_book.setHasFixedSize(true);
        recycler_look_book.setLayoutManager(new LinearLayoutManager(getActivity()));
        recycler_look_book.setAdapter(new LookbookAdapter(getActivity(), banners));
    }

    @Override
    public void onLookbookLoadFailed(String message) {
        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBannerLoadSuccess(List<Banner> banners) {
        banner_slider.setAdapter(new HomeSliderAdapter(banners));
    }

    @Override
    public void onBannerLoadFailed(String message) {
        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBookingInfoLoadEmpty() {
        card_booking_info.setVisibility(View.GONE);
    }

    @Override
    public void onBookingInfoLoadSuccess(BookingInformation bookingInformation, String bookingId) {

        Common.currentBooking = bookingInformation;
        Common.currentBookingId = bookingId;

        txt_salon_address.setText(bookingInformation.getSalonAddress());
        txt_salon_barber.setText(bookingInformation.getBarberName());
        txt_time.setText(bookingInformation.getTime());
        String dateRemain = DateUtils.getRelativeTimeSpanString(
                Long.valueOf(bookingInformation.getTimestamp().toDate().getTime()),
                Calendar.getInstance().getTimeInMillis(), 0).toString();
        txt_time_remain.setText(dateRemain);

        card_booking_info.setVisibility(View.VISIBLE);

        dialog.dismiss();
    }

    @Override
    public void onBookingInfoLoadFailed(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBookingInformationChange() {
        //Here we will just start activity booking
        startActivity(new Intent(getActivity(), BookingActivity.class));
    }

    @Override
    public void onDestroy() {
        if (userBookingListener != null) {
            userBookingListener.remove();
        }
        super.onDestroy();
    }
}
