package com.example.shakil.androidbarberbooking.Interface;

import com.example.shakil.androidbarberbooking.Model.ShoppingItem;

import java.util.List;

public interface IShoppingDataLoadListener {
    void onShoppingDataLoadSuccess(List<ShoppingItem> shoppingItemList);

    void onShoppingDataLoadFailed(String message);
}
