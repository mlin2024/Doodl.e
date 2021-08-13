package com.example.doodle.activities;

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
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.doodle.BitmapScaler;
import com.example.doodle.R;
import com.example.doodle.fragments.CanvasFragment;
import com.example.doodle.models.Doodle;
import com.example.doodle.models.Player;
import com.google.android.material.snackbar.Snackbar;
import com.parse.FunctionCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;

public class DoodleActivity extends AppCompatActivity {
    public static final String TAG = "DoodleActivity";
    public static final String PARENT_DOODLE = "ParentDoodle";

    // Views in the layout
    private RelativeLayout doodleRelativeLayout;
    private Toolbar toolbar;

    // Other necessary member variables
    private ProgressDialog savingProgressDialog;
    private FragmentManager fragmentManager;
    private Fragment canvasFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doodle);

        // Initialize the views in the layout
        doodleRelativeLayout = findViewById(R.id.doodleRelativeLayout);
        toolbar = findViewById(R.id.doodleToolbar);

        // Initialize other member variables
        savingProgressDialog = new ProgressDialog(DoodleActivity.this);
        fragmentManager = getSupportFragmentManager();
        Doodle parentDoodle = getIntent().getParcelableExtra(PARENT_DOODLE);
        Bitmap parentBitmap = getBitmapFromDoodle(parentDoodle);
        // There is no timer, so deadline is -1
        canvasFragment = CanvasFragment.newInstance(parentBitmap, -1);

        // Set up toolbar
        toolbar.setTitleTextColor(getResources().getColor(R.color.white, getTheme()));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        // Set up ProgressDialog
        savingProgressDialog.setMessage(getResources().getString(R.string.saving_doodle));
        savingProgressDialog.setCancelable(false);

        // Set up canvas fragment
        fragmentManager.beginTransaction().add(R.id.canvasFrameLayout, canvasFragment).show(canvasFragment).commit();

        // Listen for result from fragment
        fragmentManager.setFragmentResultListener(CanvasFragment.RESULT_DOODLE, this, (requestKey, bundle) -> {
            Bitmap drawingBitmap = bundle.getParcelable(CanvasFragment.DRAWING_BITMAP);
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
                Snackbar.make(doodleRelativeLayout, getResources().getString(R.string.error_finding_doodle), Snackbar.LENGTH_LONG).show();
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
        // The parent is the doodle that was passed in via intent
        // If it has no parent, just don't set it and let it default to the default defined in the database
        if (parentDoodle != null) childDoodle.setParent(parentDoodle);
        // The tail length is just one longer than it's parent
        // If it doesn't have a parent, don't set it and it will default to 1 as defined in the database
        if (parentDoodle != null) childDoodle.setTailLength(parentDoodle.getTailLength() + 1);
        // The root is the same as its parent
        // If it has no parent, its root is equal to its objectId, which will be set after it is saved
        if (parentDoodle != null) childDoodle.setRoot(parentDoodle.getRoot());

        savingProgressDialog.show();
        // Save doodle to database
        childDoodle.saveInBackground(e -> {
            if (e != null) { // Save has failed
                Snackbar.make(doodleRelativeLayout, getResources().getString(R.string.error_saving_doodle), Snackbar.LENGTH_LONG).show();
            }
            else { // Save has succeeded
                // Now if it has no parent, set its root equal to its objectId
                if (parentDoodle == null) setRootToObjectId();
                else addToUserRootsContributedTo(parentDoodle.getRoot());

                // Handle push notification to the artist of the parent doodle
                if (parentDoodle != null) {
                    try {
                        Player player = new Player(parentDoodle.getArtist().fetchIfNeeded());
                        // If the artist has enabled notifications, send them notification
                        if (player.getGetsNotifications()) {
                            handlePushNotification(parentDoodle.getArtist());
                        }
                    } catch (ParseException e1) {
                        Snackbar.make(doodleRelativeLayout, getResources().getString(R.string.error_saving_doodle), Snackbar.LENGTH_LONG).show();
                        e1.printStackTrace();
                    }
                }
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
                }
                else { // Function call failed
                    e.printStackTrace();
                }
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
        query.getFirstInBackground((doodle, e) -> {
            if (e != null) { // Query has failed
                savingProgressDialog.dismiss();
                Snackbar.make(doodleRelativeLayout, getResources().getString(R.string.error_saving_doodle), Snackbar.LENGTH_LONG).show();
            }
            else { // Query has succeeded
                String root = doodle.getObjectId();
                doodle.setRoot(root);
                doodle.saveInBackground(e1 -> {
                    if (e1 != null) { // Save has failed
                        Snackbar.make(doodleRelativeLayout, getResources().getString(R.string.error_saving_doodle), Snackbar.LENGTH_LONG).show();
                    }
                    else { // Save has succeeded
                        addToUserRootsContributedTo(root);
                    }
                });
            }
        });
    }

    // Adds the given root to the current user's list of roots contributed to
    private void addToUserRootsContributedTo (String root) {
        Player player = new Player(ParseUser.getCurrentUser());
        player.addRootContributedTo(root);
        player.saveInBackground(e -> {
            if (e != null) { // Save has failed
                Snackbar.make(doodleRelativeLayout, getResources().getString(R.string.error_saving_doodle), Snackbar.LENGTH_LONG).show();
            }
            else { // Save has succeeded
                savingProgressDialog.dismiss();
                Toast.makeText(this, getResources().getString(R.string.doodle_submitted), Toast.LENGTH_SHORT).show();
                goHomeActivity();
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
            if (e != null) { // Logout has failed
                Snackbar.make(doodleRelativeLayout, getResources().getString(R.string.logout_failed), Snackbar.LENGTH_LONG).show();
            }
            else { // Logout has succeeded
                goLoginSignupActivity();
                finish();
            }
        });
    }
}