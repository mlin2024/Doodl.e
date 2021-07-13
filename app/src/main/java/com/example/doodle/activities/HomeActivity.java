package com.example.doodle.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.example.doodle.R;
import com.google.android.material.snackbar.Snackbar;
import com.parse.ParseUser;

public class HomeActivity extends AppCompatActivity {
    public static final String TAG = "HomeActivity";

    RelativeLayout homeRelativeLayout;
    private Toolbar toolbar;
    private Button doodleModeButton;
    private Button gameModeButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        homeRelativeLayout = findViewById(R.id.homeRelativeLayout);
        toolbar = findViewById(R.id.homeActivityToolbar);
        toolbar.setTitleTextColor(getResources().getColor(R.color.white));
        setSupportActionBar(toolbar);
        doodleModeButton = findViewById(R.id.doodleModeButton);
        gameModeButton = findViewById(R.id.gameModeButton);

        doodleModeButton.setOnClickListener(v -> {
            goDoodleModeActivity();
        });

        gameModeButton.setOnClickListener(v -> {
            goGameModeActivity();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (ParseUser.getCurrentUser() == null) {
            finish();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.profileMenuItem:
                goProfileActivity();
                return true;
            case R.id.logoutMenuItem:
                logout();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
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

    // Starts an intent to go to the profile activity
    private void goProfileActivity() {
        Intent intent = new Intent(this, ProfileActivity.class);
        startActivity(intent);
    }

    // Starts an intent to go to the doodle mode activity
    private void goDoodleModeActivity() {
        Intent intent = new Intent(this, DoodleModeActivity.class);
        startActivity(intent);
    }

    // Starts an intent to go to the game mode activity
    private void goGameModeActivity() {
        Intent intent = new Intent(this, GameModeActivity.class);
        startActivity(intent);
    }
}