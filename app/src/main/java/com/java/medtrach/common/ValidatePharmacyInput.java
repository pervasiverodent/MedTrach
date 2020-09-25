package com.java.medtrach.common;

import android.content.Context;
import android.widget.EditText;
import android.widget.Toast;

public class ValidatePharmacyInput {
    Context context;
    EditText pharmacyName, pharmacyDescription, pharmacyLocation;

    String pharmacyNameInput, pharmacyDescriptionInput, pharmacyLocationInput;

    public ValidatePharmacyInput(Context myContext, EditText myPharmacyName, EditText myPharmacyDescription, EditText myPharmacyLocation) {
        context = myContext;
        pharmacyName = myPharmacyName;
        pharmacyDescription = myPharmacyDescription;
        pharmacyLocation = myPharmacyLocation;
    }

    public boolean validatePharmacyName() {
        pharmacyNameInput = pharmacyName.getText().toString();

        if(pharmacyNameInput.isEmpty()) {
            Toast.makeText(context, "Pharmacy's name cannot be empty.", Toast.LENGTH_SHORT).show();
            return  false;
        } else {
            return true;
        }
    }

    public boolean validatePharmacyDescription() {
        pharmacyDescriptionInput = pharmacyDescription.getText().toString();

        if(pharmacyDescriptionInput.isEmpty()) {
            Toast.makeText(context, "Description cannot be empty.", Toast.LENGTH_SHORT).show();
            return false;
        } else {
            return true;
        }
    }
}
