package com.example.doodle.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewTreeObserver;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.example.doodle.R;
import com.google.android.material.snackbar.Snackbar;
import com.parse.ParseUser;


public class DoodleModeActivity extends AppCompatActivity {
    public static final String TAG = "DoodleModeActivity";

    // Views in the layout
    private RelativeLayout doodleModeRelativeLayout;
    private Toolbar toolbar;
    private ImageView background;
    private Button createDoodleButton;
    private Button contributeDoodleButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doodle_mode);

        // Initialize the views in the layout
        doodleModeRelativeLayout = findViewById(R.id.doodleModeRelativeLayout);
        toolbar = findViewById(R.id.doodleModeToolbar);
        background = findViewById(R.id.doodleModeBackground);
        createDoodleButton = findViewById(R.id.createDoodleButton);
        contributeDoodleButton = findViewById(R.id.contributeDoodleButton);

        // Set up toolbar
        toolbar.setTitleTextColor(getResources().getColor(R.color.white, getTheme()));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        // Set up background animation
        ViewTreeObserver vto = background.getViewTreeObserver();
        vto.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            public boolean onPreDraw() {
                background.getViewTreeObserver().removeOnPreDrawListener(this);
                int fheight = background.getMeasuredHeight();
                int fwidth = background.getMeasuredWidth();

                Drawable backgroundDrawable = getResources().getDrawable(R.drawable.background, getTheme());
                backgroundDrawable.setBounds(0, 0, fwidth * 2, fheight * 2);
                background.setImageDrawable(backgroundDrawable);
                background.setScaleX(2);
                background.setScaleY(2);
                background.setPivotX(0);
                background.setPivotY(0);

                TranslateAnimation anim = new TranslateAnimation(
                        TranslateAnimation.ABSOLUTE, 0.0f,
                        TranslateAnimation.ABSOLUTE, -fwidth,
                        TranslateAnimation.ABSOLUTE, 0.0f,
                        TranslateAnimation.ABSOLUTE, -fheight
                );
                anim.setFillAfter(true);
                anim.setDuration(getResources().getInteger(R.integer.background_scroll_speed));
                anim.setRepeatCount(-1);
                anim.setInterpolator(new LinearInterpolator());

                background.startAnimation(anim);

                return true;
            }
        });

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

    // Starts an intent to go to the profile activity
    private void goProfileActivity() {
        Intent intent = new Intent(this, ProfileActivity.class);
        startActivity(intent);
    }

    // Starts an intent to go to the doodle activity
    private void goDoodleActivity() {
        Intent intent = new Intent(this, DoodleActivity.class);
        // Don't pass in anything for ParentDoodle, it has no parent since it is a new doodle
        startActivity(intent);
    }

    // Starts an intent to go to the contribute activity
    private void goContributeActivity() {
        Intent intent = new Intent(this, ContributeActivity.class);
        startActivity(intent);
    }

    // Logs out user and sends them back to login/signup page
    private void logout() {
        ProgressDialog logoutProgressDialog = new ProgressDialog(DoodleModeActivity.this);
        logoutProgressDialog.setMessage(getResources().getString(R.string.logging_out));
        logoutProgressDialog.setCancelable(false);
        logoutProgressDialog.show();
        ParseUser.logOutInBackground(e -> {
            logoutProgressDialog.dismiss();
            if (e != null) { // Logout has failed
                Snackbar.make(doodleModeRelativeLayout, getResources().getString(R.string.logout_failed), Snackbar.LENGTH_LONG).show();
            }
            else { // Logout has succeeded
                goLoginSignupActivity();
                finish();
            }
        });
    }
}