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
import com.java.medtrach.R;
import com.java.medtrach.common.Common;
import com.java.medtrach.model.DrugListModel;
import com.java.medtrach.model.DrugModel;
import com.java.medtrach.model.PharmacyModel;
import com.java.medtrach.ui.pharmacy.PharmacyViewHolder;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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
                microphoneImageView.setImageResource(R.drawable.ic_baseline_mic_24);
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
                Toast.makeText(getContext(), "TESTING MIC!!!", Toast.LENGTH_SHORT).show();

                if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    speechRecognizer.stopListening();
                }
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN){
                    microphoneImageView.setImageResource(R.drawable.ic_baseline_map_24);
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

//        drugReference.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                final String drugPharmacyId = snapshot.child("pharmacyId").getValue(String.class);
//                final String drugPharmacyName = snapshot.child("drugPharmacyName").getValue(String.class);
//                final String drugPharmacyLocation = snapshot.child("drugPharmacyLocation").getValue(String.class);
//
//                final String drugId = snapshot.child("drugId").getValue(String.class);
//                final String drugName = snapshot.child("drugName").getValue(String.class);
//                final String drugDescription = snapshot.child("drugDescription").getValue(String.class);
//
//
//                Log.d(TAG, "ID: " + drugPharmacyId);
//                Log.d(TAG, "Name: " + drugPharmacyName);
//                Log.d(TAG, "Location: " + drugPharmacyLocation);
//
//                Log.d(TAG, drugId);
//                Log.d(TAG, drugName);
//                Log.d(TAG, drugDescription);
//            }
//
//            @Override
//            public void onCancelled(@NonNull  DatabaseError error) {
//                Log.d(TAG, "E: " + error.getMessage());
//            }
//        });

        return root;
    }

    private void loadData(String data) {
        Query query = drugReference.orderByChild("drugName").startAt(data).endAt(data + "\uf8ff");

        options = new FirebaseRecyclerOptions.Builder<DrugModel>()
                .setQuery(query, DrugModel.class)
                .build();

        adapter = new FirebaseRecyclerAdapter<DrugModel, DrugsViewHolder>(options) {
            @NonNull
            @Override
            public DrugsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_view_catalogue_drugs, parent, false);
                return new DrugsViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull DrugsViewHolder holder, int position, @NonNull DrugModel model) {
                final String myDrugName, myDrugDescription, myDrugPharmacyName, myDrugPharmacyLocation;

                myDrugName = model.getDrugName();
                myDrugDescription = model.getDrugDescription();
                myDrugPharmacyName = model.getDrugPharmacyName();
                myDrugPharmacyLocation = model.getDrugPharmacyLocation();

                holder.drugName.setText(myDrugName);
                holder.drugDescription.setText(myDrugDescription);
                holder.pharmacyName.setText(myDrugPharmacyName);
                holder.pharmacyLocation.setText(myDrugPharmacyLocation);
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



}
