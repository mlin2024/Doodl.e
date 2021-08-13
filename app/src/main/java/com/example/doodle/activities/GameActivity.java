package com.example.doodle.activities;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.doodle.BitmapScaler;
import com.example.doodle.R;
import com.example.doodle.fragments.CanvasFragment;
import com.example.doodle.models.Doodle;
import com.example.doodle.models.Game;
import com.example.doodle.models.Player;
import com.google.android.material.snackbar.Snackbar;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.io.ByteArrayOutputStream;
import java.util.concurrent.TimeUnit;

public class GameActivity extends AppCompatActivity {
    public static final String TAG = "DoodleActivity";
    public static final long ONE_SECOND = TimeUnit.SECONDS.toMillis(1);

    // Views in the layout
    private RelativeLayout gameRelativeLayout;
    private Toolbar toolbar;
    private TextView roundTextView;
    private TextView timeTextView;
    private TextView waitingForOtherPlayers;

    // Other necessary member variables
    private Game game;
    private long timeCurRoundEnds;
    private ProgressDialog savingProgressDialog;
    private FragmentManager fragmentManager;
    private Fragment canvasFragment;
    private int indexInPlayerList;
    private int numPlayers;
    private int round;
    private Doodle parentDoodle;
    private Handler updateHandler;
    private Handler timeHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        // Initialize the views in the layout
        gameRelativeLayout = findViewById(R.id.gameRelativeLayout);
        toolbar = findViewById(R.id.gameToolbar);
        roundTextView = findViewById(R.id.roundTextView);
        timeTextView = findViewById(R.id.timeTextView);
        waitingForOtherPlayers = findViewById(R.id.waitingForOtherPlayers);

        // Initialize other member variables
        // Unwrap the game that was passed in by the intent
        game = getIntent().getParcelableExtra(GameModeActivity.TAG_GAME);
        timeCurRoundEnds = game.getUpdatedAt().getTime() + (game.getTimeLimit() * 1000);
        savingProgressDialog = new ProgressDialog(GameActivity.this);
        fragmentManager = getSupportFragmentManager();
        canvasFragment = CanvasFragment.newInstance(null, timeCurRoundEnds);
        indexInPlayerList = 0;
        numPlayers = game.getPlayers().size();
        round = 1;
        parentDoodle = null;
        updateHandler = new Handler(Looper.getMainLooper());
        timeHandler = new Handler(Looper.getMainLooper());

        // Set up toolbar
        toolbar.setTitleTextColor(getResources().getColor(R.color.white, getTheme()));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        // Set up round text view
        roundTextView.setText(getResources().getString(R.string.round) + " " + game.getRound() + "/" + numPlayers);

        // Set up ProgressDialog
        savingProgressDialog.setMessage(getResources().getString(R.string.saving_doodle));
        savingProgressDialog.setCancelable(false);

        // Set up canvas fragment
        fragmentManager.beginTransaction().add(R.id.canvasFrameLayout_GAME, canvasFragment).show(canvasFragment).commit();

        // Set up numPlayer
        String curPlayer = ParseUser.getCurrentUser().getObjectId();
        for (ParseUser player : game.getPlayers()) {
            // Iterate through the list of players, once the current user is found, indexInPlayerList is equal to their index
            if (player.getObjectId().equals(curPlayer)) break;
            indexInPlayerList++;
        }

