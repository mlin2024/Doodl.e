package com.example.doodle.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager2.widget.ViewPager2;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.example.doodle.R;
import com.example.doodle.adapters.DoodleAdapter;
import com.example.doodle.models.Doodle;
import com.google.android.material.snackbar.Snackbar;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ContributeActivity extends AppCompatActivity {
    public static final String TAG = "ContributeActivity";
    public static final int NUM_TO_LOAD = 10;

    private RelativeLayout contributeRelativeLayout;
    private Toolbar toolbar;
    private ViewPager2 selectViewPager;
    private Button selectButton;

    private List<Doodle> doodles;
    private DoodleAdapter doodleAdapter;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contribute);

        contributeRelativeLayout = findViewById(R.id.contributeRelativeLayout);
        toolbar = findViewById(R.id.contributeToolbar);
        toolbar.setTitleTextColor(getResources().getColor(R.color.white));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        selectViewPager = findViewById(R.id.selectViewPager);
        selectButton = findViewById(R.id.selectButton);

        doodles = new ArrayList<>();
        doodleAdapter = new DoodleAdapter(this, doodles, true);
        selectViewPager.setAdapter(doodleAdapter);

        progressDialog = new ProgressDialog(ContributeActivity.this);
        progressDialog.setMessage(getResources().getString(R.string.loading_doodles));

        // Grab doodles to populate the ViewPager
        queryDoodles();

        selectButton.setOnClickListener(v -> {
            Doodle parentDoodle = doodles.get(selectViewPager.getCurrentItem());
            goDoodleActivity(parentDoodle);
            finish();
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
        ProgressDialog logoutProgressDialog = new ProgressDialog(ContributeActivity.this);
        logoutProgressDialog.setMessage(getResources().getString(R.string.logging_out));
        logoutProgressDialog.show();
        ParseUser.logOutInBackground(e -> {
            logoutProgressDialog.dismiss();
            if (e != null) {
                Snackbar.make(contributeRelativeLayout, R.string.logout_failed, Snackbar.LENGTH_LONG).show();
            }
            else {
                goLoginSignupActivity();
                finish();
            }
        });
    }

    private void queryDoodles() {
        // Specify what type of data we want to query - Doodle.class
        ParseQuery<Doodle> query = ParseQuery.getQuery(Doodle.class);
        // Don't include doodles with the current user as the artist
        query.whereNotEqualTo(Doodle.KEY_ARTIST, ParseUser.getCurrentUser());
        // TODO: don't include doodles whose root is included in the list of doodles the user has contributed to
        // query.whereNotContainedIn(Doodle.KEY_ROOT, ParseUser.getCurrentUser().getContributedTo());
        // Limit query to NUM_TO_LOAD items
        query.setLimit(NUM_TO_LOAD);

        progressDialog.show();
        // Start an asynchronous call for doodles
        query.findInBackground((foundDoodles, e) -> {
            progressDialog.dismiss();
            if (e != null) { // Query has failed
                Snackbar.make(contributeRelativeLayout, R.string.failed_to_load_doodles, Snackbar.LENGTH_LONG).show();
                return;
            }
            else { // Query has succeeded
                // Clear out old items before appending in the new ones
                doodleAdapter.clear();
                // Save received posts to list and notify adapter of new data
                doodleAdapter.addAll(foundDoodles);
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
    private void goDoodleActivity(Doodle parentDoodle) {
        Intent intent = new Intent(this, DoodleActivity.class);
        // Pass the parent doodle ID
        intent.putExtra(DoodleActivity.PARENT_DOODLE_ID, (String) parentDoodle.getObjectId());
        // Don't pass in anything for inGame, it defaults to false
        startActivity(intent);
    }
}