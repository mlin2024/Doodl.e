package com.example.doodle.fragments;

import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.divyanshu.draw.widget.DrawView;
import com.example.doodle.R;
import com.example.doodle.models.ColorViewModel;

import net.cachapa.expandablelayout.ExpandableLayout;

import org.jetbrains.annotations.NotNull;

public class CanvasFragment extends Fragment {
    public static final String TAG = "CanvasFragment";
    public static final float STROKE_WIDTH_SMALL = 10;
    public static final float STROKE_WIDTH_MEDIUM = 20;
    public static final float STROKE_WIDTH_LARGE = 30;
    public static final String PARENT_DOODLE = "ParentDoodle";
    public static final String TIME_CUR_ROUND_ENDS = "TimeCurRoundEnds";
    public static final String RESULT_DOODLE = "ResultDoodle";
    public static final String DRAWING_BITMAP = "DrawingBitmap";

    // Views in the layout
    private ImageView parentImageView;
    private DrawView doodleDrawView;
    private Button undoButton;
    private Button redoButton;
    private Button smallButton;
    private Button mediumButton;
    private Button largeButton;
    private ImageButton eraserButton;
    private ImageButton colorButton;
    private ExpandableLayout colorPickerExpandableLayout;
    private Button doneButton;

    // Other necessary member variables
    Bitmap parentBitmap;
    private FragmentManager fragmentManager;
    private Fragment colorPickerFragment;
    private ViewModelProvider viewModelProvider;
    private ColorViewModel colorViewModel;
    private ColorStateList currentColor;
    private Button currentSizeButton;
    private ImageButton currentPenButton;
    private Handler roundEndHandler;

    public CanvasFragment() {}

    public static CanvasFragment newInstance(Bitmap parentBitmap, long deadline) {
        CanvasFragment canvasFragment = new CanvasFragment();

        Bundle args = new Bundle();
        args.putParcelable(PARENT_DOODLE, parentBitmap);
        args.putLong(TIME_CUR_ROUND_ENDS, deadline);
        canvasFragment.setArguments(args);

        return canvasFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        parentBitmap = getArguments().getParcelable(PARENT_DOODLE);
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_canvas, container, false);
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize the views in the layout
        parentImageView = view.findViewById(R.id.parentImageView);
        doodleDrawView = view.findViewById(R.id.doodleDrawView);
        undoButton = view.findViewById(R.id.undoButton);
        redoButton = view.findViewById(R.id.redoButton);
        smallButton = view.findViewById(R.id.smallButton);
        mediumButton = view.findViewById(R.id.mediumButton);
        largeButton = view.findViewById(R.id.largeButton);
        eraserButton = view.findViewById(R.id.eraserButton);
        colorButton = view.findViewById(R.id.colorButton);
        colorPickerExpandableLayout = view.findViewById(R.id.colorPickerExpandableLayout);
        doneButton = view.findViewById(R.id.doneButton);

        // Initialize other member variables
        fragmentManager = getActivity().getSupportFragmentManager();
        colorPickerFragment = new ColorPickerFragment();
        viewModelProvider = new ViewModelProvider(this);
        // Set up ViewModel for color picker fragment
        colorViewModel = viewModelProvider.get(ColorViewModel.class);
        currentColor = getResources().getColorStateList(R.color.button_black, getActivity().getTheme());
        currentSizeButton = mediumButton;
        currentPenButton = colorButton;
        roundEndHandler = new Handler(Looper.getMainLooper());

        // Set up parent ImageView (if parentDoodle exists)
        // Get parent doodle from intent
        Bitmap parentBitmap = getArguments().getParcelable(PARENT_DOODLE);
        if (parentBitmap != null) {
            AnimationDrawable loadingDrawable = (AnimationDrawable) getResources().getDrawable(R.drawable.loading_circle, getActivity().getTheme());
            loadingDrawable.start();
            Glide.with(this)
                    .load(parentBitmap)
                    .placeholder(loadingDrawable)
                    .into(parentImageView);
        }

        // Set up color picker fragment
        fragmentManager.beginTransaction().add(R.id.colorPickerFrameLayout, colorPickerFragment).show(colorPickerFragment).commit();

        // Set up canvas
        resetCanvas();

