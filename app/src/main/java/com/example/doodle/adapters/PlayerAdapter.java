package com.example.doodle.adapters;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.doodle.R;
import com.google.android.material.snackbar.Snackbar;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;

import java.util.List;

public class PlayerAdapter extends RecyclerView.Adapter<PlayerAdapter.ViewHolder>{
    public static final String TAG = "PlayerAdapter";

    public Context context;
    public List<ParseUser> players;
    public String gameCode; // Used to calculate unique username color

    public PlayerAdapter(Context context, List<ParseUser> players, String gameCode) {
        this.context = context;
        this.players = players;
        this.gameCode = gameCode;
    }

    @NonNull
    @org.jetbrains.annotations.NotNull
    @Override
    public PlayerAdapter.ViewHolder onCreateViewHolder(@NonNull @org.jetbrains.annotations.NotNull ViewGroup parent, int viewType) {
        View view;
        view = LayoutInflater.from(context).inflate(R.layout.item_player, parent, false);
        return new PlayerAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull @org.jetbrains.annotations.NotNull PlayerAdapter.ViewHolder holder, int position) {
        ParseUser player = players.get(position);
        holder.bind(player);
    }

    @Override
    public int getItemCount() {
        return players.size();
    }

    // Clean all elements of the recycler
    public void clear() {
        players.clear();
        notifyDataSetChanged();
    }

    // Add a list of items
    public void addAll(List<ParseUser> list) {
        players.addAll(list);
        notifyDataSetChanged();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private TextView usernameTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            usernameTextView = itemView.findViewById(R.id.usernameTextView);
        }

        public void bind(ParseUser player) {
            // Bind the player data to the view elements
            try {
                usernameTextView.setText(player.fetchIfNeeded().getUsername());
                // Make the name a random color
                long hash = gameCode.hashCode();
                long hash2 = player.getObjectId().hashCode();
                int r = (int)(((hash * 17) + (hash2 * 23)) % 200) + 20;
                int g = (int)(((hash * 13) + (hash2 * 17)) % 200) + 20;
                int b = (int)(((hash * 23) + (hash2 * 13)) % 200) + 20;
                int textColor = Color.argb(255, r, g, b);
                int shadowColor = Color.argb(100, Math.max(0, r - 50), Math.max(0, g - 50), Math.max(0, b - 50));
                usernameTextView.setTextColor(textColor);
                usernameTextView.setShadowLayer(3, 3, 3, shadowColor);
            } catch (ParseException e) {
                Toast.makeText(context, context.getResources().getString(R.string.error_finding_players), Toast.LENGTH_SHORT).show();
            }
        }
    }
}
