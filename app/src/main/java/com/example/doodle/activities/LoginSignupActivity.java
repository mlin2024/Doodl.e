package com.example.doodle.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.example.doodle.R;
import com.google.android.material.snackbar.Snackbar;
import com.parse.ParseUser;

import net.cachapa.expandablelayout.ExpandableLayout;

public class LoginSignupActivity extends AppCompatActivity {
    public static final String TAG = "LoginSignupActivity";

    // Views in the layout
    private RelativeLayout loginSignupRelativeLayout;
    private LinearLayout loginSignupLinearLayout;
    private ExpandableLayout loginButtonExpandableLayout;
    private Button loginButton;
    private ExpandableLayout loginExpandableLayout;
    private EditText usernameEditTextLogin;
    private EditText passwordEditTextLogin;
    private Button loginGoButton;
    private ExpandableLayout signupButtonExpandableLayout;
    private Button signupButton;
    private ExpandableLayout signupExpandableLayout;
    private EditText usernameEditTextSignup;
    private EditText passwordEditTextSignup;
    private Button signupGoButton;

    // Other necessary member variables
    public Animation shake;
    private ProgressDialog verifyingProgressDialog;
    private TextWatcher textWatcher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_signup);

        if (ParseUser.getCurrentUser() != null) {
            goHomeActivity();
        }

        // Initialize the views in the layout
        loginSignupRelativeLayout = findViewById(R.id.loginSignupRelativeLayout);
        loginSignupLinearLayout = findViewById(R.id.loginSignupLinearLayout);
        loginButtonExpandableLayout = findViewById(R.id.loginButtonExpandableLayout);
        loginButton = findViewById(R.id.loginButton);
        loginExpandableLayout = findViewById(R.id.loginExpandableLayout);
        usernameEditTextLogin = findViewById(R.id.usernameEditTextLogin);
        passwordEditTextLogin = findViewById(R.id.passwordEditTextLogin);
        loginGoButton = findViewById(R.id.loginGoButton);
        signupButtonExpandableLayout = findViewById(R.id.signupButtonExpandableLayout);
        signupButton = findViewById(R.id.signupButton);
        signupExpandableLayout = findViewById(R.id.signupExpandableLayout);
        usernameEditTextSignup = findViewById(R.id.usernameEditTextSignup);
        passwordEditTextSignup = findViewById(R.id.passwordEditTextSignup);
        signupGoButton = findViewById(R.id.signupGoButton);

        // Initialize other member variables
        shake = AnimationUtils.loadAnimation(LoginSignupActivity.this, R.anim.shake);
        verifyingProgressDialog = new ProgressDialog(LoginSignupActivity.this);
        // TextWatcher to disable the go button unless both username and password have been filled in
        textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {}
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {}
            @Override
            public void afterTextChanged(Editable editable) {
                checkForEmptyFields();
            }
        };

        // Set up ProgressDialog
        verifyingProgressDialog.setMessage(getResources().getString(R.string.verifying_credentials));
        verifyingProgressDialog.setCancelable(false);

        // Set up TextWatcher
        usernameEditTextLogin.addTextChangedListener(textWatcher);
        passwordEditTextLogin.addTextChangedListener(textWatcher);
        usernameEditTextSignup.addTextChangedListener(textWatcher);
        passwordEditTextSignup.addTextChangedListener(textWatcher);
        checkForEmptyFields();

        loginButton.setOnClickListener(v -> {
            loginExpandableLayout.expand();
            loginButtonExpandableLayout.collapse();
            signupExpandableLayout.collapse();
            signupButtonExpandableLayout.expand();
            clearEditTexts();
        });

        signupButton.setOnClickListener(v -> {
            signupExpandableLayout.expand();
            signupButtonExpandableLayout.collapse();
            loginExpandableLayout.collapse();
            loginButtonExpandableLayout.expand();
            clearEditTexts();
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

    // Checks if the username and password fields are empty, and enables the button only if they are both populated
    private void checkForEmptyFields() {
        String loginUsername = usernameEditTextLogin.getText().toString();
        String loginPassword = passwordEditTextLogin.getText().toString();
        if (loginUsername.isEmpty() || loginPassword.isEmpty()) loginGoButton.setEnabled(false);
        else loginGoButton.setEnabled(true);

        String signupUsername = usernameEditTextSignup.getText().toString();
        String signupPassword = passwordEditTextSignup.getText().toString();
        if (signupUsername.isEmpty() || signupPassword.isEmpty()) signupGoButton.setEnabled(false);
        else signupGoButton.setEnabled(true);
    }

    // Uses parse method logInInBackground to attempt to log in with the credentials given
    private void loginUser(String username, String password) {
        verifyingProgressDialog.show();
        ParseUser.logInInBackground(username, password, (user, e) -> {
            verifyingProgressDialog.dismiss();
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
        verifyingProgressDialog.show();
        user.signUpInBackground(e -> {
            verifyingProgressDialog.dismiss();
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