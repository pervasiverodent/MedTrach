package com.java.medtrach.common;

import android.content.Context;
import android.util.Patterns;
import android.widget.EditText;
import android.widget.Toast;

public class ValidateInput {
    Context context;
    EditText email, password, confirmPassword;

    String emailInput, passwordInput, confirmPasswordInput;

    /**
     * SignInActivity.class
     */

    public ValidateInput(Context myContext, EditText myEmail, EditText myPassword) {
        context = myContext;
        email = myEmail;
        password = myPassword;
    }

    /**
     * SignUpActivity.class
     */

    public ValidateInput(Context myContext, EditText myEmail, EditText myPassword, EditText myConfirmPassword) {
        context = myContext;
        email = myEmail;
        password = myPassword;
        confirmPassword = myConfirmPassword;
    }

    /**
     * AddDrugActivity.class
     */

    /**
     * AddPharmacyActivity.class
     */

    public boolean validateEmail() {
        emailInput = email.getText().toString().trim();

        if(emailInput.isEmpty()) {
            Toast.makeText(context, "Your email address is empty.", Toast.LENGTH_SHORT).show();
            return false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(emailInput).matches()) {
            Toast.makeText(context, "Invalid email address input.", Toast.LENGTH_SHORT).show();
            return false;
        } else {
            return true;
        }
    }

    public boolean validatePassword() {
        passwordInput = password.getText().toString().trim();

        if(passwordInput.isEmpty()) {
            Toast.makeText(context, "Please enter your password.", Toast.LENGTH_SHORT).show();
            return false;
        } else if (passwordInput.length() < 8) {
            Toast.makeText(context, "Password is too short. Less than 8 characters", Toast.LENGTH_SHORT).show();
            return false;
        } else {
            return true;
        }
    }

    public boolean validateConfirmPassword() {
        confirmPasswordInput = confirmPassword.getText().toString().trim();

        if(confirmPasswordInput.isEmpty()) {
            Toast.makeText(context, "Please fill out all fields.", Toast.LENGTH_SHORT).show();
            return false;
        } else if (!confirmPasswordInput.equals(passwordInput)) {
            Toast.makeText(context, "Passwords dont match.", Toast.LENGTH_SHORT).show();
            return false;
        } else {
            return true;
        }
    }
}
