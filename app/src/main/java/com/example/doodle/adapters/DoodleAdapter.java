package com.example.doodle.adapters;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.os.Looper;
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
import com.example.doodle.activities.ContributionsGalleryActivity;
import com.example.doodle.models.Doodle;
import com.example.doodle.models.Player;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;

import java.util.List;
import java.util.function.Consumer;

public class DoodleAdapter extends RecyclerView.Adapter<DoodleAdapter.ViewHolder>{
    public static final String TAG = "DoodleAdapter";

    public Context context;
    public List<Doodle> doodles;
    public boolean usedForViewPager;
    public boolean showArtist;
    public boolean showSeeContributionsButton;

    public DoodleAdapter(Context context, List<Doodle> doodles, boolean usedForViewPager, boolean showArtist, boolean showSeeContributionsButton) {
        this.context = context;
        this.doodles = doodles;
        this.usedForViewPager = usedForViewPager;
        this.showArtist = showArtist;
        this.showSeeContributionsButton = showSeeContributionsButton;
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
        private TextView artistTextView;
        private View dividerView;
        private TextView timestampTextView;

        private Dialog dialog;
        private Doodle doodle;
        private ProgressDialog loadingProgressDialog;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            if (usedForViewPager) itemView.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            doodleImageView = itemView.findViewById(R.id.doodleImageView);
            artistTextView = itemView.findViewById(R.id.artistTextView);
            dividerView = itemView.findViewById(R.id.dividerView);
            timestampTextView = itemView.findViewById(R.id.timestampTextView);

            loadingProgressDialog = new ProgressDialog(context);
            loadingProgressDialog.setMessage(context.getResources().getString(R.string.loading_doodle_history));

            itemView.setOnClickListener(this);
        }

        public void bind(Doodle doodle) {
            // Bind the doodle data to the view elements
            try {
                ParseFile image = doodle.getImage();
                if (image != null) {
                    AnimationDrawable loadingDrawable = (AnimationDrawable) context.getResources().getDrawable(R.drawable.loading_circle, context.getTheme());
                    loadingDrawable.start();
                    Glide.with(context)
                            .load(image.getUrl())
                            .placeholder(loadingDrawable)
                            .into(doodleImageView);
                }
                // Remove the artist TextView if it is not meant to be shown
                if (!showArtist) {
                    artistTextView.setVisibility(View.GONE);
                    dividerView.setVisibility(View.GONE);
                }
                // Otherwise, populate it
                else {
                    Player artist = new Player(doodle.getArtist().fetchIfNeeded());
                    if (artist.getObjectId().equals(ParseUser.getCurrentUser().getObjectId())) {
                        artistTextView.setText(context.getResources().getString(R.string.you));
                    }
                    else if (artist.getIsAnonymous()) {
                        artistTextView.setText(context.getResources().getString(R.string.anon));
                    }
                    else {
                        artistTextView.setText(artist.getUsername());
                    }
                }
                timestampTextView.setText(doodle.getTimestamp());
            } catch (ParseException e) {
                Snackbar.make(itemView, context.getResources().getString(R.string.error_finding_doodle), Snackbar.LENGTH_LONG).show();
            }
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            if (position != RecyclerView.NO_POSITION) { // Check if position is valid

                // Get doodle
                doodle = doodles.get(position);

                // Set up dialog
                dialog = new Dialog(context);
                dialog.setContentView(R.layout.item_doodle_detail);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

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
                TabLayout versionTabLayout = dialog.findViewById(R.id.versionTabLayout);
                Button forwardButton = dialog.findViewById(R.id.forwardButton);
                ImageView doodleImageView = dialog.findViewById(R.id.doodleImageView);
                TextView artistTextView = dialog.findViewById(R.id.artistTextView);
                TextView timestampTextView = dialog.findViewById(R.id.timestampTextView);
                Button seeContributionsButton = dialog.findViewById(R.id.seeContributionsButton);
                Button Xbutton = dialog.findViewById(R.id.Xbutton);

                // Hide seeContributionsButton if it is not wanted
                if (!showSeeContributionsButton) seeContributionsButton.setVisibility(View.GONE);

                int tailLength = doodle.getTailLength();

                // Recursively find the history of this doodle
                Doodle[] doodleHistory = findDoodleHistory(doodle);

                // Lambda function that disables the appropriate buttons if at first/last tab
                Consumer<Integer> disableAppropriateButtons = (tab) -> {
                    if (tab == 0) backButton.setEnabled(false);
                    else backButton.setEnabled(true);
                    if (tab == tailLength - 1) forwardButton.setEnabled(false);
                    else forwardButton.setEnabled(true);
                };

                // Lambda function that loads the appropriate data into the view
                Consumer<Integer> loadTab = (tab) -> {
                    try {
                        Doodle currentDoodle = doodleHistory[tab];
                        ParseFile image = currentDoodle.getImage();
                        if (image != null) {
                            AnimationDrawable loadingDrawable = (AnimationDrawable) context.getResources().getDrawable(R.drawable.loading_circle, context.getTheme());
                            loadingDrawable.start();
                            Glide.with(context)
                                    .load(image.getUrl())
                                    .placeholder(loadingDrawable)
                                    .into(doodleImageView);
                        }
                        Player artist = new Player(currentDoodle.getArtist().fetchIfNeeded());
                        if (artist.getObjectId().equals(ParseUser.getCurrentUser().getObjectId())) {
                            artistTextView.setText(context.getResources().getString(R.string.you));
                        }
                        else if (artist.getIsAnonymous()) {
                            artistTextView.setText(context.getResources().getString(R.string.anon));
                        }
                        else {
                            artistTextView.setText(artist.getUsername());
                        }
                        timestampTextView.setText(currentDoodle.getTimestamp());
                    } catch (ParseException e) {
                        Snackbar.make(itemView, context.getResources().getString(R.string.error_loading_history), Snackbar.LENGTH_LONG).show();
                    }
                };

                // Add a tab for each doodle in the history
                for (int i = 0; i < tailLength; i++)
                    versionTabLayout.addTab(versionTabLayout.newTab());

                // Set selected tab to last tab
                versionTabLayout.selectTab(versionTabLayout.getTabAt(tailLength - 1));
                loadTab.accept(tailLength - 1);

                // Disable appropriate button if at first/last tab
                disableAppropriateButtons.accept(versionTabLayout.getSelectedTabPosition());

                // Listen for whenever the tab is changed
                versionTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                    @Override
                    public void onTabSelected(TabLayout.Tab tab) {
                        // Disable appropriate button if at first/last tab
                        disableAppropriateButtons.accept(tab.getPosition());

                        // Load the appropriate doodle
                        loadTab.accept(tab.getPosition());
                    }

                    @Override
                    public void onTabUnselected(TabLayout.Tab tab) {
                    }

                    @Override
                    public void onTabReselected(TabLayout.Tab tab) {
                        // Disable appropriate button if at first/last tab
                        disableAppropriateButtons.accept(tab.getPosition());
                    }
                });

