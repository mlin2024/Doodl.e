package com.example.doodle.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
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

import com.example.doodle.R;
import com.example.doodle.adapters.DoodleAdapter;
import com.example.doodle.adapters.GameDoodleAdapter;
import com.example.doodle.models.Doodle;
import com.example.doodle.models.Game;
import com.google.android.material.snackbar.Snackbar;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

public class GameGalleryActivity extends AppCompatActivity {
    public static final String TAG = "GameGalleryActivity";

    // Views in the layout
    private RelativeLayout gameGalleryRelativeLayout;
    private Toolbar toolbar;
    private RecyclerView gameGalleryRecyclerView;

    // Other necessary member variables
    private ArrayList<Doodle> gameDoodles;
    private GameDoodleAdapter gameDoodleAdapter;
    private Game game;
    private ProgressDialog loadingProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_gallery);

        // Initialize the views in the layout
        gameGalleryRelativeLayout = findViewById(R.id.gameGalleryRelativeLayout);
        toolbar = findViewById(R.id.gameGalleryToolbar);
        gameGalleryRecyclerView = findViewById(R.id.gameGalleryRecyclerView);

        // Initialize other member variables
        gameDoodles = new ArrayList<>();
        gameDoodleAdapter = new GameDoodleAdapter(this, gameDoodles);
        // Get game from intent
        game = getIntent().getParcelableExtra(GameModeActivity.GAME_TAG);
        loadingProgressDialog = new ProgressDialog(GameGalleryActivity.this);

        // Set up toolbar
        toolbar.setTitleTextColor(getResources().getColor(R.color.white, getTheme()));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        // Set up gallery RecyclerView
        // This allows for optimizations
        gameGalleryRecyclerView.setHasFixedSize(true);
        // Define a single column layout with new linearLayoutManager
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        gameGalleryRecyclerView.setLayoutManager(linearLayoutManager);
        // Set adapter
        gameGalleryRecyclerView.setAdapter(gameDoodleAdapter);
        ViewCompat.setNestedScrollingEnabled(gameGalleryRecyclerView, false);

        // Set up ProgressDialog
        loadingProgressDialog.setMessage(getResources().getString(R.string.loading_doodles));
        loadingProgressDialog.setCancelable(false);

        // Grab doodles to populate the RecyclerView
        findGameDoodles();
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
            case R.id.logoutMenuItem:
                logout();
                return true;
            case android.R.id.home:
                goHomeActivity();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        goHomeActivity();
    }

    private void findGameDoodles() {
        // Specify what type of data we want to query - Doodle.class
        ParseQuery<Doodle> query = ParseQuery.getQuery(Doodle.class);
        // Include only doodles from this game
        query.whereEqualTo(Doodle.KEY_IN_GAME, game.getObjectId());
        // Include only original doodles (with a tail length of 1)
        query.whereEqualTo(Doodle.KEY_TAIL_LENGTH, 1);

        loadingProgressDialog.show();
        // Start an asynchronous call for doodles
        query.findInBackground((foundDoodles, e) -> {
            loadingProgressDialog.dismiss();
            if (e != null) { // Query has failed
                Snackbar.make(gameGalleryRelativeLayout, R.string.failed_to_load_doodles_from_game, Snackbar.LENGTH_LONG).show();
                return;
            }
            else { // Query has succeeded
                // Clear out old items before appending in the new ones
                gameDoodleAdapter.clear();
                // Save received posts to list and notify adapter of new data
                gameDoodleAdapter.addAll(foundDoodles);
            }
        });
    }

    // Starts an intent to go to the login/signup activity
    private void goLoginSignupActivity() {
        Intent intent = new Intent(this, LoginSignupActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    // Starts an intent to go to the home activity
    private void goHomeActivity() {
        Intent intent = new Intent(this, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    // Logs out user and sends them back to login/signup page
    private void logout() {
        ProgressDialog logoutProgressDialog = new ProgressDialog(GameGalleryActivity.this);
        logoutProgressDialog.setMessage(getResources().getString(R.string.logging_out));
        logoutProgressDialog.setCancelable(false);
        logoutProgressDialog.show();
        ParseUser.logOutInBackground(e -> {
            logoutProgressDialog.dismiss();
            if (e != null) {
                Snackbar.make(gameGalleryRelativeLayout, R.string.logout_failed, Snackbar.LENGTH_LONG).show();
            }
            else {
                goLoginSignupActivity();
                finish();
            }
        });
    }
}