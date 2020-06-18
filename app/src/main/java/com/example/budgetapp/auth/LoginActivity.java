package com.example.budgetapp.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.budgetapp.R;
import com.example.budgetapp.tabset.MainActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {

    EditText mEmailUser, mPasswordUser;
    Button mLoginButton, mSignupButton;
    ProgressBar mProgressBar;
    FirebaseAuth mFirebaseAuth;

    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);

        initialiseFields();

        if (checkIfUserIsLogged()) {
            goToMain();
        }

        mLoginButton.setOnClickListener(v -> {

            String email = mEmailUser.getText().toString().trim();
            String password = mPasswordUser.getText().toString().trim();

            if (!validateFields(email, password)) {
                return;
            }

            mProgressBar.setVisibility(View.VISIBLE);

            mFirebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(LoginActivity.this, "Logged in Successfully", Toast.LENGTH_SHORT).show();
                    goToMain();
                } else {
                    Toast.makeText(LoginActivity.this, "Error ! " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    mProgressBar.setVisibility(View.GONE);
                }

            });

        });

        mSignupButton.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(), SignupActivity.class)));
    }

    private void goToMain() {
        startActivity(new Intent(getApplicationContext(), MainActivity.class));
    }

    private boolean checkIfUserIsLogged() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            return true;
        }
        return false;
    }

    private boolean validateFields(String email, String password) {
        if (TextUtils.isEmpty(email)) {
            mEmailUser.setError("Email is Required.");
            return false;
        }

        if (TextUtils.isEmpty(password)) {
            mPasswordUser.setError("Password is Required.");
            return false;
        }

        if (password.length() < 6) {
            mPasswordUser.setError("Password Must be >= 6 Characters");
            return false;
        }
        return true;
    }

    private void initialiseFields() {
        mEmailUser = findViewById(R.id.edittextEmail);
        mPasswordUser = findViewById(R.id.registerPassword);
        mProgressBar = findViewById(R.id.progressBar);
        mFirebaseAuth = FirebaseAuth.getInstance();
        mLoginButton = findViewById(R.id.loginButton);
        mSignupButton = findViewById(R.id.signupButton);

        db = FirebaseFirestore.getInstance();
    }
}
