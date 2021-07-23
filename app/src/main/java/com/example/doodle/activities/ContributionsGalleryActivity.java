package com.example.doodle.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.doodle.R;
import com.example.doodle.adapters.DoodleAdapter;
import com.example.doodle.models.Doodle;
import com.google.android.material.snackbar.Snackbar;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

public class ContributionsGalleryActivity extends AppCompatActivity {
    public static final String TAG = "ContributionsGalleryActivity";
    public static final String ORIGINAL_DOODLE = "originalDoodle";

    // Views in the layout
    private RelativeLayout contributionsGalleryRelativeLayout;
    private Toolbar toolbar;
    private ImageView originalDoodleImageView;
    private RecyclerView contributionsGalleryRecyclerView;
    private TextView noContributionsYet;

    // Other necessary member variables
    private List<Doodle> contributions;
    private DoodleAdapter doodleAdapter;
    private ProgressDialog loadingProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contributions_gallery);

        // Initialize the views in the layout
        contributionsGalleryRelativeLayout = findViewById(R.id.contributionsGalleryRelativeLayout);
        toolbar = findViewById(R.id.contributionsGalleryToolbar);
        originalDoodleImageView = findViewById(R.id.originalDoodleImageView);
        contributionsGalleryRecyclerView = findViewById(R.id.contributionsGalleryRecyclerView);
        noContributionsYet = findViewById(R.id.noContributionsYet);

        // Initialize other member variables
        contributions = new ArrayList<>();
        doodleAdapter = new DoodleAdapter(this, contributions, false);
        loadingProgressDialog = new ProgressDialog(ContributionsGalleryActivity.this);

        // Set up toolbar
        toolbar.setTitleTextColor(getResources().getColor(R.color.white, getTheme()));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        // Get current doodle from intent
        Doodle currentDoodle = getIntent().getParcelableExtra(ORIGINAL_DOODLE);

        // Set up ImageView
        Glide.with(this)
                .load(currentDoodle.getImage().getUrl())
                .placeholder(R.drawable.placeholder)
                .into(originalDoodleImageView);

        // This allows for optimizations
        contributionsGalleryRecyclerView.setHasFixedSize(true);

        // Define 2 column grid layout with a new GridLayoutManager
        GridLayoutManager gridLayoutManager = new GridLayoutManager(ContributionsGalleryActivity.this, 2);
        contributionsGalleryRecyclerView.setLayoutManager(gridLayoutManager);

        // Set up RecyclerView
        contributionsGalleryRecyclerView.setAdapter(doodleAdapter);

        // Set up ProgressDialog
        loadingProgressDialog.setMessage(getResources().getString(R.string.loading_contributions));
        loadingProgressDialog.setCancelable(false);

        // Grab doodles to populate the RecyclerView
        findContributions(currentDoodle);
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

    private void findContributions(Doodle currentDoodle) {
        // Specify what type of data we want to query - Doodle.class
        ParseQuery<Doodle> query = ParseQuery.getQuery(Doodle.class);
        // Include doodles with the current doodle as the parent
        query.whereEqualTo(Doodle.KEY_PARENT, currentDoodle);
        // Order doodles by creation date (newest first)
        query.addDescendingOrder("createdAt");

        loadingProgressDialog.show();
        // Start an asynchronous call for doodles
        query.findInBackground((foundDoodles, e) -> {
            loadingProgressDialog.dismiss();
            if (e != null) { // Query has failed
                Snackbar.make(contributionsGalleryRelativeLayout, R.string.failed_to_load_gallery, Snackbar.LENGTH_LONG).show();
                return;
            }
            else { // Query has succeeded
                // Clear out old items before appending in the new ones
                doodleAdapter.clear();
                // Save received posts to list and notify adapter of new data
                doodleAdapter.addAll(foundDoodles);
                // Show empty message if gallery is empty
                if (contributions.size() == 0) noContributionsYet.setVisibility(View.VISIBLE);
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
        ProgressDialog logoutProgressDialog = new ProgressDialog(ContributionsGalleryActivity.this);
        logoutProgressDialog.setMessage(getResources().getString(R.string.logging_out));
        logoutProgressDialog.setCancelable(false);
        logoutProgressDialog.show();
        ParseUser.logOutInBackground(e -> {
            logoutProgressDialog.dismiss();
            if (e != null) {
                Snackbar.make(contributionsGalleryRelativeLayout, R.string.logout_failed, Snackbar.LENGTH_LONG).show();
            }
            else {
                goLoginSignupActivity();
                finish();
            }
        });
    }
}