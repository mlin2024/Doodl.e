package com.example.doodle.models;

import android.util.Log;
import android.view.View;

import com.google.android.material.snackbar.Snackbar;
import com.parse.ParseUser;

import org.json.JSONArray;

import java.util.ArrayList;

// Extending the ParseUser class is not recommended, so I am doing this instead
public class Player {
    public static final String TAG = "Player";
    public static final String KEY_GETS_NOTIFICATIONS = "getsNotifications";
    public static final String KEY_IS_ANONYMOUS = "isAnonymous";
    public static final String KEY_ROOTS_CONTRIBUTED_TO = "rootsContributedTo";

    private ParseUser user;

    public Player(ParseUser user) {
        this.user = user;
    }

    public String getObjectId() {
        return user.getObjectId();
    }

    public String getUsername() {
        return user.getUsername();
    }

    public boolean getGetsNotifications() {
        return user.getBoolean(KEY_GETS_NOTIFICATIONS);
    }

    public void setGetsNotifications(boolean getsNotifications) {
        user.put(KEY_GETS_NOTIFICATIONS, getsNotifications);
    }

    public boolean getIsAnonymous() {
        return user.getBoolean(KEY_IS_ANONYMOUS);
    }

    public void setIsAnonymous(boolean isAnonymous) {
        user.put(KEY_IS_ANONYMOUS, isAnonymous);
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
    public void saveInBackground(View view, String errorMessage, Runnable run) {
        user.saveInBackground(e -> {
            if (e != null) { // Save has failed
                Snackbar.make(view, errorMessage, Snackbar.LENGTH_LONG).show();
            }
            else { // Save has succeeded
                run.run();
            }
        });
    }
}
