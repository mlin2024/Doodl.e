package com.example.doodle.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.example.doodle.R;
import com.example.doodle.adapters.GameDoodleAdapter;
import com.example.doodle.models.Doodle;
import com.example.doodle.models.Game;
import com.google.android.material.snackbar.Snackbar;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;

public class GameGalleryActivity extends AppCompatActivity {
    public static final String TAG = "GameGalleryActivity";

    // Views in the layout
    private RelativeLayout gameGalleryRelativeLayout;
    private Toolbar toolbar;
    private RecyclerView gameGalleryRecyclerView;
    private Button homeButton;

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
        homeButton = findViewById(R.id.homeButton);

        // Initialize other member variables
        gameDoodles = new ArrayList<>();
        gameDoodleAdapter = new GameDoodleAdapter(this, gameDoodles);
        // Get game from intent
        game = getIntent().getParcelableExtra(GameModeActivity.TAG_GAME);
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

        homeButton.setOnClickListener(v -> {
            goHomeActivity();
            finish();
        });

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
            case R.id.profileMenuItem:
                goProfileActivity();
                finish();
                return true;
            case R.id.logoutMenuItem:
                logout();
                finish();
                return true;
            case android.R.id.home:
                leaveGame();
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        leaveGame();
        finish();
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
                Snackbar.make(gameGalleryRelativeLayout, getResources().getString(R.string.failed_to_load_doodles_from_game), Snackbar.LENGTH_LONG).show();
            }
            else { // Query has succeeded
                // Clear out old items before appending in the new ones
                gameDoodleAdapter.clear();
                // Save received posts to list and notify adapter of new data
                gameDoodleAdapter.addAll(foundDoodles);
            }
        });
    }

    private void leaveGame() {
        try {
            game.fetch();
            game.removePlayer(ParseUser.getCurrentUser());
            game.saveInBackground(e -> {
                if (e != null) { // Save has failed
                    Snackbar.make(gameGalleryRelativeLayout, getResources().getString(R.string.error_updating_game), Snackbar.LENGTH_LONG).show();
                }
                else { // Save has succeeded
                    // Once everyone has left, delete the game from the database
                    if (game.getPlayers().size() == 0) {
                        game.deleteInBackground();
                    }
                }
            });
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    // Starts an intent to go to the login/signup activity
    private void goLoginSignupActivity() {
        leaveGame();
        Intent intent = new Intent(this, LoginSignupActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    // Starts an intent to go to the profile activity
    private void goProfileActivity() {
        leaveGame();
        Intent intent = new Intent(this, ProfileActivity.class);
        startActivity(intent);
    }

    // Starts an intent to go to the home activity
    private void goHomeActivity() {
        leaveGame();
        Intent intent = new Intent(this, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    // Logs out user and sends them back to login/signup page
    private void logout() {
        leaveGame();
        ProgressDialog logoutProgressDialog = new ProgressDialog(GameGalleryActivity.this);
        logoutProgressDialog.setMessage(getResources().getString(R.string.logging_out));
        logoutProgressDialog.setCancelable(false);
        logoutProgressDialog.show();
        ParseUser.logOutInBackground(e -> {
            logoutProgressDialog.dismiss();
            if (e != null) { // Logout has failed
                Snackbar.make(gameGalleryRelativeLayout, getResources().getString(R.string.logout_failed), Snackbar.LENGTH_LONG).show();
            }
            else { // Logout has succeeded
                goLoginSignupActivity();
                finish();
            }
        });
    }
}