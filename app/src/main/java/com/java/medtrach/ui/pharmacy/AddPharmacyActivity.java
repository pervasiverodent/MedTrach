package com.java.medtrach.ui.pharmacy;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.java.medtrach.R;
import com.java.medtrach.common.Common;
import com.java.medtrach.common.ValidatePharmacyInput;
import com.java.medtrach.model.PharmacyModel;

public class AddPharmacyActivity extends AppCompatActivity {

    private EditText pharmacyNameEditText, pharmacyLocationEditText;
    private Button submitButton;

    private PharmacyModel pharmacyModel;

    private String pharmacyName, pharmacyLocation;
    
    private ValidatePharmacyInput validatePharmacyInput;

    private FirebaseDatabase mDatabase;
    private DatabaseReference pharmacyReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_pharmacy);
        initializeContent();
        
        validatePharmacyInput = new ValidatePharmacyInput(AddPharmacyActivity.this,
                pharmacyNameEditText);

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                submitToFirebase();
            }
        });
    }

    private void initializeContent() {
        pharmacyNameEditText = findViewById(R.id.pharmacy_name_edit_text);
        pharmacyLocationEditText = findViewById(R.id.pharmacy_location_edit_text);
        submitButton = findViewById(R.id.pharmacy_submit_button);

        pharmacyReference = FirebaseDatabase.getInstance().getReference().child(Common.PHARMACY_REF);
    }

    private void submitToFirebase() {
        final String pharmacyId = pharmacyReference.push().getKey();

        pharmacyName = pharmacyNameEditText.getText().toString();
        pharmacyLocation = pharmacyLocationEditText.getText().toString();
        
        boolean verifiedPharmacyName = validatePharmacyInput.validatePharmacyName();

        if(verifiedPharmacyName) {
            pharmacyModel = new PharmacyModel();

            pharmacyModel.setPharmacyId(pharmacyId);
            pharmacyModel.setPharmacyName(pharmacyName);
            pharmacyModel.setPharmacyLocation(pharmacyLocation);

            pharmacyReference.child(pharmacyId).setValue(pharmacyModel).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Toast.makeText(AddPharmacyActivity.this, "Added entry.", Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(AddPharmacyActivity.this, "Failed to add entry.", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(this, "Test", Toast.LENGTH_SHORT).show();
        }

        
    }


}