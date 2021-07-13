package com.example.doodle.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.doodle.R;
import com.example.doodle.models.Player;
import com.google.android.material.snackbar.Snackbar;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

public class WaitingRoomActivity extends AppCompatActivity {
    public static final String TAG = "WaitingRoomActivity";

    private RelativeLayout waitingRoomRelativeLayout;
    private Toolbar toolbar;
    private TextView gameCodeTextViewWR;
    private RecyclerView playersRecyclerView;
    private TextView numPlayersTextView;
    private Button startGameButton;

    private String gameCode;
    private List<Player> players;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_waiting_room);

        waitingRoomRelativeLayout = findViewById(R.id.waitingRoomRelativeLayout);
        toolbar = findViewById(R.id.waitingRoomActivityToolbar);
        toolbar.setTitleTextColor(getResources().getColor(R.color.white));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        gameCodeTextViewWR = findViewById(R.id.gameCodeTextViewWR);
        playersRecyclerView = findViewById(R.id.playersRecyclerView);
        numPlayersTextView = findViewById(R.id.numPlayersTextView);
        numPlayersTextView.setText(getResources().getString(R.string.Players) + " 0");
        startGameButton = findViewById(R.id.startGameButton);

        players = new ArrayList<>();

        // Unwrap the game code that was passed in by the intent
        gameCode = (String) getIntent().getExtras().getSerializable(GameModeActivity.GAME_CODE_TAG);
        gameCodeTextViewWR.setText(gameCode);

        // TODO: repeat this periodically
        refreshPlayers();

        startGameButton.setOnClickListener(v -> {
            goGameActivity();
        });
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
        ParseUser.logOutInBackground(e -> {
            if (e != null) {
                Snackbar.make(waitingRoomRelativeLayout, R.string.logout_failed, Snackbar.LENGTH_LONG).show();
            }
            else {
                finish();
            }
        });
    }

    // Refreshes the player list
    private void refreshPlayers() {
        numPlayersTextView.setText(getResources().getString(R.string.Players) + " " + players.size());
    }

    // Starts an intent to go to the profile activity
    private void goProfileActivity() {
        Intent intent = new Intent(this, ProfileActivity.class);
        startActivity(intent);
    }

    // Starts an intent to go to the game activity
    private void goGameActivity() {
        Intent intent = new Intent(this, GameActivity.class);
        startActivity(intent);
    }
}