package com.example.doodle.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.divyanshu.draw.widget.DrawView;
import com.example.doodle.BitmapScaler;
import com.example.doodle.R;
import com.example.doodle.models.Doodle;
import com.google.android.material.snackbar.Snackbar;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class DoodleActivity extends AppCompatActivity {
    public static final String TAG = "DoodleActivity";
    public static final float STROKE_WIDTH = 15;
    public static final String PARENT_DOODLE_ID = "ParentDoodleId";
    public static final String IN_GAME = "inGame";
    public static final String SET_ROOT = "setRoot";

    private RelativeLayout doodleRelativeLayout;
    private Toolbar toolbar;
    private DrawView doodleDrawView;
    private Button doneButton;

    private Doodle parentDoodle;
    private boolean inGame;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doodle);

        doodleRelativeLayout = findViewById(R.id.doodleRelativeLayout);
        toolbar = findViewById(R.id.doodleToolbar);
        toolbar.setTitleTextColor(getResources().getColor(R.color.white));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        doodleDrawView = findViewById(R.id.doodleDrawView);
        doneButton = findViewById(R.id.doneButton);

        // Get parent doodle from intent
        String parentDoodleId = getIntent().getStringExtra(PARENT_DOODLE_ID);
        Doodle parentDoodle = queryDoodle(parentDoodleId);

        // Get inGame from intent
        inGame = getIntent().getBooleanExtra(IN_GAME, false);

        progressDialog = new ProgressDialog(DoodleActivity.this);
        progressDialog.setMessage(getResources().getString(R.string.saving_doodle));

        // Prepare canvas
        doodleDrawView.clearCanvas();
        doodleDrawView.setStrokeWidth(STROKE_WIDTH);
        doodleDrawView.setColor(R.color.black);

        doneButton.setOnClickListener(v -> {
            Bitmap drawingBitmap = doodleDrawView.getBitmap();
            File drawingFile = saveBitmapToFile(drawingBitmap);
            saveDoodle(parentDoodle, drawingFile);
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_menu, menu);
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

    private void logout() {
        ProgressDialog logoutProgressDialog = new ProgressDialog(DoodleActivity.this);
        logoutProgressDialog.setMessage(getResources().getString(R.string.logging_out));
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

    private Doodle queryDoodle(String objectId) {
        if (objectId == null) return null;

        // Specify what type of data we want to query - Doodle.class
        ParseQuery<Doodle> query = ParseQuery.getQuery(Doodle.class);
        // Include data referred by objectId key
        query.whereEqualTo(Doodle.KEY_OBJECT_ID, objectId);
        // Start an asynchronous call for the doodle
        query.getFirstInBackground((foundDoodle, e) -> {
            if (e != null) { // Query has failed
                Snackbar.make(doodleRelativeLayout, R.string.error_finding_doodle, Snackbar.LENGTH_LONG).show();
                finish();
            }
            else { // Query has succeeded
                parentDoodle = foundDoodle;
                return;
            }
        });
        return parentDoodle;
    }

    private File saveBitmapToFile(Bitmap drawingBitmap) {
        try {
            Bitmap resizedBitmap = BitmapScaler.scaleToFitWidth(drawingBitmap, 1000);
            // Configure byte output stream
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            // Compress the image further
            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 40, bytes);
            // Create a new file for the resized bitmap
            File resizedFile = getPhotoFileUri("doodle" + System.currentTimeMillis() + ".png");
            resizedFile.createNewFile();
            FileOutputStream fos = new FileOutputStream(resizedFile);
            // Write the bytes of the bitmap to file
            fos.write(bytes.toByteArray());
            fos.close();
            return resizedFile;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    // Returns the File for a photo stored on disk given the fileName
    private File getPhotoFileUri(String fileName) {
        // Get safe storage directory for photos
        // Use `getExternalFilesDir` on Context to access package-specific directories.
        // This way, we don't need to request external read/write runtime permissions.
        File mediaStorageDir = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), TAG);

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists() && !mediaStorageDir.mkdirs()) {
            Log.d(TAG, "Failed to create directory");
        }

        // Return the file target for the photo based on filename
        File file = new File(mediaStorageDir.getPath() + File.separator + fileName);

        return file;
    }

    private void saveDoodle(Doodle parentDoodle, File drawingFile) {
        Doodle childDoodle = new Doodle();

        // The artist is the current artist
        childDoodle.setArtist(ParseUser.getCurrentUser());
        // The image is the image that was just drawn
        childDoodle.setImage(new ParseFile(drawingFile));
        // The parent is the doodle that was passed in via intent
        // If it has no parent, just don't set it and let it default to the default defined in the database
        if (parentDoodle != null) childDoodle.setParent(parentDoodle);
        // The tail length is just one longer than it's parent
        // If it doesn't have a parent, don't set it and it will default to 1 as defined in the database
        if (parentDoodle != null) childDoodle.setTailLength(parentDoodle.getTailLength() + 1);
        // The root is the same as its parent
        // If it has no parent, its root is equal to its objectId, which will be set after it is saved
        // For now set its root equal to setRoot so we know we have to set it
        if (parentDoodle != null) childDoodle.setRoot(parentDoodle.getRoot());
        else childDoodle.setRoot(SET_ROOT);
        // inGame is same as the parent
        // If it has no parent, inGame is passed in by intent
        if (parentDoodle != null) childDoodle.setInGame(parentDoodle.getInGame());
        else childDoodle.setInGame(inGame);

        progressDialog.show();
        // Save doodle to database
        childDoodle.saveInBackground(e -> {
            progressDialog.dismiss();
            if (e != null) { // Saving doodle failed
                Snackbar.make(doodleRelativeLayout, R.string.error_saving_doodle, Snackbar.LENGTH_LONG).show();
            }
            else { // Saving doodle succeeded
                // Now if it has no parent, set its root equal to its objectId
                setRootToObjectId();

                Toast.makeText(this, R.string.doodle_submitted, Toast.LENGTH_SHORT).show();
                goHomeActivity();
            }
        });
    }

    private void setRootToObjectId() {
        // Specify what type of data we want to query - Doodle.class
        ParseQuery<Doodle> query = ParseQuery.getQuery(Doodle.class);
        // Include data referred by user key
        query.whereEqualTo(Doodle.KEY_ROOT, SET_ROOT);
        // Start an asynchronous call for the doodle
        query.findInBackground((foundDoodles, e) -> {
            if (e != null) { // Query has failed
                Snackbar.make(doodleRelativeLayout, R.string.error_saving_doodle, Snackbar.LENGTH_LONG).show();
            }
            else { // Query has succeeded
                for (Doodle doodle: foundDoodles) {
                    doodle.setRoot(doodle.getObjectId());
                    doodle.saveInBackground(e1 -> {
                        if (e1 != null) {
                            Snackbar.make(doodleRelativeLayout, R.string.error_saving_doodle, Snackbar.LENGTH_LONG).show();
                        }
                    });
                }
            }
        });
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
}