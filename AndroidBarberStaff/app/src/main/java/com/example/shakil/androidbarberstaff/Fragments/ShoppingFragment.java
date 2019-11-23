package com.example.shakil.androidbarberstaff.Fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.shakil.androidbarberstaff.Adapter.MyShoppingItemAdapter;
import com.example.shakil.androidbarberstaff.Common.SpacesItemDecoration;
import com.example.shakil.androidbarberstaff.Interface.IOnShoppingItemSelected;
import com.example.shakil.androidbarberstaff.Interface.IShoppingDataLoadListener;
import com.example.shakil.androidbarberstaff.Model.ShoppingItem;
import com.example.shakil.androidbarberstaff.R;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class ShoppingFragment extends BottomSheetDialogFragment implements IShoppingDataLoadListener, IOnShoppingItemSelected {

    Unbinder unbinder;
    IOnShoppingItemSelected callBackToActivity;

    IShoppingDataLoadListener iShoppingDataLoadListener;

    CollectionReference shoppingItemRef;

    @BindView(R.id.chip_group)
    ChipGroup chipGroup;

    @BindView(R.id.recycler_items)
    RecyclerView recycler_items;

    @BindView(R.id.chip_wax)
    Chip chip_wax;

    @OnClick(R.id.chip_wax)
    void waxLoadClick() {
        setSelectedChip(chip_wax);
        loadShoppingItem("Wax");
    }

    @BindView(R.id.chip_spray)
    Chip chip_spray;

    @OnClick(R.id.chip_spray)
    void sprayLoadClick() {
        setSelectedChip(chip_spray);
        loadShoppingItem("Spray");
    }

    @BindView(R.id.chip_body_care)
    Chip chip_body_care;

    @OnClick(R.id.chip_body_care)
    void bodyCareLoadClick() {
        setSelectedChip(chip_body_care);
        loadShoppingItem("BodyCare");
    }

    @BindView(R.id.chip_hair_care)
    Chip chip_hair_care;

    @OnClick(R.id.chip_hair_care)
    void hairCareLoadClick() {
        setSelectedChip(chip_hair_care);
        loadShoppingItem("HairCare");
    }

    @SuppressLint("ValidFragment")
    public ShoppingFragment(IOnShoppingItemSelected callBackToActivity) {
        this.callBackToActivity = callBackToActivity;
    }

    private static ShoppingFragment instance;

    public static ShoppingFragment getInstance(IOnShoppingItemSelected iOnShoppingItemSelected) {
        return instance == null ? new ShoppingFragment(iOnShoppingItemSelected) : instance;
    }

    private void loadShoppingItem(String itemMenu) {
        shoppingItemRef = FirebaseFirestore.getInstance().collection("Shopping")
                .document(itemMenu).collection("Items");

        //Get Data
        shoppingItemRef.get().addOnFailureListener(e -> iShoppingDataLoadListener.onShoppingDataLoadFailed(e.getMessage()))
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<ShoppingItem> shoppingItems = new ArrayList<>();
                        for (DocumentSnapshot itemSnapShot : task.getResult()) {
                            ShoppingItem shoppingItem = itemSnapShot.toObject(ShoppingItem.class);
                            shoppingItem.setId(itemSnapShot.getId()); //Remember add it if you don't want to get null reference
                            shoppingItems.add(shoppingItem);
                        }
                        iShoppingDataLoadListener.onShoppingDataLoadSuccess(shoppingItems);
                    }
                });
    }

    private void setSelectedChip(Chip chip) {
        //Set Color
        for (int i = 0; i < chipGroup.getChildCount(); i++) {
            Chip chipItem = (Chip) chipGroup.getChildAt(i);
            //If not selected
            if (chipItem.getId() != chip.getId()) {
                chipItem.setChipBackgroundColorResource(android.R.color.darker_gray);
                chipItem.setTextColor(getResources().getColor(android.R.color.white));
            }
            //If selected
            else {
                chipItem.setChipBackgroundColorResource(android.R.color.holo_orange_dark);
                chipItem.setTextColor(getResources().getColor(android.R.color.black));
            }
        }
    }

    public ShoppingFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View itemView = inflater.inflate(R.layout.fragment_shopping, container, false);
        unbinder = ButterKnife.bind(this, itemView);

        //Default load
        loadShoppingItem("Wax");

        init();

        initView();

        return itemView;
    }

    private void initView() {
        recycler_items.setHasFixedSize(true);
        recycler_items.setLayoutManager(new GridLayoutManager(getContext(), 2));
        recycler_items.addItemDecoration(new SpacesItemDecoration(8));
    }

    private void init() {
        iShoppingDataLoadListener = this;
    }

    @Override
    public void onShoppingDataLoadSuccess(List<ShoppingItem> shoppingItemList) {
        MyShoppingItemAdapter adapter = new MyShoppingItemAdapter(getContext(), shoppingItemList, this);
        recycler_items.setAdapter(adapter);
    }

    @Override
    public void onShoppingDataLoadFailed(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onShoppingItemSelected(ShoppingItem shoppingItem) {
        callBackToActivity.onShoppingItemSelected(shoppingItem);
    }
}
