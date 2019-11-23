package com.example.shakil.androidbarberstaff.Common;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;

import com.example.shakil.androidbarberstaff.Model.MyNotification;

import java.util.List;

public class MyDiffCallBack extends DiffUtil.Callback {

    List<MyNotification> oldList;
    List<MyNotification> newList;

    public MyDiffCallBack(List<MyNotification> oldList, List<MyNotification> newList) {
        this.oldList = oldList;
        this.newList = newList;
    }

    @Override
    public int getOldListSize() {
        return oldList.size();
    }

    @Override
    public int getNewListSize() {
        return newList.size();
    }

    @Override
    public boolean areItemsTheSame(int i, int i1) {
        return oldList.get(i).getUid() == newList.get(i1).getUid();
    }

    @Override
    public boolean areContentsTheSame(int i, int i1) {
        return oldList.get(i) == newList.get(i1);
    }

    @Nullable
    @Override
    public Object getChangePayload(int oldItemPosition, int newItemPosition) {
        return super.getChangePayload(oldItemPosition, newItemPosition);
    }
}
