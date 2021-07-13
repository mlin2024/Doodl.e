package com.example.doodle.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.RelativeLayout;

import com.example.doodle.R;
import com.google.android.material.snackbar.Snackbar;
import com.parse.ParseUser;

public class HomeActivity extends AppCompatActivity {
    public static final String TAG = "HomeActivity";

    RelativeLayout homeRelativeLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        homeRelativeLayout = findViewById(R.id.homeRelativeLayout);
    }

    private void logout() {
        ParseUser.logOutInBackground(e -> {
            if (e != null) {
                Snackbar.make(homeRelativeLayout, R.string.logout_failed, Snackbar.LENGTH_LONG).show();
            }
            else {
                finish();
            }
        });
    }
}