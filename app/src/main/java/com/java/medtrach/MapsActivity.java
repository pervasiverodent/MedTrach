package com.java.medtrach;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.java.medtrach.common.Common;
import com.java.medtrach.directions.DirectionFinder;
import com.java.medtrach.directions.DirectionFinderListener;
import com.java.medtrach.directions.Route;

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
    private boolean isContinue = false;
    private boolean isGps;

    private enum PolylineStyle {
        DOTTED,
        PLAIN
    }

    private enum MapModeStyle {
        WALKING,
        DRIVING
    }

    private static final String[] POLYLINE_STYLE_OPTIONS = new String[]{
            "PLAIN",
            "DOTTED"
    };

    private static final String[] MAP_MODE_OPTIONS = new String[]{
            "WALKING",
            "DRIVING"
    };

    PolylineStyle polylineStyle = PolylineStyle.DOTTED;
    MapModeStyle mapModeStyle = MapModeStyle.WALKING;

    private DatabaseReference pharmacyReference;

    private LatLng pharmacyLatLng;
    private LatLng myLatLng;
    Marker destinationMarker;
    Marker userMarker, pharmacyMarker;
    Circle userLocationAccuracyCircle;

    private GoogleMap googleMap;
    private Polyline polyline;

    private TextView pharmacyNameTextView, pharmacyLocationTextView;
    private MaterialDialog materialDialog;

    private Location finalLocation, gpsLocation, networkLocation, passiveLocation, extraLocation;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationRequest locationRequest;

    private Double myLatitude, myLongitude;
    private Double pharmacyLatitude, pharmacyLongitude;
    private String pharmacyId, pharmacyName, pharmacyLocation;
    private String origin, destination;

    private int ACCESS_LOCATION_REQUEST_CODE = 10001;
    String mapModeOption;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        Places.initialize(getApplicationContext(), "AIzaSyBl5MEJvaKveEKEo_-Js_8PolRKXIm0-vM");
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        Intent intent = getIntent();
        pharmacyId = intent.getStringExtra("pharmacyId");
        pharmacyName = intent.getStringExtra("pharmacyName");
        pharmacyLocation = intent.getStringExtra("pharmacyLocation");
        pharmacyLongitude = intent.getDoubleExtra("pharmacyLongitude", 0.0);
        pharmacyLatitude = intent.getDoubleExtra("pharmacyLatitude", 0.0);


        assert mapFragment != null;
        mapFragment.getMapAsync(googleMap -> {
            defaultMapSettings(googleMap);
            this.googleMap = googleMap;
        });

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        locationRequest = LocationRequest.create();
        locationRequest.setInterval(500);
        locationRequest.setFastestInterval(500);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);


        pharmacyReference = FirebaseDatabase.getInstance().getReference(Common.PHARMACY_REF);

        AppCompatSpinner polylineStyleSpinner = findViewById(R.id.polylineStyleSpinner);
        AppCompatSpinner mapModeSpinner = findViewById(R.id.modeMapSpinner);
        pharmacyNameTextView = findViewById(R.id.maps_pharmacy_name_textView);
        pharmacyLocationTextView = findViewById(R.id.maps_pharmacy_address_textView);

        ArrayAdapter polyLineAdapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, POLYLINE_STYLE_OPTIONS);
        ArrayAdapter mapModeAdapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, MAP_MODE_OPTIONS);

        polyLineAdapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
        polylineStyleSpinner.setAdapter(polyLineAdapter);
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

        mapModeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
        mapModeSpinner.setAdapter(mapModeAdapter);
        mapModeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                if (position == 0) {
                    mapModeStyle = MapModeStyle.WALKING;
                    mapModeOption = "walking";
                    Log.d(TAG, "Map Mode Spinner: " + mapModeStyle);
                } else if (position == 1) {
                    mapModeStyle = MapModeStyle.DRIVING;
                    mapModeOption = "driving";
                    Log.d(TAG, "Map Mode Spinner: " + mapModeStyle);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Toast.makeText(MapsActivity.this, "Please select a mode from the option.", Toast.LENGTH_SHORT).show();
            }
        });


        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // Check permission
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_NETWORK_STATE) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    ACCESS_LOCATION_REQUEST_CODE);

            return;
        } else {
            // Already granted
        }


        /**
         * Attempt to call JSON to parse Google directions for pathing.
         */
        findViewById(R.id.find_route_maps_activity_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Concat PharmacyLatLng and MyLatLng to String
                origin = pharmacyLatitude + "," + pharmacyLongitude;
//                destination = myLatitude + "," + myLongitude;
                Log.d(TAG, "Map Mode: " + mapModeOption);
                fetchDirections(origin, destination, mapModeOption);
            }
        });

        // Retrieve user's location.
        try {
            Log.d(TAG, "Enabled Providers: " + locationManager.getProviders(true).toString());
            gpsLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            networkLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            passiveLocation = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
            extraLocation = locationManager.getLastKnownLocation(LocationManager.EXTRA_PROVIDER_NAME);
        } catch (Exception e) {
            e.printStackTrace();
        }


        pharmacyNameTextView.setText(pharmacyName);
        pharmacyLocationTextView.setText(pharmacyLocation);

        assert pharmacyId != null;
        pharmacyReference.child(pharmacyId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                pharmacyLatitude = snapshot.child("pharmacyLocationY").getValue(double.class);
//                pharmacyLongitude = snapshot.child("pharmacyLocationX").getValue(double.class);
//                pharmacyLatLng = new LatLng(pharmacyLatitude, pharmacyLongitude);
//                Log.d(TAG, "Pharmacy Location Y: " + pharmacyLatitude);
//                Log.d(TAG, "Pharmacy Location X: " + pharmacyLongitude);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, error.getMessage());
            }
        });

        ActivityCompat.requestPermissions(this, new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_NETWORK_STATE},
                1);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            getLastLocation();
