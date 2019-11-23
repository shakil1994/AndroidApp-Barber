package com.example.shakil.androidbarberbooking.Adapter;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.shakil.androidbarberbooking.Database.CartDataSource;
import com.example.shakil.androidbarberbooking.Database.CartDatabase;
import com.example.shakil.androidbarberbooking.Database.CartItem;
import com.example.shakil.androidbarberbooking.Database.LocalCartDataSource;
import com.example.shakil.androidbarberbooking.R;
import com.squareup.picasso.Picasso;

import java.util.List;

public class MyCartAdapter extends RecyclerView.Adapter<MyCartAdapter.MyViewHolder> {

    Context context;
    List<CartItem> cartItemList;
    CartDataSource cartDataSource;
    CompositeDisposable compositeDisposable;

    public void onDestroy() {
        compositeDisposable.clear();
    }

    public MyCartAdapter(Context context, List<CartItem> cartItemList) {
        this.context = context;
        this.cartItemList = cartItemList;
        this.cartDataSource = new LocalCartDataSource(CartDatabase.getInstance(context).cartDAO());
        this.compositeDisposable = new CompositeDisposable();
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.layout_cart_item, viewGroup, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder myViewHolder, final int i) {
        Picasso.get().load(cartItemList.get(i).getProductImage()).into(myViewHolder.img_product);
        myViewHolder.txt_cart_name.setText(new StringBuilder(cartItemList.get(i).getProductName()));
        myViewHolder.txt_cart_price.setText(new StringBuilder("$").append(cartItemList.get(i).getProductPrice()));
        myViewHolder.txt_quantity.setText(new StringBuilder(String.valueOf(cartItemList.get(i).getProductQuantity())));

        //Event
        myViewHolder.setListener((view, pos, isDecrease) -> {
            if (isDecrease) {
                if (cartItemList.get(pos).getProductQuantity() > 1) {
                    cartItemList.get(pos).setProductQuantity(cartItemList.get(pos).getProductQuantity() - 1);

                    cartDataSource.update(cartItemList.get(pos))
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(new SingleObserver<Integer>() {
                                @Override
                                public void onSubscribe(Disposable d) {

                                }

                                @Override
                                public void onSuccess(Integer integer) {
                                    //When update success , just set quantity
                                    myViewHolder.txt_quantity.setText(new StringBuilder(String.valueOf(cartItemList.get(i).getProductQuantity())));
                                }

                                @Override
                                public void onError(Throwable e) {
                                    Toast.makeText(context, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                }

                //If User click decrease when item = 1, just delete it
                else if (cartItemList.get(pos).getProductQuantity() == 1) {
                    cartDataSource.delete(cartItemList.get(pos))
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(new SingleObserver<Integer>() {
                                @Override
                                public void onSubscribe(Disposable d) {

                                }

                                @Override
                                public void onSuccess(Integer integer) {
                                    cartItemList.remove(pos);
                                    notifyItemRemoved(pos);
                                }

                                @Override
                                public void onError(Throwable e) {
                                    Toast.makeText(context, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            } else {
                if (cartItemList.get(pos).getProductQuantity() < 99) {
                    cartItemList.get(pos).setProductQuantity(cartItemList.get(pos).getProductQuantity() + 1);

                    cartDataSource.update(cartItemList.get(pos)).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new SingleObserver<Integer>() {
                        @Override
                        public void onSubscribe(Disposable d) {

                        }

                        @Override
                        public void onSuccess(Integer integer) {
                            myViewHolder.txt_quantity.setText(new StringBuilder(String.valueOf(cartItemList.get(i).getProductQuantity())));
                        }

                        @Override
                        public void onError(Throwable e) {
                            Toast.makeText(context, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return cartItemList.size();
    }

    interface IImageButtonListener {
        void onImageButtonClick(View view, int pos, boolean isDecrease);
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        TextView txt_cart_name, txt_cart_price, txt_quantity;
        ImageView img_product, img_decrease, img_increase;

        IImageButtonListener listener;

        public void setListener(IImageButtonListener listener) {
            this.listener = listener;
        }

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            txt_cart_name = itemView.findViewById(R.id.txt_cart_name);
            txt_cart_price = itemView.findViewById(R.id.txt_cart_price);
            txt_quantity = itemView.findViewById(R.id.txt_cart_quantity);
            img_product = itemView.findViewById(R.id.cart_img);
            img_decrease = itemView.findViewById(R.id.img_decrease);
            img_increase = itemView.findViewById(R.id.img_increase);

            //Event
            img_decrease.setOnClickListener(v -> listener.onImageButtonClick(v, getAdapterPosition(), true));

            img_increase.setOnClickListener(v -> listener.onImageButtonClick(v, getAdapterPosition(), false));
        }
    }
}
