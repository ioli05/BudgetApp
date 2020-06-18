package com.example.budgetapp.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.budgetapp.R;
import com.example.budgetapp.model.UserDetailsModel;
import com.example.budgetapp.utils.DateInputMask;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static java.util.Objects.isNull;

public class SignupActivity extends AppCompatActivity {

    EditText mEmailUser, mPassword, mRetypePassword, mDate, mAge;
    Button mCreateAccount;
    CheckBox useDataAccept;

    FirebaseAuth mFirebaseAuth;
    ProgressBar mProgressBar;

    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        initializeFields();

        mFirebaseAuth = FirebaseAuth.getInstance();

        mCreateAccount.setOnClickListener(v -> {

            String email = mEmailUser.getText().toString();
            String password = mPassword.getText().toString();
            String passwordRetype = mRetypePassword.getText().toString();

            if (!validateEntries(email, password, passwordRetype)) {
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
        });
    }

    private boolean validateEntries(String email, String password, String passwordRetype) {

        if (TextUtils.isEmpty(email)) {
            Toast.makeText(SignupActivity.this, "Please enter an email", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (TextUtils.isEmpty(password)) {
            Toast.makeText(SignupActivity.this, "Please enter a password", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (password.length() < 6) {
            mPassword.setError("Password Must be >= 6 Characters");
            return false;
        }
        if (!password.equals(passwordRetype)) {
            mRetypePassword.setError("Password is not the same");
            return false;
        }
        return true;
    }

    private Date getDate(String date) {
        Date tranzactionDate = null;
        if (!date.equals("")) {
            try {
                tranzactionDate = new SimpleDateFormat("MM/dd/yyyy").parse(date);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        } else {
            try {
                tranzactionDate = new SimpleDateFormat("MM/dd/yyyy").parse(new Date().toString());
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return tranzactionDate;
    }

    private void createFirestoreCollectionForUser(FirebaseUser user) {
        DocumentReference users =
                db.collection("users").document(FirebaseAuth.getInstance()
                        .getCurrentUser().getUid());

        Date date = getDate(mDate.getText().toString());
        if (isNull(date)) {
            date = new Date();
        }
        Integer age = Integer.parseInt(mAge.getText().toString());
        Boolean useData = useDataAccept.isChecked();
        Date createdAt = new Date();
        UserDetailsModel currentUser = new UserDetailsModel(user.getEmail(), date, age, useData,
                false, createdAt);

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
        mDate = findViewById(R.id.date_to_reset);
        mAge = findViewById(R.id.age);
        useDataAccept = findViewById(R.id.check_data_usage);

        mCreateAccount = findViewById(R.id.createAccount);
        mProgressBar = findViewById(R.id.progressBar2);

        new DateInputMask(mDate);
        db = FirebaseFirestore.getInstance();
    }
}
