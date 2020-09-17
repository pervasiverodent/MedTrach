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
import com.java.medtrach.common.LoadingAnimation;
import com.java.medtrach.common.ValidateInput;
import com.java.medtrach.model.User;

public class SignUpActivity extends AppCompatActivity {

    ValidateInput validateInput;
    LoadingAnimation loadingAnimation;
    User userModel;

    EditText emailEditText, passwordEditText, confirmPasswordEditText;
    TextView loginRedirectTextView;
    Button signUpButton;

    private FirebaseAuth mAuth;
    private FirebaseUser mUser;
    private FirebaseDatabase mDatabase;
    private DatabaseReference userReference;

    String userId, email, password, confirmPassword;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        emailEditText = findViewById(R.id.sign_up_username);
        passwordEditText = findViewById(R.id.sign_up_password);
        confirmPasswordEditText = findViewById(R.id.sign_up_confirm_password);
        loginRedirectTextView = findViewById(R.id.sign_up_login_text_view);
        signUpButton = findViewById(R.id.sign_up_button);

        // Loading animation
        loadingAnimation = new LoadingAnimation(SignUpActivity.this);

        mDatabase = FirebaseDatabase.getInstance();
        mUser = FirebaseAuth.getInstance().getCurrentUser();
        mAuth = FirebaseAuth.getInstance();
        userReference = FirebaseDatabase.getInstance().getReference(Common.USER_REF);


        validateInput = new ValidateInput(
            SignUpActivity.this, emailEditText, passwordEditText, confirmPasswordEditText
        );

        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                registerNewAccount();
            }
        });
    }

    private void registerNewAccount() {

        loadingAnimation.LoadingAnimationDialog();

        boolean emailVerified = validateInput.validateEmail();
        boolean passwordVerified = validateInput.validatePassword();
        boolean confirmPasswordVerified = validateInput.validateConfirmPassword();

        if(emailVerified && passwordVerified && confirmPasswordVerified) {
            email = emailEditText.getText().toString().trim();
            password = passwordEditText.getText().toString().trim();

            mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        // Firebase
                        mUser = FirebaseAuth.getInstance().getCurrentUser();
                        String userId = mUser.getUid().toString();

                        User userModel = new User();

                        userModel.setUid(userId);
                        userModel.setUsername(email);
                        userModel.setPassword(password);

                        userReference.child(userId).setValue(userModel);

                        if(task.isSuccessful()) {

                            // Redirect User to Home
                            Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
                            startActivity(intent);
                            //Loading animation
                            loadingAnimation.dismissLoadingAnimation();
                            finish();
                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(SignUpActivity.this, "Fatal Error", Toast.LENGTH_SHORT).show();
                            loadingAnimation.dismissLoadingAnimation();
                        }
                    }
                });

        } else {
            loadingAnimation.dismissLoadingAnimation();
        }
    }



}