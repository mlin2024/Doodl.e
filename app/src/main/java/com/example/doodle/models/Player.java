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

    // Methods I needed to add
    public ArrayList<String> getRootsContributedTo() {
        ArrayList<String> rootsContributedTo = new ArrayList<>();
        if (user.containsKey(KEY_ROOTS_CONTRIBUTED_TO)) {
            rootsContributedTo = (ArrayList) user.get(KEY_ROOTS_CONTRIBUTED_TO);
        }
        return rootsContributedTo;
    }

    public void addRootContributedTo(String root) {
        JSONArray rootsContributedTo = new JSONArray();

        if (user.containsKey(KEY_ROOTS_CONTRIBUTED_TO)) {
            ArrayList<String> rootsContributedToList = (ArrayList) user.get(KEY_ROOTS_CONTRIBUTED_TO);
            for (String s: rootsContributedToList) {
                rootsContributedTo.put(s);
            }
        }

        rootsContributedTo.put(root);
        user.put(KEY_ROOTS_CONTRIBUTED_TO, rootsContributedTo);
    }

    public void saveInBackground(View view) {
        user.saveInBackground(e -> {
            if (e != null) {
                Snackbar.make(view, R.string.error_saving_doodle, Snackbar.LENGTH_LONG).show();
            }
        });
    }
}
