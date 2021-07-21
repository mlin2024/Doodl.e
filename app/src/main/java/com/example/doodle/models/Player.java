package com.example.doodle.models;

import android.util.Log;
import android.view.View;

import com.example.doodle.R;
import com.google.android.material.snackbar.Snackbar;
import com.parse.LogOutCallback;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.json.JSONArray;

import java.util.ArrayList;

// Extending the ParseUser class is not recommended, so I am doing this instead
public class Player {
    public static final String TAG = "Player";
    public static final String KEY_ROOTS_CONTRIBUTED_TO = "rootsContributedTo";

    private ParseUser user;

    public Player(ParseUser user) {
        this.user = user;
    }

    // Gets the list of roots the user has contributed to
    public ArrayList<String> getRootsContributedTo() {
        ArrayList<String> rootsContributedTo = new ArrayList<>();
        if (user.containsKey(KEY_ROOTS_CONTRIBUTED_TO)) {
            rootsContributedTo = (ArrayList) user.get(KEY_ROOTS_CONTRIBUTED_TO);
        }
        return rootsContributedTo;
    }

    // Adds a root to the list of roots the user has contributed to
    public void addRootContributedTo(String root) {
        JSONArray rootsContributedTo = new JSONArray();

        // If the user already has a list of roots they contributed to, add all those roots to the JSON array
        if (user.containsKey(KEY_ROOTS_CONTRIBUTED_TO)) {
            ArrayList<String> rootsContributedToList = (ArrayList) user.get(KEY_ROOTS_CONTRIBUTED_TO);
            // The "get" method returns an ArrayList, so manually add all the elements in the ArrayList to the JSON array
            for (String s: rootsContributedToList) {
                rootsContributedTo.put(s);
            }
        }

        // Add the new root to the JSON array
        rootsContributedTo.put(root);

        // Assign this JSON array as the user's list of roots they've contributed to
        user.put(KEY_ROOTS_CONTRIBUTED_TO, rootsContributedTo);
    }

    // Asynchronously saves the user data to the database
    public void saveInBackground(View view) {
        user.saveInBackground(e -> {
            if (e != null) {
                Snackbar.make(view, R.string.error_saving_doodle, Snackbar.LENGTH_LONG).show();
            }
        });
    }
}
