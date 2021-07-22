package com.example.doodle.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.doodle.R;
import com.example.doodle.activities.DoodleActivity;
import com.example.doodle.models.ColorViewModel;

import org.jetbrains.annotations.NotNull;

public class ColorPickerFragment extends Fragment {
    public static final String TAG = "ColorPickerFragment";

    // Views in the layout
    private Button pinkButton;
    private Button redButton;
    private Button orangeButton;
    private Button yellowOrangeButton;
    private Button yellowButton;
    private Button lightGreenButton;
    private Button darkGreenButton;
    private Button tealButton;
    private Button lightBlueButton;
    private Button darkBlueButton;
    private Button purpleButton;
    private Button brownButton;
    private Button greyButton;
    private Button blackButton;

    // Other necessary member variables
    private Button currentButton;
    private ViewModelProvider viewModelProvider;
    private ColorViewModel colorViewModel;

    public ColorPickerFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_color_picker, container, false);
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize the views in the layout
        pinkButton = view.findViewById(R.id.pinkButton);
        redButton = view.findViewById(R.id.redButton);
        orangeButton = view.findViewById(R.id.orangeButton);
        yellowOrangeButton = view.findViewById(R.id.yellowOrangeButton);
        yellowButton = view.findViewById(R.id.yellowButton);
        lightGreenButton = view.findViewById(R.id.lightGreenButton);
        darkGreenButton = view.findViewById(R.id.darkGreenButton);
        tealButton = view.findViewById(R.id.tealButton);
        lightBlueButton = view.findViewById(R.id.lightBlueButton);
        darkBlueButton = view.findViewById(R.id.darkBlueButton);
        purpleButton = view.findViewById(R.id.purpleButton);
        brownButton = view.findViewById(R.id.brownButton);
        greyButton = view.findViewById(R.id.greyButton);
        blackButton = view.findViewById(R.id.blackButton);

        // Initialize other member variables
        currentButton = blackButton;
        // Set up view model
        viewModelProvider = new ViewModelProvider(requireActivity());
        colorViewModel = viewModelProvider.get(ColorViewModel.class);

        // Set up click listeners for each color button
        pinkButton.setOnClickListener(v -> handleClick(pinkButton, R.color.button_pink));
        redButton.setOnClickListener(v -> handleClick(redButton, R.color.button_red));
        orangeButton.setOnClickListener(v -> handleClick(orangeButton, R.color.button_orange));
        yellowOrangeButton.setOnClickListener(v -> handleClick(yellowOrangeButton, R.color.button_yellow_orange));
        yellowButton.setOnClickListener(v -> handleClick(yellowButton, R.color.button_yellow));
        lightGreenButton.setOnClickListener(v -> handleClick(lightGreenButton, R.color.button_light_green));
        darkGreenButton.setOnClickListener(v -> handleClick(darkGreenButton, R.color.button_dark_green));
        tealButton.setOnClickListener(v -> handleClick(tealButton, R.color.button_teal));
        lightBlueButton.setOnClickListener(v -> handleClick(lightBlueButton, R.color.button_light_blue));
        darkBlueButton.setOnClickListener(v -> handleClick(darkBlueButton, R.color.button_dark_blue));
        purpleButton.setOnClickListener(v -> handleClick(purpleButton, R.color.button_purple));
        brownButton.setOnClickListener(v -> handleClick(brownButton, R.color.button_brown));
        greyButton.setOnClickListener(v -> handleClick(greyButton, R.color.button_grey));
        blackButton.setOnClickListener(v -> handleClick(blackButton, R.color.button_black));
    }

    // Makes the currently selected button have the foreground icon
    public void handleClick(Button button, int colorStateListId) {
        colorViewModel.selectItem(getResources().getColorStateList(colorStateListId, getActivity().getTheme()));

        // Hide the icon on the previously selected button
        currentButton.setForegroundTintList(getResources().getColorStateList(R.color.button_transparent, getActivity().getTheme()));

        // Display the icon on the newly selected button
        currentButton = button;
        currentButton.setForegroundTintList(getResources().getColorStateList(R.color.button_white, getActivity().getTheme()));
    }
}