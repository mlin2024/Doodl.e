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
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.divyanshu.draw.widget.DrawView;
import com.example.doodle.R;
import com.example.doodle.fragments.ColorPickerFragment;
import com.example.doodle.models.ColorViewModel;
import com.example.doodle.models.Doodle;
import com.example.doodle.models.Player;
import com.google.android.material.snackbar.Snackbar;
import com.parse.FunctionCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import net.cachapa.expandablelayout.ExpandableLayout;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;

public class DoodleActivity extends AppCompatActivity {
    public static final String TAG = "DoodleActivity";
    public static final float STROKE_WIDTH_SMALL = 10;
    public static final float STROKE_WIDTH_MEDIUM = 20;
    public static final float STROKE_WIDTH_LARGE = 30;
    public static final String PARENT_DOODLE = "ParentDoodle";
    public static final String IN_GAME = "inGame";

    // Views in the layout
    private RelativeLayout doodleRelativeLayout;
    private Toolbar toolbar;
    private ImageView parentImageView;
    private DrawView doodleDrawView;
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
    private boolean inGame;
    private ProgressDialog savingProgressDialog;
    private FragmentManager fragmentManager;
    private Fragment colorPickerFragment;
    private ViewModelProvider viewModelProvider;
    private ColorViewModel colorViewModel;
    private ColorStateList currentColor;
    private Button currentSizeButton;
    private ImageButton currentPenButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doodle);

        // Initialize the views in the layout
        doodleRelativeLayout = findViewById(R.id.doodleRelativeLayout);
        toolbar = findViewById(R.id.doodleToolbar);
        parentImageView = findViewById(R.id.parentImageView);
        doodleDrawView = findViewById(R.id.doodleDrawView);
        undoButton = findViewById(R.id.undoButton);
        redoButton = findViewById(R.id.redoButton);
        smallButton = findViewById(R.id.smallButton);
        mediumButton = findViewById(R.id.mediumButton);
        largeButton = findViewById(R.id.largeButton);
        eraserButton = findViewById(R.id.eraserButton);
        colorButton = findViewById(R.id.colorButton);
        colorPickerExpandableLayout = findViewById(R.id.colorPickerExpandableLayout);
        colorPickerFrameLayout = findViewById(R.id.colorPickerFrameLayout);
        doneButton = findViewById(R.id.doneButton);

        // Initialize other member variables
        inGame = false;
        savingProgressDialog = new ProgressDialog(DoodleActivity.this);
        fragmentManager = getSupportFragmentManager();
        colorPickerFragment = new ColorPickerFragment();
        viewModelProvider = new ViewModelProvider(this);
        // Set up ViewModel for color picker fragment
        colorViewModel = viewModelProvider.get(ColorViewModel.class);
        currentColor = getResources().getColorStateList(R.color.button_black, getTheme());
        currentSizeButton = mediumButton;
        currentPenButton = colorButton;

        // Set up toolbar
        toolbar.setTitleTextColor(getResources().getColor(R.color.white, getTheme()));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);


        // Get parent doodle from intent
        Doodle parentDoodle = getIntent().getParcelableExtra(PARENT_DOODLE);
        Log.e(TAG, ""+parentDoodle);
        Bitmap parentBitmap = getBitmapFromDoodle(parentDoodle);

        // Get inGame from intent
        inGame = getIntent().getBooleanExtra(IN_GAME, false);

        // Set up parent ImageView (if parentDoodle exists)
        if (parentDoodle != null)
            Glide.with(this)
                .load(parentDoodle.getImage().getUrl())
                .placeholder(R.drawable.placeholder)
                .into(parentImageView);

        // Set up pen buttons
        eraserButton.setSelected(false);
        colorButton.setSelected(true);

        // Set up ProgressDialog
        savingProgressDialog.setMessage(getResources().getString(R.string.saving_doodle));
        savingProgressDialog.setCancelable(false);

        // Set up color picker fragment
        fragmentManager.beginTransaction().add(R.id.colorPickerFrameLayout, colorPickerFragment).show(colorPickerFragment).commit();

        // Set up canvas
        doodleDrawView.clearCanvas();
        doodleDrawView.setStrokeWidth(STROKE_WIDTH_MEDIUM);
        doodleDrawView.setColor(currentColor.getDefaultColor());

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
            saveDoodle(parentDoodle, parentBitmap, drawingBitmap);
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
                Snackbar.make(doodleRelativeLayout, R.string.error_finding_doodle, Snackbar.LENGTH_LONG).show();
                return null;
            }
        }
    }

    // Saves the current doodle to the database
    private void saveDoodle(Doodle parentDoodle, Bitmap parentBitmap, Bitmap drawingBitmap) {
        if (parentDoodle == null) Log.e(TAG, "parentDoodle is null");

        Doodle childDoodle = new Doodle();

        // The artist is the current artist
        childDoodle.setArtist(ParseUser.getCurrentUser());
        // The image is the file that was passed in
        ParseFile drawingFile = combineBitmapsToParseFile(drawingBitmap, parentBitmap);
        childDoodle.setImage(drawingFile);
        // The parent is the doodle that was passed in via intent
        // If it has no parent, just don't set it and let it default to the default defined in the database
        if (parentDoodle != null) childDoodle.setParent(parentDoodle);
        // The tail length is just one longer than it's parent
        // If it doesn't have a parent, don't set it and it will default to 1 as defined in the database
        if (parentDoodle != null) childDoodle.setTailLength(parentDoodle.getTailLength() + 1);
        // The root is the same as its parent
        // If it has no parent, its root is equal to its objectId, which will be set after it is saved
        if (parentDoodle != null) childDoodle.setRoot(parentDoodle.getRoot());
        // inGame is same as the parent
        // If it has no parent, inGame is passed in by intent
        if (parentDoodle != null) childDoodle.setInGame(parentDoodle.getInGame());
        else childDoodle.setInGame(inGame);

        savingProgressDialog.show();
        // Save doodle to database
        childDoodle.saveInBackground(e -> {
            savingProgressDialog.dismiss();
            if (e != null) { // Saving doodle failed
                Snackbar.make(doodleRelativeLayout, R.string.error_saving_doodle, Snackbar.LENGTH_LONG).show();
            }
            else { // Saving doodle succeeded
                // Now if it has no parent, set its root equal to its objectId
                if (parentDoodle == null) setRootToObjectId();
                else addToUserRootsContributedTo(parentDoodle.getRoot());

                // Handle push notification to the artist of the parent doodle
                if (parentDoodle != null) {
                    handlePushNotification(parentDoodle.getArtist());
                }

                Toast.makeText(this, R.string.doodle_submitted, Toast.LENGTH_SHORT).show();
                goHomeActivity();
            }
        });
    }

    // Sends push notification to the artist of the parent doodle
    private void handlePushNotification(ParseUser user) {
        final HashMap<String, Object> params = new HashMap<>();
        params.put("user", user.getObjectId());
        // Calling the cloud code function
        ParseCloud.callFunctionInBackground("doodlenotification", params, new FunctionCallback<Object>() {
            @Override
            public void done(Object response, ParseException e) {
                if(e == null) { // Function call succeeded
                    Log.i(TAG, "Notification sent");
                }
                else { // Function call failed
                    Log.e(TAG, "Notification failed", e);
                }
            }
        });
    }

    // Sets the root of a doodle to its objectId
    private void setRootToObjectId() {
        // Specify what type of data we want to query - Doodle.class
        ParseQuery<Doodle> query = ParseQuery.getQuery(Doodle.class);
        // Include data referred by user key
        // The doodle we want to change the root of is distinguished by having a null root
        query.whereEqualTo(Doodle.KEY_ROOT, null);
        // Start an asynchronous call for the doodle
        query.getFirstInBackground((doodle, e) -> {
            if (e != null) { // Query has failed
                Snackbar.make(doodleRelativeLayout, R.string.error_saving_doodle, Snackbar.LENGTH_LONG).show();
            }
            else { // Query has succeeded
                String root = doodle.getObjectId();
                doodle.setRoot(root);
                updateDoodle(doodle);
                addToUserRootsContributedTo(root);
            }
        });
    }

    // Adds the given root to the current user's list of roots contributed to
    private void addToUserRootsContributedTo (String root) {
        Player player = new Player(ParseUser.getCurrentUser());
        player.addRootContributedTo(root);
        player.saveInBackground(doodleRelativeLayout);
    }

    // Updates the doodle's data in the database
    private void updateDoodle(Doodle doodle) {
        doodle.saveInBackground(e -> {
            if (e != null) {
                Snackbar.make(doodleRelativeLayout, R.string.error_saving_doodle, Snackbar.LENGTH_LONG).show();
            }
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

    // Starts an intent to go to the home activity
    private void goHomeActivity() {
        Intent intent = new Intent(this, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
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

    // Log out user and send them back to login/signup page
    private void logout() {
        ProgressDialog logoutProgressDialog = new ProgressDialog(DoodleActivity.this);
        logoutProgressDialog.setMessage(getResources().getString(R.string.logging_out));
        logoutProgressDialog.setCancelable(false);
        logoutProgressDialog.show();
        ParseUser.logOutInBackground(e -> {
            logoutProgressDialog.dismiss();
            if (e != null) {
                Snackbar.make(doodleRelativeLayout, R.string.logout_failed, Snackbar.LENGTH_LONG).show();
            }
            else {
                goLoginSignupActivity();
                finish();
            }
        });
    }
}