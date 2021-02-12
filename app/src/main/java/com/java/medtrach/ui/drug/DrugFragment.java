package com.java.medtrach.ui.drug;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
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
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.annotations.NotNull;
import com.google.maps.android.SphericalUtil;
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

public class DrugFragment extends Fragment {
    private static final String TAG = DrugFragment.class.getName();
    public static final Integer RecordAudioRequestCode = 1;
    private int LOCATION_REQUEST_CODE = 10001;

    //RecyclerView
    private PharmacyModel pharmacyModel;
    private List<DrugModel> drugModelList;

    private RecyclerView recyclerView;

    //Firebase
    DatabaseReference pharmacyReference;
    DatabaseReference drugReference;

    FirebaseRecyclerAdapter<DrugModel, DrugsViewHolder> adapter;
    FirebaseRecyclerOptions<DrugModel> options;

    private SpeechRecognizer speechRecognizer;

    private TextView pharmacyName, pharmacyLocation;
    private EditText searchBarEditText;
    private ImageView microphoneImageView;

    private FusedLocationProviderClient fusedLocationClient;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationRequest locationRequest;
    private Double myLatitude, myLongitude;
    private LatLng myLatLng, pharmacyLatLng;

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


        fusedLocationClient = LocationServices.getFusedLocationProviderClient(getContext());
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if(location != null) {
                            myLongitude = location.getLongitude();
                            myLatitude = location.getLatitude();

                            myLatLng = new LatLng(myLatitude, myLongitude);

                            Log.d(TAG, "onSuccess: " + myLongitude);
                            Log.d(TAG, "onSuccess: " + myLatitude);
                        }
                    }
                });

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(getContext());


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
            protected void onBindViewHolder(@NonNull DrugsViewHolder holder, final int position, @NonNull DrugModel model) {
                final String myDrugId = model.getDrugId().toString();
                final Double myPharmacyLatitude = model.getDrugPharmacyLatitude();
                final Double myPharmacyLongitude = model.getDrugPharmacyLongitude();

                Log.d(TAG, "onBindViewHolder: Pharmacy Latitude: " + myPharmacyLatitude);
                Log.d(TAG, "onBindViewHolder: Pharmacy Longitude: " + myPharmacyLongitude);

                holder.drugName.setText(model.getDrugName());
                holder.drugDescription.setText(model.getDrugDescription());
                holder.pharmacyName.setText(model.getDrugPharmacyName());
                holder.pharmacyLocation.setText(model.getDrugPharmacyLocation());

                drugReference.child(myDrugId).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {

                        Log.d(TAG, "onDataChange: Drug ID " +  myDrugId);
                        final String pharmacyId = snapshot.child("drugPharmacyId").getValue().toString();
                        final String pharmacyName = snapshot.child("drugPharmacyName").getValue().toString();
                        final String pharmacyLocation = snapshot.child("drugPharmacyLocation").getValue().toString();

                        pharmacyLatLng = new LatLng(myPharmacyLatitude, myPharmacyLongitude);
                        double distance = SphericalUtil.computeDistanceBetween(myLatLng, pharmacyLatLng) / 1000;
                        String convertedDistance = String.format("%.2f", distance).toLowerCase()    ;

                        Log.d(TAG, "onDataChange: Distance " + distance);
                        holder.pharmacyDistance.setText(convertedDistance + "km");

                        holder.itemView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent intent = new Intent(getActivity(), MapsActivity.class);
                                try {
                                    intent.putExtra("pharmacyId", pharmacyId);
                                    intent.putExtra("pharmacyName", pharmacyName);
                                    intent.putExtra("pharmacyLocation", pharmacyLocation);
                                    intent.putExtra("pharmacyLatitude", myPharmacyLatitude);
                                    intent.putExtra("pharmacyLongitude", myPharmacyLongitude);
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
    }

    private void checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ActivityCompat.requestPermissions(getActivity(),new String[]{Manifest.permission.RECORD_AUDIO},RecordAudioRequestCode);
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
