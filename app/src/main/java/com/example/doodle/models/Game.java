package com.example.doodle.models;

import android.os.Parcelable;
import android.util.Log;
import android.view.View;

import com.example.doodle.R;
import com.google.android.material.snackbar.Snackbar;
import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseUser;

import org.json.JSONArray;

import java.util.ArrayList;

@ParseClassName("Game")
public class Game extends ParseObject implements Parcelable {
    public static final String TAG = "Doodle";
    public static final String KEY_GAME_CODE = "gameCode";
    public static final String KEY_PLAYERS = "players";
    public static final String KEY_CREATOR = "creator";
    public static final String KEY_STARTED = "started";

    public String getGameCode() {
        return getString(KEY_GAME_CODE);
    }

    public void setGameCode(String gameCode) {
        put(KEY_GAME_CODE, gameCode);
    }

    public ArrayList<ParseUser> getPlayers() {
        ArrayList<ParseUser> players = new ArrayList<>();
        if (containsKey(KEY_PLAYERS)) {
            for (ParseUser player : (ArrayList<ParseUser>) get(KEY_PLAYERS)) {
                players.add(player);
            }
        }
        return players;
    }

    public void addPlayer(ParseUser player) {
        ArrayList<ParseUser> players = getPlayers();
        players.add(player);
        put(KEY_PLAYERS, players);
    }

    public ParseUser getCreator() {
        return getParseUser(KEY_CREATOR);
    }

    public void setCreator(ParseUser creator) {
        put(KEY_CREATOR, creator);
    }

    public boolean getStarted() {
        return getBoolean(KEY_STARTED);
    }

    public void setStarted(boolean started) {
        put(KEY_STARTED, started);
    }

    // Asynchronously saves the user data to the database
    public void saveInBackground(View view) {
        saveInBackground(e -> {
            if (e != null) {
                Snackbar.make(view, R.string.error_updating_game, Snackbar.LENGTH_LONG).show();
            }
        });
    }
}
