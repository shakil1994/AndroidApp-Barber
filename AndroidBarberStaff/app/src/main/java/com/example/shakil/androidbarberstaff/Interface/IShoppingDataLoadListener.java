package com.example.shakil.androidbarberstaff.Interface;

import com.example.shakil.androidbarberstaff.Model.ShoppingItem;

import java.util.List;

public interface IShoppingDataLoadListener {
    void onShoppingDataLoadSuccess(List<ShoppingItem> shoppingItemList);

    void onShoppingDataLoadFailed(String message);
}
