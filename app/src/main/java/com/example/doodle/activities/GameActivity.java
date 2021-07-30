package com.example.doodle.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.divyanshu.draw.widget.DrawView;
import com.example.doodle.R;
import com.example.doodle.fragments.ColorPickerFragment;
import com.example.doodle.models.ColorViewModel;
import com.example.doodle.models.Doodle;
import com.example.doodle.models.Game;
import com.example.doodle.models.Player;
import com.google.android.material.snackbar.Snackbar;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import net.cachapa.expandablelayout.ExpandableLayout;

import java.io.ByteArrayOutputStream;

public class GameActivity extends AppCompatActivity {
    public static final String TAG = "DoodleActivity";
    public static final float STROKE_WIDTH_SMALL = 10;
    public static final float STROKE_WIDTH_MEDIUM = 20;
    public static final float STROKE_WIDTH_LARGE = 30;

    // Views in the layout
    private RelativeLayout gameRelativeLayout;
    private Toolbar toolbar;
    private TextView roundTextView;
    private ImageView parentImageView;
    private DrawView doodleDrawView;
    private TextView waitingForOtherPlayers;
    private Button undoButton;
    private Button redoButton;
    private Button smallButton;
    private Button mediumButton;
    private Button largeButton;
    private ImageButton eraserButton;
    private ImageButton colorButton;
    private ExpandableLayout colorPickerExpandableLayout;
    private FrameLayout colorPickerFrameLayout;
    private Button doneButton;

    // Other necessary member variables
    private ProgressDialog savingProgressDialog;
    private FragmentManager fragmentManager;
    private Fragment colorPickerFragment;
    private ViewModelProvider viewModelProvider;
    private ColorViewModel colorViewModel;
    private ColorStateList currentColor;
    private Button currentSizeButton;
    private ImageButton currentPenButton;
    private Game game;
    private int round;
    private boolean currentlyDrawing;
    private Doodle parentDoodle;
    private Handler updateHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        // Initialize the views in the layout
        gameRelativeLayout = findViewById(R.id.gameRelativeLayout);
        toolbar = findViewById(R.id.gameToolbar);
        roundTextView = findViewById(R.id.roundTextView);
        parentImageView = findViewById(R.id.parentImageView_GAME);
        doodleDrawView = findViewById(R.id.doodleDrawView_GAME);
        waitingForOtherPlayers = findViewById(R.id.waitingForOtherPlayers);
        undoButton = findViewById(R.id.undoButton_GAME);
        redoButton = findViewById(R.id.redoButton_GAME);
        smallButton = findViewById(R.id.smallButton_GAME);
        mediumButton = findViewById(R.id.mediumButton_GAME);
        largeButton = findViewById(R.id.largeButton_GAME);
        eraserButton = findViewById(R.id.eraserButton_GAME);
        colorButton = findViewById(R.id.colorButton_GAME);
        colorPickerExpandableLayout = findViewById(R.id.colorPickerExpandableLayout_GAME);
        colorPickerFrameLayout = findViewById(R.id.colorPickerFrameLayout_GAME);
        doneButton = findViewById(R.id.doneButton_GAME);

        // Initialize other member variables
        savingProgressDialog = new ProgressDialog(GameActivity.this);
        fragmentManager = getSupportFragmentManager();
        colorPickerFragment = new ColorPickerFragment();
        viewModelProvider = new ViewModelProvider(this);
        // Set up ViewModel for color picker fragment
        colorViewModel = viewModelProvider.get(ColorViewModel.class);
        currentColor = getResources().getColorStateList(R.color.button_black, getTheme());
        currentSizeButton = mediumButton;
        currentPenButton = colorButton;
        // Unwrap the game that was passed in by the intent
        game = getIntent().getParcelableExtra(GameModeActivity.GAME_TAG);
        round = 1;
        currentlyDrawing = true;
        parentDoodle = null;
        updateHandler = new Handler(Looper.getMainLooper());

