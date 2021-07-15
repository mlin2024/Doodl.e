package com.example.doodle.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.example.doodle.R;
import com.example.doodle.models.Doodle;
import com.google.android.material.snackbar.Snackbar;
import com.parse.ParseUser;

import java.io.Serializable;

public class DoodleModeActivity extends AppCompatActivity {
    public static final String TAG = "DoodleModeActivity";

    private RelativeLayout doodleModeRelativeLayout;
    private Toolbar toolbar;
    private Button createDoodleButton;
    private Button contributeDoodleButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doodle_mode);

        doodleModeRelativeLayout = findViewById(R.id.doodleModeRelativeLayout);
        toolbar = findViewById(R.id.doodleModeToolbar);
        toolbar.setTitleTextColor(getResources().getColor(R.color.white));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        createDoodleButton = findViewById(R.id.createDoodleButton);
        contributeDoodleButton = findViewById(R.id.contributeDoodleButton);

        createDoodleButton.setOnClickListener(v -> {
            goDoodleActivity();
        });

        contributeDoodleButton.setOnClickListener(v -> {
            goContributeActivity();
        });
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
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    private void logout() {
        ProgressDialog logoutProgressDialog = new ProgressDialog(DoodleModeActivity.this);
        logoutProgressDialog.setMessage(getResources().getString(R.string.logging_out));
        logoutProgressDialog.show();
        ParseUser.logOutInBackground(e -> {
            logoutProgressDialog.dismiss();
            if (e != null) {
                Snackbar.make(doodleModeRelativeLayout, R.string.logout_failed, Snackbar.LENGTH_LONG).show();
            }
            else {
                goLoginSignupActivity();
                finish();
            }
        });
    }

    // Starts an intent to go to the login/signup activity
    private void goLoginSignupActivity() {
        Intent intent = new Intent(this, LoginSignupActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    // Starts an intent to go to the profile activity
    private void goProfileActivity() {
        Intent intent = new Intent(this, ProfileActivity.class);
        startActivity(intent);
    }

    // Starts an intent to go to the doodle activity
    private void goDoodleActivity() {
        Intent intent = new Intent(this, DoodleActivity.class);
        // Don't pass in anything for ParentDoodleId, it has no parent since it is a new doodle
        // Don't pass in anything for inGame, it defaults to false
        startActivity(intent);
    }

    // Starts an intent to go to the contribute activity
    private void goContributeActivity() {
        Intent intent = new Intent(this, ContributeActivity.class);
        startActivity(intent);
    }
}