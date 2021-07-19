package com.example.doodle.adapters;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.doodle.R;
import com.example.doodle.activities.GalleryActivity;
import com.example.doodle.models.Doodle;
import com.parse.ParseFile;

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
        if (usedForViewPager) view = LayoutInflater.from(context).inflate(R.layout.item_doodle_bordered, parent, false);
        else view = LayoutInflater.from(context).inflate(R.layout.item_doodle, parent, false);
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
                dialog.setContentView(R.layout.item_doodle);
                ImageView doodleImageView = dialog.findViewById(R.id.doodleImageView);
                TextView timestampTextView = dialog.findViewById(R.id.timestampTextView);
                ParseFile image = doodle.getImage();
                if (image != null) {
                    Glide.with(context)
                            .load(image.getUrl())
                            .into(doodleImageView);
                }
                timestampTextView.setText(doodle.getTimestamp());
                dialog.show();
            }
        }
    }
}
