package com.example.budgetapp.user;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.budgetapp.R;
import com.example.budgetapp.model.UserDetailsModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class SignupActivity extends AppCompatActivity {

    EditText mEmailUser, mPassword, mRetypePassword;
    Button mCreateAccount;

    FirebaseAuth mFirebaseAuth;
    ProgressBar mProgressBar;

    FirebaseFirestore db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        initializeFields();

        mFirebaseAuth = FirebaseAuth.getInstance();

        mCreateAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = mEmailUser.getText().toString();
                String password = mPassword.getText().toString();
                String passwordRetype = mRetypePassword.getText().toString();

                if (TextUtils.isEmpty(email)) {
                    Toast.makeText(SignupActivity.this, "Please enter an email", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(password)) {
                    Toast.makeText(SignupActivity.this, "Please enter a password", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(password.length() < 6) {
                    mPassword.setError("Password Must be >= 6 Characters");
                    return;
                }
                if (!password.equals(passwordRetype)) {
                    mRetypePassword.setError("Password is not the same");
                    return;
                }

                mProgressBar.setVisibility(View.VISIBLE);
                mFirebaseAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(task -> {
                            mProgressBar.setVisibility(View.GONE);

                            if (task.isSuccessful()) {
                                Toast.makeText(SignupActivity.this, "Account successfully created", Toast.LENGTH_SHORT).show();
                                FirebaseUser currentUser = mFirebaseAuth.getCurrentUser();
                                createFirestoreCollectionForUser(currentUser);
                            } else {
                                Toast.makeText(SignupActivity.this, "Error ! " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                }
        });
    }

    private void createFirestoreCollectionForUser(FirebaseUser user) {
        DocumentReference users = db.collection("users").document(FirebaseAuth.getInstance().getCurrentUser().getUid());

        UserDetailsModel currentUser = new UserDetailsModel(user.getDisplayName(), user.getEmail());

        users.set(currentUser).addOnSuccessListener(aVoid -> {
            Log.d("TAG", "Added user in database" + FirebaseAuth.getInstance().getCurrentUser().getUid());
            goToLoginPage();
        });
    }

    private void goToLoginPage() {
        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
        startActivity(intent);
    }

    private void initializeFields() {
        mEmailUser = findViewById(R.id.registerEmail);
        mPassword = findViewById(R.id.registerPassword);
        mRetypePassword = findViewById(R.id.registerPassword2);
        mCreateAccount = findViewById(R.id.createAccount);
        mProgressBar = findViewById(R.id.progressBar2);
        db = FirebaseFirestore.getInstance();
    }
}
