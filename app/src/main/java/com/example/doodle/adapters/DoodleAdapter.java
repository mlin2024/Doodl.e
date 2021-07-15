package com.example.doodle.adapters;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.doodle.R;
import com.example.doodle.activities.GalleryActivity;
import com.example.doodle.fragments.DoodleDetailsFragment;
import com.example.doodle.models.Doodle;
import com.parse.ParseFile;

import java.io.Serializable;
import java.util.List;

public class DoodleAdapter extends RecyclerView.Adapter<DoodleAdapter.ViewHolder>{
    public static final String TAG = "DoodleAdapter";

    public Context context;
    public List<Doodle> doodles;

    public DoodleAdapter(Context context, List<Doodle> doodles) {
        this.context = context;
        this.doodles = doodles;
    }

    @NonNull
    @org.jetbrains.annotations.NotNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull @org.jetbrains.annotations.NotNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_doodle, parent, false);
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

        private DoodleDetailsFragment doodleDetailsFragment;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            itemView.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            doodleImageView = itemView.findViewById(R.id.doodleImageView);

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
        }

        @Override
        public void onClick(View v) {
            // TODO: move onclicklistener to gallery activity, out of the adapter
            int position = getAdapterPosition();
            if (position != RecyclerView.NO_POSITION) { // Check if position is valid
                // Get doodle
                Doodle doodle = doodles.get(position);

                // Set up popup detail fragment
                doodleDetailsFragment = DoodleDetailsFragment.newInstance(doodle);

                // Make sure there isn't already a fragment
                FragmentTransaction ft = GalleryActivity.fragmentManager.beginTransaction();
                Fragment prev = GalleryActivity.fragmentManager.findFragmentByTag(DoodleDetailsFragment.TAG);
                if (prev != null) {
                    ft.remove(prev);
                }
                ft.addToBackStack(null);

                // Reveal the fragment
                doodleDetailsFragment.show(GalleryActivity.fragmentManager, DoodleDetailsFragment.TAG);
            }
        }
    }
}
