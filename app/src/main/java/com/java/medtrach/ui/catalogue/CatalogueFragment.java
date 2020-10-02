package com.java.medtrach.ui.catalogue;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.java.medtrach.R;
import com.java.medtrach.common.Common;
import com.java.medtrach.model.DrugModel;

public class CatalogueFragment extends Fragment {

    View root;

    private RecyclerView recyclerView;
    private FirebaseRecyclerAdapter<DrugModel, CatalogueViewHolder> adapter;

    private FirebaseRecyclerOptions<DrugModel> options;
    private DatabaseReference catalogueReference;

    private View view;

    private Button addPharmacyButton, addDrugButton;
    private EditText searchBarEditText;
    private ImageView microphoneButton;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        root = inflater.inflate(R.layout.fragment_catalogue, container, false);

        addPharmacyButton = root.findViewById(R.id.add_pharmacy_button);
        addDrugButton = root.findViewById(R.id.add_drugs_button);
        searchBarEditText = root.findViewById(R.id.catalogue_search_bar);
        microphoneButton = root.findViewById(R.id.catalogue_microphone_image);

        recyclerView = root.findViewById(R.id.catalogue_recycler_view);
        recyclerView.setHasFixedSize(true);

        catalogueReference = FirebaseDatabase.getInstance().getReference().child(Common.CATALOGUE_REF);

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

        loadData("");

        searchBarEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(editable.toString() != null) {
                    loadData(editable.toString());
                } else {
                    loadData("");
                }
            }
        });

        return root;
    }

    private void loadData(String data) {
        Query query = catalogueReference.orderByChild("drugName").startAt(data).endAt(data + "\uf8ff");

        options = new FirebaseRecyclerOptions.Builder<DrugModel>()
                .setQuery(query, DrugModel.class)
                .build();

        adapter = new FirebaseRecyclerAdapter<DrugModel, CatalogueViewHolder>(options) {

            @NonNull
            @Override
            public CatalogueViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                return null;
            }

            @Override
            protected void onBindViewHolder(@NonNull CatalogueViewHolder holder, int position, @NonNull DrugModel model) {
                // To do, open maps activity.
            }

        };

        recyclerView.setAdapter(adapter);
        adapter.startListening();
    }

}