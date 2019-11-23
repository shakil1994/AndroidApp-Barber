package com.example.shakil.androidbarberbooking.Database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;

import java.util.List;

@Dao
public interface CartDAO {

    @Query("SELECT SUM(productPrice * productQuantity) FROM Cart WHERE userPhone = :userPhone")
    Single<Long> sumPrice(String userPhone);

    @Query("SELECT * FROM Cart WHERE userPhone = :userPhone")
    Flowable<List<CartItem>> getAllItemFromCart(String userPhone);

    @Query("SELECT COUNT(*) FROM Cart WHERE userPhone = :userPhone")
    Single<Integer> countItemInCart(String userPhone);

    @Query("SELECT * FROM Cart WHERE productId = :productId AND userPhone= :userPhone")
    Flowable<CartItem> getProductInCart(String productId, String userPhone);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Completable insert(CartItem... carts);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    Single<Integer> update(CartItem cart);

    @Delete
    Single<Integer> delete(CartItem cartItem);

    @Query("DELETE FROM Cart WHERE userPhone = :userPhone")
    Single<Integer> clearCart(String userPhone);
}
