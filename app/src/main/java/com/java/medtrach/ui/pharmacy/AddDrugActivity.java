package com.java.medtrach.ui.pharmacy;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.java.medtrach.R;
import com.java.medtrach.common.Common;
import com.java.medtrach.common.ValidateDrugInput;
import com.java.medtrach.model.DrugModel;

public class AddDrugActivity extends AppCompatActivity {

    private DatabaseReference drugReference, pharmacyReference, catalogueReference;
    private FirebaseDatabase mDatabase;

    final String TAG = AddDrugActivity.class.getName();
    String pharmacyId;
    String pharmacyName;
    String pharmacyLocation;
    Double pharmacyLatitude;
    Double pharmacyLongitude;

    ValidateDrugInput validateDrugInput;
    DrugModel drugModel, drugListModel;

    TextView pharmacyNameTextView, pharmacyLocationTextView;
    EditText drugNameEditText, drugDescriptionEditText;
    Button submitButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_drug);
        initializeContent();

        Intent intent = getIntent();
        pharmacyId = intent.getStringExtra("pharmacyId");
        pharmacyName = intent.getStringExtra("pharmacyName");
        pharmacyLocation = intent.getStringExtra("pharmacyLocation");
        pharmacyLatitude = intent.getDoubleExtra("pharmacyLatitude", 0);
        pharmacyLongitude = intent.getDoubleExtra("pharmacyLongitude", 0);

        Log.d(TAG, "Pharmacy ID: " + pharmacyId);
        Log.d(TAG, "Pharmacy Name: " + pharmacyName);
        Log.d(TAG, "Pharmacy Location: " + pharmacyLocation);
        Log.d(TAG, "Pharmacy Latitude: " + pharmacyLatitude);
        Log.d(TAG, "PharmacyLongitude: " + pharmacyLongitude);

        pharmacyNameTextView.setText(pharmacyName);
        pharmacyLocationTextView.setText(pharmacyLocation);

        validateDrugInput = new ValidateDrugInput(
                AddDrugActivity.this, drugNameEditText, drugDescriptionEditText
        );

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                submitToFirebase();
            }
        });
    }

    private void initializeContent() {
        pharmacyNameTextView = findViewById(R.id.add_drugs_pharmacy_name_textView);
        pharmacyLocationTextView = findViewById(R.id.add_drugs_pharmacy_location_textView);

        drugNameEditText = findViewById(R.id.add_drug_drug_name_edit_text);
        drugDescriptionEditText = findViewById(R.id.add_drug_description_edit_text);
        submitButton = findViewById(R.id.add_drug_submit_button);

        drugReference = FirebaseDatabase.getInstance().getReference().child(Common.DRUG_REF);
        pharmacyReference = FirebaseDatabase.getInstance().getReference().child(Common.PHARMACY_REF);
    }

    private void submitToFirebase() {
        final String drugId = drugReference.push().getKey();

        String drugName = drugNameEditText.getText().toString().trim();
        String drugDescription = drugDescriptionEditText.getText().toString().trim();

        boolean drugNameVerified = validateDrugInput.validateDrugName();
        
        if(drugNameVerified) {
            drugModel = new DrugModel();
            drugListModel = new DrugModel();

            drugModel.setDrugId(drugId);
            drugModel.setDrugName(drugName);
            drugModel.setDrugDescription(drugDescription);

            drugListModel.setDrugId(drugId);
            drugListModel.setDrugName(drugName);
            drugListModel.setDrugDescription(drugDescription);
            drugListModel.setDrugPharmacyId(pharmacyId);
            drugListModel.setDrugPharmacyName(pharmacyName);
            drugListModel.setDrugPharmacyLocation(pharmacyLocation);
            drugListModel.setDrugPharmacyLatitude(pharmacyLatitude);
            drugListModel.setDrugPharmacyLongitude(pharmacyLongitude);

            pharmacyReference.child(pharmacyId).child(Common.DRUG_REF).child(drugId).setValue(drugModel)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(AddDrugActivity.this, "Added entry.", Toast.LENGTH_SHORT).show();
                        /**
                         * To create a separate copy of the Drug list database for indexing DRUGLIST model
                         * on a separate activity.
                         */
                        drugReference.child(drugId).setValue(drugListModel)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    Log.d(TAG + "submitToFirebase", "Success");
                                    Toast.makeText(AddDrugActivity.this, "Created a copy to Drug list.", Toast.LENGTH_SHORT).show();
                                    submitButton.setText("Submitted");
                                    submitButton.setEnabled(false);
//                                    submitButton.setBackgroundColor(COLOR.GREY);
                                }
                        })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.d("submitToFirebase", e.getMessage());
                                }
                            });
                    }
                }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(AddDrugActivity.this, "Failed to add entry.", Toast.LENGTH_SHORT).show();
                }
            });


        } else {
            Toast.makeText(this, "Fields cannot be empty.", Toast.LENGTH_SHORT).show();
        }

        
    }


}