        // Listen for result from fragment
        fragmentManager.setFragmentResultListener(CanvasFragment.TAG_RESULT_DOODLE, this, (requestKey, bundle) -> {
            timeHandler.removeCallbacksAndMessages(null);

            Bitmap drawingBitmap = bundle.getParcelable(CanvasFragment.TAG_DRAWING_BITMAP);
            drawingBitmap = makeTransparent(drawingBitmap, Color.WHITE);
            Bitmap parentBitmap = getBitmapFromDoodle(parentDoodle);
            saveDoodle(parentDoodle, parentBitmap, drawingBitmap);
            endCurrentRound();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Only start checking for new messages when the app becomes active in foreground
        updateHandler.postDelayed(updateGame, WaitingRoomActivity.POLL_INTERVAL);
        timeHandler.postDelayed(updateTime, ONE_SECOND);
    }

    @Override
    protected void onPause() {
        // Stop background task from refreshing messages, to avoid unnecessary traffic & battery drain
        updateHandler.removeCallbacksAndMessages(null);
        timeHandler.removeCallbacksAndMessages(null);

        super.onPause();
    }

    @Override
    protected void onDestroy() {
        // Stop background task from refreshing messages, to avoid unnecessary traffic & battery drain
        updateHandler.removeCallbacksAndMessages(null);
        timeHandler.removeCallbacksAndMessages(null);

        super.onDestroy();
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
                leaveGameDialog(() -> {
                    goProfileActivity();
                    finish();
                });
                return true;
            case R.id.logoutMenuItem:
                leaveGameDialog(() -> {
                    logout();
                    finish();
                });
                return true;
            case android.R.id.home:
                leaveGameDialog(() -> {
                    finish();
                });
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        leaveGameDialog(() -> {
            finish();
        });
    }

    private Runnable updateGame = new Runnable() {
        @Override
        public void run() {
            try {
                game.fetch();

                // Account for if any player has left the game
                int diff = numPlayers - game.getPlayers().size();
                if (diff > 0) {
                    numPlayers -= diff;
                    if (diff == 1) Snackbar.make(gameRelativeLayout, getResources().getString(R.string.player_has_left_the_game), Snackbar.LENGTH_LONG).show();
                    else Snackbar.make(gameRelativeLayout, diff + " " + getResources().getString(R.string.players_have_left_the_game), Snackbar.LENGTH_LONG).show();
                    // Only change the denominator if the player is not on the last round (just so it doesn't say something nonsensical like Round 2/1)
                    if (game.getRound() <= numPlayers) {
                        roundTextView.setText(getResources().getString(R.string.round) + " " + game.getRound() + "/" +  + numPlayers);
                    }
                }

                if (game.getRound() > round) {
                    round = game.getRound();
                    // End the game if all rounds are finished
                    if (round > numPlayers) {
                        goGameGalleryActivity();
                        finish();
                    }
                    // Else, start next round
                    queryForNextDoodle();
                }
                updateHandler.postDelayed(this, WaitingRoomActivity.POLL_INTERVAL);
            } catch (ParseException e) {
                Snackbar.make(gameRelativeLayout, getResources().getString(R.string.error_updating_game), Snackbar.LENGTH_LONG).show();
            }
        }
    };

    private Runnable updateTime = new Runnable() {
        @Override
        public void run() {
            long timeLeftInRoundMillis = timeCurRoundEnds - System.currentTimeMillis();
            int timeLeftInRound = (int)(timeLeftInRoundMillis/1000);
            if (timeLeftInRound >= 0) {
                timeTextView.setText(timeLeftInRound + getResources().getString(R.string.seconds_unit));
                timeHandler.postDelayed(this, ONE_SECOND);
                if (timeLeftInRound <= 10) {
                    timeTextView.setTextColor(getResources().getColor(R.color.red, getTheme()));
                }
            }
        }
    };

    // Receives the next doodle for the player to edit from the database
    // (the doodles in the game all just cyclically shift to the next player)
    private void queryForNextDoodle() {
        // Specify what type of data we want to query - Doodle.class
        ParseQuery<Doodle> query = ParseQuery.getQuery(Doodle.class);
        // Find doodle in current game
        query.whereEqualTo(Doodle.KEY_IN_GAME, game.getObjectId());
        // Find doodle by the next player in line
        int index = (indexInPlayerList + 1) % numPlayers;
        query.whereEqualTo(Doodle.KEY_ARTIST, game.getPlayers().get(index));
        // Only include doodles with tail length equal to the number of doodles the player has already added to
        // e.g. in round 2 we want a doodle that has tail length 1 (no one has added to it yet)
        query.whereEqualTo(Doodle.KEY_TAIL_LENGTH, game.getRound() - 1);

        // Start an asynchronous call for the doodle
        query.getFirstInBackground((nextDoodle, e) -> {
            if (e != null) { // Query has failed
                Snackbar.make(gameRelativeLayout, getResources().getString(R.string.error_finding_doodle), Snackbar.LENGTH_LONG).show();
            } else { // Query has succeeded
                parentDoodle = nextDoodle;
                startNextRound();
            }
        });
    }

    // Checks if everyone is done the current round
    private void checkCurrentRound() {
        // Specify what type of data we want to query - Doodle.class
        ParseQuery<Doodle> query = ParseQuery.getQuery(Doodle.class);
        // Find doodle in current game
        query.whereEqualTo(Doodle.KEY_IN_GAME, game.getObjectId());
        // Only find doodles submitted in current round - if all players have submitted a doodle in the current round, the round ends
        query.whereEqualTo(Doodle.KEY_TAIL_LENGTH, round);

        // Start an asynchronous call for the doodle
        query.findInBackground((doodlesSubmittedInRound, e) -> {
            if (e != null) { // Query has failed
                Snackbar.make(gameRelativeLayout, getResources().getString(R.string.error_updating_game), Snackbar.LENGTH_LONG).show();
            }
            else { // Query has succeeded
                if (doodlesSubmittedInRound.size() == numPlayers) {
                    game.setRound(game.getRound() + 1);
                    game.saveInBackground(e1 -> {
                        if (e1 != null) { // Save has failed
                            Snackbar.make(gameRelativeLayout, getResources().getString(R.string.error_updating_game), Snackbar.LENGTH_LONG).show();
                        }
                    });
                }
            }
        });
    }

    private void startNextRound() {
        timeCurRoundEnds = game.getUpdatedAt().getTime() + (game.getTimeLimit() * 1000);
        timeHandler.post(updateTime);
        roundTextView.setText(getResources().getString(R.string.round) + " " + game.getRound() + "/" +  + numPlayers);
        waitingForOtherPlayers.setVisibility(View.INVISIBLE);
        canvasFragment = CanvasFragment.newInstance(getBitmapFromDoodle(parentDoodle), timeCurRoundEnds);
        fragmentManager.beginTransaction().add(R.id.canvasFrameLayout_GAME, canvasFragment).show(canvasFragment).commit();
    }

    private void endCurrentRound() {
        timeTextView.setText("");
        parentDoodle = null;
        waitingForOtherPlayers.setVisibility(View.VISIBLE);
        fragmentManager.beginTransaction().remove(canvasFragment).commit();
    }

    // Convert transparentColor to be transparent in a Bitmap
    public static Bitmap makeTransparent(Bitmap bitmap, int transparentColor) {
        int width =  bitmap.getWidth();
        int height = bitmap.getHeight();
        Bitmap transparentBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        int [] allpixels = new int [transparentBitmap.getHeight() * transparentBitmap.getWidth()];
        bitmap.getPixels(allpixels, 0, transparentBitmap.getWidth(), 0, 0, transparentBitmap.getWidth(),transparentBitmap.getHeight());
        transparentBitmap.setPixels(allpixels, 0, width, 0, 0, width, height);

        for (int i = 0; i < transparentBitmap.getHeight() * transparentBitmap.getWidth(); i++){
            if (allpixels[i] == transparentColor) {
                allpixels[i] = Color.alpha(Color.TRANSPARENT);
            }
        }

        transparentBitmap.setPixels(allpixels, 0, transparentBitmap.getWidth(), 0, 0, transparentBitmap.getWidth(), transparentBitmap.getHeight());
        return transparentBitmap;
    }

    // Takes the image from a Doodle and converts it to a bitmap
    private Bitmap getBitmapFromDoodle(Doodle doodle) {
        if (doodle == null) return null;
        else {
            try {
                byte[] bitmapData = doodle.getImage().getData();
                Bitmap bitmap = BitmapFactory.decodeByteArray(bitmapData, 0, bitmapData.length);
                return bitmap;
            } catch (ParseException e) {
                Snackbar.make(gameRelativeLayout, getResources().getString(R.string.error_finding_doodle), Snackbar.LENGTH_LONG).show();
                return null;
            }
        }
    }

    // Saves the current doodle to the database
    private void saveDoodle(Doodle parentDoodle, Bitmap parentBitmap, Bitmap drawingBitmap) {
        Doodle childDoodle = new Doodle();

        // The artist is the current artist
        childDoodle.setArtist(ParseUser.getCurrentUser());
        // The image is the file that was passed in
        ParseFile drawingFile = combineBitmapsToParseFile(drawingBitmap, parentBitmap);
        childDoodle.setImage(drawingFile);
        // If it has no parent, just don't set it and let it default to the default defined in the database
        if (parentDoodle != null) childDoodle.setParent(parentDoodle);
        // The tail length is just one longer than it's parent
        // If it doesn't have a parent, don't set it and it will default to 1 as defined in the database
        if (parentDoodle != null) childDoodle.setTailLength(parentDoodle.getTailLength() + 1);
        // The root is the same as its parent
        // If it has no parent, its root is equal to its objectId, which will be set after it is saved
        if (parentDoodle != null) childDoodle.setRoot(parentDoodle.getRoot());
        // Set inGame to the objectId of the current game
        childDoodle.setInGame(game.getObjectId());

        savingProgressDialog.show();
        // Save doodle to database
        childDoodle.saveInBackground(e -> {
            if (e != null) { // Save has failed
                savingProgressDialog.dismiss();
                Snackbar.make(gameRelativeLayout, getResources().getString(R.string.error_saving_doodle), Snackbar.LENGTH_LONG).show();
            }
            else { // Save has succeeded
                // Now if it has no parent, set its root equal to its objectId
                if (parentDoodle == null) setRootToObjectId();
                else addToUserRootsContributedTo(parentDoodle.getRoot());
            }
        });
    }

    // Sets the root of a doodle to its objectId
    private void setRootToObjectId() {
        // Specify what type of data we want to query - Doodle.class
        ParseQuery<Doodle> query = ParseQuery.getQuery(Doodle.class);
        // The doodle we want to change the root of is distinguished by having a null root
        query.whereEqualTo(Doodle.KEY_ROOT, null);
        // Start an asynchronous call for the doodle
        query.findInBackground((doodles, e) -> {
            if (e != null) { // Query has failed
                savingProgressDialog.dismiss();
                Snackbar.make(gameRelativeLayout, getResources().getString(R.string.error_saving_doodle), Snackbar.LENGTH_LONG).show();
            }
            else { // Query has succeeded
                for (Doodle doodle: doodles) {
                    String root = doodle.getObjectId();
                    doodle.setRoot(root);
                    doodle.saveInBackground(e1 -> {
                        if (e1 != null) { // Save has failed
                            savingProgressDialog.dismiss();
                            Snackbar.make(gameRelativeLayout, getResources().getString(R.string.error_saving_doodle), Snackbar.LENGTH_LONG).show();
                        }
                        else { // Save has succeeded
                            addToUserRootsContributedTo(root);
                        }
                    });
                }
            }
        });
    }

    // Adds the given root to the current user's list of roots contributed to
    private void addToUserRootsContributedTo (String root) {
        Player player = new Player(ParseUser.getCurrentUser());
        player.addRootContributedTo(root);
        // Only check if the next round should start after everything has been saved
        player.saveInBackground(e -> {
            savingProgressDialog.dismiss();
            if (e != null) { // Save has failed
                Snackbar.make(gameRelativeLayout, getResources().getString(R.string.error_saving_doodle), Snackbar.LENGTH_LONG).show();
            }
            else { // Save has succeeded
                Toast.makeText(this, getResources().getString(R.string.doodle_submitted), Toast.LENGTH_SHORT).show();
                checkCurrentRound();
            }
        });
    }

    // Layers drawingBitmap on top of parentBitmap and returns the result as a ParseFile
    private ParseFile combineBitmapsToParseFile(Bitmap drawingBitmap, Bitmap parentBitmap) {
        // If it has no parent, there is nothing to overlay it with
        if (parentBitmap == null) return saveBitmapToParseFile(drawingBitmap);

        Bitmap bmOverlay = Bitmap.createBitmap(drawingBitmap.getWidth(), drawingBitmap.getHeight(), drawingBitmap.getConfig());
        Canvas canvas = new Canvas(bmOverlay);
        canvas.drawBitmap(BitmapScaler.scaleToFitWidth(parentBitmap, drawingBitmap.getWidth()), new Matrix(), null);
        canvas.drawBitmap(drawingBitmap, 0, 0, null);
        return saveBitmapToParseFile(bmOverlay);
    }

    // Converts a bitmap to a ParseFile
    private ParseFile saveBitmapToParseFile(Bitmap bitmap) {
        String fileName = "doodle" + System.currentTimeMillis() + ".png";
        // Configure byte output stream
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        // Compress the image
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, bytes);
        // Save to ParseFile
        ParseFile parseFile = new ParseFile(fileName, bytes.toByteArray());
        return parseFile;
    }