        // Set up toolbar
        toolbar.setTitleTextColor(getResources().getColor(R.color.white, getTheme()));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        // Set up round text view
        roundTextView.setText(getResources().getString(R.string.round) + " " + round + "/" + game.getPlayers().size());

        // Set up ProgressDialog
        savingProgressDialog.setMessage(getResources().getString(R.string.saving_doodle));
        savingProgressDialog.setCancelable(false);

        // Set up color picker fragment
        fragmentManager.beginTransaction().add(R.id.colorPickerFrameLayout_GAME, colorPickerFragment).show(colorPickerFragment).commit();

        // Set up canvas
        resetCanvas();

        undoButton.setOnClickListener(v -> {
            doodleDrawView.undo();
        });

        redoButton.setOnClickListener(v -> {
            doodleDrawView.redo();
        });

        smallButton.setOnClickListener(v -> {
            handleSizeButtonChange(smallButton);
            doodleDrawView.setStrokeWidth(STROKE_WIDTH_SMALL);
        });

        mediumButton.setOnClickListener(v -> {
            handleSizeButtonChange(mediumButton);
            doodleDrawView.setStrokeWidth(STROKE_WIDTH_MEDIUM);
        });

        largeButton.setOnClickListener(v -> {
            handleSizeButtonChange(largeButton);
            doodleDrawView.setStrokeWidth(STROKE_WIDTH_LARGE);
        });

        eraserButton.setOnClickListener(v -> {
            colorPickerExpandableLayout.collapse();

            handlePenButtonChange(eraserButton);

            // Change pen color to eraser color
            doodleDrawView.setColor(getResources().getColor(R.color.transparent, getTheme()));
        });

        colorButton.setOnClickListener(v -> {
            // If it's not selected, select it
            if (colorButton.isSelected() == false) {
                handlePenButtonChange(colorButton);

                // Change pen color to the current color
                doodleDrawView.setColor(currentColor.getDefaultColor());
            }
            // If it's already selected, click will expand/retract the color picker ExpandableLayout
            else {
                if (colorPickerExpandableLayout.isExpanded())
                    colorPickerExpandableLayout.collapse();
                else colorPickerExpandableLayout.expand();
                colorViewModel.getSelectedItem().observe(this, color -> {
                    currentColor = color;
                    colorButton.setBackgroundTintList(color);
                    doodleDrawView.setColor(color.getDefaultColor());
                });
            }
        });

