package com.java.medtrach.ui.pharmacy;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.java.medtrach.R;

public class PharmacyViewHolder extends RecyclerView.ViewHolder {
    TextView pharmacyName, pharmacyLocation;

    public PharmacyViewHolder(@NonNull View itemView) {
        super(itemView);

        pharmacyName = itemView.findViewById(R.id.pharmacy_name_text_view);
        pharmacyLocation = itemView.findViewById(R.id.pharmacy_location_text_view);
    }
}
