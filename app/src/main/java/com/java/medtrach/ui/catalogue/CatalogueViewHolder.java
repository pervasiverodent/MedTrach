package com.java.medtrach.ui.catalogue;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.java.medtrach.R;

public class CatalogueViewHolder extends RecyclerView.ViewHolder {

    TextView drugName, pharmacyName, pharmacyLocation;

    public CatalogueViewHolder(@NonNull View itemView) {
        super(itemView);

        drugName = itemView.findViewById(R.id.drug_name);
        pharmacyName = itemView.findViewById(R.id.drug_pharmacy_name);
        pharmacyLocation = itemView.findViewById(R.id.drug_pharmacy_location);
    }
}
