package com.example.doodle.models;

import android.os.Parcelable;
import android.util.Log;
import android.view.View;

import com.google.android.material.snackbar.Snackbar;
import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseUser;

import java.util.ArrayList;

@ParseClassName("Game")
public class Game extends ParseObject implements Parcelable {
    public static final String TAG = "Doodle";
    public static final String KEY_GAME_CODE = "gameCode";
    public static final String KEY_PLAYERS = "players";
    public static final String KEY_HOST = "host";
    public static final String KEY_TIME_LIMIT = "timeLimit";
    public static final String KEY_ROUND = "round";

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

    public void removePlayer(ParseUser player) {
        ArrayList<ParseUser> players = getPlayers();
        for (int i = 0; i < players.size(); i++) {
            if (players.get(i).getObjectId().equals(player.getObjectId())) {
                players.remove(i);
                put(KEY_PLAYERS, players);
                return;
            }
        }
    }

    public ParseUser getHost() {
        return getParseUser(KEY_HOST);
    }

    public void setHost(ParseUser host) {
        put(KEY_HOST, host);
    }

    public int getTimeLimit() {
        return getInt(KEY_TIME_LIMIT);
    }

    public void setTimeLimit(int timeLimit) {
        put(KEY_TIME_LIMIT, timeLimit);
    }

    public int getRound() {
        return getInt(KEY_ROUND);
    }

    public void setRound(int round) {
        put(KEY_ROUND, round);
    }

    // Asynchronously saves the user data to the database
    public void saveInBackground(View view, String errorMessage, Runnable run) {
        saveInBackground(e -> {
            if (e != null) {  // Query has failed
                Snackbar.make(view, errorMessage, Snackbar.LENGTH_LONG).show();
            }
            else {  // Query has succeeded
                run.run();
            }
        });
    }
}
