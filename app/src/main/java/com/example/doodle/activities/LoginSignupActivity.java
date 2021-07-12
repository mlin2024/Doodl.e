package com.example.doodle.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.doodle.R;

import net.cachapa.expandablelayout.ExpandableLayout;

public class LoginSignupActivity extends AppCompatActivity {

    private RelativeLayout loginSignupRelativeLayout;
    private LinearLayout loginSignupLinearLayout;
    private TextView titleText;
    private Button loginButton;
    private ExpandableLayout loginExpandableLayout;
    private EditText usernameEditTextLogin;
    private EditText passwordEditTextLogin;
    private Button signupButton;
    private ExpandableLayout signupExpandableLayout;
    private EditText usernameEditTextSignup;
    private EditText passwordEditTextSignup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_signup);

        loginSignupRelativeLayout = findViewById(R.id.loginSignupRelativeLayout);
        loginSignupLinearLayout = findViewById(R.id.loginSignupLinearLayout);
        titleText = findViewById(R.id.titleText);
        loginButton = findViewById(R.id.loginButton);
        loginExpandableLayout = findViewById(R.id.loginExpandableLayout);
        usernameEditTextLogin = findViewById(R.id.usernameEditTextLogin);
        passwordEditTextLogin = findViewById(R.id.passwordEditTextLogin);
        signupButton = findViewById(R.id.signupButton);
        signupExpandableLayout = findViewById(R.id.signupExpandableLayout);
        usernameEditTextSignup = findViewById(R.id.usernameEditTextSignup);
        passwordEditTextSignup = findViewById(R.id.passwordEditTextSignup);

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
    }
}