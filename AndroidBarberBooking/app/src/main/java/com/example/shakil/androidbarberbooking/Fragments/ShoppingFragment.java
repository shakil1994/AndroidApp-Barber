package com.example.shakil.androidbarberbooking.Fragments;

import android.os.Bundle;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.shakil.androidbarberbooking.Adapter.MyShoppingItemAdapter;
import com.example.shakil.androidbarberbooking.Common.SpacesItemDecoration;
import com.example.shakil.androidbarberbooking.Interface.IShoppingDataLoadListener;
import com.example.shakil.androidbarberbooking.Model.ShoppingItem;
import com.example.shakil.androidbarberbooking.R;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class ShoppingFragment extends Fragment implements IShoppingDataLoadListener {

    Unbinder unbinder;
    CollectionReference shoppingItemRef;
    MyShoppingItemAdapter adapter;

    IShoppingDataLoadListener iShoppingDataLoadListener;

    @BindView(R.id.chip_group)
    ChipGroup chipGroup;

    @BindView(R.id.recycler_items)
    RecyclerView recycler_items;

    @BindView(R.id.chip_wax)
    Chip chip_wax;

    @OnClick(R.id.chip_wax)
    void waxChipClick() {
        setSelectedChip(chip_wax);
        loadShoppingItem("Wax");
    }

    @BindView(R.id.chip_spray)
    Chip chip_spray;

    @OnClick(R.id.chip_spray)
    void sprayChipClick() {
        setSelectedChip(chip_spray);
        loadShoppingItem("Spray");
    }

    @BindView(R.id.chip_body_care)
    Chip chip_body_care;

    @OnClick(R.id.chip_body_care)
    void bodyCareChipClick() {
        setSelectedChip(chip_body_care);
        loadShoppingItem("BodyCare");
    }

    @BindView(R.id.chip_hair_care)
    Chip chip_hair_care;

    @OnClick(R.id.chip_hair_care)
    void hairCareChipClick() {
        setSelectedChip(chip_hair_care);
        loadShoppingItem("HairCare");
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
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View itemView = inflater.inflate(R.layout.fragment_shopping, container, false);
        unbinder = ButterKnife.bind(this, itemView);

        iShoppingDataLoadListener = this;

        //Default load
        loadShoppingItem("Wax");

        initView();

        return itemView;
    }

    private void initView() {
        recycler_items.setHasFixedSize(true);
        recycler_items.setLayoutManager(new GridLayoutManager(getContext(), 2));
        recycler_items.addItemDecoration(new SpacesItemDecoration(8));
    }

    @Override
    public void onShoppingDataLoadSuccess(List<ShoppingItem> shoppingItemList) {
        adapter = new MyShoppingItemAdapter(getContext(), shoppingItemList);
        recycler_items.setAdapter(adapter);
    }

    @Override
    public void onShoppingDataLoadFailed(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroy() {
        if (adapter != null) {
            adapter.onDestroy();
        }
        super.onDestroy();
    }
}