//            enableUserLocation();
//            zoomToUserLocation();
        } else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                //We can show user a dialog why this permission is necessary
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, ACCESS_LOCATION_REQUEST_CODE);
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, ACCESS_LOCATION_REQUEST_CODE);
            }

        }

    }

    LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            super.onLocationResult(locationResult);
            Log.d(TAG, "onLocationResult: " + locationResult.getLastLocation());
            if (googleMap != null) {
                setUserLocationMarker(locationResult.getLastLocation());
            }
        }
    };

    private void setUserLocationMarker(Location location) {
        destination = location.getLatitude() + "," + location.getLongitude();

        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        LatLng pharmacyLatLng = new LatLng(pharmacyLatitude, pharmacyLongitude);
        Log.d(TAG, "setUserLocationMarker: Latitude: " + pharmacyLatitude.toString());
        Log.d(TAG, "setUserLocationMarker: Longitude: " + pharmacyLongitude.toString());

        if (pharmacyMarker == null) {
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(pharmacyLatLng);
            markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.pharmacy_marker));
            pharmacyMarker = googleMap.addMarker(markerOptions);

        }
        if (userMarker == null) {
            //Create a new marker
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(latLng);
            markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.car));
            markerOptions.rotation(location.getBearing());
            markerOptions.anchor((float) 0.5, (float) 0.5);
            userMarker = googleMap.addMarker(markerOptions);
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17));
        } else {
            //use the previously created marker
            userMarker.setPosition(latLng);
            userMarker.setRotation(location.getBearing());
            pharmacyMarker.setPosition(pharmacyLatLng);
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17));
        }

        if (userLocationAccuracyCircle == null) {
            CircleOptions circleOptions = new CircleOptions();
            circleOptions.center(latLng);
            circleOptions.strokeWidth(4);
            circleOptions.strokeColor(Color.argb(255, 255, 0, 0));
            circleOptions.fillColor(Color.argb(32, 255, 0, 0));
            circleOptions.radius(location.getAccuracy());
            userLocationAccuracyCircle = googleMap.addCircle(circleOptions);
        } else {
            userLocationAccuracyCircle.setCenter(latLng);
            userLocationAccuracyCircle.setRadius(location.getAccuracy());
        }
    }

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
    }

    private void stopLocationUpdates() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            startLocationUpdates();
        } else {
            // you need to request permissions...
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopLocationUpdates();
    }

    private void enableUserLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }

        googleMap.setMyLocationEnabled(true);
    }

    private void zoomToUserLocation() {
        Task<Location> locationTask = fusedLocationProviderClient.getLastLocation();
        locationTask.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 20));
                fetchDirections(origin, destination, mapModeOption);
//                mMap.addMarker(new MarkerOptions().position(latLng));
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == ACCESS_LOCATION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableUserLocation();
                zoomToUserLocation();
            } else {
                //We can show a dialog that permission is not granted...
            }
        }
    }


    private void fetchDirections(String origin, String destination, String mapModeOption) {
        try {
            new DirectionFinder(this, origin, destination, mapModeOption).execute();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }


    private void getLastLocation() {
        FusedLocationProviderClient mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mFusedLocationClient.getLastLocation()
            .addOnSuccessListener(new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (location != null) {
                        Log.d(TAG, "Fused Latitude: " + location.getLatitude());
                        Log.d(TAG, "Fused Longitude: " + location.getLongitude());
                        myLatitude = location.getLatitude();
                        myLongitude = location.getLongitude();
                        myLatLng = new LatLng(myLatitude, myLongitude);
                        googleMap.animateCamera(buildCameraUpdate(myLatLng), 15, null);
                    }
                }
            })
            .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d(TAG, "Error trying to get GPS location");
                    e.printStackTrace();
                }
            });
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
//                if (mapModeStyle == MapModeStyle.DRIVING)

                polyline = googleMap.addPolyline(polylineOptions);
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error occurred on finding the directions...", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
//        googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(pharmacyLatLng));
        googleMap.animateCamera(buildCameraUpdate(routes.get(0).endLocation), 15, null);
    }


}