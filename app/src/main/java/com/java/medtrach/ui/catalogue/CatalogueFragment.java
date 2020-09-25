package com.java.medtrach.ui.catalogue;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.java.medtrach.R;

public class CatalogueFragment extends Fragment {

    View root;

    Button addPharmacyButton, addDrugButton;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        root = inflater.inflate(R.layout.fragment_catalogue, container, false);

        addPharmacyButton = root.findViewById(R.id.add_pharmacy_button);
        addDrugButton = root.findViewById(R.id.add_drugs_button);

        addPharmacyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), AddPharmacyActivity.class);
                startActivity(intent);
            }
        });

        addDrugButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), AddDrugActivity.class);
                startActivity(intent);
            }
        });


        return root;
    }

}