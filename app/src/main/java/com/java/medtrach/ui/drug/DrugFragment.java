package com.java.medtrach.ui.drug;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.annotations.NotNull;
import com.java.medtrach.MapsActivity;
import com.java.medtrach.R;
import com.java.medtrach.common.Common;
import com.java.medtrach.model.DrugListModel;
import com.java.medtrach.model.DrugModel;
import com.java.medtrach.model.PharmacyModel;
import com.java.medtrach.ui.pharmacy.PharmacyDetailedActivity;
import com.java.medtrach.ui.pharmacy.PharmacyViewHolder;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * 1. Search kelangan pa kasi i-tap para magpakita yung list.
 * 2. On start, show logo, on speak show desired list.
 * 3. On tap Firebase Recycler Adapter, imediately show route rather than on button click.
 * 4. On map show options for walking, driving, motorcycle.
 * 5. waze pathing
 * 6. remove debug coords.
 */

public class DrugFragment extends Fragment {
    private static final String TAG = DrugFragment.class.getName();
    public static final Integer RecordAudioRequestCode = 1;

    //RecyclerView
    private PharmacyModel pharmacyModel;
    private List<DrugModel> drugModelList;

    private RecyclerView recyclerView;

    //Firebase
    DatabaseReference pharmacyReference;
    DatabaseReference drugReference;

    //Insert Adapter
    FirebaseRecyclerAdapter<DrugModel, DrugsViewHolder> adapter;
    FirebaseRecyclerOptions<DrugModel> options;

    private SpeechRecognizer speechRecognizer;

    private TextView pharmacyName, pharmacyLocation;
    private EditText searchBarEditText;
    private ImageView microphoneImageView;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        //Initialize Views and IDs
        View root = inflater.inflate(R.layout.fragment_drug, container, false);
//        pharmacyName = root.findViewById(R.id.fragment_drug_pharmacy_name_textView);
//        pharmacyLocation = root.findViewById(R.id.fragment_drug_pharmacy_location_textView);
        searchBarEditText = root.findViewById(R.id.drug_search_bar_edit_text);
        microphoneImageView = root.findViewById(R.id.drug_microphone_image_view);

        pharmacyModel = new PharmacyModel();

        //Initialize adapter
        recyclerView = (RecyclerView) root.findViewById(R.id.drug_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setHasFixedSize(true);

        //Initialize Firebase
        pharmacyReference = FirebaseDatabase.getInstance().getReference(Common.PHARMACY_REF);
        drugReference = FirebaseDatabase.getInstance().getReference(Common.DRUG_REF);

        if(ContextCompat.checkSelfPermission(getContext(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED){
            checkPermission();
        }

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(getContext());
        final Intent speechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());

        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle bundle) {

            }

            @Override
            public void onBeginningOfSpeech() {
                searchBarEditText.setText("Listening...");
            }

            @Override
            public void onRmsChanged(float v) {

            }

            @Override
            public void onBufferReceived(byte[] bytes) {

            }

            @Override
            public void onEndOfSpeech() {

            }

            @Override
            public void onError(int i) {

            }

            @Override
            public void onResults(Bundle bundle) {
                microphoneImageView.setImageResource(R.drawable.ic_baseline_mic_off_24);
                ArrayList<String> data = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                searchBarEditText.setText(StringUtils.capitalize(data.get(0)));
            }

            @Override
            public void onPartialResults(Bundle bundle) {

            }

            @Override
            public void onEvent(int i, Bundle bundle) {

            }
        });

        microphoneImageView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    speechRecognizer.stopListening();
                }
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN){
                    microphoneImageView.setImageResource(R.drawable.ic_baseline_mic_24);
                    speechRecognizer.startListening(speechRecognizerIntent);
                }
                return false;
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
        Query query = drugReference.orderByChild("drugName").startAt(data).endAt(data + "\uf8ff");

        options = new FirebaseRecyclerOptions.Builder<DrugModel>()
                .setQuery(query, DrugModel.class)
                .build();

        adapter = new FirebaseRecyclerAdapter<DrugModel, DrugsViewHolder>(options) {

            @Override
            protected void onBindViewHolder(@NonNull DrugsViewHolder holder, int position, @NonNull DrugModel model) {

                final String myDrugId = model.getDrugId().toString();

                holder.drugName.setText(model.getDrugName());
                holder.drugDescription.setText(model.getDrugDescription());
                holder.pharmacyName.setText(model.getDrugPharmacyName());
                holder.pharmacyLocation.setText(model.getDrugPharmacyLocation());

                drugReference.child(myDrugId).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {

                        Log.d(TAG, "onDataChange: Drug ID " +  myDrugId);
                        final String pharmacyName = snapshot.child("drugPharmacyName").getValue().toString();
                        final String pharmacyLocation = snapshot.child("drugPharmacyLocation").getValue().toString();
                        final Double pharmacyLongitude = (Double) snapshot.child("pharmacyLocationX").getValue();
                        final Double pharmacyLatitude = (Double) snapshot.child("pharmacyLocationY").getValue();

                        holder.itemView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent intent = new Intent(getActivity(), MapsActivity.class);
                                try {
                                    intent.putExtra("pharmacyId", model.getDrugPharmacyId());
                                    intent.putExtra("pharmacyName", pharmacyName);
                                    intent.putExtra("pharmacyLocation", pharmacyLocation);
                                    intent.putExtra("pharmacyLongitude", pharmacyLongitude);
                                    intent.putExtra("pharmacyLatitude", pharmacyLatitude);
                                    startActivity(intent);
                                } catch (NullPointerException e) {
                                    Toast.makeText(getContext(), "E: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {

                    }
                });


            }

            @NonNull
            @Override
            public DrugsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_view_catalogue_drugs, parent, false);
                return new DrugsViewHolder(view);
            }


        };
        adapter.startListening();
        recyclerView.setAdapter(adapter);
//        adapter.notifyDataSetChanged();
    }

    private void checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ActivityCompat.requestPermissions(getActivity(),new String[]{Manifest.permission.RECORD_AUDIO},RecordAudioRequestCode);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == RecordAudioRequestCode && grantResults.length > 0 ){
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED)
                Toast.makeText(getContext(),"Permission Granted", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onStart() {
        adapter.startListening();
        adapter.notifyDataSetChanged();
        super.onStart();
    }

    @Override
    public void onStop() {
        adapter.stopListening();
        super.onStop();
    }

    @Override
    public void onResume() {
        adapter.notifyDataSetChanged();
        super.onResume();
    }
}
