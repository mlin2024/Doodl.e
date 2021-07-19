package com.example.doodle.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.doodle.R;
import com.google.android.material.snackbar.Snackbar;
import com.parse.ParseUser;

import net.cachapa.expandablelayout.ExpandableLayout;

public class GameModeActivity extends AppCompatActivity {
    public static final String TAG = "GameModeActivity";
    public static final String GAME_CODE_TAG = "gameCode";

    private RelativeLayout gameModeRelativeLayout;
    private Toolbar toolbar;
    private Button createGameButton;
    private ExpandableLayout createGameExpandableLayout;
    private TextView gameCodeTextView;
    private Button createGameButtonGo;
    private Button joinGameButton;
    private ExpandableLayout joinGameExpandableLayout;
    private EditText gameCodeEditText;
    private Button joinGameButtonGo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_mode);

        String gameCode = generateRandomGameCode();

        gameModeRelativeLayout = findViewById(R.id.gameModeRelativeLayout);
        toolbar = findViewById(R.id.gameModeToolbar);
        toolbar.setTitleTextColor(getResources().getColor(R.color.white));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        createGameButton = findViewById(R.id.createGameButton);
        createGameExpandableLayout = findViewById(R.id.createGameExpandableLayout);
        gameCodeTextView = findViewById(R.id.gameCodeTextView);
        gameCodeTextView.setText(gameCode);
        createGameButtonGo = findViewById(R.id.createGameButtonGo);
        joinGameButton = findViewById(R.id.joinGameButton);
        joinGameExpandableLayout = findViewById(R.id.joinGameExpandableLayout);
        gameCodeEditText = findViewById(R.id.gameCodeEditText);
        joinGameButtonGo = findViewById(R.id.joinGameButtonGo);

        createGameButton.setOnClickListener(v -> {
            if (createGameExpandableLayout.isExpanded()) createGameExpandableLayout.collapse();
            else createGameExpandableLayout.expand();
            joinGameExpandableLayout.collapse();
        });

        joinGameButton.setOnClickListener(v -> {
            if (joinGameExpandableLayout.isExpanded()) joinGameExpandableLayout.collapse();
            else joinGameExpandableLayout.expand();
            createGameExpandableLayout.collapse();
        });

        createGameButtonGo.setOnClickListener(v -> {
            goWaitingRoomActivity(gameCode);
        });

        joinGameButtonGo.setOnClickListener(v -> {
            hideSoftKeyboard(gameModeRelativeLayout);
            String inputtedGameCode = gameCodeEditText.getText().toString();
            if (inputtedGameCode.isEmpty()) {
                Snackbar.make(gameModeRelativeLayout, R.string.must_enter_game_code, Snackbar.LENGTH_LONG).show();
            }
            else if (inputtedGameCode.length() < 4) {
                Snackbar.make(gameModeRelativeLayout, R.string.game_code_too_short, Snackbar.LENGTH_LONG).show();
            }
            else {
                goWaitingRoomActivity(inputtedGameCode);
            }
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

    private String generateRandomGameCode() {
        // TODO: implement generating random game code
        return "AFXP";
    }

    private void logout() {
        ProgressDialog logoutProgressDialog = new ProgressDialog(GameModeActivity.this);
        logoutProgressDialog.setMessage(getResources().getString(R.string.logging_out));
        logoutProgressDialog.show();
        ParseUser.logOutInBackground(e -> {
            logoutProgressDialog.dismiss();
            if (e != null) {
                Snackbar.make(gameModeRelativeLayout, R.string.logout_failed, Snackbar.LENGTH_LONG).show();
            }
            else {
                goLoginSignupActivity();
                finish();
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

    // Starts an intent to go to the waiting room activity
    private void goWaitingRoomActivity(String gameCode) {
        Intent intent = new Intent(this, WaitingRoomActivity.class);
        intent.putExtra(GAME_CODE_TAG, gameCode);
        startActivity(intent);
    }

    // Minimizes the soft keyboard
    private void hideSoftKeyboard(View view){
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
}