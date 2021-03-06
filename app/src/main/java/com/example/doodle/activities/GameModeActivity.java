package com.example.doodle.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.doodle.R;
import com.example.doodle.models.Game;
import com.google.android.material.snackbar.Snackbar;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import net.cachapa.expandablelayout.ExpandableLayout;

public class GameModeActivity extends AppCompatActivity {
    public static final String TAG = "GameModeActivity";
    public static final String TAG_GAME = "game";
    public static final int MAX_PLAYERS = 10;

    // Views in the layout
    private RelativeLayout gameModeRelativeLayout;
    private Toolbar toolbar;
    private ImageView background;
    private Button createGameButton;
    private ExpandableLayout createGameExpandableLayout;
    private TextView gameCodeTextView;
    private Button createGameButtonGo;
    private Button joinGameButton;
    private ExpandableLayout joinGameExpandableLayout;
    private EditText gameCodeEditText;
    private Button joinGameButtonGo;

    // Other necessary member variables
    private Animation shake;
    private ProgressDialog creatingProgressDialog;
    private ProgressDialog findingProgressDialog;
    private ProgressDialog joiningProgressDialog;
    private TextWatcher textWatcher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_mode);

        String gameCode = generateRandomGameCode();

        // Initialize the views in the layout
        gameModeRelativeLayout = findViewById(R.id.gameModeRelativeLayout);
        toolbar = findViewById(R.id.gameModeToolbar);
        background = findViewById(R.id.gameModeBackground);
        createGameButton = findViewById(R.id.createGameButton);
        createGameExpandableLayout = findViewById(R.id.createGameExpandableLayout);
        gameCodeTextView = findViewById(R.id.gameCodeTextView);
        createGameButtonGo = findViewById(R.id.createGameButtonGo);
        joinGameButton = findViewById(R.id.joinGameButton);
        joinGameExpandableLayout = findViewById(R.id.joinGameExpandableLayout);
        gameCodeEditText = findViewById(R.id.gameCodeEditText);
        joinGameButtonGo = findViewById(R.id.joinGameButtonGo);

        // Initialize other member variables
        creatingProgressDialog = new ProgressDialog(GameModeActivity.this);
        findingProgressDialog = new ProgressDialog(GameModeActivity.this);
        joiningProgressDialog = new ProgressDialog(GameModeActivity.this);
        shake = AnimationUtils.loadAnimation(GameModeActivity.this, R.anim.shake);
        // TextWatcher to disable the join game button unless 4-character game code has been filled in
        textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {}
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {}
            @Override
            public void afterTextChanged(Editable editable) {
                checkGameCodeField();
            }
        };

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

                Drawable backgroundDrawable;
                if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                    backgroundDrawable = getResources().getDrawable(R.drawable.background, getTheme());
                }
                else {
                    backgroundDrawable = getResources().getDrawable(R.drawable.background_land, getTheme());
                }
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

        // Set up game code TextView
        gameCodeTextView.setText(gameCode);

        // Set up ProgressDialogs
        creatingProgressDialog.setMessage(getResources().getString(R.string.creating_game));
        creatingProgressDialog.setCancelable(false);
        findingProgressDialog.setMessage(getResources().getString(R.string.finding_game));
        findingProgressDialog.setCancelable(false);
        joiningProgressDialog.setMessage(getResources().getString(R.string.joining_game));
        joiningProgressDialog.setCancelable(false);

        // Set up TextWatcher
        gameCodeEditText.addTextChangedListener(textWatcher);
        checkGameCodeField();

        createGameButton.setOnClickListener(v -> {
            createGameExpandableLayout.toggle();
            joinGameExpandableLayout.collapse();
        });

        joinGameButton.setOnClickListener(v -> {
            if (joinGameExpandableLayout.isExpanded()) joinGameExpandableLayout.collapse();
            else joinGameExpandableLayout.expand();
            createGameExpandableLayout.collapse();
        });

        createGameButtonGo.setOnClickListener(v -> {
            try {
                Game game = new Game();
                game.setGameCode(gameCode);
                game.setHost(ParseUser.getCurrentUser().fetch());
                game.addPlayer(ParseUser.getCurrentUser().fetch());

                // Save game to database
                creatingProgressDialog.show();
                game.saveInBackground(e -> {
                    creatingProgressDialog.dismiss();
                    if (e != null) { // Save has failed
                        Snackbar.make(gameModeRelativeLayout, getResources().getString(R.string.error_creating_game), Snackbar.LENGTH_LONG).show();
                    }
                    else { // Save has succeeded
                        goWaitingRoomActivity(game);
                        finish();
                    }
                });
            } catch (ParseException e) {
                Snackbar.make(gameModeRelativeLayout, getResources().getString(R.string.error_creating_game), Snackbar.LENGTH_LONG).show();
            }
        });

        joinGameButtonGo.setOnClickListener(v -> {
            hideSoftKeyboard(gameModeRelativeLayout);
            String inputtedGameCode = gameCodeEditText.getText().toString();
            findGameByGameCode(inputtedGameCode);
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

    // Enables the join game button only if the game code field is populated by 4 characters
    private void checkGameCodeField() {
        String inputtedGameCode = gameCodeEditText.getText().toString();
        if (inputtedGameCode.length() < 4) joinGameButtonGo.setEnabled(false);
        else joinGameButtonGo.setEnabled(true);
    }

    // Generates a random 4-character code for the game
    private String generateRandomGameCode() {
        String gameCode = "";
        for (int i = 0; i < 4; i++) {
            gameCode += (char)('A' + (Math.random() * 26));
        }
        return gameCode;
    }

    private void findGameByGameCode(String gameCode) {
        // Specify what type of data we want to query - Game.class
        ParseQuery<Game> query = ParseQuery.getQuery(Game.class);
        // Find game with gameCode equal to given game code
        query.whereEqualTo(Game.KEY_GAME_CODE, gameCode);

        findingProgressDialog.show();
        // Start an asynchronous call for the game
        query.getFirstInBackground((foundGame, e) -> {
            findingProgressDialog.dismiss();
            if (e != null) { // Query has failed
                Snackbar.make(gameModeRelativeLayout, getResources().getString(R.string.error_finding_game), Snackbar.LENGTH_LONG).show();
                joinGameExpandableLayout.startAnimation(shake);
                return;
            }
            else { // Query has succeeded
                if (foundGame.getPlayers().size() >= MAX_PLAYERS) {
                    Snackbar.make(gameModeRelativeLayout, getResources().getString(R.string.that_game_is_full), Snackbar.LENGTH_LONG).show();
                }
                else if (foundGame.getRound() != 0) {
                    Snackbar.make(gameModeRelativeLayout, getResources().getString(R.string.that_game_already_started), Snackbar.LENGTH_LONG).show();
                }
                else {
                    foundGame.addPlayer(ParseUser.getCurrentUser());
                    joiningProgressDialog.show();
                    foundGame.saveInBackground(e1 -> {
                        joiningProgressDialog.dismiss();
                        if (e1 != null) { // Save has failed
                            Snackbar.make(gameModeRelativeLayout, getResources().getString(R.string.error_joining_game), Snackbar.LENGTH_LONG).show();
                        }
                        else { // Save has succeeded
                            goWaitingRoomActivity(foundGame);
                            finish();
                        }
                    });
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

    // Starts an intent to go to the waiting room activity
    private void goWaitingRoomActivity(Game game) {
        Intent intent = new Intent(this, WaitingRoomActivity.class);
        intent.putExtra(TAG_GAME, game);
        startActivity(intent);
    }

    // Logs out user and sends them back to login/signup page
    private void logout() {
        ProgressDialog logoutProgressDialog = new ProgressDialog(GameModeActivity.this);
        logoutProgressDialog.setMessage(getResources().getString(R.string.logging_out));
        logoutProgressDialog.setCancelable(false);
        logoutProgressDialog.show();
        ParseUser.logOutInBackground(e -> {
            logoutProgressDialog.dismiss();
            if (e != null) { // Logout has failed
                Snackbar.make(gameModeRelativeLayout, getResources().getString(R.string.logout_failed), Snackbar.LENGTH_LONG).show();
            }
            else { // Logout has succeeded
                goLoginSignupActivity();
                finish();
            }
        });
    }

    // Minimizes the soft keyboard
    private void hideSoftKeyboard(View view){
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
}