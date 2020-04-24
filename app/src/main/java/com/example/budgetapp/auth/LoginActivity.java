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

        mEmailUser = findViewById(R.id.edittextEmail);
        mPasswordUser = findViewById(R.id.registerPassword);
        mProgressBar = findViewById(R.id.progressBar);
        mFirebaseAuth = FirebaseAuth.getInstance();
        mLoginButton = findViewById(R.id.loginButton);
        mSignupButton = findViewById(R.id.signupButton);

        db = FirebaseFirestore.getInstance();

        mEmailUser.setText("alex@gmail.com");
        mPasswordUser.setText("parola");

        mLoginButton.setOnClickListener(v -> {

            String email = mEmailUser.getText().toString().trim();
            String password = mPasswordUser.getText().toString().trim();

            if (TextUtils.isEmpty(email)) {
                mEmailUser.setError("Email is Required.");
                return;
            }

            if (TextUtils.isEmpty(password)) {
                mPasswordUser.setError("Password is Required.");
                return;
            }

            if (password.length() < 6) {
                mPasswordUser.setError("Password Must be >= 6 Characters");
                return;
            }

            mProgressBar.setVisibility(View.VISIBLE);

            // authenticate the user

            mFirebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {

                    Toast.makeText(LoginActivity.this, "Logged in Successfully", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(getApplicationContext(), MainActivity.class));
                } else {
                    Toast.makeText(LoginActivity.this, "Error ! " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    mProgressBar.setVisibility(View.GONE);
                }

            });

        });

        mLoginButton.performClick();
        mSignupButton.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(), SignupActivity.class)));
    }
}