                backButton.setOnClickListener(v -> {
                    int currentIndex = versionTabLayout.getSelectedTabPosition();
                    TabLayout.Tab tab = versionTabLayout.getTabAt(currentIndex - 1);
                    tab.select();
                });

                forwardButton.setOnClickListener(v -> {
                    int currentIndex = versionTabLayout.getSelectedTabPosition();
                    TabLayout.Tab tab = versionTabLayout.getTabAt(currentIndex + 1);
                    tab.select();
                });

                seeContributionsButton.setOnClickListener(v -> {
                    // Go to the contributions gallery and show this doodle's children
                    goContributionsGalleryActivity(doodleHistory[versionTabLayout.getSelectedTabPosition()]);
                });

                Xbutton.setOnClickListener(v -> dialog.dismiss());

                loadingProgressDialog.dismiss();
                dialog.show();
            }
        };

        // Recursively finds the history of this doodle
        private Doodle[] findDoodleHistory(Doodle doodle) {
            int tailLength = doodle.getTailLength();
            Doodle[] doodleHistory = new Doodle[tailLength];
            Doodle currentDoodleInHistory = doodle;
            for (int i = 0; i < tailLength; i++) {
                doodleHistory[doodle.getTailLength() - i - 1] = currentDoodleInHistory;
                try {
                    currentDoodleInHistory = (Doodle) currentDoodleInHistory.fetchIfNeeded().getParseObject(Doodle.KEY_PARENT);
                } catch (ParseException e) {
                    Snackbar.make(itemView, context.getResources().getString(R.string.error_loading_history), Snackbar.LENGTH_LONG).show();
                }
            }
            return doodleHistory;
        }

        // Starts an intent to go to the contributions gallery activity
        private void goContributionsGalleryActivity(Doodle doodle) {
            Intent intent = new Intent(context, ContributionsGalleryActivity.class);
            // Pass in the doodle
            intent.putExtra(ContributionsGalleryActivity.ORIGINAL_DOODLE, doodle);
            context.startActivity(intent);
        }
    }
}