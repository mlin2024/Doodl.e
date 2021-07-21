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

        // Set up view model
        viewModelProvider = new ViewModelProvider(requireActivity());
        colorViewModel = viewModelProvider.get(ColorViewModel.class);

        // Set up click listeners for each color button
        pinkButton.setOnClickListener(v -> colorViewModel.selectItem(getResources().getColorStateList(R.color.button_pink, getActivity().getTheme())));
        redButton.setOnClickListener(v -> colorViewModel.selectItem(getResources().getColorStateList(R.color.button_red, getActivity().getTheme())));
        orangeButton.setOnClickListener(v -> colorViewModel.selectItem(getResources().getColorStateList(R.color.button_orange, getActivity().getTheme())));
        yellowOrangeButton.setOnClickListener(v -> colorViewModel.selectItem(getResources().getColorStateList(R.color.button_yellow_orange, getActivity().getTheme())));
        yellowButton.setOnClickListener(v -> colorViewModel.selectItem(getResources().getColorStateList(R.color.button_yellow, getActivity().getTheme())));
        lightGreenButton.setOnClickListener(v -> colorViewModel.selectItem(getResources().getColorStateList(R.color.button_light_green, getActivity().getTheme())));
        darkGreenButton.setOnClickListener(v -> colorViewModel.selectItem(getResources().getColorStateList(R.color.button_dark_green, getActivity().getTheme())));
        tealButton.setOnClickListener(v -> colorViewModel.selectItem(getResources().getColorStateList(R.color.button_teal, getActivity().getTheme())));
        lightBlueButton.setOnClickListener(v -> colorViewModel.selectItem(getResources().getColorStateList(R.color.button_light_blue, getActivity().getTheme())));
        darkBlueButton.setOnClickListener(v -> colorViewModel.selectItem(getResources().getColorStateList(R.color.button_dark_blue, getActivity().getTheme())));
        purpleButton.setOnClickListener(v -> colorViewModel.selectItem(getResources().getColorStateList(R.color.button_purple, getActivity().getTheme())));
        brownButton.setOnClickListener(v -> colorViewModel.selectItem(getResources().getColorStateList(R.color.button_brown, getActivity().getTheme())));
        greyButton.setOnClickListener(v -> colorViewModel.selectItem(getResources().getColorStateList(R.color.button_grey, getActivity().getTheme())));
        blackButton.setOnClickListener(v -> colorViewModel.selectItem(getResources().getColorStateList(R.color.button_black, getActivity().getTheme())));

    }
}