        doneButton.setOnClickListener(v -> {
            Bitmap drawingBitmap = doodleDrawView.getBitmap();
            drawingBitmap = makeTransparent(drawingBitmap, Color.WHITE);
            Bitmap parentBitmap = getBitmapFromDoodle(parentDoodle);
            saveDoodle(parentDoodle, parentBitmap, drawingBitmap);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Only start checking for new messages when the app becomes active in foreground
        updateHandler.postDelayed(updateGame, WaitingRoomActivity.POLL_INTERVAL);
    }

    @Override
    protected void onPause() {
        // Stop background task from refreshing messages, to avoid unnecessary traffic & battery drain
        updateHandler.removeCallbacksAndMessages(null);
        super.onPause();
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

    private Runnable updateGame = new Runnable() {
        @Override
        public void run() {
            try {
                game.fetch();

                // If the player is waiting for a new doodle, see if any are available
                if (!currentlyDrawing) queryForNextDoodle();

                updateHandler.postDelayed(this, WaitingRoomActivity.POLL_INTERVAL);
            } catch (ParseException e) {
                Snackbar.make(gameRelativeLayout, R.string.error_fetching_doodles, Snackbar.LENGTH_LONG).show();
            }
        }
    };

    private void queryForNextDoodle() {
        // Specify what type of data we want to query - Doodle.class
        ParseQuery<Doodle> query = ParseQuery.getQuery(Doodle.class);
        // Find doodle in current game
        query.whereEqualTo(Doodle.KEY_IN_GAME, game.getObjectId());
        // Don't include doodles that the current user has already edited an ancestor of
        Player player = new Player(ParseUser.getCurrentUser());
        query.whereNotContainedIn(Doodle.KEY_ROOT, player.getRootsContributedTo());
        // Don't include doodles that don't have a root (it must still be getting assigned)
        query.whereNotEqualTo(Doodle.KEY_ROOT, null);
        // Only include doodles with tail length equal to the number of doodles the player has already added to
        // e.g. after round 1 we want a doodle that has tail length 1 (no one has added to it yet)
        query.whereEqualTo(Doodle.KEY_TAIL_LENGTH, round);

        // Start an asynchronous call for the doodle
        query.getFirstInBackground((nextDoodle, e) -> {
            if (e != null) { // Query has failed, no doodle is available yet
                return;
            }
            else { // Query has succeeded
                parentDoodle = nextDoodle;
                startNextRound();
            }
        });
    }

    private Runnable endIfComplete = new Runnable() {
        @Override
        public void run() {
            // Specify what type of data we want to query - Doodle.class
            ParseQuery<Doodle> query = ParseQuery.getQuery(Doodle.class);
            // Find doodle in current game
            query.whereEqualTo(Doodle.KEY_IN_GAME, game.getObjectId());
            // Find all doodles in the game which are complete
            query.whereEqualTo(Doodle.KEY_TAIL_LENGTH, round);

            // Start an asynchronous call for the doodle
            query.findInBackground((foundDoodles, e) -> {
                if (e != null) { // Query has failed
                    Snackbar.make(gameRelativeLayout, R.string.failed_to_load_doodles_from_game, Snackbar.LENGTH_LONG).show();
                    return;
                }
                else { // Query has succeeded
                    // If all the game doodles are included, that means all the game doodles are complete
                    if (foundDoodles != null && foundDoodles.size() == round) goGameGalleryActivity();
                    else updateHandler.postDelayed(this, WaitingRoomActivity.POLL_INTERVAL);
                }
            });
        }
    };

    private void startNextRound() {
        round++;
        roundTextView.setText(getResources().getString(R.string.round) + " " + round + "/" +  + game.getPlayers().size());
        waitingForOtherPlayers.setVisibility(View.INVISIBLE);
        enableAllButtons();
        currentlyDrawing = true;
        // Set up parent ImageView
        if (parentDoodle != null)
            Glide.with(this)
                    .load(parentDoodle.getImage().getUrl())
                    .placeholder(R.drawable.placeholder)
                    .into(parentImageView);
    }

    private void endCurrentRound() {
        parentDoodle = null;
        doodleDrawView.clearCanvas();
        waitingForOtherPlayers.setVisibility(View.VISIBLE);
        disableAllButtons();
        currentlyDrawing = false;

        // Clear parent ImageView
        parentImageView.setImageBitmap(null);

        // If it's the last round and you're done drawing, check if the game should end
        if (round >= game.getPlayers().size() && !currentlyDrawing) {
            // If all the doodles are complete, end the game
            updateHandler.post(endIfComplete);
        }
    }

    private void handleSizeButtonChange(Button button) {
        // Hide the icon on the previously selected button
        currentSizeButton.setForeground(null);

        // Display the icon on the newly selected button
        currentSizeButton = button;
        currentSizeButton.setForeground(getResources().getDrawable(R.drawable.transparent_circle_indicator, getTheme()));
    }

    private void handlePenButtonChange(ImageButton button) {
        // Set previously selected button to unselected
        currentPenButton.setSelected(false);

        // Hide the icon on the previously selected button
        currentPenButton.setForeground(null);

        // Display the icon on the newly selected button
        currentPenButton = button;
        currentPenButton.setForeground(getResources().getDrawable(R.drawable.transparent_circle_indicator, getTheme()));

        // Set the newly selected button to selected
        currentPenButton.setSelected(true);
    }

    // Resets the canvas to a default state
    private void resetCanvas() {
        doodleDrawView.clearCanvas();
        doodleDrawView.setStrokeWidth(STROKE_WIDTH_MEDIUM);
        doodleDrawView.setColor(currentColor.getDefaultColor());

        // Set up pen buttons
        eraserButton.setSelected(false);
        colorButton.setSelected(true);
        handlePenButtonChange(colorButton);
        handleSizeButtonChange(mediumButton);
    }

    // Disables all buttons
    private void disableAllButtons() {
        undoButton.setEnabled(false);
        redoButton.setEnabled(false);
        smallButton.setEnabled(false);
        mediumButton.setEnabled(false);
        largeButton.setEnabled(false);
        eraserButton.setEnabled(false);
        colorButton.setEnabled(false);
        doneButton.setEnabled(false);
        colorPickerExpandableLayout.collapse();
    }

    // Enables all buttons
    private void enableAllButtons() {
        undoButton.setEnabled(true);
        redoButton.setEnabled(true);
        smallButton.setEnabled(true);
        mediumButton.setEnabled(true);
        largeButton.setEnabled(true);
        eraserButton.setEnabled(true);
        colorButton.setEnabled(true);
        doneButton.setEnabled(true);
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
                Snackbar.make(gameRelativeLayout, R.string.error_finding_doodle, Snackbar.LENGTH_LONG).show();
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
            if (e != null) { // Saving doodle failed
                savingProgressDialog.dismiss();
                Snackbar.make(gameRelativeLayout, R.string.error_saving_doodle, Snackbar.LENGTH_LONG).show();
            }
            else { // Saving doodle succeeded
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
                Snackbar.make(gameRelativeLayout, R.string.error_saving_doodle, Snackbar.LENGTH_LONG).show();
            }
            else { // Query has succeeded
                for (Doodle doodle: doodles) {
                    String root = doodle.getObjectId();
                    doodle.setRoot(root);
                    doodle.saveInBackground(gameRelativeLayout, () -> {
                        addToUserRootsContributedTo(root);
                    });
                }
            }
        });
    }

