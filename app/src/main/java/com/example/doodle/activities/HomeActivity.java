package com.example.doodle.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityOptionsCompat;
import androidx.transition.Transition;
import androidx.transition.TransitionInflater;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.doodle.R;
import com.google.android.material.snackbar.Snackbar;
import com.parse.ParseUser;

public class HomeActivity extends AppCompatActivity {
    public static final String TAG = "HomeActivity";

    // Views in the layout
    private RelativeLayout homeRelativeLayout;
    private Toolbar toolbar;
    private ImageView background;
    private TextView homeTitleTextView;
    private Button doodleModeButton;
    private Button gameModeButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Initialize the views in the layout
        homeRelativeLayout = findViewById(R.id.homeRelativeLayout);
        toolbar = findViewById(R.id.homeToolbar);
        background = findViewById(R.id.homeBackground);
        homeTitleTextView = findViewById(R.id.homeTitleTextView);
        doodleModeButton = findViewById(R.id.doodleModeButton);
        gameModeButton = findViewById(R.id.gameModeButton);

        // Set up toolbar
        toolbar.setTitleTextColor(getResources().getColor(R.color.white, getTheme()));
        setSupportActionBar(toolbar);

        // Set up background animation
        DisplayMetrics dm = getResources().getDisplayMetrics();
        float fwidth = dm.density * dm.widthPixels;
        float fheight = dm.density * dm.heightPixels;
        background.setScaleX(fwidth/getResources().getDimension(R.dimen.background_width));
        background.setScaleY(fheight/getResources().getDimension(R.dimen.background_height));

        // Set up title text
        SpannableString string = new SpannableString(getResources().getString(R.string.app_name));
        Drawable e = getResources().getDrawable(R.drawable.e_icon, getTheme());
        e.setBounds(0, 0,
                // Size the 'e' icon relative to the TextView's size and its own size
                (int)(homeTitleTextView.getTextSize() * 0.65 * (e.getIntrinsicWidth())/Math.max(e.getIntrinsicHeight(), e.getIntrinsicWidth())),
                (int)(homeTitleTextView.getTextSize() * 0.65 * (e.getIntrinsicHeight())/Math.max(e.getIntrinsicHeight(), e.getIntrinsicWidth())));
        int len = getResources().getString(R.string.app_name).length();
        string.setSpan(new ImageSpan(e, ImageSpan.ALIGN_BASELINE), len - 1, len, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        homeTitleTextView.setText(string);

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

    // Logs out user and sends them back to login/signup page
    private void logout() {
        ProgressDialog logoutProgressDialog = new ProgressDialog(HomeActivity.this);
        logoutProgressDialog.setMessage(getResources().getString(R.string.logging_out));
        logoutProgressDialog.setCancelable(false);
        logoutProgressDialog.show();
        ParseUser.logOutInBackground(e -> {
            logoutProgressDialog.dismiss();
            if (e != null) { // Logout has failed
                Snackbar.make(homeRelativeLayout, getResources().getString(R.string.logout_failed), Snackbar.LENGTH_LONG).show();
            }
            else { // Logout has succeeded
                goLoginSignupActivity();
                finish();
            }
        });
    }
}