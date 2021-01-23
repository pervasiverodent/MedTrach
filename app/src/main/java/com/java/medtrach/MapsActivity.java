package com.java.medtrach;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.java.medtrach.common.Common;
import com.java.medtrach.directions.DirectionFinder;
import com.java.medtrach.directions.DirectionFinderListener;
import com.java.medtrach.directions.Route;
import com.java.medtrach.util.GoogleMapHelper;

import java.io.UnsupportedEncodingException;
import java.util.List;

import static com.java.medtrach.util.GoogleMapHelper.buildCameraUpdate;
import static com.java.medtrach.util.GoogleMapHelper.defaultMapSettings;
import static com.java.medtrach.util.GoogleMapHelper.getDefaultPolyLines;
import static com.java.medtrach.util.GoogleMapHelper.getDottedPolylines;
import static com.java.medtrach.util.UiHelper.showAlwaysCircularProgressDialog;

public class MapsActivity extends AppCompatActivity implements
        OnMapReadyCallback,
        DirectionFinderListener {

    private static final String TAG = MapsActivity.class.getSimpleName();

    private enum PolylineStyle {
        DOTTED,
        PLAIN
    }

    private static final String[] POLYLINE_STYLE_OPTIONS = new String[]{
            "PLAIN",
            "DOTTED"
    };

    PolylineStyle polylineStyle = PolylineStyle.DOTTED; //RUNTIME ERROR

    private LatLng pharmacyLatLng;
    private LatLng myLatLng;
    Marker pharmacyMarker;
    Marker myLatLngMarker;

    private GoogleMap googleMap;
    private Polyline polyline;
    private TextView XCoordinateTextView, YCoordinateTextView;
    private MaterialDialog materialDialog;

    private Location finalLocation, gpsLocation, networkLocation;
    private DatabaseReference pharmacyReference;

    private Double myLatitude, myLongitude;
    private Double pharmacyLatitude, pharmacyLongitude;
    private String pharmacyId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        assert mapFragment != null;
        mapFragment.getMapAsync(googleMap -> {
            defaultMapSettings(googleMap);
            this.googleMap = googleMap;
        });

        XCoordinateTextView = findViewById(R.id.x_coordinate_activity_maps_textView);
        YCoordinateTextView = findViewById(R.id.y_coordinate_activity_maps_textView);
        pharmacyReference = FirebaseDatabase.getInstance().getReference(Common.PHARMACY_REF);

        AppCompatSpinner polylineStyleSpinner = findViewById(R.id.polylineStyleSpinner);
        ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, POLYLINE_STYLE_OPTIONS);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
        polylineStyleSpinner.setAdapter(adapter);
        polylineStyleSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0)
                    polylineStyle = PolylineStyle.PLAIN;
                else if (position == 1)
                    polylineStyle = PolylineStyle.DOTTED;
                if (polyline == null || !polyline.isVisible())
                    return;
                List<LatLng> points = polyline.getPoints();
                polyline.remove();
                if (position == 0)
                    polyline = googleMap.addPolyline(getDefaultPolyLines(points));
                else if (position == 1)
                    polyline = googleMap.addPolyline(getDottedPolylines(points));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });



        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // Check permission
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_NETWORK_STATE) != PackageManager.PERMISSION_GRANTED) {

            return;
        }

        /**
         * Attempt to call JSON to parse Google directions for pathing.
         */
        findViewById(R.id.find_route_maps_activity_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Concat PharmacyLatLng and MyLatLng to String
                String origin = pharmacyLatitude + "," + pharmacyLongitude;
                String destination = myLatitude + "," + myLongitude;
                fetchDirections(origin, destination);
            }
        });

        // Retrieve user's location.
        try {
            gpsLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            networkLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (gpsLocation != null) {
            finalLocation = gpsLocation;
            myLatitude = finalLocation.getLatitude();
            myLongitude = finalLocation.getLongitude();
            Log.d(TAG, "GPS used");

        } else if (networkLocation != null) {
            finalLocation = networkLocation;
            myLatitude = finalLocation.getLatitude();
            myLongitude = finalLocation.getLongitude();
            Log.d(TAG, "Network used");

        } else {
            myLatitude  = 0.0;
            myLongitude = 0.0;
        }

        XCoordinateTextView.setText("" + myLatitude);
        YCoordinateTextView.setText("" + myLongitude);


        Intent intent = getIntent();

        pharmacyId = intent.getStringExtra("pharmacyId");
        assert pharmacyId != null;
        pharmacyReference.child(pharmacyId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                pharmacyLatitude = snapshot.child("pharmacyLocationY").getValue(double.class);
                pharmacyLongitude = snapshot.child("pharmacyLocationX").getValue(double.class);
                pharmacyLatLng = new LatLng(pharmacyLatitude, pharmacyLongitude);
                Log.d(TAG, "Pharmacy Location Y: " + pharmacyLatitude);
                Log.d(TAG, "Pharmacy Location X: " + pharmacyLongitude);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, error.getMessage());
            }
        });

        Log.d(TAG, "Pharmacy Location Y: " + pharmacyLatitude);
        Log.d(TAG, "Pharmacy Location X: " + pharmacyLongitude);


        Log.d(TAG, "My Latitude: " + myLatitude);
        Log.d(TAG, "My Longitude: " + myLongitude);
        myLatLng = new LatLng(myLatitude, myLongitude);

        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_NETWORK_STATE}, 1);


    }

    private void fetchDirections(String origin, String destination) {
        try {
            new DirectionFinder(this, origin, destination).execute();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDirectionFinderStart() {
        if (materialDialog == null)
            materialDialog = showAlwaysCircularProgressDialog(this, "Fetching Directions...");
        else materialDialog.show();
    }

    @Override
    public void onDirectionFinderSuccess(List<Route> routes) {
        if (materialDialog != null && materialDialog.isShowing())
            materialDialog.dismiss();
        if (!routes.isEmpty() && polyline != null) polyline.remove();
        try {
            for (Route route : routes) {
                PolylineOptions polylineOptions = getDefaultPolyLines(route.points);
                if (polylineStyle == PolylineStyle.DOTTED)
                    polylineOptions = getDottedPolylines(route.points);
                polyline = googleMap.addPolyline(polylineOptions);
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error occurred on finding the directions...", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
        googleMap.animateCamera(buildCameraUpdate(routes.get(0).endLocation), 10, null);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        myLatLngMarker = googleMap.addMarker(new MarkerOptions()
                .position(myLatLng)
                .title("You")
                .snippet("Your location")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));

        pharmacyMarker = googleMap.addMarker(new MarkerOptions()
                .position(pharmacyLatLng)
                .title("Pharmacy")
                .snippet("Pharmacy's location")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));

        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLatLng, 14.0f));
    }

}