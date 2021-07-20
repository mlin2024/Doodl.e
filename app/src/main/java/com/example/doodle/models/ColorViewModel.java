package com.example.doodle.models;

import android.content.ClipData;
import android.content.res.ColorStateList;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class ColorViewModel extends ViewModel {
    private final MutableLiveData<ColorStateList> selectedItem = new MutableLiveData<>();

    public void selectItem(ColorStateList color) {
        selectedItem.setValue(color);
    }

    public LiveData<ColorStateList> getSelectedItem() {
        return selectedItem;
    }
}
