package com.example.doodle.models;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.doodle.R;

import java.util.LinkedHashMap;

public class ColorViewModel extends ViewModel {
    private final MutableLiveData<Integer> selectedColorButtonId = new MutableLiveData<>();
    private final MutableLiveData<Integer> selectedColorId = new MutableLiveData<>();

    public void selectColorButton(int colorButtonId) {
        selectedColorButtonId.setValue(colorButtonId);
    }

    public void selectColor(int colorId) {
        selectedColorId.setValue(colorId);
    }

    public LiveData<Integer> getSelectedColorButtonId() {
        // Set black as default color button if no color button has been selected
        if (selectedColorButtonId.getValue() == null) selectColorButton(R.id.blackButton);
        return selectedColorButtonId;
    }

    public LiveData<Integer> getSelectedColorId() {
        // Set black as default color if no color has been selected
        if (selectedColorId.getValue() == null) selectColor(R.color.button_black);
        return selectedColorId;
    }

    // Resets the ViewModel to default
    public void clear() {
        selectColorButton(R.id.blackButton);
        selectColor(R.color.button_black);
    }
}