        // Set timer to send result back to parent activity by the deadline, if the deadline exists
        long timeCurRoundEnds = getArguments().getLong(TIME_CUR_ROUND_ENDS);
        // If timeCurRoundEnds is -1, the parent activity is DoodleActivity and there is no round or time limit
        // Else, the parent activity is GameActivity and has set the timeCurRoundEnds
        if (timeCurRoundEnds != -1) {
            long timeLeftInRoundMillis = timeCurRoundEnds - System.currentTimeMillis();
            // Set the handler to send the result at the end of the round
            roundEndHandler.postDelayed(sendResultToParentActivity, timeLeftInRoundMillis);
        }

        undoButton.setOnClickListener(v -> {
            doodleDrawView.undo();
        });

        redoButton.setOnClickListener(v -> {
            doodleDrawView.redo();
        });

        smallButton.setOnClickListener(v -> {
            handleSizeButtonChange(smallButton);
            doodleDrawView.setStrokeWidth(STROKE_WIDTH_SMALL);
        });

        mediumButton.setOnClickListener(v -> {
            handleSizeButtonChange(mediumButton);
            doodleDrawView.setStrokeWidth(STROKE_WIDTH_MEDIUM);
        });

        largeButton.setOnClickListener(v -> {
            handleSizeButtonChange(largeButton);
            doodleDrawView.setStrokeWidth(STROKE_WIDTH_LARGE);
        });

        eraserButton.setOnClickListener(v -> {
            colorPickerExpandableLayout.collapse();

            handlePenButtonChange(eraserButton);

            // Change pen color to eraser color
            doodleDrawView.setColor(getResources().getColor(R.color.transparent, getActivity().getTheme()));
        });

        colorButton.setOnClickListener(v -> {
            // If it's not selected, select it
            if (colorButton.isSelected() == false) {
                handlePenButtonChange(colorButton);

                // Change pen color to the current color
                doodleDrawView.setColor(currentColor.getDefaultColor());
            }
            // If it's already selected, click will expand/retract the color picker ExpandableLayout
            else {
                if (colorPickerExpandableLayout.isExpanded()) {
                    colorPickerExpandableLayout.collapse();
                }
                else {
                    colorPickerExpandableLayout.expand();
                }
                colorViewModel.getSelectedItem().observe(getViewLifecycleOwner(), color -> {
                    currentColor = color;
                    colorButton.setBackgroundTintList(color);
                    doodleDrawView.setColor(color.getDefaultColor());
                });
            }
        });

        doneButton.setOnClickListener(v -> {
            roundEndHandler.removeCallbacksAndMessages(null);
            sendResultToParentActivity.run();
        });
    }

    // Put result doodle in bundle to send back to parent activity
    private Runnable sendResultToParentActivity = new Runnable() {
        @Override
        public void run() {
            doneButton.setEnabled(false);

            Bundle result = new Bundle();
            result.putParcelable(DRAWING_BITMAP, doodleDrawView.getBitmap());
            getParentFragmentManager().setFragmentResult(RESULT_DOODLE, result);
        }
    };

    private void handleSizeButtonChange(Button button) {
        // Hide the icon on the previously selected button
        currentSizeButton.setForeground(null);

        // Display the icon on the newly selected button
        currentSizeButton = button;
        currentSizeButton.setForeground(getResources().getDrawable(R.drawable.transparent_circle_indicator, getActivity().getTheme()));
    }

    private void handlePenButtonChange(ImageButton button) {
        // Set previously selected button to unselected
        currentPenButton.setSelected(false);

        // Hide the icon on the previously selected button
        currentPenButton.setForeground(null);

        // Display the icon on the newly selected button
        currentPenButton = button;
        currentPenButton.setForeground(getResources().getDrawable(R.drawable.transparent_circle_indicator, getActivity().getTheme()));

        // Set the newly selected button to selected
        currentPenButton.setSelected(true);
    }

    // Resets the canvas to a default state
    private void resetCanvas() {
        doodleDrawView.clearCanvas();
        doodleDrawView.setStrokeWidth(STROKE_WIDTH_MEDIUM);
        doodleDrawView.setColor(currentColor.getDefaultColor());

        // Set up pen buttons
        eraserButton.setSelected(false);
        colorButton.setSelected(true);
        handlePenButtonChange(colorButton);
        handleSizeButtonChange(mediumButton);
    }


}