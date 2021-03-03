package com.java.medtrach;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.java.medtrach.common.Common;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.karumi.dexter.listener.single.DialogOnDeniedPermissionListener;
import com.karumi.dexter.listener.single.PermissionListener;

import java.util.List;

public class LandingPageActivity extends AppCompatActivity {
    final String TAG = LandingPageActivity.class.getSimpleName();
    private DatabaseReference roleRef;
    int LOCATION_REQUEST_CODE = 10001;
    private int roleType;
    private String userId, roleId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landing_page);
        roleRef = FirebaseDatabase.getInstance().getReference(Common.ROLE_REF);

        PermissionListener dialogPermissionListener =
                DialogOnDeniedPermissionListener.Builder
                        .withContext(this)
                        .withTitle("Camera permission")
                        .withMessage("Camera permission is needed to take pictures of your cat")
                        .withButtonText(android.R.string.ok)
                        .build();


        Dexter.withContext(this)
                .withPermissions(
                        Manifest.permission.LOCATION_HARDWARE,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.RECORD_AUDIO
                ).withListener(new MultiplePermissionsListener() {
            @Override public void onPermissionsChecked(MultiplePermissionsReport report) {
                if(report.areAllPermissionsGranted()) {
                    String userId = getIntent().getStringExtra("userId");

                    roleRef.child(userId).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if(snapshot.exists()) {
                                try {
                                    int roleType = snapshot.child("roleType").getValue(int.class);

                                    switch(roleType) {
                                        case Common.ROLE_ADMIN:
                                            Intent intentAdmin = new Intent(LandingPageActivity.this, HomeActivity.class);
                                            startActivity(intentAdmin);
                                            break;
                                        case Common.ROLE_USER:
                                            Intent intentUser = new Intent(LandingPageActivity.this, HomeActivity.class);
                                            startActivity(intentUser);
                                            break;
                                        default:
                                            Log.d(TAG, "onDataChange: Triggered default case.");
                                            break;
                                    }
                                } catch (NullPointerException e ) {
                                    Log.d(TAG, "onDataChange: " + e.getMessage().toString());
                                }
                            } else {

                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }

                if(report.isAnyPermissionPermanentlyDenied()) {
//                    Toast.makeText(LandingPageActivity.this, "All permissions need to be approved", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(LandingPageActivity.this, LoginActivity.class);
                    startActivity(intent);
                }
            }
            @Override public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                token.continuePermissionRequest();
            }
        }).check();
    }


}