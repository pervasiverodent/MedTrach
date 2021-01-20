package com.java.medtrach.ui.pharmacy;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.java.medtrach.R;
import com.java.medtrach.common.Common;
import com.java.medtrach.model.DrugModel;
import com.java.medtrach.model.PharmacyModel;

import static java.security.AccessController.getContext;


/**
 * TODO
 * 1. Set permissions
 * 2. Get current location
 * 3. Set fragment
 * 4. Add coordinates
 * 5. Pass coordinates to previous intent
 * 6. Pass intents
 * 7. Fix bugs
 */

public class MapsPharmacyActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    final String TAG = MapsPharmacyActivity.class.getName();

    private LocationRequest locationRequest;
    private Location myLocation = null;
    private Marker marker;

    private TextView pharmacyNameTextView, pharmacyLocationTextView;
    private TextView pharmacyXLongitude, pharmacyYLatitude;
    private Button submitActivityMapsButton;

    private FirebaseDatabase mDatabase;
    private DatabaseReference drugReference, pharmacyReference;

    private GoogleMap googleMap;

    String pharmacyName, pharmacyLocation;
    Double pharmacyLongitude, pharmacyLatitude;

    boolean locationPermission = false;

    DrugModel drugModel = new DrugModel();
    PharmacyModel pharmacyModel = new PharmacyModel();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps_pharmacy);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.pharmacy_map_fragment);
        mapFragment.getMapAsync(MapsPharmacyActivity.this::onMapReady);
        buildLocationRequest();

        pharmacyNameTextView = findViewById(R.id.card_view_pharmacy_name_textView);
        pharmacyLocationTextView = findViewById(R.id.card_view_parmacy_location_textView);
        pharmacyXLongitude = findViewById(R.id.longitude_x_text_view);
        pharmacyYLatitude = findViewById(R.id.latitude_y_text_view);
        submitActivityMapsButton = findViewById(R.id.submit_activity_maps_button);

        mDatabase = FirebaseDatabase.getInstance();
        drugReference = mDatabase.getReference().child(Common.DRUG_REF);
        pharmacyReference = mDatabase.getReference().child(Common.PHARMACY_REF);

        Intent intent = getIntent();
        pharmacyName = intent.getStringExtra("pharmacyName");
        pharmacyLocation = intent.getStringExtra("pharmacyLocation");

        pharmacyNameTextView.setText(pharmacyName);
        pharmacyLocationTextView.setText(pharmacyLocation);

        submitActivityMapsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(TextUtils.isEmpty(pharmacyYLatitude.toString()) && TextUtils.isEmpty(pharmacyXLongitude.toString())) {
                    Toast.makeText(MapsPharmacyActivity.this, "Tap screen to input coordinates.", Toast.LENGTH_SHORT).show();
                } else {
                    String pharmacyId = pharmacyReference.push().getKey();

                    pharmacyModel.setPharmacyId(pharmacyId);
                    pharmacyModel.setPharmacyName(pharmacyName);
                    pharmacyModel.setPharmacyLocation(pharmacyLocation);
                    pharmacyModel.setPharmacyLocationX(pharmacyLongitude);
                    pharmacyModel.setPharmacyLocationY(pharmacyLatitude);

                    pharmacyReference.child(pharmacyId).setValue(pharmacyModel)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                Toast.makeText(MapsPharmacyActivity.this, "Registered to Database.", Toast.LENGTH_SHORT).show();
                                submitActivityMapsButton.setText("Submitted");
                                submitActivityMapsButton.setEnabled(false);
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(MapsPharmacyActivity.this, "Failed to register to Database.", Toast.LENGTH_SHORT).show();
                                Log.d("onFailure", "E: " + e.getMessage());
                            }
                        });

                }

            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        requestPermissions();

        googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                Log.d("onMapClick", "Coordinates" + latLng.latitude + " | " + latLng.longitude);
                pharmacyXLongitude.setText(String.format("X:%s", latLng.longitude));
                pharmacyYLatitude.setText(String.format("Y: %s", latLng.latitude));

                pharmacyLatitude = latLng.latitude;
                pharmacyLongitude = latLng.longitude;

                addMarker(latLng);
            }
        });
    }

    private void addMarker(LatLng latLng) {
        latLng = new LatLng(latLng.longitude, latLng.latitude);

        marker = googleMap.addMarker(new MarkerOptions()
                .position(latLng)
                .title(pharmacyName)
                .snippet("Pharmacy's location.")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));

        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 5f));
    }

    private void buildLocationRequest() {
        locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(1000); //15 seconds
        locationRequest.setFastestInterval(10000); //10 seconds
        locationRequest.setSmallestDisplacement(20f); // 20 meters
    }

    private void requestPermissions() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    Common.LOCATION_REQUEST_CODE);
        } else {
            locationPermission = true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case Common.LOCATION_REQUEST_CODE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    locationPermission = true;
                    getMyLocation();
                } else {
                    Toast.makeText(this, "Permission Denied.", Toast.LENGTH_SHORT).show();
                }
                return;
            }
        }
    }

    private void getMyLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        googleMap.setMyLocationEnabled(true);
        googleMap.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {
            @Override
            public void onMyLocationChange(Location location) {
                myLocation = location;
                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(
                        latLng, 16f);

                googleMap.animateCamera(cameraUpdate);
            }
        });

    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        return false;
    }
}