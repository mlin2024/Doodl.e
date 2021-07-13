package com.example.doodle.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.RelativeLayout;

import com.example.doodle.R;
import com.google.android.material.snackbar.Snackbar;
import com.parse.ParseUser;

public class GalleryActivity extends AppCompatActivity {
    public static final String TAG = "GalleryActivity";

    RelativeLayout galleryRelativeLayout;
    private Toolbar toolbar;
    RecyclerView galleryRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        galleryRelativeLayout = findViewById(R.id.galleryRelativeLayout);
        toolbar = findViewById(R.id.galleryActivityToolbar);
        toolbar.setTitleTextColor(getResources().getColor(R.color.white));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        galleryRecyclerView = findViewById(R.id.galleryRecyclerView);

        // Allows for optimizations
        galleryRecyclerView.setHasFixedSize(true);

        // Define 2 column grid layout with a new GridLayoutManager
        galleryRecyclerView.setLayoutManager(new GridLayoutManager(GalleryActivity.this, 2));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.profile_menu, menu);
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
                Snackbar.make(galleryRelativeLayout, R.string.logout_failed, Snackbar.LENGTH_LONG).show();
            }
            else {
                finish();
            }
        });
    }
}