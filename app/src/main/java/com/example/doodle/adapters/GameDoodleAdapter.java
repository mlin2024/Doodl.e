package com.example.doodle.adapters;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.doodle.R;
import com.example.doodle.models.Doodle;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class GameDoodleAdapter extends RecyclerView.Adapter<GameDoodleAdapter.ViewHolder>{
    public static final String TAG = "GameDoodleAdapter";

    public Context context;
    public List<Doodle> doodles;

    public GameDoodleAdapter(Context context, List<Doodle> doodles) {
        this.context = context;
        this.doodles = doodles;
    }

    @NonNull
    @org.jetbrains.annotations.NotNull
    @Override
    public GameDoodleAdapter.ViewHolder onCreateViewHolder(@NonNull @org.jetbrains.annotations.NotNull ViewGroup parent, int viewType) {
        View view;
        view = LayoutInflater.from(context).inflate(R.layout.item_doodle_detail_game, parent, false);
        return new GameDoodleAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull @org.jetbrains.annotations.NotNull GameDoodleAdapter.ViewHolder holder, int position) {
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

    class ViewHolder extends RecyclerView.ViewHolder {
        private TextView originalArtistTextView;
        private Button backButton_GAME;
        private TabLayout versionTabLayout_GAME;
        private Button forwardButton_GAME;
        private ImageView doodleImageView_GAME;
        private TextView artistTextView;

        private Doodle currentDoodle;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            originalArtistTextView = itemView.findViewById(R.id.originalArtistTextView);
            backButton_GAME = itemView.findViewById(R.id.backButton_GAME);
            versionTabLayout_GAME = itemView.findViewById(R.id.versionTabLayout_GAME);
            forwardButton_GAME = itemView.findViewById(R.id.forwardButton_GAME);
            doodleImageView_GAME = itemView.findViewById(R.id.doodleImageView_GAME);
            artistTextView = itemView.findViewById(R.id.artistTextView);
        }

        public void bind(Doodle doodle) {
            int position = getAdapterPosition();
            if (position != RecyclerView.NO_POSITION) { // Check if position is valid
                currentDoodle = doodles.get(position);

                // Set up the original artist TextView
                try {
                    originalArtistTextView.setText(currentDoodle.getArtist().fetchIfNeeded().getUsername()
                            + context.getResources().getString(R.string.s_doodle));
                } catch (ParseException e) {
                    Snackbar.make(itemView, context.getResources().getString(R.string.error_finding_doodle), Snackbar.LENGTH_LONG).show();
                }

                // Find the future versions of this doodle
                ArrayList<Doodle> versions = findVersions(doodle);

                int tailLength = versions.size();

                // Lambda function that disables the appropriate buttons if at first/last tab
                Consumer<Integer> disableAppropriateButtons = (tab) -> {
                    if (tab == 0) backButton_GAME.setEnabled(false);
                    else backButton_GAME.setEnabled(true);
                    if (tab == tailLength - 1) forwardButton_GAME.setEnabled(false);
                    else forwardButton_GAME.setEnabled(true);
                };

                // Lambda function that loads the appropriate data into the view
                Consumer<Integer> loadTab = (tab) -> {
                    try {
                        Doodle currentDoodle = versions.get(tab);
                        ParseFile image = currentDoodle.getImage();
                        if (image != null) {
                            AnimationDrawable loadingDrawable = (AnimationDrawable) context.getResources().getDrawable(R.drawable.loading_circle, context.getTheme());
                            loadingDrawable.start();
                            Glide.with(context)
                                    .load(image.getUrl())
                                    .placeholder(loadingDrawable)
                                    .into(doodleImageView_GAME);
                        }
                        artistTextView.setText(currentDoodle.getArtist().fetchIfNeeded().getUsername());
                    } catch (ParseException e) {
                        Snackbar.make(itemView, context.getResources().getString(R.string.error_finding_doodle), Snackbar.LENGTH_LONG).show();
                    }
                };

                // Add a tab for each doodle in the history
                if (versionTabLayout_GAME.getTabCount() == 0) { // Only add new tabs if it doesn't already have them from being previously loaded
                    for (int i = 0; i < tailLength; i++)
                        versionTabLayout_GAME.addTab(versionTabLayout_GAME.newTab());
                }

                // Set selected tab to first tab
                versionTabLayout_GAME.selectTab(versionTabLayout_GAME.getTabAt(0));
                loadTab.accept(0);

                // Disable appropriate button if at first/last tab
                disableAppropriateButtons.accept(versionTabLayout_GAME.getSelectedTabPosition());

                // Listen for whenever the tab is changed
                versionTabLayout_GAME.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
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

                backButton_GAME.setOnClickListener(v -> {
                    int currentIndex = versionTabLayout_GAME.getSelectedTabPosition();
                    TabLayout.Tab tab = versionTabLayout_GAME.getTabAt(currentIndex - 1);
                    tab.select();
                });

                forwardButton_GAME.setOnClickListener(v -> {
                    int currentIndex = versionTabLayout_GAME.getSelectedTabPosition();
                    TabLayout.Tab tab = versionTabLayout_GAME.getTabAt(currentIndex + 1);
                    tab.select();
                });
            }
        }

        private ArrayList<Doodle> findVersions(Doodle doodle) {
            // Specify what type of data we want to query - Doodle.class
            ParseQuery<Doodle> query = ParseQuery.getQuery(Doodle.class);
            // Include only doodles with the root of the original
            query.whereEqualTo(Doodle.KEY_ROOT, doodle.getRoot());
            // Order doodles by tail length in order to get the right order
            query.addAscendingOrder(Doodle.KEY_TAIL_LENGTH);
            // Start a synchronous call for the doodles and return the result
            try {
                return (ArrayList) query.find();
            }  catch (ParseException e) {
                Snackbar.make(itemView, context.getResources().getString(R.string.error_loading_history), Snackbar.LENGTH_LONG).show();
                return new ArrayList<>();
            }
        }
    }
}
