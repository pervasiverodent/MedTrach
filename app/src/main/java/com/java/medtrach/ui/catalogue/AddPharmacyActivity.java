package com.java.medtrach.ui.catalogue;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.java.medtrach.R;

public class AddPharmacyActivity extends AppCompatActivity {

    TextView pharmacyUploadImage;
    EditText pharmacyName, pharmacyDescription, pharmacyLocation;
    Button submitButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_pharmacy);


    }


}