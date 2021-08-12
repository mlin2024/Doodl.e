package com.example.doodle.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.doodle.R;
import com.example.doodle.models.Player;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.parse.ParseUser;

public class ProfileActivity extends AppCompatActivity {
    public static final String TAG = "ProfileActivity";

    // Views in the layout
    private RelativeLayout profileRelativeLayout;
    private Toolbar toolbar;
    private TextView profileUsernameTextView;
    private SwitchMaterial notificationSwitch;
    private SwitchMaterial anonymousSwitch;
    private Button galleryButton;

    // Other necessary member variables
    Player curPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Initialize the views in the layout
        profileRelativeLayout = findViewById(R.id.profileRelativeLayout);
        toolbar = findViewById(R.id.profileToolbar);
        profileUsernameTextView = findViewById(R.id.profileUsernameTextView);
        notificationSwitch = findViewById(R.id.notificationSwitch);
        anonymousSwitch = findViewById(R.id.anonymousSwitch);
        galleryButton = findViewById(R.id.galleryButton);

        // Initialize other member variables
        curPlayer = new Player(ParseUser.getCurrentUser());

        // Set up toolbar
        toolbar.setTitleTextColor(getResources().getColor(R.color.white, getTheme()));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        // Set up username TextView
        profileUsernameTextView.setText(ParseUser.getCurrentUser().getUsername());

        // Set up notification switch
        // Set initial configuration
        if (curPlayer.getGetsNotifications()) notificationSwitch.setChecked(true);
        else notificationSwitch.setChecked(false);
        // Set up toggle logic
        notificationSwitch.setOnClickListener(v -> {
            if (notificationSwitch.isChecked()) {
                curPlayer.setGetsNotifications(true);
                curPlayer.saveInBackground(profileRelativeLayout, getResources().getString(R.string.failed_to_save_user_settings), () -> {
                    Snackbar.make(profileRelativeLayout, getResources().getString(R.string.you_will_receive_notifications), Snackbar.LENGTH_LONG).show();
                });
            }
            else {
                curPlayer.setGetsNotifications(false);
                curPlayer.saveInBackground(profileRelativeLayout, getResources().getString(R.string.failed_to_save_user_settings), () -> {
                    Snackbar.make(profileRelativeLayout, getResources().getString(R.string.you_will_not_receive_notifications), Snackbar.LENGTH_LONG).show();
                });
            }
        });

        // Set up anonymous switch
        // Set initial configuration
        if (curPlayer.getIsAnonymous()) anonymousSwitch.setChecked(true);
        else anonymousSwitch.setChecked(false);
        // Set up toggle logic
        anonymousSwitch.setOnClickListener(v -> {
            if (anonymousSwitch.isChecked()) {
                curPlayer.setIsAnonymous(true);
                curPlayer.saveInBackground(profileRelativeLayout, getResources().getString(R.string.failed_to_save_user_settings), () -> {
                    Snackbar.make(profileRelativeLayout, getResources().getString(R.string.you_will_be_anonymous), Snackbar.LENGTH_LONG).show();
                });
            }
            else {
                curPlayer.setIsAnonymous(false);
                curPlayer.saveInBackground(profileRelativeLayout, getResources().getString(R.string.failed_to_save_user_settings), () -> {
                    Snackbar.make(profileRelativeLayout, getResources().getString(R.string.you_will_not_be_anonymous), Snackbar.LENGTH_LONG).show();
                });
            }
        });

        galleryButton.setOnClickListener(v -> {
            goGalleryActivity();
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.profile_menu, menu);

        // Add username next to profile icon
        menu.findItem(R.id.username).setTitle(ParseUser.getCurrentUser().getUsername());
        // Make the username text unclickable
        menu.findItem(R.id.username).setEnabled(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
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

    // Starts an intent to go to the login/signup activity
    private void goLoginSignupActivity() {
        Intent intent = new Intent(this, LoginSignupActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    // Starts an intent to go to the gallery activity
    private void goGalleryActivity() {
        Intent intent = new Intent(this, GalleryActivity.class);
        startActivity(intent);
    }

    // Logs out user and sends them back to login/signup page
    private void logout() {
        ProgressDialog logoutProgressDialog = new ProgressDialog(ProfileActivity.this);
        logoutProgressDialog.setMessage(getResources().getString(R.string.logging_out));
        logoutProgressDialog.setCancelable(false);
        logoutProgressDialog.show();
        ParseUser.logOutInBackground(e -> {
            logoutProgressDialog.dismiss();
            if (e != null) { // Logout has failed
                Snackbar.make(profileRelativeLayout, getResources().getString(R.string.logout_failed), Snackbar.LENGTH_LONG).show();
            }
            else { // Logout has succeeded
                goLoginSignupActivity();
                finish();
            }
        });
    }
}