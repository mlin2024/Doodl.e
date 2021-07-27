package com.example.doodle.models;

import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.doodle.R;
import com.google.android.material.snackbar.Snackbar;
import com.parse.GetCallback;
import com.parse.ParseClassName;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@ParseClassName("Doodle")
public class Doodle extends ParseObject implements Parcelable {
    public static final String TAG = "Doodle";
    public static final String KEY_ARTIST = "artist";
    public static final String KEY_IMAGE = "image";
    public static final String KEY_PARENT = "parent";
    public static final String KEY_TAIL_LENGTH = "tailLength";
    public static final String KEY_ROOT = "root";
    public static final String KEY_IN_GAME = "inGame";

    @NonNull
    @NotNull
    @Override
    public String toString() {
        return getObjectId();
    }

    public ParseUser getArtist() {
        return getParseUser(KEY_ARTIST);
    }

    public void setArtist(ParseUser user) {
        put(KEY_ARTIST, user);
    }

    public ParseFile getImage() {
        return getParseFile(KEY_IMAGE);
    }

    public void setImage(ParseFile image) {
        put(KEY_IMAGE, image);
    }

    public Doodle getParent() {
        return (Doodle) getParseObject(KEY_PARENT);
    }

    public void setParent(ParseObject parent) {
        put(KEY_PARENT, parent);
    }

    public int getTailLength() {
        return getInt(KEY_TAIL_LENGTH);
    }

    public void setTailLength(int tailLength) {
        put(KEY_TAIL_LENGTH, tailLength);
    }

    public String getRoot() {
        return getString(KEY_ROOT);
    }

    public void setRoot(String root) {
        put(KEY_ROOT, root);
    }

    public boolean getInGame() {
        return getBoolean(KEY_IN_GAME);
    }

    public void setInGame(boolean inGame) {
        put(KEY_IN_GAME, inGame);
    }

    public String getTimestamp() {
        Date createdAt = this.getCreatedAt();
        int SECOND_MILLIS = 1000;
        int MINUTE_MILLIS = 60 * SECOND_MILLIS;
        int HOUR_MILLIS = 60 * MINUTE_MILLIS;
        int DAY_MILLIS = 24 * HOUR_MILLIS;

        try {
            createdAt.getTime();
            long time = createdAt.getTime();
            long now = System.currentTimeMillis();

            final long diff = now - time;
            if (diff < MINUTE_MILLIS) {
                return "just now";
            } else if (diff < 2 * MINUTE_MILLIS) {
                return "a minute ago";
            } else if (diff < 50 * MINUTE_MILLIS) {
                return diff / MINUTE_MILLIS + "m";
            } else if (diff < 90 * MINUTE_MILLIS) {
                return "an hour ago";
            } else if (diff < 24 * HOUR_MILLIS) {
                return diff / HOUR_MILLIS + "h";
            } else if (diff < 48 * HOUR_MILLIS) {
                return "yesterday";
            } else {
                return diff / DAY_MILLIS + "d";
            }
        } catch (Exception e) {
            Log.i(TAG, "getTimestamp failed", e);
            e.printStackTrace();
        }

        return "";
    }
}