    // Handles when the player tries to leave the game
    private void leaveGameDialog(Runnable runIfReallyLeaving) {
        // Create an alert to ask user if they really want to leave the game
        AlertDialog.Builder builder = new AlertDialog.Builder(GameActivity.this);
        builder.setTitle(getResources().getString(R.string.sure_you_want_to_leave_game));
        // If they are the last player left, warn them specifically
        if (game.getPlayers().size() == 1) {
            builder.setMessage(getResources().getString(R.string.you_are_the_last_player))
                    .setPositiveButton(getResources().getString(R.string.leave_game), (dialog, which) -> {
                        game.deleteInBackground();
                        runIfReallyLeaving.run();
                    });
        }
        // Else, just warn them normally
        else {
            builder.setMessage(getResources().getString(R.string.once_you_leave_you_cant_come_back))
                    .setPositiveButton(getResources().getString(R.string.leave_game), (dialog, which) -> {
                        game.removePlayer(ParseUser.getCurrentUser());
                        game.saveInBackground(e -> {
                            if (e != null) { // Save has failed
                                Snackbar.make(gameRelativeLayout, getResources().getString(R.string.error_updating_game), Snackbar.LENGTH_LONG).show();
                            }
                        });
                        runIfReallyLeaving.run();
                    });
        }
        builder.setNegativeButton(getResources().getString(R.string.never_mind), null);

        // Create and show the alert
        AlertDialog alert = builder.create();
        alert.show();
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

    // Starts an intent to go to the game gallery activity
    private void goGameGalleryActivity() {
        Intent intent = new Intent(this, GameGalleryActivity.class);
        intent.putExtra(GameModeActivity.TAG_GAME, game);
        startActivity(intent);
    }

    // Log out user and send them back to login/signup page
    private void logout() {
        ProgressDialog logoutProgressDialog = new ProgressDialog(GameActivity.this);
        logoutProgressDialog.setMessage(getResources().getString(R.string.logging_out));
        logoutProgressDialog.setCancelable(false);
        logoutProgressDialog.show();
        ParseUser.logOutInBackground(e -> {
            logoutProgressDialog.dismiss();
            if (e != null) { // Logout has failed
                Snackbar.make(gameRelativeLayout, getResources().getString(R.string.logout_failed), Snackbar.LENGTH_LONG).show();
            }
            else { // Logout has succeeded
                goLoginSignupActivity();
                finish();
            }
        });
    }
}