    // Adds the given root to the current user's list of roots contributed to
    private void addToUserRootsContributedTo (String root) {
        Player player = new Player(ParseUser.getCurrentUser());
        player.addRootContributedTo(root);
        // The round only ends after everything has been saved
        player.saveInBackground(gameRelativeLayout, () -> {
            savingProgressDialog.dismiss();
            Toast.makeText(this, R.string.doodle_submitted, Toast.LENGTH_SHORT).show();
            endCurrentRound();
        });
    }

    // Layers drawingBitmap on top of parentBitmap and returns the result as a ParseFile
    private ParseFile combineBitmapsToParseFile(Bitmap drawingBitmap, Bitmap parentBitmap) {
        // If it has no parent, there is nothing to overlay it with
        if (parentBitmap == null) return saveBitmapToParseFile(drawingBitmap);

        Bitmap bmOverlay = Bitmap.createBitmap(drawingBitmap.getWidth(), drawingBitmap.getHeight(), drawingBitmap.getConfig());
        Canvas canvas = new Canvas(bmOverlay);
        canvas.drawBitmap(parentBitmap, new Matrix(), null);
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

    // Starts an intent to go to the home activity
    private void goHomeActivity() {
        Intent intent = new Intent(this, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    // Starts an intent to go to the game gallery activity
    private void goGameGalleryActivity() {
        Intent intent = new Intent(this, GameGalleryActivity.class);
        intent.putExtra(GameModeActivity.GAME_TAG, game);
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
            if (e != null) {
                Snackbar.make(gameRelativeLayout, R.string.logout_failed, Snackbar.LENGTH_LONG).show();
            }
            else {
                goLoginSignupActivity();
                finish();
            }
        });
    }
}