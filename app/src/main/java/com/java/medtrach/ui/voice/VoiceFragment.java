package com.java.medtrach.ui.voice;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
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
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;
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
import com.java.medtrach.model.DrugModel;
import com.java.medtrach.model.PharmacyModel;
import com.java.medtrach.ui.drug.DrugsViewHolder;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class VoiceFragment extends Fragment {
    private static final String TAG = VoiceFragment.class.getName();
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

    private EditText searchBarEditText;
    private ImageView microphoneImageView;

    private FusedLocationProviderClient fusedLocationClient;
    private Double myLatitude, myLongitude;
    private LatLng myLatLng, pharmacyLatLng;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        //Initialize Views and IDs
        View root = inflater.inflate(R.layout.fragment_voice, container, false);
        searchBarEditText = root.findViewById(R.id.searchViewEditText);
        microphoneImageView = root.findViewById(R.id.microphoneImageView);

        pharmacyModel = new PharmacyModel();

        //Initialize adapter
        recyclerView = (RecyclerView) root.findViewById(R.id.resultRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setHasFixedSize(true);

        //Initialize Firebase
        pharmacyReference = FirebaseDatabase.getInstance().getReference(Common.PHARMACY_REF);
        drugReference = FirebaseDatabase.getInstance().getReference(Common.DRUG_REF);


//        checkPermission();
//        if(ContextCompat.checkSelfPermission(getContext(), Manifest.permission.RECORD_AUDIO)
//                != PackageManager.PERMISSION_GRANTED){
//            checkPermission();
//        }

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
                    Log.d(TAG, "onTouch: Stop listening.");
                }
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN){
                    microphoneImageView.setImageResource(R.drawable.ic_baseline_mic_24);
                    speechRecognizer.startListening(speechRecognizerIntent);
                    Log.d(TAG, "onTouch: Start listening.");
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
                    adapter.startListening();
                } else {
                    loadData("");
                    adapter.startListening();
                }
            }
        });
        return root;
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

//        if (requestCode == RecordAudioRequestCode && grantResults.length > 0 ){
//            if(grantResults[0] == PackageManager.PERMISSION_GRANTED)
//                Toast.makeText(getContext(),"Permission Granted", Toast.LENGTH_SHORT).show();
//        }
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
                        String convertedDistance = String.format("%.2f", distance).toLowerCase();

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
//        adapter.startListening();
        recyclerView.setAdapter(adapter);
    }
//
//    private void checkPermission() {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            ActivityCompat.requestPermissions(getActivity(), new String[]{
//                            Manifest.permission.ACCESS_FINE_LOCATION,
//                            Manifest.permission.ACCESS_COARSE_LOCATION,
//                            Manifest.permission.ACCESS_NETWORK_STATE,
//                            Manifest.permission.RECORD_AUDIO},
//                    1);
//            Toast.makeText(getContext(), "PERMISSION GRANTED", Toast.LENGTH_SHORT).show();
////            adapter.notifyDataSetChanged();
//        }
//
//        return;
//    }

    @Override
    public void onStart() {
//        adapter.startListening();
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
