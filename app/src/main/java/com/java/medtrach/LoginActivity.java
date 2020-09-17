package com.java.medtrach;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.java.medtrach.R;
import com.java.medtrach.common.LoadingAnimation;
import com.java.medtrach.common.ValidateInput;

public class LoginActivity extends AppCompatActivity {

    EditText userNameEditText, passwordEditText;
    Button loginButton;
    TextView signUpTextView;

    String email, password;

    ValidateInput validateInput;
    LoadingAnimation loadingAnimation;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        userNameEditText = findViewById(R.id.login_username);
        passwordEditText = findViewById(R.id.login_password);
        loginButton = findViewById(R.id.login_button);
        signUpTextView = findViewById(R.id.login_sign_up_text);

        validateInput = new ValidateInput(
                LoginActivity.this,
                userNameEditText,
                passwordEditText
        );

        // Firebase Authentication
        mAuth = FirebaseAuth.getInstance();

        // Loading Animation
        loadingAnimation = new LoadingAnimation(LoginActivity.this);

        signUpTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
                startActivity(intent);
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signInUser();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser mUser = mAuth.getCurrentUser();

        if(mUser != null) {
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
        } else {
            Toast.makeText(this, "Please login to continue.", Toast.LENGTH_SHORT).show();
        }

    }

    private void signInUser() {
        loadingAnimation.LoadingAnimationDialog();

        boolean emailVerified = validateInput.validateEmail();
        boolean passwordVerified = validateInput.validatePassword();

        if(emailVerified && passwordVerified) {
            email = userNameEditText.getText().toString().trim();
            password = passwordEditText.getText().toString().trim();

            mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()) {
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            startActivity(intent);
                            loadingAnimation.dismissLoadingAnimation();
                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(LoginActivity.this, "Fatal Error.", Toast.LENGTH_SHORT).show();
                            loadingAnimation.dismissLoadingAnimation();
                        }
                    }
                });
        } else {
            loadingAnimation.dismissLoadingAnimation();
        }
    }
}