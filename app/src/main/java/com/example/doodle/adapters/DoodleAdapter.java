package com.example.doodle.adapters;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.doodle.R;
import com.example.doodle.models.Doodle;
import com.google.android.material.snackbar.Snackbar;
import com.parse.ParseException;
import com.parse.ParseFile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class DoodleAdapter extends RecyclerView.Adapter<DoodleAdapter.ViewHolder>{
    public static final String TAG = "DoodleAdapter";

    public Context context;
    public List<Doodle> doodles;
    public boolean usedForViewPager;

    public DoodleAdapter(Context context, List<Doodle> doodles, boolean usedForViewPager) {
        this.context = context;
        this.doodles = doodles;
        this.usedForViewPager = usedForViewPager;
    }

    @NonNull
    @org.jetbrains.annotations.NotNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull @org.jetbrains.annotations.NotNull ViewGroup parent, int viewType) {
        View view;
        view = LayoutInflater.from(context).inflate(R.layout.item_doodle, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull @org.jetbrains.annotations.NotNull DoodleAdapter.ViewHolder holder, int position) {
        Doodle doodle = doodles.get(position);
        holder.bind(doodle);
    }

    @Override
    public int getItemCount() {
        return doodles.size();
    }

    // Clean all elements of the recycler
    public void clear() {
        doodles.clear();
        notifyDataSetChanged();
    }

    // Add a list of items
    public void addAll(List<Doodle> list) {
        doodles.addAll(list);
        notifyDataSetChanged();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private ImageView doodleImageView;
        private TextView timestampTextView;

        private Dialog dialog;
        private Doodle currentDoodle;
        private Doodle parent;
        private Doodle[] ancestors;
        private ArrayList<Doodle> children;
        private ArrayList<Doodle> siblings;
        private int currentAncestor;
        private int currentSibling;
        private ProgressDialog loadingProgressDialog;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            if (usedForViewPager) itemView.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            doodleImageView = itemView.findViewById(R.id.doodleImageView);
            timestampTextView = itemView.findViewById(R.id.timestampTextView);

            // Set up ProgressDialog
            loadingProgressDialog = new ProgressDialog(context);
            loadingProgressDialog.setMessage(context.getResources().getString(R.string.loading_doodle_history));
            loadingProgressDialog.setCancelable(false);

            itemView.setOnClickListener(this);
        }

        public void bind(Doodle doodle) {
            // Bind the post data to the view elements
            ParseFile image = doodle.getImage();
            if (image != null) {
                Glide.with(context)
                        .load(image.getUrl())
                        .placeholder(R.drawable.placeholder)
                        .into(doodleImageView);
            }
            timestampTextView.setText(doodle.getTimestamp());
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            if (position != RecyclerView.NO_POSITION) { // Check if position is valid

                // Get doodle
                currentDoodle = doodles.get(position);

                // Initialize the currently blank family tree, only known is current doodle at the end
                ancestors = new Doodle[currentDoodle.getTailLength()];
                Arrays.fill(ancestors, null);
                currentAncestor = currentDoodle.getTailLength() - 1;
                ancestors[currentAncestor] = currentDoodle;

                // Set up dialog
                dialog = new Dialog(context);
                dialog.setContentView(R.layout.doodle_detail);
                dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

                // Asynchronously load the doodle's history
                loadingProgressDialog.show();
                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(setupHistoryDialog);
            }
        }

        // Sets up the dialog that contains the doodle's history
        Runnable setupHistoryDialog = new Runnable() {
            public void run() {
                Button backButton = dialog.findViewById(R.id.backButton);
                Button forwardButton = dialog.findViewById(R.id.forwardButton);
                Button upButton = dialog.findViewById(R.id.upButton);
                Button downButton = dialog.findViewById(R.id.downButton);
                ImageView doodleImageView = dialog.findViewById(R.id.doodleImageView);
                TextView timestampTextView = dialog.findViewById(R.id.timestampTextView);

                Runnable initializeDoodle = () -> {
                    // Find all the relations of this doodle
                    try {
                        // Find parent
                        // If it is the very first one, it has no parent
                        if (currentAncestor == 0) parent = null;
                        // Else, if parent is part of the original doodle's ancestors, provide it
                        else if (currentAncestor - 1 <= ancestors.length - 1) {
                            // If parent is not yet loaded into family tree, load it in
                            if (ancestors[currentAncestor - 1] == null) ancestors[currentAncestor - 1] = (Doodle) currentDoodle.fetchIfNeeded().get(Doodle.KEY_PARENT);
                            parent = ancestors[currentAncestor - 1];
                        }
                        // Else, just find it
                        else parent = (Doodle) currentDoodle.fetchIfNeeded().get(Doodle.KEY_PARENT);

                        // Find children
                        // If child is part of original doodle's ancestors, only provide that one child as the child
                        if (currentAncestor + 1 <= ancestors.length - 1) {
                            // It is physically impossible to be here and not have the child already loaded in
                            // Just grab the one child and put it in the ArrayList
                            children = new ArrayList<>();
                            children.add(ancestors[currentAncestor + 1]);
                        }
                        // Else, load the full list of children
                        else children = currentDoodle.getChildren();

                        // Find siblings
                        // If current doodle is part of original doodle's ancestors, don't load any siblings
                        if (currentAncestor < ancestors.length) siblings = new ArrayList<>();

                        // Else, load its siblings
                        else siblings = currentDoodle.getDoodlesWithParent(parent);

                    } catch (ParseException e) {
                        Snackbar.make(itemView, context.getResources().getString(R.string.error_loading_history), Snackbar.LENGTH_LONG).show();
                        e.printStackTrace();
                    }

                    currentSibling = 0;

                    Log.d(TAG, "initializeDoodle:\n\tcurrentDoodle = " + currentDoodle + "\n\tparent = " + parent + "\n\tchildren = " + children.toString() + "\n\tsiblings = " + siblings.toString());
                };

                // Lambda function that loads the appropriate data into the view
                Runnable loadDoodle = () -> {
                    try {
                        Log.d(TAG, "Loading doodle " + currentDoodle);
                        ParseFile image = null;
                        image = currentDoodle.fetchIfNeeded().getParseFile(Doodle.KEY_IMAGE);
                        if (image != null) {
                            Glide.with(context)
                                    .load(image.getUrl())
                                    .placeholder(R.drawable.placeholder)
                                    .into(doodleImageView);
                        }
                        timestampTextView.setText(currentDoodle.getTimestamp());
                    } catch (ParseException e) {
                        Snackbar.make(itemView, context.getResources().getString(R.string.error_loading_history), Snackbar.LENGTH_LONG).show();
                        e.printStackTrace();
                    }
                };

                // Lambda function that disables the appropriate buttons if at first/last doodle
                Runnable disableAppropriateButtons = () -> {
                    // If it has no parent, you can't go back
                    if (parent == null) backButton.setEnabled(false);
                    else backButton.setEnabled(true);

                    // If it has no children, you can't go forward
                    if (children.size() == 0) forwardButton.setEnabled(false);
                    else forwardButton.setEnabled(true);

                    // If it has no siblings or its only sibling is itself, you can't go to next/previous sibling
                    if (siblings == null || siblings.size() <= 1) {
                        upButton.setEnabled(false);
                        downButton.setEnabled(false);
                    }
                    else {
                        upButton.setEnabled(true);
                        downButton.setEnabled(true);
                    }
                };

                Log.d(TAG, "\nLoading doodle for first time:\n\tcurrentAncestor = " + currentAncestor + "\n\tcurrentSibling = " + currentSibling);
                // Initialize the current doodle
                initializeDoodle.run();
                // Load the current doodle
                loadDoodle.run();
                // Disable appropriate button if at first/last doodle
                disableAppropriateButtons.run();

                backButton.setOnClickListener(v1 -> {
                    currentAncestor--;
                    currentDoodle = parent;
                    Log.d(TAG, "Back button pressed:\n\tcurrentAncestor = " + currentAncestor + "\n\tcurrentSibling = " + currentSibling);

                    // Initialize and load the appropriate doodle
                    initializeDoodle.run();
                    loadDoodle.run();

                    // Disable appropriate button if at first/last doodle
                    disableAppropriateButtons.run();
                });

                forwardButton.setOnClickListener(v1 -> {
                    currentAncestor++;
                    currentDoodle = children.get(0);
                    Log.d(TAG, "Forward button pressed:\n\tcurrentAncestor = " + currentAncestor + "\n\tcurrentSibling = " + currentSibling);

                    // Initialize and load the appropriate doodle
                    initializeDoodle.run();
                    loadDoodle.run();

                    // Disable appropriate button if at first/last doodle
                    disableAppropriateButtons.run();
                });

                upButton.setOnClickListener(v1 -> {
                    try {
                        currentSibling--;
                        // Loop around if out of bounds
                        if (currentSibling < 0) currentSibling = siblings.size() - 1;
                        currentDoodle = siblings.get(currentSibling);
                        Log.d(TAG, "Up button pressed:\n\tcurrentAncestor = " + currentAncestor + "\n\tcurrentSibling = " + currentSibling);
                        children = currentDoodle.getChildren();

                        // Load the appropriate doodle
                        loadDoodle.run();

                        // Disable appropriate button if at first/last doodle
                        disableAppropriateButtons.run();
                    } catch (ParseException e) {
                        Snackbar.make(itemView, context.getResources().getString(R.string.error_loading_history), Snackbar.LENGTH_LONG).show();
                        e.printStackTrace();
                    }
                });

                downButton.setOnClickListener(v1 -> {
                    try {
                        currentSibling++;
                        // Loop around if out of bounds
                        if (currentSibling > siblings.size() - 1) currentSibling = 0;
                        currentDoodle = siblings.get(currentSibling);
                        Log.d(TAG, "Down button pressed:\n\tcurrentAncestor = " + currentAncestor + "\n\tcurrentSibling = " + currentSibling);
                        children = currentDoodle.getChildren();

                        // Load the appropriate doodle
                        loadDoodle.run();

                        // Disable appropriate button if at first/last doodle
                        disableAppropriateButtons.run();
                    } catch (ParseException e) {
                        Snackbar.make(itemView, context.getResources().getString(R.string.error_loading_history), Snackbar.LENGTH_LONG).show();
                        e.printStackTrace();
                    }
                });

                loadingProgressDialog.dismiss();
                dialog.show();
            }
        };
    }
}
