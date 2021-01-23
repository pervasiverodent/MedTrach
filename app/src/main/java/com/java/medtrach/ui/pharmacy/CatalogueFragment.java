package com.java.medtrach.ui.pharmacy;

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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.java.medtrach.R;
import com.java.medtrach.common.Common;
import com.java.medtrach.model.DrugModel;
import com.java.medtrach.model.PharmacyModel;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Locale;


public class CatalogueFragment extends Fragment {
    final String TAG = "CatalogueFragment";
    private EditText searchBarEditText;
    private ImageView microphoneButton;

    private SpeechRecognizer speechRecognizer;
    public static final Integer RecordAudioRequestCode = 1;

    private RecyclerView.LayoutManager layoutManager;
    private View view;
    private RecyclerView recyclerView;

    FirebaseRecyclerAdapter<PharmacyModel, PharmacyViewHolder> adapter;
    FirebaseRecyclerOptions<PharmacyModel> options;
    private DatabaseReference drugReference, pharmacyReference;

    private DrugModel drugModel;

    @SuppressLint("ClickableViewAccessibility")
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_catalogue, container, false);

        searchBarEditText = root.findViewById(R.id.catalogue_search_bar);
        microphoneButton = root.findViewById(R.id.catalogue_microphone_image);

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
                microphoneButton.setImageResource(R.drawable.ic_baseline_mic_24);
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
        microphoneButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                Toast.makeText(getContext(), "TESTING MIC!!!", Toast.LENGTH_SHORT).show();

                if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    speechRecognizer.stopListening();
                }
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN){
                    microphoneButton.setImageResource(R.drawable.ic_baseline_map_24);
                    speechRecognizer.startListening(speechRecognizerIntent);
                }
                return false;
            }
        });

        drugModel = new DrugModel();

        recyclerView = (RecyclerView) root.findViewById(R.id.catalogue_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setHasFixedSize(true);

        drugReference = FirebaseDatabase.getInstance().getReference().child(Common.DRUG_REF);
        pharmacyReference = FirebaseDatabase.getInstance().getReference().child(Common.PHARMACY_REF);

        root.findViewById(R.id.add_pharmacy_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), AddPharmacyActivity.class);
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


    /**
     * Loads pharmacy first.
     * @param data
     */

    private void loadData(String data) {
        Query query = pharmacyReference.orderByChild("pharmacyName").startAt(data).endAt(data + "\uf8ff");

        options = new FirebaseRecyclerOptions.Builder<PharmacyModel>()
                .setQuery(query, PharmacyModel.class)
                .build();

        adapter = new FirebaseRecyclerAdapter<PharmacyModel, PharmacyViewHolder>(options) {
            @NonNull
            @Override
            public PharmacyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_view_catalogue_pharmacy, parent, false);
                return new PharmacyViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull PharmacyViewHolder holder, int position, @NonNull PharmacyModel model) {

                final String myPharmacyId = model.getPharmacyId();
                final String myPharmacyName = model.getPharmacyName();
                final String myPharmacyLocation = model.getPharmacyLocation();

                holder.pharmacyName.setText(myPharmacyName);
                holder.pharmacyLocation.setText(myPharmacyLocation);

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(getActivity(), PharmacyDetailedActivity.class);
                        try {
                            intent.putExtra("pharmacyId", myPharmacyId);
                            Log.d(TAG, "Pharmacy ID: " + myPharmacyId);
                            startActivity(intent);
                        } catch (NullPointerException e) {
                            Toast.makeText(getContext(), "E: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
            };

        adapter.startListening();
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
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