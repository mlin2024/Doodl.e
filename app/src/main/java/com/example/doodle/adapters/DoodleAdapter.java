package com.example.doodle.adapters;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.doodle.R;
import com.example.doodle.models.Doodle;
import com.google.android.material.tabs.TabLayout;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseQuery;

import java.util.ArrayList;
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

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            if (usedForViewPager) itemView.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            doodleImageView = itemView.findViewById(R.id.doodleImageView);
            timestampTextView = itemView.findViewById(R.id.timestampTextView);

            itemView.setOnClickListener(this);
        }

        public void bind(Doodle doodle) {
            // Bind the post data to the view elements
            ParseFile image = doodle.getImage();
            if (image != null) {
                Glide.with(context)
                        .load(image.getUrl())
                        .into(doodleImageView);
            }
            timestampTextView.setText(doodle.getTimestamp());
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            if (position != RecyclerView.NO_POSITION) { // Check if position is valid
                // Get doodle
                Doodle doodle = doodles.get(position);

                // Set up popup detail fragment
                Dialog dialog = new Dialog(context);
                dialog.setContentView(R.layout.fragment_doodle_detail);

                Button backButton = dialog.findViewById(R.id.backButton);
                TabLayout versionTabLayout = dialog.findViewById(R.id.versionTabLayout);
                Button forwardButton = dialog.findViewById(R.id.forwardButton);
                ImageView doodleImageView = dialog.findViewById(R.id.doodleImageView);
                TextView timestampTextView = dialog.findViewById(R.id.timestampTextView);

                int tailLength = doodle.getTailLength();

                // Recursively find the history of this doodle
                Doodle[] doodleHistory = new Doodle[tailLength];
                Doodle currentDoodleInHistory = doodle;
                for (int i = 0; i < tailLength; i++) {
                    doodleHistory[doodle.getTailLength() - i - 1] = currentDoodleInHistory;
                    try {
                        currentDoodleInHistory = (Doodle) currentDoodleInHistory.fetchIfNeeded().getParseObject(Doodle.KEY_PARENT);
                    } catch (ParseException e) {
                        Toast.makeText(context, context.getResources().getString(R.string.error_finding_doodle), Toast.LENGTH_SHORT).show();
                    }
                }

                // Add a tab for each doodle in the history
                for (int i = 0; i < tailLength; i++) versionTabLayout.addTab(versionTabLayout.newTab());

                // Set selected tab to last tab
                versionTabLayout.selectTab(versionTabLayout.getTabAt(tailLength - 1));
                ParseFile image = doodle.getImage();
                if (image != null) {
                    Glide.with(context)
                            .load(image.getUrl())
                            .into(doodleImageView);
                }
                timestampTextView.setText(doodle.getTimestamp());

                // Disable appropriate button if at first/last tab
                if (versionTabLayout.getSelectedTabPosition() == 0) backButton.setEnabled(false);
                else backButton.setEnabled(true);
                if (versionTabLayout.getSelectedTabPosition() == tailLength - 1) forwardButton.setEnabled(false);
                else forwardButton.setEnabled(true);

                versionTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                    @Override
                    public void onTabSelected(TabLayout.Tab tab) {
                        // Disable appropriate button if at first/last tab
                        if (tab.getPosition() == 0) backButton.setEnabled(false);
                        else backButton.setEnabled(true);
                        if (tab.getPosition() == tailLength - 1) forwardButton.setEnabled(false);
                        else forwardButton.setEnabled(true);

                        // Load the appropriate doodle
                        Doodle currentDoodle = doodleHistory[tab.getPosition()];
                        ParseFile image = currentDoodle.getImage();
                        if (image != null) {
                            Glide.with(context)
                                    .load(image.getUrl())
                                    .into(doodleImageView);
                        }
                        timestampTextView.setText(currentDoodle.getTimestamp());
                    }

                    @Override
                    public void onTabUnselected(TabLayout.Tab tab) {}

                    @Override
                    public void onTabReselected(TabLayout.Tab tab) {
                        // Disable appropriate button if at first/last tab
                        if (tab.getPosition() == 0) backButton.setEnabled(false);
                        else backButton.setEnabled(true);
                        if (tab.getPosition() == tailLength - 1) forwardButton.setEnabled(false);
                        else forwardButton.setEnabled(true);
                    }
                });

                backButton.setOnClickListener(v1 -> {
                    int currentIndex = versionTabLayout.getSelectedTabPosition();
                    TabLayout.Tab tab = versionTabLayout.getTabAt(currentIndex - 1);
                    tab.select();
                });

                forwardButton.setOnClickListener(v1 -> {
                    int currentIndex = versionTabLayout.getSelectedTabPosition();
                    TabLayout.Tab tab = versionTabLayout.getTabAt(currentIndex + 1);
                    tab.select();
                });

                dialog.show();
            }
        }
    }
}
