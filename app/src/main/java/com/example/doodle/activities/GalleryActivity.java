package com.example.doodle.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.doodle.R;
import com.example.doodle.adapters.DoodleAdapter;
import com.example.doodle.fragments.DoodleDetailsFragment;
import com.example.doodle.models.Doodle;
import com.google.android.material.snackbar.Snackbar;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

public class GalleryActivity extends AppCompatActivity {
    public static final String TAG = "GalleryActivity";

    private RelativeLayout galleryRelativeLayout;
    private Toolbar toolbar;
    private RecyclerView galleryRecyclerView;
    private TextView nothingHereYet;

    private List<Doodle> doodles;
    private DoodleAdapter doodleAdapter;
    private ProgressDialog progressDialog;
    public static FragmentManager fragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        GridLayoutManager gridLayoutManager = new GridLayoutManager(GalleryActivity.this, 2);
        galleryRelativeLayout = findViewById(R.id.galleryRelativeLayout);
        toolbar = findViewById(R.id.galleryToolbar);
        toolbar.setTitleTextColor(getResources().getColor(R.color.white));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        galleryRecyclerView = findViewById(R.id.galleryRecyclerView);
        nothingHereYet = findViewById(R.id.nothingHereYet);

        doodles = new ArrayList<>();
        doodleAdapter = new DoodleAdapter(this, doodles);
        galleryRecyclerView.setAdapter(doodleAdapter);
        fragmentManager = getSupportFragmentManager();

        // Allows for optimizations
        galleryRecyclerView.setHasFixedSize(true);

        // Define 2 column grid layout with a new GridLayoutManager
        galleryRecyclerView.setLayoutManager(gridLayoutManager);

        progressDialog = new ProgressDialog(GalleryActivity.this);
        progressDialog.setMessage(getResources().getString(R.string.loading_gallery));

        // Grab doodles
        queryDoodles();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.profile_menu, menu);
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

    private void logout() {
        ParseUser.logOutInBackground(e -> {
            if (e != null) {
                Snackbar.make(galleryRelativeLayout, R.string.logout_failed, Snackbar.LENGTH_LONG).show();
            }
            else {
                goLoginSignupActivity();
                finish();
            }
        });
    }

    private void queryDoodles() {
        // specify what type of data we want to query - Post.class
        ParseQuery<Doodle> query = ParseQuery.getQuery(Doodle.class);
        // include data referred by user key
        query.include(Doodle.KEY_ARTIST);
        // order posts by creation date (newest first)
        query.addDescendingOrder("createdAt");

        progressDialog.show();
        // start an asynchronous call for posts
        query.findInBackground((foundDoodles, e) -> {
            progressDialog.dismiss();
            if (e != null) { // Query has failed
                Snackbar.make(galleryRelativeLayout, R.string.failed_to_load_gallery, Snackbar.LENGTH_LONG).show();
                return;
            }
            else {
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
}