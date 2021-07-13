package com.example.doodle.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.doodle.R;
import com.google.android.material.snackbar.Snackbar;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

import net.cachapa.expandablelayout.ExpandableLayout;

public class LoginSignupActivity extends AppCompatActivity {
    public static final String TAG = "LoginSignupActivity";

    private RelativeLayout loginSignupRelativeLayout;
    private LinearLayout loginSignupLinearLayout;
    private Button loginButton;
    private ExpandableLayout loginExpandableLayout;
    private EditText usernameEditTextLogin;
    private EditText passwordEditTextLogin;
    private Button loginGoButton;
    private Button signupButton;
    private ExpandableLayout signupExpandableLayout;
    private EditText usernameEditTextSignup;
    private EditText passwordEditTextSignup;
    private Button signupGoButton;

    public Animation shake;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_signup);

        if (ParseUser.getCurrentUser() != null) {
            goHomeActivity();
        }

        loginSignupRelativeLayout = findViewById(R.id.loginSignupRelativeLayout);
        loginSignupLinearLayout = findViewById(R.id.loginSignupLinearLayout);
        loginButton = findViewById(R.id.loginButton);
        loginExpandableLayout = findViewById(R.id.loginExpandableLayout);
        usernameEditTextLogin = findViewById(R.id.usernameEditTextLogin);
        passwordEditTextLogin = findViewById(R.id.passwordEditTextLogin);
        loginGoButton = findViewById(R.id.loginGoButton);
        signupButton = findViewById(R.id.signupButton);
        signupExpandableLayout = findViewById(R.id.signupExpandableLayout);
        usernameEditTextSignup = findViewById(R.id.usernameEditTextSignup);
        passwordEditTextSignup = findViewById(R.id.passwordEditTextSignup);
        signupGoButton = findViewById(R.id.signupGoButton);

        shake = AnimationUtils.loadAnimation(LoginSignupActivity.this, R.anim.shake);
        progressDialog = new ProgressDialog(LoginSignupActivity.this);
        progressDialog.setMessage(getResources().getString(R.string.verifying_credentials));

        loginButton.setOnClickListener(v -> {
            if (loginExpandableLayout.isExpanded()) loginExpandableLayout.collapse();
            else loginExpandableLayout.expand();
            signupExpandableLayout.collapse();
        });

        signupButton.setOnClickListener(v -> {
            if (signupExpandableLayout.isExpanded()) signupExpandableLayout.collapse();
            else signupExpandableLayout.expand();
            loginExpandableLayout.collapse();
        });

        loginGoButton.setOnClickListener(v -> {
            String username = usernameEditTextLogin.getText().toString();
            String password = passwordEditTextLogin.getText().toString();
            hideSoftKeyboard(loginSignupRelativeLayout);
            loginUser(username, password);
        });

        signupGoButton.setOnClickListener(v -> {
            String username = usernameEditTextSignup.getText().toString();
            String password = passwordEditTextSignup.getText().toString();
            hideSoftKeyboard(loginSignupRelativeLayout);
            signupUser(username, password);
        });
    }

    // Uses parse method logInInBackground to attempt to log in with the credentials given
    private void loginUser(String username, String password) {
        progressDialog.show();
        ParseUser.logInInBackground(username, password, (user, e) -> {
            progressDialog.dismiss();
            if (e != null) { // The login failed
                Snackbar.make(loginSignupRelativeLayout, R.string.login_failed, Snackbar.LENGTH_LONG).show();
                loginSignupLinearLayout.startAnimation(shake);
                return;
            }
            else { // The login succeded
                clearEditTexts();
                goHomeActivity();
            }
        });
    }

    // Uses parse method signUpInBackground to attempt to sign up with the credentials given
    private void signupUser(String username, String password) {
        // Create the ParseUser
        ParseUser user = new ParseUser();
        // Set core properties
        user.setUsername(username);
        user.setPassword(password);
        progressDialog.show();
        user.signUpInBackground(e -> {
            progressDialog.dismiss();
            if (e != null) { // The signup failed
                Snackbar.make(loginSignupRelativeLayout, R.string.signup_failed, Snackbar.LENGTH_LONG).show();
                loginSignupLinearLayout.startAnimation(shake);
                return;
            }
            else { // The signup succeeded
                clearEditTexts();
                goHomeActivity();
            }
        });
    }

    // Clears the EditTexts
    private void clearEditTexts() {
        usernameEditTextLogin.setText("");
        passwordEditTextLogin.setText("");
        usernameEditTextSignup.setText("");
        passwordEditTextSignup.setText("");
    }

    // Starts an intent to go to the main activity
    private void goHomeActivity() {
        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);
    }

    // Minimizes the soft keyboard
    private void hideSoftKeyboard(View view){
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
}