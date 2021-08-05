package com.example.doodle.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.doodle.R;
import com.example.doodle.adapters.DoodleAdapter;
import com.example.doodle.models.Doodle;
import com.google.android.material.snackbar.Snackbar;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

public class GalleryActivity extends AppCompatActivity {
    public static final String TAG = "GalleryActivity";
    public static final String CURRENT_DOODLE = "currentDoodle";

    // Views in the layout
    private RelativeLayout galleryRelativeLayout;
    private Toolbar toolbar;
    private RecyclerView galleryRecyclerView;
    private TextView nothingHereYet;

    // Other necessary member variables
    private ArrayList<Doodle> doodles;
    private DoodleAdapter doodleAdapter;
    private ProgressDialog loadingProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        // Initialize the views in the layout
        galleryRelativeLayout = findViewById(R.id.galleryRelativeLayout);
        toolbar = findViewById(R.id.galleryToolbar);
        galleryRecyclerView = findViewById(R.id.galleryRecyclerView);
        nothingHereYet = findViewById(R.id.nothingHereYet);

        // Initialize other member variables
        doodles = new ArrayList<>();
        doodleAdapter = new DoodleAdapter(this, doodles, false, true);
        loadingProgressDialog = new ProgressDialog(GalleryActivity.this);

        // Set up toolbar
        toolbar.setTitleTextColor(getResources().getColor(R.color.white, getTheme()));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        // Set up gallery RecyclerView
        // This allows for optimizations
        galleryRecyclerView.setHasFixedSize(true);
        // Define 2 column grid layout with a new GridLayoutManager
        GridLayoutManager gridLayoutManager = new GridLayoutManager(GalleryActivity.this, 2);
        galleryRecyclerView.setLayoutManager(gridLayoutManager);
        // Set adapter
        galleryRecyclerView.setAdapter(doodleAdapter);

        // Set up ProgressDialog
        loadingProgressDialog.setMessage(getResources().getString(R.string.loading_gallery));
        loadingProgressDialog.setCancelable(false);

        // Grab doodles to populate the RecyclerView
        findDoodlesByCurrentUser();
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

    // Finds all the doodles created by the current user
    private void findDoodlesByCurrentUser() {
        // Specify what type of data we want to query - Doodle.class
        ParseQuery<Doodle> query = ParseQuery.getQuery(Doodle.class);
        // Include only doodles by the current user
        query.whereEqualTo(Doodle.KEY_ARTIST, ParseUser.getCurrentUser());
        // Order doodles by creation date (newest first)
        query.addDescendingOrder("createdAt");

        loadingProgressDialog.show();
        // Start an asynchronous call for doodles
        query.findInBackground((foundDoodles, e) -> {
            loadingProgressDialog.dismiss();
            if (e != null) { // Query has failed
                Snackbar.make(galleryRelativeLayout, getResources().getString(R.string.failed_to_load_gallery), Snackbar.LENGTH_LONG).show();
                return;
            }
            else { // Query has succeeded
                // Clear out old items before appending in the new ones
                doodleAdapter.clear();
                // Save received posts to list and notify adapter of new data
                doodleAdapter.addAll(foundDoodles);
                // Show empty message if gallery is empty
                if (doodles.size() == 0) nothingHereYet.setVisibility(View.VISIBLE);
            }
        });
    }

    // Starts an intent to go to the login/signup activity
    private void goLoginSignupActivity() {
        Intent intent = new Intent(this, LoginSignupActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    // Logs out user and sends them back to login/signup page
    private void logout() {
        ProgressDialog logoutProgressDialog = new ProgressDialog(GalleryActivity.this);
        logoutProgressDialog.setMessage(getResources().getString(R.string.logging_out));
        logoutProgressDialog.setCancelable(false);
        logoutProgressDialog.show();
        ParseUser.logOutInBackground(e -> {
            logoutProgressDialog.dismiss();
            if (e != null) {  // Logout has failed
                Snackbar.make(galleryRelativeLayout, getResources().getString(R.string.logout_failed), Snackbar.LENGTH_LONG).show();
            }
            else { // Logout has succeeded
                goLoginSignupActivity();
                finish();
            }
        });
    }
}