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
import android.widget.TextView;

import com.example.doodle.R;
import com.example.doodle.adapters.DoodleAdapter;
import com.example.doodle.models.Doodle;
import com.example.doodle.models.Player;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

public class ContributeActivity extends AppCompatActivity {
    public static final String TAG = "ContributeActivity";
    public static final int NUM_TO_LOAD = 10;

    // Views in the layout
    private RelativeLayout contributeRelativeLayout;
    private Toolbar toolbar;
    private TextView noDoodlesToContributeTo;
    private ViewPager2 selectViewPager;
    private TabLayout selectTabLayout;
    private Button selectButton;

    // Other necessary member variables
    private List<Doodle> doodles;
    private DoodleAdapter doodleAdapter;
    private ProgressDialog loadingProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contribute);

        // Initialize the views in the layout
        contributeRelativeLayout = findViewById(R.id.contributeRelativeLayout);
        toolbar = findViewById(R.id.contributeToolbar);
        noDoodlesToContributeTo = findViewById(R.id.noDoodlesToContributeTo);
        selectViewPager = findViewById(R.id.selectViewPager);
        selectTabLayout = findViewById(R.id.selectTabLayout);
        selectButton = findViewById(R.id.selectButton);

        // Initialize other member variables
        doodles = new ArrayList<>();
        doodleAdapter = new DoodleAdapter(this, doodles, true);
        loadingProgressDialog = new ProgressDialog(ContributeActivity.this);

        // Set up toolbar
        toolbar.setTitleTextColor(getResources().getColor(R.color.white, getTheme()));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        // Set up ViewPager
        selectViewPager.setAdapter(doodleAdapter);

        // Set up ProgressDialog
        loadingProgressDialog.setMessage(getResources().getString(R.string.loading_doodles));
        loadingProgressDialog.setCancelable(false);

        // Attach the TabLayout to the ViewPager
        new TabLayoutMediator(selectTabLayout, selectViewPager, true, true, (tab, position) -> {}).attach();

        // Grab doodles to populate the ViewPager
        findContributableDoodles();

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

    // Finds the oldest 10 doodles the user has never contributed to before
    private void findContributableDoodles() {
        // Specify what type of data we want to query - Doodle.class
        ParseQuery<Doodle> query = ParseQuery.getQuery(Doodle.class);
        // Don't include doodles with the current user as the artist
        query.whereNotEqualTo(Doodle.KEY_ARTIST, ParseUser.getCurrentUser());
        // Don't include doodles that the current user has already edited an ancestor of
        Player player = new Player(ParseUser.getCurrentUser());
        query.whereNotContainedIn(Doodle.KEY_ROOT, player.getRootsContributedTo());
        // Don't include doodles with a tail longer than 4
        query.whereLessThanOrEqualTo(Doodle.KEY_TAIL_LENGTH, 4);
        // TODO: figure out how to not include doodles with more than 1 sibling
        // Limit query to NUM_TO_LOAD items
        query.setLimit(NUM_TO_LOAD);
        // Order doodles by creation date (oldest first)
        query.addAscendingOrder("createdAt");

        loadingProgressDialog.show();
        // Start an asynchronous call for doodles
        query.findInBackground((foundDoodles, e) -> {
            loadingProgressDialog.dismiss();
            if (e != null) { // Query has failed
                Snackbar.make(contributeRelativeLayout, R.string.failed_to_load_doodles, Snackbar.LENGTH_LONG).show();
                return;
            }
            else { // Query has succeeded
                // Clear out old items before appending in the new ones
                doodleAdapter.clear();
                // Save received posts to list and notify adapter of new data
                doodleAdapter.addAll(foundDoodles);
                // Show empty message if gallery is empty
                if (doodles.size() == 0) {
                    noDoodlesToContributeTo.setVisibility(View.VISIBLE);
                    selectButton.setEnabled(false);
                }
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
        // Pass the parent doodle
        intent.putExtra(DoodleActivity.PARENT_DOODLE, parentDoodle);
        // Don't pass in anything for inGame, it defaults to false
        startActivity(intent);
    }

    // Logs out user and sends them back to login/signup page
    private void logout() {
        ProgressDialog logoutProgressDialog = new ProgressDialog(ContributeActivity.this);
        logoutProgressDialog.setMessage(getResources().getString(R.string.logging_out));
        logoutProgressDialog.setCancelable(false);
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
}