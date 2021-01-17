package com.java.medtrach.ui.drug;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.java.medtrach.R;

public class DrugsViewHolder extends RecyclerView.ViewHolder {

    TextView drugName, drugDescription;

    public DrugsViewHolder(@NonNull View itemView) {
        super(itemView);

        drugName = itemView.findViewById(R.id.drug_name_text_view);
        drugDescription = itemView.findViewById(R.id.drug_description_text_view);
    }
}
