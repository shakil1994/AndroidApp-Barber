package com.example.shakil.androidbarberstaff.Interface;

import android.content.DialogInterface;

public interface IDialogClickListener {
    void onClickPositiveButton(DialogInterface dialogInterface, String userName, String password);

    void onClickNegativeButton(DialogInterface dialogInterface);
}
