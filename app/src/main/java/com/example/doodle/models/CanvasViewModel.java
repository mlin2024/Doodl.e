package com.example.doodle.models;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.doodle.R;

import java.util.LinkedHashMap;

public class CanvasViewModel extends ViewModel {
    private final MutableLiveData<Integer> selectedSizeButtonId = new MutableLiveData<>();
    private final MutableLiveData<Integer> selectedPenButtonId = new MutableLiveData<>();
    private final MutableLiveData<LinkedHashMap> currentPaths = new MutableLiveData<>();
    private final MutableLiveData<Boolean> colorPickerIsExpanded = new MutableLiveData<>();

    public void selectSizeButton(int sizeButtonId) {
        selectedSizeButtonId.setValue(sizeButtonId);
    }

    public void selectPenButton(int penButtonId) {
        selectedPenButtonId.setValue(penButtonId);
    }

    public void setPaths(LinkedHashMap paths) {
        currentPaths.setValue(paths);
    }

    public void setColorPickerIsExpanded(boolean expanded) {
        colorPickerIsExpanded.setValue(expanded);
    }

    public LiveData<Integer> getSelectedSizeButtonId() {
        // Set medium as default size button if no size button has been selected
        if (selectedSizeButtonId.getValue() == null) selectSizeButton(R.id.mediumButton);
        return selectedSizeButtonId;
    }

    public LiveData<Integer> getSelectedPenButtonId() {
        // Set color as default pen button if no pen button has been selected
        if (selectedPenButtonId.getValue() == null) selectPenButton(R.id.colorButton);
        return selectedPenButtonId;
    }

    public LiveData<LinkedHashMap> getPaths() {
        // Set empty LinkedHashMap as default
        if (currentPaths.getValue() == null) setPaths(new LinkedHashMap());
        return currentPaths;
    }

    public LiveData<Boolean> getColorPickerIsExpanded() {
        // Set false as default
        if (colorPickerIsExpanded.getValue() == null) setColorPickerIsExpanded(false);
        return colorPickerIsExpanded;
    }

    // Resets the ViewModel to default
    public void clear() {
        selectSizeButton(R.id.mediumButton);
        selectPenButton(R.id.colorButton);
        setPaths(new LinkedHashMap());
        setColorPickerIsExpanded(false);
    }
}
