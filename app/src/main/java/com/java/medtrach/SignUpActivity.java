package com.java.medtrach;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.java.medtrach.R;
import com.java.medtrach.common.Common;
import com.java.medtrach.model.User;

public class SignUpActivity extends AppCompatActivity {

    private EditText usernameEditText, passwordEditText, confirmPasswordEditText;
    private TextView loginRedirectTextView;
    private Button signUpButton;

    private FirebaseAuth mAuth;
    private FirebaseUser mUser;
    private FirebaseDatabase mDatabase;
    private DatabaseReference userReference;

    String userId, username, password, confirmPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        initializeContent();
        initializeFirebase();


        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkUserInput();
            }
        });
    }

    private void initializeContent() {
        usernameEditText = findViewById(R.id.sign_up_username);
        passwordEditText = findViewById(R.id.sign_up_password);
        confirmPasswordEditText = findViewById(R.id.sign_up_confirm_password);
        loginRedirectTextView = findViewById(R.id.sign_up_login_text_view);
        signUpButton = findViewById(R.id.sign_up_button);

        mDatabase = FirebaseDatabase.getInstance();
        mUser = FirebaseAuth.getInstance().getCurrentUser();
        mAuth = FirebaseAuth.getInstance();
        userReference = FirebaseDatabase.getInstance().getReference(Common.USER_REF);
    }

    private void initializeFirebase() {

    }

    private void checkUserInput() {
        final String email = usernameEditText.getText().toString().trim();
        final String password = passwordEditText.getText().toString().trim();
        final String confirmPassword = confirmPasswordEditText.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, "Username must not be empty.", Toast.LENGTH_SHORT).show();
        }

        if(TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Password must not be empty.", Toast.LENGTH_SHORT).show();
        }

        if(TextUtils.isEmpty(confirmPassword)) {
            Toast.makeText(this, "Confirm password must not be empty.", Toast.LENGTH_SHORT).show();
        }

        if(password.length() <= 6) {
            Toast.makeText(this, "Password length cannot be less than six characters.", Toast.LENGTH_SHORT).show();
        }

        if(!password.equals(confirmPassword)) {
            Toast.makeText(this, "Please check your password fields.", Toast.LENGTH_SHORT).show();
        }

        mAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    mUser = FirebaseAuth.getInstance().getCurrentUser();
                    String userId = mUser.getUid();

                    User user = new User();

                    user.setUid(userId);
                    user.setUsername(username);
                    user.setPassword(password);

                    userReference.child(userId).setValue(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(SignUpActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

                    if(!task.isSuccessful()) {
                        Toast.makeText(SignUpActivity.this, "Register failed.", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(SignUpActivity.this, "User has successfully registered!", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
                        startActivity(intent);
                    }

                }
            });


    }



}