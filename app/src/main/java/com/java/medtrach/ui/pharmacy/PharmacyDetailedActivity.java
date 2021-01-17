package com.java.medtrach.ui.pharmacy;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.java.medtrach.R;
import com.java.medtrach.common.Common;
import com.java.medtrach.ui.catalogue.AddDrugActivity;

//TODO Create RecyclerView list adapter.
public class PharmacyDetailedActivity extends AppCompatActivity {
    final String TAG = "PharmacyDetailedActivity";

    TextView pharmacyDetailedName, pharmacyDetailedLocation;
    Button addDrugsButton;
    private String fromPharmacyId;

    FirebaseDatabase mDatabase;
    DatabaseReference pharmacyReference, drugsReference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pharmacy);

        pharmacyDetailedName = findViewById(R.id.pharmacy_detailed_name_text_view);
        pharmacyDetailedLocation = findViewById(R.id.pharmacy_detailed_location_text_view);
        addDrugsButton = findViewById(R.id.add_drugs_button);

        mDatabase = FirebaseDatabase.getInstance();
        pharmacyReference = mDatabase.getReference(Common.PHARMACY_REF);
        drugsReference = mDatabase.getReference(Common.DRUG_REF);

        // Retrieve from intent
        Intent intent = getIntent();
        fromPharmacyId = intent.getStringExtra("pharmacyId");

        // Retrieve pharmacyId as look up field for Database.
        pharmacyReference.child(fromPharmacyId).addListenerForSingleValueEvent(new ValueEventListener() {
            final String TAG = "testing";

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                final String pharmacyId = snapshot.child("pharmacyId").getValue().toString();
                final String pharmacyName = snapshot.child("pharmacyName").getValue().toString();
                final String pharmacyLocation = snapshot.child("pharmacyLocation").getValue().toString();

                Log.d(TAG, "From Firebase Database");
                Log.d(TAG, "ID: " + pharmacyId);
                Log.d(TAG, "Name: " + pharmacyName);
                Log.d(TAG, "Location: " + pharmacyLocation);

                pharmacyDetailedName.setText(pharmacyName);
                pharmacyDetailedLocation.setText(pharmacyLocation);

                addDrugsButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(getApplicationContext(), AddDrugActivity.class);
                        intent.putExtra("pharmacyId", pharmacyId);
                        intent.putExtra("pharmacyName", pharmacyName);
                        intent.putExtra("pharmacyLocation", pharmacyLocation);

                        startActivity(intent);
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(PharmacyDetailedActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });




    }
}