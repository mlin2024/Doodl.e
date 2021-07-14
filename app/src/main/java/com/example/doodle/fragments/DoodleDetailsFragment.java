package com.example.doodle.fragments;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Point;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.Resource;
import com.example.doodle.R;
import com.example.doodle.models.Doodle;

import org.jetbrains.annotations.NotNull;

public class DoodleDetailsFragment extends DialogFragment {
    public static final String TAG = "DoodleDetailsFragment";

    private ImageView doodleDetailsImageView;
    private TextView timestampTextView;

    private Doodle doodle;

    public DoodleDetailsFragment() {}

    public static DoodleDetailsFragment newInstance(Doodle doodle) {
        DoodleDetailsFragment frag = new DoodleDetailsFragment();
        Bundle args = new Bundle();
        args.putSerializable(Doodle.class.getSimpleName(), doodle);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_doodle_details, container, false);
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        doodleDetailsImageView = view.findViewById(R.id.doodleDetailsImageView);
        timestampTextView = view.findViewById(R.id.timestampTextView);

        // Unwrap the doodle that was passed in to the fragment by the activity
        doodle = (Doodle) getArguments().getSerializable(Doodle.class.getSimpleName());

        Glide.with(this)
                .load(doodle.getImage().getUrl())
                .into(doodleDetailsImageView);
        timestampTextView.setText(doodle.getTimestamp());
    }

    public void onResume() {
        // Store access variables for window and blank point
        Window window = getDialog().getWindow();
        Point size = new Point();
        // Store dimensions of the screen in `size`
        Display display = window.getWindowManager().getDefaultDisplay();
        display.getSize(size);
        // Set the width of the dialog to the screen width
        window.setLayout((int) size.x, WindowManager.LayoutParams.WRAP_CONTENT);
        window.setGravity(Gravity.CENTER);
        // Call super onResume after sizing
        super.onResume();
    }
}