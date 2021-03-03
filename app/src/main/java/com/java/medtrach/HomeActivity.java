package com.java.medtrach;

import android.Manifest;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.MenuItem;
import android.view.Menu;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.annotations.NotNull;
import com.java.medtrach.EventBus.MenuItemBack;
import com.java.medtrach.common.Common;
import com.java.medtrach.common.LoadingAnimation;
import com.java.medtrach.model.UserModel;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.DexterBuilder;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.karumi.dexter.listener.single.PermissionListener;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

public class HomeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private final String TAG = HomeActivity.class.getSimpleName();
    private AppBarConfiguration mAppBarConfiguration;

    private DrawerLayout drawer;
    private NavigationView navigationView;
    private NavController navController;

    private FirebaseAuth mAuth;
    String userId;
    private DatabaseReference roleRef;

    int LOCATION_REQUEST_CODE = 10001;

    FusedLocationProviderClient fusedLocationProviderClient;
    LocationRequest locationRequest;

    LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            if (locationResult == null) {
                return;
            }
            for (Location location : locationResult.getLocations()) {
                Log.d(TAG, "onLocationResult: " + location.toString());
            }
        }
    };

    int menuClickId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        LoadingAnimation loadingAnimation;

        drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        Menu navMenu = navigationView.getMenu();
        navigationView.setNavigationItemSelectedListener(this);
//        navMenu.findItem(R.id.nav_drugs).setVisible(false);
//        navMenu.findItem(R.id.nav_pharmacy).setVisible(false);

        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_voice,
                R.id.nav_pharmacy,
                R.id.nav_drugs)
                .setDrawerLayout(drawer)
                .build();

        mAuth = FirebaseAuth.getInstance();
        userId = mAuth.getUid();

        roleRef = FirebaseDatabase.getInstance().getReference(Common.ROLE_REF);

        Log.d(TAG, "onCreate: User ID: " + userId.toString());
        roleRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if(snapshot.exists()) {
                    try {
                        int roleType = snapshot.child("roleType").getValue(int.class);
                        Log.d(TAG, "Role Type: " + roleType);
                        switch(roleType) {
                            case Common.ROLE_USER:
                                Log.d(TAG, "onDataChange: Role Type: " + roleType);
                                navMenu.findItem(R.id.nav_drugs).setVisible(false);
                                navMenu.findItem(R.id.nav_pharmacy).setVisible(false);
                                break;
                            case Common.ROLE_ADMIN:
                                Log.d(TAG, "onDataChange: Role Type: " + roleType);
                                navMenu.findItem(R.id.nav_drugs).setVisible(true);
                                navMenu.findItem(R.id.nav_pharmacy).setVisible(true);
                                break;
                            default:
                                Log.d(TAG, "onDataChange: Triggered default case.");
                                break;
                        }
                    } catch (NullPointerException e ) {
                        Log.d(TAG, "onDataChange: " + e.getMessage().toString());
                    }
                } else {
                    Log.d(TAG, "onDataChange: snapshot does not exist.");
                    Log.d(TAG, "onDataChange: " + userId);
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });

        navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.bringToFront();

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        locationRequest = LocationRequest.create();
        locationRequest.setInterval(4000);
        locationRequest.setFastestInterval(2000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);


    ActivityCompat.requestPermissions(this, new String[]{
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_NETWORK_STATE,
        Manifest.permission.RECORD_AUDIO},
1);
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            getLastLocation();
            checkSettingsAndStartLocationUpdates();
        } else {
            askLocationPermission();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
        stopLocationUpdates();
    }

    private void checkSettingsAndStartLocationUpdates() {
        LocationSettingsRequest request = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest).build();
        SettingsClient client = LocationServices.getSettingsClient(this);

        Task<LocationSettingsResponse> locationSettingsResponseTask = client.checkLocationSettings(request);
        locationSettingsResponseTask.addOnSuccessListener(new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                //Settings of device are satisfied and we can start location updates
                startLocationUpdates();
            }
        });
        locationSettingsResponseTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (e instanceof ResolvableApiException) {
                    ResolvableApiException apiException = (ResolvableApiException) e;
                    try {
                        apiException.startResolutionForResult(HomeActivity.this, 1001);
                    } catch (IntentSender.SendIntentException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });
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

    private void getLastLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        Task<Location> locationTask = fusedLocationProviderClient.getLastLocation();
        locationTask.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    //We have a location
                    Log.d(TAG, "onSuccess: " + location.toString());
                    Log.d(TAG, "onSuccess: " + location.getLatitude());
                    Log.d(TAG, "onSuccess: " + location.getLongitude());
                } else  {
                    Log.d(TAG, "onSuccess: Location was null...");
                }
            }
        });
        locationTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(TAG, "onFailure: " + e.getLocalizedMessage() );
            }
        });
    }


    private void askLocationPermission() {
//        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
//                Log.d(TAG, "askLocationPermission: Should show an alert dialog...");
//                ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
////                startLocationUpdates();
//            } else {
//                ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
//            }
//        }
    }

//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        if (requestCode == LOCATION_REQUEST_CODE) {
//            if (grantResults.length >0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                // Permission granted
//                getLastLocation();
//                checkSettingsAndStartLocationUpdates();
//            } else {
//                //Permission not granted
//            }
//        }
//    }


    private void signOut() {
        UserModel userModel = null;

        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);

        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        menuItem.setChecked(true);
        drawer.closeDrawers();
        switch(menuItem.getItemId()) {
            case R.id.nav_voice:
                if(menuItem.getItemId() != menuClickId) {
                    Log.d(TAG, "onNavigationItemSelected: " + menuClickId);
                    navController.popBackStack();
                    navController.navigate(R.id.nav_voice);
                }
                break;
            case R.id.nav_pharmacy:
                if(menuItem.getItemId() != menuClickId) {
                    Log.d(TAG, "onNavigationItemSelected: " + menuClickId);
                    navController.popBackStack();
                    navController.navigate(R.id.nav_pharmacy);
                }
                break;
            case R.id.nav_drugs:
                if(menuItem.getItemId() != menuClickId) {
                    Log.d(TAG, "onNavigationItemSelected: " + menuClickId);
                    navController.popBackStack();
                    navController.navigate(R.id.nav_drugs);
                }
                break;
            case R.id.nav_logout:
                signOut();
                break;
            default:
                menuClickId = -1;
                break;
        }
        menuClickId = menuItem.getItemId();
        return true;
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onMenuItemBack(MenuItemBack event) {
        menuClickId = -1;
        if(getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();
        }
    }


}