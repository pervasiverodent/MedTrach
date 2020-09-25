package com.java.medtrach.common;

import android.content.Context;
import android.widget.EditText;
import android.widget.Toast;

public class ValidateDrugInput {
    Context context;
    EditText drugName, drugDescription;

    String drugNameInput, drugDescriptionInput;

    public ValidateDrugInput(Context myContext, EditText myDrugName, EditText myDrugDescription) {
        context = myContext;
        drugName = myDrugName;
        drugDescription = myDrugDescription;
    }

    public boolean validateDrugName() {
        drugNameInput = drugName.getText().toString();

        if(drugNameInput.isEmpty()) {
            Toast.makeText(context, "Name cannot be empty.", Toast.LENGTH_SHORT).show();
            return false;
        } else {
            return true;
        }
    }

    public boolean validateDrugDescription() {
        drugDescriptionInput = drugDescription.getText().toString();

        if(drugDescriptionInput.isEmpty()) {
            Toast.makeText(context, "Description cannot be empty.", Toast.LENGTH_SHORT).show();
            return false;
        } else {
            return true;
        }
    }
}
